import Shared
import SwiftUI

struct CourseSelectionTimetableView: View {
    let sharedHost: IosSharedHost
    
    var body: some View {
        CourseSelectionTimetableComposeView(sharedHost: sharedHost)
    }
}

