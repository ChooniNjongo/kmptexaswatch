import SwiftUI
import CoreLocation
import UserNotifications

// ── Screen 2: Notifications + Location permissions ────────────────────────────

struct OnboardingNotificationsView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var onDone: () -> Void

    @State private var nearbyAlerts = false
    @State private var locationAlerts = false

    @StateObject private var locationDelegate = LocationPermissionDelegate()

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    OnboardingHeroIcon(colors: colors)

                    Text("Stay Informed")
                        .font(typography.h1)
                        .foregroundColor(colors.primaryText)
                        .accessibilityAddTraits(.isHeader)

                    Text("Grant permissions so we can alert you about offenders nearby and keep you up to date.")
                        .font(typography.text1)
                        .foregroundColor(colors.secondaryText)

                    VStack(spacing: 8) {
                        // Nearby Alerts → notification permission
                        OnboardingToggleItem(
                            title: "Nearby Alerts",
                            description: "Get notified when a new offender is registered near you.",
                            isOn: $nearbyAlerts,
                            colors: colors,
                            typography: typography,
                            onToggle: { enabled in
                                if enabled {
                                    requestNotificationPermission { granted in
                                        nearbyAlerts = granted
                                    }
                                } else {
                                    nearbyAlerts = false
                                }
                            }
                        )
                        // Location Alerts → location permission
                        OnboardingToggleItem(
                            title: "Location Alerts",
                            description: "Allow location access to find offenders near you.",
                            isOn: $locationAlerts,
                            colors: colors,
                            typography: typography,
                            onToggle: { enabled in
                                if enabled {
                                    locationDelegate.request { granted in
                                        locationAlerts = granted
                                    }
                                } else {
                                    locationAlerts = false
                                }
                            }
                        )
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 24)
            }

            Spacer()

            Divider().background(colors.strokePale)
            HStack {
                OnboardingButton(
                    label: "Let's get started",
                    primary: true,
                    colors: colors,
                    typography: typography,
                    action: onDone
                )
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 16)
        }
        .background(colors.mainBackground.ignoresSafeArea())
    }

    // ── Notification permission ───────────────────────────────────────────────
    private func requestNotificationPermission(completion: @escaping (Bool) -> Void) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            DispatchQueue.main.async {
                completion(granted)
            }
        }
    }
}

// ── Location permission delegate ─────────────────────────────────────────────
// CLLocationManager requires a delegate object; we wrap it in an ObservableObject.

class LocationPermissionDelegate: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var completion: ((Bool) -> Void)?

    override init() {
        super.init()
        manager.delegate = self
    }

    func request(completion: @escaping (Bool) -> Void) {
        let status = manager.authorizationStatus
        switch status {
        case .authorizedWhenInUse, .authorizedAlways:
            completion(true)
        case .denied, .restricted:
            completion(false)
        case .notDetermined:
            self.completion = completion
            manager.requestWhenInUseAuthorization()
        @unknown default:
            completion(false)
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard let completion else { return }
        switch manager.authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways:
            completion(true)
        default:
            completion(false)
        }
        self.completion = nil
    }
}
