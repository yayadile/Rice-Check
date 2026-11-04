RiceCheck — System Architecture (v1)
Pola: MVVM + Repository, unidirectional data flow (UI observe StateFlow dari ViewModel).
┌──────────────────────┐
│ UI (Compose)         │  screens, components, navigation graph
│ + Navigation         │
└──────────┬───────────┘
           │ UiState (StateFlow) / user events
┌──────────▼───────────┐
│ ViewModel            │  1 ViewModel per screen, logic di sini
└──────────┬───────────┘
┌──────────▼───────────┐
│ Repository           │  ScanRepository = single source of truth
└───┬──────┬───────┬───┘
    │      │       │
  Room   Files   LiteRT
 (data) (foto)  (model)
1. Struktur package
com.yayadile.ricecheck/
├── ui/
│   ├── theme/            # warna, tipografi, shape (dari design-tokens.md)
│   ├── navigation/       # NavGraph + routes (lihat IA.md)
│   ├── screens/          # onboarding, home, camera, analyzing,
│   │                     # result, history, historydetail (1 file per screen)
│   └── components/       # VerdictBadge, ConfidenceBar, ScanRow, dll (dipakai ulang)
├── viewmodel/            # HomeViewModel, CameraViewModel, ResultViewModel, ...
├── domain/               # VerdictCalculator, VerdictConfig (pure logic, testable)
├── data/
│   ├── local/            # ScanResultEntity, ScanDao, AppDatabase
│   ├── repo/             # ScanRepository, ScanRepositoryImpl
│   └── prefs/            # DataStore: onboarding_completed
├── ml/                   # RiceClassifier (wrapper LiteRT)
└── di/                   # AppContainer (manual DI)
2. Database — Room 2.8.5
Satu tabel saja untuk v1.
@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,          // path file di internal storage
    val price: Int?,                // Rp/kg, null = tidak diisi
    val result: String,             // "ASLI" | "BERPOTENSI_OPLOSAN" | "LAPORAN_KUALITAS"
    val qualityScore: Int,          // 0..100
    val wholeGrainPct: Int,         // % butir utuh
    val brokenPct: Int,             // % menir/patah
    val bleachLevel: String,        // "RENDAH" | "TINGGI"
    val createdAt: Long             // epoch millis
)
@Dao
interface ScanDao {
    @Insert suspend fun insert(result: ScanResultEntity): Long
    @Query("SELECT * FROM scan_results ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ScanResultEntity>>
    @Query("SELECT * FROM scan_results WHERE id = :id")
    fun getById(id: Long): Flow<ScanResultEntity?>
    @Query("SELECT * FROM scan_results WHERE result = :result ORDER BY createdAt DESC")
    fun getByResult(result: String): Flow<List<ScanResultEntity>>
    @Query("SELECT COUNT(*) FROM scan_results WHERE createdAt >= :since")
    fun countSince(since: Long): Flow<Int>   // untuk strip "N pemindaian minggu ini"
    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteById(id: Long)
}
AppDatabase: version 1, exportSchema = true.
Catatan: jangan pakai room-ktx (blank artifact sejak 2.8.3). Flow dari room-runtime langsung sudah cukup; collect di viewModelScope.
3. File storage (foto)
Foto disimpan di internal storage: filesDir/scans/<timestamp>.jpg. DB hanya simpan path, bukan blob.
Hapus riwayat = hapus row DB + hapus file. Logikanya di ScanRepository.delete(), bukan di UI.
4. ML pipeline — LiteRT 2.2.0
Model = penilai kualitas, bukan classifier oplosan. Vonis dihitung dari rumus
di §4b, bukan dari model langsung.
Dua model on-device (semua inferensi di Dispatchers.Default):
- Detektor butir: assets/yolo11s_beras.tflite
  Input NHWC 1x1280x1280x3 RGB ternormalisasi 0..1. Output NCHW 1x9x33600 (4 box + 5 kelas).
  Preprocess letterbox: resize INTER_LINEAR, pad 114, scaleup, stride 32.
  Postprocess: conf 0.25, class-aware NMS IoU 0.45, max 1000; box dikembalikan ke piksel asli.
  Kelas: 0 beras_utuh, 1 beras_patah, 2 menir, 3 gabah, 4 batu.
- Klasifikasi kimia: assets/bleach_svm_rbf.bin (RBF SVM + StandardScaler, 198 SV, 8202 fitur).
  Fitur dari citra BGR 256x256: GLCM 6 + HOG(9 bin, 16x16 cell, 2x2 block, L2-Hys) 8100 +
  histogram warna 32 bin x 3 kanal (L2) 96.
  Fitur diekstrak di FeatureExtractor.kt (port sama persis dengan scikit-image/OpenCV).
Mapping yang disetujui (QualityMapping):
  grain = utuh + patah + menir
  wholeGrainPct = round(100 * utuh / grain)
  brokenPct = round(100 * (patah + menir) / grain)
  qualityScore = round(100 * utuh / (utuh + patah + menir + gabah + batu))
  prediksi "pemutih" -> BleachLevel.TINGGI, selain itu RENDAH.
Tanpa deteksi atau model gagal dimuat -> AnalysisException, layar failure; tidak ada angka stub.
Tidak ada angka palsu di build apa pun.
Dataset: gambar beras asli (Ultralytics dataset_det_v3 + classifier pemutih), bukan data contoh.
4b. Rumus hasil (domain layer)
object VerdictConfig {
    const val PREMIUM_PRICE = 14_000   // Rp/kg
    const val LOW_QUALITY = 60         // skor < 60 = rendah
}

fun decide(price: Int?, q: QualityReport): String = when {
    price == null -> "LAPORAN_KUALITAS"
    price >= VerdictConfig.PREMIUM_PRICE && q.qualityScore < VerdictConfig.LOW_QUALITY
        -> "BERPOTENSI_OPLOSAN"
    price >= VerdictConfig.PREMIUM_PRICE -> "ASLI"
    else -> "LAPORAN_KUALITAS"   // harga murah: tampilkan kualitas saja, tanpa vonis
}
VerdictCalculator tinggal di domain/ (pure function, gampang di-unit-test).
Repository menggabungkan: foto → RiceClassifier → QualityReport → decide() →
simpan ScanResultEntity.
5. Camera pipeline — CameraX 1.6.2
Preview + ImageCapture, lifecycle-bound ke screen Camera.
Frame guide = overlay Compose di atas preview (bukan bagian kamera).
Flow izin: minta CAMERA saat user tap Scan pertama kali → ditolak = screen penjelasan + tombol "Buka pengaturan".
Input harga: field numeric "Harga per kg (Rp)" di layar kamera, diisi sebelum shutter. Boleh kosong → hasil jadi LAPORAN_KUALITAS (tanpa vonis).
Capture → simpan ke filesDir/scans/ → navigasi ke analyzing dengan photoUri + price.
6. Preferences — DataStore
Key v1: onboarding_completed: Boolean. Plus legacy_stub_rows_cleared: Boolean (sekali pakai, untuk
membersihkan riwayat yang dibuat saat model masih stub). Jangan taruh di Room.
7. Dependency Injection
Manual AppContainer di Application class: database, DAO, repository, classifier (lazy). ViewModel dibuat via factory sederhana. Hilt opsional — tidak wajib untuk v1, jangan tambah complexity kalau belum perlu.
8. Threading & error handling
Operasi
Di mana
Query Room
Flow, collect di viewModelScope
Inferensi model
Dispatchers.Default
Insert/delete
viewModelScope.launch(Dispatchers.IO)
Error kamera / model gagal load / 0 deteksi
AnalysisException → AnalyzingState.Failed → layar gagal (bukan angka stub)
9. Build config
compileSdk 37, minSdk 26, AGP 9.1.1+, Kotlin 2.x
Compose BOM 2026.09.00, CameraX 1.6.2, Room 2.8.5, com.google.ai.edge.litert:litert:2.2.0
packaging { resources { excludes } } + noCompress "tflite"
Manifest: <uses-permission android:name="android.permission.CAMERA" />