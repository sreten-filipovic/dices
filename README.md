# KMP App Template

Starter for Compose Multiplatform (Android + iOS) apps. Extracted from the mtel.ch app;
keeps the architecture, drops the domain. One complete demo flow (list → details) shows
every layer so new screens can be copied from it instead of from an old project.

## Quick start

```bash
./rename.sh com.company.app "My App"      # package, display name, (optional) app id / bundle id
./gradlew :androidApp:assembleDebug       # Android
open iosApp/iosApp.xcodeproj              # iOS – set the Team, run the iosApp scheme
```

Base URL lives in `androidApp/build.gradle.kts` (`buildConfigField BaseUrl`) and in
`iosApp/iosApp/*.xcconfig`. The template points at JSONPlaceholder so the demo works out of the box.

## Architecture

MVVM with unidirectional data flow, a thin Clean-Architecture layering and manual DI.

```
shared/src/commonMain/kotlin/<package>/shared/
├── data/
│   ├── remote/        ApiClient (Ktor), RemoteDataSource, Result, *Api DTOs in response/
│   └── local/         DataStore wrapper (DataStoreRepository) behind LocalDataSource
├── domain/
│   ├── model/         Domain models used by the UI
│   └── use_case/      One class per operation; calls data source, maps DTO -> model
├── di/
│   ├── AppCompositionRoot      app singletons (api client, data sources, popup manager…)
│   └── ScreenCompositionRoot   get<Screen>ViewModel(...) factories
└── ui/
    ├── app/           App() entry, AppScreen / AppScreenNavigator (top-level back stack), AppViewModel (startup)
    ├── splash/        SplashView
    ├── main/          MainScreen, MainScreenNavigator (nested back stack), MainView (NavDisplay)
    │   ├── demo_list/      Demo screen #1 (list, search, popup on error, open URL)
    │   └── demo_details/   Demo screen #2 (argument, refresh, submit + success popup, back)
    └── common/        AppTheme, Colors, Fonts, Validator, popup/, views/ (Toolbar, Button, states…)
```

Platform code: `shared/src/androidMain`, `shared/src/iosMain` (`expect`/`actual` for HTTP engine,
DataStore path, dialog properties, platform name). Entry points: `androidApp/.../MainApplication.kt`
+ `MainActivity.kt` and `iosApp/iosApp/iosAppApp.swift`.

### Screen contract

Every screen has three files in its own package:

| File | Contents |
|------|----------|
| `Xyz.kt` | `object Xyz { sealed class ViewState { Loading / Error / Success } ; data class ViewData(..., listener) }`, `interface XyzViewListener`, optional `interface XyzScreenNavigator` |
| `XyzViewModel.kt` | `class XyzViewModel(navigator, popupManager, stringProvider, useCases…) : ViewModel(), XyzViewListener` exposing `StateFlow<Xyz.ViewState>` |
| `XyzView.kt` | `XyzView(viewModel)` collects state, `XyzScreen(viewState)` is stateless and previewable |

Rules that keep it consistent:

- `ViewState` always carries `ViewData`, so the UI can show stale data while reloading.
- The Composable never calls the ViewModel directly; it calls `viewState.data.listener`.
- ViewModels depend on a narrow navigator interface (`XyzScreenNavigator`), implemented by `MainNavigatorImpl`.
- Network results are `Result<T>`; mapping `*Api` → domain happens in the use case.
- Errors that the user must see go through `PopupManager.show(PopupState.Error(...))`.
- Strings come from Compose resources; in ViewModels use `StringProvider`.

### Adding a screen (checklist)

1. `ui/main/<feature>/Feature.kt`, `FeatureViewModel.kt`, `FeatureView.kt` (copy from `demo_details`).
2. Use case in `ui/main/<feature>/domain/use_case/` (screen-specific) or `domain/use_case/` (shared).
3. DTO in `data/remote/response/`, endpoint method in `RemoteDataSource`.
4. Destination in `MainScreen`, navigation method in `MainScreenNavigator` + `MainNavigatorImpl`.
5. `getFeatureViewModel(...)` in `ScreenCompositionRoot`.
6. `entry<MainScreen.Feature> { ... }` in `MainView`.
7. Fake state in `PreviewHelper` and a `@Preview` in `FeatureView.kt`.

### Adding a flow (e.g. Intro / Login)

Add `AppScreen.Intro`, an `IntroScreen` sealed class with its own `IntroNavigatorImpl : ViewModel()`,
and an `entry<AppScreen.Intro>` in `App.kt`, mirroring how `Main` is wired.

## What is intentionally not included

| Concern | How to add |
|---------|-----------|
| Authentication | Implement `ApiClientAuthExtension` (Ktor `Auth` bearer provider + token storage), swap `NoAuthExtension` in `AppCompositionRoot`. |
| Firebase (analytics, push, remote config, crashlytics) | Add `google-services` / `firebase-*` plugins and `dev.gitlive:firebase-*` deps; expose managers from `AppCompositionRoot`. |
| Localization switching at runtime | Add `LocalizationManager` + `values-xx` folders; on Android recreate the Activity on change. |
| Forced update dialog | Check version on startup in `AppViewModel`, show a dialog over `SplashView`. |
| Bottom navigation | Wrap `NavDisplay` in `MainView` with a `Scaffold` and derive the selected tab from the back stack. |
| Release signing | `signingConfigs` in `androidApp/build.gradle.kts`; never commit passwords. |

## Tooling

- Gradle 9.3, AGP 9.1, Kotlin 2.3, Compose Multiplatform 1.10, Navigation 3, Ktor 3, Coil 3, DataStore.
- Fonts: Open Sans (Apache 2.0) in `composeResources/font`. Replace the `.ttf` files to rebrand; names stay.
- Compose resources class is fixed to `<package>.shared.resources.Res` via `compose.resources.packageOfResClass`.
