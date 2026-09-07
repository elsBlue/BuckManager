# Ringkasan Arsitektur & Tech Stack: BuckManagerApp

Dokumen ini merangkum arsitektur utama dan tumpukan teknologi (Tech Stack) yang digunakan dalam pengembangan aplikasi **BuckManager**.

## 1. Tech Stack (Tumpukan Teknologi)

### Kerangka Kerja (Framework) & Inti
- **React Native**: Framework utama untuk membangun aplikasi *mobile* lintas platform.
- **Expo**: Platform ekosistem di sekitar React Native yang digunakan untuk mempercepat pengembangan, *build* (EAS Build), dan akses API perangkat (Kamera, File System, SQLite).
- **JavaScript**: Bahasa pemrograman utama yang digunakan (berbasis standar ES6+).

### Antarmuka Pengguna (UI) & Desain
- **NativeWind (Tailwind CSS)**: Digunakan untuk *styling* antarmuka secara *inline* dengan kelas utilitas gaya Tailwind, memberikan desain yang konsisten dan responsif.
- **Expo Vector Icons**: Kumpulan ikon SVG (menggunakan koleksi *Ionicons*).
- **Komponen Kustom**: Komponen *overlay/modal* seperti `EnvelopeEditorOverlay`, `WidgetEditorOverlay`, `SettingsOverlay` untuk memberikan pengalaman pengguna jenis SPA (Single Page Application).

### Penyimpanan Lokal & Database
- **SQLite (`expo-sqlite`)**: Digunakan sebagai basis data utama aplikasi (melalui `src/database/db.js`). Semua data seperti konfigurasi *envelope/slot*, preferensi tema global, riwayat login harian (*streak*), dan konfigurasi *widget* disimpan secara lokal.
- **Expo Secure Store (`expo-secure-store`)**: Digunakan untuk penyimpanan data sensitif yang terenkripsi oleh *Keychain/Keystore* OS (seperti token Google Drive `user_session`, tiket iklan `ad_tickets`, dan status premium `is_premium`).
- **Expo File System**: Digunakan untuk membaca direktori lokal perangkat, khususnya mengambil *string base64* dari gambar yang dipilih oleh pengguna untuk disimpan ke dalam database.

### Integrasi Sistem & Layanan Eksternal
- **Google Drive REST API**: Layanan sinkronisasi *cloud* opsional yang mengunggah (`upload`) dan memulihkan (`download`) berkas `buckmanager.db` langsung dari akun Google pengguna untuk perlindungan data lintas-perangkat.
- **React Native Android Widget (`react-native-android-widget`)**: Library utama yang digunakan untuk membuat *Home Screen Widget* Android secara langsung menggunakan React Native.
- **Expo Background Tasks & Notifications**: Digunakan untuk memperbarui status *widget* secara asinkron di latar belakang (melalui `WidgetTask.js`) serta mengatur penjadwalan alarm pengingat.

## 2. Arsitektur Aplikasi

### Pendekatan "Single Page with Overlays"
Aplikasi tidak banyak menggunakan pustaka navigasi yang berat (seperti React Navigation dengan banyak tumpukan layar). Sebaliknya, interaksi utama berpusat pada satu dasbor (*DashboardScreen*), dan menu-menu lainnya muncul sebagai hamparan/modal dari bawah (*bottom sheet*).
- **`DashboardScreen.js`**: Pusat aplikasi yang menampilkan kartu sisa saldo (Needs, Wants, Savings) dan latar belakang dinamis.
- **`/components/*Overlay.js`**: Komponen terpisah untuk mengatur bagian spesifik (contoh: `SettingsOverlay`, `MonetizationOverlay`).

### State Management (Manajemen Status)
- **Auth Context (`AuthContext.js`)**: Mengelola status autentikasi pengguna (`isAuthenticated`, `user`), persistensi sesi via `SecureStore`, serta logika pertukaran kode otentikasi Google OAuth dengan PKCE dan penyegaran token otomatis (`getValidToken`).
- **Envelope Context (`EnvelopeContext.js`)**: Bertindak sebagai *store* utama untuk keuangan. Konteks ini memuat semua logika inti:
  - Sinkronisasi dengan database SQLite.
  - Memori *state* sesi saat ini (daftar envelop, pengaturan tema global, *streak*, akses premium dari `SecureStore`).
  - Metode `update*` untuk menyimpan ke *state* lokal dan menyinkronkannya kembali ke basis data.
- **Poling/Reload**: Fungsi `reloadAll()` di dalam Context dipanggil di awal untuk memuat seluruh tabel `settings` dari SQLite ke dalam memori JavaScript.

### Arsitektur Widget Latar Belakang
- Pengguna dapat merancang *widget* langsung melalui aplikasi JS (`WidgetEditorOverlay`).
- Konfigurasi desain (warna *hex*, *base64* gambar, jari-jari border) disimpan ke SQLite.
- Konfigurasi ini dibaca kembali oleh `WidgetTask.js` di luar *lifecycle* React komponen biasa (di latar belakang), lalu di-*render* menjadi *RemoteViews* natif Android oleh modul *Widget Factory* (`EnvelopesWidget.js`).

## 3. Sistem Fitur Tambahan
- **Sistem Hadiah / Streak**: Pelacakan aktivitas (*daily login*) yang disimpan di DB (`streak_data`). Jika pengguna rajin masuk 7 hari berturut-turut, sistem memberikan "Ad Tickets".
- **Sistem Premium**: Fitur kosmetik dan kebebasan desain dikunci oleh variabel `isCustomizationLocked`. Pengguna dapat membukanya baik melalui status Premium permanen maupun langganan berjangka waktu (Time-based expiry).
- **Efek Partikel**: Aplikasi menggunakan efek kanvas/rendering dinamis mandiri untuk estetika seperti `ParticleStarfall` dan `ParticleLines`.
