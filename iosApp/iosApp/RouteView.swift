import SwiftUI

struct RouteView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        VStack {
            Spacer()
            Text("Route coming soon")
                .font(typography.text1)
                .foregroundColor(colors.secondaryText)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.mainBackground)
        .navigationTitle("Route")
        .navigationBarTitleDisplayMode(.large)
    }
}
