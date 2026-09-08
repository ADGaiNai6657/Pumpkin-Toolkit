import SwiftUI

@main
struct iOSApp: App {
    
    // 初始化 WidgetReloader，监听数据变化通知并刷新 Widget
    private let widgetReloader = WidgetReloader.shared
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
