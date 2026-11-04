import SwiftUI

/// "디톡스" 탭: 집중 세션 시작, 남은 시간, 연속 기록과 통계
struct ChallengeView: View {
    @EnvironmentObject private var focus: FocusModel
    @EnvironmentObject private var block: BlockModel

    @State private var selectedMinutes = 30
    @State private var showGiveUp = false
    @State private var now = Date()

    private let ticker = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    if let active = focus.active {
                        runningCard(active)
                    } else {
                        startCard
                    }
                    statsRow
                    goalCard
                    weekCard
                    historyCard
                }
                .padding()
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("디톡스")
            .onReceive(ticker) { date in
                now = date
                focus.checkCompletion()
            }
            .sheet(isPresented: $showGiveUp) {
                CooldownSheet(
                    title: "정말 그만둘까요?",
                    message: "포기하면 이 세션은 '실패'로 기록되고\n집중 시간에 포함되지 않아요.",
                    confirmTitle: "포기하기"
                ) {
                    focus.giveUp()
                }
            }
            .alert("알림", isPresented: errorBinding) {
                Button("확인", role: .cancel) {}
            } message: {
                Text(focus.errorMessage ?? "")
            }
        }
    }

    // MARK: - 집중 세션 시작 카드

    private var startCard: some View {
        Card {
            VStack(alignment: .leading, spacing: 16) {
                Text("집중 세션")
                    .font(.title3.bold())
                Text("시간을 정하면 그동안 선택한 앱과 사이트가 잠겨요.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 3), spacing: 10) {
                    ForEach(FocusModel.durationOptions, id: \.self) { minutes in
                        let isSelected = selectedMinutes == minutes
                        Button {
                            selectedMinutes = minutes
                        } label: {
                            Text(Self.durationLabel(minutes))
                                .font(.subheadline.weight(.semibold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(isSelected ? Color.accentColor : Color(.tertiarySystemFill),
                                            in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                                .foregroundStyle(isSelected ? Color.white : Color.primary)
                        }
                        .buttonStyle(.plain)
                    }
                }

                if block.isSelectionEmpty {
                    Label("아직 차단할 앱을 고르지 않았어요. '차단' 탭에서 먼저 골라주세요.",
                          systemImage: "exclamationmark.triangle")
                        .font(.footnote)
                        .foregroundStyle(.orange)
                }

                Button {
                    focus.start(minutes: selectedMinutes)
                } label: {
                    Text("\(Self.durationLabel(selectedMinutes)) 집중 시작")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 4)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
            }
        }
    }

    // MARK: - 진행 중 카드

    private func runningCard(_ active: ActiveFocus) -> some View {
        let total = active.endDate.timeIntervalSince(active.startDate)
        let remaining = max(0, active.endDate.timeIntervalSince(now))
        let progress = total > 0 ? 1 - remaining / total : 1

        return Card {
            VStack(spacing: 18) {
                Text("집중하는 중")
                    .font(.headline)
                    .foregroundStyle(.secondary)

                ZStack {
                    Circle()
                        .stroke(Color.secondary.opacity(0.15), lineWidth: 14)
                    Circle()
                        .trim(from: 0, to: progress)
                        .stroke(Color.accentColor, style: StrokeStyle(lineWidth: 14, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                        .animation(.linear(duration: 1), value: progress)
                    VStack(spacing: 4) {
                        Text(Self.clock(remaining))
                            .font(.system(size: 44, weight: .bold, design: .rounded))
                            .monospacedDigit()
                        Text("\(Self.durationLabel(active.plannedMinutes)) 세션")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                }
                .frame(width: 220, height: 220)

                Text("선택한 앱과 사이트가 잠겨 있어요.\n폰을 내려놓고 하고 싶었던 일을 해봐요.")
                    .font(.subheadline)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)

                Button("포기하기", role: .destructive) {
                    showGiveUp = true
                }
                .buttonStyle(.bordered)
            }
            .frame(maxWidth: .infinity)
        }
    }

    // MARK: - 통계

    private var statsRow: some View {
        HStack(spacing: 12) {
            StatTile(icon: "flame.fill", value: "\(focus.streak)일", label: "연속 달성")
            StatTile(icon: "clock.fill", value: "\(focus.todayMinutes)분", label: "오늘 집중")
            StatTile(icon: "hand.thumbsup.fill", value: "\(focus.todayResists)번", label: "오늘 참은 횟수")
        }
    }

    private var goalCard: some View {
        Card {
            VStack(alignment: .leading, spacing: 12) {
                Stepper(value: $focus.dailyGoalMinutes, in: 15...480, step: 15) {
                    Text("하루 목표  **\(Self.durationLabel(focus.dailyGoalMinutes))**")
                }
                ProgressView(value: Double(min(focus.todayMinutes, focus.dailyGoalMinutes)),
                             total: Double(focus.dailyGoalMinutes))
                Text(focus.todayMinutes >= focus.dailyGoalMinutes
                     ? "오늘 목표 달성! 🎉"
                     : "목표까지 \(focus.dailyGoalMinutes - focus.todayMinutes)분 남았어요. 목표를 채운 날이 이어지면 연속 달성일이 늘어요.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var weekCard: some View {
        let days = focus.lastSevenDays
        let goal = focus.dailyGoalMinutes
        let maxMinutes = max(goal, days.map(\.minutes).max() ?? 0, 1)

        return Card {
            VStack(alignment: .leading, spacing: 12) {
                Text("최근 7일")
                    .font(.headline)
                HStack(alignment: .bottom, spacing: 10) {
                    ForEach(days) { item in
                        VStack(spacing: 6) {
                            Text("\(item.minutes)")
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                            RoundedRectangle(cornerRadius: 6, style: .continuous)
                                .fill(item.minutes >= goal ? Color.accentColor : Color.accentColor.opacity(0.3))
                                .frame(height: max(4, 110 * CGFloat(item.minutes) / CGFloat(maxMinutes)))
                            Text(item.day.formatted(.dateTime.weekday(.narrow)))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                    }
                }
                .frame(height: 150, alignment: .bottom)
            }
        }
    }

    private var historyCard: some View {
        Card {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text("기록")
                        .font(.headline)
                    Spacer()
                    Text("완료 \(focus.completedCount)회")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                if focus.records.isEmpty {
                    Text("아직 기록이 없어요. 첫 집중 세션을 시작해 보세요!")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(focus.records.prefix(10)) { record in
                        HStack {
                            Image(systemName: record.completed ? "checkmark.circle.fill" : "xmark.circle.fill")
                                .foregroundStyle(record.completed ? Color.green : Color.red)
                            if record.completed {
                                Text("\(Self.durationLabel(record.plannedMinutes)) 집중 완료")
                            } else {
                                let lasted = Int(record.endDate.timeIntervalSince(record.startDate) / 60)
                                Text("\(lasted)분 만에 포기")
                            }
                            Spacer()
                            Text(record.startDate.formatted(.dateTime.month().day().hour().minute()))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .font(.subheadline)
                    }
                }
            }
        }
    }

    // MARK: - 도우미

    private var errorBinding: Binding<Bool> {
        Binding(
            get: { focus.errorMessage != nil },
            set: { if !$0 { focus.errorMessage = nil } }
        )
    }

    /// 1500초 → "25:00", 3900초 → "1:05:00"
    static func clock(_ interval: TimeInterval) -> String {
        let seconds = Int(interval.rounded(.up))
        let h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60
        return h > 0 ? String(format: "%d:%02d:%02d", h, m, s) : String(format: "%02d:%02d", m, s)
    }

    /// 30 → "30분", 60 → "1시간", 90 → "1시간 30분"
    static func durationLabel(_ minutes: Int) -> String {
        let h = minutes / 60, m = minutes % 60
        switch (h, m) {
        case (0, _): return "\(m)분"
        case (_, 0): return "\(h)시간"
        default: return "\(h)시간 \(m)분"
        }
    }
}
