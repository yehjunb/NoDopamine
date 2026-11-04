import DeviceActivity
import ManagedSettings

/// 예약된 시간이 "시작"되거나 "끝날" 때 iOS 가 깨워서 불러주는 확장 프로그램.
/// 앱이 꺼져 있어도 동작하기 때문에, 밤 10시에 앱을 안 열어도 차단이 걸려요.
class DeviceActivityMonitorExtension: DeviceActivityMonitor {

    override func intervalDidStart(for activity: DeviceActivityName) {
        super.intervalDidStart(for: activity)

        if activity == .schedule {
            ShieldController.apply(to: .schedule)
        } else if activity == .focus {
            ShieldController.apply(to: .focus)
        }
    }

    override func intervalDidEnd(for activity: DeviceActivityName) {
        super.intervalDidEnd(for: activity)

        if activity == .schedule {
            ShieldController.clear(.schedule)
        } else if activity == .focus {
            // 집중 세션이 끝나면 차단을 풀어요.
            // "완료" 기록은 다음에 앱을 열 때 앱이 남겨요.
            ShieldController.clear(.focus)
        }
    }
}
