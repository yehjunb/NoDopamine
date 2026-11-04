import SwiftUI
import FamilyControls

/// 앱의 첫 화면. 스크린 타임 권한이 없으면 안내 화면을, 있으면 탭 3개를 보여줘요.
struct ContentView: View {
    @ObservedObject private var authorization = AuthorizationCenter.shared
    @StateObject private var block = BlockModel()
    @StateObject private var focus = FocusModel()
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        Group {
            if authorization.authorizationStatus == .approved {
                TabView {
                    ChallengeView()
                        .tabItem { Label("디톡스", systemImage: "leaf") }
                    BlockView()
                        .tabItem { Label("차단", systemImage: "hand.raised") }
                    SimplifyView()
                        .tabItem { Label("화면 단순화", systemImage: "circle.lefthalf.filled") }
                }
                .environmentObject(block)
                .environmentObject(focus)
            } else {
                OnboardingView()
            }
        }
        // 앱으로 돌아올 때마다: 끝난 집중 세션 기록, 차단 목록 최신화
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                focus.checkCompletion()
                block.refreshActiveShields()
            }
        }
    }
}
