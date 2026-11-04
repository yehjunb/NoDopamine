import SwiftUI
import UIKit

/// "화면 단순화" 탭.
///
/// iOS 는 보안 때문에 다른 앱이 화면 색이나 홈 화면을 직접 바꾸지 못하게 막아요.
/// 그래서 이 탭은 설정 방법을 단계별로 알려주고, 해둔 것을 체크해 두는 체크리스트예요.
struct SimplifyView: View {
    @AppStorage("simplify.done") private var doneRaw = ""

    private var done: Set<String> {
        Set(doneRaw.split(separator: ",").map(String.init))
    }

    var body: some View {
        NavigationStack {
            List {
                Section {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("\(done.intersection(SimplifyTip.all.map(\.id)).count) / \(SimplifyTip.all.count) 완료")
                            .font(.headline)
                        ProgressView(value: Double(done.intersection(SimplifyTip.all.map(\.id)).count),
                                     total: Double(SimplifyTip.all.count))
                        Text("색이 사라지고 화면이 단순해지면, 앱이 주는 자극이 확 줄어들어요. 하나씩 따라 해보세요.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                    .padding(.vertical, 4)

                    Button {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                    } label: {
                        Label("설정 앱 열기", systemImage: "gear")
                    }
                } footer: {
                    Text("설정 앱이 이 앱의 설정 페이지로 열리면, 왼쪽 위 뒤로 가기를 눌러 첫 화면으로 가세요.")
                }

                ForEach(SimplifyTip.all) { tip in
                    Section {
                        TipRow(tip: tip,
                               isDone: done.contains(tip.id),
                               toggle: { toggle(tip.id) })
                    }
                }
            }
            .navigationTitle("화면 단순화")
        }
    }

    private func toggle(_ id: String) {
        var set = done
        if set.contains(id) { set.remove(id) } else { set.insert(id) }
        doneRaw = set.sorted().joined(separator: ",")
    }
}

private struct TipRow: View {
    let tip: SimplifyTip
    let isDone: Bool
    let toggle: () -> Void

    @State private var isExpanded = false

    var body: some View {
        DisclosureGroup(isExpanded: $isExpanded) {
            VStack(alignment: .leading, spacing: 10) {
                Text(tip.why)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                ForEach(Array(tip.steps.enumerated()), id: \.offset) { index, step in
                    HStack(alignment: .top, spacing: 10) {
                        Text("\(index + 1)")
                            .font(.caption.bold())
                            .frame(width: 22, height: 22)
                            .background(Color.accentColor.opacity(0.15), in: Circle())
                        Text(step)
                            .font(.subheadline)
                    }
                }
                Button {
                    toggle()
                } label: {
                    Label(isDone ? "완료 취소" : "다 했어요", systemImage: isDone ? "arrow.uturn.backward" : "checkmark")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .padding(.top, 4)
            }
            .padding(.vertical, 6)
        } label: {
            HStack(spacing: 12) {
                Image(systemName: isDone ? "checkmark.circle.fill" : tip.icon)
                    .font(.title3)
                    .foregroundStyle(isDone ? Color.green : Color.accentColor)
                    .frame(width: 28)
                Text(tip.title)
                    .strikethrough(isDone, color: .secondary)
                    .foregroundStyle(isDone ? Color.secondary : Color.primary)
            }
        }
    }
}

struct SimplifyTip: Identifiable {
    let id: String
    let icon: String
    let title: String
    let why: String
    let steps: [String]

    static let all: [SimplifyTip] = [
        SimplifyTip(
            id: "grayscale",
            icon: "circle.lefthalf.filled",
            title: "화면을 흑백으로 바꾸기",
            why: "빨간 알림 배지, 화려한 썸네일은 뇌를 자극하도록 만들어졌어요. 흑백으로 바꾸면 폰이 금방 지루해져요.",
            steps: [
                "설정 > 손쉬운 사용 > 디스플레이 및 텍스트 크기로 가요.",
                "'색상 필터'를 눌러 켜요.",
                "'흑백'을 선택해요.",
            ]
        ),
        SimplifyTip(
            id: "grayscale-shortcut",
            icon: "button.horizontal.top.press",
            title: "측면 버튼 세 번으로 흑백 켜고 끄기",
            why: "사진을 볼 때처럼 색이 꼭 필요할 때만 잠깐 켤 수 있어요.",
            steps: [
                "설정 > 손쉬운 사용 > 손쉬운 사용 단축키로 가요. (맨 아래에 있어요)",
                "'색상 필터'에 체크해요.",
                "이제 측면 버튼(홈 버튼이 있는 기종은 홈 버튼)을 세 번 누르면 흑백이 켜지고 꺼져요.",
            ]
        ),
        SimplifyTip(
            id: "grayscale-automation",
            icon: "wand.and.stars",
            title: "특정 앱을 열면 자동으로 흑백 되게 하기",
            why: "평소엔 컬러로 쓰다가, 인스타그램·유튜브를 열 때만 자동으로 흑백이 되게 할 수 있어요.",
            steps: [
                "'단축어' 앱 > 아래 '자동화' 탭 > 오른쪽 위 + 를 눌러요.",
                "'앱'을 고르고, 인스타그램 등 자극적인 앱을 선택해요.",
                "'열려 있음'을 체크하고 '즉시 실행'을 고른 뒤 '다음'.",
                "'새로운 빈 자동화' > 동작 추가에서 '색상 필터 설정'을 검색해 추가하고 '켬'으로 둬요.",
                "같은 방법으로 '닫혀 있음' 자동화를 만들고 '끔'으로 두면, 앱을 닫을 때 컬러로 돌아와요.",
            ]
        ),
        SimplifyTip(
            id: "home-screen",
            icon: "square.grid.2x2",
            title: "홈 화면 첫 페이지 비우기",
            why: "폰을 켜자마자 보이는 앱이 적을수록 무심코 누르는 일이 줄어요.",
            steps: [
                "홈 화면 빈 곳을 길게 눌러 편집 모드로 들어가요.",
                "자주 '무심코' 여는 앱의 − 를 눌러 '홈 화면에서 제거'를 골라요. (앱은 지워지지 않고 앱 보관함으로 가요)",
                "전화·메시지·지도·캘린더처럼 꼭 필요한 앱만 첫 페이지에 남겨요.",
                "설정 > 홈 화면 및 앱 보관함에서 '새로 다운로드한 앱'을 '앱 보관함에만'으로 바꿔요.",
            ]
        ),
        SimplifyTip(
            id: "badges",
            icon: "app.badge",
            title: "빨간 알림 배지 끄기",
            why: "숫자 배지는 '확인해야 할 것 같은' 불안감을 만들어요.",
            steps: [
                "설정 > 알림으로 가요.",
                "SNS·쇼핑·게임 앱을 하나씩 눌러 '배지'를 꺼요.",
                "꼭 필요 없는 앱은 '알림 허용' 자체를 꺼도 좋아요.",
            ]
        ),
        SimplifyTip(
            id: "summary",
            icon: "tray.full",
            title: "알림을 하루 몇 번 모아서 받기",
            why: "알림이 올 때마다 폰을 집는 대신, 정해진 시간에 한 번에 확인해요.",
            steps: [
                "설정 > 알림 > '예약 요약'을 켜요.",
                "받을 시간을 정해요. (예: 오후 12시, 저녁 7시)",
                "요약으로 받을 앱을 골라요. 메신저처럼 바로 봐야 하는 앱은 빼세요.",
            ]
        ),
        SimplifyTip(
            id: "focus-mode",
            icon: "moon.circle",
            title: "'디톡스' 집중 모드 만들기",
            why: "집중 모드를 켜면 홈 화면을 필수 앱만 있는 페이지로 바꾸고 알림도 거를 수 있어요.",
            steps: [
                "설정 > 집중 모드 > 오른쪽 위 + > '사용자 설정'을 눌러요.",
                "이름을 '디톡스'로 하고 아이콘과 색을 골라요.",
                "알림을 허용할 사람·앱을 꼭 필요한 것만 남겨요.",
                "'화면 사용자화' > 홈 화면에서 필수 앱만 있는 페이지 하나를 선택해요.",
                "제어 센터에서 집중 모드를 눌러 언제든 켤 수 있어요.",
            ]
        ),
        SimplifyTip(
            id: "wallpaper",
            icon: "rectangle.portrait",
            title: "배경화면을 단색으로",
            why: "화려한 배경 대신 단색 배경은 눈과 뇌를 덜 피곤하게 해요.",
            steps: [
                "설정 > 배경화면 > '새로운 배경화면 추가'를 눌러요.",
                "'색상'을 골라 검정이나 회색 같은 차분한 색을 선택해요.",
                "잠금 화면 위젯도 꼭 필요한 것만 남겨요.",
            ]
        ),
    ]
}
