import CoreLocation
import Shared
import SwiftUI

private func haversine(userLat: Double?, userLon: Double?, offender: OffenderSummary) -> Double? {
    guard let uLat = userLat, let uLon = userLon,
          let oLatK = offender.lat, let oLonK = offender.lon else { return nil }
    let oLat = oLatK.doubleValue
    let oLon = oLonK.doubleValue
    let r = 3958.8
    let dLat = (oLat - uLat) * .pi / 180.0
    let dLon = (oLon - uLon) * .pi / 180.0
    let sinDLat = sin(dLat / 2)
    let sinDLon = sin(dLon / 2)
    let a = sinDLat * sinDLat + cos(uLat * .pi / 180.0) * cos(oLat * .pi / 180.0) * sinDLon * sinDLon
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private enum ScrollAnchor: Hashable { case top }

struct OffendersSnapAndSearchView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    @StateObject private var viewModel = NearbyOffendersViewModel()
    @State private var showBackToTop = false
    @State private var navigateToSearch = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            ScrollViewReader { proxy in
                List {
                    // ── Scroll anchor ─────────────────────────────────────────
                    Color.clear
                        .frame(height: 0)
                        .id(ScrollAnchor.top)
                        .onAppear  { withAnimation { showBackToTop = false } }
                        .onDisappear { withAnimation { showBackToTop = true  } }
                        .listRowInsets(EdgeInsets())
                        .listRowSeparator(.hidden)
                        .listRowBackground(colors.mainBackground)

                    // ── Nearby Offenders Card ─────────────────────────────────
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

                    // ── Radius Slider ─────────────────────────────────────────
                    if viewModel.locationGranted {
                        Section {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(String(format: "%.1f mi radius", viewModel.radiusMiles))
                                    .font(typography.text2)
                                    .foregroundColor(colors.secondaryText)
                                Slider(
                                    value: Binding(
                                        get: { viewModel.radiusMiles },
                                        set: { viewModel.onRadiusDrag($0) }
                                    ),
                                    in: 0.5...5.0,
                                    step: 0.5,
                                    onEditingChanged: { editing in
                                        if !editing { viewModel.onRadiusChangeFinished() }
                                    }
                                )
                                .tint(colors.ringActive)
                            }
                            .padding(.vertical, 4)
                        }
                        .listRowBackground(colors.mainBackground)

                        // ── Closest Offenders section ─────────────────────────
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
                                    OffenderCard(
                                        offender: offender,
                                        distanceMiles: haversine(userLat: viewModel.userLat, userLon: viewModel.userLon, offender: offender)
                                    )
                                    .listRowInsets(EdgeInsets())
                                    .listRowSeparator(.hidden)
                                    .listRowBackground(colors.mainBackground)
                                    .onAppear {
                                        // Trigger next page when within 5 items of the bottom
                                        if let last = viewModel.offenders.suffix(5).first,
                                           last.indIdn == offender.indIdn {
                                            viewModel.loadNextPage()
                                        }
                                    }
                                }
                                // ── Load-more spinner ─────────────────────────
                                if viewModel.isLoadingMore {
                                    HStack {
                                        Spacer()
                                        ProgressView()
                                            .tint(colors.ringActive)
                                            .padding(.vertical, 16)
                                        Spacer()
                                    }
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
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        NavigationLink(destination: SearchView()) {
                            Image(systemName: "magnifyingglass")
                                .foregroundStyle(colors.primaryText)
                        }
                    }
                }
                .refreshable {
                    await viewModel.refresh()
                }
                // ── Back-to-top FAB ───────────────────────────────────────────
                .overlay(alignment: .bottomTrailing) {
                    if showBackToTop {
                        Button {
                            withAnimation {
                                proxy.scrollTo(ScrollAnchor.top, anchor: .top)
                            }
                        } label: {
                            Image(systemName: "arrow.up")
                                .font(.system(size: 18, weight: .semibold))
                                .foregroundStyle(colors.invertedText)
                                .frame(width: 48, height: 48)
                                .background(colors.ringActive)
                                .clipShape(Circle())
                                .shadow(color: .black.opacity(0.25), radius: 6, x: 0, y: 3)
                        }
                        .padding(.bottom, 24)
                        .padding(.trailing, 16)
                        .transition(.scale.combined(with: .opacity))
                    }
                }
            }
        }
    }
}
