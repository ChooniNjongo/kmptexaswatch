import SwiftUI

// ── Native iOS Root Navigation ────────────────────────────────────────────────
// Mirrors Android AppNavigation: TabView with 3 tabs —
//   Offenders · Map · Settings
// Each tab wraps its content in a NavigationStack for push navigation.

struct HomeView: View {
    @Environment(\.twColors) private var colors
    @State private var selectedTab: Tab = .offenders

    enum Tab {
        case offenders, map, settings
    }

    var body: some View {
        TabView(selection: $selectedTab.animation(.easeInOut(duration: 0.25))) {

            // ── Offenders tab ────────────────────────────────────────────────
            NavigationStack {
                OffendersSnapAndSearchView()
            }
            .tabItem {
                Label(
                    "Offenders",
                    systemImage: selectedTab == .offenders
                        ? "person.2.fill"
                        : "person.2"
                )
            }
            .tag(Tab.offenders)

            // ── Map tab ──────────────────────────────────────────────────────
            NavigationStack {
                MapView()
            }
            .tabItem {
                Label(
                    "Map",
                    systemImage: selectedTab == .map
                        ? "map.fill"
                        : "map"
                )
            }
            .tag(Tab.map)

            // ── Settings tab ─────────────────────────────────────────────────
            NavigationStack {
                SettingsView()
            }
            .tabItem {
                Label(
                    "Settings",
                    systemImage: selectedTab == .settings
                        ? "info.circle.fill"
                        : "info.circle"
                )
            }
            .tag(Tab.settings)
        }
        .tint(colors.primaryAccent)
    }
}
