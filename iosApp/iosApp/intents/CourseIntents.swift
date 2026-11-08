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

/// The Course Catalog search with the same keyword and filters as the search bar and filter sheet, answered in Siri.
struct FindCoursesIntent: AppIntent {
    static let title: LocalizedStringResource = "找課程"
    static let description = IntentDescription("用選課的搜尋與篩選條件找本學期的課程，直接顯示結果。")

    @Parameter(title: "關鍵字", description: "課名、課號、老師或系所。", requestValueDialog: "要找什麼課？")
    var keyword: String?

    @Parameter(title: "課程類型")
    var courseType: CourseTypeOption?

    @Parameter(title: "名額")
    var seats: SeatsOption?

    @Parameter(title: "學分", optionsProvider: CreditOptions())
    var credits: [Double]?

    @Parameter(title: "系所", optionsProvider: DepartmentOptions())
    var departments: [String]?

    @Parameter(title: "學院", optionsProvider: CollegeOptions())
    var colleges: [String]?

    @Parameter(title: "老師", optionsProvider: TeacherOptions())
    var teachers: [String]?

    @Parameter(title: "星期")
    var day: WeekdayOption?

    @Parameter(title: "時段")
    var timeOfDay: TimeOfDayOption?

    @Parameter(title: "節次")
    var periods: [PeriodOption]?

    static var parameterSummary: some ParameterSummary {
        Summary("找\(\.$keyword)課程") {
            \.$courseType
            \.$seats
            \.$credits
            \.$departments
            \.$colleges
            \.$teachers
            \.$day
            \.$timeOfDay
            \.$periods
        }
    }

    @Dependency private var sharedHost: IosSharedHost

    func perform() async throws -> some ReturnsValue<[CourseEntity]> & ProvidesDialog & ShowsSnippetView {
        let semester = CourseIdentity.currentSemester
        let query = try await courseQuery(semester: semester)
        if query.keyword.isEmpty, query.filters.isEmpty {
            throw $keyword.needsValueError()
        }
        let courses = try await sharedHost.findCourses(
            semester: semester,
            keyword: query.keyword,
            filters: query.filters,
            limit: Int32.max
        )
        let entities = courses.prefix(20).map { CourseEntity(semester: semester, course: $0) }
        return .result(
            value: Array(entities),
            dialog: IntentDialog(
                full: LocalizedStringResource(stringLiteral: Self.spokenSummary(of: courses)),
                supporting: LocalizedStringResource(stringLiteral: Self.countSummary(of: courses))
            ),
            view: CourseSearchSnippet(
                criteria: query.criteria,
                total: courses.count,
                rows: courses.prefix(5).map(CourseSnippetRow.init(course:))
            )
        )
    }

    /// Builds the filters the sheet would hold for these parameters.
    private func courseQuery(semester: String) async throws -> IntentCourseQuery {
        var query = IntentCourseQuery(keyword: keyword?.trimmingCharacters(in: .whitespaces) ?? "")
        if let courseType {
            query.add(CourseFilterType(type: courseType.courseType))
        }
        if let seats {
            query.add(CourseFilterSeats(availability: seats.availability))
        }
        for value in credits ?? [] {
            query.add(CourseFilterCredits(credits: value))
        }
        let names = [departments, colleges, teachers].contains { !($0 ?? []).isEmpty }
            ? try await sharedHost.courseFilterOptions(semester: semester)
            : nil
        if let names {
            query.addNames(departments, choices: names.departments, make: CourseFilterDepartment.init(name:))
            query.addNames(colleges, choices: names.colleges, make: CourseFilterCollege.init(name:))
            query.addNames(teachers, choices: names.teachers, make: CourseFilterTeacher.init(name:))
        }
        if let schedule {
            query.add(schedule.filter, label: schedule.label)
        }
        return query
    }

    /// The slot grid selection: the chosen day (or every day) times the chosen periods (or every period).
    private var schedule: (filter: CourseFilterSchedule, label: String)? {
        let chosenPeriods = (periods ?? []).map(\.coursePeriod) + (timeOfDay?.periods ?? [])
        guard day != nil || !chosenPeriods.isEmpty else { return nil }
        let days = day.map { [$0] } ?? WeekdayOption.allCases
        let slotPeriods = chosenPeriods.isEmpty ? CoursePeriod.allCases : chosenPeriods
        let slots = days.flatMap { day in
            slotPeriods.map { CourseTime(day: day.courseDay, period: $0) }
        }
        let periodNumbers = (periods ?? []).map(\.coursePeriod.description_)
        let periodLabel = periodNumbers.isEmpty ? nil : "第 \(periodNumbers.joined(separator: "、")) 節"
        let label = [day?.label, timeOfDay?.label, periodLabel].compactMap { $0 }.joined(separator: " ")
        // Like the slot grid, a course matches only when every meeting falls within the chosen slots.
        return (CourseFilterSchedule(slots: Set(slots), match: .fitsWithin), label)
    }

    private static func countSummary(of courses: [CourseSummary]) -> String {
        courses.isEmpty ? "找不到符合的課程。" : "找到 \(courses.count) 門課。"
    }

    private static func spokenSummary(of courses: [CourseSummary]) -> String {
        guard !courses.isEmpty else { return countSummary(of: courses) }
        let top = courses.prefix(3).map(\.spokenName).joined(separator: "、")
        if courses.count <= 3 {
            return "找到 \(courses.count) 門課：\(top)。"
        }
        return "找到 \(courses.count) 門課，前 3 門是：\(top)。"
    }
}

/// A Course Query assembled from intent parameters, with a label for each criterion the snippet shows.
private struct IntentCourseQuery {
    var keyword: String
    var filters: [CourseFilter] = []
    var criteria: [String] = []

    init(keyword: String) {
        self.keyword = keyword
        if !keyword.isEmpty { criteria.append("「\(keyword)」") }
    }

    mutating func add(_ filter: CourseFilter, label: String? = nil) {
        filters.append(filter)
        criteria.append(label ?? CourseFilterLabelKt.label(filter))
    }

    /// Uses a catalog name as a filter, the way the sheet does; a spoken name that matches none is searched as text.
    mutating func addNames(_ names: [String]?, choices: [String], make: (String) -> CourseFilter) {
        for name in names ?? [] {
            let trimmed = name.trimmingCharacters(in: .whitespaces)
            guard !trimmed.isEmpty else { continue }
            let sameName = { (choice: String) in
                choice.compare(trimmed, options: [.caseInsensitive, .widthInsensitive]) == .orderedSame
            }
            if let choice = choices.first(where: sameName) {
                add(make(choice))
            } else {
                keyword = [keyword, trimmed].filter { !$0.isEmpty }.joined(separator: " ")
                criteria.append("「\(trimmed)」")
            }
        }
    }
}

extension CourseSummary {
    /// "王小明老師的微積分", which tells sections of the same course apart when read aloud.
    var spokenName: String {
        guard let teacher = teachers.first(where: { !$0.isEmpty }) else { return title }
        return "\(teacher)老師的\(title)"
    }
}
