import SwiftUI

/// 둥근 카드 모양 상자
struct Card<Content: View>: View {
    @ViewBuilder var content: Content

    var body: some View {
        content
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.secondarySystemGroupedBackground),
                        in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

/// 숫자 하나를 크게 보여주는 작은 타일
struct StatTile: View {
    let icon: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundStyle(Color.accentColor)
            Text(value)
                .font(.title2.bold())
                .monospacedDigit()
            Text(label)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(Color(.secondarySystemGroupedBackground),
                    in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}

/// 차단을 풀거나 세션을 포기하기 전에 10초 동안 기다리게 하는 화면.
/// "충동"은 보통 몇 초면 지나가요. 그 몇 초를 벌어주는 장치예요.
struct CooldownSheet: View {
    let title: String
    let message: String
    let confirmTitle: String
    let onConfirm: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var secondsLeft = 10
    private let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "wind")
                .font(.system(size: 44))
                .foregroundStyle(Color.accentColor)
            Text(title)
                .font(.title2.bold())
            Text(message)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)

            Text(secondsLeft > 0 ? "\(secondsLeft)초만 더 생각해 봐요" : "그래도 원한다면 괜찮아요")
                .font(.headline)
                .monospacedDigit()

            Button {
                dismiss()
            } label: {
                Text("계속 지키기").frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)

            Button(role: .destructive) {
                onConfirm()
                dismiss()
            } label: {
                Text(confirmTitle).frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .controlSize(.large)
            .disabled(secondsLeft > 0)
        }
        .padding(24)
        .onReceive(timer) { _ in
            if secondsLeft > 0 { secondsLeft -= 1 }
        }
        .presentationDetents([.medium])
    }
}
