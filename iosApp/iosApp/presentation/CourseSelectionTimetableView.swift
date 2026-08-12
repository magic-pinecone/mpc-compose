import Shared
import SwiftUI

struct CourseSelectionTimetableView: View {
    let sharedHost: IosSharedHost
    let planBridge: CoursePlanBridge

    var body: some View {
        CourseSelectionTimetableComposeView(
            sharedHost: sharedHost,
            planBridge: planBridge
        )
    }
}
