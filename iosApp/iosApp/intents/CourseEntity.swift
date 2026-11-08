import AppIntents
import CoreSpotlight
@preconcurrency import Shared

/// A course in the Course Catalog, identified by its Course Identity.
struct CourseEntity: IndexedEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: "課程",
        numericFormat: "\(placeholder: .int) 門課程"
    )
    static let defaultQuery = CourseEntityQuery()

    let id: String

    @Property(title: "課程名稱")
    var title: String

    @Property(title: "班次")
    var classNo: String

    @Property(title: "授課教師")
    var teachers: [String]

    @Property(title: "開課系所")
    var department: String

    @Property(title: "學院")
    var college: String

    @Property(title: "學分")
    var credits: Double

    @Property(title: "修別")
    var courseType: String

    @Property(title: "上課時間")
    var schedule: String

    @Property(title: "名額")
    var seats: String

    init(semester: String, course: CourseSummary) {
        id = CourseIdentity(semester: semester, serialNumber: course.serialNumber).entityID
        title = course.title
        classNo = course.classNo
        teachers = course.teachers.filter { !$0.isEmpty }
        department = course.departmentName
        college = course.collegeName
        credits = course.credit
        courseType = course.courseType.description_
        schedule = course.meetingsDescription
        seats = course.seatsDescription
    }

    var identity: CourseIdentity? { CourseIdentity(entityID: id) }

    var subtitle: String {
        [classNo, teachers.joined(separator: "、")]
            .filter { !$0.isEmpty }
            .joined(separator: " · ")
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: "\(subtitle)",
            image: .init(systemName: "book.closed")
        )
    }

    var attributeSet: CSSearchableItemAttributeSet {
        let attributes = defaultAttributeSet
        attributes.contentDescription = "\(subtitle)\n\(schedule)"
        attributes.keywords = teachers + [department, college, classNo, identity?.serialNumber ?? ""]
            .filter { !$0.isEmpty }
        return attributes
    }
}

extension CourseSummary {
    /// "星期一 第 1–2 節 08:00–09:50；星期三 第 3 節 10:00–10:50"
    var meetingsDescription: String {
        let meetings = meetings()
        guard !meetings.isEmpty else { return "未提供上課時間" }
        return meetings
            .map { "星期\($0.day.description_) \($0.periodLabel()) \($0.timeRangeLabel())" }
            .joined(separator: "；")
    }

    var seatsDescription: String {
        let remaining = Int(limitCnt) - Int(admitCnt)
        return remaining > 0
            ? "已選 \(admitCnt)／\(limitCnt) 人，尚餘 \(remaining) 個名額"
            : "已選 \(admitCnt)／\(limitCnt) 人，已額滿"
    }
}

struct CourseEntityQuery: EntityStringQuery {
    @Dependency private var sharedHost: IosSharedHost

    func entities(for identifiers: [CourseEntity.ID]) async throws -> [CourseEntity] {
        var entities: [CourseEntity] = []
        for identity in identifiers.compactMap(CourseIdentity.init(entityID:)) {
            if let course = try await sharedHost.course(
                semester: identity.semester,
                serialNumber: identity.serialNumber
            ) {
                entities.append(CourseEntity(semester: identity.semester, course: course))
            }
        }
        return entities
    }

    func entities(matching string: String) async throws -> [CourseEntity] {
        let semester = CourseIdentity.currentSemester
        return try await sharedHost
            .findCourses(semester: semester, keyword: string, filters: [], limit: 10)
            .map { CourseEntity(semester: semester, course: $0) }
    }

    func suggestedEntities() async throws -> [CourseEntity] {
        try await sharedHost.savedPlanCourses()
            .map { CourseEntity(semester: CourseIdentity.currentSemester, course: $0) }
    }
}

extension IosSharedHost {
    /// Courses of the Saved Course Plan in weekly order, each once.
    func savedPlanCourses() async throws -> [CourseSummary] {
        var seen = Set<String>()
        var courses: [CourseSummary] = []
        for day in CourseDay.allCases where day != .unknown {
            for meeting in try await savedMeetingsOn(semester: CourseIdentity.currentSemester, day: day)
            where seen.insert(meeting.course.serialNumber).inserted {
                courses.append(meeting.course)
            }
        }
        return courses
    }
}
