RiceCheck — Information Architecture (v1)
Scope: v1 = scan → hasil → riwayat. Di luar itu (login/sync, statistik pola, share) post-v1.
1. Peta Screen
Onboarding (first launch, skippable)
└── Home (Beranda)                     ← bottom nav
    ├── Camera (Pindai Beras)          ← CTA "Scan Beras" / FAB tengah
    │   └── Analyzing
    │       └── Result (Detail Hasil Analisis)
    │           ├── → Camera ("Scan lagi")
    │           └── → History ("Lihat riwayat")
    └── History (Riwayat Analisis)     ← bottom nav
        └── History Detail
            └── Delete → dialog konfirmasi → kembali ke History
Bottom nav (3 titik): Beranda | Scan (FAB tengah, menonjol) | Riwayat
2. Route (untuk Compose Navigation)
Route
Screen
Argumen
onboarding
Onboarding
—
home
Home
—
camera
Camera/Scan
—
analyzing
Analyzing
photoUri
result/{scanId}
Result
scanId
history
History
—
historyDetail/{scanId}
History Detail
scanId
3. User Flow
3.1 First launch
Onboarding (3 kartu, bisa Skip) → Home.
3.2 Core loop (maks 3 tap dari Home)
Home → tap Scan Beras → Camera (isi harga per kg → frame guide: atur jarak + cahaya, tombol shutter, opsi "pilih dari galeri") → capture → Analyzing (progress, cancel) → Result.
3.3 History
Tab Riwayat → filter (Semua / Asli / Oplosan) → tap baris → History Detail → Hapus (dialog konfirmasi "Hapus dari riwayat?") → kembali ke list.
3.4 Tips
Home → kartu "Tips Foto Beras" (collapsible, 3 tips: latar kontras, pencahayaan rata, jarak 10–15 cm tegak lurus).
4. Isi Tiap Screen (data jujur saja)
Data real v1: harga (Rp/kg), skor kualitas 0–100, metrik (% butir utuh,
% menir/patah, indikasi pemutih rendah/tinggi), hasil (ASLI / BERPOTENSI
OPLOSAN / LAPORAN KUALITAS), tanggal scan. Hanya ini yang boleh tampil.
Onboarding — 3 kartu (foto + harga → analisis → hasil), Skip, Mulai.
Home — hero banner (ilustrasi + sapaan), CTA Scan Beras, strip insight (1 angka real: "N pemindaian minggu ini"), kartu tips (collapsible), 3 scan terakhir (thumbnail, hasil, tanggal).
Camera — input harga per kg (Rp, numeric; boleh kosong → fallback laporan kualitas), viewfinder, frame guide overlay, hint jarak/cahaya, shutter, pilih dari galeri, back.
Analyzing — animasi scanning, 1 baris copy, progress indicator, tombol Batal.
Result — 3 state:
ASLI (badge hijau #388E33): harga mahal + kualitas tinggi
BERPOTENSI OPLOSAN (badge merah #D32F2F): harga mahal + kualitas rendah + kalimat penjelasan ("Harga Rp16.000/kg tergolong premium, tapi kualitas terdeteksi rendah (45%)")
LAPORAN KUALITAS (tanpa badge vonis): harga murah / tidak diisi
  Semua state: harga yang diinput, breakdown metrik (butir utuh %, menir/patah %, pemutih), 1 disclaimer ("Hasil estimasi, bukan uji laboratorium"), 2 tombol: Scan lagi, Lihat riwayat.
History — tab filter Semua / Asli / Berpotensi Oplosan, baris (thumbnail, hasil, tanggal), empty state ("Belum ada pemindaian").
History Detail — foto besar, harga, hasil, skor + metrik kualitas, tanggal, tombol Hapus (+ dialog konfirmasi).
5. States
Kondisi
Penanganan
History kosong
Empty state + CTA "Scan sekarang"
Izin kamera ditolak
Screen penjelasan + tombol "Buka pengaturan"
Analyzing dibatalkan
Kembali ke Camera, tidak menyimpan apa-apa
Hapus riwayat
Dialog konfirmasi dulu, baru hapus
6. Data Model (Room, ringkas)
ScanResult: id, photoPath, price (nullable), result (ASLI / BERPOTENSI_OPLOSAN / LAPORAN_KUALITAS), qualityScore (0–100), wholeGrainPct, brokenPct, bleachLevel (RENDAH/TINGGI), timestamp. Detail penuh di architecture.md §2.
7. Di luar scope v1
Login & sinkronisasi antar-device, statistik pola (merek/toko), share hasil scan.