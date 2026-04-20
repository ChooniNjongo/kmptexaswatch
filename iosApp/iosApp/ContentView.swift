import SwiftUI
import Shared

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel

    var body: some View {
        NavigationView {
            listView()
                .navigationTitle("TX Sex Offender Registry")
                .navigationBarItems(trailing:
                    Button("Reload") {
                        viewModel.loadOffenders(forceReload: true)
                    }
                )
        }
    }

    private func listView() -> AnyView {
        switch viewModel.state {
        case .loading:
            return AnyView(ProgressView("Loading..."))
        case .error(let msg):
            return AnyView(Text("Error: \(msg)").foregroundColor(.red).padding())
        case .result(let offenders):
            return AnyView(
                List(offenders, id: \.indIdn) { offender in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(offender.fullName).fontWeight(.bold)
                        Text("DPS: \(offender.dpsNumber)").font(.caption).foregroundColor(.secondary)
                        if let age = offender.age {
                            Text("Age: \(age)").font(.caption).foregroundColor(.secondary)
                        }
                        if let address = offender.address {
                            Text(address).font(.caption).foregroundColor(.gray)
                        }
                    }
                    .padding(.vertical, 4)
                }
            )
        }
    }
}

extension ContentView {
    enum ViewState {
        case loading
        case result([OffenderSummary])
        case error(String)
    }

    @MainActor
    class ViewModel: ObservableObject {
        @Published var state = ViewState.loading
        let helper = TexasWatchHelper()

        init() {
            loadOffenders(forceReload: false)
        }

        func loadOffenders(forceReload: Bool) {
            Task {
                self.state = .loading
                do {
                    let offenders = try await helper.getOffenders(forceReload: forceReload)
                    self.state = .result(offenders)
                } catch {
                    self.state = .error(error.localizedDescription)
                }
            }
        }
    }
}
