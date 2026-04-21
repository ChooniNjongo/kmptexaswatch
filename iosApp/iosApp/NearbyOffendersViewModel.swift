import CoreLocation
import Shared
import SwiftUI

@MainActor
final class NearbyOffendersViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {

    @Published var count: Int = 0
    @Published var offenders: [OffenderSummary] = []
    @Published var isLoading: Bool = false
    @Published var isListLoading: Bool = false
    @Published var isLoadingMore: Bool = false
    @Published var locationGranted: Bool = false
    @Published var radiusMiles: Double = 5.0
    @Published var userLat: Double? = nil
    @Published var userLon: Double? = nil
    @Published var currentPage: Int = 0
    @Published var totalPages: Int = 0
    @Published var error: String? = nil

    private let manager = CLLocationManager()
    private let helper = TexasWatchHelper()
    private var activeTask: Task<Void, Never>? = nil

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        checkCurrentAuth()
    }

    func requestLocation() {
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .denied, .restricted:
            break
        default:
            fetchPage(page: 0, resetList: true)
        }
    }

    func onRadiusChange(_ miles: Double) {
        radiusMiles = miles
        if locationGranted {
            offenders = []
            isListLoading = true
            fetchPage(page: 0, resetList: true)
        }
    }

    func loadNextPage() {
        guard !isLoadingMore, !isListLoading, currentPage + 1 < totalPages else { return }
        fetchPage(page: currentPage + 1, resetList: false)
    }

    // MARK: - CLLocationManagerDelegate

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        Task { @MainActor in
            let granted = status == .authorizedWhenInUse || status == .authorizedAlways
            self.locationGranted = granted
            if granted {
                self.isLoading = true
                self.isListLoading = true
                self.fetchPage(page: 0, resetList: true)
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager,
                                     didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.first else { return }
        let lat = loc.coordinate.latitude
        let lon = loc.coordinate.longitude
        Task { @MainActor in
            self.userLat = lat
            self.userLon = lon
            activeTask?.cancel()
            activeTask = Task {
                await self.runPage(lat: lat, lon: lon, page: self.pendingPage, resetList: self.pendingResetList)
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager,
                                     didFailWithError error: Error) {
        Task { @MainActor in
            self.error = error.localizedDescription
            self.isLoading = false
            self.isListLoading = false
        }
    }

    // MARK: - Private

    private var pendingPage: Int = 0
    private var pendingResetList: Bool = true

    private func checkCurrentAuth() {
        let status = manager.authorizationStatus
        locationGranted = status == .authorizedWhenInUse || status == .authorizedAlways
        if locationGranted {
            isLoading = true
            isListLoading = true
            fetchPage(page: 0, resetList: true)
        }
    }

    private func fetchPage(page: Int, resetList: Bool) {
        pendingPage = page
        pendingResetList = resetList

        // If we already have location, run immediately — no need to re-request
        if let lat = userLat, let lon = userLon {
            activeTask?.cancel()
            activeTask = Task {
                await runPage(lat: lat, lon: lon, page: page, resetList: resetList)
            }
        } else {
            if page == 0 { isLoading = true; isListLoading = true }
            manager.requestLocation()
        }
    }

    private func runPage(lat: Double, lon: Double, page: Int, resetList: Bool) async {
        guard !Task.isCancelled else { return }

        if page == 0 {
            isLoading = true
            isListLoading = true
            error = nil
        } else {
            isLoadingMore = true
        }

        do {
            let result = try await helper.getOffendersPage(
                lat: lat, lon: lon,
                radiusMiles: radiusMiles,
                page: Int32(page),
                size: Int32(20)
            )
            guard !Task.isCancelled else { return }
            let newOffenders = result.offenders
            offenders = resetList ? newOffenders : offenders + newOffenders
            count = Int(result.totalCount)
            currentPage = page
            totalPages = Int(result.totalPages)
            userLat = lat
            userLon = lon
        } catch {
            guard !Task.isCancelled else { return }
            print("[NearbyVM] ERROR: \(error)")
            self.error = error.localizedDescription
        }
        isLoading = false
        isListLoading = false
        isLoadingMore = false
    }
}
