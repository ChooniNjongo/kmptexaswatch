import SwiftUI
import Shared

// ── Native iOS Offender Card ──────────────────────────────────────────────────

struct OffenderCard: View {
    let offender: OffenderSummary
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        HStack(spacing: 12) {
            OffenderPhoto(photoUrl: offender.photoUrl, initials: initials)
                .frame(width: 96, height: 96)
                .clipShape(RoundedRectangle(cornerRadius: 12))

            VStack(alignment: .leading, spacing: 4) {
                Text(offender.fullName)
                    .font(typography.h3)
                    .foregroundStyle(colors.primaryText)

                Text(subtitle)
                    .font(typography.text2)
                    .foregroundStyle(colors.secondaryText)

                if let address = offender.address, !address.isEmpty {
                    Text(address)
                        .font(typography.text2)
                        .foregroundStyle(colors.secondaryText)
                        .lineLimit(2)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(12)
        .background(colors.mainBackground)
        .clipShape(RoundedRectangle(cornerRadius: 0))
    }

    private var initials: String {
        let f = offender.firstName.first.map { String($0).uppercased() } ?? ""
        let l = offender.lastName.first.map { String($0).uppercased() } ?? ""
        return f + l
    }

    private var subtitle: String {
        var parts: [String] = []
        if let age = offender.age { parts.append("Age \(age)") }
        parts.append("DPS: \(offender.dpsNumber)")
        return parts.joined(separator: "  ·  ")
    }
}

// ── Photo with async loading + initials fallback ──────────────────────────────

struct OffenderPhoto: View {
    let photoUrl: String?
    let initials: String

    var body: some View {
        if let url = photoUrl.flatMap(URL.init), !url.absoluteString.isEmpty {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image.resizable().scaledToFill()
                case .failure, .empty:
                    InitialsAvatar(initials: initials)
                @unknown default:
                    InitialsAvatar(initials: initials)
                }
            }
        } else {
            InitialsAvatar(initials: initials)
        }
    }
}

// ── Initials fallback ─────────────────────────────────────────────────────────

struct InitialsAvatar: View {
    let initials: String
    @Environment(\.twColors) private var colors

    var body: some View {
        ZStack {
            colors.primaryAccent
            Text(initials)
                .font(.title2.bold())
                .foregroundStyle(colors.invertedText)
        }
    }
}
