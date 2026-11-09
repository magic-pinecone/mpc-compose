import AppIntents

// Phrases are translated in AppShortcuts.xcstrings; Siri matches them in the device language.
struct MagicPineconeShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: NextClassIntent(),
            phrases: [
                "What's my next class in \(.applicationName)",
                "Next class in \(.applicationName)",
                "Check my next class with \(.applicationName)"
            ],
            shortTitle: "下一堂課",
            systemImageName: "clock"
        )
        AppShortcut(
            intent: ClassesOnDayIntent(),
            phrases: [
                "What classes do I have today in \(.applicationName)",
                "What classes do I have on \(\.$day) in \(.applicationName)",
                "Show today's classes in \(.applicationName)"
            ],
            shortTitle: "今天的課",
            systemImageName: "calendar"
        )
        AppShortcut(
            intent: FindCoursesIntent(),
            phrases: [
                "Find courses with \(.applicationName)",
                "Find \(\.$timeOfDay) courses with \(.applicationName)",
                "Find courses on \(\.$day) with \(.applicationName)"
            ],
            shortTitle: "找課",
            systemImageName: "magnifyingglass"
        )
        AppShortcut(
            intent: CourseDetailsIntent(),
            phrases: [
                "Course details in \(.applicationName)",
                "Tell me about \(\.$course) in \(.applicationName)"
            ],
            shortTitle: "課程資訊",
            systemImageName: "book.closed"
        )
    }
}
