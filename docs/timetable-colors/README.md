# Timetable colors

The Settings screen offers **依課程類別** (default) and **隨機配色**. This preference is shared by Android and iOS and persists in DataStore. The category legend includes only categories present in the draft and scrolls horizontally.

## Categories

Common categories take precedence over the general required/elective flag:

| Course-code prefix | Category | Palette hue |
| --- | --- | --- |
| CC | 核心通識 | Orange |
| GS | 通識選修 | Purple |
| MN | 國防通識 | Teal |
| CL0 | 共同國文 | Indigo |
| LN1 | 共同英文 | Pink |
| HI0 | 共同歷史 | Mint |
| PE | 體育 | Red |
| SC | 服務學習 | Cyan |
| RV | 預留時段 | Yellow |
| Other + required | 必修 | Blue |
| Other + elective | 選修 | Green |
| Other + unknown | 其他 | Neutral |

Prefix matching requires a following numeric character. CL and HI departmental courses and other LN courses do not automatically become common required courses. CC01–CC04 are search subject areas absent from CourseSummary; they are grouped under CC rather than inferred from titles. These are visual groupings, not graduation-credit rules or curriculum eligibility.

Sources: the supplied course-search screenshot, [NCU course map](https://course.ncu.edu.tw/p/412-1014-82.php?Lang=zh-tw), and [NCU course-selection instructions confirming CC/GS prefixes](https://course.ncu.edu.tw/static/file/14/1014/img/371/crs_info.pdf).

## Platform palettes

The iOS palette resolves UIColor system hues and background against the host view's traits. A zero-size accessibility-hidden UIKit observer refreshes the Compose palette on trait changes, including Increase Contrast; the selected app light/dark mode overrides only interface style. The block background remains softly tinted, while the resolved system hue supplies a leading marker and initial text color. Text shifts toward black or white only as much as needed to meet the WCAG sRGB contrast ratio of 4.5:1.

Android uses eleven Google Calendar event swatches in the stable timetable slot order: Blueberry, Basil, Tangerine, Grape, Graphite, Lavender, Flamingo, Sage, Tomato, Peacock, and Banana. The Teal slot uses Graphite for the MN category’s neutral mapping. Light mode uses the deeper member of each current Google Calendar Android event-color pair with white text; dark mode uses its lighter member with black text. Google’s [Apps Script EventColor enum](https://developers.google.com/apps-script/reference/calendar/event-color) names the standard event hues. The [Calendar Colors API](https://developers.google.com/workspace/calendar/api/v3/reference/colors) exposes one event background and foreground, not theme pairs; the exact darker/lighter pairs here were read from the installed Google Calendar Android app (2026.38.0-989268529), not presented as a public API guarantee. The requested white/black foregrounds are fixed to preserve those fills: white-on-deep-fill contrast ranges from 1.69:1 to 7.04:1, and black-on-light-fill contrast ranges from 4.29:1 to 16.06:1, so some combinations fall below 4.5:1.

The shared timetable draws a continuous grid behind its cells, uses tighter 1 dp block insets, and places course titles at the top-left of each block.

Callers can supply `colorMode` and a nonempty `List<TimetableBlockColors>` to `CoursePlanningTimetableSuccessView` to override the defaults.

## Shuffle allocation and tracking

A semester-seeded permutation maps eleven slots to palette hues. The saved per-course map is keyed by serial number within the semester and survives UI state restoration. Repeated meeting blocks use the same assignment. Existing assignments, including removed courses, are retained while editing. Colors are not persisted with the course plan; a fresh application session rebuilds assignments from the current plan.

When assigning a new course, count live courses in each color slot and choose a least-used slot. Historical usage breaks ties, followed by permutation position. More than eleven courses reuse hues. Restoring a removed course retains its old color even if another course has since reused it, so existing colors are never changed solely to rebalance a plan. This can leave uneven counts after removals or restorations; subsequent new courses choose underused colors.

`timetableColorDistribution` counts active course identities per resolved palette hue, excluding removed courses and repeated meetings. The regression suite checks classification, stable assignments, allocation after removals, and distributions for 1, 6, 11, 22, and 33 courses across 1,000 synthetic semester seeds each.

Measured Kotlin test output is saved in [shuffle-distribution.csv](shuffle-distribution.csv). All initial plans have per-color counts differing by at most one. Eleven-course plans use each hue once; 22- and 33-course plans use each hue twice and three times respectively. Across 1,000 six-course plans, aggregate hue frequencies range from 522 to 576 (ideal: 545.45). These are synthetic algorithm measurements, not student data or analytics telemetry.

## Screenshot references

These captures show the populated timetable on an iPhone 15 simulator running iOS 27, in shuffled mode. LN0025-A and LN0025-B are separate course sections with the same title, so they have different course colors. Weekday and period labels remain readable, and periods C/D fit above the native tab bar.

| Light | Dark |
| --- | --- |
| ![iOS light timetable](screenshots/ios-light.png) | ![iOS dark timetable](screenshots/ios-dark.png) |

The Android captures show the same LN0025-A/B sections in a draft on a Pixel 9 emulator running Android 17 (API 37), using shuffled colors. They show Google Calendar's Banana and Sage fills with white course text in light mode and black course text in dark mode. Weekday/period labels and system bar icons follow the selected appearance; all periods through D fit above the navigation bar.

| Light | Dark |
| --- | --- |
| ![Android light timetable](screenshots/android-light.png) | ![Android dark timetable](screenshots/android-dark.png) |

The supplied Apple Calendar and Google Calendar images informed the block and grid styling. Runtime screenshots cover both iOS and Android in light and dark appearances.
