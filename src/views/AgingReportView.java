package views;

import controllers.AgingReportController;
import controllers.AuthController;
import models.AgingReport;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AgingReportView extends JPanel {
    private final AgingReportController agingReportController;
    private final AuthController authController;
    private JTable agingTable;
    private JTable summaryTable; // Tambahkan tabel summary
    private DefaultTableModel tableModel;
    private DefaultTableModel summaryTableModel; // Tambahkan model untuk summary
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;

    public AgingReportView(AuthController authController) {
        this.authController = authController;
        this.agingReportController = new AgingReportController();
        
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(248, 249, 251));
        setName("aging-report");

        JPanel mainContentPanel = createMainContentPanel();
        add(mainContentPanel, BorderLayout.CENTER);
        
        loadTableData();
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 32, 32, 32));

        // Header with title and date filters
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Laporan Umur Piutang");
        title.setFont(new Font("Montserrat", Font.BOLD, 32));
        title.setForeground(new Color(39, 49, 157));
        headerPanel.add(title, BorderLayout.WEST);

        // Date filter panel
        JPanel filterPanel = createFilterPanel();
        headerPanel.add(filterPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Create tables container dengan kedua tabel
        JPanel tablesContainer = createTablesContainer();
        panel.add(tablesContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                          "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setFont(new Font("Montserrat", Font.PLAIN, 14));
        monthComboBox.setSelectedIndex(java.time.LocalDate.now().getMonthValue() - 1);
        filterPanel.add(monthComboBox);

        JLabel yearLabel = new JLabel("Tahun:");
        yearLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
        filterPanel.add(yearLabel);

        Integer[] years = {2023, 2024, 2025, 2026};
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setFont(new Font("Montserrat", Font.PLAIN, 14));
        yearComboBox.setSelectedItem(java.time.LocalDate.now().getYear());
        filterPanel.add(yearComboBox);

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setFont(new Font("Montserrat", Font.BOLD, 14));
        refreshButton.setBackground(new Color(26, 35, 80));
        refreshButton.setForeground(new Color(39, 49, 157));
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setPreferredSize(new Dimension(130, 40));
        refreshButton.addActionListener(e -> refreshData());
        filterPanel.add(refreshButton);

        return filterPanel;
    }

    private JPanel createTablesContainer() {
        JPanel tablesContainer = new JPanel(new BorderLayout(0, 20));
        tablesContainer.setOpaque(false);

        // Summary table di atas
        JPanel summaryContainer = createSummaryTableContainer();
        tablesContainer.add(summaryContainer, BorderLayout.NORTH);

        // Detail table per cabang di bawah
        JPanel detailContainer = createDetailTableContainer();
        tablesContainer.add(detailContainer, BorderLayout.CENTER);

        return tablesContainer;
    }

    private JPanel createSummaryTableContainer() {
        JPanel summaryContainer = new JPanel(new BorderLayout());
        summaryContainer.setBackground(Color.WHITE);
        summaryContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
            "RINGKASAN SEMUA CABANG",
            0, 0,
            new Font("Montserrat", Font.BOLD, 16),
            new Color(100, 100, 100)
        ));

        String[] columns = {"Total Nasabah", "1-30 Hari", "31-60 Hari", "61-90 Hari", ">90 Hari", "Total"};
        summaryTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        summaryTable = new JTable(summaryTableModel);
        setupSummaryTableStyle();

        JScrollPane summaryScrollPane = new JScrollPane(summaryTable);
        summaryScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        summaryScrollPane.getViewport().setBackground(Color.WHITE);
        summaryScrollPane.setPreferredSize(new Dimension(0, 80)); // Set tinggi fixed

        summaryContainer.add(summaryTable.getTableHeader(), BorderLayout.NORTH);
        summaryContainer.add(summaryScrollPane, BorderLayout.CENTER);

        return summaryContainer;
    }

    private JPanel createDetailTableContainer() {
        JPanel detailContainer = new JPanel(new BorderLayout());
        detailContainer.setBackground(Color.WHITE);
        detailContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
            "DETAIL PER CABANG",
            0, 0,
            new Font("Montserrat", Font.BOLD, 16),
            new Color(100, 100, 100)
        ));

        String[] columns = {"Cabang", "Total Nasabah", "1-30 Hari", "31-60 Hari", "61-90 Hari", ">90 Hari", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        agingTable = new JTable(tableModel);
        setupTableStyle();

        JScrollPane scrollPane = new JScrollPane(agingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        detailContainer.add(agingTable.getTableHeader(), BorderLayout.NORTH);
        detailContainer.add(scrollPane, BorderLayout.CENTER);

        return detailContainer;
    }

    private void setupSummaryTableStyle() {
        Color borderColor = new Color(220, 220, 220);
        summaryTable.setRowHeight(50);
        summaryTable.setFont(new Font("Montserrat", Font.BOLD, 16));
        summaryTable.setSelectionBackground(new Color(235, 240, 255));
        summaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        summaryTable.setShowGrid(true);
        summaryTable.setGridColor(borderColor);
        summaryTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = summaryTable.getTableHeader();
        header.setPreferredSize(new Dimension(100, 40));
        header.setFont(new Font("Montserrat", Font.BOLD, 14));
        header.setBackground(new Color(245, 247, 250));
        header.setForeground(new Color(100, 100, 100));
        header.setBorder(BorderFactory.createLineBorder(borderColor));

        // Right-align currency columns
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        currencyRenderer.setFont(new Font("Montserrat", Font.BOLD, 16));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setFont(new Font("Montserrat", Font.BOLD, 16));

        summaryTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Total Nasabah
        
        for(int i = 1; i < summaryTable.getColumnCount(); i++){
            summaryTable.getColumnModel().getColumn(i).setCellRenderer(currencyRenderer);
        }

        // Tambahkan mouse listener untuk toggle selection
        setupToggleableSelection(summaryTable);
    }

    private void setupTableStyle() {
        Color borderColor = new Color(220, 220, 220);
        agingTable.setRowHeight(45);
        agingTable.setFont(new Font("Montserrat", Font.PLAIN, 14));
        agingTable.setSelectionBackground(new Color(235, 240, 255));
        agingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        agingTable.setShowGrid(true);
        agingTable.setGridColor(borderColor);
        agingTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = agingTable.getTableHeader();
        header.setPreferredSize(new Dimension(100, 50));
        header.setFont(new Font("Montserrat", Font.BOLD, 14));
        header.setBackground(new Color(245, 247, 250));
        header.setForeground(new Color(100, 100, 100));
        header.setBorder(BorderFactory.createLineBorder(borderColor));

        // Right-align currency columns
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        agingTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Cabang
        agingTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Total Nasabah
        
        for(int i = 2; i < agingTable.getColumnCount(); i++){
            agingTable.getColumnModel().getColumn(i).setCellRenderer(currencyRenderer);
        }

        // Tambahkan mouse listener untuk toggle selection
        setupToggleableSelection(agingTable);
    }

    // Tambahkan method baru untuk handle toggle selection
    private void setupToggleableSelection(JTable table) {
        table.setSelectionModel(new DefaultListSelectionModel() {
            private int lastSelectedRow = -1;
            
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (lastSelectedRow == index0) {
                    // Jika row yang sama diklik ulang, clear selection
                    clearSelection();
                    lastSelectedRow = -1;
                } else {
                    // Jika row berbeda, select row tersebut
                    super.setSelectionInterval(index0, index1);
                    lastSelectedRow = index0;
                }
            }
            
            @Override
            public void clearSelection() {
                super.clearSelection();
                lastSelectedRow = -1;
            }
        });
    }

    private void loadTableData() {
        try {
            // Debug data terlebih dahulu
            AgingReport.debugAgingData();
            
            // Clear both tables
            tableModel.setRowCount(0);
            summaryTableModel.setRowCount(0);
            
            // Get selected month and year
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = (Integer) yearComboBox.getSelectedItem();
            
            // Load summary data
            AgingReport summary = agingReportController.getAgingReportSummaryByMonthYear(selectedMonth, selectedYear);
            long summaryTotal = summary.getAging1to30() + summary.getAging31to60() + 
                              summary.getAging61to90() + summary.getAgingOver90();
            
            summaryTableModel.addRow(new Object[]{
                summary.getTotalNasabah(),
                formatCurrency(summary.getAging1to30()),
                formatCurrency(summary.getAging31to60()),
                formatCurrency(summary.getAging61to90()),
                formatCurrency(summary.getAgingOver90()),
                formatCurrency(summaryTotal)
            });
            
            // Load detail data per branch
            List<AgingReport> reports = agingReportController.getAgingReportByMonthYear(selectedMonth, selectedYear);
            
            System.out.println("Found " + reports.size() + " aging reports for " + selectedMonth + "/" + selectedYear);
            System.out.println("Summary - Total Nasabah: " + summary.getTotalNasabah() + ", Total Outstanding: " + formatCurrency(summaryTotal));
            
            for (AgingReport report : reports) {
                long totalOutstanding = report.getAging1to30() + report.getAging31to60() + 
                                      report.getAging61to90() + report.getAgingOver90();
                
                tableModel.addRow(new Object[]{
                    report.getBranch(),
                    report.getTotalNasabah(),
                    formatCurrency(report.getAging1to30()),
                    formatCurrency(report.getAging31to60()),
                    formatCurrency(report.getAging61to90()),
                    formatCurrency(report.getAgingOver90()),
                    formatCurrency(totalOutstanding)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatCurrency(long amount) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(amount);
    }

    public void refreshData() {
        System.out.println("\n🔄 Refreshing aging report data...");
        loadTableData();
    }
}