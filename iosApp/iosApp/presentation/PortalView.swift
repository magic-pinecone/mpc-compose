import Shared
import SwiftUI

struct PortalView: View {
    let sharedHost: IosSharedHost
    let onOpenSettings: () -> Void
    @State private var webDestination: PortalDestination?

    var body: some View {
        PortalComposeView(sharedHost: sharedHost) { destination in
            webDestination = PortalDestination(title: destination.title, url: destination.url)
        }
            .ignoresSafeArea(.container, edges: .bottom)
            .navigationTitle("Portal")
            .navigationDestination(item: $webDestination) { destination in
                PortalWebComposeView(sharedHost: sharedHost, destination: destination)
                    .navigationTitle(destination.title)
                    .navigationBarTitleDisplayMode(.inline)
            }
            .toolbar {
                if webDestination == nil {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("設定", systemImage: "gear", action: onOpenSettings)
                            .labelStyle(.iconOnly)
                    }
                }
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
