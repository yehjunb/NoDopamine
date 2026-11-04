import Foundation

/// 앱과 확장 프로그램(차단 화면, 예약 감시)이 데이터를 함께 쓰기 위한 "공유 저장소" 설정.
///
/// iOS 에서는 앱과 확장 프로그램이 서로 다른 공간에서 실행되기 때문에,
/// 같은 App Group 에 속해야만 같은 UserDefaults 를 읽고 쓸 수 있어요.
/// App Group ID 는 Config.xcconfig 의 APP_GROUP_ID 에서 정하고, 각 Info.plist 를 통해 들어옵니다.
enum AppGroup {
    static let id: String =
        Bundle.main.object(forInfoDictionaryKey: "AppGroupID") as? String
        ?? "group.com.example.nodopamine"

    static var defaults: UserDefaults {
        UserDefaults(suiteName: id) ?? .standard
    }
}
