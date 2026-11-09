import Shared
import SwiftUI

/// One homework: its dates, late policy, weight, grading, description, attachments, and whether you handed it in.
struct EeclassHomeworkView: View {
    let sharedHost: IosSharedHost
    let link: EeclassHomeworkLink

    @State private var page: EeclassHomeworkPage?
    @State private var reloadID = 0
    @State private var openedLink: EeclassBodyLink?

    var body: some View {
        EeclassDetailContent(
            status: page?.status,
            unavailableTitle: "無法載入作業",
            reloadID: $reloadID
        ) {
            if let page {
                List {
                    Section {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(link.title)
                                .font(.title3.weight(.semibold))
                                .accessibilityAddTraits(.isHeader)
                            Label(
                                page.report,
                                systemImage: page.submitted ? "checkmark.circle.fill" : "circle.dashed"
                            )
                            .font(.subheadline)
                            .foregroundStyle(page.submitted ? AnyShapeStyle(.green) : AnyShapeStyle(.secondary))
                        }
                    }
                    Section {
                        ForEach(Array(page.fields.enumerated()), id: \.offset) { _, field in
                            LabeledContent(field.label, value: field.value)
                        }
                    }
                    if !page.descriptionHtml.isEmpty {
                        Section("說明") {
                            EeclassHtmlText(sharedHost: sharedHost, html: page.descriptionHtml) { openedLink = $0 }
                        }
                    }
                    EeclassAttachmentsSection(attachments: page.attachments)
                    EeclassOpenOnWebSection(title: link.title, path: link.path)
                }
                .refreshable { await load() }
            }
        }
        .navigationTitle("作業")
        .navigationBarTitleDisplayMode(.inline)
        .navigationDestination(item: $openedLink) { opened in
            WebPageView(sharedHost: sharedHost, title: link.title, url: opened.url)
        }
        .task(id: reloadID) { await load() }
    }

    private func load() async {
        guard let result = try? await sharedHost.loadEeclassHomework(path: link.path) else { return }
        page = result
    }
}
