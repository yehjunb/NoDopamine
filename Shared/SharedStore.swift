import Foundation
import FamilyControls

/// 매일 반복되는 예약 차단 시간.
struct BlockSchedule: Codable, Equatable {
    var isEnabled = false
    var startHour = 22
    var startMinute = 0
    var endHour = 7
    var endMinute = 0

    /// 지금(또는 주어진 시각)이 예약 차단 시간 안에 있는지.
    /// 시작이 끝보다 늦으면(밤 10시 ~ 아침 7시처럼) 자정을 넘어가는 것으로 봐요.
    func contains(_ date: Date, calendar: Calendar = .current) -> Bool {
        guard isEnabled else { return false }
        let c = calendar.dateComponents([.hour, .minute], from: date)
        let now = (c.hour ?? 0) * 60 + (c.minute ?? 0)
        let start = startHour * 60 + startMinute
        let end = endHour * 60 + endMinute
        if start == end { return false }
        return start < end ? (now >= start && now < end) : (now >= start || now < end)
    }
}

/// 지금 진행 중인 집중 세션.
struct ActiveFocus: Codable, Equatable {
    var id: UUID
    var startDate: Date
    var endDate: Date

    var plannedMinutes: Int { Int((endDate.timeIntervalSince(startDate) / 60).rounded()) }
}

/// 끝난 집중 세션 기록.
struct FocusRecord: Codable, Identifiable, Equatable {
    var id: UUID
    var startDate: Date
    var endDate: Date
    var plannedMinutes: Int
    var completed: Bool
}

/// 앱과 확장 프로그램이 함께 쓰는 데이터 창고.
/// 모든 값은 App Group 의 UserDefaults 에 JSON 으로 저장돼요.
enum SharedStore {
    private enum Key {
        static let selection = "selection"
        static let domains = "blockedDomains"
        static let manualBlock = "manualBlockOn"
        static let schedule = "schedule"
        static let activeFocus = "activeFocus"
        static let records = "focusRecords"
        static let resists = "resistCounts"
    }

    private static var defaults: UserDefaults { AppGroup.defaults }

    /// 차단할 앱·카테고리·웹사이트 (FamilyActivityPicker 에서 고른 것)
    static var selection: FamilyActivitySelection {
        get { load(Key.selection) ?? FamilyActivitySelection() }
        set { save(newValue, Key.selection) }
    }

    /// 직접 입력한 웹사이트 주소 (예: "youtube.com")
    static var blockedDomains: [String] {
        get { load(Key.domains) ?? [] }
        set { save(newValue, Key.domains) }
    }

    static var isManualBlockOn: Bool {
        get { defaults.bool(forKey: Key.manualBlock) }
        set { defaults.set(newValue, forKey: Key.manualBlock) }
    }

    static var schedule: BlockSchedule {
        get { load(Key.schedule) ?? BlockSchedule() }
        set { save(newValue, Key.schedule) }
    }

    static var activeFocus: ActiveFocus? {
        get { load(Key.activeFocus) }
        set { save(newValue, Key.activeFocus) }
    }

    static var focusRecords: [FocusRecord] {
        get { load(Key.records) ?? [] }
        set { save(newValue, Key.records) }
    }

    // MARK: - 유혹을 이긴 횟수 (차단 화면에서 "닫기"를 누른 횟수)

    /// 날짜("2026-11-04") → 횟수
    static var resistCounts: [String: Int] {
        get { load(Key.resists) ?? [:] }
        set { save(newValue, Key.resists) }
    }

    static func recordResist(on date: Date = Date()) {
        var counts = resistCounts
        counts[dayKey(date), default: 0] += 1
        resistCounts = counts
    }

    static func resistCount(on date: Date) -> Int {
        resistCounts[dayKey(date)] ?? 0
    }

    static func dayKey(_ date: Date) -> String {
        let c = Calendar.current.dateComponents([.year, .month, .day], from: date)
        return String(format: "%04d-%02d-%02d", c.year ?? 0, c.month ?? 0, c.day ?? 0)
    }

    // MARK: - 저장/불러오기 도우미

    private static func load<T: Decodable>(_ key: String) -> T? {
        guard let data = defaults.data(forKey: key) else { return nil }
        return try? JSONDecoder().decode(T.self, from: data)
    }

    private static func save<T: Encodable>(_ value: T?, _ key: String) {
        guard let value, let data = try? JSONEncoder().encode(value) else {
            defaults.removeObject(forKey: key)
            return
        }
        defaults.set(data, forKey: key)
    }
}
