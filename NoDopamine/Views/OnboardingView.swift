import SwiftUI
import FamilyControls

/// 처음 실행할 때 스크린 타임 권한을 요청하는 화면
struct OnboardingView: View {
    @State private var isRequesting = false
    @State private var errorText: String?

    var body: some View {
        VStack(spacing: 28) {
            Spacer()

            Image(systemName: "leaf.circle.fill")
                .font(.system(size: 84))
                .foregroundStyle(Color.accentColor)

            VStack(spacing: 8) {
                Text("NoDopamine")
                    .font(.largeTitle.bold())
                Text("자극적인 앱과 사이트를 잠그고\n폰 대신 내 시간을 되찾아요.")
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)
            }

            VStack(alignment: .leading, spacing: 14) {
                FeatureRow(icon: "hand.raised.fill", text: "앱·웹사이트 차단 (지금 바로 / 매일 예약)")
                FeatureRow(icon: "timer", text: "집중 세션과 연속 달성 기록")
                FeatureRow(icon: "circle.lefthalf.filled", text: "흑백 화면 등 폰을 덜 자극적으로 바꾸기")
            }
            .padding(.horizontal)

            Spacer()

            if let errorText {
                Text(errorText)
                    .font(.footnote)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }

            Button {
                Task { await requestAuthorization() }
            } label: {
                Text(isRequesting ? "요청 중…" : "스크린 타임 권한 허용하기")
                    .font(.headline)
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .disabled(isRequesting)

            Text("차단 기능은 Apple 스크린 타임을 이용해요.\n어떤 앱을 썼는지는 기기 밖으로 나가지 않아요.")
                .font(.caption)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(24)
    }

    private func requestAuthorization() async {
        isRequesting = true
        defer { isRequesting = false }
        do {
            // .individual = 내 폰을 내가 관리 (자녀 보호용이 아닌 본인용)
            try await AuthorizationCenter.shared.requestAuthorization(for: .individual)
            errorText = nil
        } catch {
            errorText = "권한을 받지 못했어요. 다시 시도해 주세요.\n(\(error.localizedDescription))"
        }
    }
}

private struct FeatureRow: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .frame(width: 28)
                .foregroundStyle(Color.accentColor)
            Text(text)
        }
    }
}
