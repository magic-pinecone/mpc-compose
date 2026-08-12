import Shared
import SwiftUI

struct CourseSelectionView: View {
    let sharedHost: IosSharedHost

    enum Section {
        case search
        case timetable
    }

    @State var selection: Section = .search
    @State private var planBridge = CoursePlanBridge()

    var body: some View {
        Group {
            switch selection {
            case .search:
                CourseSelectionSearchView(
                    sharedHost: sharedHost,
                    planBridge: planBridge
                )
            case .timetable:
                CourseSelectionTimetableView(
                    sharedHost: sharedHost,
                    planBridge: planBridge
                )
            }
        }
        .navigationTitle(selection == .search ? "課程搜尋": "我的課表")
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("儲存") {
                    planBridge.requestSave()
                }
            }

            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    selection = selection == .search
                    ? .timetable
                        : .search
                } label: {
                    Image(
                        systemName: selection == .search
                            ? "calendar"
                            : "magnifyingglass"
                    )
                }
            }
        }

    }
}
