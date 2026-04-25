import Shared

// Swift wrapper around the KMP TexasCounty — adds displayName and Identifiable
extension TexasCounty: @retroactive Identifiable {
    public var id: String { code }
    var displayName: String { name.capitalized }
}

// Lowercase alias matching SearchFilterView usage
let texasCounties: [TexasCounty] = TexasCountiesKt.TEXAS_COUNTIES as [TexasCounty]
