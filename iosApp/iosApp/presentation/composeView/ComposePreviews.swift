import Shared
import SwiftUI
import UIKit

private func composePreview(
    _ scenario: ComposePreviewScenario,
    isDarkTheme: Bool = false
) -> UIViewController {
    IosComposePreviewHost.shared.createController(
        scenario: scenario,
        isDarkTheme: isDarkTheme
    )
}

#Preview("Catalog results · populated") {
    composePreview(.catalogPopulated)
}

#Preview("Catalog results · empty") {
    composePreview(.catalogEmpty)
}

#Preview("Catalog results · loading") {
    composePreview(.catalogLoading)
}

#Preview("Catalog results · failure") {
    composePreview(.catalogFailure)
}

#Preview("Timetable · populated") {
    composePreview(.timetablePopulated)
}

#Preview("Timetable · empty") {
    composePreview(.timetableEmpty)
}

#Preview("Timetable · loading") {
    composePreview(.timetableLoading)
}

#Preview("Timetable · failure") {
    composePreview(.timetableFailure)
}

#Preview("Course details") {
    composePreview(.courseDetails)
}

#Preview("Portal") {
    composePreview(.portal)
}

#Preview("Settings") {
    composePreview(.settings)
}

#Preview("Catalog results · populated · dark") {
    composePreview(.catalogPopulated, isDarkTheme: true)
}
