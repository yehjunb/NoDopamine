import Foundation
import FamilyControls
import ManagedSettings

/// 실제로 "차단"을 걸고 푸는 곳.
///
/// ManagedSettingsStore 에 차단할 앱/사이트를 넣으면 iOS 가 알아서 그 앱 위에
/// 차단 화면(Shield)을 띄워줘요. 저장소를 비우면 차단이 풀려요.
enum ShieldController {

    /// 직접 입력한 사이트 최대 개수 (iOS 웹 필터 제한을 넘지 않도록 넉넉히 잡은 값)
    static let maxDomains = 25

    static func apply(to name: ManagedSettingsStore.Name) {
        let store = ManagedSettingsStore(named: name)
        let selection = SharedStore.selection

        // 1) 선택한 앱
        store.shield.applications =
            selection.applicationTokens.isEmpty ? nil : selection.applicationTokens

        // 2) 선택한 카테고리 (예: 소셜 네트워킹 전체) — 앱과 웹사이트 모두
        if selection.categoryTokens.isEmpty {
            store.shield.applicationCategories = nil
            store.shield.webDomainCategories = nil
        } else {
            store.shield.applicationCategories = .specific(selection.categoryTokens)
            store.shield.webDomainCategories = .specific(selection.categoryTokens)
        }

        // 3) 선택 화면에서 고른 웹사이트
        store.shield.webDomains =
            selection.webDomainTokens.isEmpty ? nil : selection.webDomainTokens

        // 4) 직접 입력한 웹사이트 → Safari 등 브라우저에서 열리지 않게 필터링
        let domains = SharedStore.blockedDomains.prefix(maxDomains).flatMap { domain in
            domain.hasPrefix("www.") ? [domain] : [domain, "www." + domain]
        }
        if domains.isEmpty {
            store.webContent.blockedByFilter = nil
        } else {
            let webDomains = Set(domains.map { WebDomain(domain: $0) })
            store.webContent.blockedByFilter = WebContentSettings.FilterPolicy.specific(webDomains)
        }
    }

    static func clear(_ name: ManagedSettingsStore.Name) {
        ManagedSettingsStore(named: name).clearAllSettings()
    }
}
