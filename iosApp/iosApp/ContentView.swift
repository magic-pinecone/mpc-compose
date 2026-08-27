import Shared
import SwiftUI

struct ContentView: View {
    let sharedHost: IosSharedHost

    var body: some View {
        TabView {
            Tab("選課", systemImage: "calendar") {
                NavigationStack {
                    CoursePlanningView(sharedHost: sharedHost)
                }
            }
            Tab("設定", systemImage: "gear") {
                NavigationStack {
                    CoursePlanningView(sharedHost: sharedHost)
                }
            }
        }
    }
}
