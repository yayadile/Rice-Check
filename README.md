# RiceCheck

RiceCheck is an Android app for checking rice quality from a photo **plus** the price per
kilogram. It does not try to detect "fake" rice directly from the image. Instead, it compares
the detected quality against the price and reports whether the rice is plausibly genuine or
potentially adulterated.

Analysis runs fully on-device and offline. There is no login, sync, or network permission.

## How it works

1. Take a photo of rice (or pick one from the gallery) and optionally enter the price per kg.
2. The photo is analyzed on-device into a `QualityReport`: quality score (0-100), whole grain %,
   broken grain %, and a bleach indication.
3. `VerdictCalculator` combines price and quality into one of three states:

| Price | Quality | Result |
|---|---|---|
| `>= Rp14.000/kg` | score `< 60` | `BERPOTENSI_OPLOSAN` |
| `>= Rp14.000/kg` | score `>= 60` | `ASLI` |
| `< Rp14.000/kg` | any | `LAPORAN_KUALITAS` (no verdict) |
| not filled | any | `LAPORAN_KUALITAS` (no verdict) |

The result is saved to history together with the photo, price, and metrics.

> The ML models are not wired into the app yet. `RiceClassifier` currently returns a stub
> `QualityReport`, and the result screen shows a clear "model not available" banner. Never treat
> the displayed stub numbers as a real analysis. See [models/README.md](models/README.md).

## Features (v1)

- Onboarding (3 swipeable cards, skippable)
- Home with weekly scan count and recent scans
- Camera scan with frame guide, gallery picker, and price input
- Analyzing screen with progress and cancel
- Result screen with 3 states, quality breakdown, and disclaimer
- History with filters, detail view, and delete confirmation
- Local persistence with Room; photos stored in app-internal storage

## Tech stack

| Component | Version |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3 |
| Architecture | MVVM + Repository, manual DI |
| Camera | CameraX 1.6.2 |
| Local DB | Room 2.8.5 (runtime + KSP, no `room-ktx`) |
| Preferences | DataStore Preferences 1.3.0 |
| ML runtime | LiteRT 2.2.0 |
| Build | AGP 9.1.1, Gradle 9.3.1 |
| SDK | compileSdk/targetSdk 37, minSdk 26 |
| Java | JDK 17 |

## Requirements

- Android Studio (or Android SDK command-line tools)
- JDK 17+
- Android SDK Platform 37 and Build-tools 37.0.0
- No emulator or physical device is required to build, run unit tests, or inspect UI previews

## Run the app

Clone the repo and let Gradle use JDK 17. If `gradle.properties` contains a machine-specific
`org.gradle.java.home` (the committed file points to a Windows path), override it for your
machine via `JAVA_HOME` or your user-level `gradle.properties`.

```bash
# build a debug APK
./gradlew assembleDebug

# output
# app/build/outputs/apk/debug/app-debug.apk
```

To install on a connected device or emulator:

```bash
./gradlew installDebug
adb shell am start -n com.yayadile.ricecheck/.MainActivity
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

### UI previews

Every screen is split into a stateful `XxxScreen` and a stateless `XxxContent`. All `@Preview`
functions render the stateless content, so you can verify the UI in Android Studio without a
device. Open a file under `app/src/main/kotlin/com/yayadile/ricecheck/ui/screens/` or
`ui/components/` and use the Preview panel. Preview data is marked with a `PREVIEW DATA` chip.

## Test

```bash
./gradlew testDebugUnitTest
```

Unit tests live in `app/src/test/kotlin/com/yayadile/ricecheck/`. They currently cover the
`VerdictCalculator` logic (10 tests).

To run everything used for a baseline check:

```bash
./gradlew assembleDebug testDebugUnitTest
```

## Project structure

```
app/src/main/kotlin/com/yayadile/ricecheck/
├── MainActivity.kt / RiceCheckApplication.kt
├── di/                  # manual DI (AppContainer, CompositionLocal)
├── domain/              # VerdictCalculator, VerdictConfig, QualityReport
├── data/                # Room, file storage, DataStore
├── ml/                  # RiceClassifier (LiteRT wrapper, currently stub)
├── util/                # image decoding (EXIF + sampling)
├── viewmodel/           # one ViewModel per screen
└── ui/
    ├── theme/           # colors, typography, shapes, dimensions
    ├── navigation/      # routes + NavGraph
    ├── screens/         # onboarding, home, camera, analyzing, result, history, detail
    ├── components/      # reusable UI
    └── preview/         # preview-only fixtures

models/                  # YOLOv11s + SVM artifacts (not used at runtime yet)
docs/                    # product/architecture/design documentation
```

## Models

The trained artifacts are staged under [`models/`](models/README.md):

- `models/yolo11s_beras/` — YOLO11s detector (`best.pt`, `best.onnx`), 5 classes
- `models/bleach_svm/` — RBF SVM bleach classifier (`svm_rbf.pkl`)

They still need conversion and integration before the app can produce real results. The app
expects a LiteRT asset at `app/src/main/assets/ricecheck_v1.tflite`, which does not exist yet.

## Documentation

- [`PROJECT_STATUS.md`](PROJECT_STATUS.md) — current status, decisions, and next steps
- [`docs/features.md`](docs/features.md) — feature scope and verdict rules
- [`docs/IA.md`](docs/IA.md) — screens and navigation
- [`docs/architecture.md`](docs/architecture.md) — architecture and data flow
- [`docs/design-tokens.md`](docs/design-tokens.md) — colors, type, spacing, shape

## Known limitations

- Real ML inference is not implemented; results are a clearly-marked stub.
- The app has not been runtime-tested on a physical device or emulator.
- No instrumentation tests, release signing, or AAB build yet.
- Preview code currently ships in the release build.

## Disclaimer

Results are estimates from image analysis, not laboratory tests.
