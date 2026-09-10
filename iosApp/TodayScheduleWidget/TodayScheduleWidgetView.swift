import SwiftUI
import WidgetKit

struct TodayScheduleWidgetView: View {
    let entry: ScheduleEntry
    @Environment(\.widgetFamily) var family
    @Environment(\.colorScheme) var colorScheme

    private var backgroundGradient: LinearGradient {
        LinearGradient(
            colors: colorScheme == .dark
                ? [Color(red: 0.10, green: 0.10, blue: 0.12), Color(red: 0.06, green: 0.06, blue: 0.08)]
                : [Color(red: 0.97, green: 0.97, blue: 0.98), Color(red: 0.92, green: 0.92, blue: 0.95)],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }

    var body: some View {
        contentView
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .modifier(WidgetBackgroundModifier(background: backgroundGradient))
    }

    @ViewBuilder
    private var contentView: some View {
        if !entry.hasData {
            NoDataView()
        } else if entry.isHoliday {
            HolidayView(dayOfWeekText: entry.dayOfWeekText)
        } else {
            switch family {
            case .systemSmall:
                SmallScheduleView(entry: entry)
            case .systemMedium:
                MediumScheduleView(entry: entry)
            default:
                LargeScheduleView(entry: entry)
            }
        }
    }
}

struct WidgetBackgroundModifier: ViewModifier {
    let background: LinearGradient

    func body(content: Content) -> some View {
        content.containerBackground(for: .widget) { background }
    }
}

// MARK: - Header

private struct HeaderView: View {
    let weekNumber: Int
    let dayOfWeekText: String
    let isHoliday: Bool

    var body: some View {
        HStack {
            if !isHoliday && weekNumber > 0 {
                Text("第\(weekNumber)周")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Text(dayOfWeekText)
                .font(.system(size: 13, weight: .medium))
                .foregroundStyle(.secondary)
        }
    }
}

// MARK: - Course Row

private struct CourseRowView: View {
    let course: DisplayCourse

    var body: some View {
        HStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 2) {
                Text(course.name)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(.primary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                HStack(spacing: 5) {
                    Text("\(course.startTime)-\(course.endTime)")
                    if !course.classroom.isEmpty {
                        Text("·")
                        Text(course.classroom)
                    }
                }
                .font(.system(size: 11))
                .foregroundStyle(course.isTomorrow ? Color.orange : .secondary)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
            }
            Spacer(minLength: 8)
            if !course.teacher.isEmpty {
                Text(course.teacher)
                    .font(.system(size: 11))
                    .foregroundStyle(course.isTomorrow ? Color.orange : .secondary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                    .frame(maxWidth: 70, alignment: .trailing)
            }
        }
        .padding(.vertical, 5)
    }
}

private struct SmallCourseRowView: View {
    let course: DisplayCourse

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(course.name)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(.primary)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
            Text("\(course.startTime)-\(course.endTime)")
                .font(.system(size: 11))
                .foregroundStyle(course.isTomorrow ? Color.orange : .secondary)
            if !course.classroom.isEmpty {
                Text(course.classroom)
                    .font(.system(size: 11))
                    .foregroundStyle(course.isTomorrow ? Color.orange : .secondary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, 3)
    }
}

// MARK: - Separators & Hints

private struct TomorrowSeparatorView: View {
    var body: some View {
        HStack(spacing: 8) {
            Rectangle()
                .fill(Color.orange.opacity(0.4))
                .frame(height: 1)
            Text("明日课程")
                .font(.system(size: 10, weight: .medium))
                .foregroundStyle(Color.orange)
            Rectangle()
                .fill(Color.orange.opacity(0.4))
                .frame(height: 1)
        }
        .padding(.vertical, 4)
    }
}

private struct CourseDivider: View {
    var body: some View {
        Divider()
            .opacity(0.25)
    }
}

// MARK: - Empty States

private struct NoDataView: View {
    var body: some View {
        VStack {
            Spacer()
            Text("请打开应用刷新")
                .font(.system(size: 14))
                .foregroundStyle(.tertiary)
            Spacer()
        }
        .frame(maxHeight: .infinity)
    }
}

private struct HolidayView: View {
    let dayOfWeekText: String
    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Spacer()
                Text(dayOfWeekText)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Text("假期中")
                .font(.system(size: 18, weight: .medium))
                .foregroundStyle(.tertiary)
            Spacer()
        }
        .frame(maxHeight: .infinity)
    }
}

// MARK: - Small Widget
// 1 course row + bottom hint (remaining today count or tomorrow info)
// Content is top-aligned.

private struct SmallScheduleView: View {
    let entry: ScheduleEntry

    var body: some View {
        VStack(spacing: 0) {
            HeaderView(weekNumber: entry.weekNumber, dayOfWeekText: entry.dayOfWeekText, isHoliday: entry.isHoliday)
                .padding(.bottom, 6)

            if entry.todayCourses.isEmpty && entry.tomorrowCourses.isEmpty {
                Spacer()
                Text("今日无课")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(.tertiary)
                Spacer()
            } else if entry.todayCourses.isEmpty {
                // Only tomorrow courses: show "明日课程" label + first tomorrow course
                Text("明日课程")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(Color.orange)
                    .padding(.bottom, 4)
                if let first = entry.tomorrowCourses.first {
                    SmallCourseRowView(course: first)
                }
                Spacer(minLength: 0)
            } else {
                // Has today courses: show first today course
                if let first = entry.todayCourses.first {
                    SmallCourseRowView(course: first)
                }
                Spacer(minLength: 0)
                // Bottom hints: today remaining + tomorrow, stacked vertically
                let remainingToday = entry.todayCourses.count - 1
                VStack(alignment: .leading, spacing: 2) {
                    if remainingToday > 0 {
                        Label("今日剩 \(remainingToday) 节", systemImage: "chevron.down.circle.fill")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundStyle(.secondary)
                    }
                    if !entry.tomorrowCourses.isEmpty {
                        Label("明日 \(entry.tomorrowCourses.count) 节", systemImage: "sun.max.fill")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundStyle(Color.orange)
                    }
                }
                .padding(.top, 4)
            }
        }
        .frame(maxHeight: .infinity, alignment: .top)
    }
}

// MARK: - Medium Widget (2 course rows total, top-aligned)
// Rules:
// - 0 today + N tomorrow: "明日课程" separator + up to 2 tomorrow courses
// - 1 today + N tomorrow: 1 today + "明日课程" separator + 1 tomorrow
// - >=2 today + 0 tomorrow: 2 today courses
// - >=2 today + N tomorrow: 2 today courses (tomorrow hint not shown for brevity)

private struct MediumScheduleView: View {
    let entry: ScheduleEntry

    var body: some View {
        VStack(spacing: 0) {
            HeaderView(weekNumber: entry.weekNumber, dayOfWeekText: entry.dayOfWeekText, isHoliday: entry.isHoliday)
                .padding(.bottom, 6)

            if entry.todayCourses.isEmpty && entry.tomorrowCourses.isEmpty {
                Spacer()
                Text("今日无课")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(.tertiary)
                Spacer()
            } else if entry.todayCourses.isEmpty {
                // 0 today: show "明日课程" + up to 2 tomorrow courses
                TomorrowSeparatorView()
                let items = Array(entry.tomorrowCourses.prefix(2))
                ForEach(items) { course in
                    CourseRowView(course: course)
                    if course.id != items.last?.id {
                        CourseDivider()
                    }
                }
            } else if entry.todayCourses.count == 1 && !entry.tomorrowCourses.isEmpty {
                // 1 today + tomorrow: 1 today + separator + 1 tomorrow
                if let course = entry.todayCourses.first {
                    CourseRowView(course: course)
                }
                TomorrowSeparatorView()
                if let course = entry.tomorrowCourses.first {
                    CourseRowView(course: course)
                }
            } else {
                // >=2 today (with or without tomorrow): show 2 today courses
                let items = Array(entry.todayCourses.prefix(2))
                ForEach(items) { course in
                    CourseRowView(course: course)
                    if course.id != items.last?.id {
                        CourseDivider()
                    }
                }
            }
        }
        .frame(maxHeight: .infinity, alignment: .top)
    }
}

// MARK: - Large Widget (6 course rows total, top-aligned)

private struct LargeScheduleView: View {
    let entry: ScheduleEntry

    private var courseAllocation: (today: Int, tomorrow: Int) {
        let todayCount = entry.todayCourses.count
        let tomorrowCount = entry.tomorrowCourses.count
        let maxTotal = 6
        if todayCount == 0 {
            return (0, min(tomorrowCount, maxTotal))
        }
        if tomorrowCount == 0 {
            return (min(todayCount, maxTotal), 0)
        }
        // Both exist: max 6 course rows, prefer today, at least 1 for tomorrow
        let todayShow = min(todayCount, maxTotal - 1)
        let tomorrowShow = min(tomorrowCount, maxTotal - todayShow)
        return (todayShow, tomorrowShow)
    }

    var body: some View {
        VStack(spacing: 0) {
            HeaderView(weekNumber: entry.weekNumber, dayOfWeekText: entry.dayOfWeekText, isHoliday: entry.isHoliday)
                .padding(.bottom, 6)

            if entry.todayCourses.isEmpty && entry.tomorrowCourses.isEmpty {
                Spacer()
                Text("今日无课")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundStyle(.tertiary)
                Spacer()
            } else if entry.todayCourses.isEmpty {
                // 0 today: "明日课程" + up to 6 tomorrow courses
                TomorrowSeparatorView()
                let items = Array(entry.tomorrowCourses.prefix(6))
                ForEach(items) { course in
                    CourseRowView(course: course)
                    if course.id != items.last?.id {
                        CourseDivider()
                    }
                }
            } else {
                let (todayShow, tomorrowShow) = courseAllocation

                if todayShow > 0 {
                    let todayItems = Array(entry.todayCourses.prefix(todayShow))
                    ForEach(todayItems) { course in
                        CourseRowView(course: course)
                        if course.id != todayItems.last?.id {
                            CourseDivider()
                        }
                    }
                }

                if tomorrowShow > 0 {
                    TomorrowSeparatorView()
                    let tomorrowItems = Array(entry.tomorrowCourses.prefix(tomorrowShow))
                    ForEach(tomorrowItems) { course in
                        CourseRowView(course: course)
                        if course.id != tomorrowItems.last?.id {
                            CourseDivider()
                        }
                    }
                }
            }
        }
        .frame(maxHeight: .infinity, alignment: .top)
    }
}
