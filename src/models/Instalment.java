package models;

import database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Instalment extends BaseModel {
    private int idCicilan;
    private int idKontrak;
    private int tenor;
    private int jumlahCicilan;
    private LocalDate tanggalCicilan;
    private int idStaff;

    // Properties utk join
    private String namaPeminjam;
    private String staffName;

    // Constructors
    public Instalment() {}

    public Instalment(int idCicilan, int idKontrak, int tenor, int jumlahCicilan, 
                   LocalDate tanggalCicilan, int idStaff) {
        this.idCicilan = idCicilan;
        this.idKontrak = idKontrak;
        this.tenor = tenor;
        this.jumlahCicilan = jumlahCicilan;
        this.tanggalCicilan = tanggalCicilan;
        this.idStaff = idStaff;
    }

    @Override
    public boolean save() {
        try {
            if (isNewRecord()) {
                return insert();
            } else {
                return update();
            }
        } catch (SQLException e) {
            System.err.println("Error saving cicilan: " + e.getMessage());
            return false;
        }
    }

    private boolean insert() throws SQLException {
        String sql = "INSERT INTO cicilan (id_kontrak, tenor, jumlah_cicilan, tanggal_cicilan, id_staff) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, idKontrak);
            stmt.setInt(2, tenor);
            stmt.setInt(3, jumlahCicilan);
            stmt.setDate(4, Date.valueOf(tanggalCicilan));
            stmt.setInt(5, idStaff);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.idCicilan = generatedKeys.getInt(1);
                        this.id = this.idCicilan;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean update() throws SQLException {
        String sql = "UPDATE cicilan SET jumlah_cicilan = ?, tanggal_cicilan = ? WHERE id_cicilan = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, jumlahCicilan);
            stmt.setDate(2, Date.valueOf(tanggalCicilan));
            stmt.setInt(3, idCicilan);
            
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete() {
        if (isNewRecord()) {
            return false;
        }
        
        try {
            String sql = "DELETE FROM cicilan WHERE id_cicilan = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, idCicilan);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting cicilan: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isNewRecord() {
        return idCicilan == 0;
    }

    // Static methods for database operations
    public static List<Instalment> findAll() {
        List<Instalment> cicilanList = new ArrayList<>();
        String sql = "SELECT c.*, k.nama_user as nama_peminjam, u.fullname as staff_name " +
                    "FROM cicilan c " +
                    "JOIN kontrak k ON c.id_kontrak = k.id_kontrak " +
                    "JOIN users u ON c.id_staff = u.user_id " +
                    "ORDER BY c.tanggal_cicilan DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cicilanList.add(createFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading all cicilan: " + e.getMessage());
        }
        return cicilanList;
    }

    public static List<Instalment> findByKontrak(int idKontrak) {
        List<Instalment> cicilanList = new ArrayList<>();
        String sql = "SELECT c.*, k.nama_user as nama_peminjam, u.fullname as staff_name " +
                    "FROM cicilan c " +
                    "JOIN kontrak k ON c.id_kontrak = k.id_kontrak " +
                    "JOIN users u ON c.id_staff = u.id_user " +
                    "WHERE c.id_kontrak = ? " +
                    "ORDER BY c.tenor ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idKontrak);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cicilanList.add(createFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading cicilan by kontrak: " + e.getMessage());
        }
        return cicilanList;
    }

    public static Instalment findById(int id) {
        String sql = "SELECT c.*, k.nama_user as nama_peminjam, u.fullname as staff_name " +
                    "FROM cicilan c " +
                    "JOIN kontrak k ON c.id_kontrak = k.id_kontrak " +
                    "JOIN users u ON c.id_staff = u.id_user " +
                    "WHERE c.id_cicilan = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding cicilan by id: " + e.getMessage());
        }
        return null;
    }

    public static LocalDate getLastPaymentDate(int idKontrak) {
        String sql = "SELECT MAX(tanggal_cicilan) as last_date FROM cicilan WHERE id_kontrak = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idKontrak);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date lastDate = rs.getDate("last_date");
                    return lastDate != null ? lastDate.toLocalDate() : null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting last payment date: " + e.getMessage());
        }
        return null;
    }

    public static int getLastTenor(int idKontrak) {
        String sql = "SELECT COALESCE(MAX(tenor), 0) as last_tenor FROM cicilan WHERE id_kontrak = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idKontrak);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("last_tenor");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting last tenor: " + e.getMessage());
        }
        return 0;
    }

    public static boolean isTenorPaid(int idKontrak, int tenor) {
        String sql = "SELECT COUNT(*) as count FROM cicilan WHERE id_kontrak = ? AND tenor = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idKontrak);
            stmt.setInt(2, tenor);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking tenor payment: " + e.getMessage());
        }
        return false;
    }

    private static Instalment createFromResultSet(ResultSet rs) throws SQLException {
        Instalment cicilan = new Instalment();
        cicilan.setIdCicilan(rs.getInt("id_cicilan"));
        cicilan.setIdKontrak(rs.getInt("id_kontrak"));
        cicilan.setTenor(rs.getInt("tenor"));
        cicilan.setJumlahCicilan(rs.getInt("jumlah_cicilan"));
        cicilan.setTanggalCicilan(rs.getDate("tanggal_cicilan").toLocalDate());
        cicilan.setIdStaff(rs.getInt("id_staff"));
        
        try {
            cicilan.setNamaPeminjam(rs.getString("nama_peminjam"));
            cicilan.setStaffName(rs.getString("staff_name"));
        } catch (SQLException e) {
            System.err.println("Error setting joined data: " + e.getMessage());
        }
        
        return cicilan;
    }

    // Business logic methods
    public boolean validatePaymentDate(int idKontrak) {
        LocalDate lastDate = getLastPaymentDate(idKontrak);
        
        if (lastDate == null) {
            return true;
        }
        
        LocalDate minimumDate = lastDate.plusMonths(1);
        return !this.tanggalCicilan.isBefore(minimumDate);
    }

    public String getFormattedAmount() {
        return String.format("Rp %,d", jumlahCicilan);
    }

    public String getTenorDisplay() {
        return "Tenor ke-" + tenor;
    }

    // Getters and Setters
    public int getIdCicilan() { return idCicilan; }
    public void setIdCicilan(int idCicilan) { this.idCicilan = idCicilan; }
    
    public int getIdKontrak() { return idKontrak; }
    public void setIdKontrak(int idKontrak) { this.idKontrak = idKontrak; }
    
    public int getTenor() { return tenor; }
    public void setTenor(int tenor) { this.tenor = tenor; }
    
    public int getJumlahCicilan() { return jumlahCicilan; }
    public void setJumlahCicilan(int jumlahCicilan) { this.jumlahCicilan = jumlahCicilan; }
    
    public LocalDate getTanggalCicilan() { return tanggalCicilan; }
    public void setTanggalCicilan(LocalDate tanggalCicilan) { this.tanggalCicilan = tanggalCicilan; }
    
    public int getIdStaff() { return idStaff; }
    public void setIdStaff(int idStaff) { this.idStaff = idStaff; }
    
    public String getNamaPeminjam() { return namaPeminjam; }
    public void setNamaPeminjam(String namaPeminjam) { this.namaPeminjam = namaPeminjam; }
    
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
}