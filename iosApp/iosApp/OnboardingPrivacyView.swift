import SwiftUI

// ── Screen 1: Privacy Notice ──────────────────────────────────────────────────
// Mirrors Android OnboardingPrivacyScreen pixel-for-pixel

struct OnboardingPrivacyView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var onDecline: () -> Void
    var onAccept: () -> Void

    @State private var detailsVisible = false

    var body: some View {
        VStack(spacing: 0) {
            if detailsVisible {
                // ── Full privacy text ─────────────────────────────────────────
                VStack(spacing: 0) {
                    OnboardingHeaderBar(title: "Privacy Notice", colors: colors, typography: typography) {
                        Button(action: { detailsVisible = false }) {
                            Image(systemName: "chevron.left")
                                .foregroundColor(colors.primaryText)
                                .frame(width: 44, height: 44)
                        }
                    }
                    Divider().background(colors.strokePale)
                    ScrollView {
                        Text(privacyNoticeText)
                            .font(typography.text1)
                            .foregroundColor(colors.secondaryText)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 16)
                    }
                    Spacer()
                }
                .transition(.opacity)
            } else {
                // ── Summary view ──────────────────────────────────────────────
                ScrollView {
                    VStack(alignment: .leading, spacing: 24) {
                        OnboardingHeroIcon(colors: colors)

                        Text("Privacy Notice")
                            .font(typography.h1)
                            .foregroundColor(colors.primaryText)

                        Text("This app collects location data to find registered sex offenders near you. We do not sell or share your personal data with third parties. All location lookups happen on your device and are not stored on our servers.")
                            .font(typography.text1)
                            .foregroundColor(colors.secondaryText)

                        OnboardingActionRow(
                            label: "Read full privacy notice",
                            colors: colors,
                            typography: typography,
                            action: { detailsVisible = true }
                        )
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 24)
                }
                .transition(.opacity)

                Spacer()
            }

            Divider().background(colors.strokePale)
            HStack(spacing: 12) {
                OnboardingButton(label: "Decline", primary: false, colors: colors, typography: typography, action: onDecline)
                OnboardingButton(label: "Accept", primary: true, colors: colors, typography: typography, action: onAccept)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 16)
        }
        .background(colors.mainBackground.ignoresSafeArea())
        .animation(.easeInOut(duration: 0.2), value: detailsVisible)
    }

    private let privacyNoticeText = """
Texas Watch – Privacy Notice

Last updated: 2026

What data we collect
Texas Watch uses your device location (when you grant permission) solely to find registered sex offenders near you. Location data is processed on-device and is never transmitted to our servers.

How we use it
Your location is used only to query our offender database API for results relevant to your area. We do not build location history or profiles.

Third-party services
We use no third-party analytics, advertising networks, or tracking SDKs.

Your rights
You may revoke location permission at any time in your device Settings. Revoking permission will limit search functionality to manual address or name searches.

Contact
For questions, contact support@texaswatch.app
"""
}
