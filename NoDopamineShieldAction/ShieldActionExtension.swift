import ManagedSettings

/// 차단 화면의 버튼을 눌렀을 때 무슨 일을 할지 정하는 확장 프로그램.
/// "참았다! 닫기"를 누르면 '유혹을 이긴 횟수'를 1 올리고 앱을 닫아요.
class ShieldActionExtension: ShieldActionDelegate {

    override func handle(action: ShieldAction,
                         for application: ApplicationToken,
                         completionHandler: @escaping (ShieldActionResponse) -> Void) {
        completionHandler(respond(to: action))
    }

    override func handle(action: ShieldAction,
                         for webDomain: WebDomainToken,
                         completionHandler: @escaping (ShieldActionResponse) -> Void) {
        completionHandler(respond(to: action))
    }

    override func handle(action: ShieldAction,
                         for category: ActivityCategoryToken,
                         completionHandler: @escaping (ShieldActionResponse) -> Void) {
        completionHandler(respond(to: action))
    }

    private func respond(to action: ShieldAction) -> ShieldActionResponse {
        switch action {
        case .primaryButtonPressed:
            SharedStore.recordResist()
            return .close
        case .secondaryButtonPressed:
            return .close
        @unknown default:
            return .close
        }
    }
}
