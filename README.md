# RiceCheck

RiceCheck is an Android app for checking rice quality from a photo **plus** the price per
kilogram. It does not try to detect "fake" rice directly from the image. Instead, it compares
the detected quality against the price and reports whether the rice is plausibly genuine or
potentially adulterated.

Analysis runs fully on-device and offline. There is no login, sync, or network permission.

## Stats

![Alt](https://repobeats.axiom.co/api/embed/5c0a17ef1bc0cab8842f665bf35b9bc2ac774658.svg "Statistics")

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

Analysis is fully on-device: YOLO11s (LiteRT) detects and counts grains, then a ported linear SVM
decides the bleach level. If a model is missing or no grain is detected the scan **fails with a
clear message** — the app never fabricates numbers. See [models/README.md](models/README.md).

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

Unit tests live in `app/src/test/kotlin/com/yayadile/ricecheck/`: verdict logic plus golden-vector
tests that pin the Kotlin ML port to the scikit-image/OpenCV/sklearn reference (feature parity
~1e-7, SVM decisions within 1e-3). On-device tests live in `app/src/androidTest/` and are run with
`connectedDebugAndroidTest` (or `adb shell am instrument`).

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
├── ml/                  # YoloDetector + FeatureExtractor + BleachClassifier + RiceClassifier
├── util/                # image decoding (EXIF + sampling)
├── viewmodel/           # one ViewModel per screen
└── ui/
    ├── theme/           # colors, typography, shapes, dimensions
    ├── navigation/      # routes + NavGraph
    ├── screens/         # onboarding, home, camera, analyzing, result, history, detail
    ├── components/      # reusable UI
    └── preview/         # preview-only fixtures

app/src/main/assets/      # yolo11s_beras.tflite + bleach_svm.bin (runtime models)
app/src/test/resources/   # golden vectors for the ML port
tools/                    # python export/verification scripts
models/                  # source artifacts (best.pt/onnx, svm_linear.pkl)
docs/                    # product/architecture/design documentation
```

## Models

Runtime assets are bundled in `app/src/main/assets/`:

- `yolo11s_beras.tflite` — YOLO11s detector (input `1x1280x1280x3` NHWC, output `1x9x33600`),
  classes `beras_utuh, beras_patah, menir, gabah, batu`.
- `bleach_svm.bin` — linear SVM + `StandardScaler` for the bleach classifier.

Source artifacts and provenance live under [`models/`](models/README.md); export/verification
scripts live under [`tools/`](tools/).

## Documentation

- [`PROJECT_STATUS.md`](PROJECT_STATUS.md) — current status, decisions, and next steps
- [`docs/features.md`](docs/features.md) — feature scope and verdict rules
- [`docs/IA.md`](docs/IA.md) — screens and navigation
- [`docs/architecture.md`](docs/architecture.md) — architecture and data flow
- [`docs/design-tokens.md`](docs/design-tokens.md) — colors, type, spacing, shape

## Known limitations

- Device smoke tests were run on an emulator (API 36) with `-gpu host`; SwiftShader crashes on
  this host, so use a hardware renderer.
- No AAB build yet.
- Preview code currently ships in the release build.

## Model provenance

- YOLO11s weights are Ultralytics models, licensed **AGPL-3.0** — see
  [models/README.md](models/README.md) before distributing.
- The bleach SVM is a scikit-learn `SVC(kernel="linear")` ported to Kotlin (156 support vectors).

## Disclaimer

Results are estimates from image analysis, not laboratory tests.
