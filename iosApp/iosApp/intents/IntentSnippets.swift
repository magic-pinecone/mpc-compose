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
