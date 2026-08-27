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
