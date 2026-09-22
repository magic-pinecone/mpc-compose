import Shared
import SwiftUI

struct PortalView: View {
    let sharedHost: IosSharedHost

    var body: some View {
        PortalComposeView(sharedHost: sharedHost)
            .navigationTitle("Portal")
    }
}

private struct PortalComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost

    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.portalScreenController()
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}
