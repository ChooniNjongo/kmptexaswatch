import Shared
import SwiftUI

struct OffenderDetailView: View {
    let indIdn: Int
    let offenderName: String
    var distanceMiles: Double? = nil
    @StateObject private var vm: OffenderDetailViewModel
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    init(indIdn: Int, offenderName: String, distanceMiles: Double? = nil) {
        self.indIdn = indIdn
        self.offenderName = offenderName
        self.distanceMiles = distanceMiles
        _vm = StateObject(wrappedValue: OffenderDetailViewModel(indIdn: indIdn))
    }

    var body: some View {
        Group {
            if vm.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(colors.mainBackground.ignoresSafeArea())
            } else if let err = vm.error {
                VStack(spacing: 16) {
                    Text(err)
                        .font(typography.text2)
                        .foregroundStyle(colors.dangerText)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 16)
                    Button("Retry") { vm.load() }
                        .font(typography.text2)
                        .foregroundStyle(colors.primaryAccent)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(colors.mainBackground.ignoresSafeArea())
            } else if let detail = vm.detail {
                detailContent(detail: detail, distanceMiles: distanceMiles)
            }
        }
        .navigationTitle(offenderName)
        .navigationBarTitleDisplayMode(.inline)
        .background(colors.mainBackground.ignoresSafeArea())
    }

    @ViewBuilder
    private func detailContent(detail: OffenderDetail, distanceMiles: Double?) -> some View {
        let baseName = detail.names?.baseName
        let firstName = baseName?.firstName ?? ""
        let lastName = baseName?.lastName ?? ""
        let initials: String = {
            let f = firstName.first.map { String($0).uppercased() } ?? ""
            let l = lastName.first.map { String($0).uppercased() } ?? ""
            return f + l
        }()
        let photoUrl = detail.photos?.currentPhoto?.photoUrl

        ScrollView {
            VStack(spacing: 0) {
                // ── Large photo ───────────────────────────────────────────────
                OffenderPhoto(photoUrl: photoUrl, initials: initials)
                    .frame(width: 300, height: 300)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.vertical, 24)

                // ── Full name ─────────────────────────────────────────────────
                Text(baseName?.fullName ?? "")
                    .font(typography.h2)
                    .foregroundStyle(colors.primaryText)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)

                Spacer().frame(height: 4)

                // ── Subtitle ──────────────────────────────────────────────────
                let age = detail.birthInfo?.age
                let subtitleText: String = {
                    var parts: [String] = []
                    if let a = age { parts.append("Age \(a)") }
                    parts.append("DPS: \(detail.dpsNumber)")
                    return parts.joined(separator: "  ·  ")
                }()
                Text(subtitleText)
                    .font(typography.text2)
                    .foregroundStyle(colors.secondaryText)
                    .padding(.horizontal, 16)

                Spacer().frame(height: 16)

                // ── Risk + Distance badges ────────────────────────────────────
                let hasRisk = detail.registryInfo?.riskLevel != nil
                let hasDist = distanceMiles != nil
                if hasRisk || hasDist {
                    HStack(spacing: 8) {
                        if let riskLevel = detail.registryInfo?.riskLevel {
                            let (badgeColor, badgeLabel): (Color, String) = {
                                switch riskLevel {
                                case "1": return (colors.successBadge, "LOW RISK")
                                case "2": return (colors.neutralBadge, "MODERATE RISK")
                                case "3": return (colors.dangerBadge, "HIGH RISK")
                                default:  return (colors.neutralBadge, riskLevel)
                                }
                            }()
                            Text(badgeLabel)
                                .font(typography.label)
                                .foregroundStyle(colors.invertedText)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 4)
                                .background(badgeColor)
                                .clipShape(RoundedRectangle(cornerRadius: 4))
                        }
                        if let dist = distanceMiles {
                            Text(String(format: "%.1f mi away", dist))
                                .font(typography.label)
                                .foregroundStyle(colors.invertedText)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 4)
                                .background(colors.primaryAccent)
                                .clipShape(RoundedRectangle(cornerRadius: 4))
                        }
                    }
                    Spacer().frame(height: 16)
                }

                // ── Physical Description ──────────────────────────────────────
                let phys = detail.physicalDescription
                if phys != nil {
                    SectionHeader(title: "Physical Description")
                    VStack(spacing: 4) {
                        if let race = phys?.raceDescription, !race.isEmpty {
                            InfoRow(label: "Race", value: race)
                        }
                        if let hair = phys?.hairColorDescription, !hair.isEmpty {
                            InfoRow(label: "Hair Color", value: hair)
                        }
                        if let eye = phys?.eyeColorDescription, !eye.isEmpty {
                            InfoRow(label: "Eye Color", value: eye)
                        }
                        if let height = phys?.heightFormatted, !height.isEmpty {
                            InfoRow(label: "Height", value: height)
                        }
                        if let weight = phys?.weightPounds {
                            InfoRow(label: "Weight", value: "\(weight) lbs")
                        }
                    }
                    .padding(.horizontal, 16)
                    Spacer().frame(height: 16)
                }

                // ── Offenses ──────────────────────────────────────────────────
                if !detail.offenses.isEmpty {
                    Divider().background(colors.strokePale)
                    SectionHeader(title: "Offenses")
                    VStack(spacing: 8) {
                        ForEach(detail.offenses, id: \.offenseId) { offense in
                            VStack(alignment: .leading, spacing: 2) {
                                if let desc = offense.offenseDescription, !desc.isEmpty {
                                    Text(desc)
                                        .font(typography.h4)
                                        .foregroundStyle(colors.primaryText)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                let offenseSub: String = {
                                    var parts: [String] = []
                                    if let date = offense.convictionDate { parts.append("Convicted: \(date)") }
                                    if let statute = offense.statute { parts.append("Statute: \(statute)") }
                                    return parts.joined(separator: "  ·  ")
                                }()
                                if !offenseSub.isEmpty {
                                    Text(offenseSub)
                                        .font(typography.text2)
                                        .foregroundStyle(colors.secondaryText)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    Spacer().frame(height: 16)
                }

                // ── Known Addresses ───────────────────────────────────────────
                if !detail.addresses.isEmpty {
                    Divider().background(colors.strokePale)
                    SectionHeader(title: "Known Addresses")
                    VStack(spacing: 8) {
                        ForEach(detail.addresses, id: \.addressId) { address in
                            VStack(alignment: .leading, spacing: 2) {
                                if let full = address.fullAddress, !full.isEmpty {
                                    Text(full)
                                        .font(typography.text2)
                                        .foregroundStyle(colors.primaryText)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                let cityLine: String = {
                                    var parts: [String] = []
                                    let cityState = [address.city, address.state, address.zipCode]
                                        .compactMap { $0 }
                                        .joined(separator: ", ")
                                    if !cityState.isEmpty { parts.append(cityState) }
                                    if let county = address.countyName { parts.append("\(county) County") }
                                    return parts.joined(separator: "  ·  ")
                                }()
                                if !cityLine.isEmpty {
                                    Text(cityLine)
                                        .font(typography.text2)
                                        .foregroundStyle(colors.secondaryText)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }

                Spacer().frame(height: 32)
            }
        }
    }

    // ── Section header ─────────────────────────────────────────────────────────

    @ViewBuilder
    private func SectionHeader(title: String) -> some View {
        Text(title)
            .font(typography.h4)
            .foregroundStyle(colors.primaryText)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 8)
    }

    // ── Info row ───────────────────────────────────────────────────────────────

    @ViewBuilder
    private func InfoRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(typography.text2)
                .foregroundStyle(colors.secondaryText)
            Spacer()
            Text(value)
                .font(typography.text2)
                .foregroundStyle(colors.primaryText)
        }
    }
}
