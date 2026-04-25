import MapKit
import Shared
import SwiftUI

// ── Map Screen ────────────────────────────────────────────────────────────────

struct MapView: View {
    @Environment(\.twColors) private var colors
    @StateObject private var vm = MapViewModel()
    @State private var selectedPin: OffenderPin? = nil

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            Map(position: Binding(
                get: { .region(vm.cameraRegion) },
                set: { _ in }
            )) {
                // User location blue dot
                UserAnnotation()

                // Offender pins
                ForEach(vm.pins) { pin in
                    Annotation("", coordinate: pin.coordinate) {
                        OffenderMapPin(pin: pin, isSelected: selectedPin?.id == pin.id)
                            .onTapGesture { selectedPin = pin }
                    }
                }
            }
            .mapStyle(.standard(elevation: .realistic))
            .mapControls {
                MapUserLocationButton()
                MapCompass()
                MapScaleView()
            }
            .onMapCameraChange(frequency: .onEnd) { context in
                vm.visibleRegion = context.region
                vm.loadPins(for: context.region.center)
            }
            .ignoresSafeArea(edges: .bottom)

            // ── Zoom buttons ──────────────────────────────────────────────────
            VStack(spacing: 0) {
                Button { vm.zoomIn() } label: {
                    Image(systemName: "plus")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundStyle(.primary)
                        .frame(width: 44, height: 44)
                }
                Divider().frame(width: 44)
                Button { vm.zoomOut() } label: {
                    Image(systemName: "minus")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundStyle(.primary)
                        .frame(width: 44, height: 44)
                }
            }
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 10))
            .shadow(color: .black.opacity(0.15), radius: 4, x: 0, y: 2)
            .padding(.trailing, 12)
            .padding(.bottom, 160)

            // ── Loading indicator ─────────────────────────────────────────────
            if vm.isLoading {
                HStack(spacing: 8) {
                    ProgressView()
                        .tint(.white)
                        .scaleEffect(0.9)
                    Text("Loading offenders…")
                        .font(.caption)
                        .foregroundStyle(.white)
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(.ultraThinMaterial, in: Capsule())
                .padding(.bottom, 100)
                .padding(.trailing, 16)
            }

            // ── Pin count badge ───────────────────────────────────────────────
            if !vm.pins.isEmpty && !vm.isLoading {
                Text("\(vm.pins.count) offenders nearby")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(.ultraThinMaterial, in: Capsule())
                    .padding(.bottom, 100)
                    .padding(.trailing, 16)
            }
        }
        .navigationTitle("Map")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            vm.requestLocation()
        }
        // ── Selected pin sheet ────────────────────────────────────────────────
        .sheet(item: $selectedPin) { pin in
            OffenderPinSheet(pin: pin)
                .presentationDetents([.fraction(0.85)])
                .presentationDragIndicator(.visible)
        }
    }
}

// ── Circular photo pin ────────────────────────────────────────────────────────

struct OffenderMapPin: View {
    let pin: OffenderPin
    let isSelected: Bool
    @Environment(\.twColors) private var colors
    @State private var uiImage: UIImage? = nil

    var body: some View {
        ZStack {
            // Outer ring
            Circle()
                .fill(isSelected ? colors.dangerBadge : colors.dangerBadge.opacity(0.85))
                .frame(width: isSelected ? 52 : 44, height: isSelected ? 52 : 44)
                .shadow(color: .black.opacity(0.35), radius: 4, x: 0, y: 2)

            // Photo or initials
            if let img = uiImage {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFill()
                    .frame(width: isSelected ? 44 : 36, height: isSelected ? 44 : 36)
                    .clipShape(Circle())
            } else {
                // Initials fallback
                let initials: String = {
                    let parts = pin.fullName.split(separator: " ")
                    let f = parts.first?.first.map { String($0) } ?? ""
                    let l = parts.dropFirst().first?.first.map { String($0) } ?? ""
                    return (f + l).uppercased()
                }()
                Text(initials.isEmpty ? "?" : initials)
                    .font(.system(size: isSelected ? 16 : 13, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(width: isSelected ? 44 : 36, height: isSelected ? 44 : 36)
                    .background(Circle().fill(colors.primaryAccent))
            }

            // Small triangle pointer at bottom
            Triangle()
                .fill(isSelected ? colors.dangerBadge : colors.dangerBadge.opacity(0.85))
                .frame(width: 10, height: 6)
                .offset(y: isSelected ? 29 : 25)
        }
        .animation(.spring(response: 0.25, dampingFraction: 0.7), value: isSelected)
        .task(id: pin.photoUrl) {
            guard let urlStr = pin.photoUrl, let url = URL(string: urlStr) else { return }
            guard let (data, _) = try? await URLSession.shared.data(from: url),
                  let img = UIImage(data: data) else { return }
            uiImage = img
        }
    }
}

// ── Triangle shape for pin pointer ───────────────────────────────────────────

private struct Triangle: Shape {
    func path(in rect: CGRect) -> Path {
        Path { p in
            p.move(to: CGPoint(x: rect.midX, y: rect.maxY))
            p.addLine(to: CGPoint(x: rect.minX, y: rect.minY))
            p.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
            p.closeSubpath()
        }
    }
}

// ── Bottom sheet when pin is tapped ──────────────────────────────────────────

struct OffenderPinSheet: View {
    let pin: OffenderPin

    var body: some View {
        NavigationStack {
            OffenderDetailView(indIdn: pin.id, offenderName: pin.fullName.capitalized)
        }
    }
}
