import Combine
import CoreLocation
import MapKit
import Shared
import SwiftUI

// ── Map pin model ─────────────────────────────────────────────────────────────

struct OffenderPin: Identifiable {
    let id: Int
    let coordinate: CLLocationCoordinate2D
    let photoUrl: String?
    let fullName: String
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@MainActor
final class MapViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {

    @Published var pins: [OffenderPin] = []
    @Published var isLoading = false
    @Published var error: String? = nil
    @Published var userLocation: CLLocationCoordinate2D? = nil
    @Published var locationGranted = false
    @Published var cameraRegion: MKCoordinateRegion = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 31.0, longitude: -100.0), // Texas center
        span: MKCoordinateSpan(latitudeDelta: 8.0, longitudeDelta: 8.0)
    )

    @Published var visibleRegion: MKCoordinateRegion? = nil

    func zoomIn() {
        let region = visibleRegion ?? cameraRegion
        cameraRegion = MKCoordinateRegion(
            center: region.center,
            span: MKCoordinateSpan(
                latitudeDelta: max(region.span.latitudeDelta / 2, 0.001),
                longitudeDelta: max(region.span.longitudeDelta / 2, 0.001)
            )
        )
    }

    func zoomOut() {
        let region = visibleRegion ?? cameraRegion
        cameraRegion = MKCoordinateRegion(
            center: region.center,
            span: MKCoordinateSpan(
                latitudeDelta: min(region.span.latitudeDelta * 2, 90.0),
                longitudeDelta: min(region.span.longitudeDelta * 2, 90.0)
            )
        )
    }

    private let helper = TexasWatchHelper()
    private let locationManager = CLLocationManager()
    private var loadTask: Task<Void, Never>? = nil
    private var lastLoadedCenter: CLLocationCoordinate2D? = nil

    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    func requestLocation() {
        locationManager.requestWhenInUseAuthorization()
    }

    func loadPins(for coordinate: CLLocationCoordinate2D) {
        // Skip if we already loaded for a very nearby center (< 0.5 mi)
        if let last = lastLoadedCenter, distance(last, coordinate) < 0.5 { return }
        lastLoadedCenter = coordinate
        loadTask?.cancel()
        loadTask = Task {
            guard !Task.isCancelled else { return }
            isLoading = true
            error = nil
            do {
                let response = try await helper.getOffendersForMap(
                    lat: coordinate.latitude,
                    lon: coordinate.longitude,
                    radiusMiles: 5.0,
                    page: 0,
                    size: 50
                )
                guard !Task.isCancelled else { return }
                pins = response.content.compactMap { offender in
                    guard let lat = offender.latitude?.doubleValue,
                          let lon = offender.longitude?.doubleValue else { return nil }
                    return OffenderPin(
                        id: Int(offender.indIdn),
                        coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lon),
                        photoUrl: offender.photoUrl,
                        fullName: offender.fullName
                    )
                }
            } catch {
                guard !Task.isCancelled else { return }
                self.error = (error as NSError).localizedDescription
            }
            isLoading = false
        }
    }

    // MARK: - CLLocationManagerDelegate

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            switch manager.authorizationStatus {
            case .authorizedWhenInUse, .authorizedAlways:
                locationGranted = true
                locationManager.requestLocation()
            default:
                locationGranted = false
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.last else { return }
        Task { @MainActor in
            userLocation = loc.coordinate
            cameraRegion = MKCoordinateRegion(
                center: loc.coordinate,
                span: MKCoordinateSpan(latitudeDelta: 0.08, longitudeDelta: 0.08)
            )
            loadPins(for: loc.coordinate)
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {}

    // MARK: - Helpers

    private func distance(_ a: CLLocationCoordinate2D, _ b: CLLocationCoordinate2D) -> Double {
        let loc1 = CLLocation(latitude: a.latitude, longitude: a.longitude)
        let loc2 = CLLocation(latitude: b.latitude, longitude: b.longitude)
        return loc1.distance(from: loc2) / 1609.34 // metres → miles
    }
}
