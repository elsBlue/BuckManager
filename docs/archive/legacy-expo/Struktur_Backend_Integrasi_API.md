# Struktur Backend & Integrasi API: BuckManagerApp

Dokumen ini menjelaskan struktur layanan *backend*, penyimpanan data, serta integrasi API eksternal yang digunakan dalam ekosistem aplikasi **BuckManager**.

## 1. Konsep "Offline-First" Backend
Aplikasi BuckManager pada dasarnya mengadopsi pendekatan **Offline-First**. Artinya, aplikasi tidak bergantung pada *server* eksternal terpusat (seperti Node.js, Firebase, atau layanan Cloud lainnya) untuk menyimpan data utama pengguna. 

Sebagai gantinya, seluruh "Backend" berjalan secara lokal di perangkat pengguna menggunakan **SQLite**.

### 1.1 Struktur Database Lokal (SQLite)
Basis data dikelola melalui `expo-sqlite` (di dalam `src/database/db.js`).

- **Tabel Tunggal Sentralistik**: Aplikasi menggunakan sebuah tabel *key-value pair* sederhana bernama `settings` alih-alih skema relasional yang kompleks.
  - Skema: `CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT);`
- **Penyimpanan Objek JSON**: Nilai-nilai konfigurasi kompleks (seperti `envelopes_config`, `widget_config`, `header_cards_config`) dikonversi menjadi *string* JSON sebelum disimpan ke dalam kolom `value`.
- **Eksekusi Asinkron**: Komunikasi antara lapisan antarmuka pengguna (Frontend) dengan SQLite (Backend lokal) diatur sepenuhnya menggunakan fungsi asinkron (`async/await`) untuk mencegah pemblokiran UI saat membaca atau menulis (*I/O operations*).

## 2. Alur Data (Data Flow)

Alur komunikasi data di dalam aplikasi melewati beberapa lapisan berikut:

1. **User Interface (UI)**: Komponen React Native menerima aksi dari pengguna (seperti mengubah target tabungan atau memodifikasi widget).
2. **Context Layer (State)**: UI memicu pembaruan pada `EnvelopeContext.js`. Konteks ini langsung memperbarui memori di tingkat sesi (React State) agar UI merespons seketika tanpa jeda (seperti *optimistic updates*).
3. **Database Layer (SQLite)**: Bersamaan dengan pembaruan *state* di atas, Context memanggil `db.runAsync()` untuk menyimpan format JSON terbaru ke dalam tabel lokal.
4. **Widget Bridge (Opsional)**: Untuk fitur *Home Screen Widget*, data yang tersimpan di SQLite dapat dibaca langsung oleh layanan *background* (`WidgetTask.js`) di tingkat platform Android murni, menembus lapisan React Native utama.

## 3. Integrasi API & Layanan Eksternal

### 3.1 Otentikasi & Otorisasi (Google OAuth 2.0 & Token Refresh)
Aplikasi memanfaatkan modul `expo-auth-session/providers/google` dengan alur **Authorization Code Flow** dengan **PKCE** (Proof Key for Code Exchange).
- **Mendapatkan Refresh Token**: Alur login diatur dengan opsi `access_type: 'offline'` dan `prompt: 'consent'` untuk memaksa Google mengirimkan `refreshToken` saat kode otentikasi dipertukarkan di sisi klien (`exchangeCodeAsync`).
- **Penyimpanan Aman (Secure Store)**: Sesi pengguna (`user_session` berisi `accessToken`, `refreshToken`, dan `expiresAt`) disimpan menggunakan **`expo-secure-store`** untuk melindunginya dari akses root, bukan lagi disimpan di SQLite dalam bentuk teks polos.
- **Penyegaran Otomatis (Silent Refresh)**: Di dalam `AuthContext.js`, fungsi `getValidToken()` mendeteksi masa berlaku token. Jika token akan habis (< 5 menit), aplikasi secara diam-diam memanggil Google Token Endpoint untuk memperbarui `accessToken` sebelum I/O Google Drive dijalankan.

### 3.2 Google Drive API (Cloud Backup & Sync)
Aplikasi memiliki fitur pencadangan (Backup) penuh menggunakan **Google Drive REST API v3**. Karena ukuran basis data sangat kecil, alih-alih menyinkronkan data baris per baris, aplikasi ini mengunggah seluruh berkas `buckmanager.db` murni ke ruang penyimpanan aplikasi tersembunyi (`appDataFolder`) di akun Google Drive pengguna (melalui `src/services/GoogleDriveService.js`).
- **REST Fetch**: Menggunakan fungsi standar `fetch` berbekal `accessToken` yang valid (didapatkan melalui `getValidToken()` dari context) untuk berinteraksi dengan *endpoint* Google APIs.
- **Atomic Replacement**: Saat mengunduh cadangan, aplikasi memastikan berkas sementara diunduh dan diverifikasi integritas SQLite-nya sebelum menimpa berkas asli (`DB_PATH`) dan menghapus *cache* (`-wal`, `-shm`) untuk mencegah korupsi.

### 3.3 Expo API & Sistem Integrasi Perangkat Layar
- **`expo-file-system`**: API untuk mengelola berkas lokal, khususnya memuat, membaca, dan mengubah *string base64* dari gambar yang ditambahkan pengguna.
- **`expo-image-picker`**: API untuk menjembatani akses galeri media perangkat.
- **`expo-notifications`** (via `NotificationService.js`): API yang digunakan untuk sistem *Local Notification*. Mengatur penjadwalan alarm dan pengingat *streak* (login harian) pengguna langsung dari dalam perangkat tanpa perlu layanan *Push Notification* eksternal (FCM/APNs).

### 3.3 Integrasi "Task Manager" (Latar Belakang)
- **`expo-task-manager` / `react-native-android-widget`**: Sebuah mekanisme API untuk mendaftarkan proses yang dapat berjalan bahkan ketika aplikasi utama ditutup. API ini mendengarkan sinyal/interupsi dari OS Android ketika *widget* layar utama (*home screen*) meminta pembaruan data secara berkala.

### 3.4 Monetisasi (Secure Virtual Economy)
Saat ini, logika "Pembelian" atau "Monetisasi" (seperti mendapat *Ad Tickets* atau *Premium Unlock*) dilakukan **secara lokal (mock-up/virtual)**. 
- Tidak ada integrasi dengan *Payment Gateway* eksternal (seperti Google Play Billing, Stripe, atau Midtrans) di dalam kode sumber saat ini. 
- "Backend" untuk hal ini semata-mata dikendalikan oleh sistem batas waktu (berbasis `Date.now()`).
- Data sensitif ekonomi virtual kini sepenuhnya ditangani menggunakan modul **`expo-secure-store`**, sehingga penyimpanan *isPremium* dan *adTickets* terlindungi oleh enkripsi KeyStore/Keychain bawaan OS Android alih-alih diletakkan dalam *plain text* di SQLite. Ini mencegah para pengguna berakses *root* untuk memodifikasi basis data sembarangan.

---
*Catatan: Kehadiran Google Drive API di atas telah memecahkan masalah ketiadaan "Sinkronisasi Cloud". Pengguna kini dapat memindahkan basis data antar-perangkat secara mandiri meskipun aplikasi tidak mengandalkan arsitektur server relasional.*
