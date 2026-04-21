import SwiftUI
import Shared

// ── Offender List Screen ──────────────────────────────────────────────────────

struct OffenderListView: View {
    @StateObject private var viewModel = OffenderListViewModel()
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView("Loading…")
                    .tint(colors.primaryAccent)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(colors.mainBackground)
            } else if let error = viewModel.error {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(colors.dangerText)
                    Text(error)
                        .font(typography.text1)
                        .foregroundStyle(colors.dangerText)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(colors.mainBackground)
            } else {
                List(viewModel.offenders, id: \.indIdn) { offender in
                    OffenderCard(offender: offender)
                        .listRowInsets(EdgeInsets(top: 0, leading: 0, bottom: 0, trailing: 0))
                        .listRowSeparator(.visible)
                        .listRowBackground(colors.mainBackground)
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
                .background(colors.mainBackground)
                .refreshable {
                    await viewModel.load(forceReload: true)
                }
            }
        }
        .navigationTitle("Sex Offender Registry")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(colors.primaryAccent, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            await viewModel.load(forceReload: false)
        }
    }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@MainActor
class OffenderListViewModel: ObservableObject {
    @Published var offenders: [OffenderSummary] = []
    @Published var isLoading = false
    @Published var error: String? = nil

    private let helper = TexasWatchHelper()

    func load(forceReload: Bool) async {
        isLoading = true
        error = nil
        do {
            let result = try await helper.getOffenders(forceReload: forceReload)
            offenders = result
        } catch {
            self.error = error.localizedDescription
        }
        isLoading = false
    }
}
