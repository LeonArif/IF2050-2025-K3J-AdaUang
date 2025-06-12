package controllers;

import models.ContractAgeReport;
import services.ContractAgeService;
import java.util.List;

public class ContractAgeController extends BaseController {
    
    private final ContractAgeService contractAgeService;

    public ContractAgeController() {
        this.contractAgeService = new ContractAgeService();
    }
    
    /**
     * Method untuk mendapatkan laporan umur piutang berdasarkan bulan dan tahun
     * @param month Bulan laporan (1-12)
     * @param year Tahun laporan
     * @return List ContractAgeReport untuk semua cabang
     */
    public List<ContractAgeReport> getContractAgeReport(int month, int year) {
        if (month < 1 || month > 12 || year < 2000) {
            return null;
        }
        return contractAgeService.generateContractAgeReport(month, year);
    }
    
    /**
     * Method untuk mendapatkan laporan umur piutang per cabang
     * @param branch Nama cabang (Jakarta, Bandung, Surabaya, Solo, Yogyakarta)
     * @param month Bulan laporan
     * @param year Tahun laporan
     * @return ContractAgeReport untuk cabang tertentu
     */
    public ContractAgeReport getContractAgeReportByBranch(String branch, int month, int year) {
        if (branch == null || branch.trim().isEmpty() || month < 1 || month > 12 || year < 2000) {
            return null;
        }
        return contractAgeService.generateContractAgeReportByBranch(branch, month, year);
    }
    
    /**
     * Method untuk mendapatkan total nasabah per cabang
     * @param branch Nama cabang
     * @return Jumlah total nasabah
     */
    public int getTotalNasabahByBranch(String branch) {
        if (branch == null || branch.trim().isEmpty()) {
            return 0;
        }
        return contractAgeService.getTotalNasabahByBranch(branch);
    }
    
    /**
     * Method untuk mendapatkan ringkasan total semua cabang
     * @param month Bulan laporan
     * @param year Tahun laporan
     * @return ContractAgeReport ringkasan total
     */
    public ContractAgeReport getTotalSummary(int month, int year) {
        if (month < 1 || month > 12 || year < 2000) {
            return null;
        }
        return contractAgeService.calculateTotalSummary(month, year);
    }
    
    /**
     * Method untuk mendapatkan daftar cabang yang tersedia
     * @return List nama cabang
     */
    public List<String> getAvailableBranches() {
        return contractAgeService.getAvailableBranches();
    }
}