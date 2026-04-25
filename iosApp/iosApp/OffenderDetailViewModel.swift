import Shared
import SwiftUI

@MainActor
final class OffenderDetailViewModel: ObservableObject {
    @Published var detail: OffenderDetail? = nil
    @Published var isLoading = true
    @Published var error: String? = nil

    private let indIdn: Int
    private let helper = TexasWatchHelper()

    init(indIdn: Int) {
        self.indIdn = indIdn
        load()
    }

    func load() {
        isLoading = true
        error = nil
        Task {
            do {
                let result = try await helper.getOffenderDetail(indIdn: Int32(indIdn))
                self.detail = result
                self.isLoading = false
            } catch {
                self.error = error.localizedDescription
                self.isLoading = false
            }
        }
    }
}
