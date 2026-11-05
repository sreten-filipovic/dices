import SwiftUI
import shared

@main
struct iosAppApp: App {

    private let appCompositionRoot: AppCompositionRoot

    init() {
        appCompositionRoot = AppCompositionRoot(
            baseUrl: Constants.BASE_URL,
            baseApiUrl: Constants.BASE_URL,
            dataStore: DataStoreFactoryImpl().get(),
            appInfo: IosAppInfo(),
            trustAllCertificates: Self.trustAllCertificates
        )
    }

    private static var trustAllCertificates: Bool {
        #if DEBUG
        return true
        #else
        return false
        #endif
    }

    var body: some Scene {
        WindowGroup {
            ContentView(appCompositionRoot: appCompositionRoot)
        }
    }
}
