import ManagedSettings
import ManagedSettingsUI
import UIKit

/// 차단된 앱/사이트를 열었을 때 보이는 "차단 화면"의 모양을 정하는 확장 프로그램.
class ShieldConfigurationExtension: ShieldConfigurationDataSource {

    override func configuration(shielding application: Application) -> ShieldConfiguration {
        makeConfiguration(name: application.localizedDisplayName)
    }

    override func configuration(shielding application: Application,
                                in category: ActivityCategory) -> ShieldConfiguration {
        makeConfiguration(name: application.localizedDisplayName)
    }

    override func configuration(shielding webDomain: WebDomain) -> ShieldConfiguration {
        makeConfiguration(name: webDomain.domain)
    }

    override func configuration(shielding webDomain: WebDomain,
                                in category: ActivityCategory) -> ShieldConfiguration {
        makeConfiguration(name: webDomain.domain)
    }

    private func makeConfiguration(name: String?) -> ShieldConfiguration {
        let target = name ?? "이 앱"
        var subtitle = "\(target) 차단 중\n\n지금 꼭 필요한 일인가요?\n숨을 한 번 깊게 쉬고, 하던 일로 돌아가요."

        if let focus = SharedStore.activeFocus, focus.endDate > Date() {
            let minutesLeft = Int((focus.endDate.timeIntervalSinceNow / 60).rounded(.up))
            subtitle = "\(target) 차단 중\n\n집중 세션이 \(minutesLeft)분 남았어요.\n조금만 더 버텨봐요."
        }

        return ShieldConfiguration(
            backgroundBlurStyle: .systemUltraThinMaterialDark,
            backgroundColor: .black,
            icon: UIImage(systemName: "leaf.fill"),
            title: ShieldConfiguration.Label(text: "잠깐, 쉬어가요", color: .white),
            subtitle: ShieldConfiguration.Label(text: subtitle, color: .lightGray),
            primaryButtonLabel: ShieldConfiguration.Label(text: "참았다! 닫기", color: .black),
            primaryButtonBackgroundColor: .white,
            secondaryButtonLabel: nil
        )
    }
}
