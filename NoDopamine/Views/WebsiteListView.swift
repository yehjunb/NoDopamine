import SwiftUI

/// 차단할 웹사이트 주소를 직접 입력하는 화면
struct WebsiteListView: View {
    let isLocked: Bool

    @EnvironmentObject private var block: BlockModel
    @State private var input = ""

    private let suggestions = [
        "youtube.com", "instagram.com", "tiktok.com", "x.com",
        "facebook.com", "reddit.com", "twitch.tv", "namu.wiki",
    ]

    var body: some View {
        Form {
            Section {
                HStack {
                    TextField("예: youtube.com", text: $input)
                        .keyboardType(.URL)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .onSubmit(add)
                    Button("추가", action: add)
                        .disabled(input.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            } footer: {
                Text("차단이 켜지면 Safari 등에서 이 사이트가 열리지 않아요. 모바일 주소(m.youtube.com 등)가 따로 있다면 함께 추가해 주세요.")
            }

            let remaining = suggestions.filter { !block.domains.contains($0) }
            if !remaining.isEmpty {
                Section("자주 차단하는 사이트") {
                    ForEach(remaining, id: \.self) { domain in
                        Button {
                            block.addDomain(domain)
                        } label: {
                            Label(domain, systemImage: "plus.circle")
                        }
                    }
                }
            }

            Section {
                if block.domains.isEmpty {
                    Text("아직 추가한 사이트가 없어요")
                        .foregroundStyle(.secondary)
                }
                ForEach(block.domains, id: \.self) { domain in
                    Label(domain, systemImage: "globe")
                }
                .onDelete { offsets in
                    block.removeDomains(at: offsets)
                }
                .deleteDisabled(isLocked)
            } header: {
                Text("차단할 사이트 \(block.domains.count)/\(ShieldController.maxDomains)")
            } footer: {
                if isLocked {
                    Text("차단 중에는 추가만 할 수 있고 지울 수는 없어요.")
                } else {
                    Text("왼쪽으로 밀어서 지울 수 있어요.")
                }
            }
        }
        .navigationTitle("웹사이트 차단")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func add() {
        if block.addDomain(input) {
            input = ""
        }
    }
}
