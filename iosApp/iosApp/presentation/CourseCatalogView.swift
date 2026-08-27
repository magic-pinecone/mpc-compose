import SwiftUI
import Shared

struct CourseCatalogView: View {
    let sharedHost: IosSharedHost
    let planBridge: CoursePlanBridge

    // TODO: make semester configurable
    let semester = "115-1"
    @State private var query: String = ""
    @State private var bridge = CourseSearchBridge()

    var body: some View {
        CourseCatalogComposeView(
            sharedHost: sharedHost,
            bridge: bridge,
            planBridge: planBridge
        )
        .searchable(
            text: $query,
            prompt: "搜尋課程名稱"
        )
        // TODO: optimize user experience when using bopomofo, and maybe add a debounce
        .onSubmit(of: .search) {
            bridge.submitSearch(request: CourseSearchRequest(semester: semester, query: query))
        }
        .ignoresSafeArea(
            .container,
            edges: .bottom
        )
    }
}
