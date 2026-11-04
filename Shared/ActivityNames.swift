import DeviceActivity
import ManagedSettings

// 이름표 모음. 앱과 확장 프로그램이 같은 이름을 써야 서로 알아들어요.

extension DeviceActivityName {
    /// 매일 반복되는 예약 차단 (예: 밤 10시 ~ 아침 7시)
    static let schedule = Self("nodopamine.schedule")
    /// 디톡스 탭의 집중 세션 (한 번만)
    static let focus = Self("nodopamine.focus")
}

extension ManagedSettingsStore.Name {
    // 차단 종류마다 저장소를 따로 둬서, 하나를 끄더라도 다른 차단은 그대로 유지돼요.
    static let manual = Self("nodopamine.manual")
    static let schedule = Self("nodopamine.schedule")
    static let focus = Self("nodopamine.focus")
}
