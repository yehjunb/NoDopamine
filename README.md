# NoDopamine 🍃

폰의 "도파민"을 줄여주는 iOS 앱이에요.
자극적인 앱과 사이트를 잠그고, 집중한 시간을 기록하고, 폰 화면 자체를 덜 자극적으로 바꾸도록 도와줘요.

## 기능

| 탭 | 할 수 있는 것 |
|---|---|
| **디톡스** | 15분~2시간 집중 세션 (그동안 선택한 앱·사이트가 잠김) · 하루 목표 · 연속 달성일 🔥 · 최근 7일 그래프 · 오늘 유혹을 참은 횟수 |
| **차단** | 차단할 앱/카테고리/사이트 고르기 · 사이트 주소 직접 입력 · **지금 바로 차단** · **매일 예약 차단**(예: 밤 10시~아침 7시) |
| **화면 단순화** | 흑백 화면, 측면 버튼으로 흑백 켜고 끄기, 특정 앱 열면 자동 흑백, 홈 화면 비우기, 배지 끄기 등 단계별 체크리스트 |

충동을 줄이는 장치도 넣었어요.
- 차단을 풀거나 세션을 포기하려면 **10초를 기다려야** 해요. (충동은 보통 몇 초면 지나가요)
- 차단이 켜져 있는 동안에는 차단 목록에서 **앱을 뺄 수 없어요.**
- 차단된 앱을 열면 "잠깐, 쉬어가요" 화면이 뜨고, **"참았다! 닫기"** 를 누르면 참은 횟수가 올라가요.

---

## 준비물

- **Mac** + **Xcode 16 이상** (Mac App Store에서 무료)
- **iPhone** (iOS 17 이상) + 케이블
  - ⚠️ 시뮬레이터에서는 스크린 타임 차단이 동작하지 않아서 **실제 아이폰이 꼭 필요해요.**
- **Apple Developer Program 유료 계정** (연 $99)
  - 이 앱은 Apple의 스크린 타임 기능(Family Controls)을 써요. 무료 계정(Personal Team)으로는 이 기능이 켜지지 않는 경우가 많아요.

## 처음 실행하기 (단계별)

### 1. 코드 받기
GitHub 페이지에서 초록색 **Code** 버튼 → **Download ZIP** 으로 받아서 압축을 풀어요.
(git 을 쓸 줄 알면 `git clone` 해도 돼요.)

### 2. 내 정보 넣기
폴더 안의 `Config.xcconfig` 파일을 열어 두 줄을 바꿔요.

```
DEVELOPMENT_TEAM = AB12CD34EF        ← 내 팀 ID (developer.apple.com/account → 멤버십 세부 사항)
BUNDLE_ID_PREFIX = com.yehjunb       ← 나만의 이름. 다른 사람과 겹치면 안 돼요
```

팀 ID를 모르겠으면 비워둬도 돼요. 3번에서 Xcode 화면으로 고를 수 있어요.

### 3. Xcode로 열고 서명 설정
1. `NoDopamine.xcodeproj` 를 더블클릭해서 열어요.
2. Xcode 상단 메뉴 **Xcode → Settings → Accounts** 에서 Apple ID로 로그인해요.
3. 왼쪽 맨 위 파란 **NoDopamine** 아이콘 클릭 → 가운데 **TARGETS** 목록이 보여요.
4. TARGETS 4개(NoDopamine, NoDopamineMonitor, NoDopamineShield, NoDopamineShieldAction)를 **하나씩** 눌러서
   **Signing & Capabilities** 탭에서
   - **Automatically manage signing** 체크
   - **Team** 에서 내 팀 선택 (2번에서 팀 ID를 넣었다면 이미 선택돼 있어요)
   - 아래쪽에 **Family Controls** 와 **App Groups** 가 보이면 정상이에요.

### 4. 아이폰 준비
1. 아이폰을 Mac에 연결하고, 아이폰에 "이 컴퓨터를 신뢰하겠습니까?"가 뜨면 **신뢰**.
2. 아이폰 **설정 → 개인정보 보호 및 보안 → 개발자 모드** 켜기 → 재시동.
   (Xcode에 한 번 연결해야 이 메뉴가 나타나요.)

### 5. 실행!
1. Xcode 위쪽 가운데에서 실행할 기기로 **내 아이폰**을 골라요.
2. ▶︎ 버튼 (또는 `⌘ + R`).
3. 아이폰에 앱이 설치되면 **"스크린 타임 권한 허용하기"** → Face ID/암호로 허용.
4. **차단** 탭에서 막고 싶은 앱을 고르고 → **디톡스** 탭에서 집중 세션을 시작해 보세요!

## 문제가 생기면

| 증상 | 해결 |
|---|---|
| `...doesn't include the com.apple.developer.family-controls entitlement` | 유료 개발자 계정인지 확인하세요. 3번의 Team 이 유료 팀으로 선택돼 있어야 해요. |
| `Failed to register bundle identifier` / 이미 사용 중인 ID | `Config.xcconfig` 의 `BUNDLE_ID_PREFIX` 를 다른 값으로 바꾸세요. (예: `com.yehjunb2026`) |
| App Group 관련 오류 | 4개 타깃 모두 같은 Team 인지 확인하고, Xcode를 껐다 켜보세요. |
| "신뢰하지 않는 개발자" | 아이폰 **설정 → 일반 → VPN 및 기기 관리** 에서 내 개발자 앱을 신뢰하세요. |
| 차단이 안 걸려요 | 차단 탭에서 앱을 골랐는지, 스크린 타임 권한을 허용했는지 확인하세요. |
| 예약 차단 등록 실패 | 시작과 끝 시간이 15분 이상 차이 나야 해요. |

---

## 어떻게 동작하나요?

iOS 앱은 다른 앱을 직접 막을 수 없어요. 대신 Apple이 제공하는 **스크린 타임 API** 에
"이 앱들을 막아줘"라고 부탁하면, iOS가 그 앱 위에 차단 화면을 씌워줘요.

```
 ┌──────────── NoDopamine 앱 ────────────┐
 │  디톡스 탭 · 차단 탭 · 화면 단순화 탭     │
 └──────┬──────────────────────┬─────────┘
        │ 차단 걸기/풀기         │ 설정 저장
        ▼                      ▼
  ManagedSettings        App Group (공유 저장소)
  (iOS가 앱 위에             ▲        ▲        ▲
   차단 화면 표시)            │        │        │
        ▲              ┌─────┴──┐ ┌───┴────┐ ┌─┴──────────┐
        └──────────────│Monitor │ │Shield  │ │ShieldAction│
         예약 시간에     │예약 감시│ │차단 화면│ │차단 버튼     │
         차단 걸기/풀기  └────────┘ └────────┘ └────────────┘
```

- **NoDopamineMonitor**: 앱이 꺼져 있어도, 예약 시간이 되면 iOS가 깨워서 차단을 걸고 풀어요.
- **NoDopamineShield**: 차단된 앱을 열 때 보이는 "잠깐, 쉬어가요" 화면을 그려요.
- **NoDopamineShieldAction**: 차단 화면의 버튼을 누르면 참은 횟수를 기록하고 앱을 닫아요.
- 앱과 확장 프로그램은 서로 다른 공간에서 실행돼서, **App Group** 이라는 공유 저장소로 데이터를 주고받아요.

## 폴더 구조

```
NoDopamine/
├── Config.xcconfig              ← 내 정보 설정 (처음 한 번)
├── NoDopamine.xcodeproj         ← Xcode로 여는 파일
├── NoDopamine/                  ← 앱 본체
│   ├── NoDopamineApp.swift      ← 앱 시작점
│   ├── ContentView.swift        ← 권한 확인 + 탭 3개
│   ├── Models/
│   │   ├── BlockModel.swift     ← 차단 설정 (지금 바로 / 예약 / 사이트)
│   │   └── FocusModel.swift     ← 집중 세션, 기록, 연속 달성일
│   └── Views/
│       ├── OnboardingView.swift ← 첫 실행 권한 요청 화면
│       ├── ChallengeView.swift  ← 디톡스 탭
│       ├── BlockView.swift      ← 차단 탭
│       ├── WebsiteListView.swift← 사이트 직접 입력
│       ├── SimplifyView.swift   ← 화면 단순화 탭
│       └── Components.swift     ← 카드, 10초 대기 화면 등 공통 부품
├── Shared/                      ← 앱과 확장 프로그램이 함께 쓰는 코드
│   ├── SharedStore.swift        ← 공유 저장소 읽기/쓰기
│   ├── ShieldController.swift   ← 실제로 차단 걸고 푸는 곳
│   ├── ActivityNames.swift      ← 이름표
│   └── AppGroup.swift
├── NoDopamineMonitor/           ← 예약 감시 확장
├── NoDopamineShield/            ← 차단 화면 확장
├── NoDopamineShieldAction/      ← 차단 버튼 확장
└── scripts/generate_project.rb  ← (필요할 때만) Xcode 프로젝트 다시 만들기
```

> 새 `.swift` 파일을 추가할 때는 Xcode에서 폴더를 우클릭 → **New File** 로 만들면 프로젝트에 자동으로 들어가요.

## 알아두면 좋은 한계

- **화면 흑백·홈 화면 정리는 앱이 대신 해줄 수 없어요.** iOS가 보안상 막아두었기 때문에, "화면 단순화" 탭에서 설정 방법을 안내해요.
- **집중 세션은 최소 15분**이에요. iOS 예약 기능이 그보다 짧은 시간은 받지 않아요.
- **앱을 지우면 모든 차단이 풀려요.** (iOS 정책)
- 내 폰에서 쓰는 건 위 과정으로 충분하지만, **App Store에 올리려면** Apple에 [Family Controls 배포 권한](https://developer.apple.com/contact/request/family-controls-distribution)을 따로 신청해야 해요.

## 다음에 추가해 볼 만한 기능

- 앱별 **하루 사용 한도** (예: 인스타그램 하루 30분 넘으면 잠금)
- 앱별 **사용 시간 리포트** (DeviceActivityReport 확장)
- 홈/잠금 화면 **위젯**으로 남은 집중 시간 표시
