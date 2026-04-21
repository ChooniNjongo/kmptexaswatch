import SwiftUI

// ── Settings Screen ───────────────────────────────────────────────────────────
// Uses native iOS inline navigation title.

struct SettingsView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        VStack {
            Spacer()
            Text("Settings coming soon")
                .font(typography.text1)
                .foregroundColor(colors.secondaryText)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.mainBackground)
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
    }
}
