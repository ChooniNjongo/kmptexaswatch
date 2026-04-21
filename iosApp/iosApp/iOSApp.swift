import SwiftUI
import Shared

@main
struct iOSApp: App {
    private let onboardingHelper: OnboardingHelper

    init() {
        KoinHelperKt.doInitKoin()
        onboardingHelper = OnboardingHelper()
    }

    var body: some Scene {
        WindowGroup {
            RootView(onboardingHelper: onboardingHelper)
                .texasWatchTheme()
        }
    }
}

struct RootView: View {
    let onboardingHelper: OnboardingHelper
    @State private var onboardingComplete: Bool

    init(onboardingHelper: OnboardingHelper) {
        self.onboardingHelper = onboardingHelper
        _onboardingComplete = State(initialValue: onboardingHelper.isOnboardingComplete())
    }

    var body: some View {
        if onboardingComplete {
            HomeView()
        } else {
            OnboardingFlow(
                onboardingHelper: onboardingHelper,
                onComplete: { onboardingComplete = true }
            )
        }
    }
}

struct OnboardingFlow: View {
    let onboardingHelper: OnboardingHelper
    let onComplete: () -> Void
    @State private var step: OnboardingStep = .privacy

    enum OnboardingStep {
        case privacy, notifications
    }

    var body: some View {
        switch step {
        case .privacy:
            OnboardingPrivacyView(
                onDecline: { /* Stay on screen — user must accept to proceed */ },
                onAccept: { step = .notifications }
            )
        case .notifications:
            OnboardingNotificationsView(
                onDone: {
                    onboardingHelper.completeOnboarding()
                    onComplete()
                }
            )
        }
    }
}
