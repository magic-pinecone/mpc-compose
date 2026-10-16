import Shared
import SwiftUI

struct ContentView: View {
    let sharedHost: IosSharedHost
    @State private var themeMode = AppThemeMode.system
    @State private var isShowingSettings = false

    var body: some View {
        TabView {
            Tab("首頁", systemImage: "house") {
                NavigationStack {
                    HomeView(onOpenSettings: openSettings)
                }
            }
            Tab("選課", systemImage: "calendar") {
                NavigationStack {
                    CoursePlanningView(
                        sharedHost: sharedHost,
                        onOpenSettings: openSettings
                    )
                }
            }
            Tab("Portal", systemImage: "building.columns") {
                NavigationStack {
                    PortalView(
                        sharedHost: sharedHost,
                        onOpenSettings: openSettings
                    )
                }
            }
        }
        .sheet(isPresented: $isShowingSettings) {
            NavigationStack {
                SettingsView(sharedHost: sharedHost)
            }
            .presentationDetents([.large])
            .presentationDragIndicator(.visible)
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

    private func openSettings() {
        isShowingSettings = true
    }
}
