import Foundation
import SwiftUI
import DeviceActivity

/// "디톡스" 탭의 집중 세션과 기록을 관리해요.
final class FocusModel: ObservableObject {

    /// 집중 세션으로 고를 수 있는 시간(분).
    /// iOS 예약 기능은 15분보다 짧은 시간은 받지 않아요.
    static let durationOptions = [15, 30, 45, 60, 90, 120]

    @Published private(set) var active: ActiveFocus?
    @Published private(set) var records: [FocusRecord]
    @Published var errorMessage: String?

    /// 하루 집중 목표(분)
    @Published var dailyGoalMinutes: Int {
        didSet { UserDefaults.standard.set(dailyGoalMinutes, forKey: "dailyGoalMinutes") }
    }

    init() {
        active = SharedStore.activeFocus
        records = SharedStore.focusRecords
        let saved = UserDefaults.standard.integer(forKey: "dailyGoalMinutes")
        dailyGoalMinutes = saved > 0 ? saved : 60
        checkCompletion()
    }

    // MARK: - 세션 시작 / 끝

    func start(minutes: Int) {
        guard active == nil else { return }

        let now = Date()
        let end = now.addingTimeInterval(TimeInterval(minutes * 60))
        let parts: Set<Calendar.Component> = [.year, .month, .day, .hour, .minute, .second]
        let calendar = Calendar.current

        // 앱이 꺼져 있어도 끝나는 시간에 차단이 풀리도록 iOS 에 예약해 둬요.
        let schedule = DeviceActivitySchedule(
            intervalStart: calendar.dateComponents(parts, from: now),
            intervalEnd: calendar.dateComponents(parts, from: end),
            repeats: false
        )
        do {
            try DeviceActivityCenter().startMonitoring(.focus, during: schedule)
        } catch {
            errorMessage = "집중 세션을 시작하지 못했어요. (\(error.localizedDescription))"
            return
        }

        let focus = ActiveFocus(id: UUID(), startDate: now, endDate: end)
        SharedStore.activeFocus = focus
        active = focus
        ShieldController.apply(to: .focus)
        errorMessage = nil
    }

    /// 시간이 다 됐는지 확인하고, 다 됐으면 "완료"로 기록해요.
    func checkCompletion() {
        if let active, Date() >= active.endDate {
            finish(completed: true)
        }
    }

    func giveUp() {
        finish(completed: false)
    }

    private func finish(completed: Bool) {
        guard let focus = active else { return }

        DeviceActivityCenter().stopMonitoring([.focus])
        ShieldController.clear(.focus)

        let record = FocusRecord(
            id: focus.id,
            startDate: focus.startDate,
            endDate: completed ? focus.endDate : Date(),
            plannedMinutes: focus.plannedMinutes,
            completed: completed
        )
        records.insert(record, at: 0)
        SharedStore.focusRecords = records
        SharedStore.activeFocus = nil
        active = nil
    }

    // MARK: - 통계

    /// 그날 완료한 집중 시간(분). 포기한 세션은 시간에 넣지 않아요.
    func focusMinutes(on day: Date) -> Int {
        let calendar = Calendar.current
        return records
            .filter { $0.completed && calendar.isDate($0.startDate, inSameDayAs: day) }
            .reduce(0) { $0 + $1.plannedMinutes }
    }

    var todayMinutes: Int { focusMinutes(on: Date()) }

    var todayResists: Int { SharedStore.resistCount(on: Date()) }

    var completedCount: Int { records.filter(\.completed).count }

    /// 연속 달성일: 하루 목표를 채운 날이 며칠 연속인지.
    /// 오늘 아직 못 채웠어도 어제까지 이어졌다면 기록은 유지돼요.
    var streak: Int {
        guard dailyGoalMinutes > 0 else { return 0 }
        let calendar = Calendar.current
        var day = calendar.startOfDay(for: Date())
        if focusMinutes(on: day) < dailyGoalMinutes {
            day = calendar.date(byAdding: .day, value: -1, to: day)!
        }
        var count = 0
        while focusMinutes(on: day) >= dailyGoalMinutes {
            count += 1
            day = calendar.date(byAdding: .day, value: -1, to: day)!
        }
        return count
    }

    struct DayStat: Identifiable {
        let day: Date
        let minutes: Int
        var id: Date { day }
    }

    /// 최근 7일(오래된 날 → 오늘) 집중 시간
    var lastSevenDays: [DayStat] {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        return (0..<7).reversed().map { offset in
            let day = calendar.date(byAdding: .day, value: -offset, to: today)!
            return DayStat(day: day, minutes: focusMinutes(on: day))
        }
    }
}
