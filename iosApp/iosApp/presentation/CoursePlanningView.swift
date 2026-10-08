import Shared
import SwiftUI
import UIKit

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
    @State private var isShowingSelectedCourses = false
    @State private var courseDetailSelection: CourseDetailSelection?
    @AppStorage("courseCatalogCompactMode") private var isCompactCatalog = false
    @State private var catalogQuery = ""
    @State private var catalogBridge = CourseSearchBridge()
    private let semester = "115-1"

    var body: some View {
        VStack(spacing: 12) {
            Picker("選課檢視", selection: $activeSection) {
                Text("搜尋").tag(Section.catalog)
                Text("課表").tag(Section.timetable)
            }
            .pickerStyle(.segmented)
            .accessibilityIdentifier("course-planning-section")
            .padding(.horizontal)
            .padding(.top, 8)

            // Retain both hosts so section changes preserve their controllers and loaded state.
            ZStack {
                CourseCatalogView(
                    sharedHost: sharedHost,
                    planBridge: planBridge,
                    selectedCourses: selectedCourses,
                    canEditPlan: canEditPlan,
                    isCompact: isCompactCatalog,
                    semester: semester,
                    query: $catalogQuery,
                    bridge: catalogBridge
                )
                .opacity(activeSection == .catalog ? 1 : 0)
                .disabled(activeSection != .catalog)
                .allowsHitTesting(activeSection == .catalog)
                .accessibilityHidden(activeSection != .catalog)

                CoursePlanningTimetableView(
                    sharedHost: sharedHost,
                    planBridge: planBridge
                )
                .opacity(activeSection == .timetable ? 1 : 0)
                .allowsHitTesting(activeSection == .timetable)
                .accessibilityHidden(activeSection != .timetable)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .navigationTitle(activeSection == .catalog ? "課程搜尋" : "我的課表")
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
        .sheet(isPresented: $isShowingSelectedCourses) {
            SelectedCoursesSheet(
                planBridge: planBridge,
                courses: selectedCourses,
                canEditPlan: canEditPlan
            )
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
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
                    isShowingSelectedCourses = true
                } label: {
                    Image(systemName: "cart")
                        .overlay(alignment: .topTrailing) {
                            if !selectedCourses.isEmpty {
                                Text(
                                    selectedCourses.count > 99 ? "99+" : "\(selectedCourses.count)"
                                )
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

            if activeSection == .catalog {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Toggle(
                            "精簡課程卡片", systemImage: "rectangle.compress.vertical",
                            isOn: $isCompactCatalog
                        )
                    } label: {
                        Label("顯示選項", systemImage: "ellipsis")
                    }
                    .accessibilityIdentifier("course-display-options")
                }
            }
        }
    }
}

struct CoursePlanningSearchField: UIViewRepresentable {
    @Binding var query: String
    let onSubmit: (String) -> Void

    func makeUIView(context: Context) -> UISearchBar {
        let searchBar = UISearchBar()
        searchBar.searchBarStyle = .minimal
        searchBar.placeholder = "搜尋課程名稱"
        searchBar.returnKeyType = .search
        searchBar.searchTextField.accessibilityIdentifier = "course-catalog-search"
        searchBar.delegate = context.coordinator
        return searchBar
    }

    func updateUIView(_ searchBar: UISearchBar, context: Context) {
        context.coordinator.parent = self
        searchBar.isUserInteractionEnabled = context.environment.isEnabled
        if !context.environment.isEnabled, searchBar.isFirstResponder {
            searchBar.resignFirstResponder()
        }
        if searchBar.text != query {
            searchBar.text = query
        }
    }

    func sizeThatFits(
        _ proposal: ProposedViewSize, uiView: UISearchBar, context: Context
    ) -> CGSize? {
        guard let width = proposal.width, width.isFinite else { return nil }
        let size = uiView.sizeThatFits(
            CGSize(width: width, height: .greatestFiniteMagnitude)
        )
        return CGSize(width: width, height: size.height)
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }

    final class Coordinator: NSObject, UISearchBarDelegate {
        var parent: CoursePlanningSearchField

        init(parent: CoursePlanningSearchField) {
            self.parent = parent
        }

        func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
            parent.query = searchText
        }

        func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
            let submittedQuery = searchBar.text ?? ""
            parent.query = submittedQuery
            searchBar.resignFirstResponder()
            parent.onSubmit(submittedQuery)
        }
    }
}
