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
        Group {
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
        .modifier(WidgetBackgroundModifier(background: backgroundGradient))
    }
}

struct WidgetBackgroundModifier: ViewModifier {
    let background: LinearGradient

    func body(content: Content) -> some View {
        content.containerBackground(for: .widget) { background }
    }
}

private struct HeaderView: View {
    let weekNumber: Int
    let dayOfWeekText: String
    let isHoliday: Bool
    @Environment(\.colorScheme) var colorScheme

    private var color: Color {
        colorScheme == .dark ? Color.white.opacity(0.85) : Color.black.opacity(0.75)
    }

    var body: some View {
        HStack {
            if !isHoliday && weekNumber > 0 {
                Text("第\(weekNumber)周")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(color)
            }
            Spacer()
            Text(dayOfWeekText)
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(color)
        }
    }
}

private struct CourseCardView: View {
    let course: DisplayCourse
    @Environment(\.colorScheme) var colorScheme

    private var textColor: Color {
        colorScheme == .dark ? Color.white.opacity(0.9) : Color.black.opacity(0.85)
    }

    private var accentColor: Color {
        course.isTomorrow
            ? (colorScheme == .dark ? Color(red: 1.0, green: 0.69, blue: 0.42) : Color(red: 0.8, green: 0.29, blue: 0.12))
            : (colorScheme == .dark ? Color.white.opacity(0.5) : Color.black.opacity(0.45))
    }

    private var cardBg: Color {
        if course.isTomorrow {
            return colorScheme == .dark
                ? Color(red: 0.18, green: 0.14, blue: 0.10)
                : Color(red: 1.0, green: 0.96, blue: 0.92)
        } else {
            return colorScheme == .dark
                ? Color(red: 0.16, green: 0.16, blue: 0.19)
                : Color.white
        }
    }

    var body: some View {
        HStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 2) {
                Text(course.name)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(textColor)
                    .lineLimit(2)
                HStack(spacing: 6) {
                    Text("\(course.startTime)-\(course.endTime)")
                    if !course.classroom.isEmpty {
                        Text(course.classroom)
                    }
                }
                .font(.system(size: 11))
                .foregroundColor(accentColor)
            }
            Spacer(minLength: 0)
            if !course.teacher.isEmpty {
                Text(course.teacher)
                    .font(.system(size: 11))
                    .foregroundColor(accentColor)
                    .lineLimit(1)
            }
        }
        .padding(10)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(cardBg)
        )
        .padding(.vertical, 2)
    }
}

private struct SmallCourseCardView: View {
    let course: DisplayCourse
    @Environment(\.colorScheme) var colorScheme

    private var textColor: Color {
        colorScheme == .dark ? Color.white.opacity(0.9) : Color.black.opacity(0.85)
    }

    private var accentColor: Color {
        course.isTomorrow
            ? (colorScheme == .dark ? Color(red: 1.0, green: 0.69, blue: 0.42) : Color(red: 0.8, green: 0.29, blue: 0.12))
            : (colorScheme == .dark ? Color.white.opacity(0.5) : Color.black.opacity(0.45))
    }

    private var cardBg: Color {
        if course.isTomorrow {
            return colorScheme == .dark
                ? Color(red: 0.18, green: 0.14, blue: 0.10)
                : Color(red: 1.0, green: 0.96, blue: 0.92)
        } else {
            return colorScheme == .dark
                ? Color(red: 0.16, green: 0.16, blue: 0.19)
                : Color.white
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(course.name)
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(textColor)
                .lineLimit(1)
            Text("\(course.startTime)-\(course.endTime)")
                .font(.system(size: 11))
                .foregroundColor(accentColor)
            if !course.classroom.isEmpty {
                Text(course.classroom)
                    .font(.system(size: 11))
                    .foregroundColor(accentColor)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(10)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(cardBg)
        )
        .padding(.vertical, 2)
    }
}

private struct TomorrowSeparatorView: View {
    @Environment(\.colorScheme) var colorScheme

    private var color: Color {
        colorScheme == .dark ? Color.white.opacity(0.3) : Color.black.opacity(0.3)
    }

    var body: some View {
        HStack {
            Rectangle().fill(color).frame(height: 0.5)
            Text("明日")
                .font(.system(size: 11))
                .foregroundColor(color)
                .padding(.horizontal, 8)
            Rectangle().fill(color).frame(height: 0.5)
        }
        .padding(.vertical, 4)
    }
}

private struct NoDataView: View {
    @Environment(\.colorScheme) var colorScheme
    var body: some View {
        VStack {
            Spacer()
            Text("请打开应用刷新")
                .font(.system(size: 14))
                .foregroundColor(colorScheme == .dark ? Color.white.opacity(0.4) : Color.black.opacity(0.35))
            Spacer()
        }
    }
}

private struct HolidayView: View {
    let dayOfWeekText: String
    @Environment(\.colorScheme) var colorScheme
    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Spacer()
                Text(dayOfWeekText)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(colorScheme == .dark ? Color.white.opacity(0.6) : Color.black.opacity(0.5))
            }
            Spacer()
            Text("假期中")
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(colorScheme == .dark ? Color.white.opacity(0.5) : Color.black.opacity(0.4))
            Spacer()
        }
    }
}

private struct SmallScheduleView: View {
    let entry: ScheduleEntry
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        VStack(spacing: 4) {
            HeaderView(weekNumber: entry.weekNumber, dayOfWeekText: entry.dayOfWeekText, isHoliday: entry.isHoliday)
            if entry.todayCourses.isEmpty && entry.tomorrowCourses.isEmpty {
                Spacer()
                Text("今日无课")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(colorScheme == .dark ? Color.white.opacity(0.5) : Color.black.opacity(0.4))
                Spacer()
            } else if let first = entry.todayCourses.first {
                SmallCourseCardView(course: first)
                Spacer(minLength: 0)
            } else if let first = entry.tomorrowCourses.first {
                SmallCourseCardView(course: first)
                Spacer(minLength: 0)
            } else {
                Spacer()
            }
        }
    }
}

private struct MediumScheduleView: View {
    let entry: ScheduleEntry

    var body: some View {
        VStack(spacing: 4) {
            HeaderView(weekNumber: entry.weekNumber, dayOfWeekText: entry.dayOfWeekText, isHoliday: entry.isHoliday)
            VStack(spacing: 2) {
                if entry.todayCourses.isEmpty && entry.tomorrowCourses.isEmpty {
                    Text("今日无课")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(.secondary)
                        .padding(.vertical, 8)
                } else {
                    ForEach(Array(entry.todayCourses.prefix(3))) { course in
                        CourseCardView(course: course)
                    }
                    if !entry.tomorrowCourses.isEmpty {
                        TomorrowSeparatorView()
                        ForEach(Array(entry.tomorrowCourses.prefix(2))) { course in
                            CourseCardView(course: course)
                        }
                    }
                }
            }
        }
    }
}

private struct LargeScheduleView: View {
    let entry: ScheduleEntry

    var body: some View {
        VStack(spacing: 4) {
            HeaderView(weekNumber: entry.weekNumber, dayOfWeekText: entry.dayOfWeekText, isHoliday: entry.isHoliday)
            VStack(spacing: 2) {
                if entry.todayCourses.isEmpty && entry.tomorrowCourses.isEmpty {
                    Text("今日无课")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.secondary)
                        .padding(.vertical, 12)
                } else {
                    ForEach(Array(entry.todayCourses.prefix(6))) { course in
                        CourseCardView(course: course)
                    }
                    if !entry.tomorrowCourses.isEmpty {
                        TomorrowSeparatorView()
                        ForEach(Array(entry.tomorrowCourses.prefix(4))) { course in
                            CourseCardView(course: course)
                        }
                    }
                }
            }
            Spacer(minLength: 0)
        }
    }
}
