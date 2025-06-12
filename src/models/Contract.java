package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Contract extends BaseModel {
    private int id_kontrak;
    private String nama_user;
    private int total;
    private int tenor;
    private int jumlah_bayar;
    private int jumlah_bayar_bunga;
    private int cicilan_per_bulan;
    private boolean status;
    private Date tanggal_pinjam;
    private int id_user;

    // Properti tambahan dari join dengan tabel 'users'
    private String username;
    private String branch;

    public Contract() {}

    @Override
    public boolean save() {
        try {
            if (isNewRecord()) {
                return insert();
            } else {
                // logika update
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean insert() throws SQLException {
        // Perbaiki: Hitung jumlah_bayar_bunga dari total, bukan jumlah_bayar
        this.jumlah_bayar_bunga = (int) Math.round(this.total * 1.1);
        this.cicilan_per_bulan = this.tenor != 0 ? this.jumlah_bayar_bunga / this.tenor : 0;

        String sql = "INSERT INTO kontrak (nama_user, total, tenor, jumlah_bayar, jumlah_bayar_bunga, cicilan_per_bulan, status, tanggal_pinjam, id_user) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, this.nama_user);
            stmt.setInt(2, this.total);
            stmt.setInt(3, this.tenor);
            stmt.setInt(4, this.jumlah_bayar);
            stmt.setInt(5, this.jumlah_bayar_bunga);
            stmt.setInt(6, this.cicilan_per_bulan);
            stmt.setBoolean(7, this.status);
            stmt.setDate(8, new java.sql.Date(this.tanggal_pinjam.getTime()));
            stmt.setInt(9, this.id_user);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id_kontrak = generatedKeys.getInt(1);
                        this.id = this.id_kontrak;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete() {
        // Logika hapus
        return false;
    }

    @Override
    public boolean isNewRecord() {
        return id_kontrak == 0;
    }

    public static List<Contract> findAll() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT k.*, u.username, u.branch FROM kontrak k " +
                "JOIN users u ON k.id_user = u.id_user " +
                "ORDER BY k.id_kontrak ASC";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                contracts.add(createFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading all contracts: " + e.getMessage());
            e.printStackTrace();
        }
        return contracts;
    }

    private static Contract createFromResultSet(ResultSet rs) throws SQLException {
        Contract contract = new Contract();
        contract.setId_kontrak(rs.getInt("id_kontrak"));
        contract.setNama_user(rs.getString("nama_user"));
        contract.setTotal(rs.getInt("total"));
        contract.setTenor(rs.getInt("tenor"));
        contract.setJumlah_bayar(rs.getInt("jumlah_bayar"));
        contract.setJumlah_bayar_bunga(rs.getInt("jumlah_bayar_bunga"));
        contract.setCicilan_per_bulan(rs.getInt("cicilan_per_bulan"));
        contract.setStatus(rs.getBoolean("status"));
        contract.setTanggal_pinjam(rs.getDate("tanggal_pinjam"));
        contract.setId_user(rs.getInt("id_user"));
        contract.setUsername(rs.getString("username"));
        contract.setBranch(rs.getString("branch"));
        return contract;
    }

    public static Contract findById(int id) {
        Contract contract = null;
        String sql = "SELECT k.*, u.username, u.branch FROM kontrak k " +
                "JOIN users u ON k.id_user = u.id_user WHERE k.id_kontrak = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    contract = createFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contract;
    }

    private boolean update() throws SQLException {
    String sql = "UPDATE kontrak SET jumlah_bayar=?, status=? WHERE id_kontrak=?";
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, this.jumlah_bayar);
        stmt.setBoolean(2, this.status);
        stmt.setInt(3, this.id_kontrak);
        return stmt.executeUpdate() > 0;
    }
}

    /**
     * Ambil semua kontrak yang masih aktif (belum lunas)
     */
    public static List<Contract> findAllAktif() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT k.*, u.username, u.branch FROM kontrak k " +
                "JOIN users u ON k.id_user = u.id_user " +
                "WHERE k.status = 0 " + 
                "ORDER BY k.id_kontrak ASC";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                contracts.add(createFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading active contracts: " + e.getMessage());
            e.printStackTrace();
        }
        return contracts;
    }

    /**
     * Update status kontrak dan jumlah bayar
     */
    public boolean updatePayment(int newJumlahBayar, boolean isLunas) {
        this.jumlah_bayar = newJumlahBayar;
        this.status = isLunas;
        
        String sql = "UPDATE kontrak SET jumlah_bayar = ?, status = ? WHERE id_kontrak = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, this.jumlah_bayar);
            stmt.setBoolean(2, this.status);
            stmt.setInt(3, this.id_kontrak);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cek apakah kontrak sudah lunas berdasarkan jumlah tenor yang sudah dibayar
     */
    public boolean isLunasByTenor() {
        String sql = "SELECT COUNT(*) as jumlah_tenor_terbayar FROM cicilan WHERE id_kontrak = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, this.id_kontrak);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int tenorTerbayar = rs.getInt("jumlah_tenor_terbayar");
                    return tenorTerbayar >= this.tenor; // Jika tenor terbayar >= tenor maksimal
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hitung tenor selanjutnya yang harus dibayar
     */
    public int getNextTenor() {
        String sql = "SELECT COALESCE(MAX(tenor), 0) as tenor_terakhir FROM cicilan WHERE id_kontrak = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, this.id_kontrak);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int tenorTerakhir = rs.getInt("tenor_terakhir");
                    return tenorTerakhir + 1; // Tenor selanjutnya
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Default tenor pertama
    }

    /**
     * Getter untuk nama peminjam (alias untuk nama_user)
     */
    public String getNamaPeminjam() {
        return this.nama_user;
    }

    /**
     * Getter untuk ID kontrak (alias)
     */
    public int getIdKontrak() {
        return this.id_kontrak;
    }

    /**
     * Format currency untuk cicilan per bulan
     */
    public String getFormattedCicilanPerBulan() {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(this.cicilan_per_bulan);
    }

    /**
     * Get status sebagai string
     */
    public String getStatusText() {
        return this.status ? "Lunas" : "Aktif";
    }

    // Getters and Setters
    public String getFormattedTotal() {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(this.total);
    }

    public int getId_kontrak() { return id_kontrak; }
    public void setId_kontrak(int id_kontrak) { this.id_kontrak = id_kontrak; }
    public String getNama_user() { return nama_user; }
    public void setNama_user(String nama_user) { this.nama_user = nama_user; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getTenor() { return tenor; }
    public void setTenor(int tenor) { this.tenor = tenor; }
    public int getJumlah_bayar() { return jumlah_bayar; }
    public void setJumlah_bayar(int jumlah_bayar) {
        this.jumlah_bayar = jumlah_bayar;
        // Set otomatis nilai derived
        this.jumlah_bayar_bunga = (int) Math.round(total * 1.1);
        this.cicilan_per_bulan = this.tenor != 0 ? this.jumlah_bayar_bunga / this.tenor : 0;
    }
    public int getJumlah_bayar_bunga() { return jumlah_bayar_bunga; }
    public void setJumlah_bayar_bunga(int jumlah_bayar_bunga) { this.jumlah_bayar_bunga = jumlah_bayar_bunga; }
    public int getCicilan_per_bulan() { 
        return cicilan_per_bulan; 
    }

    public void setCicilan_per_bulan(int cicilan_per_bulan) { 
        this.cicilan_per_bulan = cicilan_per_bulan; 
    }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public Date getTanggal_pinjam() { return tanggal_pinjam; }
    public void setTanggal_pinjam(Date tanggal_pinjam) { this.tanggal_pinjam = tanggal_pinjam; }
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    /**
     * Getter untuk cicilan per bulan
     */
    public int getCicilanPerBulan() {
        return this.cicilan_per_bulan;
    }

    /**
     * Setter untuk cicilan per bulan
     */
    public void setCicilanPerBulan(int cicilanPerBulan) {
        this.cicilan_per_bulan = cicilanPerBulan;
    }
}