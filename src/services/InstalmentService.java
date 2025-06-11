package services;

import database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.Cicilan; 
import models.Contract; // Pastikan Contract di-import

public class InstalmentService {
    private final DatabaseConnection dbConnection;

    public InstalmentService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public boolean addCicilan(int idKontrak, int jumlah, int tenor, LocalDate tanggal, int idStaff) {
        Connection conn = dbConnection.getConnection();
        
        try {
            System.out.println("Starting addCicilan: kontrak=" + idKontrak + ", jumlah=" + jumlah + ", tenor=" + tenor);
            
            // Cek apakah tenor sudah pernah dibayar
            String checkSql = "SELECT COUNT(*) FROM cicilan WHERE id_kontrak = ? AND tenor = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, idKontrak);
                checkStmt.setInt(2, tenor);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Tenor " + tenor + " sudah pernah dibayar untuk kontrak " + idKontrak);
                        return false;
                    }
                }
            }
            
            System.out.println("Tenor belum pernah dibayar, melanjutkan insert...");
            
            // Insert cicilan baru
            String insertSql = "INSERT INTO cicilan (id_kontrak, tenor, jumlah_cicilan, tanggal_cicilan, id_staff) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, idKontrak);
                insertStmt.setInt(2, tenor);
                insertStmt.setInt(3, jumlah);
                insertStmt.setDate(4, java.sql.Date.valueOf(tanggal));
                insertStmt.setInt(5, idStaff);
                
                int insertResult = insertStmt.executeUpdate();
                System.out.println("Insert cicilan result: " + insertResult);
                
                if (insertResult > 0) {
                    // Update jumlah_bayar di tabel kontrak
                    String updateKontrakSql = "UPDATE kontrak SET jumlah_bayar = jumlah_bayar + ? WHERE id_kontrak = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateKontrakSql)) {
                        updateStmt.setInt(1, jumlah);
                        updateStmt.setInt(2, idKontrak);
                        
                        int updateResult = updateStmt.executeUpdate();
                        System.out.println("Update kontrak result: " + updateResult);
                        
                        if (updateResult > 0) {
                            updateKontrakStatusIfComplete(idKontrak);
                            System.out.println("Cicilan berhasil ditambahkan untuk kontrak " + idKontrak);
                            return true;
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error menambahkan cicilan: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("AddCicilan gagal untuk kontrak " + idKontrak);
        return false;
    }

    public List<Cicilan> getAllCicilan() {
        List<Cicilan> cicilanList = new ArrayList<>();
        Connection conn = dbConnection.getConnection();
        
        try {
            // Coba query sederhana dulu tanpa JOIN
            String sql = "SELECT * FROM cicilan ORDER BY id_kontrak ASC, tanggal_cicilan DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Cicilan cicilan = new Cicilan(
                        rs.getInt("id_cicilan"),
                        rs.getInt("id_kontrak"),
                        rs.getInt("tenor"),
                        rs.getInt("jumlah_cicilan"),
                        rs.getDate("tanggal_cicilan").toLocalDate(),
                        rs.getInt("id_staff")
                    );
                    cicilanList.add(cicilan);
                    System.out.println("Added cicilan: ID=" + rs.getInt("id_cicilan") + ", Kontrak=" + rs.getInt("id_kontrak"));
                }
            }
            
            System.out.println("Found " + cicilanList.size() + " cicilan records in database");
            
        } catch (SQLException e) {
            System.err.println("Error mengambil data cicilan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cicilanList;
    }

    /**
     * Ambil semua cicilan untuk kontrak tertentu
     */
    public List<Cicilan> getCicilanByKontrak(int idKontrak) {
        List<Cicilan> cicilanList = new ArrayList<>();
        Connection conn = dbConnection.getConnection();
        
        try {
            String sql = "SELECT * FROM cicilan WHERE id_kontrak = ? ORDER BY tenor ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idKontrak);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Cicilan cicilan = new Cicilan(
                            rs.getInt("id_cicilan"),
                            rs.getInt("id_kontrak"),
                            rs.getInt("tenor"),
                            rs.getInt("jumlah_cicilan"),
                            rs.getDate("tanggal_cicilan").toLocalDate(),
                            rs.getInt("id_staff")
                        );
                        cicilanList.add(cicilan);
                    }
                }
            }
            
            System.out.println("Found " + cicilanList.size() + " cicilan records for kontrak ID: " + idKontrak);
            
        } catch (SQLException e) {
            System.err.println("Error mengambil data cicilan by kontrak: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cicilanList;
    }

    /**
     * Ambil tenor terakhir yang sudah dibayar untuk kontrak tertentu
     */
    public int getLastTenor(int idKontrak) {
        Connection conn = dbConnection.getConnection();
        
        try {
            String sql = "SELECT COALESCE(MAX(tenor), 0) as tenor_terakhir FROM cicilan WHERE id_kontrak = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idKontrak);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("tenor_terakhir");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error mengambil tenor terakhir: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0; // Default jika belum ada pembayaran
    }

    /**
     * Cek apakah tenor sudah pernah dibayar
     */
    public boolean isTenorlreadyPaid(int idKontrak, int tenor) {
        Connection conn = dbConnection.getConnection();
        
        try {
            String sql = "SELECT COUNT(*) FROM cicilan WHERE id_kontrak = ? AND tenor = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idKontrak);
                stmt.setInt(2, tenor);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking tenor payment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }


    public boolean updateKontrakStatusIfComplete(int idKontrak) {
        try {
            // Ambil data kontrak
            Contract contract = Contract.findById(idKontrak);
            if (contract == null) return false;
            
            // Hitung total cicilan yang sudah dibayar
            String sql = "SELECT COUNT(*) as total_terbayar FROM cicilan WHERE id_kontrak = ?";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, idKontrak);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int totalTerbayar = rs.getInt("total_terbayar");
                        
                        // Jika sudah terbayar semua, ubah status jadi lunas
                        if (totalTerbayar >= contract.getTenor()) {
                            return updateKontrakStatusToLunas(idKontrak);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking contract completion: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateKontrakStatusToLunas(int idKontrak) {
        String sql = "UPDATE kontrak SET status = 0 WHERE id_kontrak = ?"; // Ubah jadi 0
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idKontrak);
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("Kontrak " + idKontrak + " berubah status menjadi INACTIVE (LUNAS)");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating contract status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
