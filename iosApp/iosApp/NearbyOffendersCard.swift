import SwiftUI

// ── Nearby Offenders Card ─────────────────────────────────────────────────────
// Ring arc and count number are driven by the same animated fraction 0→1
// so they always finish together.

struct NearbyOffendersCard: View {
    var count: Int = 0
    var locationActive: Bool = false
    var isLoading: Bool = false
    var onAllowLocation: () -> Void = {}

    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    // Single source of truth — both ring and count read from this
    @State private var fraction: Double = 0

    private let ringSize: CGFloat = 88
    private let ringStroke: CGFloat = 10
    private let duration: Double = 1.2

    var body: some View {
        HStack(alignment: .center, spacing: 16) {

            // ── Left ──────────────────────────────────────────────────────────
            VStack(alignment: .leading, spacing: 4) {
                Text("Nearby Offenders")
                    .font(typography.text2)
                    .foregroundColor(colors.secondaryText)

                if !locationActive {
                    Button(action: onAllowLocation) {
                        Text("Allow Location")
                            .font(typography.label)
                            .foregroundColor(colors.invertedText)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(colors.ringActive)
                            .cornerRadius(8)
                    }
                    .padding(.top, 4)
                } else {
                    HStack(alignment: .lastTextBaseline, spacing: 4) {
                        Text(isLoading ? "..." : "\(Int((fraction * Double(count)).rounded()))")
                            .font(typography.h1)
                            .foregroundColor(colors.primaryText)
                            .contentTransition(.numericText())
                        if !isLoading {
                            Text("found")
                                .font(typography.text2)
                                .foregroundColor(colors.secondaryText)
                        }
                    }
                    Text("within radius")
                        .font(typography.text2)
                        .foregroundColor(colors.secondaryText)
                }
            }

            Spacer()

            // ── Right: donut ring ─────────────────────────────────────────────
            ZStack {
                Circle()
                    .stroke(colors.ringTrack,
                            style: StrokeStyle(lineWidth: ringStroke, lineCap: .round))
                    .frame(width: ringSize, height: ringSize)

                Circle()
                    .trim(from: 0, to: locationActive ? fraction : 0)
                    .stroke(
                        locationActive ? colors.ringActive : colors.ringTrack,
                        style: StrokeStyle(lineWidth: ringStroke, lineCap: .round)
                    )
                    .frame(width: ringSize, height: ringSize)
                    .rotationEffect(.degrees(-90))

                Image(systemName: "location.fill")
                    .font(.system(size: 24, weight: .medium))
                    .foregroundColor(locationActive ? colors.ringActive : colors.ringTrack)
            }
        }
        .padding(20)
        .background(colors.cardBackground)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.12), radius: 8, x: 0, y: 3)
        .padding(.horizontal, 16)
        .padding(.top, 16)
        .onChange(of: count) { _ in animate() }
        .onChange(of: locationActive) { active in
            if active { animate() } else { fraction = 0 }
        }
        .onAppear { if locationActive && count > 0 { animate() } }
    }

    private func animate() {
        guard locationActive && count > 0 else { return }
        fraction = 0
        withAnimation(.easeInOut(duration: duration)) {
            fraction = 1.0
        }
    }
}
