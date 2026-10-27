import Shared
import SwiftUI

/// The in-app browser. Sharing and opening externally use the URL it was opened with,
/// because later pages can carry one-time sign-in URLs.
struct WebPageView: View {
    let sharedHost: IosSharedHost
    let title: String
    let url: String

    var body: some View {
        WebPageComposeView(sharedHost: sharedHost, url: url)
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if let pageURL = URL(string: url) {
                    ToolbarItem(placement: .topBarTrailing) {
                        Menu {
                            ShareLink(item: pageURL)
                            Link(destination: pageURL) {
                                Label("在瀏覽器中開啟", systemImage: "safari")
                            }
                        } label: {
                            Label("更多選項", systemImage: "ellipsis")
                        }
                    }
                }
            }
    }
}

private struct WebPageComposeView: UIViewControllerRepresentable {
    let sharedHost: IosSharedHost
    let url: String

    func makeUIViewController(context: Context) -> some UIViewController {
        sharedHost.portalWebScreenController(url: url)
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    }
}
