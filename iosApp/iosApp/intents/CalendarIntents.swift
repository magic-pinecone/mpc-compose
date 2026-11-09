import AppIntents
@preconcurrency import Shared

#if SIRI_AI_PREVIEW
@AppIntent(.tool)
#endif
struct AddClassesToCalendarIntent: AppIntent {
    static let title: LocalizedStringResource = "將課表加入行事曆"
    static let description = IntentDescription(
        "把已儲存的本學期課表加入「神奇松果課表」行事曆。再次加入時，會以目前的課表取代這學期的課程。"
    )

    @Dependency private var sharedHost: IosSharedHost

    func perform() async throws -> some ProvidesDialog {
        let count = try await ClassCalendar.exportSavedPlan(
            sharedHost: sharedHost,
            semester: CourseIdentity.currentSemester
        )
        return .result(dialog: IntentDialog(stringLiteral: ClassCalendar.resultMessage(classCount: count)))
    }
}
