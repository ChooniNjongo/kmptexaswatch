import SwiftUI

// ── Offenders Snap & Search Screen ───────────────────────────────────────────

struct OffendersSnapAndSearchView: View {
    @Environment(\.twColors) private var colors

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                NearbyOffendersCard(
                    count: 247,
                    progress: 1.0,
                    locationActive: true
                )
            }
        }
        .background(colors.mainBackground)
        .navigationTitle("Offenders")
        .navigationBarTitleDisplayMode(.large)
    }
}
