import Shared
import SwiftUI
import UIKit

/// Which announcement the course screen shows in a sheet.
struct EeclassBulletinSelection: Identifiable {
    let course: String
    let bulletin: EeclassBulletinRow

    var id: String { bulletin.number }
}

/// A link in an announcement, material, or homework body that opens in the in-app browser.
struct EeclassBodyLink: Hashable {
    let url: String
}

/// One announcement: its title, ee-class's summary line, the body as rich text, and its attachments.
struct EeclassBulletinSheet: View {
    let sharedHost: IosSharedHost
    let selection: EeclassBulletinSelection

    @State private var page: EeclassBulletinPage?
    @State private var reloadID = 0
    @State private var path = NavigationPath()
    @State private var opener: EeclassFileOpener
    @Environment(\.dismiss) private var dismiss

    init(sharedHost: IosSharedHost, selection: EeclassBulletinSelection) {
        self.sharedHost = sharedHost
        self.selection = selection
        _opener = State(initialValue: EeclassFileOpener(sharedHost: sharedHost))
    }

    var body: some View {
        NavigationStack(path: $path) {
            content
                .navigationTitle("公告")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("關閉", systemImage: "xmark") { dismiss() }
                            .labelStyle(.iconOnly)
                    }
                }
                .navigationDestination(for: EeclassBodyLink.self) { link in
                    WebPageView(sharedHost: sharedHost, title: selection.bulletin.title, url: link.url)
                }
        }
        .eeclassFilePreview(opener)
        .task(id: reloadID) { await load() }
    }

    @ViewBuilder
    private var content: some View {
        if let page {
            switch page.status {
            case .loaded:
                List {
                    Section {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(selection.bulletin.title)
                                .font(.title3.weight(.semibold))
                                .accessibilityAddTraits(.isHeader)
                            if !page.info.isEmpty {
                                Text(page.info)
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        EeclassHtmlText(sharedHost: sharedHost, opener: opener, html: page.bodyHtml) { path.append($0) }
                    }
                    EeclassAttachmentsSection(attachments: page.attachments, opener: opener)
                }
            case .needsPortal:
                EeclassUnavailableView(
                    title: "需要登入 Portal",
                    systemImage: "person.crop.circle.badge.exclamationmark",
                    description: "在 Portal 分頁登入後再重新整理。"
                ) { reloadID += 1 }
            case .unavailable:
                EeclassUnavailableView(
                    title: "無法載入公告",
                    systemImage: "wifi.exclamationmark",
                    description: "請稍後再試。"
                ) { reloadID += 1 }
            }
        } else {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }

    private func load() async {
        guard let result = try? await sharedHost.loadEeclassBulletin(
            course: selection.course,
            bulletin: selection.bulletin.number
        ) else { return }
        page = result
    }
}

/// A teacher-written HTML fragment as selectable rich text. A link to ee-class goes to `onOpenLink`, so it stays in
/// the in-app browser with the ee-class session, except a file link, which opens in QuickLook. Other links open in
/// the system.
struct EeclassHtmlText: View {
    let sharedHost: IosSharedHost
    let opener: EeclassFileOpener
    let html: String
    let onOpenLink: (EeclassBodyLink) -> Void

    @State private var text: AttributedString?

    var body: some View {
        Group {
            if let text {
                Text(text)
                    .textSelection(.enabled)
                    .environment(\.openURL, OpenURLAction { url in
                        if opener.openLink(url) { return .handled }
                        guard let entry = sharedHost.campusLinkUrl(url: url.absoluteString) else {
                            return .systemAction
                        }
                        onOpenLink(EeclassBodyLink(url: entry))
                        return .handled
                    })
            }
        }
        .task(id: html) { text = EeclassRichText.attributed(html: html) }
    }
}

/// Turns a teacher-written HTML fragment into native rich text: system body font, the app's text colors, working links.
enum EeclassRichText {
    @MainActor
    static func attributed(html: String) -> AttributedString {
        let styled = "<style>body { font: -apple-system-body; }</style>" + html
        guard
            let data = styled.data(using: .utf8),
            let imported = try? NSMutableAttributedString(
                data: data,
                options: [
                    .documentType: NSAttributedString.DocumentType.html,
                    .characterEncoding: String.Encoding.utf8.rawValue
                ],
                documentAttributes: nil
            )
        else {
            return AttributedString(html)
        }
        // The HTML's fixed colors would ignore dark mode; drop them so Text uses the primary color.
        imported.removeAttribute(.foregroundColor, range: NSRange(location: 0, length: imported.length))
        let trimmed = imported.attributedSubstring(from: trailingNewlinesTrimmed(imported.string))
        var result = (try? AttributedString(trimmed, including: \.uiKit)) ?? AttributedString(trimmed.string)
        // SwiftUI Text reads its own font attribute, not UIKit's, so carry bold and italic across.
        for run in result.runs {
            if let font = run.uiKit.font {
                result[run.range].font = Font(font as CTFont)
            }
        }
        return result
    }

    private static func trailingNewlinesTrimmed(_ string: String) -> NSRange {
        let utf16 = Array(string.utf16)
        var end = utf16.count
        while end > 0, utf16[end - 1] == 0x0A { end -= 1 }
        return NSRange(location: 0, length: end)
    }
}
