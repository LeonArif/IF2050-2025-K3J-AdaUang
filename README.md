# AdaUang

## Deskripsi Aplikasi

AdaUang adalah suatu sistem untuk membantu perusahaan dalam mencatat kontrak pinjaman, memantau status pembayaran nasabah, serta mengelola resiko kredit secara lebih terstruktur dengan mengotomatisasi pembuatan laporan aging piutang secara berkala.


## Anggota Kelompok

| Nama | NIM |
|------|-----|
| Nicholas Zefanya Lamtyo Nababan | 18223111 |
| Leonard Arif Sutiono | 18223120 |
| Harfhan Ikhtiar Ahmad Ridzky | 18223123 |
| M Azizdzaki Khrisnanurmuflih | 18223128 |
| Izhar Alif Akbar | 18223129 |
| Muhammad Rafly Fauzan | 18223132 |


## Cara Menjalankan Aplikasi

### Prasyarat
- Java Development Kit (JDK) 11 atau versi yang lebih baru
- Gradle 7.0 atau versi yang lebih baru
- Database (MySQL/PostgreSQL/H2)

### Langkah Instalasi

1. **Clone Repository**
   Lakukan Clone Repository, Lalu masuk ke dalam folder repository tersebut

2. **Konfigurasi SQL**
   Pada file app/src/main/java/config/DatabaseConfig.java
   Line 42
   Ubah Password dengan password SQL anda

3. **Build Aplikasi**
   ```bash
   ./gradlew build
   ```


4. **Menjalankan Aplikasi**
   ```bash
   ./gradlew run
   ```


## Daftar Modul yang Diimplementasi

### 1. Modul Autentikasi
- **Dibuat Oleh**: Izhar Alif Akbar
- **Deskripsi**: Sistem login dan manajemen pengguna
- **Fitur**: Login, logout, session management, role-based access


### 2. Modul Kontrak Pinjaman
- **Dibuat Oleh**: Izhar Alif Akbar & Leonard Arif Sutiono
- **Deskripsi**: Mencatat dan mengelola kontrak pinjaman
- **Fitur**: Pembuatan kontrak


### 3. Modul Cicilan
- **Dibuat Oleh**: Nicholas Zefanya Lamtyo Nababan & M Azizdzaki Khrisnanurmuflih
- **Deskripsi**: Mengelola pembayaran cicilan nasabah
- **Fitur**: Input cicilan, tracking pembayaran, status cicilan


### 4. Modul Laporan Umur Piutang
- **Dibuat Oleh**: Muhammad Rafly Fauzan & Harfhan Ikhtiar Ahmad Ridzky
- **Deskripsi**: Menghasilkan laporan aging piutang otomatis
- **Fitur**: Laporan berkala, klasifikasi umur piutang


## Daftar Tabel Basis Data

### 1. Tabel `users`
| Kolom | Tipe Data | Keterangan |
|-------|-----------|------------|
| `id_user` | int | PRIMARY KEY, AUTO_INCREMENT |
| `username` | varchar(50) | Username untuk login |
| `fullname` | varchar(100) | Nama lengkap pengguna |
| `password` | varchar(255) | Password terenkripsi |
| `branch` | varchar(50) | Cabang/lokasi kerja |
| `role` | varchar(20) | Role pengguna (admin, staff, manajemen perusahaan) |

### 2. Tabel `kontrak`  
| Kolom | Tipe Data | Keterangan |
|-------|-----------|------------|
| `id_kontrak` | int | PRIMARY KEY, AUTO_INCREMENT |
| `nama_user` | varchar(100) | Nama pengguna kontrak |
| `total` | int | Total nilai kontrak |
| `tenor` | int | Jangka waktu kontrak (bulan) |
| `jumlah_bayar` | int | Jumlah yang harus dibayar |
| `jumlah_bayar_bunga` | int | Jumlah bunga yang harus dibayar |
| `cicilan_per_bulan` | int | Nominal cicilan bulanan |
| `status` | tinyint(1) | Status kontrak (aktif/tidak) |
| `tanggal_pinjam` | date | Tanggal mulai kontrak |
| `id_user` | int | FOREIGN KEY ke tabel users |

### 3. Tabel `cicilan`
| Kolom | Tipe Data | Keterangan |
|-------|-----------|------------|
| `id_cicilan` | int | PRIMARY KEY, AUTO_INCREMENT |
| `id_kontrak` | int | FOREIGN KEY ke tabel kontrak |
| `tenor` | int | Cicilan ke-n |
| `jumlah_cicilan` | int | Nominal cicilan |
| `tanggal_cicilan` | date | Tanggal jatuh tempo cicilan |
| `id_staff` | int | FOREIGN KEY ke tabel users |

