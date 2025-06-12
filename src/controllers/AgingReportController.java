package controllers;

import models.AgingReport;
import services.AgingReportService;
import java.util.List;

public class AgingReportController extends BaseController {

    private final AgingReportService agingReportService;

    public AgingReportController() {
        this.agingReportService = new AgingReportService();
    }
    
    public List<AgingReport> getAgingReport() {
        return agingReportService.getAgingReportByBranch();
    }

    public List<AgingReport> getAgingReportByMonthYear(int month, int year) {
        return agingReportService.getAgingReportByBranchAndDate(month, year);
    }
    
    // Tambahkan method baru untuk summary
    public AgingReport getAgingReportSummary() {
        return agingReportService.getAgingReportSummary();
    }
    
    public AgingReport getAgingReportSummaryByMonthYear(int month, int year) {
        return agingReportService.getAgingReportSummaryByDate(month, year);
    }
}