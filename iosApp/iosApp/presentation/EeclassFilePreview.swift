import QuickLook
import Shared
import SwiftUI

/// Downloads ee-class attachments and shows each in QuickLook, which carries the system share button.
@MainActor
@Observable
final class EeclassFileOpener {
    var previewURL: URL?
    var failureMessage: String?

    let bridge: EeclassFilesBridge

    init(sharedHost: IosSharedHost) {
        bridge = sharedHost.eeclassFilesBridge()
    }

    func open(_ row: EeclassAttachmentRow, announcingFailure: Bool = false) async {
        guard let file = try? await bridge.fetch(row: row) else {
            if announcingFailure, let status = bridge.current(row: row)?.status {
                failureMessage = status == .needsPortal ? "需要登入 Portal" : "無法下載檔案"
            }
            return
        }
        previewURL = URL(fileURLWithPath: file.path)
    }

    /// Starts opening the file an ee-class body link points to. Returns false when `url` is not such a link.
    func openLink(_ url: URL) -> Bool {
        guard let row = bridge.linkRow(url: url.absoluteString) else { return false }
        Task { await open(row, announcingFailure: true) }
        return true
    }
}

/// One attachment row: tap to open, with the download's progress or its failure shown in place.
struct EeclassAttachmentRowView: View {
    let row: EeclassAttachmentRow
    let opener: EeclassFileOpener

    @State private var transfer: EeclassFileTransfer?

    var body: some View {
        Button {
            Task { await opener.open(row) }
        } label: {
            VStack(alignment: .leading, spacing: 6) {
                Label(row.title, systemImage: "paperclip")
                    .foregroundStyle(.primary)
                status
            }
        }
        .disabled(transfer?.status == .downloading)
        .task {
            for await value in opener.bridge.transfer(row: row) {
                transfer = value
            }
        }
    }

    @ViewBuilder
    private var status: some View {
        switch transfer?.status {
        case .downloading:
            if let fraction = transfer?.fraction?.doubleValue {
                ProgressView(value: fraction)
            } else {
                ProgressView().progressViewStyle(.linear)
            }
        case .failed:
            Label("下載失敗，點一下重試", systemImage: "arrow.clockwise")
                .font(.footnote)
                .foregroundStyle(.red)
        case .needsPortal:
            Label("需要登入 Portal，登入後點一下重試", systemImage: "person.crop.circle.badge.exclamationmark")
                .font(.footnote)
                .foregroundStyle(.secondary)
        case nil:
            EmptyView()
        }
    }
}

/// The attachments of an announcement, material, or homework.
struct EeclassAttachmentsSection: View {
    let attachments: [EeclassAttachmentRow]
    let opener: EeclassFileOpener

    var body: some View {
        if !attachments.isEmpty {
            Section("附件") {
                ForEach(attachments, id: \.key) { attachment in
                    EeclassAttachmentRowView(row: attachment, opener: opener)
                }
            }
        }
    }
}

private struct EeclassFilePreviewModifier: ViewModifier {
    @Bindable var opener: EeclassFileOpener

    func body(content: Content) -> some View {
        content
            .quickLookPreview($opener.previewURL)
            .alert(
                "無法開啟檔案",
                isPresented: Binding(
                    get: { opener.failureMessage != nil },
                    set: { if !$0 { opener.failureMessage = nil } }
                ),
                actions: { Button("好") {} },
                message: { Text(opener.failureMessage ?? "") }
            )
    }
}

extension View {
    /// Presents the files `opener` finishes downloading, and says why a tapped body link's file did not open.
    func eeclassFilePreview(_ opener: EeclassFileOpener) -> some View {
        modifier(EeclassFilePreviewModifier(opener: opener))
    }
}
