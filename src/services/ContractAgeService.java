package services;

import models.ContractAgeReport;
import models.Contract;
import models.User;
import java.util.*;

public class ContractAgeService {
    
    /**Menghasilkan laporan umur piutang untuk semua cabang**/
    public List<ContractAgeReport> generateContractAgeReport(int month, int year) {
        List<ContractAgeReport> reports = new ArrayList<>();
        List<String> branches = getAvailableBranches();
        
        for (String branch : branches) {
            ContractAgeReport branchReport = generateContractAgeReportByBranch(branch, month, year);
            if (branchReport != null) {
                reports.add(branchReport);
            }
        }
        
        return reports;
    }
    
    /**
     * Method untuk menghasilkan laporan umur piutang per cabang tertentu
     * @param branch Nama cabang
     * @param month Bulan laporan
     * @param year Tahun laporan
     * @return ContractAgeReport untuk cabang tertentu
     */
    public ContractAgeReport generateContractAgeReportByBranch(String branch, int month, int year) {
        // Mendapatkan total nasabah per cabang
        int totalNasabah = getTotalNasabahByBranch(branch);
        
        // Mendapatkan semua kontrak yang belum dibayar untuk cabang tertentu
        List<Contract> unpaidContracts = getUnpaidContractsByBranch(branch);
        
        // Inisialisasi nilai untuk setiap range umur piutang
        long range1to30 = 0;
        long range31to60 = 0;
        long range61to90 = 0;
        long rangeOver90 = 0;
        
        // Menghitung dan mengkategorikan berdasarkan selisih bulan
        for (Contract contract : unpaidContracts) {
            if (contract.getBulan_jatuh_tempo() != 0 && contract.getTahun_jatuh_tempo() != 0) {
                int monthsDifference = calculateMonthsDifference(
                    contract.getBulan_jatuh_tempo(), 
                    contract.getTahun_jatuh_tempo(), 
                    month, 
                    year
                );
                
                long outstandingAmount = contract.getOutstanding_amount();
                
                // Kategorikan berdasarkan selisih bulan
                if (monthsDifference == 1) {
                    range1to30 += outstandingAmount;
                } else if (monthsDifference == 2) {
                    range31to60 += outstandingAmount;
                } else if (monthsDifference == 3) {
                    range61to90 += outstandingAmount;
                } else if (monthsDifference >= 4) {
                    rangeOver90 += outstandingAmount;
                }
            }
        }
        
        long total = range1to30 + range31to60 + range61to90 + rangeOver90;
        
        ContractAgeReport report = new ContractAgeReport();
        report.setBranch(branch);
        report.setTotalNasabah(totalNasabah);
        report.setRange1to30(range1to30);
        report.setRange31to60(range31to60);
        report.setRange61to90(range61to90);
        report.setRangeOver90(rangeOver90);
        report.setTotal(total);
        
        return report;
    }
    
    /**
     * Method untuk mendapatkan total nasabah per cabang
     * @param branch Nama cabang
     * @return Jumlah total nasabah
     */
    public int getTotalNasabahByBranch(String branch) {
        List<Nasabah> nasabahList = Nasabah.findByBranch(branch);
        return nasabahList != null ? nasabahList.size() : 0;
    }
    
    /**
     * Method untuk menghitung ringkasan total semua cabang
     * @param month Bulan laporan
     * @param year Tahun laporan
     * @return ContractAgeReport ringkasan total
     */
    public ContractAgeReport calculateTotalSummary(int month, int year) {
        List<ContractAgeReport> allReports = generateContractAgeReport(month, year);
        
        int totalNasabah = 0;
        long totalRange1to30 = 0;
        long totalRange31to60 = 0;
        long totalRange61to90 = 0;
        long totalRangeOver90 = 0;
        
        for (ContractAgeReport report : allReports) {
            totalNasabah += report.getTotalNasabah();
            totalRange1to30 += report.getRange1to30();
            totalRange31to60 += report.getRange31to60();
            totalRange61to90 += report.getRange61to90();
            totalRangeOver90 += report.getRangeOver90();
        }
        
        long grandTotal = totalRange1to30 + totalRange31to60 + totalRange61to90 + totalRangeOver90;
        
        ContractAgeReport summary = new ContractAgeReport();
        summary.setBranch("TOTAL");
        summary.setTotalNasabah(totalNasabah);
        summary.setRange1to30(totalRange1to30);
        summary.setRange31to60(totalRange31to60);
        summary.setRange61to90(totalRange61to90);
        summary.setRangeOver90(totalRangeOver90);
        summary.setTotal(grandTotal);
        
        return summary;
    }
    
    /**
     * Method untuk mendapatkan daftar cabang yang tersedia
     * @return List nama cabang
     */
    public List<String> getAvailableBranches() {
        return Arrays.asList("Jakarta", "Bandung", "Surabaya", "Solo", "Yogyakarta");
    }
    

    private List<Contract> getUnpaidContractsByBranch(String branch) {
        List<Contract> allContracts = Contract.findAllWithUserDetails();
        List<Contract> unpaidContracts = new ArrayList<>();
        
        for (Contract contract : allContracts) {
            // Cek apakah kontrak milik cabang yang diminta dan masih ada outstanding amount
            if (contract.getNasabah() != null && 
                branch.equals(contract.getNasabah().getBranch()) &&
                contract.getOutstanding_amount() > 0 &&
                !contract.isLunas()) {
                
                unpaidContracts.add(contract);
            }
        }
        
        return unpaidContracts;
    }
    
    /**
     * Method helper untuk menghitung selisih bulan
     * @param dueMonth Bulan jatuh tempo
     * @param dueYear Tahun jatuh tempo
     * @param currentMonth Bulan laporan
     * @param currentYear Tahun laporan
     * @return Selisih bulan (1 = 1-30 hari, 2 = 31-60 hari, dst)
     */
    private int calculateMonthsDifference(int dueMonth, int dueYear, int currentMonth, int currentYear) {
        // Jika tahun laporan lebih kecil dari tahun jatuh tempo, return 0 (belum jatuh tempo)
        if (currentYear < dueYear) {
            return 0;
        }
        
        // Jika tahun sama, hitung selisih bulan
        if (currentYear == dueYear) {
            return Math.max(0, currentMonth - dueMonth);
        }
        
        // Jika tahun laporan lebih besar, hitung total selisih bulan
        int yearDifference = currentYear - dueYear;
        int totalMonthsDifference = (yearDifference * 12) + (currentMonth - dueMonth);
        
        return Math.max(0, totalMonthsDifference);
    }
}