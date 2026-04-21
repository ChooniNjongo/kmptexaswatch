import SwiftUI

// ── Reusable onboarding UI components (mirror Android OnboardingComponents.kt) ─

// Hero icon (circle with person icon)
struct OnboardingHeroIcon: View {
    let colors: TexasWatchColors

    var body: some View {
        ZStack {
            Circle()
                .fill(colors.primaryAccent.opacity(0.12))
                .frame(width: 120, height: 120)
            Image(systemName: "person.2")
                .resizable()
                .scaledToFit()
                .frame(width: 52, height: 52)
                .foregroundColor(colors.primaryAccent)
        }
    }
}

// Header bar for full-text detail view
struct OnboardingHeaderBar<StartContent: View>: View {
    let title: String
    let colors: TexasWatchColors
    let typography: TexasWatchTypography
    @ViewBuilder let startContent: () -> StartContent

    var body: some View {
        HStack(spacing: 8) {
            startContent()
            Text(title)
                .font(typography.h3)
                .foregroundColor(colors.primaryText)
            Spacer()
        }
        .frame(height: 56)
        .padding(.horizontal, 4)
    }
}

// "Read more" action row
struct OnboardingActionRow: View {
    let label: String
    let colors: TexasWatchColors
    let typography: TexasWatchTypography
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Text(label)
                    .font(typography.text1)
                    .foregroundColor(colors.primaryText)
                Spacer()
                Image(systemName: "chevron.right")
                    .foregroundColor(colors.primaryAccent)
                    .frame(width: 20, height: 20)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(colors.mainBackground)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(colors.strokePale, lineWidth: 1)
            )
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }
}

// Primary / secondary button
struct OnboardingButton: View {
    let label: String
    let primary: Bool
    let colors: TexasWatchColors
    let typography: TexasWatchTypography
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(typography.h4)
                .foregroundColor(primary ? colors.invertedText : colors.primaryText)
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(primary ? colors.primaryAccent : colors.mainBackground)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(primary ? colors.primaryAccent : colors.strokePale, lineWidth: 1)
                )
                .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }
}

// Toggle setting row
// onToggle is optional — if provided it intercepts the tap (used for permission requests).
// If nil, the binding is mutated directly.
struct OnboardingToggleItem: View {
    let title: String
    let description: String
    @Binding var isOn: Bool
    let colors: TexasWatchColors
    let typography: TexasWatchTypography
    var onToggle: ((Bool) -> Void)? = nil

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(typography.h4)
                    .foregroundColor(colors.primaryText)
                Text(description)
                    .font(typography.text2)
                    .foregroundColor(colors.secondaryText)
            }
            Spacer()
            Toggle("", isOn: Binding(
                get: { isOn },
                set: { newValue in
                    if let onToggle {
                        onToggle(newValue)
                    } else {
                        isOn = newValue
                    }
                }
            ))
            .labelsHidden()
            .tint(colors.primaryAccent)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(colors.strokePale, lineWidth: 1)
        )
        .cornerRadius(12)
    }
}
