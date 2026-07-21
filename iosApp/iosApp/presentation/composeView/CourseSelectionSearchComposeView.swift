import SwiftUI
import Shared

struct CourseSelectionSearchComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost
    let bridge: CourseSearchBridge

    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.courseSearchScreenController(bridge: bridge)
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}
