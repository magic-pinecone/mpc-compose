@preconcurrency import Shared
import SwiftUI

struct CourseSnippetRow: Identifiable {
    let id: String
    let leading: String?
    let title: String
    let subtitle: String
    let detail: String

    init(course: CourseSummary) {
        id = course.serialNumber
        leading = nil
        title = course.title
        subtitle = Self.subtitle(course)
        detail = course.meetingsDescription
    }

    init(meeting: ClassMeeting) {
        id = "\(meeting.course.serialNumber)-\(meeting.startMinute)"
        leading = clockLabel(meeting.startMinute)
        title = meeting.course.title
        subtitle = Self.subtitle(meeting.course)
        detail = "\(meeting.periodLabel()) \(meeting.timeRangeLabel())"
    }

    init(upcoming: UpcomingMeeting) {
        let meeting = upcoming.meeting
        id = meeting.course.serialNumber
        leading = upcoming.inProgress ? "上課中" : upcoming.whenLabel()
        title = meeting.course.title
        subtitle = Self.subtitle(meeting.course)
        detail = "\(meeting.periodLabel()) \(meeting.timeRangeLabel())"
    }

    private static func subtitle(_ course: CourseSummary) -> String {
        ([course.classNo] + course.teachers.filter { !$0.isEmpty }.prefix(2))
            .filter { !$0.isEmpty }
            .joined(separator: " · ")
    }
}

/// The card Siri and Shortcuts show under an intent's spoken answer.
struct CourseSnippetList: View {
    let rows: [CourseSnippetRow]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if rows.isEmpty {
                Label("沒有課程", systemImage: "calendar")
                    .foregroundStyle(.secondary)
            }
            ForEach(Array(rows.enumerated()), id: \.element.id) { index, row in
                if index > 0 {
                    Divider()
                }
                HStack(alignment: .firstTextBaseline, spacing: 12) {
                    if let leading = row.leading {
                        Text(leading)
                            .font(.subheadline.monospacedDigit().weight(.semibold))
                            .foregroundStyle(.tint)
                            .frame(minWidth: 52, alignment: .leading)
                    }
                    VStack(alignment: .leading, spacing: 2) {
                        Text(row.title)
                            .font(.headline)
                        if !row.subtitle.isEmpty {
                            Text(row.subtitle)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        Text(row.detail)
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                            .lineLimit(2)
                    }
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct CourseSnippetDetails {
    let title: String
    let subtitle: String
    let facts: [(label: String, value: String)]
    let meetings: [String]
    let sections: [(title: String, text: String)]
    let distributionRules: [String]?

    init(course: CourseSummary, detail: CourseDetail?) {
        title = course.title
        subtitle = ([course.classNo] + course.teachers.filter { !$0.isEmpty })
            .filter { !$0.isEmpty }
            .joined(separator: " · ")
        var facts = [
            (label: "學分", value: course.credit.formatted()),
            (label: "修別", value: course.courseType.description_),
            (label: "開課", value: [course.collegeName, course.departmentName]
                .filter { !$0.isEmpty }
                .joined(separator: "／")),
            (label: "名額", value: "\(course.admitCnt)／\(course.limitCnt) 人"),
            (label: "密碼卡", value: course.passwordCard.description_)
        ]
        if course.waitCnt > 0 {
            facts.insert((label: "候補", value: "\(course.waitCnt) 人"), at: 4)
        }
        self.facts = facts
        meetings = course.meetings()
            .map { "星期\($0.day.description_) \($0.periodLabel()) \($0.timeRangeLabel())" }
        sections = detail.map { detail in
            [
                (title: "課程目標", text: detail.objectives),
                (title: "課程內容", text: detail.content),
                (title: "教科書", text: detail.books),
                (title: "教學方式", text: detail.teachingMethod),
                (title: "評量方式", text: detail.gradingPolicy)
            ]
            .map { section in
                // Catalog text often carries blank lines, which waste the card's few lines.
                let lines = section.text
                    .split(whereSeparator: \.isNewline)
                    .map { $0.trimmingCharacters(in: .whitespaces) }
                    .filter { !$0.isEmpty }
                return (title: section.title, text: lines.joined(separator: "\n"))
            }
            .filter { !$0.text.isEmpty }
        } ?? []
        distributionRules = detail?.rankedDistributionRules
    }
}

/// The card Siri and Shortcuts show for one course's details.
struct CourseDetailSnippet: View {
    let details: CourseSnippetDetails?

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if let details {
                VStack(alignment: .leading, spacing: 2) {
                    Text(details.title)
                        .font(.headline)
                    if !details.subtitle.isEmpty {
                        Text(details.subtitle)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                }
                Grid(alignment: .leading, horizontalSpacing: 16, verticalSpacing: 6) {
                    ForEach(details.facts, id: \.label) { fact in
                        GridRow {
                            Text(fact.label)
                                .foregroundStyle(.secondary)
                            Text(fact.value)
                        }
                    }
                }
                .font(.subheadline)
                Divider()
                if details.meetings.isEmpty {
                    Label("未提供上課時間", systemImage: "calendar")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(details.meetings, id: \.self) { meeting in
                        Label(meeting, systemImage: "calendar")
                            .font(.subheadline)
                    }
                }
                CourseDetailSnippetDescription(details: details)
            } else {
                Label("找不到這門課", systemImage: "book.closed")
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct CourseDetailSnippetDescription: View {
    let details: CourseSnippetDetails

    var body: some View {
        if let rules = details.distributionRules {
            ForEach(details.sections, id: \.title) { section in
                Divider()
                VStack(alignment: .leading, spacing: 4) {
                    Text(section.title)
                        .font(.subheadline.weight(.semibold))
                    // A Siri card cannot scroll; the full text stays on the returned course.
                    Text(section.text)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .lineLimit(4)
                }
            }
            Divider()
            VStack(alignment: .leading, spacing: 4) {
                Text("分發條件")
                    .font(.subheadline.weight(.semibold))
                if rules.isEmpty {
                    Text("無")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                ForEach(Array(rules.enumerated()), id: \.offset) { index, rule in
                    Text("\(index + 1). \(rule)")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
        } else {
            Divider()
            Label("目前無法載入課程說明與分發條件", systemImage: "exclamationmark.circle")
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
    }
}
