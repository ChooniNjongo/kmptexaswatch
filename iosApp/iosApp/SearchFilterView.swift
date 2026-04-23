import SwiftUI

// ── Data model ────────────────────────────────────────────────────────────────

private enum FilterGroup: String, CaseIterable {
    case offenderLevel = "Offender Level"
    case race          = "Race"
    case hairColor     = "Hair Color"
    case eyeColor      = "Eye Color"
}

private struct FilterChip: Identifiable, Hashable {
    let id = UUID()
    let group: FilterGroup
    let label: String
    var selected: Bool = false
}

private func defaultFilters() -> [FilterChip] {[
    // Offender Level
    .init(group: .offenderLevel, label: "Low Risk"),
    .init(group: .offenderLevel, label: "Moderate Risk"),
    .init(group: .offenderLevel, label: "High Risk"),
    // Race
    .init(group: .race, label: "White"),
    .init(group: .race, label: "Black"),
    .init(group: .race, label: "Asian"),
    .init(group: .race, label: "American Indian"),
    .init(group: .race, label: "Other"),
    // Hair Color
    .init(group: .hairColor, label: "Black"),
    .init(group: .hairColor, label: "Brown"),
    .init(group: .hairColor, label: "Blonde"),
    .init(group: .hairColor, label: "Red"),
    .init(group: .hairColor, label: "Gray"),
    .init(group: .hairColor, label: "White"),
    .init(group: .hairColor, label: "Bald"),
    // Eye Color
    .init(group: .eyeColor, label: "Brown"),
    .init(group: .eyeColor, label: "Blue"),
    .init(group: .eyeColor, label: "Green"),
    .init(group: .eyeColor, label: "Hazel"),
    .init(group: .eyeColor, label: "Gray"),
    .init(group: .eyeColor, label: "Black"),
    .init(group: .eyeColor, label: "Maroon"),
]}

// ── County data (mirrors shared TexasCounties.kt) ─────────────────────────────

struct TexasCounty: Identifiable, Equatable {
    var id: String { code }
    let code: String
    let name: String
    var displayName: String { name.capitalized }
}

let texasCounties: [TexasCounty] = [
    .init(code: "001", name: "ANDERSON"),
    .init(code: "002", name: "ANDREWS"),
    .init(code: "003", name: "ANGELINA"),
    .init(code: "004", name: "ARANSAS"),
    .init(code: "005", name: "ARCHER"),
    .init(code: "006", name: "ARMSTRONG"),
    .init(code: "007", name: "ATASCOSA"),
    .init(code: "008", name: "AUSTIN"),
    .init(code: "009", name: "BAILEY"),
    .init(code: "010", name: "BANDERA"),
    .init(code: "011", name: "BASTROP"),
    .init(code: "012", name: "BAYLOR"),
    .init(code: "013", name: "BEE"),
    .init(code: "014", name: "BELL"),
    .init(code: "015", name: "BEXAR"),
    .init(code: "016", name: "BLANCO"),
    .init(code: "017", name: "BORDEN"),
    .init(code: "018", name: "BOSQUE"),
    .init(code: "019", name: "BOWIE"),
    .init(code: "020", name: "BRAZORIA"),
    .init(code: "021", name: "BRAZOS"),
    .init(code: "022", name: "BREWSTER"),
    .init(code: "023", name: "BRISCOE"),
    .init(code: "024", name: "BROOKS"),
    .init(code: "025", name: "BROWN"),
    .init(code: "026", name: "BURLESON"),
    .init(code: "027", name: "BURNET"),
    .init(code: "028", name: "CALDWELL"),
    .init(code: "029", name: "CALHOUN"),
    .init(code: "030", name: "CALLAHAN"),
    .init(code: "031", name: "CAMERON"),
    .init(code: "032", name: "CAMP"),
    .init(code: "033", name: "CARSON"),
    .init(code: "034", name: "CASS"),
    .init(code: "035", name: "CASTRO"),
    .init(code: "036", name: "CHAMBERS"),
    .init(code: "037", name: "CHEROKEE"),
    .init(code: "038", name: "CHILDRESS"),
    .init(code: "039", name: "CLAY"),
    .init(code: "040", name: "COCHRAN"),
    .init(code: "041", name: "COKE"),
    .init(code: "042", name: "COLEMAN"),
    .init(code: "043", name: "COLLIN"),
    .init(code: "044", name: "COLLINGSWORTH"),
    .init(code: "045", name: "COLORADO"),
    .init(code: "046", name: "COMAL"),
    .init(code: "047", name: "COMANCHE"),
    .init(code: "048", name: "CONCHO"),
    .init(code: "049", name: "COOKE"),
    .init(code: "050", name: "CORYELL"),
    .init(code: "051", name: "COTTLE"),
    .init(code: "052", name: "CRANE"),
    .init(code: "053", name: "CROCKETT"),
    .init(code: "054", name: "CROSBY"),
    .init(code: "055", name: "CULBERSON"),
    .init(code: "056", name: "DALLAM"),
    .init(code: "057", name: "DALLAS"),
    .init(code: "058", name: "DAWSON"),
    .init(code: "059", name: "DEAF SMITH"),
    .init(code: "060", name: "DELTA"),
    .init(code: "061", name: "DENTON"),
    .init(code: "062", name: "DEWITT"),
    .init(code: "063", name: "DICKENS"),
    .init(code: "064", name: "DIMMIT"),
    .init(code: "065", name: "DONLEY"),
    .init(code: "066", name: "DUVAL"),
    .init(code: "067", name: "EASTLAND"),
    .init(code: "068", name: "ECTOR"),
    .init(code: "069", name: "EDWARDS"),
    .init(code: "070", name: "ELLIS"),
    .init(code: "071", name: "EL PASO"),
    .init(code: "072", name: "ERATH"),
    .init(code: "073", name: "FALLS"),
    .init(code: "074", name: "FANNIN"),
    .init(code: "075", name: "FAYETTE"),
    .init(code: "076", name: "FISHER"),
    .init(code: "077", name: "FLOYD"),
    .init(code: "078", name: "FOARD"),
    .init(code: "079", name: "FORT BEND"),
    .init(code: "080", name: "FRANKLIN"),
    .init(code: "081", name: "FREESTONE"),
    .init(code: "082", name: "FRIO"),
    .init(code: "083", name: "GAINES"),
    .init(code: "084", name: "GALVESTON"),
    .init(code: "085", name: "GARZA"),
    .init(code: "086", name: "GILLESPIE"),
    .init(code: "087", name: "GLASSCOCK"),
    .init(code: "088", name: "GOLIAD"),
    .init(code: "089", name: "GONZALES"),
    .init(code: "090", name: "GRAY"),
    .init(code: "091", name: "GRAYSON"),
    .init(code: "092", name: "GREGG"),
    .init(code: "093", name: "GRIMES"),
    .init(code: "094", name: "GUADALUPE"),
    .init(code: "095", name: "HALE"),
    .init(code: "096", name: "HALL"),
    .init(code: "097", name: "HAMILTON"),
    .init(code: "098", name: "HANSFORD"),
    .init(code: "099", name: "HARDEMAN"),
    .init(code: "100", name: "HARDIN"),
    .init(code: "101", name: "HARRIS"),
    .init(code: "102", name: "HARRISON"),
    .init(code: "103", name: "HARTLEY"),
    .init(code: "104", name: "HASKELL"),
    .init(code: "105", name: "HAYS"),
    .init(code: "106", name: "HEMPHILL"),
    .init(code: "107", name: "HENDERSON"),
    .init(code: "108", name: "HIDALGO"),
    .init(code: "109", name: "HILL"),
    .init(code: "110", name: "HOCKLEY"),
    .init(code: "111", name: "HOOD"),
    .init(code: "112", name: "HOPKINS"),
    .init(code: "113", name: "HOUSTON"),
    .init(code: "114", name: "HOWARD"),
    .init(code: "115", name: "HUDSPETH"),
    .init(code: "116", name: "HUNT"),
    .init(code: "117", name: "HUTCHINSON"),
    .init(code: "118", name: "IRION"),
    .init(code: "119", name: "JACK"),
    .init(code: "120", name: "JACKSON"),
    .init(code: "121", name: "JASPER"),
    .init(code: "122", name: "JEFF DAVIS"),
    .init(code: "123", name: "JEFFERSON"),
    .init(code: "124", name: "JIM HOGG"),
    .init(code: "125", name: "JIM WELLS"),
    .init(code: "126", name: "JOHNSON"),
    .init(code: "127", name: "JONES"),
    .init(code: "128", name: "KARNES"),
    .init(code: "129", name: "KAUFMAN"),
    .init(code: "130", name: "KENDALL"),
    .init(code: "131", name: "KENEDY"),
    .init(code: "132", name: "KENT"),
    .init(code: "133", name: "KERR"),
    .init(code: "134", name: "KIMBLE"),
    .init(code: "135", name: "KING"),
    .init(code: "136", name: "KINNEY"),
    .init(code: "137", name: "KLEBERG"),
    .init(code: "138", name: "KNOX"),
    .init(code: "139", name: "LAMAR"),
    .init(code: "140", name: "LAMB"),
    .init(code: "141", name: "LAMPASAS"),
    .init(code: "142", name: "LA SALLE"),
    .init(code: "143", name: "LAVACA"),
    .init(code: "144", name: "LEE"),
    .init(code: "145", name: "LEON"),
    .init(code: "146", name: "LIBERTY"),
    .init(code: "147", name: "LIMESTONE"),
    .init(code: "148", name: "LIPSCOMB"),
    .init(code: "149", name: "LIVE OAK"),
    .init(code: "150", name: "LLANO"),
    .init(code: "151", name: "LOVING"),
    .init(code: "152", name: "LUBBOCK"),
    .init(code: "153", name: "LYNN"),
    .init(code: "154", name: "MCCULLOCH"),
    .init(code: "155", name: "MCLENNAN"),
    .init(code: "156", name: "MCMULLEN"),
    .init(code: "157", name: "MADISON"),
    .init(code: "158", name: "MARION"),
    .init(code: "159", name: "MARTIN"),
    .init(code: "160", name: "MASON"),
    .init(code: "161", name: "MATAGORDA"),
    .init(code: "162", name: "MAVERICK"),
    .init(code: "163", name: "MEDINA"),
    .init(code: "164", name: "MENARD"),
    .init(code: "165", name: "MIDLAND"),
    .init(code: "166", name: "MILAM"),
    .init(code: "167", name: "MILLS"),
    .init(code: "168", name: "MITCHELL"),
    .init(code: "169", name: "MONTAGUE"),
    .init(code: "170", name: "MONTGOMERY"),
    .init(code: "171", name: "MOORE"),
    .init(code: "172", name: "MORRIS"),
    .init(code: "173", name: "MOTLEY"),
    .init(code: "174", name: "NACOGDOCHES"),
    .init(code: "175", name: "NAVARRO"),
    .init(code: "176", name: "NEWTON"),
    .init(code: "177", name: "NOLAN"),
    .init(code: "178", name: "NUECES"),
    .init(code: "179", name: "OCHILTREE"),
    .init(code: "180", name: "OLDHAM"),
    .init(code: "181", name: "ORANGE"),
    .init(code: "182", name: "PALO PINTO"),
    .init(code: "183", name: "PANOLA"),
    .init(code: "184", name: "PARKER"),
    .init(code: "185", name: "PARMER"),
    .init(code: "186", name: "PECOS"),
    .init(code: "187", name: "POLK"),
    .init(code: "188", name: "POTTER"),
    .init(code: "189", name: "PRESIDIO"),
    .init(code: "190", name: "RAINS"),
    .init(code: "191", name: "RANDALL"),
    .init(code: "192", name: "REAGAN"),
    .init(code: "193", name: "REAL"),
    .init(code: "194", name: "RED RIVER"),
    .init(code: "195", name: "REEVES"),
    .init(code: "196", name: "REFUGIO"),
    .init(code: "197", name: "ROBERTS"),
    .init(code: "198", name: "ROBERTSON"),
    .init(code: "199", name: "ROCKWALL"),
    .init(code: "200", name: "RUNNELS"),
    .init(code: "201", name: "RUSK"),
    .init(code: "202", name: "SABINE"),
    .init(code: "203", name: "SAN AUGUSTINE"),
    .init(code: "204", name: "SAN JACINTO"),
    .init(code: "205", name: "SAN PATRICIO"),
    .init(code: "206", name: "SAN SABA"),
    .init(code: "207", name: "SCHLEICHER"),
    .init(code: "208", name: "SCURRY"),
    .init(code: "209", name: "SHACKELFORD"),
    .init(code: "210", name: "SHELBY"),
    .init(code: "211", name: "SHERMAN"),
    .init(code: "212", name: "SMITH"),
    .init(code: "213", name: "SOMERVELL"),
    .init(code: "214", name: "STARR"),
    .init(code: "215", name: "STEPHENS"),
    .init(code: "216", name: "STERLING"),
    .init(code: "217", name: "STONEWALL"),
    .init(code: "218", name: "SUTTON"),
    .init(code: "219", name: "SWISHER"),
    .init(code: "220", name: "TARRANT"),
    .init(code: "221", name: "TAYLOR"),
    .init(code: "222", name: "TERRELL"),
    .init(code: "223", name: "TERRY"),
    .init(code: "224", name: "THROCKMORTON"),
    .init(code: "225", name: "TITUS"),
    .init(code: "226", name: "TOM GREEN"),
    .init(code: "227", name: "TRAVIS"),
    .init(code: "228", name: "TRINITY"),
    .init(code: "229", name: "TYLER"),
    .init(code: "230", name: "UPSHUR"),
    .init(code: "231", name: "UPTON"),
    .init(code: "232", name: "UVALDE"),
    .init(code: "233", name: "VAL VERDE"),
    .init(code: "234", name: "VAN ZANDT"),
    .init(code: "235", name: "VICTORIA"),
    .init(code: "236", name: "WALKER"),
    .init(code: "237", name: "WALLER"),
    .init(code: "238", name: "WARD"),
    .init(code: "239", name: "WASHINGTON"),
    .init(code: "240", name: "WEBB"),
    .init(code: "241", name: "WHARTON"),
    .init(code: "242", name: "WHEELER"),
    .init(code: "243", name: "WICHITA"),
    .init(code: "244", name: "WILBARGER"),
    .init(code: "245", name: "WILLACY"),
    .init(code: "246", name: "WILLIAMSON"),
    .init(code: "247", name: "WILSON"),
    .init(code: "248", name: "WINKLER"),
    .init(code: "249", name: "WISE"),
    .init(code: "250", name: "WOOD"),
    .init(code: "251", name: "YOAKUM"),
    .init(code: "252", name: "YOUNG"),
    .init(code: "253", name: "ZAPATA"),
    .init(code: "254", name: "ZAVALA"),
]

// ── SearchFilterView ──────────────────────────────────────────────────────────

struct SearchFilterView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    @Environment(\.dismiss) private var dismiss

    @State private var searchQuery = ""
    @State private var filters = defaultFilters()
    @State private var selectedCounty: TexasCounty? = nil
    @State private var showCountySheet = false
    @State private var filtersExpanded = true

    private var selectedCount: Int { filters.filter { $0.selected }.count }
    private var buttonLabel: String {
        (selectedCount == 0 && searchQuery.isEmpty && selectedCounty == nil)
            ? "Search all offenders"
            : "See matching offenders now!"
    }

    var body: some View {
        VStack(spacing: 0) {
            // ── Search bar ────────────────────────────────────────────────────
            HStack(spacing: 0) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(colors.secondaryText)
                    .padding(.leading, 16)
                    .padding(.trailing, 8)

                TextField("Search offenders...", text: $searchQuery)
                    .font(typography.text1)
                    .foregroundStyle(colors.primaryText)
                    .submitLabel(.search)
                    .padding(.vertical, 12)

                if !searchQuery.isEmpty {
                    Button { searchQuery = "" } label: {
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
            .frame(maxWidth: .infinity)
            .background(colors.mainBackground)

            Divider().background(colors.strokePale)

            // ── Scrollable filter body ─────────────────────────────────────────
            ScrollView {
                VStack(spacing: 12) {
                    FilterPanel(
                        filters: $filters,
                        expanded: $filtersExpanded,
                        selectedCounty: $selectedCounty,
                        onCountyTap: { showCountySheet = true }
                    )
                }
                .padding(12)
            }

            // ── Bottom CTA ────────────────────────────────────────────────────
            Button {
                // TODO: trigger search
            } label: {
                Text(buttonLabel)
                    .font(typography.h4)
                    .foregroundStyle(colors.invertedText)
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                    .background(colors.primaryAccent)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
        .background(colors.mainBackground.ignoresSafeArea())
        .sheet(isPresented: $showCountySheet) {
            CountyPickerSheet(selected: $selectedCounty)
        }
    }
}

// ── Filter panel card ─────────────────────────────────────────────────────────

private struct FilterPanel: View {
    @Binding var filters: [FilterChip]
    @Binding var expanded: Bool
    @Binding var selectedCounty: TexasCounty?
    let onCountyTap: () -> Void

    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        VStack(spacing: 0) {
            // Header row
            Button {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                    expanded.toggle()
                }
            } label: {
                HStack(spacing: 8) {
                    Text("Filter by tags")
                        .font(typography.h4)
                        .foregroundStyle(colors.primaryText)
                    Spacer()
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
                VStack(spacing: 12) {
                    Divider().background(colors.strokePale)
                        .padding(.horizontal, 12)

                    // County picker row
                    FilterSectionHeader(title: "County")
                    CountyPickerRow(
                        selected: selectedCounty,
                        onTap: onCountyTap
                    )
                    .padding(.horizontal, 12)

                    // Tag filter groups
                    ForEach(FilterGroup.allCases, id: \.self) { group in
                        let chips = filters.filter { $0.group == group }
                        FilterGroupSection(
                            title: group.rawValue,
                            chips: chips,
                            onToggle: { chip in
                                if let idx = filters.firstIndex(where: {
                                    $0.group == chip.group && $0.label == chip.label
                                }) {
                                    filters[idx].selected.toggle()
                                }
                            }
                        )
                    }
                }
                .padding(.bottom, 16)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(colors.strokePale, lineWidth: 0.5)
        )
    }
}

// ── County picker row (tappable dropdown trigger) ─────────────────────────────

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
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 8))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(
                        selected != nil ? colors.primaryAccent : colors.strokeFull,
                        lineWidth: selected != nil ? 1.5 : 0.5
                    )
            )
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

private struct FilterSectionHeader: View {
    let title: String
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        HStack {
            Text(title)
                .font(typography.text2)
                .foregroundStyle(colors.secondaryText)
            Spacer()
        }
        .padding(.horizontal, 12)
    }
}

// ── Filter group with flow-wrap glass chips ───────────────────────────────────

private struct FilterGroupSection: View {
    let title: String
    let chips: [FilterChip]
    let onToggle: (FilterChip) -> Void

    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            FilterSectionHeader(title: title)
            FlowLayout(spacing: 8) {
                ForEach(chips) { chip in
                    GlassChip(label: chip.label, selected: chip.selected) {
                        onToggle(chip)
                    }
                }
            }
            .padding(.horizontal, 12)
        }
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
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background {
                    if selected {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(colors.primaryAccent)
                    } else {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(.regularMaterial)
                    }
                }
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .strokeBorder(
                            selected ? colors.primaryAccent : colors.strokeFull.opacity(0.4),
                            lineWidth: selected ? 0 : 0.5
                        )
                )
        }
        .buttonStyle(.plain)
        .animation(.spring(response: 0.25, dampingFraction: 0.75), value: selected)
    }
}

// ── Flow layout (wrapping HStack) ─────────────────────────────────────────────

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > maxWidth && x > 0 {
                y += rowHeight + spacing
                x = 0
                rowHeight = 0
            }
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        return CGSize(width: maxWidth, height: y + rowHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX && x > bounds.minX {
                y += rowHeight + spacing
                x = bounds.minX
                rowHeight = 0
            }
            view.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(width: size.width, height: size.height))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}

// ── County picker bottom sheet ────────────────────────────────────────────────

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
                        Button("Clear") {
                            selected = nil
                            dismiss()
                        }
                        .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }
}
