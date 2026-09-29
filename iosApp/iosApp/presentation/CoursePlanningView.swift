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
    @State private var isShowingSelectedCourses = false

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
            planBridge.observeSelectedCourses(observer: { courses in
                selectedCourses = courses
            })
        }
        .onDisappear {
            planBridge.observeSelectedCourses(observer: nil)
        }
        .sheet(isPresented: $isShowingSelectedCourses) {
            SelectedCoursesSheet(courses: selectedCourses)
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
                    isShowingSelectedCourses = true
                } label: {
                    Image(systemName: "cart")
                        .overlay(alignment: .topTrailing) {
                            if !selectedCourses.isEmpty {
                                Text(selectedCourses.count > 99 ? "99+" : "\(selectedCourses.count)")
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
                .accessibilityLabel("已選課程")
                .accessibilityValue("\(selectedCourses.count) 門課")
                .accessibilityHint("顯示目前已加入的課程")
            }
        }
    }
}

private struct SelectedCoursesSheet: View {
    @Environment(\.dismiss) private var dismiss

    let courses: [CourseSummary]

    var body: some View {
        NavigationStack {
            Group {
                if courses.isEmpty {
                    ContentUnavailableView(
                        "尚未選擇課程",
                        systemImage: "cart",
                        description: Text("加入課程後，會顯示在這裡。")
                    )
                } else {
                    List(courses.indices, id: \.self) { index in
                        let course = courses[index]

                        VStack(alignment: .leading, spacing: 4) {
                            Text(course.title)
                                .font(.headline)

                            Text("班級 \(course.classNo) · \(course.credit, specifier: "%.1f") 學分")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)

                            if !course.teachers.isEmpty {
                                Text(course.teachers.joined(separator: "、"))
                                    .font(.footnote)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("已選課程")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("完成") {
                        dismiss()
                    }
                }
            }
        }
    }
}
