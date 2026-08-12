import Shared
import SwiftUI

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
