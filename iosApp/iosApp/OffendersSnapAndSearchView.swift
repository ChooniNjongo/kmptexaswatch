import SwiftUI

struct OffendersSnapAndSearchView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    @StateObject private var viewModel = NearbyOffendersViewModel()

    var body: some View {
        List {
            // ── Nearby Offenders Card ─────────────────────────────────────────
            Section {
                NearbyOffendersCard(
                    count: viewModel.count,
                    locationActive: viewModel.locationGranted,
                    isLoading: viewModel.isLoading,
                    onAllowLocation: { viewModel.requestLocation() }
                )
                .listRowInsets(EdgeInsets())
                .listRowBackground(colors.mainBackground)
            }

            // ── Radius Slider ─────────────────────────────────────────────────
            if viewModel.locationGranted {
                Section {
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
                    .padding(.vertical, 4)
                }
                .listRowBackground(colors.mainBackground)

                // ── Closest Offenders section ─────────────────────────────────
                Section {
                    if viewModel.isListLoading {
                        ForEach(0..<8, id: \.self) { _ in
                            ShimmerOffenderCard()
                                .listRowInsets(EdgeInsets())
                                .listRowSeparator(.hidden)
                                .listRowBackground(colors.mainBackground)
                        }
                    } else {
                        ForEach(viewModel.offenders, id: \.indIdn) { offender in
                            OffenderCard(offender: offender)
                                .listRowInsets(EdgeInsets())
                                .listRowSeparator(.hidden)
                                .listRowBackground(colors.mainBackground)
                        }
                    }
                } header: {
                    Text("Closest Offenders")
                        .font(typography.h4)
                        .foregroundColor(colors.primaryText)
                        .textCase(nil)
                }
            }

            // Error
            if let error = viewModel.error {
                Section {
                    Text(error)
                        .font(typography.text2)
                        .foregroundColor(colors.dangerText)
                }
                .listRowBackground(colors.mainBackground)
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .background(colors.mainBackground)
        .navigationTitle("Offenders")
        .navigationBarTitleDisplayMode(.large)
    }
}
