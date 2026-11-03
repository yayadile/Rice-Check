RiceCheck — Design Tokens
Sumber: Stitch final, 3 Nov 2026 (di-extract dari HTML). Kalau mockup dan file ini konflik, file ini yang menang.
Warna (canonical)
Token
Hex
Dipakai untuk
primary
#388E33
CTA utama, badge ASLI, tab filter aktif
cream
#FFE3A7
Hero banner Beranda, kartu Tips
background
#FBFBF9
Background semua screen
surface
#FFFFFF
Kartu
surface_green
#EBFFE6
Chip, background ikon, banner info
text
#0D200E
Teks utama
text_secondary
#40493D
Teks sekunder, tanggal
outline
#BFCABA
Border kartu
danger
#D32F2F
Badge OPLOSAN, tombol hapus
on_primary
#FFFFFF
Teks di atas hijau/merah
Mockup Stitch tidak konsisten di dua titik — ini versi canonical-nya:
- Background: mockup pakai #FBFBF9 (Beranda) dan #EBFFE6 (screen lain). Pakai #FBFBF9 untuk semua.
- Merah: mockup pakai #D32F2F, #AC0C18, #BA1A1A. Pakai #D32F2F untuk semua.
Tipografi
Headline: Manrope (Bold 700 / ExtraBold 800)
Body & label: Hanken Grotesk (Regular 400 – Bold 700)
Keduanya dari Google Fonts — di Compose pakai downloadable font atau bundle TTF di res/font.
Token
Ukuran / Line-height / Weight
Contoh pakai
Display
28sp / 36sp / 800
Sapaan "Halo!"
Title
22sp / 30sp / 700
Judul screen ("Riwayat Analisis")
Heading
20sp / 28sp / 700
Judul kartu
Heading small
18sp / 24sp / 600
Judul app bar
Body large
16sp / 24sp / 500
Body penting
Body
14sp / 20sp / 400
Body umum
Caption
12sp / 16sp / 400
Tanggal, hint
Label large
14sp / 20sp / 700
Label tombol
Label
12sp / 16sp / 600
Label kecil
Label small
11sp / 14sp / 700
Badge (ASLI/OPLOSAN)
Shape
Token
Nilai
Dipakai untuk
pill
full (50%)
Tombol utama, badge, tab filter
card
12dp
Kartu umum, baris riwayat
card_large
16dp
Hero banner, kartu tips
viewfinder
24dp
Frame kamera
Spacing
xs 4dp · sm 8dp · md 16dp · lg 24dp · xl 32dp. Padding tepi screen: 16dp konsisten.
Ikon
Material Symbols Outlined, ukuran 20–28dp. Jangan pakai emoji sebagai ikon.

Catatan: sisa slop di mockup Stitch (betulkan sebelum implement)
Revisi aturan data (3 Nov): vonis sekarang dihitung dari rumus harga-vs-kualitas
(lihat features.md). Data real v1 = harga (Rp/kg), skor kualitas 0–100, metrik
(% butir utuh, % menir/patah, indikasi pemutih rendah/tinggi), hasil
(ASLI / BERPOTENSI OPLOSAN / LAPORAN KUALITAS), tanggal. Breakdown metrik kualitas
boleh tampil — itu data beneran, bukan karangan.
Onboarding kartu 3: "Murni 100%" — invented stat, hapus. Yang boleh tampil cuma metrik di atas.
"Mudah, Cepat, dan Akurat" (onboarding) — klaim "Akurat" tanpa bukti = badge palsu. Ganti "Mudah dan Cepat" saja.
Toggle Asli/Oplosan di layar Hasil — itu mainan demo Stitch, BUKAN fitur. Hasil datang dari rumus, user tidak bisa toggle.
"ID: #RC-9842" di Riwayat — format ID karangan. Data model kita tidak punya display ID; tampilkan tanggal saja.
Angka "Semua (18) / Asli (14) / Oplosan (4)" — mock data, wajar di mockup; di app real hitung dari DB. Label filter jadi: Semua / Asli / Berpotensi Oplosan.
Tombol/menu Share (ada di script Detail Riwayat) — sharing itu post-v1. Jangan implement di v1.
Badge "Aktif" di strip "7 pemindaian minggu ini" — dekorasi, hapus. Stripnya sendiri OK karena angkanya real (dari countSince).
Layar Hasil butuh redesign total — sekarang ada 3 state (ASLI / BERPOTENSI OPLOSAN / LAPORAN KUALITAS) + input harga di layar Kamera.