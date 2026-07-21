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
        TabView {
            Tab("選課", systemImage: "calendar") {
                NavigationStack {
                    CourseSelectionView(sharedHost: sharedHost)
                }
            }
            Tab("設定", systemImage: "gear") {
                NavigationStack {
                    CourseSelectionView(sharedHost: sharedHost)
                }
            }
        }
    }
}
