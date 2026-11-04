# RiceCheck — Status Proyek & Handoff v1

> **Update terakhir:** 3 November 2026
> **Repo:** `https://github.com/yayadile/Rice-Check.git` (branch `main`, commit `b4a1491`)
> **Build terakhir:** `assembleDebug` + `testDebugUnitTest` → **BUILD SUCCESSFUL**, 10/10 unit test lulus.
> **Status singkat:** fitur v1 (scan → hasil → riwayat) sudah terimplementasi dan bisa di-preview di IDE.
> **Yang belum:** model ML asli (masih stub) dan uji jalan di HP/emulator (belum ada device di mesin ini).

Dokumen ini untuk AI/dev berikutnya. Baca ini dulu, baru `docs/`. Kalau dokumen ini dan `docs/` berbeda,
**`docs/` tetap sumber kebenaran produk**, sedangkan dokumen ini mencatat kondisi kode apa adanya
(termasuk penyimpangan kecil yang sudah disepakati).

### Kalau kamu AI dan baru masuk
1. Baca file ini sampai habis (10 menit).
2. Baca `docs/features.md` → `docs/IA.md` → `docs/architecture.md` → `docs/design-tokens.md`.
3. Jalankan `.\gradlew.bat assembleDebug testDebugUnitTest` untuk memastikan baseline hijau.
4. Buka `ui/screens/` di Android Studio, cek Preview — UI bisa diverifikasi tanpa HP.
5. Mulai dari §12 (next steps). Jangan refactor arsitektur tanpa alasan kuat.
6. Sebelum mengubah data/verdict, baca §1 (aturan main) dan §5–§6.

---

## 1. Aturan main (jangan dilanggar)

1. **Jangan tampilkan data palsu sebagai data nyata.** Semua angka di UI harus berasal dari Room/model.
   Selama model belum ada, hasil stub **wajib** ditandai banner "Model belum tersedia… data contoh (stub)".
2. **Vonis bukan dari model.** Model hanya memberi `QualityReport`; vonis dihitung `VerdictCalculator`
   dari harga vs kualitas. Jangan pindahkan logika ini ke UI atau ke model.
3. **Jangan tambah fitur post-v1:** login, sync, statistik pola, share, toggle Asli/Oplosan,
   ID karangan, klaim "akurat"/"murni 100%".
4. **Scope v1 = satu loop:** onboarding → home → kamera (foto + harga) → analyzing → result → riwayat.
5. **Jangan pakai `room-ktx`** (artifact kosong sejak Room 2.8.3). Flow dari `room-runtime` sudah cukup.
6. **Skema Room jangan diubah** tanpa migration + naikkan `version`. `app/schemas/` wajib ikut di-commit.
7. **Warna/font/shape hanya dari `ui/theme/`** yang mengacu ke `docs/design-tokens.md`. Jangan hardcode warna di screen.
8. **Teks UI bahasa Indonesia**, identifier/kode bahasa Inggris. Komentar menyebut bagian dokumen (`docs/IA.md §…`) bila relevan.
9. **Klaim build hanya kalau benar-benar lulus.** Jalankan Gradle sebelum bilang "beres".
10. **Jangan hapus/ubah file yang tidak dipahami.** Repo ini tidak memakai branch selain `main`.

---

## 2. Produk: apa yang dibangun

RiceCheck = aplikasi Android untuk memeriksa kualitas beras dari foto **plus** harga per kg,
lalu memberi vonis berbasis ketidakcocokan harga vs kualitas. Analisis sepenuhnya on-device/offline.

### Rumus vonis (sumber: `docs/features.md`, `domain/VerdictConfig.kt`)

| Harga | Kualitas | Hasil |
|---|---|---|
| ≥ Rp14.000/kg | skor < 60 | `BERPOTENSI_OPLOSAN` |
| ≥ Rp14.000/kg | skor ≥ 60 | `ASLI` |
| < Rp14.000/kg | apa pun | `LAPORAN_KUALITAS` (tanpa vonis) |
| tidak diisi (`null`) | apa pun | `LAPORAN_KUALITAS` (tanpa vonis) |

- `VerdictConfig.PREMIUM_PRICE = 14_000`, `VerdictConfig.LOW_QUALITY = 60` — satu tempat, jangan duplikasi.
- Metrik kualitas: `qualityScore` 0–100, `wholeGrainPct`, `brokenPct`, `bleachLevel` (`RENDAH`/`TINGGI`).

### Di luar scope v1
Login & sinkronisasi, statistik pola merek/toko, share hasil, dark mode, dynamic color, multi-bahasa.

---

## 3. Stack & konfigurasi

| Komponen | Versi |
|---|---|
| AGP | 9.1.1 |
| Gradle wrapper | 9.3.1 |
| Kotlin plugin | 2.2.10 |
| KSP | 2.2.10-2.0.2 |
| JDK | 17 (Temurin 17.0.20.1) — `sourceCompatibility`/`targetCompatibility` 17 |
| compileSdk / targetSdk | 37 |
| minSdk | 26 |
| Compose BOM | 2026.09.00 |
| CameraX | 1.6.2 |
| Room | 2.8.5 (runtime + compiler KSP, **tanpa room-ktx**) |
| LiteRT | `com.google.ai.edge.litert:litert:2.2.0` |
| Navigation Compose | 2.10.2 |
| DataStore Preferences | 1.3.0 |
| Lifecycle | 2.8.7 |
| JUnit | 4.13.2 |

- Package/namespace: **`com.yayadile.ricecheck`** (jangan pakai nama lama `com.kel2.*`).
- `noCompress += "tflite"` sudah diset supaya model bisa di-mmap LiteRT.
- Manifest **tidak meminta INTERNET** — memang offline penuh. Galeri memakai Android Photo Picker
  (`PickVisualMedia`), jadi tidak butuh izin storage.
- **Tanpa Hilt.** DI manual: `AppContainer` di `RiceCheckApplication`, diakses lewat
  `CompositionLocal` (`LocalAppContainer`).
- Theme: light-only, tanpa dark scheme/dynamic color (token hanya mendefinisikan satu palet).
- Font: `manrope.ttf` + `hanken_grotesk.ttf` di-bundle sebagai variable font (offline, weight asli di API 28+).
- Ikon: hasil konversi Material Symbols Outlined → vector drawable (`res/drawable/ic_*.xml`), tanpa dependency ikon.

### Lingkungan build di mesin ini
- SDK: `C:\Users\MSI\AppData\Local\Android\Sdk` (tertulis di `local.properties`, file ini di-`.gitignore`).
- Platform terpasang: `android-34`, `android-35`, `android-36`, `android-37.0`.
- Build-tools: `36.0.0`, `37.0.0`.
- **Tidak ada `emulator` dan tidak ada device** (`adb devices` kosong) → runtime belum pernah diuji.
- APK debug: `app/build/outputs/apk/debug/app-debug.apk` (± **44 MB**, besar karena LiteRT).

---

## 4. Arsitektur & alur data

Pola: **MVVM + Repository**, unidirectional data flow (UI observe `StateFlow`).

```
Compose UI (screens/components)
        │  events (lambda)
        ▼
ViewModel (StateFlow / UiState)
        │
        ▼
ScanRepository (single source of truth)
   ├── Room (ScanDao)
   ├── Files (filesDir/scans/*.jpg)
   └── LiteRT (RiceClassifier → QualityReport)
        │
        ▼
VerdictCalculator.decide(price, report)  ← domain, pure
        │
        ▼
ScanResultEntity disimpan ke Room
```

### Alur satu scan
1. Home → `camera`.
2. `CameraScreen`: minta izin CAMERA (sekali), tampilkan preview CameraX + frame guide +
   field harga (`onPriceChange` hanya digit, maks 7 karakter, boleh kosong).
3. Shutter → `ImageCapture` (targetRotation diset dari display agar JPEG tegak) →
   file baru `filesDir/scans/<timestamp>.jpg`.
   Alternatif: galeri → `PickVisualMedia` → disalin ke internal storage (`ScanFileStore.importFromUri`).
4. Navigasi `analyzing?photoUri=…&price=…`.
5. `AnalyzingViewModel` → `ScanRepository.save(photoPath, price)`:
   decode + EXIF rotation (`ImageDecoder`) → `RiceClassifier.classify()` → `VerdictCalculator.decide()`
   → insert Room → return `scanId`.
6. Navigasi `result/{scanId}` (Camera + Analyzing di-pop dari back stack).
7. Result membaca row via `observeById(scanId)`; kalau stub, tampilkan banner.

### Navigasi (`ui/navigation/NavGraph.kt`, `Routes.kt`)

| Route | Screen | Argumen |
|---|---|---|
| `onboarding` | Onboarding | — |
| `home` | Home | — |
| `camera` | Camera | — |
| `analyzing?photoUri={photoUri}&price={price}` | Analyzing | photoUri, price (nullable) |
| `result/{scanId}` | Result | scanId |
| `history` | History | — |
| `historyDetail/{scanId}` | History Detail | scanId |

Catatan: `docs/IA.md §2` hanya menyebut `analyzing` dengan `photoUri`, sedangkan
`docs/architecture.md §5` bilang "photoUri + price". **Keputusan: price ikut di route** agar harga
yang diisi di kamera tidak hilang. Jangan hapus argumen `price`.

- Start destination ditentukan `RiceCheckRoot`: baca `onboarding_completed` dari DataStore **sebelum**
  NavHost dibuat (tidak ada flash onboarding).
- Tab switching pakai `switchTab()` (`popUpTo(home)`), `Scan lagi` pop Result, `Lihat riwayat` ke tab History.
- Cancel analyzing = pop back ke Camera; file temp dihapus, tidak ada row DB.
- Hapus riwayat = dialog konfirmasi → `repository.delete(id)` (row + file) → pop ke History.

### DI (`di/`)
`AppContainer` lazy membuat: `AppDatabase`, `ScanDao`, `RiceClassifier`, `ScanFileStore`,
`ScanRepositoryImpl`, `OnboardingPrefs`, dan `isClassifierStub` (turunan `!riceClassifier.isModelAvailable`).
ViewModel dibuat lewat factory manual di `viewmodel/ViewModelFactories.kt`.

---

## 5. Data layer

### Room (versi 1, `exportSchema = true`)
Satu tabel: `scan_results`

| Kolom | Tipe | Catatan |
|---|---|---|
| `id` | Long PK autoGenerate | |
| `photoPath` | String | path internal, bukan blob |
| `price` | Int? | Rp/kg, null = tidak diisi |
| `result` | String | `ASLI` / `BERPOTENSI_OPLOSAN` / `LAPORAN_KUALITAS` |
| `qualityScore` | Int | 0..100 |
| `wholeGrainPct` | Int | % butir utuh |
| `brokenPct` | Int | % menir/patah |
| `bleachLevel` | String | `RENDAH` / `TINGGI` |
| `createdAt` | Long | epoch millis |

DAO: `insert`, `getAll`, `getById`, `getByResult`, `countSince`, `deleteById` (semua Flow/suspend).
Skema JSON ada di `app/schemas/com.yayadile.ricecheck.data.local.AppDatabase/1.json` — wajib ikut di-commit.

**`isStub` sengaja TIDAK disimpan di DB.** Status stub diturunkan dari `AppContainer.isClassifierStub`
(karena tidak ada model → semua baris lama juga stub). Kalau model ditambahkan nanti, pertimbangkan
konsekuensi baris lama yang dibuat saat masih stub.

### File foto
- Lokasi: `filesDir/scans/<timestamp>.jpg`.
- DB hanya menyimpan path. Hapus row harus menghapus file juga — dilakukan repository, bukan UI.
- `ImageDecoder` menangani EXIF rotation + sampling; dipakai untuk model dan thumbnail.

### Preferences
- DataStore `ricecheck_prefs`, satu key: `onboarding_completed: Boolean`. Jangan pindah ke Room.

---

## 6. ML — LiteRT (kondisi sekarang: STUB)

File: `ml/RiceClassifier.kt`.

- Model dicari di `assets/ricecheck_v1.tflite` dan di-load **lazy** sekali.
- **Folder `app/src/main/assets/` saat ini kosong** → `CompiledModel` gagal → `isModelAvailable = false`.
- Karena itu `classify()` mengembalikan `QualityReport` stub:
  `qualityScore=72, wholeGrainPct=82, brokenPct=18, bleachLevel=RENDAH, isStub=true`.
- `ResultScreen` membaca `container.isClassifierStub` dan menampilkan banner peringatan.
  Banner ini **bukan** dekorasi — jangan dihapus selama model belum ada.
- Preprocessing sudah ada: resize 224×224, RGB float 0..1, di `Dispatchers.Default`.
- `TODO(ml)` di `classify()` berisi rencana pemanggilan `CompiledModel` (input/output buffers).

### Kalau mau memasang model asli
1. Taruh file di `app/src/main/assets/ricecheck_v1.tflite` (nama harus sama / ubah `ASSET_NAME`).
2. Implement inferensi di `RiceClassifier.classify()`: tulis input float, `model.run(inputs, outputs)`,
   mapping output → `QualityReport(isStub = false)`.
3. **Cocokkan preprocessing dengan training** (ukuran input, normalisasi, urutan channel). Kalau model
   dilatih dengan 300×300 atau normalisasi ImageNet, ubah `INPUT_SIZE`/`preprocess()`.
4. Begitu model termuat, `isClassifierStub` otomatis `false` dan banner hilang. Verifikasi ulang di device.
5. Tambah unit test untuk mapping output (tanpa Android) bila logika mapping dipisah ke fungsi murni.

Ingat: output model = **penilai kualitas**, bukan classifier oplosan. Vonis tetap dari `VerdictCalculator`.

---

## 7. UI, komponen, dan PREVIEW

### Pola wajib: stateful wrapper + stateless content
Setiap screen dipecah dua:

```kotlin
@Composable fun XxxScreen(...)   // ambil ViewModel/AppContainer, teruskan state + lambda
@Composable fun XxxContent(...)  // murni(state, callbacks), aman untuk @Preview
```

Semua `@Preview` merender `XxxContent` (bukan `XxxScreen`), sehingga **tidak menyentuh Room,
DataStore, CameraX, atau Application**. Ini syarat dari user: UI dicek lewat preview IDE,
bukan install ke HP.

### Daftar preview (total 21 fungsi)

| File | Preview |
|---|---|
| `screens/HomeScreen.kt` | `Beranda`, `Beranda — belum ada scan` |
| `screens/HistoryScreen.kt` | `Riwayat — ada data`, `Riwayat — kosong` |
| `screens/ResultScreen.kt` | `Hasil — ASLI`, `Hasil — Berpotensi Oplosan`, `Hasil — Laporan Kualitas` |
| `screens/HistoryDetailScreen.kt` | `Detail Riwayat`, `Detail Riwayat — ASLI` |
| `screens/CameraScreen.kt` | `Pindai Beras — viewfinder`, `Pindai Beras — izin ditolak` |
| `screens/AnalyzingScreen.kt` | `Analisis — berjalan`, `Analisis — gagal` |
| `screens/OnboardingScreen.kt` | `Onboarding — kartu 1` |
| `components/BackTopBar.kt` | `BackTopBar` |
| `components/Buttons.kt` | `Buttons` (primary, secondary, disabled) |
| `components/QualityBreakdown.kt` | `QualityBreakdown` |
| `components/RiceBottomBar.kt` | `RiceBottomBar` |
| `components/ScanPhoto.kt` | `ScanThumbnail` (fallback saat file hilang) |
| `components/ScanRow.kt` | `ScanRow` (3 vonis) |
| `components/VerdictBadge.kt` | `VerdictBadge — semua status` |

- Semua memakai `@Preview(showBackground = true, …)`.
- Preview dibungkus `PreviewSurface` (`ui/preview/PreviewFixtures.kt`) yang memasang `RiceCheckTheme`
  dan menampilkan chip kecil **`PREVIEW DATA`** di kanan atas — penanda visual bahwa data itu palsu.
- Data palsu: `PreviewFixtures` (`asli`, `berpotensiOplosan`, `laporanKualitas`, `recentScans`),
  KDoc-nya tegas **PREVIEW ONLY — production code must never import this object**.
  `photoPath = ""` supaya thumbnail memakai fallback asli (tidak ada bitmap palsu).
- Preview kamera memakai backdrop warna datar (CameraX tidak bisa jalan di preview); sisa overlay
  (frame guide, field harga, shutter, galeri) adalah UI asli.
- Preview ada di `main` source set (bukan `debug`) supaya gampang ditemukan dan ikut terkompilasi.
  Kalau nanti mau ramping, pindahkan fungsi preview + `ui/preview/` ke `src/debug/` —
  composable `Content` sudah public sehingga tetap bisa diakses.

### Cara cek
Buka file screen/komponen di Android Studio → panel **Preview** (atau Split). Pilih preview
sesuai daftar di atas. Tidak perlu emulator.

### Perilaku UI yang sudah ada
- Onboarding: 3 kartu swipeable + Lewati/Mulai; selesai → set flag DataStore.
- Home: sapaan, hero CTA, jumlah scan minggu ini (real dari `countSince`), kartu tips collapsible,
  3 scan terakhir; empty state teks + "Scan sekarang".
- Camera: izin kamera (rationale + tombol buka Settings kalau ditolak), preview CameraX, frame guide,
  hint jarak/cahaya, input harga, shutter, galeri, snackbar saat gagal.
- Analyzing: scan-line animation, progress, Batal; state gagal punya "Coba lagi"/"Kembali".
- Result: 3 state + harga + breakdown + tanggal + disclaimer + 2 tombol; banner stub bila perlu.
- History: filter Semua/Asli/Berpotensi Oplosan (label & isi real, tanpa angka mock),
  row thumbnail+hasil+tanggal, empty state.
- History Detail: foto besar (atau placeholder), harga, badge, metrik, tanggal, hapus + dialog.

---

## 8. Theme (ringkas dari `docs/design-tokens.md`)

| Token | Hex | Dipakai |
|---|---|---|
| primary | `#388E33` | CTA, badge ASLI, filter aktif |
| cream | `#FFE3A7` | hero Beranda, kartu tips |
| background | `#FBFBF9` | semua screen |
| surface | `#FFFFFF` | kartu |
| surface_green | `#EBFFE6` | chip, background ikon |
| text | `#0D200E` | teks utama |
| text_secondary | `#40493D` | teks sekunder, tanggal |
| outline | `#BFCABA` | border |
| danger | `#D32F2F` | badge oplosan, hapus |
| on_primary | `#FFFFFF` | teks di atas hijau/merah |

- Font: Manrope (headline), Hanken Grotesk (body/label) — lihat `ui/theme/Type.kt`.
- Shape: pill 50%, card 12dp, card_large 16dp, viewfinder 24dp.
- Spacing: xs 4 · sm 8 · md 16 · lg 24 · xl 32; padding tepi screen **16dp**.

---

## 9. Status per fitur

| Item | Status | Catatan |
|---|---|---|
| Scaffold + Gradle + manifest | ✅ | package `com.yayadile.ricecheck` |
| Theme dari token | ✅ | light-only |
| Onboarding (3 kartu, skip) | ✅ | DataStore gate |
| Home (CTA, tips, count, 3 terakhir) | ✅ | data real dari Room |
| Camera (CameraX, izin, harga, galeri) | ✅ | belum diuji di device |
| Analyzing (animasi, cancel, error) | ✅ | |
| Result 3 state | ✅ | + banner stub + disclaimer |
| History + filter + empty state | ✅ | |
| Detail riwayat + hapus | ✅ | row + file |
| Room + schema export | ✅ | v1, tanpa room-ktx |
| Manual DI + factories | ✅ | |
| Unit test domain | ✅ | 10 test, 0 gagal |
| Preview semua screen/komponen | ✅ | 21 preview, data preview-only |
| Inferensi model asli | ❌ | masih stub `TODO(ml)` |
| Uji runtime di HP/emulator | ❌ | tidak ada device/emulator |
| Instrumentation test | ❌ | belum ada |
| Release signing / AAB | ❌ | baru debug build |
| Screenshot/preview rendering otomatis | ❌ | dicek manual di IDE |

---

## 10. Build, test, run

```powershell
# dari root repo
.\gradlew.bat assembleDebug testDebugUnitTest
```

- APK: `app\build\outputs\apk\debug\app-debug.apk`
- Hasil test: 10 test di `app/src/test/.../VerdictCalculatorTest.kt`.
- JDK 17 (Gradle daemon memakai `C:\Users\MSI\.jdks\jdk-17`).
  Di mesin lain, sesuaikan `org.gradle.java.home` (project/user `gradle.properties`) atau set `JAVA_HOME`
  ke JDK 17+; jangan andalkan path `C:\Program Files\Android\Android Studio\jbr` yang bisa tidak ada.
- Kalau SDK/komponen tidak ada, **laporkan apa yang harus di-install**, jangan diamkan:
  - SDK Platform 37, Build-tools 37.0.0, platform-tools, (opsional) emulator + system image.

```powershell
# install ke device (butuh adb device terhubung)
.\gradlew.bat installDebug
adb shell am start -n com.yayadile.ricecheck/.MainActivity
```

---

## 11. Known issues / gotchas

1. **Belum ada bukti jalan di device.** Build + preview hijau, tapi alur kamera/izin/penyimpanan
   belum diuji runtime. Jangan klaim "sudah berfungsi di HP" sebelum benar-benar dites.
2. **Semua hasil analisis masih stub.** Angka 72/82/18/RENDAH bukan hasil model.
3. Warning build yang tidak berbahaya (jangan panik):
   - LiteRT duplicate namespace `com.google.ai.edge.litert`,
   - `android.disallowKotlinSourceSets=false` masih experimental,
   - `LocalLifecycleOwner` deprecated (pindah ke `androidx.lifecycle.compose`),
   - `Locale(String, String)` deprecated.
4. APK ±44 MB karena LiteRT; wajar untuk v1.
5. Preview ikut terkompilasi ke release (belum dipindah ke `src/debug`).
6. `HistoryFilter` hanya 3 nilai (Semua/Asli/Berpotensi Oplosan). `LAPORAN_KUALITAS`
   tetap muncul di "Semua", tidak punya chip sendiri — sesuai `docs/IA.md §4`.
7. Input harga: hanya digit, maksimal 7 karakter, kosong → `null` → laporan kualitas.
8. Format tanggal/harga memakai locale `id-ID` (`NumberFormat`, `DateTimeFormatter`).
9. Git: branch `main`, commit terakhir `b4a1491`, **unggul 1 commit dari `origin/main`** (belum di-push).
   Sebelum dokumen ini dibuat working tree bersih; `PROJECT_STATUS.md` sendiri belum di-commit.
10. Model `CompiledModel` di-load lazy; kalau asset rusak, app tetap jalan dengan stub (tidak crash).
11. `ScanResultEntity.photoPath` non-null; file yang hilang ditangani fallback UI (`ScanThumbnail`,
    "Foto tidak tersedia" di detail).

---

## 12. Next steps — status

1. **Smoke test emulator** — SELESAI (API 36). Instrumented test `RiceClassifierInstrumentedTest`
   lulus 3/3 (model load, YOLO deteksi, mapping). Catatan: emulator harus pakai `-gpu host`;
   SwiftShader segfault di mesin ini.
2. **Model asli + inferensi** — SELESAI. YOLO11s (LiteRT) + RBF SVM di Kotlin; parity fitur
   ~1e-7 dan keputusan SVM <1e-3 terhadap scikit-image/OpenCV/sklearn.
3. **Baris lama saat stub** — SELESAI: dihapus sekali saat upgrade (`legacy_stub_rows_cleared`).
4. **Build release** — BELUM: signing config, `versionName`, minify, cek ukuran.
5. Opsional: pindahkan preview ke `src/debug` + Screenshot Test — BELUM.
6. Docs — SELESAI untuk pipeline ML (README, models/README, docs/architecture.md).

### Temuan porting (penting untuk regen)
- OpenCV 5.0 `BGR2GRAY`: `(9798R + 19235G + 3735B + 16384) >> 15` (bukan koefisien 14-bit lama).
- skimage `graycomatrix(symmetric=True)` = `P + Pᵀ` (diagonal digandakan).
- OpenCV `MORPH_CLOSE, iterations=2` = satu kernel 5×5 (bukan dua kali 3×3).
- HOG skimage 256×256/16/2×2 → 8100, bin nearest (tanpa interpolasi).

---

## 13. Peta file penting

```
app/src/main/kotlin/com/yayadile/ricecheck/
├── RiceCheckApplication.kt          # membuat AppContainer
├── MainActivity.kt                  # CompositionLocalProvider + theme + root
├── di/
│   ├── AppContainer.kt              # manual DI, isClassifierStub
│   └── LocalAppContainer.kt
├── domain/
│   ├── VerdictConfig.kt             # PREMIUM_PRICE=14_000, LOW_QUALITY=60
│   ├── VerdictCalculator.kt         # decide() pure
│   └── QualityReport.kt             # output model + BleachLevel
├── data/
│   ├── local/                       # ScanResultEntity, ScanDao, AppDatabase
│   ├── repo/                        # ScanRepository, Impl, ScanFileStore
│   └── prefs/OnboardingPrefs.kt
├── ml/
│   ├── RiceClassifier.kt            # orkestrasi YOLO + SVM -> QualityReport
│   ├── YoloDetector.kt / YoloPost.kt# LiteRT, letterbox, decode, NMS
│   ├── FeatureExtractor.kt          # GLCM + HOG + histogram (parity Python)
│   ├── BleachClassifier.kt          # StandardScaler + RBF SVM
│   ├── QualityMapping.kt            # rumus metrik yang disetujui
│   └── AnalysisException.kt         # kegagalan analisis (tanpa angka stub)
├── util/ImageDecoder.kt             # EXIF + sampling
├── viewmodel/                       # 7 VM + factories
└── ui/
    ├── theme/                       # Color, Type, Shape, Dimens, Theme
    ├── navigation/                  # Routes, NavGraph, start gate
    ├── format/ScanFormat.kt         # harga, tanggal, label
    ├── components/                  # BackTopBar, Buttons, QualityBreakdown,
    │                                # RiceBottomBar, ScanPhoto, ScanRow, VerdictBadge
    ├── screens/                     # 7 screen (Screen + Content + Preview)
    └── preview/PreviewFixtures.kt   # data PREVIEW ONLY + PreviewSurface
app/src/main/assets/                 # yolo11s_beras.tflite, bleach_svm_rbf.bin
app/src/test/.../VerdictCalculatorTest.kt
app/src/test/.../ml/                 # golden-vector + postprocess + mapping tests
app/src/test/resources/golden/       # golden vectors (svm resized/features/scaled, yolo)
app/src/androidTest/                 # RiceClassifierInstrumentedTest + foto uji
app/schemas/.../AppDatabase/1.json
tools/                               # export/verify/golden python scripts
docs/                                # features, IA, architecture, design-tokens
```

---

## 14. Keputusan yang sudah diambil (jangan diulang/debatkan tanpa alasan baru)

- `analyzing` membawa `photoUri` **dan** `price` (resolusi konflik dokumen).
- Font di-bundle, bukan downloadable font → app tetap 100% offline.
- Light-only, tanpa dynamic color.
- Stub ditampilkan dengan banner jelas, bukan disembunyikan/dipalsukan.
- Ikon vector drawable, tanpa library ikon.
- Tanpa Hilt; factory manual.
- Tanpa room-ktx.
- `isStub` tidak dipersist di DB.
- Preview memakai `Content` stateless + `PreviewFixtures` + chip `PREVIEW DATA`.
- Tidak ada angka stub sejak model asli terpasang: kegagalan → `AnalysisException`, bukan angka palsu.
- Model tetap hanya menghasilkan metrik kualitas; vonis tetap di `VerdictCalculator`.
- SVM dipilih varian RBF non-CLAHE (`C=10`, `gamma=scale`), di-port ke Kotlin (bukan runtime Python).
- YOLO TFLite NHWC + output NCHW; NMS class-aware conf 0.25 / IoU 0.45 / max 1000.
- Model disimpan sebagai aset runtime di `app/src/main/assets/` (bukan di `models/`).
