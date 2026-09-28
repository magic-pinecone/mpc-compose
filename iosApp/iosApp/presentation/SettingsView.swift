import Shared
import SwiftUI

struct SettingsView: View {
    let sharedHost: IosSharedHost

    var body: some View {
        SettingsComposeView(sharedHost: sharedHost)
            .navigationTitle("設定")
    }
}

private struct SettingsComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost

    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.settingsScreenController()
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}
