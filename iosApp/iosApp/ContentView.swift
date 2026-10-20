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
        .task(id: ObjectIdentifier(sharedHost)) {
            for await selectedMode in sharedHost.themeMode {
                themeMode = selectedMode
            }
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
        switch themeMode {
        case .system:
            return nil
        case .light:
            return .light
        case .dark:
            return .dark
        }
    }

    private func openSettings() {
        isShowingSettings = true
    }
}
