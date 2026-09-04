import Foundation

struct WidgetData: Codable {
    let startDate: String?
    let totalWeek: Int
    let timeList: [WidgetScheduleTime]
    let courses: [WidgetCourseItem]
    let updateTime: Int64
}

struct WidgetScheduleTime: Codable {
    let start: String
    let end: String
}

struct WidgetCourseItem: Codable {
    let name: String
    let classroom: String
    let teacher: String
    let dayOfWeek: Int
    let lessonOfDay: Int
    let duration: Int
    let weeks: [[Int]]
}

struct DisplayCourse: Identifiable {
    let id = UUID()
    let name: String
    let classroom: String
    let teacher: String
    let startTime: String
    let endTime: String
    let isTomorrow: Bool
}
