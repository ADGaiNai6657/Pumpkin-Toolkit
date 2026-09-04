import WidgetKit
import SwiftUI

@main
struct TodayScheduleWidget: Widget {
    let kind: String = "TodayScheduleWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: TodayScheduleProvider()) { entry in
            TodayScheduleWidgetView(entry: entry)
        }
        .configurationDisplayName("今日课程")
        .description("显示今日课程及明日课程")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}
