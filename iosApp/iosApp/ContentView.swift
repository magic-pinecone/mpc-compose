import Shared
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    let sharedHost: IosSharedHost
    
    var body: some View {
        NavigationStack {
            CourseSelectionSearchView(sharedHost: sharedHost)
                .ignoresSafeArea(
                    .container,
                    edges: Edge.Set.bottom
                )
        }
    }
}
