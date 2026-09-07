# Logika Frontend & UI Design: BuckManagerApp

Dokumen ini memaparkan prinsip desain antarmuka, struktur komponen visual, serta alur logika *frontend* yang menghidupkan pengalaman pengguna pada aplikasi **BuckManager**.

## 1. Filosofi Desain & Estetika (UI Design)

BuckManager dirancang dengan estetika modern, *sleek*, dan mengutamakan nuansa *dark mode* premium yang dapat dikustomisasi secara mendalam (mirip antarmuka dasbor *gaming* atau *cyberpunk-lite*).

- **Sistem Pewarnaan Dinamis**: Tidak seperti aplikasi biasa yang warnanya kaku, BuckManager memberikan kebebasan mutlak bagi pengguna untuk mengatur warna dasar (*Background*), aksen (*Accent*), hingga warna spesifik teks (Label, *Value*, Deskripsi) di setiap elemen. 
- **Tailwind CSS via NativeWind**: Keseluruhan kerangka antarmuka ditata menggunakan utilitas kelas Tailwind (seperti `bg-primary`, `text-textLight`, `rounded-2xl`, `flex-row`). Hal ini mempercepat proses *styling* dan menjaga konsistensi jarak antar elemen (margin/padding).
- **Efek Latar Belakang (Background FX)**: Tersedia komponen animasi *canvas* rekayasa khusus seperti `ParticleStarfall.js` (efek bintang jatuh) dan `ParticleLines.js` (efek garis partikel dinamis) yang merender animasi berkelanjutan di belakang komponen utama, memberikan kesan "hidup" pada dasbor.

## 2. Struktur Komponen (Frontend Logic)

Pendekatan *frontend* mengadopsi struktur komponen tersarang (*nested*) dan modular.

### 2.1 Tab Navigation & Layar Utama (Screens)
Secara teknis, sebelum masuk ke layar utama, pengguna harus melalui **`LoginScreen`** (Gateway Autentikasi Google). Setelah *session* OAuth tervalidasi, aplikasi merender pola **Bottom Tab Navigation** (melalui `@react-navigation/bottom-tabs`) yang membawahi tiga layar utama:
- **`DashboardScreen`**: Layar beranda yang menampilkan Ringkasan Keuangan (Net Worth, Income, Expense) dan daftar persentase *slot/envelope* secara langsung.
- **`TransactionScreen`**: Layar manajemen entri keuangan (pemasukan/pengeluaran) lengkap dengan fitur "Recurring Bills" (Tagihan Rutin) dan histori transaksi bulanan.
- **`AccountantScreen`**: Layar ("Allocations") yang bertugas menghitung dan mendistribusikan angka riil total pendapatan ke masing-masing *envelope* sesuai persentase target yang telah diatur pengguna.

### 2.2 Sistem Bottom-Sheet (Overlays)
Untuk sub-menu kustomisasi atau form yang padat, aplikasi tidak beralih layar penuh, melainkan memunculkan *Overlay* (Hamparan) berbasis `Modal` bawaan React Native dengan animasi `slide` dari bawah.
- **Pola Desain Overlay**: Layar memiliki lapisan latar belakang setengah transparan (`bg-black/50`). Konten *overlay* memiliki sudut membulat di bagian atas yang mensimulasikan laci geser (*drawer*).
- Komponen *Overlay* krusial yang tersedia:
  - `SettingsOverlay.js`: Panel pengaturan aplikasi, *backup* Google Drive, dan tema global.
  - `EnvelopeEditorOverlay.js`: Panel memodifikasi persentase dan warna *slot* anggaran.
  - `WidgetEditorOverlay.js`: Panel desain khusus antarmuka *widget* layar beranda Android.
  - `BillsOverlay.js`: Panel untuk menjadwalkan sistem tagihan berulang.

### 2.3 Komponen Kustom Interaktif
- **Collapsible Sections (Accordion)**: Di dalam editor seperti `EnvelopeEditorOverlay`, opsi-opsi yang padat dikelompokkan dalam menu tarik-turun yang rapi untuk mencegah *scrolling* tanpa batas (*infinite scrolling fatigue*).
- **Custom Color Picker & Swatches**: Aplikasi menghindari modul pihak ketiga (yang rentan *crash* di level *Native*), dan membangun komponen `SimpleColorPicker.js` mandiri dengan fitur unggulan:
  - **Manual HEX Input**: Kolom teks berawalan `#` yang siap menerima *copy-paste* kode warna presisi.
  - **Palet Warna Terakhir (Swatches Global)**: Otomatis membaca dan menyimpan 8 warna terakhir yang diklik ke dalam SQLite (`recent_colors`). Warna-warna ini akan muncul persisten lintas *overlay*.
  - **Performa Responsif**: Mengutilisasi *event* `onValueChange` murni untuk animasi pratinjau warna secara aktual, sementara penyimpanan ke basis data ditunda dan dilempar secara eksklusif ke *event* `onSlidingComplete` agar jari pengguna tidak patah-patah saat menggeser.
- **Tombol Penyelamat ("Reset to Default")**: Sebagai proteksi psikologis UX, semua panel kustomisasi ekstrem (seperti Editor Widget atau Envelope) dipersenjatai dengan tombol *Ghost/Outline* bertuliskan "Reset to Default". Tombol ini seketika mengembalikan bentuk/estetika elemen ke pengaturan bawaan awal yang solid jika pengguna merasa salah mendesain.
- **Image Handling**: Latar belakang yang diunggah dikelola dengan fungsi `Dim Light` (lapisan `rgba` hitam di atas gambar utama). Ini memastikan teks indikator keuangan selalu mudah dibaca terlepas dari seberapa terang foto yang diunggah pengguna.

## 3. Alur Logika Tampilan (State-to-View)

### 3.1 Pratinjau Waktu Nyata (Live Preview)
Semua halaman editor didesain untuk memiliki "Live Preview Sticky Card" di bagian atas halaman.
- **Mekanisme**: Komponen membaca variabel *state* lokal React (seperti `colorHex`, `borderRadius`, `padding`) secara aktual. Setiap kali penggeser digerakkan, *state* lokal berubah dan React memicu proses *re-render* secara seketika pada komponen *preview*, memberi *feedback* visual kilat kepada pengguna.
- **Pembatasan Loop (Memoization)**: Terutama di editor *widget* (yang memuat *Native Bridge* di bawah tenda), fungsi *render* dibungkus menggunakan `useCallback` atau deklarasi `'use no memo';` untuk mencegah *memory leak* dari kompilasi React (*React Compiler loop crash*).

### 3.2 Sinkronisasi Antarmuka & Penyimpanan
Saat pengguna menekan "Save", data *state* lokal milik *Overlay* dilempar kembali ke `EnvelopeContext` (melalui fungsi seperti `updateEnvelope` atau `updateWidgetConfig`). Konteks utama kemudian memperbarui diri, memaksa `DashboardScreen` untuk menggambar ulang antarmukanya dengan pengaturan/desain baru, sambil menyimpannya di SQLite (*backend* lokal) secara paralel.
