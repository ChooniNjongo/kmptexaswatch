import SwiftUI

// ── Main Header Bar ───────────────────────────────────────────────────────────
// iOS equivalent of Android's MainHeaderTitleBar (48dp):
//   - Fixed 48pt height
//   - Centered title using h4 typography
//   - Optional leading and trailing action slots

struct MainHeaderBar: View {
    let title: String
    var leadingContent: AnyView? = nil
    var trailingContent: AnyView? = nil

    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        ZStack {
            // Background
            colors.mainBackground

            // Centered title
            Text(title)
                .font(typography.h4)
                .foregroundColor(colors.primaryText)

            // Leading / trailing slots
            HStack {
                if let leading = leadingContent {
                    leading
                }
                Spacer()
                if let trailing = trailingContent {
                    trailing
                }
            }
            .padding(.horizontal, 16)
        }
        .frame(height: 48)
    }
}
