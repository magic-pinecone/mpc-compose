import Shared
import SwiftUI

struct CourseSelectionView: View {
    let sharedHost: IosSharedHost

    enum Section {
        case search
        case timetable
    }

    @State var selection: Section = .search

    var body: some View {
        Group {
            switch selection {
            case .search:
                CourseSelectionSearchView(sharedHost: sharedHost)
            case .timetable:
                CourseSelectionTimetableView(sharedHost: sharedHost)
            }
        }
        .navigationTitle(selection == .search ? "課程搜尋": "我的課表")
        .toolbar {
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
