# Daftar Tech Debt & Isu Kode: BuckManagerApp

Dokumen ini merangkum *Technical Debt* (Utang Teknis) serta beberapa celah/isu kode yang telah diidentifikasi selama siklus pengembangan BuckManager. Hal ini bertujuan sebagai catatan untuk perbaikan di rilis masa depan.

## Area Risiko Kritis & Temuan QC
Meskipun aplikasinya dirancang dengan baik, ada beberapa titik lemah (*tech debt*) yang berpotensi menyebabkan aplikasi *crash*, data korup, atau kerugian finansial di masa depan.

- **[Telah Diselesaikan] Risiko Database Lock (Race Conditions)**: Pembaruan UI yang sebelumnya langsung memicu `db.runAsync()` kini telah dibungkus menggunakan `lodash.debounce` (jeda 500ms), sehingga I/O SQLite aman dari tumpukan instruksi yang berpotensi merusak fail `buckmanager.db`.
- **[Telah Diselesaikan] Ancaman Kebocoran Memori (Widget Engine)**: Ketergantungan fungsi yang memicu *infinite loop* pada Native Bridge akibat React Compiler telah dirombak. Logika pengolahan Base64 dan `formatIDR` dipindah ke luar *render cycle* sehingga komponen `EnvelopesWidget.js` kini sepenuhnya statis dan stabil.
- **[Telah Diselesaikan] Keamanan Ekosistem (Client-Side Validation)**: Data ekonomi virtual (*Ad Tickets*, *Premium Unlock*) telah dimigrasikan sepenuhnya dari SQLite lokal ke `expo-secure-store` yang terenkripsi oleh *Keychain/Keystore* OS Android, mengamankannya dari serangan akses *root*.
- **[Telah Diselesaikan] Bom Waktu Dependensi**: `expo-file-system/legacy` telah dihapus secara menyeluruh dari kode sumber (`WidgetEditorOverlay`, `WidgetTask`, `GoogleDriveService`) dan diganti menggunakan API standar `expo-file-system` untuk menjamin kompatibilitas masa depan.
- **[Telah Diselesaikan] Keamanan Sesi & Pemulihan Cloud**: Kredensial sesi pengguna Google OAuth (`user_session` berisi `accessToken` dan `refreshToken`) telah dimigrasikan dari database SQLite lokal ke `expo-secure-store`. Kami juga mengimplementasikan *Authorization Code Exchange* dengan token refresh otomatis guna mencegah kegagalan Google Drive Sync setelah satu jam token kedaluwarsa.

---

## 1. Detail Isu Kestabilan Komponen (Frontend & Native)

### 1.1 Inkompatibilitas React Compiler (`'use no memo;'`)
- **Masalah**: Fitur React Compiler (Babel *plugin* tingkat lanjut) yang hadir pada versi Expo/React terbaru terdeteksi bentrok dengan renderisasi komponen khusus *Native Bridge*, seperti yang ada pada `EnvelopesWidget.js`. Kompilasi otomatis memo menyebabkan aplikasi *crash* atau *widget* gagal dirender.
- **Penyelesaian Saat Ini**: Logika manipulasi data dinamis (seperti penggabungan *string* Base64 dan fungsi *formatter*) telah diekstrak ke luar dari badan komponen `EnvelopesWidget.js`. Pendekatan fungsional murni ini terbukti menstabilkan *widget* dan menghindari *infinite render loop* dari *React Compiler*. Arahan `'use no memo';` tetap dipertahankan sebagai langkah kehati-hatian ekstra.

### 1.2 "Infinite Loop" pada Widget Preview
- **Masalah**: Pada komponen `WidgetEditorOverlay.js`, meneruskan fungsi `renderWidget` secara langsung ke dalam `<WidgetPreview>` tanpa pembungkus dapat memicu siklus perulangan gambar (render) tak terbatas pada sisi mesin *Native* Java. Hal ini membuat aplikasi *freeze* atau memori habis seketika.
- **Penyelesaian Saat Ini**: Wajib menggunakan *hook* `useCallback` secara ketat pada `renderWidget` dengan dependensi (`config`) yang relevan untuk memastikan sisi *Native* tidak terpicu terus-menerus.

### 1.3 Kerapuhan Komponen Pihak Ketiga (Color Picker)
- **Masalah**: Library bawaan seperti `react-native-color-picker` memiliki cacat arsitektur pada versi rilis Android yang modern, menyebabkan *crash* murni (`SIGSEGV` atau kegagalan hierarki `View`) saat dikompilasi secara *Release* (berjalan lancar di sesi *Dev*).
- **Penyelesaian Saat Ini**: Seluruh pustaka tersebut dihapus dan diganti secara total menggunakan mekanisme mandiri berbasis *Slider* (`@react-native-community/slider`) serta kalkulasi pengubah warna (HSV ke HEX).

## 2. Isu Pengolahan Data & File System

### 2.1 Anomali Pembacaan Base64 (Gambar Widget)
- **Masalah**: Komponen `ImageWidget` (Java sisi *Native*) dirancang dengan mekanisme pembacaan URI primitif. Jika kita mengirimkan *string* *base64* mentah hasil dari File System, Java menganggapnya sebagai URL `http` biasa, gagal secara senyap (*Silent MalformedURLException*), dan merender `null` (layar kosong/hanya menampilkan warna hijau).
- **Penyelesaian Saat Ini**: Kode wajib melakukan pengecekan `startsWith('data:')` dan menyuntikkan *prefix* manual (`data:image/jpeg;base64,...`) ke dalam memori *state* sebelum diserahkan ke komponen `ImageWidget`.

### 2.2 Dependensi Modul File System Lama
- **Masalah (Tech Debt)**: Aplikasi sebelumnya mengimpor `expo-file-system/legacy` secara eksplisit guna menghindari beberapa *bug* pada rilis eksperimental terbaru *file system* terkait *bundling base64* Android 13+.
- **Penyelesaian Saat Ini [Resolved]**: Pustaka usang `expo-file-system/legacy` telah sepenuhnya dihapus. Seluruh operasi pembacaan base64 dan penulisan backup kini memakai API standar `expo-file-system` yang mutakhir dan stabil untuk *update* Expo SDK masa depan.

## 3. Skalabilitas & Arsitektur

### 3.1 Transaksi SQLite Tanpa Antrean (Race Conditions)
- **Masalah (Tech Debt)**: Di dalam `EnvelopeContext.js`, operasi penulisan (*Write*) ke `db.runAsync` sering kali tereksekusi bersamaan dengan perubahan UI yang cepat (misal: menggeser-geser *Slider* warna). Ini berpotensi memicu masalah penguncian tabel basis data (*database locks*) atau *race conditions*.
- **Penyelesaian Saat Ini [Resolved]**: Telah diimplementasikan integrasi `lodash.debounce` dengan jeda 500ms pada seluruh fungsi penulis UI-ke-DB (`saveBackgroundToDB`, `saveEnvelopesToDB`, dll) di dalam `EnvelopeContext.js`. UI kini mulus secara sinkron sementara eksekusi I/O SQLite ditunda dan diamankan dari benturan.

### 3.2 Sistem "Keuangan" Virtual Rentan Dimanipulasi
- **Masalah**: Sistem monetisasi (*Ad Tickets*, *Premium Unlock*) dan *Daily Streak* tervalidasi sepenuhnya melalui logika JavaScript dan basis data lokal (SQLite) pada perangkat klien (*Client-side validation*).
- **Penyelesaian Saat Ini [Resolved]**: Basis penyimpanan data status Ekonomi Premium dan Tiket Iklan telah dimigrasikan keluar dari SQLite ke `expo-secure-store`. Penyimpanan kini terenkripsi di *Keystore/Keychain* perangkat keras yang mencegah pengguna yang melakukan modifikasi paksa (*rooted user*) dari akses atau manipulasi data sepihak. Ini menjadi pilar awal sebelum aplikasi menggunakan server otentikasi eksternal sesungguhnya.

### 3.3 Kebocoran Token Google Drive & Kedaluwarsa Sesi
- **Masalah**: Token Google OAuth disimpan dalam teks polos pada database SQLite lokal, yang juga diunggah ke Google Drive sebagai cadangan. Selain itu, tidak ada mekanisme penyegaran (*refresh*) token, sehingga sesi sinkronisasi cloud terputus setelah satu jam (token kedaluwarsa).
- **Penyelesaian Saat Ini [Resolved]**: Sesi dipindahkan ke `expo-secure-store` yang terenkripsi. Alur login diubah menggunakan *Authorization Code Flow* dengan PKCE untuk memperoleh *Refresh Token*, dan ditambahkan logika penyegaran otomatis di `AuthContext.js` sebelum setiap panggilan I/O Google Drive.
