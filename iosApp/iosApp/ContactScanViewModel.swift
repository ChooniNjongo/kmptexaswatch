import Contacts
import Shared
import SwiftUI

// ── Scan state ────────────────────────────────────────────────────────────────

enum ContactScanStep {
    case idle
    case loadingContacts
    case scanning(progress: Int, total: Int)
    case done(results: [ContactMatchResult], totalMatches: Int, contactsWithMatches: Int)
    case error(String)
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@MainActor
final class ContactScanViewModel: ObservableObject {

    @Published var step: ContactScanStep = .idle
    @Published var contactsPermission: CNAuthorizationStatus = CNContactStore.authorizationStatus(for: .contacts)

    private let helper = TexasWatchHelper()
    private let store = CNContactStore()

    func requestPermissionAndScan() {
        let status = CNContactStore.authorizationStatus(for: .contacts)
        switch status {
        case .authorized:
            startScan()
        case .notDetermined:
            Task {
                do {
                    let granted = try await store.requestAccess(for: .contacts)
                    contactsPermission = CNContactStore.authorizationStatus(for: .contacts)
                    if granted { startScan() }
                } catch {
                    step = .error("Contacts permission denied")
                }
            }
        default:
            step = .error("Contacts access denied. Please enable it in Settings → Privacy → Contacts.")
        }
    }

    private func startScan() {
        Task {
            step = .loadingContacts
            do {
                let names = try loadContactNames()
                if names.isEmpty {
                    step = .done(results: [], totalMatches: 0, contactsWithMatches: 0)
                    return
                }

                var allResults: [ContactMatchResult] = []
                var totalMatches = 0
                var contactsWithMatches = 0
                let chunks = stride(from: 0, to: names.count, by: 20).map {
                    Array(names[$0..<min($0 + 20, names.count)])
                }

                for (i, chunk) in chunks.enumerated() {
                    step = .scanning(progress: i * 20, total: names.count)
                    let response = try await helper.searchByContacts(names: chunk)
                    allResults += response.results
                    totalMatches += Int(response.totalMatches)
                    contactsWithMatches += Int(response.contactsWithMatches)
                }

                step = .done(results: allResults, totalMatches: totalMatches, contactsWithMatches: contactsWithMatches)
            } catch {
                step = .error(error.localizedDescription)
            }
        }
    }

    private func loadContactNames() throws -> [String] {
        let keys = [CNContactGivenNameKey, CNContactFamilyNameKey] as [CNKeyDescriptor]
        let request = CNContactFetchRequest(keysToFetch: keys)
        var names: [String] = []
        try store.enumerateContacts(with: request) { contact, _ in
            let first = contact.givenName.trimmingCharacters(in: .whitespaces)
            let last = contact.familyName.trimmingCharacters(in: .whitespaces)
            let full: String
            if !first.isEmpty && !last.isEmpty {
                full = "\(first) \(last)"
            } else if !last.isEmpty {
                full = last
            } else if !first.isEmpty {
                full = first
            } else {
                return
            }
            names.append(full)
        }
        return Array(Set(names)).sorted()
    }
}
