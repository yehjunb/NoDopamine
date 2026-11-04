import SwiftUI
import FamilyControls
import ManagedSettings

/// "차단" 탭: 무엇을 막을지, 언제 막을지 정해요.
struct BlockView: View {
    @EnvironmentObject private var block: BlockModel
    @EnvironmentObject private var focus: FocusModel

    @State private var showPicker = false
    @State private var unlockRequest: UnlockRequest?

    private var isLocked: Bool { block.isLocked(focusActive: focus.active != nil) }

    var body: some View {
        NavigationStack {
            Form {
                statusSection
                selectionSection
                manualSection
                scheduleSection
            }
            .navigationTitle("차단")
            .familyActivityPicker(isPresented: $showPicker, selection: $block.selection)
            .sheet(item: $unlockRequest) { request in
                CooldownSheet(title: request.title,
                              message: request.message,
                              confirmTitle: request.confirmTitle,
                              onConfirm: request.action)
            }
            .alert("알림", isPresented: errorBinding) {
                Button("확인", role: .cancel) {}
            } message: {
                Text(block.errorMessage ?? "")
            }
        }
    }

    // MARK: - 현재 상태

    /// 지금 차단이 걸려 있는 이유들
    private var activeReasons: [String] {
        var reasons: [String] = []
        if focus.active != nil { reasons.append("집중 세션") }
        if block.isManualBlockOn { reasons.append("지금 바로 차단") }
        if block.isScheduleActiveNow { reasons.append("예약 차단") }
        return reasons
    }

    private var statusSection: some View {
        let reasons = activeReasons
        return Section {
            if reasons.isEmpty {
                Label("지금은 아무것도 차단하지 않고 있어요", systemImage: "lock.open")
                    .foregroundStyle(.secondary)
            } else {
                Label("차단 중 · \(reasons.joined(separator: ", "))", systemImage: "lock.fill")
                    .foregroundStyle(Color.accentColor)
                    .font(.headline)
            }
        }
    }

    // MARK: - 차단 목록

    private var selectionSection: some View {
        Section {
            Button {
                showPicker = true
            } label: {
                Label("차단할 앱·카테고리·사이트 고르기", systemImage: "plus.circle.fill")
            }
            .disabled(isLocked)

            ForEach(Array(block.selection.applicationTokens), id: \.self) { token in
                Label(token)
            }
            ForEach(Array(block.selection.categoryTokens), id: \.self) { token in
                Label(token)
            }
            ForEach(Array(block.selection.webDomainTokens), id: \.self) { token in
                Label(token)
            }

            NavigationLink {
                WebsiteListView(isLocked: isLocked)
            } label: {
                HStack {
                    Label("사이트 주소 직접 입력", systemImage: "globe")
                    Spacer()
                    Text("\(block.domains.count)개")
                        .foregroundStyle(.secondary)
                }
            }
        } header: {
            Text("차단 목록")
        } footer: {
            if isLocked {
                Text("차단이 켜져 있는 동안에는 목록을 바꿀 수 없어요. 몰래 빼는 걸 막기 위해서예요.")
            } else {
                Text("인스타그램, 유튜브, 틱톡처럼 무심코 열게 되는 앱을 골라보세요. '소셜 네트워킹' 같은 카테고리를 통째로 고를 수도 있어요.")
            }
        }
    }

    // MARK: - 지금 바로 차단

    private var manualSection: some View {
        Section {
            Toggle(isOn: Binding(
                get: { block.isManualBlockOn },
                set: { turnOn in
                    if turnOn {
                        block.setManualBlock(true)
                    } else {
                        unlockRequest = UnlockRequest(
                            title: "차단을 풀까요?",
                            message: "지금 열고 싶은 그 앱, 10초 뒤에도 정말 필요할까요?",
                            confirmTitle: "차단 풀기"
                        ) {
                            block.setManualBlock(false)
                        }
                    }
                }
            )) {
                Label("지금 바로 차단", systemImage: "bolt.fill")
            }
        } footer: {
            Text("켜면 직접 끌 때까지 계속 차단돼요. 끌 때는 10초를 기다려야 해요.")
        }
    }

    // MARK: - 예약 차단

    private var scheduleSection: some View {
        Section {
            Toggle(isOn: Binding(
                get: { block.schedule.isEnabled },
                set: { turnOn in
                    if !turnOn && block.isScheduleActiveNow {
                        unlockRequest = UnlockRequest(
                            title: "예약 차단을 끌까요?",
                            message: "지금은 스스로 정한 차단 시간이에요.",
                            confirmTitle: "예약 끄기"
                        ) {
                            block.schedule.isEnabled = false
                        }
                    } else {
                        block.schedule.isEnabled = turnOn
                    }
                }
            )) {
                Label("매일 예약 차단", systemImage: "moon.fill")
            }

            if block.schedule.isEnabled {
                DatePicker("시작", selection: timeBinding(isStart: true), displayedComponents: .hourAndMinute)
                    .disabled(block.isScheduleActiveNow)
                DatePicker("끝", selection: timeBinding(isStart: false), displayedComponents: .hourAndMinute)
                    .disabled(block.isScheduleActiveNow)
            }
        } header: {
            Text("예약 차단")
        } footer: {
            Text("예: 밤 10시 ~ 아침 7시. 끝 시간이 시작보다 빠르면 다음 날까지 이어져요. 앱을 열지 않아도 매일 자동으로 차단돼요.")
        }
    }

    // MARK: - 도우미

    /// 시:분 값을 DatePicker 가 다루는 Date 로 바꿔주는 연결 고리
    private func timeBinding(isStart: Bool) -> Binding<Date> {
        Binding(
            get: {
                let s = block.schedule
                let hour = isStart ? s.startHour : s.endHour
                let minute = isStart ? s.startMinute : s.endMinute
                return Calendar.current.date(bySettingHour: hour, minute: minute, second: 0, of: Date()) ?? Date()
            },
            set: { date in
                let c = Calendar.current.dateComponents([.hour, .minute], from: date)
                var s = block.schedule
                if isStart {
                    s.startHour = c.hour ?? 0
                    s.startMinute = c.minute ?? 0
                } else {
                    s.endHour = c.hour ?? 0
                    s.endMinute = c.minute ?? 0
                }
                block.schedule = s
            }
        )
    }

    private var errorBinding: Binding<Bool> {
        Binding(
            get: { block.errorMessage != nil },
            set: { if !$0 { block.errorMessage = nil } }
        )
    }
}

/// "차단 풀기" 같은 행동을 10초 대기 화면 뒤로 미루기 위한 요청서
struct UnlockRequest: Identifiable {
    let id = UUID()
    let title: String
    let message: String
    let confirmTitle: String
    let action: () -> Void
}
