import SwiftUI

// ── Texas Watch iOS Theme ─────────────────────────────────────────────────────
// Mirrors the Kotlin Colors.kt / ColorValues.kt / Typography.kt tokens exactly.
// Usage: TexasWatchTheme.colors.primaryAccent, TexasWatchTheme.typography.h3, etc.

// MARK: - Brand palette (mirrors ColorValues.kt Brand object)

private enum Brand {
    static let navy100 = Color(hex: 0x1A237E)
    static let navy80  = Color(hex: 0x1A237E).opacity(0.80)
    static let red100  = Color(hex: 0xBF0000)
    static let danger  = Color(hex: 0xD32F2F)
    static let dangerDark = Color(hex: 0xFF6659)
    static let success    = Color(hex: 0x388E3C)
    static let successDark = Color(hex: 0x66BB6A)
}

private enum UI {
    static let black100 = Color(hex: 0x19191C)
    static let black60  = Color(hex: 0x19191C).opacity(0.60)
    static let black15  = Color(hex: 0x19191C).opacity(0.15)
    static let white100 = Color.white
    static let white100c = Color(hex: 0xFFFFFF)
    static let white70  = Color.white.opacity(0.70)
    static let white20  = Color.white.opacity(0.20)
    static let grey100  = Color(hex: 0xE8E8E8)
    static let grey400  = Color(hex: 0xA3A3A4)
    static let grey500  = Color(hex: 0x757577)
    static let grey900  = Color(hex: 0x303033)
}

// MARK: - Colors (mirrors Colors.kt)

struct TexasWatchColors {
    let isDark: Bool

    let mainBackground: Color
    let surfaceBackground: Color
    let cardBackground: Color

    let primaryAccent: Color
    let secondaryAccent: Color

    let strokeFull: Color
    let strokePale: Color

    let primaryText: Color
    let secondaryText: Color
    let invertedText: Color
    let accentText: Color
    let dangerText: Color
    let successText: Color

    let dangerBadge: Color
    let successBadge: Color
    let neutralBadge: Color

    let ringActive: Color
    let ringTrack: Color
}

let TexasWatchLightColors = TexasWatchColors(
    isDark:            false,
    mainBackground:    UI.white100,
    surfaceBackground: UI.grey100,
    cardBackground:    UI.white100,
    primaryAccent:     Brand.navy100,
    secondaryAccent:   Brand.red100,
    strokeFull:        UI.black100,
    strokePale:        UI.black15,
    primaryText:       UI.black100,
    secondaryText:     UI.black60,
    invertedText:      UI.white100,
    accentText:        Brand.navy100,
    dangerText:        Brand.danger,
    successText:       Brand.success,
    dangerBadge:       Brand.danger,
    successBadge:      Brand.success,
    neutralBadge:      UI.grey500,
    ringActive:        Brand.navy100,
    ringTrack:         UI.black15
)

let TexasWatchDarkColors = TexasWatchColors(
    isDark:            true,
    mainBackground:    UI.black100,
    surfaceBackground: UI.grey900,
    cardBackground:    Color(hex: 0x2A2A2D),
    primaryAccent:     Brand.navy80,
    secondaryAccent:   Brand.red100.opacity(0.80),
    strokeFull:        UI.white100c,
    strokePale:        UI.white20,
    primaryText:       UI.white100c,
    secondaryText:     UI.white70,
    invertedText:      UI.black100,
    accentText:        Color(hex: 0x7986CB),
    dangerText:        Brand.dangerDark,
    successText:       Brand.successDark,
    dangerBadge:       Brand.dangerDark,
    successBadge:      Brand.successDark,
    neutralBadge:      UI.grey400,
    ringActive:        Brand.navy80,
    ringTrack:         UI.white20
)

// MARK: - Typography (mirrors Typography.kt — uses JetBrains Sans)

struct TexasWatchTypography {
    let h1:    Font   // 28sp SemiBold
    let h2:    Font   // 22sp SemiBold
    let h3:    Font   // 16sp SemiBold
    let h4:    Font   // 13sp SemiBold
    let text1: Font   // 16sp Regular
    let text2: Font   // 13sp Regular
    let label: Font   // 11sp SemiBold
}

// JetBrains Sans font names as registered in Info.plist
private enum JetBrainsSans {
    static func semibold(_ size: CGFloat) -> Font {
        Font.custom("JetBrainsSans-SemiBold", size: size)
    }
    static func regular(_ size: CGFloat) -> Font {
        Font.custom("JetBrainsSans-Regular", size: size)
    }
    static func bold(_ size: CGFloat) -> Font {
        Font.custom("JetBrainsSans-Bold", size: size)
    }
}

let TexasWatchTypographyTokens = TexasWatchTypography(
    h1:    JetBrainsSans.semibold(28),
    h2:    JetBrainsSans.semibold(22),
    h3:    JetBrainsSans.semibold(16),
    h4:    JetBrainsSans.semibold(13),
    text1: JetBrainsSans.regular(16),
    text2: JetBrainsSans.regular(13),
    label: JetBrainsSans.semibold(11)
)

// MARK: - Theme environment key

private struct ColorsKey: EnvironmentKey {
    static let defaultValue = TexasWatchLightColors
}
private struct TypographyKey: EnvironmentKey {
    static let defaultValue = TexasWatchTypographyTokens
}

extension EnvironmentValues {
    var twColors: TexasWatchColors {
        get { self[ColorsKey.self] }
        set { self[ColorsKey.self] = newValue }
    }
    var twTypography: TexasWatchTypography {
        get { self[TypographyKey.self] }
        set { self[TypographyKey.self] = newValue }
    }
}

// MARK: - Theme wrapper view modifier

struct TexasWatchTheme: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        content
            .environment(\.twColors, colorScheme == .dark ? TexasWatchDarkColors : TexasWatchLightColors)
            .environment(\.twTypography, TexasWatchTypographyTokens)
    }
}

extension View {
    func texasWatchTheme() -> some View {
        modifier(TexasWatchTheme())
    }
}

// MARK: - Color(hex:) helper

extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8)  & 0xFF) / 255
        let b = Double( hex        & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}
