package services;

import models.AgingReport;
import java.util.List;

public class AgingReportService {
    
    public List<AgingReport> getAgingReportByBranch() {
        return AgingReport.getAgingReportByBranch();
    }

    public List<AgingReport> getAgingReportByBranchAndDate(int month, int year) {
        return AgingReport.getAgingReportByBranchAndDate(month, year);
    }
    
    // Tambahkan method baru untuk summary
    public AgingReport getAgingReportSummary() {
        return AgingReport.getAgingReportSummary();
    }
    
    public AgingReport getAgingReportSummaryByDate(int month, int year) {
        return AgingReport.getAgingReportSummaryByDate(month, year);
    }
}