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
    @State private var selectedCourseCount = 0

    var body: some View {
        VStack(spacing: 12) {
            Picker("選課檢視", selection: $activeSection) {
                Text("搜尋").tag(Section.catalog)
                Text("課表").tag(Section.timetable)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)
            .padding(.top, 8)

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
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .navigationTitle(activeSection == .catalog ? "課程搜尋" : "我的課表")
        .onAppear {
            planBridge.observeSelectedCourseCount(observer: { count in
                selectedCourseCount = Int(truncating: count)
            })
        }
        .onDisappear {
            planBridge.observeSelectedCourseCount(observer: nil)
        }
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("儲存") {
                    planBridge.requestSave()
                }
            }

            ToolbarItem(placement: .topBarTrailing) {
                if activeSection == .catalog {
                    Button {
                        activeSection = .timetable
                    } label: {
                        Image(systemName: "cart")
                            .overlay(alignment: .topTrailing) {
                                if selectedCourseCount > 0 {
                                    Text(selectedCourseCount > 99 ? "99+" : "\(selectedCourseCount)")
                                        .font(.system(size: 10, weight: .bold, design: .rounded))
                                        .foregroundStyle(.white)
                                        .padding(.horizontal, 4)
                                        .frame(minWidth: 16, minHeight: 16)
                                        .background(.red, in: Capsule())
                                        .offset(x: 9, y: -8)
                                        .accessibilityHidden(true)
                                }
                            }
                    }
                    .accessibilityLabel("查看課表")
                    .accessibilityValue("\(selectedCourseCount) 門課")
                    .accessibilityHint("開啟已加入的課程")
                } else {
                    Button {
                        activeSection = .catalog
                    } label: {
                        Image(systemName: "magnifyingglass")
                    }
                    .accessibilityLabel("搜尋課程")
                }
            }
        }
    }
}
