import Shared
import SwiftUI

struct HomeView<CourseDestination: View>: View {
    let loadCourses: () async throws -> EeclassCourses
    @ViewBuilder let courseDestination: (EeclassCourseLink) -> CourseDestination
    let onOpenSettings: () -> Void

    var body: some View {
        EeclassCourseList(loadCourses: loadCourses, courseDestination: courseDestination)
            .navigationTitle("首頁")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("設定", systemImage: "gear", action: onOpenSettings)
                        .labelStyle(.iconOnly)
                }
            }
    }
}
