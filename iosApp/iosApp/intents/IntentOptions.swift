import AppIntents
import Foundation
@preconcurrency import Shared

enum WeekdayOption: String, AppEnum {
    case monday, tuesday, wednesday, thursday, friday, saturday, sunday

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: "星期")
    static let caseDisplayRepresentations: [Self: DisplayRepresentation] = [
        .monday: DisplayRepresentation(title: "星期一", synonyms: ["週一", "禮拜一"]),
        .tuesday: DisplayRepresentation(title: "星期二", synonyms: ["週二", "禮拜二"]),
        .wednesday: DisplayRepresentation(title: "星期三", synonyms: ["週三", "禮拜三"]),
        .thursday: DisplayRepresentation(title: "星期四", synonyms: ["週四", "禮拜四"]),
        .friday: DisplayRepresentation(title: "星期五", synonyms: ["週五", "禮拜五"]),
        .saturday: DisplayRepresentation(title: "星期六", synonyms: ["週六", "禮拜六"]),
        .sunday: DisplayRepresentation(title: "星期日", synonyms: ["星期天", "週日", "禮拜天"])
    ]

    static let weekdays: [Self] = [.monday, .tuesday, .wednesday, .thursday, .friday]

    var courseDay: CourseDay {
        switch self {
        case .monday: .monday
        case .tuesday: .tuesday
        case .wednesday: .wednesday
        case .thursday: .thursday
        case .friday: .friday
        case .saturday: .saturday
        case .sunday: .sunday
        }
    }

    var label: String { "星期\(courseDay.description_)" }
}

/// The current weekday and minute of the day, in the terms the Saved Course Plan queries use.
struct ClassClock {
    let day: CourseDay
    let minuteOfDay: Int32

    init(date: Date = .now, calendar: Calendar = .current) {
        // Calendar weekdays start at 1 for Sunday; course days start at 1 for Monday.
        let isoWeekday = (calendar.component(.weekday, from: date) + 5) % 7 + 1
        day = CourseDay.companion.fromIsoWeekday(isoWeekday: Int32(isoWeekday))
        let time = calendar.dateComponents([.hour, .minute], from: date)
        minuteOfDay = Int32((time.hour ?? 0) * 60 + (time.minute ?? 0))
    }
}

enum TimeOfDayOption: String, AppEnum {
    case morning, noon, afternoon, evening

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: "時段")
    static let caseDisplayRepresentations: [Self: DisplayRepresentation] = [
        .morning: DisplayRepresentation(title: "上午", subtitle: "第 1–4 節", synonyms: ["早上"]),
        .noon: DisplayRepresentation(title: "中午", subtitle: "第 Z 節"),
        .afternoon: DisplayRepresentation(title: "下午", subtitle: "第 5–9 節"),
        .evening: DisplayRepresentation(title: "晚上", subtitle: "第 A–D 節", synonyms: ["夜間"])
    ]

    var periods: [CoursePeriod] {
        switch self {
        case .morning: [.one, .two, .three, .four]
        case .noon: [.noon]
        case .afternoon: [.five, .six, .seven, .eight, .nine]
        case .evening: [.a, .b, .c, .d]
        }
    }
}

enum CourseTypeOption: String, AppEnum {
    case required, elective

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: "修別")
    static let caseDisplayRepresentations: [Self: DisplayRepresentation] = [
        .required: "必修",
        .elective: "選修"
    ]

    var courseType: CourseType {
        switch self {
        case .required: .required
        case .elective: .elective
        }
    }
}
