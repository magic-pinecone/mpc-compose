import Shared
import SwiftUI

/// One material: its title, who posted it and when, the body as rich text, and its attachments.
struct EeclassMaterialView: View {
    let sharedHost: IosSharedHost
    let link: EeclassMaterialLink

    @State private var page: EeclassMaterialPage?
    @State private var reloadID = 0
    @State private var openedLink: EeclassBodyLink?
    @State private var opener: EeclassFileOpener

    init(sharedHost: IosSharedHost, link: EeclassMaterialLink) {
        self.sharedHost = sharedHost
        self.link = link
        _opener = State(initialValue: EeclassFileOpener(sharedHost: sharedHost))
    }

    var body: some View {
        EeclassDetailContent(
            status: page?.status,
            unavailableTitle: "無法載入教材",
            reloadID: $reloadID
        ) {
            if let page {
                List {
                    Section {
                        EeclassDetailHeader(title: link.title, subtitle: page.info)
                        EeclassHtmlText(sharedHost: sharedHost, opener: opener, html: page.bodyHtml) { openedLink = $0 }
                    }
                    EeclassAttachmentsSection(attachments: page.attachments, opener: opener)
                    EeclassOpenOnWebSection(title: link.title, path: link.path)
                }
                .refreshable { await load() }
            }
        }
        .eeclassFilePreview(opener)
        .navigationTitle("教材")
        .navigationBarTitleDisplayMode(.inline)
        .navigationDestination(item: $openedLink) { opened in
            WebPageView(sharedHost: sharedHost, title: link.title, url: opened.url)
        }
        .task(id: reloadID) { await load() }
    }

    private func load() async {
        guard let result = try? await sharedHost.loadEeclassMaterial(path: link.path) else { return }
        page = result
    }
}

/// The loaded content of an ee-class detail screen, or why it could not be shown, with a way to try again.
struct EeclassDetailContent<Content: View>: View {
    let status: EeclassLoadStatus?
    let unavailableTitle: String
    @Binding var reloadID: Int
    @ViewBuilder let content: () -> Content

    var body: some View {
        switch status {
        case .loaded:
            content()
        case .needsPortal:
            EeclassUnavailableView(
                title: "需要登入 Portal",
                systemImage: "person.crop.circle.badge.exclamationmark",
                description: "在 Portal 分頁登入後再重新整理。",
                onRetry: { reloadID += 1 }
            )
        case .unavailable:
            EeclassUnavailableView(
                title: unavailableTitle,
                systemImage: "wifi.exclamationmark",
                description: "請稍後再試。",
                onRetry: { reloadID += 1 }
            )
        default:
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

struct EeclassDetailHeader: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.title3.weight(.semibold))
                .accessibilityAddTraits(.isHeader)
            if !subtitle.isEmpty {
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
    }
}

/// The way out to the ee-class website page the screen was built from.
struct EeclassOpenOnWebSection: View {
    let title: String
    let path: String

    var body: some View {
        Section {
            NavigationLink(value: EeclassPageLink(title: title, path: path)) {
                Label("在網頁開啟", systemImage: "safari")
            }
        }
    }
}
