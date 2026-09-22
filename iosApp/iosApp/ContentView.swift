import Shared
import SwiftUI

struct ContentView: View {
    let sharedHost: IosSharedHost
    @State private var themeMode = AppThemeMode.system

    var body: some View {
        TabView {
            Tab("選課", systemImage: "calendar") {
                NavigationStack {
                    CoursePlanningView(sharedHost: sharedHost)
                }
            }
            Tab("Portal", systemImage: "building.columns") {
                NavigationStack {
                    PortalView(sharedHost: sharedHost)
                }
            }
            Tab("設定", systemImage: "gear") {
                NavigationStack {
                    SettingsView(sharedHost: sharedHost)
                }
            }
        }
        .preferredColorScheme(preferredColorScheme)
        .onAppear {
            sharedHost.observeThemeMode(observer: { selectedMode in
                themeMode = selectedMode
            })
        }
        .onDisappear {
            sharedHost.observeThemeMode(observer: nil)
        }
    }

    private var preferredColorScheme: ColorScheme? {
        if themeMode == .light {
            return .light
        }
        if themeMode == .dark {
            return .dark
        }
        return nil
    }
}
