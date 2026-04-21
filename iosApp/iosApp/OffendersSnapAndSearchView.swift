import SwiftUI

// ── Offenders Snap & Search Screen ───────────────────────────────────────────

struct OffendersSnapAndSearchView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    @StateObject private var viewModel = NearbyOffendersViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {

                // ── Nearby Offenders Card ─────────────────────────────────────
                NearbyOffendersCard(
                    count: viewModel.count,
                    progress: viewModel.isLoading ? 0 : (viewModel.locationGranted ? 1.0 : 0),
                    locationActive: viewModel.locationGranted,
                    isLoading: viewModel.isLoading,
                    onAllowLocation: {
                        if viewModel.locationGranted == false {
                            viewModel.requestLocation()
                        }
                    }
                )

                // ── Radius Slider ─────────────────────────────────────────────
                if viewModel.locationGranted {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(String(format: "%.1f mi radius", viewModel.radiusMiles))
                            .font(typography.text2)
                            .foregroundColor(colors.secondaryText)

                        Slider(
                            value: Binding(
                                get: { viewModel.radiusMiles },
                                set: { viewModel.onRadiusChange($0) }
                            ),
                            in: 0.5...5.0,
                            step: 0.5
                        )
                        .tint(colors.ringActive)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
                    .padding(.bottom, 16)
                }

                // Error
                if let error = viewModel.error {
                    Text(error)
                        .font(typography.text2)
                        .foregroundColor(colors.dangerText)
                        .padding()
                }
            }
        }
        .background(colors.mainBackground)
        .navigationTitle("Offenders")
        .navigationBarTitleDisplayMode(.large)
    }
}
