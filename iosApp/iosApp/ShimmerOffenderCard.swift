import SwiftUI

// ── Shimmer Offender Card ─────────────────────────────────────────────────────
// Skeleton matching the OffenderCard layout: 96pt photo + text lines

struct ShimmerOffenderCard: View {
    @Environment(\.twColors) private var colors
    @State private var phase: CGFloat = -1.0

    var body: some View {
        HStack(spacing: 12) {
            // Photo placeholder
            RoundedRectangle(cornerRadius: 12)
                .fill(shimmerBrush)
                .frame(width: 96, height: 96)

            // Text lines
            VStack(alignment: .leading, spacing: 8) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(shimmerBrush)
                    .frame(width: 160, height: 16)

                RoundedRectangle(cornerRadius: 4)
                    .fill(shimmerBrush)
                    .frame(width: 110, height: 12)

                RoundedRectangle(cornerRadius: 4)
                    .fill(shimmerBrush)
                    .frame(width: 200, height: 12)
            }

            Spacer()
        }
        .padding(12)
        .background(colors.cardBackground)
        .onAppear {
            withAnimation(
                .linear(duration: 1.0).repeatForever(autoreverses: false)
            ) {
                phase = 1.0
            }
        }
    }

    private var shimmerBrush: LinearGradient {
        LinearGradient(
            stops: [
                .init(color: colors.surfaceBackground, location: 0.0),
                .init(color: colors.strokePale.opacity(0.6), location: 0.5),
                .init(color: colors.surfaceBackground, location: 1.0),
            ],
            startPoint: UnitPoint(x: phase - 0.3, y: 0),
            endPoint: UnitPoint(x: phase + 0.3, y: 0)
        )
    }
}
