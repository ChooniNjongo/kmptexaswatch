import SwiftUI

// ── Nearby Offenders Card ─────────────────────────────────────────────────────

struct NearbyOffendersCard: View {
    var count: Int = 0
    var progress: Double = 0
    var locationActive: Bool = false
    var isLoading: Bool = false
    var onAllowLocation: () -> Void = {}

    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    @State private var animatedProgress: Double = 0
    @State private var animatedCount: Int = 0

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
                        Text(isLoading ? "..." : "\(animatedCount)")
                            .font(typography.h1)
                            .foregroundColor(colors.primaryText)
                            .contentTransition(.numericText())
                            .animation(.easeInOut(duration: duration), value: animatedCount)
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
                    .trim(from: 0, to: locationActive ? animatedProgress : 0)
                    .stroke(
                        locationActive ? colors.ringActive : colors.ringTrack,
                        style: StrokeStyle(lineWidth: ringStroke, lineCap: .round)
                    )
                    .frame(width: ringSize, height: ringSize)
                    .rotationEffect(.degrees(-90))
                    .animation(.easeInOut(duration: duration), value: animatedProgress)

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
        .onChange(of: progress) { newProgress in
            withAnimation(.easeInOut(duration: duration)) {
                animatedProgress = newProgress
            }
        }
        .onChange(of: count) { newCount in
            animatedCount = 0
            guard newCount > 0 else { return }
            let interval = duration / Double(newCount)
            for i in 1...newCount {
                DispatchQueue.main.asyncAfter(deadline: .now() + interval * Double(i)) {
                    animatedCount = i
                }
            }
        }
        .onAppear {
            guard locationActive && progress > 0 else { return }
            withAnimation(.easeInOut(duration: duration)) {
                animatedProgress = progress
            }
            let steps = count
            guard steps > 0 else { return }
            let interval = duration / Double(steps)
            for i in 1...steps {
                DispatchQueue.main.asyncAfter(deadline: .now() + interval * Double(i)) {
                    animatedCount = i
                }
            }
        }
    }
}
