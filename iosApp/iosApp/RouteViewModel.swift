import Combine
import CoreLocation
import MapKit
import Shared
import SwiftUI

// ── Route state ───────────────────────────────────────────────────────────────

enum RouteStep {
    case idle
    case calculating
    case loadingPins
    case ready
    case error(String)
}

enum PickingMode { case start, end, none }

// ── ViewModel ─────────────────────────────────────────────────────────────────

@MainActor
final class RouteViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {

    @Published var step: RouteStep = .idle
    @Published var pickingMode: PickingMode = .start   // always start in "pick A" mode
    @Published var route: MKRoute? = nil
    @Published var pins: [OffenderPin] = []
    @Published var selectedPin: OffenderPin? = nil
    @Published var cameraPosition: MapCameraPosition = .region(MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 31.0, longitude: -100.0),
        span: MKCoordinateSpan(latitudeDelta: 8.0, longitudeDelta: 8.0)
    ))
    @Published var startCoord: CLLocationCoordinate2D? = nil
    @Published var endCoord: CLLocationCoordinate2D? = nil
    @Published var userLocation: CLLocationCoordinate2D? = nil

    private let helper = TexasWatchHelper()
    private let locationManager = CLLocationManager()
    private var routeTask: Task<Void, Never>? = nil
    private var generation: Int = 0  // incremented on every clear; stale tasks bail out early

    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    func requestLocation() {
        locationManager.requestWhenInUseAuthorization()
    }

    // MARK: - Pin control actions

    func rePickStart() {
        pickingMode = .start
    }

    func rePickEnd() {
        pickingMode = .end
    }

    /// Places whichever pin is currently being picked at the user's current location.
    func placeMyLocation() {
        guard let loc = userLocation else { return }
        place(coord: loc)
    }

    // MARK: - Tap interaction

    func tapCoordinate(_ coord: CLLocationCoordinate2D) {
        place(coord: coord)
    }

    private func place(coord: CLLocationCoordinate2D) {
        switch pickingMode {
        case .start:
            startCoord = coord
            endCoord = nil
            route = nil
            pins = []
            pickingMode = .end
        case .end:
            endCoord = coord
            guard let start = startCoord else { return }
            pickingMode = .none
            scheduleRoute(from: start, to: coord)
        case .none:
            // Tapping while calculating/ready: start over
            clearRoute()
        }
    }

    // MARK: - Zoom

    @Published var visibleRegion: MKCoordinateRegion? = nil

    func zoomIn() {
        let region = visibleRegion ?? MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 31.0, longitude: -100.0),
            span: MKCoordinateSpan(latitudeDelta: 8.0, longitudeDelta: 8.0)
        )
        cameraPosition = .region(MKCoordinateRegion(
            center: region.center,
            span: MKCoordinateSpan(
                latitudeDelta: max(region.span.latitudeDelta / 2, 0.001),
                longitudeDelta: max(region.span.longitudeDelta / 2, 0.001)
            )
        ))
    }

    func zoomOut() {
        let region = visibleRegion ?? MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 31.0, longitude: -100.0),
            span: MKCoordinateSpan(latitudeDelta: 8.0, longitudeDelta: 8.0)
        )
        cameraPosition = .region(MKCoordinateRegion(
            center: region.center,
            span: MKCoordinateSpan(
                latitudeDelta: min(region.span.latitudeDelta * 2, 90.0),
                longitudeDelta: min(region.span.longitudeDelta * 2, 90.0)
            )
        ))
    }

    func clearRoute() {
        generation += 1          // invalidate any in-flight task
        routeTask?.cancel()
        routeTask = nil
        route = nil
        pins = []
        startCoord = nil
        endCoord = nil
        step = .idle
        pickingMode = .start
        // Use last known region or user location — avoid .automatic which freezes the map on re-tap
        if let loc = userLocation {
            cameraPosition = .region(MKCoordinateRegion(
                center: loc,
                span: MKCoordinateSpan(latitudeDelta: 0.15, longitudeDelta: 0.15)
            ))
        } else if let region = visibleRegion {
            cameraPosition = .region(region)
        }
    }

    // MARK: - Route calculation

    private func scheduleRoute(from start: CLLocationCoordinate2D, to end: CLLocationCoordinate2D) {
        routeTask?.cancel()
        let gen = generation
        routeTask = Task {
            await calculateRoute(
                from: MKMapItem(placemark: MKPlacemark(coordinate: start)),
                to:   MKMapItem(placemark: MKPlacemark(coordinate: end)),
                generation: gen
            )
        }
    }

    private func calculateRoute(from start: MKMapItem, to end: MKMapItem, generation gen: Int) async {
        guard generation == gen else { return }
        step = .calculating
        route = nil
        pins = []

        let request = MKDirections.Request()
        request.source = start
        request.destination = end
        request.transportType = .automobile

        do {
            let directions = MKDirections(request: request)
            let response = try await withTimeout(seconds: 15) {
                try await directions.calculate()
            }
            guard generation == gen else { return }
            guard let mkRoute = response.routes.first else {
                step = .error("No route found")
                pickingMode = .start
                return
            }
            route = mkRoute

            let rect = mkRoute.polyline.boundingMapRect
            cameraPosition = .rect(rect.insetBy(dx: -rect.width * 0.15, dy: -rect.height * 0.15))

            await loadOffendersAlongRoute(mkRoute, generation: gen)
        } catch {
            guard generation == gen else { return }
            step = .error(error.localizedDescription)
            pickingMode = .start
        }
    }

    // MARK: - Load offenders along route

    private func loadOffendersAlongRoute(_ mkRoute: MKRoute, generation gen: Int) async {
        guard generation == gen else { return }
        step = .loadingPins

        let samplePoints = samplePolyline(mkRoute.polyline, intervalMiles: 0.5)

        var seen = Set<Int>()
        var allPins: [OffenderPin] = []

        let batches = stride(from: 0, to: samplePoints.count, by: 8).map {
            Array(samplePoints[$0..<min($0 + 8, samplePoints.count)])
        }

        for batch in batches {
            guard generation == gen else { return }
            await withTaskGroup(of: [OffenderPin].self) { group in
                for coord in batch {
                    group.addTask { [weak self] in
                        guard let self else { return [] }
                        do {
                            let response = try await self.helper.getOffendersForMap(
                                lat: coord.latitude,
                                lon: coord.longitude,
                                radiusMiles: 0.5,
                                page: 0,
                                size: 50
                            )
                            return response.content.compactMap { o in
                                guard let lat = o.latitude?.doubleValue,
                                      let lon = o.longitude?.doubleValue else { return nil }
                                return OffenderPin(
                                    id: Int(o.indIdn),
                                    coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lon),
                                    photoUrl: o.photoUrl,
                                    fullName: o.fullName
                                )
                            }
                        } catch { return [] }
                    }
                }
                for await result in group {
                    for pin in result where seen.insert(pin.id).inserted {
                        allPins.append(pin)
                    }
                }
            }
        }

        guard generation == gen else { return }
        pins = allPins
        step = .ready
        pickingMode = .start
    }

    // MARK: - Polyline sampling

    private func samplePolyline(_ polyline: MKPolyline, intervalMiles: Double) -> [CLLocationCoordinate2D] {
        let points = polyline.points()
        let count = polyline.pointCount
        guard count > 0 else { return [] }

        var result: [CLLocationCoordinate2D] = []
        var accumulated: Double = 0
        let intervalMetres = intervalMiles * 1609.34

        result.append(points[0].coordinate)

        for i in 1..<count {
            let prev = CLLocation(coordinate: points[i - 1].coordinate)
            let curr = CLLocation(coordinate: points[i].coordinate)
            accumulated += prev.distance(from: curr)
            if accumulated >= intervalMetres {
                result.append(points[i].coordinate)
                accumulated = 0
            }
        }

        if let last = result.last {
            let end = points[count - 1].coordinate
            if CLLocation(coordinate: last).distance(from: CLLocation(coordinate: end)) > 100 {
                result.append(end)
            }
        }

        return result
    }

    // MARK: - CLLocationManagerDelegate

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            if manager.authorizationStatus == .authorizedWhenInUse ||
               manager.authorizationStatus == .authorizedAlways {
                manager.requestLocation()
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.last else { return }
        Task { @MainActor in
            userLocation = loc.coordinate
            if route == nil {
                cameraPosition = .region(MKCoordinateRegion(
                    center: loc.coordinate,
                    span: MKCoordinateSpan(latitudeDelta: 0.15, longitudeDelta: 0.15)
                ))
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {}
}

// MARK: - Helpers

private extension CLLocation {
    convenience init(coordinate: CLLocationCoordinate2D) {
        self.init(latitude: coordinate.latitude, longitude: coordinate.longitude)
    }
}

private func withTimeout<T: Sendable>(seconds: Double, operation: @escaping @Sendable () async throws -> T) async throws -> T {
    try await withThrowingTaskGroup(of: T.self) { group in
        group.addTask { try await operation() }
        group.addTask {
            try await Task.sleep(nanoseconds: UInt64(seconds * 1_000_000_000))
            throw URLError(.timedOut)
        }
        let result = try await group.next()!
        group.cancelAll()
        return result
    }
}
