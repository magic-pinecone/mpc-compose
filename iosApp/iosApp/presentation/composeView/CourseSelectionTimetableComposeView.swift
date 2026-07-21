import Shared
import SwiftUI

struct CourseSelectionTimetableComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost
    
    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.courseSelectionTimetableScreenController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}
