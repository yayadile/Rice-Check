RiceCheck — Feature Scope
Aturan: kalau fitur tidak ada di daftar v1, dia post-v1. Tidak ada "sekalian aja".
Konsep vonis v1: harga vs kualitas
Aplikasi tidak memvonis oplosan langsung dari citra. Vonis dihitung dari
ketidakcocokan harga vs kualitas:
Harga
Kualitas
Hasil
Mahal (≥ Rp14.000/kg)
Rendah (skor < 60)
BERPOTENSI OPLOSAN
Mahal (≥ Rp14.000/kg)
Tinggi (skor ≥ 60)
ASLI, kualitas baik
Murah (< Rp14.000/kg)
Apapun
Laporan kualitas saja, tanpa vonis
Tidak diisi
Apapun
Laporan kualitas saja, tanpa vonis
Threshold 14_000 dan 60 adalah konstanta di satu tempat (VerdictConfig) —
gampang diubah tanpa sentuh logic lain.
v1 (wajib)
Scan
Scan beras via kamera dengan frame guide (hint jarak + pencahayaan di viewfinder)
Alternatif: pilih foto dari galeri
Input harga per kg (Rp) di layar kamera, sebelum shutter. Boleh dikosongkan → fallback ke laporan kualitas saja
Minta izin kamera saat pertama kali tap Scan; kalau ditolak → screen penjelasan + tombol "Buka pengaturan"
Analisis
Deteksi on-device (LiteRT), offline penuh
Model = penilai kualitas, bukan classifier oplosan. Output: skor kualitas
  0–100 + metrik: % butir utuh, % menir/patah, indikasi pemutih (rendah/tinggi
  + persentase keyakinan model)
Layar analyzing: animasi scan, 1 baris copy, progress, tombol Batal
Hasil — 3 state:
ASLI (badge hijau): harga mahal + kualitas tinggi
BERPOTENSI OPLOSAN (badge merah): harga mahal + kualitas rendah, plus
   kalimat penjelasan ("Harga Rp16.000/kg tergolong premium, tapi kualitas
   terdeteksi rendah (45%)")
LAPORAN KUALITAS (tanpa badge vonis): harga murah atau tidak diisi —
   tampilkan breakdown kualitas saja
Semua state menampilkan: harga yang diinput, breakdown metrik kualitas,
  disclaimer "Hasil estimasi, bukan uji laboratorium"
Tombol: "Scan lagi", "Lihat riwayat"
Riwayat
Otomatis tersimpan tiap scan: foto, harga, verdict/state, skor + metrik kualitas, tanggal
Filter: Semua / Asli / Berpotensi Oplosan / (atau filter per state)
Detail per scan + hapus (dengan dialog konfirmasi)
Empty state kalau belum ada scan
Onboarding
3 kartu swipeable (foto + harga → analisis → hasil), bisa Skip
Tips foto berkualitas: latar kontras, pencahayaan rata, jarak 10–15 cm tegak lurus
Fokus v1: satu loop — input harga + scan → hasil → riwayat.
Post-v1 (sengaja ditunda)
Login & sinkronisasi antar-device
Statistik pola (merek/toko yang paling sering oplosan)
Share hasil scan