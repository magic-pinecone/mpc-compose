@preconcurrency import Shared
import SwiftUI

struct CourseSnippetRow: Identifiable {
    let id: String
    let leading: String?
    let title: String
    let people: String
    let time: String
    let badges: [String]

    init(course: CourseSummary) {
        id = course.serialNumber
        leading = nil
        title = course.title
        people = Self.people(course)
        time = course.meetingsDescription
        badges = [
            CourseFilterLabelKt.label(CourseFilterCredits(credits: course.credit)),
            course.courseType == .unknown ? nil : course.courseType.description_,
            course.seatsBadge
        ].compactMap { $0 }
    }

    init(meeting: ClassMeeting) {
        id = "\(meeting.course.serialNumber)-\(meeting.startMinute)"
        leading = clockLabel(meeting.startMinute)
        title = meeting.course.title
        people = Self.people(meeting.course)
        time = "\(meeting.periodLabel()) \(meeting.timeRangeLabel())"
        badges = []
    }

    init(upcoming: UpcomingMeeting) {
        let meeting = upcoming.meeting
        id = meeting.course.serialNumber
        leading = upcoming.inProgress ? "上課中" : upcoming.whenLabel()
        title = meeting.course.title
        people = Self.people(meeting.course)
        time = "\(meeting.periodLabel()) \(meeting.timeRangeLabel())"
        badges = []
    }

    private static func people(_ course: CourseSummary) -> String {
        (course.teachers.filter { !$0.isEmpty }.prefix(2) + [course.classNo])
            .filter { !$0.isEmpty }
            .joined(separator: " · ")
    }
}

private extension CourseSummary {
    var seatsBadge: String {
        let remaining = Int(limitCnt) - Int(admitCnt)
        if limitCnt == 0 { return "不限名額" }
        return remaining > 0 ? "尚餘 \(remaining) 名額" : "已額滿"
    }
}

/// Siri draws snippets on its own material, where a concrete label color stays legible in both appearances.
private extension ShapeStyle where Self == Color {
    static var snippetSecondary: Color { Color(uiColor: .secondaryLabel) }
}

/// The card Siri and Shortcuts show under a schedule intent's spoken answer.
struct CourseSnippetList: View {
    let rows: [CourseSnippetRow]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            if rows.isEmpty {
                Label("沒有課程", systemImage: "calendar")
                    .foregroundStyle(.snippetSecondary)
            }
            CourseSnippetRows(rows: rows)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// The card for a Course Catalog search: what Siri searched for, then the best matches.
struct CourseSearchSnippet: View {
    let criteria: [String]
    let total: Int
    let rows: [CourseSnippetRow]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            VStack(alignment: .leading, spacing: 4) {
                Label(criteria.joined(separator: " · "), systemImage: "magnifyingglass")
                    .font(.subheadline.weight(.medium))
                    .lineLimit(2)
                Text(total == 0 ? "沒有符合的課程" : "\(total) 門課程")
                    .font(.footnote)
                    .foregroundStyle(.snippetSecondary)
            }
            if !rows.isEmpty {
                Divider()
                CourseSnippetRows(rows: rows)
            }
            if total > rows.count {
                Text("還有 \(total - rows.count) 門，可在選課中查看。")
                    .font(.footnote)
                    .foregroundStyle(.snippetSecondary)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct CourseSnippetRows: View {
    let rows: [CourseSnippetRow]

    var body: some View {
        ForEach(Array(rows.enumerated()), id: \.element.id) { index, row in
            if index > 0 {
                Divider()
            }
            VStack(alignment: .leading, spacing: 4) {
                if let leading = row.leading {
                    Text(leading)
                        .font(.subheadline.monospacedDigit().weight(.semibold))
                        .foregroundStyle(.tint)
                }
                Text(row.title)
                    .font(.headline)
                    .foregroundStyle(.primary)
                if !row.people.isEmpty {
                    Label(row.people, systemImage: "person")
                }
                Label(row.time, systemImage: "clock")
                    .lineLimit(2)
                if !row.badges.isEmpty {
                    Text(row.badges.joined(separator: " · "))
                        .font(.caption.weight(.medium))
                        .padding(.top, 2)
                }
            }
            .font(.subheadline)
            .foregroundStyle(.snippetSecondary)
            .labelStyle(SnippetDetailLabelStyle())
            .accessibilityElement(children: .combine)
        }
    }
}

private struct SnippetDetailLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(alignment: .firstTextBaseline, spacing: 6) {
            configuration.icon
                .font(.caption)
                .frame(width: 16)
            configuration.title
        }
    }
}
