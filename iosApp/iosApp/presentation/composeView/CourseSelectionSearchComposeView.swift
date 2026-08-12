import SwiftUI
import Shared

struct CourseSelectionSearchComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost
    let bridge: CourseSearchBridge
    let planBridge: CoursePlanBridge

    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.courseSearchScreenController(
            bridge: bridge,
            planBridge: planBridge
        )
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}
