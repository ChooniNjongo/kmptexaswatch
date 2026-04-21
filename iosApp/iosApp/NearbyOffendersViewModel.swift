import CoreLocation
import Shared
import SwiftUI

@MainActor
final class NearbyOffendersViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {

    @Published var count: Int = 0
    @Published var isLoading: Bool = false
    @Published var locationGranted: Bool = false
    @Published var radiusMiles: Double = 5.0
    @Published var error: String? = nil

    private let manager = CLLocationManager()
    private let helper = TexasWatchHelper()

    private var cache: [String: (Int, Date)] = [:]
    private let cacheTtl: TimeInterval = 600

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        print("[NearbyVM] init — checking auth status")
        checkCurrentAuth()
    }

    func requestLocation() {
        print("[NearbyVM] requestLocation — status: \(manager.authorizationStatus.rawValue)")
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .denied, .restricted:
            print("[NearbyVM] location denied/restricted — open Settings")
        default:
            fetchNearby()
        }
    }

    func onRadiusChange(_ miles: Double) {
        print("[NearbyVM] onRadiusChange: \(miles) mi")
        radiusMiles = miles
        if locationGranted { fetchNearby() }
    }

    // MARK: - CLLocationManagerDelegate

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        print("[NearbyVM] authorizationChanged: \(status.rawValue)")
        Task { @MainActor in
            let granted = status == .authorizedWhenInUse || status == .authorizedAlways
            locationGranted = granted
            print("[NearbyVM] locationGranted = \(granted)")
            if granted { fetchNearby() }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager,
                                     didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.first else {
            print("[NearbyVM] didUpdateLocations: empty array")
            return
        }
        print("[NearbyVM] didUpdateLocations: lat=\(loc.coordinate.latitude) lon=\(loc.coordinate.longitude)")
        let lat = loc.coordinate.latitude
        let lon = loc.coordinate.longitude
        Task { @MainActor in
            await loadStats(lat: lat, lon: lon)
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager,
                                     didFailWithError error: Error) {
        print("[NearbyVM] locationManager didFail: \(error.localizedDescription)")
        Task { @MainActor in
            self.error = error.localizedDescription
            self.isLoading = false
        }
    }

    // MARK: - Private

    private func checkCurrentAuth() {
        let status = manager.authorizationStatus
        print("[NearbyVM] checkCurrentAuth: status=\(status.rawValue)")
        locationGranted = status == .authorizedWhenInUse || status == .authorizedAlways
        if locationGranted { fetchNearby() }
    }

    private func fetchNearby() {
        print("[NearbyVM] fetchNearby — calling requestLocation()")
        manager.requestLocation()
    }

    private func loadStats(lat: Double, lon: Double) async {
        let key = String(format: "%.3f,%.3f,%.1f", lat, lon, radiusMiles)
        print("[NearbyVM] loadStats: key=\(key)")

        if let cached = cache[key], Date().timeIntervalSince(cached.1) < cacheTtl {
            print("[NearbyVM] loadStats: cache hit count=\(cached.0)")
            count = cached.0
            return
        }

        isLoading = true
        error = nil
        print("[NearbyVM] loadStats: calling API lat=\(lat) lon=\(lon) radius=\(radiusMiles)")
        do {
            let stats = try await helper.getRiskStats(lat: lat, lon: lon, radiusMiles: radiusMiles)
            let total = Int(stats.lowAndModerateCount + stats.highRiskCount)
            print("[NearbyVM] loadStats: API success low=\(stats.lowAndModerateCount) high=\(stats.highRiskCount) total=\(total)")
            cache[key] = (total, Date())
            count = total
        } catch {
            print("[NearbyVM] loadStats: API ERROR \(error)")
            self.error = error.localizedDescription
        }
        isLoading = false
    }
}
