import Shared
import SwiftUI

struct ContentView: View {
    let sharedHost: IosSharedHost
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var themeMode = AppThemeMode.system
    @State private var isShowingSettings = false

    var body: some View {
        TabView {
            Tab("首頁", systemImage: "house") {
                NavigationStack {
                    HomeView(onOpenSettings: openSettings)
                }
                .ignoresSafeArea(.container, edges: bottomSafeAreaEdges)
            }
            Tab("選課", systemImage: "calendar") {
                NavigationStack {
                    CoursePlanningView(
                        sharedHost: sharedHost,
                        onOpenSettings: openSettings
                    )
                }
                .ignoresSafeArea(.container, edges: bottomSafeAreaEdges)
            }
            Tab("Portal", systemImage: "building.columns") {
                NavigationStack {
                    PortalView(
                        sharedHost: sharedHost,
                        onOpenSettings: openSettings
                    )
                }
                .ignoresSafeArea(.container, edges: bottomSafeAreaEdges)
            }
        }
        .sheet(isPresented: $isShowingSettings) {
            NavigationStack {
                SettingsView(sharedHost: sharedHost)
            }
            .presentationDetents([.large])
            .presentationDragIndicator(.visible)
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

    private func openSettings() {
        isShowingSettings = true
    }
}
