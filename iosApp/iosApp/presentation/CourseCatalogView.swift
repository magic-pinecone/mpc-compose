import Shared
import SwiftUI

struct CourseCatalogView: View {
    let sharedHost: IosSharedHost
    let planBridge: CoursePlanBridge
    let selectedCourses: [CourseSummary]
    let canEditPlan: Bool
    let isCompact: Bool

    let semester: String
    @Binding var query: String
    let bridge: CourseSearchBridge
    @State private var catalogSnapshot: CourseCatalogSnapshot?

    private var selectedSerialNumbers: Set<String> {
        Set(selectedCourses.map(\.serialNumber))
    }

    var body: some View {
        ZStack(alignment: .topLeading) {
            CourseCatalogComposeView(
                sharedHost: sharedHost,
                bridge: bridge,
                planBridge: planBridge
            )
            .frame(width: 1, height: 1)
            .opacity(0)
            .accessibilityHidden(true)
            .allowsHitTesting(false)

            VStack(spacing: 0) {
                CoursePlanningSearchField(query: $query) { submittedQuery in
                    bridge.submitSearch(
                        request: CourseSearchRequest(semester: semester, query: submittedQuery)
                    )
                }
                .padding(.horizontal, 8)
                .padding(.top, 8)

                CourseCatalogResultsView(
                    snapshot: catalogSnapshot,
                    selectedSerialNumbers: selectedSerialNumbers,
                    canEditPlan: canEditPlan,
                    isCompact: isCompact,
                    onRetry: submitSearch,
                    onCourseDetails: { course in
                        planBridge.requestCourseDetails(semester: semester, course: course)
                    },
                    onToggleCourse: { course in
                        planBridge.requestToggleCourse(course: course)
                    }
                )
            }
        }
        .background(Color(uiColor: .systemGroupedBackground).ignoresSafeArea())
        // TODO: optimize user experience when using bopomofo, and maybe add a debounce
        .onChange(of: query) { _, newValue in
            if newValue.isEmpty {
                submitSearch()
            }
        }
        .onAppear {
            bridge.observeCatalogSnapshot(observer: { snapshot in
                catalogSnapshot = snapshot
            })
        }
        .onDisappear {
            bridge.observeCatalogSnapshot(observer: nil)
        }
    }

    private func submitSearch() {
        bridge.submitSearch(request: CourseSearchRequest(semester: semester, query: query))
    }
}

private struct CourseCatalogResultsView: View {
    let snapshot: CourseCatalogSnapshot?
    let selectedSerialNumbers: Set<String>
    let canEditPlan: Bool
    let isCompact: Bool
    let onRetry: () -> Void
    let onCourseDetails: (CourseSummary) -> Void
    let onToggleCourse: (CourseSummary) -> Void

    var body: some View {
        if let snapshot {
            if snapshot.isLoading {
                CourseCatalogFeedbackView(
                    symbol: nil,
                    title: "正在載入課程",
                    message: nil,
                    actionTitle: nil,
                    action: nil,
                    showsProgress: true
                )
            } else if let failureMessage = snapshot.failureMessage {
                CourseCatalogFeedbackView(
                    symbol: "exclamationmark.triangle",
                    title: "課程載入失敗",
                    message: failureMessage,
                    actionTitle: "重新載入",
                    action: onRetry,
                    showsProgress: false
                )
            } else if snapshot.courses.isEmpty {
                CourseCatalogFeedbackView(
                    symbol: "magnifyingglass",
                    title: "找不到符合的課程",
                    message: "試試其他關鍵字，或清除搜尋條件查看全部課程。",
                    actionTitle: nil,
                    action: nil,
                    showsProgress: false
                )
            } else {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: isCompact ? 8 : 12) {
                        Text("共 \(snapshot.courses.count) 門課程")
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(.secondary)
                            .padding(.horizontal, 2)

                        ForEach(snapshot.courses, id: \.serialNumber) { course in
                            CourseCatalogCard(
                                course: course,
                                isSelected: selectedSerialNumbers.contains(course.serialNumber),
                                canEditPlan: canEditPlan,
                                isCompact: isCompact,
                                onOpenDetails: { onCourseDetails(course) },
                                onToggleSelection: { onToggleCourse(course) }
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                    .padding(.bottom, 24)
                }
            }
        } else {
            CourseCatalogFeedbackView(
                symbol: nil,
                title: "正在載入課程",
                message: nil,
                actionTitle: nil,
                action: nil,
                showsProgress: true
            )
        }
    }
}

private struct CourseCatalogFeedbackView: View {
    let symbol: String?
    let title: String
    let message: String?
    let actionTitle: String?
    let action: (() -> Void)?
    let showsProgress: Bool

    var body: some View {
        VStack(spacing: 12) {
            if showsProgress {
                ProgressView()
                    .controlSize(.large)
                    .accessibilityLabel("正在載入課程")
            } else if let symbol {
                Image(systemName: symbol)
                    .font(.system(size: 30, weight: .regular))
                    .foregroundStyle(.secondary)
                    .accessibilityHidden(true)
            }

            Text(title)
                .font(.title3.weight(.semibold))
                .multilineTextAlignment(.center)

            if let message {
                Text(message)
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
            }

            if let actionTitle, let action {
                Button(actionTitle, action: action)
                    .buttonStyle(.borderedProminent)
                    .padding(.top, 4)
            }
        }
        .padding(28)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
