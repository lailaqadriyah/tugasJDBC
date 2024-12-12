import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

// Kelas untuk menangani koneksi database
class DatabaseConnection {
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/JDBC"; 
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "101104"; 

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Koneksi ke database gagal: " + e.getMessage());
            return null;
        }
    }
}

// Kelas Utama (Main Program)
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection conn = null;

        try {
            // Koneksi ke database menggunakan DatabaseConnection
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.out.println("Tidak dapat terhubung ke database. Program berhenti.");
                return;
            }
            System.out.println("Koneksi ke database berhasil.");

            // Login Section
            boolean loggedIn = login(scanner);

            if (!loggedIn) {
                System.out.println("Login gagal. Program berhenti.");
                return;
            }

            // Menu utama setelah login
            String pilihanMenu;
            do {
                System.out.println("\n===== Menu =====");
                System.out.println("1. Melihat Data Barang");
                System.out.println("2. Tambah Data Barang");
                System.out.println("3. Hapus Data Barang");
                System.out.println("4. Edit Data Barang");
                System.out.println("5. Tambah Transaksi");
                System.out.println("6. Keluar");
                System.out.print("Pilih menu: ");

                pilihanMenu = scanner.nextLine();

                switch (pilihanMenu) {
                    case "1":
                        viewDataBarang(conn);
                        break;
                    case "2":
                        addDataBarang(conn, scanner);
                        break;
                    case "3":
                        deleteDataBarang(conn, scanner);
                        break;
                    case "4":
                        editDataBarang(conn, scanner);
                        break;
                    case "5":
                        addTransaksi(conn, scanner);
                        break;
                    case "6":
                        System.out.println("Terima kasih telah menggunakan program ini.");
                        break;
                    default:
                        System.out.println("Pilihan tidak valid! Silakan coba lagi.");
                        break;
                }

            } while (!pilihanMenu.equals("6"));

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean login(Scanner scanner) {
        System.out.println("+-----------------------------------------------------+");
        System.out.println("Log in");

        System.out.print("Username : ");
        String username = scanner.nextLine().trim();
        System.out.print("Password : ");
        String password = scanner.nextLine().trim();

                // Generate captcha
        Random random = new Random();
        int captcha = random.nextInt(9000) + 1000; // Generate angka 4 digit
        System.out.println("Captcha: " + captcha);
        System.out.print("Masukkan captcha: ");
        int userCaptcha;
        try {
            userCaptcha = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Captcha harus berupa angka.");
            return false;
        }

        if (userCaptcha != captcha) {
            System.out.println("Captcha salah. Login gagal.");
            return false;
        }

        return username.equals("admin") && password.equals("1234");
    }

    private static void viewDataBarang(Connection conn) throws SQLException {
        String query = "SELECT * FROM barang";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n===== Daftar Barang =====");
            while (rs.next()) {
                System.out.println("Kode Barang : " + rs.getString("kode_barang"));
                System.out.println("Nama Barang : " + rs.getString("nama_barang"));
                System.out.println("Harga Barang: Rp " + rs.getDouble("harga_barang"));
                System.out.println("+----------------------------------------------------+");
            }
        }
    }

    private static void addDataBarang(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Masukkan Kode Barang: ");
        String kodeBarang = scanner.nextLine();

        // Cek apakah kode_barang sudah ada
        String checkQuery = "SELECT COUNT(*) FROM barang WHERE kode_barang = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, kodeBarang);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Kode barang sudah ada. Gunakan kode barang yang berbeda.");
                    return;
                }
            }
        }

        System.out.print("Masukkan Nama Barang: ");
        String namaBarang = scanner.nextLine();

        System.out.print("Masukkan Harga Barang: ");
        double hargaBarang = Double.parseDouble(scanner.nextLine());

        String query = "INSERT INTO barang (kode_barang, nama_barang, harga_barang) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, kodeBarang);
            pstmt.setString(2, namaBarang);
            pstmt.setDouble(3, hargaBarang);
            pstmt.executeUpdate();
            System.out.println("Data barang berhasil ditambahkan.");
        }
    }

    private static void deleteDataBarang(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Masukkan Kode Barang yang ingin dihapus: ");
        String kodeBarang = scanner.nextLine();

        String query = "DELETE FROM barang WHERE kode_barang = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, kodeBarang);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data barang berhasil dihapus.");
            } else {
                System.out.println("Kode barang tidak ditemukan.");
            }
        }
    }

    private static void editDataBarang(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Masukkan Kode Barang yang ingin diedit: ");
        String kodeBarang = scanner.nextLine();

        System.out.print("Masukkan Nama Baru: ");
        String namaBaru = scanner.nextLine();

        System.out.print("Masukkan Harga Baru: ");
        double hargaBaru = Double.parseDouble(scanner.nextLine());

        String query = "UPDATE barang SET nama_barang = ?, harga_barang = ? WHERE kode_barang = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, namaBaru);
            pstmt.setDouble(2, hargaBaru);
            pstmt.setString(3, kodeBarang);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data barang berhasil diperbarui.");
            } else {
                System.out.println("Kode barang tidak ditemukan.");
            }
        }
    }

    private static void addTransaksi(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Masukkan No Faktur: ");
        String noFaktur = scanner.nextLine();

        System.out.print("Masukkan Kode Barang: ");
        String kodeBarang = scanner.nextLine();

        System.out.print("Masukkan Jumlah Beli: ");
        int jumlahBeli = Integer.parseInt(scanner.nextLine());

        System.out.print("Masukkan Nama Pelanggan: ");
        String namaPelanggan = scanner.nextLine();

        System.out.print("Masukkan Nomor HP: ");
        String nomorHp = scanner.nextLine();

        System.out.print("Masukkan Alamat: ");
        String alamat = scanner.nextLine();

        // Ambil harga barang dari database
        String getPriceQuery = "SELECT harga_barang FROM barang WHERE kode_barang = ?";
        double hargaBarang;
        try (PreparedStatement pstmt = conn.prepareStatement(getPriceQuery)) {
            pstmt.setString(1, kodeBarang);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    hargaBarang = rs.getDouble("harga_barang");
                } else {
                    System.out.println("Kode barang tidak ditemukan.");
                    return;
                }
            }
        }

        double totalHarga = hargaBarang * jumlahBeli;
        Date tanggalTransaksi = new Date();
        java.sql.Date sqlDate = new java.sql.Date(tanggalTransaksi.getTime());

        // Simpan transaksi ke database
        String query = "INSERT INTO transaksi (no_faktur, kode_barang, jumlah_beli, total_harga, nama_pelanggan, nomo_hp, alamat) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, noFaktur);
            pstmt.setString(2, kodeBarang);
            pstmt.setInt(3, jumlahBeli);
            pstmt.setDouble(4, totalHarga);
            pstmt.setString(5, namaPelanggan);
            pstmt.setString(6, nomorHp);
            pstmt.setString(7, alamat);
            pstmt.executeUpdate();
            System.out.println("Transaksi berhasil ditambahkan.");
        }

        // Cetak struk
        cetakStruk(noFaktur, namaPelanggan, nomorHp, alamat, kodeBarang, hargaBarang, jumlahBeli, totalHarga);
    }

    public static void cetakStruk(String noFaktur, String namaPelanggan, String nomorHp, String alamat, String kodeBarang, double hargaBarang, int jumlahBeli, double totalHarga) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a z");
        Date now = new Date();

        System.out.println("\n======================================================");
        System.out.println("                     STRUK PEMBELIAN                 ");
        System.out.println("                  Supermarket Maju Jaya                       ");
        System.out.println("Tanggal : " + dateFormat.format(now));
        System.out.println("Waktu   : " + timeFormat.format(now));
        System.out.println("======================================================");
        System.out.println("DATA PELANGGAN");
        System.out.println("Nama Pelanggan : " + namaPelanggan);
        System.out.println("No. HP         : " + nomorHp);
        System.out.println("Alamat         : " + alamat);
        System.out.println("======================================================");
        System.out.println("DATA PEMBELIAN BARANG");
        System.out.println("No. Faktur     : " + noFaktur);
        System.out.println("Kode Barang    : " + kodeBarang);
        System.out.println("Harga Barang   : " + hargaBarang);
        System.out.println("Jumlah Beli    : " + jumlahBeli);
        System.out.println("TOTAL BAYAR    : " + totalHarga);
        System.out.println("======================================================");
        System.out.println("Kasir          : Laila");
        System.out.println("======================================================");
    }
}