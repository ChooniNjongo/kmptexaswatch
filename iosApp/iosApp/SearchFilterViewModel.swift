import Shared
import SwiftUI

// ── Search criteria — single source of truth ─────────────────────────────────

struct SearchCriteria: Equatable {
    var name: String = ""
    var county: TexasCounty? = nil
    var riskLevels: Set<String> = []   // "1","2","3"
    var races: Set<String> = []        // "W","B","A","I","O"
    var hairColors: Set<String> = []   // "BLK","BRO","BLN","RED","GRY","WHI","BAL"
    var eyeColors: Set<String> = []    // "BRO","BLU","GRN","HAZ","GRY","BLK","MAR"

    var hasAnyFilter: Bool {
        !name.trimmingCharacters(in: .whitespaces).isEmpty ||
        county != nil ||
        !riskLevels.isEmpty || !races.isEmpty ||
        !hairColors.isEmpty || !eyeColors.isEmpty
    }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@MainActor
final class SearchFilterViewModel: ObservableObject {

    @Published var criteria = SearchCriteria()
    @Published var results: [OffenderSummary] = []
    @Published var isLoading = false
    @Published var isLoadingMore = false
    @Published var error: String? = nil
    @Published var totalResults: Int = 0

    private var currentPage = 0
    private var totalPages = 0
    private var searchTask: Task<Void, Never>? = nil
    private var debounceTask: Task<Void, Never>? = nil
    private let helper = TexasWatchHelper()

    // Called from name TextField onChange — debounced 400ms
    func onNameChanged(_ value: String) {
        criteria.name = value
        debounceTask?.cancel()
        debounceTask = Task {
            try? await Task.sleep(nanoseconds: 400_000_000)
            guard !Task.isCancelled else { return }
            triggerSearch()
        }
    }

    // Called when any filter chip or county changes — fires immediately
    func onFilterChanged() {
        debounceTask?.cancel()
        triggerSearch()
    }

    func clearAllFilters() {
        debounceTask?.cancel()
        criteria = SearchCriteria(name: criteria.name)
        triggerSearch()
    }

    func loadNextPage() {
        guard !isLoadingMore, !isLoading, currentPage + 1 < totalPages else { return }
        searchTask?.cancel()
        searchTask = Task { await runSearch(page: currentPage + 1, reset: false) }
    }

    // MARK: - Private

    private func triggerSearch() {
        searchTask?.cancel()
        searchTask = Task { await runSearch(page: 0, reset: true) }
    }

    private func runSearch(page: Int, reset: Bool) async {
        guard criteria.hasAnyFilter else {
            results = []
            totalResults = 0
            isLoading = false
            return
        }
        guard !Task.isCancelled else { return }

        if page == 0 { isLoading = true; error = nil }
        else { isLoadingMore = true }

        do {
            let response = try await helper.searchComprehensive(
                name: criteria.name.trimmed.nilIfEmpty,
                countyName: criteria.county?.name,
                riskLevels: criteria.riskLevels.arrayOrNil,
                races: criteria.races.arrayOrNil,
                hairColors: criteria.hairColors.arrayOrNil,
                eyeColors: criteria.eyeColors.arrayOrNil,
                page: Int32(page),
                size: Int32(20)
            )
            guard !Task.isCancelled else { return }
            results = reset ? response.content : results + response.content
            totalResults = Int(response.totalElements)
            currentPage = page
            totalPages = Int(response.totalPages)
        } catch {
            guard !Task.isCancelled else { return }
            self.error = (error as NSError).localizedDescription
        }

        isLoading = false
        isLoadingMore = false
    }
}

// MARK: - Helpers

private extension String {
    var trimmed: String { trimmingCharacters(in: .whitespaces) }
    var nilIfEmpty: String? { trimmed.isEmpty ? nil : self }
}

private extension Set {
    var arrayOrNil: [Element]? { isEmpty ? nil : Array(self) }
}
