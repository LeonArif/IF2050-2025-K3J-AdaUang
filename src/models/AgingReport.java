package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgingReport extends BaseModel {
    private String branch;
    private int totalNasabah;
    private long aging1to30;
    private long aging31to60;
    private long aging61to90;
    private long agingOver90;

    public AgingReport() {}

    public static List<AgingReport> getAgingReportByBranch() {
        List<AgingReport> reports = new ArrayList<>();
        
        String sql = """
            SELECT 
                u.branch,
                COUNT(DISTINCT k.id_kontrak) as total_nasabah,
                SUM(CASE WHEN overdue_tenor = 1 THEN outstanding_amount ELSE 0 END) as aging_1_30,
                SUM(CASE WHEN overdue_tenor = 2 THEN outstanding_amount ELSE 0 END) as aging_31_60,
                SUM(CASE WHEN overdue_tenor = 3 THEN outstanding_amount ELSE 0 END) as aging_61_90,
                SUM(CASE WHEN overdue_tenor > 3 THEN outstanding_amount ELSE 0 END) as aging_over_90
            FROM kontrak k
            JOIN users u ON k.id_user = u.id_user
            INNER JOIN (
                SELECT 
                    k.id_kontrak,
                    -- Hitung tenor yang seharusnya sudah dibayar (berdasarkan bulan yang berlalu + 1)
                    LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, CURDATE()) + 1) as should_pay_tenor,
                    -- Tenor tertinggi yang sudah dibayar
                    COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0) as paid_tenor,
                    -- Selisih = tenor yang tertunggak
                    GREATEST(0, 
                        LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, CURDATE()) + 1) - 
                        COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                    ) as overdue_tenor,
                    -- Outstanding amount = cicilan per bulan * jumlah tenor tertunggak
                    k.cicilan_per_bulan * GREATEST(0, 
                        LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, CURDATE()) + 1) - 
                        COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                    ) as outstanding_amount
                FROM kontrak k
                WHERE k.status = true
            ) overdue_calc ON k.id_kontrak = overdue_calc.id_kontrak
            WHERE overdue_calc.overdue_tenor > 0
            GROUP BY u.branch
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AgingReport report = new AgingReport();
                    report.setBranch(rs.getString("branch"));
                    report.setTotalNasabah(rs.getInt("total_nasabah"));
                    report.setAging1to30(rs.getLong("aging_1_30"));
                    report.setAging31to60(rs.getLong("aging_31_60"));
                    report.setAging61to90(rs.getLong("aging_61_90"));
                    report.setAgingOver90(rs.getLong("aging_over_90"));
                    reports.add(report);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public static List<AgingReport> getAgingReportByBranchAndDate(int month, int year) {
        List<AgingReport> reports = new ArrayList<>();
        
        String sql = """
            SELECT 
                u.branch,
                COUNT(DISTINCT k.id_kontrak) as total_nasabah,
                SUM(CASE WHEN overdue_tenor = 1 THEN outstanding_amount ELSE 0 END) as aging_1_30,
                SUM(CASE WHEN overdue_tenor = 2 THEN outstanding_amount ELSE 0 END) as aging_31_60,
                SUM(CASE WHEN overdue_tenor = 3 THEN outstanding_amount ELSE 0 END) as aging_61_90,
                SUM(CASE WHEN overdue_tenor > 3 THEN outstanding_amount ELSE 0 END) as aging_over_90
            FROM kontrak k
            JOIN users u ON k.id_user = u.id_user
            INNER JOIN (
                SELECT 
                    k.id_kontrak,
                    -- Hitung tenor yang seharusnya sudah dibayar sampai tanggal tertentu
                    LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, ?) + 1) as should_pay_tenor,
                    -- Tenor tertinggi yang sudah dibayar
                    COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0) as paid_tenor,
                    -- Selisih = tenor yang tertunggak
                    GREATEST(0, 
                        LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, ?) + 1) - 
                        COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                    ) as overdue_tenor,
                    -- Outstanding amount
                    k.cicilan_per_bulan * GREATEST(0, 
                        LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, ?) + 1) - 
                        COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                    ) as outstanding_amount
                FROM kontrak k
                WHERE k.status = true
            ) overdue_calc ON k.id_kontrak = overdue_calc.id_kontrak
            WHERE overdue_calc.overdue_tenor > 0
            GROUP BY u.branch
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameter tanggal
            String dateStr = String.format("%d-%02d-01", year, month);
            stmt.setString(1, dateStr);
            stmt.setString(2, dateStr);
            stmt.setString(3, dateStr);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AgingReport report = new AgingReport();
                    report.setBranch(rs.getString("branch"));
                    report.setTotalNasabah(rs.getInt("total_nasabah"));
                    report.setAging1to30(rs.getLong("aging_1_30"));
                    report.setAging31to60(rs.getLong("aging_31_60"));
                    report.setAging61to90(rs.getLong("aging_61_90"));
                    report.setAgingOver90(rs.getLong("aging_over_90"));
                    reports.add(report);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public static AgingReport getAgingReportSummary() {
        String sql = """
            SELECT 
                'TOTAL SEMUA CABANG' as branch,
                COUNT(DISTINCT k.id_kontrak) as total_nasabah,
                SUM(CASE WHEN overdue_tenor = 1 THEN outstanding_amount ELSE 0 END) as aging_1_30,
                SUM(CASE WHEN overdue_tenor = 2 THEN outstanding_amount ELSE 0 END) as aging_31_60,
                SUM(CASE WHEN overdue_tenor = 3 THEN outstanding_amount ELSE 0 END) as aging_61_90,
                SUM(CASE WHEN overdue_tenor > 3 THEN outstanding_amount ELSE 0 END) as aging_over_90
            FROM kontrak k
            JOIN users u ON k.id_user = u.id_user
            INNER JOIN (
                SELECT 
                    k.id_kontrak,
                    GREATEST(0, 
                        LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, CURDATE()) + 1) - 
                        COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                    ) as overdue_tenor,
                    k.cicilan_per_bulan * GREATEST(0, 
                        LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, CURDATE()) + 1) - 
                        COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                    ) as outstanding_amount
                FROM kontrak k
                WHERE k.status = true
            ) overdue_calc ON k.id_kontrak = overdue_calc.id_kontrak
            WHERE overdue_calc.overdue_tenor > 0
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    AgingReport summary = new AgingReport();
                    summary.setBranch(rs.getString("branch"));
                    summary.setTotalNasabah(rs.getInt("total_nasabah"));
                    summary.setAging1to30(rs.getLong("aging_1_30"));
                    summary.setAging31to60(rs.getLong("aging_31_60"));
                    summary.setAging61to90(rs.getLong("aging_61_90"));
                    summary.setAgingOver90(rs.getLong("aging_over_90"));
                    return summary;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Return empty summary if no data
        AgingReport emptySummary = new AgingReport();
        emptySummary.setBranch("TOTAL SEMUA CABANG");
        emptySummary.setTotalNasabah(0);
        emptySummary.setAging1to30(0);
        emptySummary.setAging31to60(0);
        emptySummary.setAging61to90(0);
        emptySummary.setAgingOver90(0);
        return emptySummary;
    }

    public static AgingReport getAgingReportSummaryByDate(int month, int year) {
        String sql = """
            SELECT 
                'TOTAL SEMUA CABANG' as branch,
                COUNT(DISTINCT k.id_kontrak) as total_nasabah,
                SUM(CASE WHEN overdue_tenor = 1 THEN outstanding_amount ELSE 0 END) as aging_1_30,
                SUM(CASE WHEN overdue_tenor = 2 THEN outstanding_amount ELSE 0 END) as aging_31_60,
                SUM(CASE WHEN overdue_tenor = 3 THEN outstanding_amount ELSE 0 END) as aging_61_90,
                SUM(CASE WHEN overdue_tenor > 3 THEN outstanding_amount ELSE 0 END) as aging_over_90
            FROM kontrak k
            JOIN users u ON k.id_user = u.id_user
            INNER JOIN (
                SELECT 
                    k.id_kontrak,
                    GREATEST(0, 
                        LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, ?) + 1) - 
                        COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                    ) as overdue_tenor,
                    k.cicilan_per_bulan * GREATEST(0, 
                        LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, ?) + 1) - 
                        COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                    ) as outstanding_amount
                FROM kontrak k
                WHERE k.status = true
            ) overdue_calc ON k.id_kontrak = overdue_calc.id_kontrak
            WHERE overdue_calc.overdue_tenor > 0
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String dateStr = String.format("%d-%02d-01", year, month);
            stmt.setString(1, dateStr);
            stmt.setString(2, dateStr);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    AgingReport summary = new AgingReport();
                    summary.setBranch(rs.getString("branch"));
                    summary.setTotalNasabah(rs.getInt("total_nasabah"));
                    summary.setAging1to30(rs.getLong("aging_1_30"));
                    summary.setAging31to60(rs.getLong("aging_31_60"));
                    summary.setAging61to90(rs.getLong("aging_61_90"));
                    summary.setAgingOver90(rs.getLong("aging_over_90"));
                    return summary;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Return empty summary if no data
        AgingReport emptySummary = new AgingReport();
        emptySummary.setBranch("TOTAL SEMUA CABANG");
        emptySummary.setTotalNasabah(0);
        emptySummary.setAging1to30(0);
        emptySummary.setAging31to60(0);
        emptySummary.setAging61to90(0);
        emptySummary.setAgingOver90(0);
        return emptySummary;
    }

    public static void debugAgingData() {
        String debugSql = """
            SELECT 
                k.id_kontrak, k.nama_user, u.branch,
                k.tanggal_pinjam, k.status, k.tenor as total_tenor,
                TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, CURDATE()) + 1 as months_passed,
                LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, CURDATE()) + 1) as should_pay_tenor,
                COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0) as paid_tenor,
                GREATEST(0, 
                    LEAST(k.tenor, TIMESTAMPDIFF(MONTH, k.tanggal_pinjam, CURDATE()) + 1) - 
                    COALESCE((SELECT MAX(c.tenor) FROM cicilan c WHERE c.id_kontrak = k.id_kontrak), 0)
                ) as overdue_tenor,
                k.cicilan_per_bulan
            FROM kontrak k
            JOIN users u ON k.id_user = u.id_user
            WHERE k.status = true
            ORDER BY k.id_kontrak
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(debugSql)) {
    
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n=== DEBUG AGING REPORT DATA (Current: " + java.time.LocalDate.now() + ") ===");
                System.out.println("ID | Nama           | Branch   | Start Date | Should Pay | Paid | Overdue | Outstanding");
                System.out.println("-------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    int overdueTenor = rs.getInt("overdue_tenor");
                    long outstanding = rs.getLong("cicilan_per_bulan") * overdueTenor;
                    
                    System.out.printf("%-2d | %-15s | %-8s | %-10s | %-10d | %-4d | %-7d | Rp %,d%n",
                        rs.getInt("id_kontrak"),
                        rs.getString("nama_user"),
                        rs.getString("branch"),
                        rs.getDate("tanggal_pinjam"),
                        rs.getInt("should_pay_tenor"),
                        rs.getInt("paid_tenor"),
                        overdueTenor,
                        outstanding
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean save() { return false; }
    @Override
    public boolean delete() { return false; }
    @Override
    public boolean isNewRecord() { return true; }

    // Getters and Setters
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public int getTotalNasabah() { return totalNasabah; }
    public void setTotalNasabah(int totalNasabah) { this.totalNasabah = totalNasabah; }
    public long getAging1to30() { return aging1to30; }
    public void setAging1to30(long aging1to30) { this.aging1to30 = aging1to30; }
    public long getAging31to60() { return aging31to60; }
    public void setAging31to60(long aging31to60) { this.aging31to60 = aging31to60; }
    public long getAging61to90() { return aging61to90; }
    public void setAging61to90(long aging61to90) { this.aging61to90 = aging61to90; }
    public long getAgingOver90() { return agingOver90; }
    public void setAgingOver90(long agingOver90) { this.agingOver90 = agingOver90; }
}