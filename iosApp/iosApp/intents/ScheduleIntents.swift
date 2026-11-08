import AppIntents
@preconcurrency import Shared

struct NextClassIntent: AppIntent {
    static let title: LocalizedStringResource = "下一堂課"
    static let description = IntentDescription("從已儲存的課表找出下一堂課。")

    @Dependency private var sharedHost: IosSharedHost

    func perform() async throws -> some ReturnsValue<CourseEntity?> & ProvidesDialog & ShowsSnippetView {
        let clock = ClassClock()
        let semester = CourseIdentity.currentSemester
        guard let next = try await sharedHost.nextSavedMeeting(
            semester: semester,
            day: clock.day,
            minuteOfDay: clock.minuteOfDay
        ) else {
            return .result(
                value: nil,
                dialog: "目前的課表沒有課程。",
                view: CourseSnippetList(rows: [])
            )
        }
        let meeting = next.meeting
        let title = meeting.course.title
        let dialog = next.inProgress
            ? "現在正在上\(title)，\(clockLabel(meeting.endMinute)) 下課。"
            : "下一堂是\(title)，\(next.whenLabel()) 開始（\(meeting.periodLabel())）。"
        return .result(
            value: CourseEntity(semester: semester, course: meeting.course),
            dialog: IntentDialog(stringLiteral: dialog),
            view: CourseSnippetList(rows: [CourseSnippetRow(upcoming: next)])
        )
    }
}

struct ClassesOnDayIntent: AppIntent {
    static let title: LocalizedStringResource = "某天的課"
    static let description = IntentDescription("列出已儲存的課表在某一天的課程。")

    @Parameter(title: "星期", description: "不指定時為今天。")
    var day: WeekdayOption?

    static var parameterSummary: some ParameterSummary {
        Summary("\(\.$day)有什麼課")
    }

    @Dependency private var sharedHost: IosSharedHost

    func perform() async throws -> some ReturnsValue<[CourseEntity]> & ProvidesDialog & ShowsSnippetView {
        let semester = CourseIdentity.currentSemester
        let today = ClassClock().day
        let courseDay = day?.courseDay ?? today
        let dayName = courseDay == today ? "今天" : "星期\(courseDay.description_)"
        let meetings = try await sharedHost.savedMeetingsOn(semester: semester, day: courseDay)
        let dialog = meetings.isEmpty
            ? "\(dayName)沒有課。"
            : "\(dayName)有 \(meetings.count) 堂課："
                + meetings.map { "\(clockLabel($0.startMinute)) \($0.course.title)" }.joined(separator: "、")
                + "。"
        return .result(
            value: meetings.map { CourseEntity(semester: semester, course: $0.course) },
            dialog: IntentDialog(stringLiteral: dialog),
            view: CourseSnippetList(rows: meetings.map(CourseSnippetRow.init(meeting:)))
        )
    }
}

/// "13:00" for a minute of the day.
func clockLabel(_ minuteOfDay: Int32) -> String {
    String(format: "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60)
}
