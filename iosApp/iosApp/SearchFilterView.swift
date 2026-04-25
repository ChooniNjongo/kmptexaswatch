import Shared
import SwiftUI

// ── Filter chip label → API code maps ─────────────────────────────────────────

private let riskMap:      [String: String] = ["Low Risk": "1", "Moderate Risk": "2", "High Risk": "3"]
private let raceMap:      [String: String] = ["White": "W", "Black": "B", "Asian": "A",
                                               "American Indian": "I", "Other": "O"]
private let hairMap:      [String: String] = ["Black": "BLK", "Brown": "BRO", "Blonde": "BLN",
                                               "Red": "RED", "Gray": "GRY", "White": "WHI", "Bald": "BAL"]
private let eyeMap:       [String: String] = ["Brown": "BRO", "Blue": "BLU", "Green": "GRN",
                                               "Hazel": "HAZ", "Gray": "GRY", "Black": "BLK", "Maroon": "MAR"]

// ── Filter group definitions ──────────────────────────────────────────────────

private struct FilterGroupDef {
    let title: String
    let labels: [String]
    let codeMap: [String: String]
    let keyPath: WritableKeyPath<SearchCriteria, Set<String>>
}

private let filterGroups: [FilterGroupDef] = [
    .init(title: "Offender Level",
          labels: ["Low Risk", "Moderate Risk", "High Risk"],
          codeMap: riskMap, keyPath: \.riskLevels),
    .init(title: "Race",
          labels: ["White", "Black", "Asian", "American Indian", "Other"],
          codeMap: raceMap, keyPath: \.races),
    .init(title: "Hair Color",
          labels: ["Black", "Brown", "Blonde", "Red", "Gray", "White", "Bald"],
          codeMap: hairMap, keyPath: \.hairColors),
    .init(title: "Eye Color",
          labels: ["Brown", "Blue", "Green", "Hazel", "Gray", "Black", "Maroon"],
          codeMap: eyeMap, keyPath: \.eyeColors),
]

// ── SearchFilterView ──────────────────────────────────────────────────────────

struct SearchFilterView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    @Environment(\.dismiss) private var dismiss

    @StateObject private var vm = SearchFilterViewModel()
    @State private var filtersExpanded = true
    @State private var showCountySheet = false

    var body: some View {
        VStack(spacing: 0) {

            // ── Search bar ────────────────────────────────────────────────────
            HStack(spacing: 0) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(colors.secondaryText)
                    .padding(.leading, 16).padding(.trailing, 8)

                TextField("Search by name…", text: Binding(
                    get: { vm.criteria.name },
                    set: { vm.onNameChanged($0) }
                ))
                .font(typography.text1)
                .foregroundStyle(colors.primaryText)
                .submitLabel(.search)
                .padding(.vertical, 12)

                if !vm.criteria.name.isEmpty {
                    Button { vm.onNameChanged("") } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(colors.secondaryText)
                    }
                    .padding(.trailing, 8)
                    .transition(.opacity.animation(.easeInOut(duration: 0.15)))
                }

                Button("Cancel") { dismiss() }
                    .font(typography.text2)
                    .foregroundStyle(colors.primaryAccent)
                    .padding(.trailing, 16)
            }
            .background(colors.mainBackground)

            Divider().background(colors.strokePale)

            // ── Main scrollable content ───────────────────────────────────────
            ScrollView {
                LazyVStack(spacing: 12, pinnedViews: []) {

                    // Filter panel
                    FilterPanel(
                        criteria: $vm.criteria,
                        expanded: $filtersExpanded,
                        onCountyTap: { showCountySheet = true },
                        onFilterChanged: { vm.onFilterChanged() },
                        onClearAll: { vm.clearAllFilters() }
                    )
                    .padding(.horizontal, 12)

                    // ── Results section ───────────────────────────────────────
                    if vm.criteria.hasAnyFilter {
                        resultsSection
                    }
                }
            }
            .scrollDismissesKeyboard(.interactively)
        }
        .background(colors.mainBackground.ignoresSafeArea())
        .sheet(isPresented: $showCountySheet) {
            CountyPickerSheet(selected: Binding(
                get: { vm.criteria.county },
                set: { vm.criteria.county = $0; vm.onFilterChanged() }
            ))
        }
    }

    // ── Results section ───────────────────────────────────────────────────────

    @ViewBuilder
    private var resultsSection: some View {
        // Result count header
        HStack {
            if vm.isLoading {
                Text("Searching…")
                    .font(typography.text2)
                    .foregroundStyle(colors.secondaryText)
            } else if !vm.results.isEmpty {
                Text("\(vm.totalResults) offender\(vm.totalResults == 1 ? "" : "s") found")
                    .font(typography.text2)
                    .foregroundStyle(colors.secondaryText)
            }
            Spacer()
        }
        .padding(.horizontal, 16)

        if vm.isLoading {
            // ── Spinning disc loader ──────────────────────────────────────────
            SpinningDiscLoader()
                .padding(.top, 32)
        } else if let err = vm.error {
            Text(err)
                .font(typography.text2)
                .foregroundStyle(colors.dangerText)
                .padding(.horizontal, 16)
        } else if vm.results.isEmpty {
            Text("No results found")
                .font(typography.text2)
                .foregroundStyle(colors.secondaryText)
                .frame(maxWidth: .infinity)
                .padding(.top, 32)
        } else {
            // ── Offender cards ────────────────────────────────────────────────
            ForEach(vm.results, id: \.indIdn) { offender in
                NavigationLink(destination: OffenderDetailView(indIdn: Int(offender.indIdn), offenderName: offender.fullName)) {
                    OffenderCard(offender: offender)
                }
                .buttonStyle(.plain)
                .padding(.horizontal, 12)
                .onAppear {
                    // Trigger next page when near bottom
                    if let last = vm.results.suffix(5).first,
                       last.indIdn == offender.indIdn {
                        vm.loadNextPage()
                    }
                }
            }

            if vm.isLoadingMore {
                SpinningDiscLoader(size: 32)
                    .padding(.vertical, 16)
            }

            Spacer(minLength: 40)
        }
    }
}

// ── Filter panel card ─────────────────────────────────────────────────────────

private struct FilterPanel: View {
    @Binding var criteria: SearchCriteria
    @Binding var expanded: Bool
    let onCountyTap: () -> Void
    let onFilterChanged: () -> Void
    let onClearAll: () -> Void

    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var activeFilterCount: Int {
        criteria.riskLevels.count + criteria.races.count +
        criteria.hairColors.count + criteria.eyeColors.count +
        (criteria.county != nil ? 1 : 0)
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header row
            Button {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                    expanded.toggle()
                }
            } label: {
                HStack(spacing: 8) {
                    Text("Filters")
                        .font(typography.h4)
                        .foregroundStyle(colors.primaryText)
                    if activeFilterCount > 0 {
                        Text("\(activeFilterCount)")
                            .font(typography.label)
                            .foregroundStyle(colors.invertedText)
                            .padding(.horizontal, 7)
                            .padding(.vertical, 3)
                            .background(colors.primaryAccent)
                            .clipShape(Capsule())
                    }
                    Spacer()
                    if activeFilterCount > 0 {
                        Button("Clear all") { onClearAll() }
                            .font(typography.text2)
                            .foregroundStyle(colors.primaryAccent)
                            .padding(.trailing, 4)
                    }
                    Image(systemName: "chevron.down")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(colors.primaryText)
                        .rotationEffect(.degrees(expanded ? 0 : -90))
                        .animation(.spring(response: 0.3, dampingFraction: 0.8), value: expanded)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
            }

            if expanded {
                VStack(spacing: 14) {
                    Divider().background(colors.strokePale).padding(.horizontal, 12)

                    // County
                    FilterSectionLabel(title: "County")
                    CountyPickerRow(selected: criteria.county, onTap: onCountyTap)
                        .padding(.horizontal, 12)

                    // Chip filter groups
                    ForEach(filterGroups, id: \.title) { group in
                        FilterSectionLabel(title: group.title)
                        ChipRow(group: group, criteria: $criteria, onChange: onFilterChanged)
                            .padding(.horizontal, 12)
                    }
                }
                .padding(.bottom, 16)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(colors.strokePale, lineWidth: 0.5))
    }
}

// ── Chip row for one filter group ─────────────────────────────────────────────

private struct ChipRow: View {
    let group: FilterGroupDef
    @Binding var criteria: SearchCriteria
    let onChange: () -> Void

    var body: some View {
        FlowLayout(spacing: 8) {
            ForEach(group.labels, id: \.self) { label in
                let code = group.codeMap[label] ?? label
                let selected = criteria[keyPath: group.keyPath].contains(code)
                GlassChip(label: label, selected: selected) {
                    if selected {
                        criteria[keyPath: group.keyPath].remove(code)
                    } else {
                        criteria[keyPath: group.keyPath].insert(code)
                    }
                    onChange()
                }
            }
        }
    }
}

// ── County picker row ─────────────────────────────────────────────────────────

private struct CountyPickerRow: View {
    let selected: TexasCounty?
    let onTap: () -> Void
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        Button(action: onTap) {
            HStack {
                Text(selected?.displayName ?? "Select a county")
                    .font(typography.text2)
                    .foregroundStyle(selected != nil ? colors.primaryText : colors.secondaryText)
                Spacer()
                Image(systemName: "chevron.down")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(colors.secondaryText)
            }
            .padding(.horizontal, 14).padding(.vertical, 12)
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(
                selected != nil ? colors.primaryAccent : colors.strokeFull,
                lineWidth: selected != nil ? 1.5 : 0.5
            ))
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

private struct FilterSectionLabel: View {
    let title: String
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    var body: some View {
        HStack { Text(title).font(typography.text2).foregroundStyle(colors.secondaryText); Spacer() }
            .padding(.horizontal, 12)
    }
}

// ── Glass chip ────────────────────────────────────────────────────────────────

private struct GlassChip: View {
    let label: String
    let selected: Bool
    let onTap: () -> Void
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        Button(action: onTap) {
            Text(label)
                .font(typography.text2)
                .foregroundStyle(selected ? colors.invertedText : colors.primaryText)
                .padding(.horizontal, 14).padding(.vertical, 10)
                .background {
                    if selected {
                        RoundedRectangle(cornerRadius: 8).fill(colors.primaryAccent)
                    } else {
                        RoundedRectangle(cornerRadius: 8).fill(.regularMaterial)
                    }
                }
                .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(
                    selected ? Color.clear : colors.strokeFull.opacity(0.4),
                    lineWidth: 0.5
                ))
        }
        .buttonStyle(.plain)
        .animation(.spring(response: 0.22, dampingFraction: 0.72), value: selected)
    }
}

// ── Spinning disc loader ──────────────────────────────────────────────────────

struct SpinningDiscLoader: View {
    var size: CGFloat = 56
    @Environment(\.twColors) private var colors
    @State private var rotation: Double = 0
    @State private var speed: Double = 1.0

    var body: some View {
        ZStack {
            // Track ring
            Circle()
                .stroke(colors.primaryAccent.opacity(0.15), lineWidth: size * 0.12)
                .frame(width: size, height: size)

            // Spinning arc
            Circle()
                .trim(from: 0, to: 0.72)
                .stroke(
                    AngularGradient(
                        colors: [colors.primaryAccent.opacity(0.1), colors.primaryAccent],
                        center: .center
                    ),
                    style: StrokeStyle(lineWidth: size * 0.12, lineCap: .round)
                )
                .frame(width: size, height: size)
                .rotationEffect(.degrees(rotation))
                .onAppear {
                    // Accelerate from slow to fast over 0.6s then loop
                    withAnimation(.linear(duration: 0.6).delay(0.1)) {
                        speed = 2.5
                    }
                    withAnimation(.linear(duration: 1.0).repeatForever(autoreverses: false)) {
                        rotation = 360
                    }
                }

            // Inner pulsing dot
            Circle()
                .fill(colors.primaryAccent)
                .frame(width: size * 0.18, height: size * 0.18)
                .scaleEffect(rotation.truncatingRemainder(dividingBy: 360) < 180 ? 1.2 : 0.8)
                .animation(.easeInOut(duration: 0.5).repeatForever(), value: rotation)
        }
        .frame(maxWidth: .infinity)
    }
}

// ── Flow layout ───────────────────────────────────────────────────────────────

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout Void) -> CGSize {
        let maxW = proposal.width ?? .infinity
        var x: CGFloat = 0, y: CGFloat = 0, rowH: CGFloat = 0
        for v in subviews {
            let s = v.sizeThatFits(.unspecified)
            if x + s.width > maxW && x > 0 { y += rowH + spacing; x = 0; rowH = 0 }
            x += s.width + spacing; rowH = max(rowH, s.height)
        }
        return CGSize(width: maxW, height: y + rowH)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout Void) {
        var x = bounds.minX, y = bounds.minY, rowH: CGFloat = 0
        for v in subviews {
            let s = v.sizeThatFits(.unspecified)
            if x + s.width > bounds.maxX && x > bounds.minX { y += rowH + spacing; x = bounds.minX; rowH = 0 }
            v.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(width: s.width, height: s.height))
            x += s.width + spacing; rowH = max(rowH, s.height)
        }
    }
}

// ── County picker sheet ───────────────────────────────────────────────────────

struct CountyPickerSheet: View {
    @Binding var selected: TexasCounty?
    @Environment(\.dismiss) private var dismiss
    @State private var query = ""

    private var filtered: [TexasCounty] {
        query.trimmingCharacters(in: .whitespaces).isEmpty
            ? texasCounties
            : texasCounties.filter { $0.name.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        NavigationStack {
            List(filtered) { county in
                Button {
                    selected = county
                    dismiss()
                } label: {
                    HStack {
                        Text(county.displayName)
                        Spacer()
                        if selected?.code == county.code {
                            Image(systemName: "checkmark")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundStyle(.tint)
                        }
                    }
                    .foregroundStyle(.primary)
                }
            }
            .listStyle(.plain)
            .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: "Search counties")
            .navigationTitle("Select County")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                if selected != nil {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Clear") { selected = nil; dismiss() }
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }
}
