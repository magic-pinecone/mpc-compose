import Shared
import SwiftUI

struct ContentView: View {
    let sharedHost: IosSharedHost
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var themeMode = AppThemeMode.system

    var body: some View {
        TabView {
            Tab("選課", systemImage: "calendar") {
                NavigationStack {
                    CoursePlanningView(sharedHost: sharedHost)
                }
                .ignoresSafeArea(.container, edges: bottomSafeAreaEdges)
            }
            Tab("Portal", systemImage: "building.columns") {
                NavigationStack {
                    PortalView(sharedHost: sharedHost)
                }
                .ignoresSafeArea(.container, edges: bottomSafeAreaEdges)
            }
            Tab("設定", systemImage: "gear") {
                NavigationStack {
                    SettingsView(sharedHost: sharedHost)
                }
                .ignoresSafeArea(.container, edges: bottomSafeAreaEdges)
            }
        }
        .ignoresSafeArea(.container, edges: bottomSafeAreaEdges)
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

    // Floating tab bars overlay content on iOS 26 and later.
    private var bottomSafeAreaEdges: Edge.Set {
        if #available(iOS 26, *), horizontalSizeClass == .compact {
            return .bottom
        }
        return []
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
