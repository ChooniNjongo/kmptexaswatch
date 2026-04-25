import MapKit
import SwiftUI

// ── Route Screen ──────────────────────────────────────────────────────────────

struct RouteView: View {
    @Environment(\.twColors) private var colors
    @Environment(\.twTypography) private var typography
    @StateObject private var vm = RouteViewModel()

    var body: some View {
        ZStack(alignment: .top) {

            // ── Map ───────────────────────────────────────────────────────────
            MapReader { proxy in
                Map(position: $vm.cameraPosition) {
                    UserAnnotation()

                    if let route = vm.route {
                        MapPolyline(route.polyline)
                            .stroke(colors.primaryAccent, lineWidth: 5)
                    }

                    ForEach(vm.pins) { pin in
                        Annotation("", coordinate: pin.coordinate) {
                            OffenderMapPin(pin: pin, isSelected: vm.selectedPin?.id == pin.id)
                                .onTapGesture { vm.selectedPin = pin }
                        }
                    }

                    if let coord = vm.startCoord {
                        Annotation("Start", coordinate: coord) {
                            RouteEndpointMarker(label: "A", color: colors.successBadge,
                                               isActive: vm.pickingMode == .start)
                                .onTapGesture { vm.rePickStart() }
                        }
                    }

                    if let coord = vm.endCoord {
                        Annotation("End", coordinate: coord) {
                            RouteEndpointMarker(label: "B", color: colors.dangerBadge,
                                               isActive: vm.pickingMode == .end)
                                .onTapGesture { vm.rePickEnd() }
                        }
                    }
                }
                .mapStyle(.standard(elevation: .realistic))
                .mapControls {
                    MapUserLocationButton()
                    MapCompass()
                    MapScaleView()
                }
                .ignoresSafeArea(edges: .bottom)
                .onMapCameraChange { context in
                    vm.visibleRegion = context.region
                }
                .onTapGesture { screenPoint in
                    guard let coord = proxy.convert(screenPoint, from: .local) else { return }
                    vm.tapCoordinate(coord)
                }
            }

            // ── Top instruction banner ────────────────────────────────────────
            instructionBanner
                .padding(.top, 8)

            // ── Zoom buttons ─────────────────────────────────────────────────
            HStack {
                Spacer()
                VStack(spacing: 0) {
                    Button {
                        withAnimation { vm.zoomIn() }
                    } label: {
                        Image(systemName: "plus")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundStyle(colors.primaryText)
                            .frame(width: 44, height: 44)
                    }
                    Divider().frame(width: 44)
                    Button {
                        withAnimation { vm.zoomOut() }
                    } label: {
                        Image(systemName: "minus")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundStyle(colors.primaryText)
                            .frame(width: 44, height: 44)
                    }
                }
                .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 10))
                .shadow(color: .black.opacity(0.15), radius: 4, x: 0, y: 2)
                .padding(.trailing, 12)
                .padding(.top, 60)
            }

            // ── Clear button (bottom-right) ───────────────────────────────────
            if vm.startCoord != nil || vm.endCoord != nil {
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Button { vm.clearRoute() } label: {
                            VStack(spacing: 3) {
                                Image(systemName: "xmark.circle")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundStyle(colors.dangerText)
                                Text("Clear")
                                    .font(typography.label)
                                    .foregroundStyle(colors.dangerText)
                            }
                            .padding(.horizontal, 18)
                            .padding(.vertical, 10)
                            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
                            .shadow(color: .black.opacity(0.12), radius: 8, x: 0, y: 4)
                        }
                        .padding(.trailing, 16)
                        .padding(.bottom, 110)
                    }
                }
            }

            // ── Status pills ──────────────────────────────────────────────────
            statusOverlay
        }
        .navigationTitle("Route")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { vm.requestLocation() }
        .sheet(item: $vm.selectedPin) { (pin: OffenderPin) in
            OffenderPinSheet(pin: pin)
                .presentationDetents([.fraction(0.85)])
                .presentationDragIndicator(.visible)
        }
    }

    // MARK: - Instruction banner

    @ViewBuilder
    private var instructionBanner: some View {
        switch vm.pickingMode {
        case .start, .end:
            bannerPill(icon: "mappin.circle", text: "Pick a start and end point to search offenders along a route", color: colors.primaryAccent)
        case .none:
            EmptyView()
        }
    }

    private func bannerPill(icon: String, text: String, color: Color) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundStyle(color)
                .font(.system(size: 14, weight: .semibold))
            Text(text)
                .font(typography.text2.weight(.medium))
                .foregroundStyle(colors.primaryText)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(.regularMaterial, in: Capsule())
        .shadow(color: .black.opacity(0.15), radius: 6, x: 0, y: 3)
    }

    // MARK: - Status overlay

    @ViewBuilder
    private var statusOverlay: some View {
        VStack {
            Spacer()
            switch vm.step {
            case .calculating:
                statusPill(icon: "arrow.triangle.turn.up.right.diamond", text: "Calculating route…", loading: true)
                    .padding(.bottom, 185)
            case .loadingPins:
                statusPill(icon: "person.2.fill", text: "Loading offenders along route…", loading: true)
                    .padding(.bottom, 185)
            case .ready:
                if vm.pins.isEmpty {
                    statusPill(icon: "checkmark.circle.fill", text: "No offenders along this route", loading: false)
                        .padding(.bottom, 185)
                } else {
                    statusPill(icon: "exclamationmark.triangle.fill", text: "\(vm.pins.count) offenders along this route", loading: false)
                        .padding(.bottom, 185)
                }
            case .error(let msg):
                statusPill(icon: "xmark.circle.fill", text: msg, loading: false)
                    .padding(.bottom, 185)
            default:
                EmptyView()
            }
        }
    }

    private func statusPill(icon: String, text: String, loading: Bool) -> some View {
        HStack(spacing: 8) {
            if loading {
                ProgressView().tint(.white).scaleEffect(0.85)
            } else {
                Image(systemName: icon)
                    .foregroundStyle(.white)
                    .font(.system(size: 13, weight: .semibold))
            }
            Text(text)
                .font(typography.text2.weight(.medium))
                .foregroundStyle(.white)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(colors.primaryAccent.opacity(0.92), in: Capsule())
        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
    }
}

// ── Route endpoint marker (A / B) ─────────────────────────────────────────────

private struct RouteEndpointMarker: View {
    let label: String
    let color: Color
    var isActive: Bool = false

    var body: some View {
        ZStack {
            Circle()
                .fill(color)
                .frame(width: isActive ? 40 : 32, height: isActive ? 40 : 32)
                .shadow(color: .black.opacity(0.3), radius: 3, x: 0, y: 2)
            Text(label)
                .font(.system(size: isActive ? 17 : 14, weight: .black))
                .foregroundStyle(.white)
        }
        .animation(.spring(response: 0.2, dampingFraction: 0.7), value: isActive)
    }
}
