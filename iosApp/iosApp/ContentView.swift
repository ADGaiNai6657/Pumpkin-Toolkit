import UIKit
import SwiftUI
import WidgetKit
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("com.pgigi.pumpkintoolkit.widgetDataChanged"))) { _ in
                WidgetCenter.shared.reloadAllTimelines()
            }
    }
}