import Shared
import SwiftUI

/// The native ee-class entry from Portal: the enrolled courses, with the ee-class website one tap away.
struct EeclassRootView: View {
    let sharedHost: IosSharedHost
    let destination: PortalDestination

    @State private var isShowingWebsite = false

    var body: some View {
        EeclassCourseList(
            loadCourses: { try await sharedHost.loadEeclassCourses() },
            courseDestination: { course in
                EeclassCourseView(sharedHost: sharedHost, course: course)
            }
        )
        .navigationTitle(destination.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("在網頁開啟") { isShowingWebsite = true }
            }
        }
        .navigationDestination(isPresented: $isShowingWebsite) {
            WebPageView(sharedHost: sharedHost, title: destination.title, url: destination.url)
        }
    }
}
