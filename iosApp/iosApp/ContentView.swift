import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {

    let appCompositionRoot: AppCompositionRoot

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(appCompositionRoot: appCompositionRoot)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let appCompositionRoot: AppCompositionRoot

    var body: some View {
        ComposeView(appCompositionRoot: appCompositionRoot)
            .ignoresSafeArea()
    }
}
