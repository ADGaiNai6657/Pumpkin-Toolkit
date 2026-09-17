import Foundation
import UIKit

/// 小组件专用的网络数据获取工具
/// 通过云端 KV 存储读取课表数据（同步，阻塞当前线程）
enum NetworkHelper {

    static let baseURL = "https://pumpkin-api.pgigi.com"
    private static let widgetDataFile = "widget_data.json"
    private static let appGroupID = "group.com.pgigi.pumpkintoolkit"

    /// 获取设备 IDFV
    /// UIDevice 在小组件 Extension 中可用（需 import UIKit）
    private static func getIDFV() -> String? {
        return UIDevice.current.identifierForVendor?.uuidString
    }

    /// 获取本地缓存文件路径（App Group共享容器）
    private static func getLocalCachePath() -> URL? {
        guard let containerURL = FileManager.default.containerURL(
            forSecurityApplicationGroupIdentifier: appGroupID
        ) else {
            return nil
        }
        return containerURL.appendingPathComponent(widgetDataFile)
    }

    /// 从本地缓存加载数据
    private static func loadFromLocalCache() -> WidgetData? {
        guard let cachePath = getLocalCachePath() else { return nil }
        
        do {
            let data = try Data(contentsOf: cachePath)
            let widgetData = try JSONDecoder().decode(WidgetData.self, from: data)
            return widgetData
        } catch {
            return nil
        }
    }

    /// 保存数据到本地缓存
    private static func saveToLocalCache(_ data: WidgetData) {
        guard let cachePath = getLocalCachePath() else { return }
        
        do {
            let jsonData = try JSONEncoder().encode(data)
            try jsonData.write(to: cachePath)
        } catch {
            // 保存失败不影响主流程
        }
    }

    /// 从网络获取课表数据（同步阻塞）
    /// POST https://pumpkin-api.pgigi.com/get/{IDFV}
    /// - Returns: 解码后的 WidgetData，失败返回 nil
    static func loadWidgetData() -> WidgetData? {
        // 首先尝试从本地缓存加载
        if let cachedData = loadFromLocalCache() {
            return cachedData
        }
        
        // 如果本地没有缓存，从网络获取
        guard let idfv = getIDFV(), !idfv.isEmpty else { return nil }

        let urlString = "\(baseURL)/get/\(idfv)"
        guard let url = URL(string: urlString) else { return nil }

        // 使用信号量实现同步请求（小组件 Timeline Provider 需要同步返回）
        let semaphore = DispatchSemaphore(value: 0)
        var result: WidgetData?

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.timeoutInterval = 5

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            defer { semaphore.signal() }
            guard error == nil,
                  let data = data,
                  let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                return
            }
            result = try? JSONDecoder().decode(WidgetData.self, from: data)
        }
        task.resume()
        semaphore.wait()

        // 如果网络请求成功，保存到本地缓存
        if let data = result {
            saveToLocalCache(data)
        }

        return result
    }
}