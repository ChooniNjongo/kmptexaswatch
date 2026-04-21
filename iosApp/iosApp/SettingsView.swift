import SwiftUI

struct SettingsView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    @State private var selectedTheme: AppTheme = .system
    @State private var nearbyAlerts = true
    @State private var appUpdates = true

    var body: some View {
        List {
            // ── Appearance ────────────────────────────────────────────────────
            Section {
                ThemeSelector(selected: $selectedTheme, colors: colors, typography: typography)
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(colors.mainBackground)
                    .padding(.vertical, 8)
            } header: {
                SectionHeading(text: "Appearance", colors: colors, typography: typography)
            }

            // ── Notifications ─────────────────────────────────────────────────
            Section {
                SettingsToggleRow(
                    title: "Nearby Alerts",
                    note: "Get notified when a new offender is registered near you.",
                    isOn: $nearbyAlerts,
                    colors: colors,
                    typography: typography
                )
                .listRowInsets(EdgeInsets())
                .listRowBackground(colors.surfaceBackground)

                SettingsToggleRow(
                    title: "App Updates",
                    note: "Receive updates about new features and data improvements.",
                    isOn: $appUpdates,
                    colors: colors,
                    typography: typography
                )
                .listRowInsets(EdgeInsets())
                .listRowBackground(colors.surfaceBackground)
            } header: {
                SectionHeading(text: "Notifications", colors: colors, typography: typography)
            }

            // ── About ─────────────────────────────────────────────────────────
            Section {
                SettingsMenuRow(title: "Privacy Notice", colors: colors, typography: typography, action: {})
                    .listRowBackground(colors.surfaceBackground)
                SettingsMenuRow(title: "Terms of Use", colors: colors, typography: typography, action: {})
                    .listRowBackground(colors.surfaceBackground)
                SettingsMenuRow(title: "Licenses", colors: colors, typography: typography, action: {})
                    .listRowBackground(colors.surfaceBackground)
            } header: {
                SectionHeading(text: "About", colors: colors, typography: typography)
            }

            // ── Version ───────────────────────────────────────────────────────
            Section {
                HStack {
                    Spacer()
                    Text("Texas Watch v1.0.0")
                        .font(typography.text2)
                        .foregroundColor(colors.secondaryText)
                    Spacer()
                }
                .listRowBackground(colors.mainBackground)
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .background(colors.mainBackground)
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// ── Section heading ───────────────────────────────────────────────────────────

private struct SectionHeading: View {
    let text: String
    let colors: TexasWatchColors
    let typography: TexasWatchTypography

    var body: some View {
        Text(text)
            .font(typography.h2)
            .foregroundColor(colors.primaryText)
            .textCase(nil)
            .padding(.top, 8)
            .padding(.bottom, 4)
    }
}

// ── Theme enum + selector ─────────────────────────────────────────────────────

enum AppTheme: CaseIterable {
    case system, light, dark

    var label: String {
        switch self {
        case .system: return "System"
        case .light:  return "Light"
        case .dark:   return "Dark"
        }
    }
}

private struct ThemeSelector: View {
    @Binding var selected: AppTheme
    let colors: TexasWatchColors
    let typography: TexasWatchTypography

    var body: some View {
        HStack(spacing: 8) {
            ForEach(AppTheme.allCases, id: \.self) { theme in
                ThemeBox(
                    theme: theme,
                    isSelected: selected == theme,
                    colors: colors,
                    typography: typography,
                    onTap: { selected = theme }
                )
            }
        }
        .padding(.horizontal, 16)
    }
}

private struct ThemeBox: View {
    let theme: AppTheme
    let isSelected: Bool
    let colors: TexasWatchColors
    let typography: TexasWatchTypography
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 8) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(isSelected ? colors.primaryAccent : colors.strokePale, lineWidth: isSelected ? 2 : 1)
                    themePreview
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .aspectRatio(1, contentMode: .fit)
                .frame(maxHeight: 112)
                .padding(isSelected ? 4 : 0)
                .overlay(
                    isSelected ? RoundedRectangle(cornerRadius: 16)
                        .stroke(colors.primaryAccent, lineWidth: 2)
                        .padding(-4) : nil
                )

                Text(theme.label)
                    .font(typography.text2)
                    .foregroundColor(colors.primaryText)
            }
        }
        .buttonStyle(.plain)
        .frame(maxWidth: .infinity)
    }

    @ViewBuilder
    private var themePreview: some View {
        switch theme {
        case .system:
            HStack(spacing: 0) {
                Rectangle().fill(Color.white)
                Rectangle().fill(Color.black)
            }
        case .light:
            Rectangle().fill(Color.white)
        case .dark:
            Rectangle().fill(Color.black)
        }
    }
}

// ── Toggle row ────────────────────────────────────────────────────────────────

private struct SettingsToggleRow: View {
    let title: String
    let note: String
    @Binding var isOn: Bool
    let colors: TexasWatchColors
    let typography: TexasWatchTypography

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(typography.h3)
                    .foregroundColor(colors.primaryText)
                Text(note)
                    .font(typography.text2)
                    .foregroundColor(colors.secondaryText)
            }
            Spacer()
            KotlinConfToggle(isOn: $isOn, colors: colors)
        }
        .padding(16)
        .contentShape(Rectangle())
        .onTapGesture { isOn.toggle() }
    }
}

// ── Custom toggle (mirrors KotlinConf Toggle.kt) ──────────────────────────────

private struct KotlinConfToggle: View {
    @Binding var isOn: Bool
    let colors: TexasWatchColors

    var body: some View {
        let trackColor = isOn ? colors.primaryAccent : colors.strokePale
        let thumbOffset: CGFloat = isOn ? 7 : -7

        ZStack {
            // Track
            Capsule()
                .fill(trackColor)
                .frame(width: 28, height: 16)
            // Thumb
            Circle()
                .fill(trackColor)
                .frame(width: 18, height: 18)
                .overlay(Circle().fill(colors.mainBackground).frame(width: 14, height: 14))
                .offset(x: thumbOffset)
        }
        .animation(.easeInOut(duration: 0.2), value: isOn)
        .onTapGesture { isOn.toggle() }
    }
}

// ── Menu row ──────────────────────────────────────────────────────────────────

private struct SettingsMenuRow: View {
    let title: String
    let colors: TexasWatchColors
    let typography: TexasWatchTypography
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Text(title)
                    .font(typography.h3)
                    .foregroundColor(colors.primaryText)
                Spacer()
                Image(systemName: "chevron.right")
                    .foregroundColor(colors.secondaryText)
                    .font(.system(size: 13, weight: .semibold))
            }
            .padding(16)
        }
        .buttonStyle(.plain)
    }
}
