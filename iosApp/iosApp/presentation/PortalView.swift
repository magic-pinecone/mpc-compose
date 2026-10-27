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
                WebPageView(sharedHost: sharedHost, title: destination.title, url: destination.url)
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
