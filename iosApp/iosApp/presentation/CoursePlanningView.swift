import Shared
import SwiftUI

struct CoursePlanningView: View {
    let sharedHost: IosSharedHost

    enum Section {
        case catalog
        case timetable
    }

    @State private var activeSection: Section = .catalog
    @State private var planBridge = CoursePlanBridge()
    @State private var selectedCourses: [CourseSummary] = []
    @State private var canEditPlan = false
    @State private var courseDetailSelection: CourseDetailSelection?

    var body: some View {
        Group {
            switch activeSection {
            case .catalog:
                CourseCatalogView(
                    sharedHost: sharedHost,
                    planBridge: planBridge
                )
            case .timetable:
                CoursePlanningTimetableView(
                    sharedHost: sharedHost,
                    planBridge: planBridge
                )
            }
        }
        .navigationTitle(activeSection == .catalog ? "課程搜尋": "我的課表")
        .onAppear {
            planBridge.observeSelectedCourses(observer: { courses in
                selectedCourses = courses
                canEditPlan = planBridge.canEditPlan
            })
            planBridge.observeCourseDetailsRequest(observer: { semester, course in
                courseDetailSelection = CourseDetailSelection(semester: semester, course: course)
            })
        }
        .onDisappear {
            planBridge.observeSelectedCourses(observer: nil)
            planBridge.observeCourseDetailsRequest(observer: nil)
        }
        .sheet(item: $courseDetailSelection) { selection in
            CourseDetailSheet(
                selection: selection,
                sharedHost: sharedHost,
                planBridge: planBridge,
                isSelected: selectedCourses.contains {
                    $0.serialNumber == selection.course.serialNumber
                },
                canEditPlan: canEditPlan
            )
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("儲存") {
                    planBridge.requestSave()
                }
            }

            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    activeSection = activeSection == .catalog
                    ? .timetable
                        : .catalog
                } label: {
                    Image(
                        systemName: activeSection == .catalog
                            ? "calendar"
                            : "magnifyingglass"
                    )
                }
            }
        }

    }
}
