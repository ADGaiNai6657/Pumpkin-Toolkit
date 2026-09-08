import Foundation
import WidgetKit

/// 监听 Kotlin 端发来的通知，触发 Widget 时间线刷新
@objc final class WidgetReloader: NSObject {
    
    static let shared = WidgetReloader()
    
    private let widgetKind = "TodayScheduleWidget"
    private let notificationName = Notification.Name("com.pgigi.pumpkintoolkit.widgetDataChanged")
    
    private override init() {
        super.init()
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleWidgetDataChanged),
            name: notificationName,
            object: nil
        )
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc private func handleWidgetDataChanged() {
        WidgetCenter.shared.reloadTimelines(ofKind: widgetKind)
    }
}
