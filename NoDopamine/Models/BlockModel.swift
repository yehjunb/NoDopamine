import Foundation
import SwiftUI
import DeviceActivity
import FamilyControls
import ManagedSettings

/// "차단" 탭의 상태를 관리해요.
/// 값이 바뀌면 공유 저장소(SharedStore)에 저장하고, 켜져 있는 차단에 바로 반영해요.
final class BlockModel: ObservableObject {

    /// 차단할 앱·카테고리·웹사이트
    @Published var selection: FamilyActivitySelection {
        didSet {
            SharedStore.selection = selection
            refreshActiveShields()
        }
    }

    /// 직접 입력한 웹사이트 목록
    @Published private(set) var domains: [String]

    /// "지금 바로 차단" 스위치
    @Published private(set) var isManualBlockOn: Bool

    /// 예약 차단 설정
    @Published var schedule: BlockSchedule {
        didSet {
            guard schedule != oldValue else { return }
            SharedStore.schedule = schedule
            startScheduleMonitoring()
        }
    }

    /// 화면에 보여줄 오류 메시지
    @Published var errorMessage: String?

    init() {
        selection = SharedStore.selection
        domains = SharedStore.blockedDomains
        isManualBlockOn = SharedStore.isManualBlockOn
        schedule = SharedStore.schedule
    }

    // MARK: - 상태 확인

    var isScheduleActiveNow: Bool { schedule.contains(Date()) }

    var isSelectionEmpty: Bool {
        selection.applicationTokens.isEmpty
            && selection.categoryTokens.isEmpty
            && selection.webDomainTokens.isEmpty
            && domains.isEmpty
    }

    /// 차단이 걸려 있는 동안에는 목록을 바꾸지 못하게 해서 "몰래 빼기"를 막아요.
    func isLocked(focusActive: Bool) -> Bool {
        isManualBlockOn || isScheduleActiveNow || focusActive
    }

    // MARK: - 지금 바로 차단

    func setManualBlock(_ on: Bool) {
        isManualBlockOn = on
        SharedStore.isManualBlockOn = on
        if on {
            ShieldController.apply(to: .manual)
        } else {
            ShieldController.clear(.manual)
        }
    }

    // MARK: - 웹사이트 직접 입력

    @discardableResult
    func addDomain(_ raw: String) -> Bool {
        guard let domain = Self.normalize(raw) else {
            errorMessage = "올바른 주소가 아니에요. 예: youtube.com"
            return false
        }
        guard !domains.contains(domain) else { return true }
        guard domains.count < ShieldController.maxDomains else {
            errorMessage = "사이트는 최대 \(ShieldController.maxDomains)개까지 추가할 수 있어요."
            return false
        }
        domains.append(domain)
        SharedStore.blockedDomains = domains
        refreshActiveShields()
        return true
    }

    func removeDomains(at offsets: IndexSet) {
        domains.remove(atOffsets: offsets)
        SharedStore.blockedDomains = domains
        refreshActiveShields()
    }

    /// "https://www.YouTube.com/watch?v=..." → "youtube.com"
    static func normalize(_ raw: String) -> String? {
        var s = raw.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if let range = s.range(of: "://") {
            s = String(s[range.upperBound...])
        }
        if let cut = s.firstIndex(where: { "/?#:".contains($0) }) {
            s = String(s[..<cut])
        }
        if s.hasPrefix("www.") {
            s.removeFirst(4)
        }
        let allowed = CharacterSet(charactersIn: "abcdefghijklmnopqrstuvwxyz0123456789.-")
        guard s.contains("."),
              !s.hasPrefix("."), !s.hasSuffix("."),
              s.unicodeScalars.allSatisfy({ allowed.contains($0) })
        else { return nil }
        return s
    }

    // MARK: - 예약 차단

    /// iOS 에 "매일 이 시간에 나를 깨워줘"라고 등록해요.
    /// 시간이 되면 NoDopamineMonitor 확장 프로그램이 차단을 걸고 풀어요.
    func startScheduleMonitoring() {
        let center = DeviceActivityCenter()
        center.stopMonitoring([.schedule])
        ShieldController.clear(.schedule)

        guard schedule.isEnabled else { return }

        let activitySchedule = DeviceActivitySchedule(
            intervalStart: DateComponents(hour: schedule.startHour, minute: schedule.startMinute),
            intervalEnd: DateComponents(hour: schedule.endHour, minute: schedule.endMinute),
            repeats: true
        )
        do {
            try center.startMonitoring(.schedule, during: activitySchedule)
            errorMessage = nil
        } catch {
            errorMessage = "예약을 등록하지 못했어요. 시작과 끝 시간이 15분 이상 차이 나야 해요."
            // 등록이 안 됐는데 스위치만 켜져 있으면 헷갈리니까 꺼둬요.
            schedule.isEnabled = false
            return
        }

        // 이미 예약 시간 안이라면 바로 차단
        if isScheduleActiveNow {
            ShieldController.apply(to: .schedule)
        }
    }

    // MARK: - 공통

    /// 앱을 다시 열었을 때나 목록이 바뀌었을 때, 켜져 있는 차단에 최신 목록을 반영해요.
    func refreshActiveShields() {
        if isManualBlockOn { ShieldController.apply(to: .manual) }
        if isScheduleActiveNow { ShieldController.apply(to: .schedule) }
        if let focus = SharedStore.activeFocus, focus.endDate > Date() {
            ShieldController.apply(to: .focus)
        }
        objectWillChange.send()
    }
}
