import Shared
import SwiftUI

@main
struct IOSApp: App {
    private let sharedHost: IosSharedHost = IosSharedHostKt.createIosSharedHost()
    var body: some Scene {
        WindowGroup {
            ContentView(sharedHost: sharedHost)
        }
    }
}
