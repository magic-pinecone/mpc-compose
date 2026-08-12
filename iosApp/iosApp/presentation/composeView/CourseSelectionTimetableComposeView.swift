import Shared
import SwiftUI

struct CourseSelectionTimetableComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost
    let planBridge: CoursePlanBridge

    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.courseSelectionTimetableScreenController(
            planBridge: planBridge
        )
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}
