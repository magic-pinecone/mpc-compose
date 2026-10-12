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
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    private let semester = "115-1"

    var body: some View {
        GeometryReader { geometry in
            // Size classes support Duo's inner display; width also supports existing landscape layouts.
            let isWide = horizontalSizeClass == .regular || geometry.size.width >= 600
            let layout = CoursePlanningPaneLayout(
                isExpanded: isWide,
                division: geometry.coursePlanningDivision
            )

            VStack(spacing: isWide ? 0 : 12) {
                if !isWide {
                    Picker("選課檢視", selection: $activeSection) {
                        Text("搜尋").tag(Section.catalog)
                        Text("課表").tag(Section.timetable)
                    }
                    .pickerStyle(.segmented)
                    .accessibilityIdentifier("course-planning-section")
                    .padding(.horizontal)
                    .padding(.top, 8)
                }

                // Retain both hosts while their frames adapt to the available regions.
                layout {
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
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .clipped()
                    .opacity(isWide || activeSection == .catalog ? 1 : 0)
                    .disabled(!isWide && activeSection != .catalog)
                    .allowsHitTesting(isWide || activeSection == .catalog)
                    .accessibilityHidden(!isWide && activeSection != .catalog)

                    Color(uiColor: .separator)
                        .allowsHitTesting(false)
                        .accessibilityHidden(true)

                    CoursePlanningTimetableView(
                        sharedHost: sharedHost,
                        planBridge: planBridge
                    )
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .clipped()
                    .opacity(isWide || activeSection == .timetable ? 1 : 0)
                    .allowsHitTesting(isWide || activeSection == .timetable)
                    .accessibilityHidden(!isWide && activeSection != .timetable)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            .navigationTitle(isWide ? "選課" : (activeSection == .catalog ? "課程搜尋" : "我的課表"))
            .toolbar {
                if isWide || activeSection == .catalog {
                    ToolbarItem(placement: .topBarTrailing) {
                        Menu {
                            Toggle(
                                "精簡課程卡片", systemImage: "rectangle.compress.vertical",
                                isOn: $isCompactCatalog
                            )
                        } label: {
                            Label("顯示選項", systemImage: "slider.horizontal.3")
                        }
                        .accessibilityIdentifier("course-display-options")
                    }
                }
            }
        }
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
                Button("儲存", systemImage: "square.and.arrow.down") {
                    planBridge.requestSave()
                }
            }

            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    isShowingSelectedCourses = true
                } label: {
                    Label("已選課程", systemImage: "cart")
                }
                .badge(selectedCourses.count)
                .accessibilityLabel("已選課程")
                .accessibilityValue("\(selectedCourses.count) 門課")
                .accessibilityHint("顯示目前已加入的課程")
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
