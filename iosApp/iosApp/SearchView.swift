import SwiftUI

struct SearchView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    @State private var searchQuery = ""
    @FocusState private var isFocused: Bool

    var body: some View {
        VStack(spacing: 0) {
            // ── Search input bar ──────────────────────────────────────────────
            HStack(spacing: 0) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(colors.secondaryText)
                    .padding(.leading, 16)
                    .padding(.trailing, 8)

                TextField("Search offenders...", text: $searchQuery)
                    .font(typography.text1)
                    .foregroundStyle(colors.primaryText)
                    .focused($isFocused)
                    .submitLabel(.search)
                    .padding(.vertical, 12)

                if !searchQuery.isEmpty {
                    Button {
                        searchQuery = ""
                        isFocused = true
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(colors.secondaryText)
                    }
                    .padding(.trailing, 16)
                    .transition(.opacity.animation(.easeInOut(duration: 0.15)))
                }
            }
            .frame(maxWidth: .infinity)
            .background(colors.mainBackground)

            Divider()
                .background(colors.strokePale)

            // ── Body — search results coming soon ─────────────────────────────
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.mainBackground)
        .navigationTitle("Search")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { isFocused = true }
    }
}
