import Shared
import SwiftUI

struct PortalView: View {
    let sharedHost: IosSharedHost
    @State private var webDestination: PortalDestination?

    var body: some View {
        PortalComposeView(sharedHost: sharedHost) { destination in
            webDestination = PortalDestination(title: destination.title, url: destination.url)
        }
            .navigationTitle("Portal")
            .navigationDestination(item: $webDestination) { destination in
                PortalWebComposeView(sharedHost: sharedHost, destination: destination)
                    .navigationTitle(destination.title)
                    .navigationBarTitleDisplayMode(.inline)
            }
    }
}

private struct PortalDestination: Hashable {
    let title: String
    let url: String
}

private struct PortalComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost
    let onOpenDestination: (PortalShortcutDestination) -> Void

    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.portalScreenController(onOpenDestination: onOpenDestination)
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}

private struct PortalWebComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost
    let destination: PortalDestination

    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.portalWebScreenController(url: destination.url)
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}
