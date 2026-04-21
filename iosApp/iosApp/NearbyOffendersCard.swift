import SwiftUI

// ── Nearby Offenders Card ─────────────────────────────────────────────────────
// Ring fills from 0→progress and count increments from 0→count on appear.

struct NearbyOffendersCard: View {
    var count: Int = 247
    var progress: Double = 1.0
    var locationActive: Bool = true

    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    @State private var animatedProgress: Double = 0
    @State private var animatedCount: Int = 0

    private let ringSize: CGFloat = 88
    private let ringStroke: CGFloat = 10
    private let duration: Double = 1.2

    var body: some View {
        HStack(alignment: .center, spacing: 16) {

            // ── Left: text info ───────────────────────────────────────────────
            VStack(alignment: .leading, spacing: 4) {
                Text("Nearby Offenders")
                    .font(typography.text2)
                    .foregroundColor(colors.secondaryText)

                HStack(alignment: .lastTextBaseline, spacing: 4) {
                    Text("\(animatedCount)")
                        .font(typography.h1)
                        .foregroundColor(colors.primaryText)
                        .contentTransition(.numericText())
                        .animation(.easeInOut(duration: duration), value: animatedCount)
                    Text("found")
                        .font(typography.text2)
                        .foregroundColor(colors.secondaryText)
                }

                Text("within 5 mile radius")
                    .font(typography.text2)
                    .foregroundColor(colors.secondaryText)
            }

            Spacer()

            // ── Right: donut ring + icon ──────────────────────────────────────
            ZStack {
                // Track ring
                Circle()
                    .stroke(colors.ringTrack,
                            style: StrokeStyle(lineWidth: ringStroke, lineCap: .round))
                    .frame(width: ringSize, height: ringSize)

                // Progress arc
                Circle()
                    .trim(from: 0, to: locationActive ? animatedProgress : 0)
                    .stroke(
                        locationActive ? colors.ringActive : colors.ringTrack,
                        style: StrokeStyle(lineWidth: ringStroke, lineCap: .round)
                    )
                    .frame(width: ringSize, height: ringSize)
                    .rotationEffect(.degrees(-90))
                    .animation(.easeInOut(duration: duration), value: animatedProgress)

                // Location pin icon
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
        .onAppear {
            guard locationActive else { return }
            // Ring fills up
            withAnimation(.easeInOut(duration: duration)) {
                animatedProgress = progress
            }
            // Count increments step by step
            let steps = count
            let interval = duration / Double(max(steps, 1))
            for i in 1...max(steps, 1) {
                DispatchQueue.main.asyncAfter(deadline: .now() + interval * Double(i)) {
                    animatedCount = i
                }
            }
        }
    }
}
