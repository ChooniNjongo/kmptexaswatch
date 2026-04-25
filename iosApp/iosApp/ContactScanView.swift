import Shared
import SwiftUI

struct ContactScanView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    @StateObject private var vm = ContactScanViewModel()

    var body: some View {
        ZStack {
            colors.mainBackground.ignoresSafeArea()

            switch vm.step {
            case .idle:
                idleView

            case .loadingContacts:
                statusView(text: "Loading contacts…", loading: true)

            case .scanning(let progress, let total):
                scanningView(progress: progress, total: total)

            case .done(let results, let totalMatches, let contactsWithMatches):
                if results.isEmpty {
                    statusView(text: "No matches found in your contacts.", loading: false)
                } else {
                    resultsView(results: results, totalMatches: totalMatches, contactsWithMatches: contactsWithMatches)
                }

            case .error(let msg):
                statusView(text: msg, loading: false)
            }
        }
        .navigationTitle("Scan Contacts")
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - Idle

    private var idleView: some View {
        VStack(spacing: 24) {
            Spacer()
            Image(systemName: "person.crop.circle.badge.magnifyingglass")
                .font(.system(size: 56))
                .foregroundStyle(colors.primaryAccent)

            Text("Scan your contacts to check if any match registered sex offenders.")
                .font(typography.text1)
                .foregroundStyle(colors.secondaryText)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button {
                vm.requestPermissionAndScan()
            } label: {
                Text("Scan My Contacts")
                    .font(typography.h4)
                    .foregroundStyle(colors.invertedText)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(colors.primaryAccent, in: RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 32)

            Spacer()
        }
    }

    // MARK: - Scanning progress

    private func scanningView(progress: Int, total: Int) -> some View {
        VStack(spacing: 20) {
            Spacer()
            SpinningDiscLoader()
            Text("Scanning \(progress) / \(total) contacts…")
                .font(typography.text1)
                .foregroundStyle(colors.secondaryText)
            ProgressView(value: total > 0 ? Double(progress) / Double(total) : 0)
                .tint(colors.primaryAccent)
                .padding(.horizontal, 32)
            Spacer()
        }
    }

    // MARK: - Status

    private func statusView(text: String, loading: Bool) -> some View {
        VStack(spacing: 16) {
            Spacer()
            if loading { SpinningDiscLoader() }
            Text(text)
                .font(typography.text1)
                .foregroundStyle(colors.secondaryText)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Spacer()
        }
    }

    // MARK: - Results

    private func resultsView(results: [ContactMatchResult], totalMatches: Int, contactsWithMatches: Int) -> some View {
        ScrollView {
            LazyVStack(spacing: 0, pinnedViews: [.sectionHeaders]) {
                // Summary header
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("\(contactsWithMatches) contact\(contactsWithMatches == 1 ? "" : "s") matched")
                            .font(typography.h4)
                            .foregroundStyle(colors.primaryText)
                        Text("\(totalMatches) offender result\(totalMatches == 1 ? "" : "s") found")
                            .font(typography.text2)
                            .foregroundStyle(colors.secondaryText)
                    }
                    Spacer()
                }
                .padding(16)

                ForEach(results, id: \.contactName) { group in
                    Section {
                        ForEach(group.matches, id: \.indIdn) { offender in
                            NavigationLink(destination: OffenderDetailView(indIdn: Int(offender.indIdn), offenderName: offender.fullName)) {
                                OffenderCard(offender: offender)
                            }
                            .buttonStyle(.plain)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 2)
                        }
                    } header: {
                        HStack {
                            Text(group.contactName)
                                .font(typography.text1.weight(.semibold))
                                .foregroundStyle(colors.primaryText)
                            Spacer()
                            Text("\(group.matches.count) match\(group.matches.count == 1 ? "" : "es")")
                                .font(typography.label)
                                .foregroundStyle(colors.dangerText)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(colors.dangerBadge.opacity(0.12), in: Capsule())
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)
                        .background(colors.surfaceBackground)
                    }
                }

                Spacer(minLength: 24)
            }
        }
    }
}
