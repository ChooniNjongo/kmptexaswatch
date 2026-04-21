import SwiftUI

// ── Map Screen ────────────────────────────────────────────────────────────────
// Uses native iOS inline navigation title.

struct MapView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        VStack {
            Spacer()
            Text("Map coming soon here")
                .font(typography.text1)
                .foregroundColor(colors.secondaryText)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.mainBackground)
        .navigationTitle("Map")
        .navigationBarTitleDisplayMode(.inline)
    }
}
