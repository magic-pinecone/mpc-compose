import AppIntents
@preconcurrency import Shared

@available(iOS 27.0, *)
@AppIntent(schema: .system.searchInApp)
struct SearchCoursesInAppIntent: ShowInAppSearchResultsIntent {
    static let title: LocalizedStringResource = "在選課中搜尋"
    static let description = IntentDescription("開啟選課並搜尋課程。")
    static let searchScopes: [StringSearchScope] = [.general]

    @Parameter(title: "搜尋內容", requestValueDialog: "要搜尋什麼課程？")
    var criteria: StringSearchCriteria

    @Dependency private var router: AppRouter

    @MainActor
    func perform() async throws -> some IntentResult {
        router.open(.catalogSearch(criteria.term))
        return .result()
    }
}

struct FindCoursesIntent: AppIntent {
    static let title: LocalizedStringResource = "找課程"
    static let description = IntentDescription("依關鍵字、老師、時段或修別找本學期的課程。")

    @Parameter(title: "關鍵字")
    var keyword: String?

    @Parameter(title: "老師")
    var teacher: String?

    @Parameter(title: "星期")
    var day: WeekdayOption?

    @Parameter(title: "時段")
    var timeOfDay: TimeOfDayOption?

    @Parameter(title: "修別")
    var courseType: CourseTypeOption?

    @Parameter(title: "只看有名額", default: false)
    var openSeatsOnly: Bool

    static var parameterSummary: some ParameterSummary {
        Summary("找\(\.$keyword)課程") {
            \.$teacher
            \.$day
            \.$timeOfDay
            \.$courseType
            \.$openSeatsOnly
        }
    }

    @Dependency private var sharedHost: IosSharedHost

    func perform() async throws -> some ReturnsValue<[CourseEntity]> & ProvidesDialog & ShowsSnippetView {
        let semester = CourseIdentity.currentSemester
        let courses = try await matchingCourses(semester: semester)
        let entities = courses.prefix(20).map { CourseEntity(semester: semester, course: $0) }
        return .result(
            value: Array(entities),
            dialog: IntentDialog(stringLiteral: Self.summary(of: courses)),
            view: CourseSnippetList(rows: courses.prefix(5).map(CourseSnippetRow.init(course:)))
        )
    }

    private func matchingCourses(semester: String) async throws -> [CourseSummary] {
        let words = [keyword, teacher].compactMap { $0?.trimmingCharacters(in: .whitespaces) }
        let keywordOnly = keyword?.trimmingCharacters(in: .whitespaces) ?? ""
        let filters = sharedFilters
        if let teacher = teacher?.trimmingCharacters(in: .whitespaces), !teacher.isEmpty {
            let exact = try await sharedHost.findCourses(
                semester: semester,
                keyword: keywordOnly,
                filters: filters + [CourseFilterTeacher(name: teacher)],
                limit: Int32.max
            )
            if !exact.isEmpty { return exact }
        }
        // A spoken teacher name rarely matches the catalog exactly, so search it as text instead.
        return try await sharedHost.findCourses(
            semester: semester,
            keyword: words.filter { !$0.isEmpty }.joined(separator: " "),
            filters: filters,
            limit: Int32.max
        )
    }

    private var sharedFilters: [CourseFilter] {
        var filters: [CourseFilter] = []
        if let courseType {
            filters.append(CourseFilterType(type: courseType.courseType))
        }
        if openSeatsOnly {
            filters.append(CourseFilterSeats(availability: .open))
        }
        if day != nil || timeOfDay != nil {
            let days = day.map { [$0] } ?? WeekdayOption.weekdays
            let periods = timeOfDay?.periods ?? CoursePeriod.allCases
            let slots = days.flatMap { day in
                periods.map { CourseTime(day: day.courseDay, period: $0) }
            }
            filters.append(CourseFilterSchedule(slots: Set(slots), match: .overlaps))
        }
        return filters
    }

    private static func summary(of courses: [CourseSummary]) -> String {
        guard !courses.isEmpty else { return "找不到符合的課程。" }
        let top = courses.prefix(3).map(\.spokenName).joined(separator: "、")
        if courses.count <= 3 {
            return "找到 \(courses.count) 門課：\(top)。"
        }
        return "找到 \(courses.count) 門課，前 3 門是：\(top)。"
    }
}

struct CourseDetailsIntent: AppIntent {
    static let title: LocalizedStringResource = "課程資訊"
    static let description = IntentDescription("查看一門課的學分、上課時間、名額、課程說明與分發條件。")

    @Parameter(title: "課程", requestValueDialog: "要查哪一門課？")
    var course: CourseEntity

    static var parameterSummary: some ParameterSummary {
        Summary("查看\(\.$course)的課程資訊")
    }

    @Dependency private var sharedHost: IosSharedHost

    func perform() async throws -> some ReturnsValue<CourseEntity> & ProvidesDialog & ShowsSnippetView {
        // Read the catalog again so seats are current, not what the entity held when it was resolved.
        guard let identity = course.identity,
              let summary = try await sharedHost.course(
                  semester: identity.semester,
                  serialNumber: identity.serialNumber
              ) else {
            return .result(
                value: course,
                dialog: "找不到這門課的資訊。",
                view: CourseDetailSnippet(details: nil)
            )
        }
        // The description is a separate request; answer with the basics when it fails.
        let detail = try? await sharedHost.loadCourseDetail(
            semester: identity.semester,
            serialNumber: identity.serialNumber
        )
        let entity = detail.map { CourseEntity(semester: identity.semester, course: summary, detail: $0) }
            ?? CourseEntity(semester: identity.semester, course: summary)
        return .result(
            value: entity,
            dialog: IntentDialog(stringLiteral: summary.spokenDetails),
            view: CourseDetailSnippet(details: CourseSnippetDetails(course: summary, detail: detail))
        )
    }
}

extension CourseSummary {
    /// "王小明老師的微積分", which tells sections of the same course apart when read aloud.
    var spokenName: String {
        guard let teacher = teachers.first(where: { !$0.isEmpty }) else { return title }
        return "\(teacher)老師的\(title)"
    }

    /// "王小明老師的微積分，3 學分，必修。上課時間：…。已選 50／60 人，尚餘 10 個名額。"
    var spokenDetails: String {
        "\(spokenName)，\(credit.formatted()) 學分，\(courseType.description_)。"
            + "上課時間：\(meetingsDescription)。\(seatsDescription)。"
    }
}
