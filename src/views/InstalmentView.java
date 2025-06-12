package views;

import controllers.AuthController;
import controllers.ContractController;
import controllers.InstalmentController;
import models.Instalment;
import models.Contract;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class InstalmentView extends JPanel {
    
    // Controllers
    private final InstalmentController instalmentController;
    private final ContractController contractController;
    private final AuthController authController;
    
    // UI Components - Tables
    private JTable contractTable;
    private DefaultTableModel contractTableModel;
    private JTable instalmentTable;
    private DefaultTableModel instalmentTableModel;
    
    // UI Components - Form
    private JTextField dateField;
    private JLabel selectedContractLabel;
    private JLabel paymentAmountLabel;
    private JLabel nextTenorLabel;
    private JButton payButton;
    private JButton cancelButton;
    
    // State
    private Contract selectedContract = null;

    public InstalmentView(AuthController authController) {
        this.authController = authController;
        this.instalmentController = new InstalmentController();
        this.contractController = new ContractController();

        initializeView();
        initializeComponents();
        loadData();
    }

    private void initializeView() {
        setLayout(null);
        setBackground(new Color(248, 249, 251));
        setPreferredSize(new Dimension(900, 600));
        setName("cicilan");
    }

    private void initializeComponents() {
        createTitle();
        createContractSection();
        createInstalmentSection();
        createPaymentForm();
    }

    private void createTitle() {
        JLabel titleLabel = new JLabel("Manajemen Cicilan");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        titleLabel.setForeground(new Color(39, 49, 157));
        titleLabel.setBounds(20, 8, 300, 22);
        add(titleLabel);
    }

    private void createContractSection() {
        // Section label
        JLabel contractLabel = new JLabel("1. Pilih Kontrak");
        contractLabel.setFont(new Font("Montserrat", Font.BOLD, 13));
        contractLabel.setForeground(new Color(30, 30, 30));
        contractLabel.setBounds(20, 35, 180, 16);
        add(contractLabel);

        // Table setup
        String[] contractColumns = {"ID", "Nama Peminjam", "Total", "Cicilan/Bulan", "Tenor", "Status"};
        contractTableModel = new DefaultTableModel(contractColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        contractTable = new JTable(contractTableModel);
        contractTable.setRowHeight(20);
        contractTable.setFont(new Font("Montserrat", Font.PLAIN, 10));
        contractTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 10));
        contractTable.getTableHeader().setBackground(new Color(245, 245, 245));
        contractTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Selection listener
        contractTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleContractSelection();
            }
        });

        JScrollPane contractScroll = new JScrollPane(contractTable);
        contractScroll.setBounds(20, 55, 800, 85);
        contractScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(contractScroll);
    }

    private void createInstalmentSection() {
        // Section label
        JLabel instalmentLabel = new JLabel("2. Riwayat Cicilan");
        instalmentLabel.setFont(new Font("Montserrat", Font.BOLD, 13));
        instalmentLabel.setForeground(new Color(30, 30, 30));
        instalmentLabel.setBounds(20, 150, 180, 16);
        add(instalmentLabel);

        // Selected contract info
        selectedContractLabel = new JLabel("Belum ada kontrak dipilih");
        selectedContractLabel.setFont(new Font("Montserrat", Font.ITALIC, 10));
        selectedContractLabel.setForeground(new Color(100, 100, 100));
        selectedContractLabel.setBounds(20, 168, 500, 14);
        add(selectedContractLabel);

        // Table setup
        String[] instalmentColumns = {"Tenor", "Jumlah", "Tanggal", "Staff"};
        instalmentTableModel = new DefaultTableModel(instalmentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        instalmentTable = new JTable(instalmentTableModel);
        instalmentTable.setRowHeight(20);
        instalmentTable.setFont(new Font("Montserrat", Font.PLAIN, 10));
        instalmentTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 10));
        instalmentTable.getTableHeader().setBackground(new Color(245, 245, 245));

        JScrollPane instalmentScroll = new JScrollPane(instalmentTable);
        instalmentScroll.setBounds(20, 185, 800, 280);
        instalmentScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(instalmentScroll);
    }

    private void createPaymentForm() {
        // Section label
        JLabel paymentLabel = new JLabel("3. Tambah Pembayaran");
        paymentLabel.setFont(new Font("Montserrat", Font.BOLD, 13));
        paymentLabel.setForeground(new Color(30, 30, 30));
        paymentLabel.setBounds(20, 475, 170, 16);
        add(paymentLabel);

        // Form panel
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBounds(20, 493, 400, 112);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(6, 6, 6, 6)
        ));
        add(formPanel);

        // Tenor info
        JLabel tenorLabel = new JLabel("Tenor ke-");
        tenorLabel.setFont(new Font("Montserrat", Font.PLAIN, 10));
        tenorLabel.setBounds(5, 5, 50, 16);
        formPanel.add(tenorLabel);

        nextTenorLabel = new JLabel("-");
        nextTenorLabel.setFont(new Font("Montserrat", Font.BOLD, 10));
        nextTenorLabel.setForeground(new Color(39, 49, 157));
        nextTenorLabel.setBounds(60, 5, 60, 16);
        formPanel.add(nextTenorLabel);

        // Amount info
        JLabel amountLabel = new JLabel("Jumlah:");
        amountLabel.setFont(new Font("Montserrat", Font.PLAIN, 10));
        amountLabel.setBounds(5, 25, 40, 16);
        formPanel.add(amountLabel);

        paymentAmountLabel = new JLabel("-");
        paymentAmountLabel.setFont(new Font("Montserrat", Font.BOLD, 10));
        paymentAmountLabel.setForeground(new Color(39, 49, 157));
        paymentAmountLabel.setBounds(50, 25, 120, 16);
        formPanel.add(paymentAmountLabel);

        // Date input
        JLabel dateLabel = new JLabel("Tanggal:");
        dateLabel.setFont(new Font("Montserrat", Font.PLAIN, 10));
        dateLabel.setBounds(5, 45, 45, 16);
        formPanel.add(dateLabel);

        dateField = new JTextField(LocalDate.now().toString());
        dateField.setBounds(50, 45, 120, 16);
        formPanel.add(dateField);

        // Buttons
        payButton = new JButton("Bayar");
        payButton.setBounds(5, 75, 70, 25);
        payButton.setBackground(new Color(40, 167, 69));
        payButton.setForeground(Color.WHITE);
        payButton.setFont(new Font("Montserrat", Font.BOLD, 9));
        payButton.setFocusPainted(false);
        payButton.setOpaque(true);
        payButton.setBorderPainted(false);
        payButton.setEnabled(false);
        payButton.addActionListener(e -> handlePayment());
        formPanel.add(payButton);

        cancelButton = new JButton("Batal");
        cancelButton.setBounds(80, 75, 70, 25);
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Montserrat", Font.BOLD, 9));
        cancelButton.setFocusPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> clearSelection());
        formPanel.add(cancelButton);
    }

    private void loadData() {
        loadContractData();
    }

    private void loadContractData() {
        if (contractTableModel == null) return;
        
        try {
            contractTableModel.setRowCount(0);
            
            List<Contract> contracts = contractController.getAllKontrak();
            
            for (Contract contract : contracts) {
                String status = contract.isStatus() ? "Aktif" : "Lunas";
                contractTableModel.addRow(new Object[]{
                    contract.getIdKontrak(),
                    contract.getNamaPeminjam(),
                    String.format("Rp %,d", contract.getTotal()),
                    String.format("Rp %,d", contract.getCicilanPerBulan()),
                    contract.getTenor() + "x",
                    status
                });
            }
            
        } catch (Exception e) {
            showErrorMessage("Error memuat data kontrak: " + e.getMessage());
        }
    }

    private void handleContractSelection() {
        int selectedRow = contractTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int contractId = (Integer) contractTableModel.getValueAt(selectedRow, 0);
                selectedContract = contractController.getKontrakById(contractId);
                
                if (selectedContract != null) {
                    updateContractSelection();
                    loadInstalmentData(contractId);
                }
            } catch (Exception e) {
                showErrorMessage("Error memilih kontrak: " + e.getMessage());
            }
        }
    }

    private void updateContractSelection() {
        if (selectedContract != null) {
            selectedContractLabel.setText(String.format("Kontrak: %s (ID: %d)", 
                selectedContract.getNamaPeminjam(), 
                selectedContract.getIdKontrak()));
            
            paymentAmountLabel.setText(String.format("Rp %,d", selectedContract.getCicilanPerBulan()));
            
            int nextTenor = instalmentController.getNextTenor(selectedContract.getIdKontrak());
            if (nextTenor > selectedContract.getTenor()) {
                nextTenorLabel.setText("LUNAS");
                payButton.setEnabled(false);
            } else {
                nextTenorLabel.setText(String.valueOf(nextTenor));
                payButton.setEnabled(true);
                
                // Set minimum date (kalo kurang dari tgl bayar terakhir akan error)
                LocalDate minDate = instalmentController.getMinimumPaymentDate(selectedContract.getIdKontrak());
                if (minDate.isAfter(LocalDate.now())) {
                    dateField.setText(minDate.toString());
                }
            }
        }
    }

    private void loadInstalmentData(int contractId) {
        if (instalmentTableModel == null) return;
        
        try {
            instalmentTableModel.setRowCount(0);
            
            List<Instalment> instalments = instalmentController.getInstalmentsByContract(contractId);
            
            for (Instalment instalment : instalments) {
                instalmentTableModel.addRow(new Object[]{
                    instalment.getTenorDisplay(),
                    instalment.getFormattedAmount(),
                    instalment.getTanggalCicilan(),
                    "Staff ID: " + instalment.getIdStaff()
                });
            }
            
        } catch (Exception e) {
            showErrorMessage("Error memuat data cicilan: " + e.getMessage());
        }
    }

    private void handlePayment() {
        if (selectedContract == null) {
            showWarningMessage("Silakan pilih kontrak terlebih dahulu.");
            return;
        }

        try {
            // Parse input
            LocalDate paymentDate = LocalDate.parse(dateField.getText().trim());
            int tenor = instalmentController.getNextTenor(selectedContract.getIdKontrak());
            int amount = selectedContract.getCicilanPerBulan();

            // Get current user
            User currentUser = authController.getCurrentUser();
            if (currentUser == null) {
                showErrorMessage("User tidak dikenali.");
                return;
            }

            // Validate payment
            if (!instalmentController.validatePayment(selectedContract.getIdKontrak(), tenor, paymentDate)) {
                LocalDate minDate = instalmentController.getMinimumPaymentDate(selectedContract.getIdKontrak());
                showWarningMessage(String.format(
                    "Tanggal pembayaran tidak valid.\nMinimal tanggal: %s", minDate));
                return;
            }

            // Process payment
            boolean success = instalmentController.addInstalment(
                selectedContract.getIdKontrak(), 
                amount, 
                tenor, 
                paymentDate, 
                currentUser.getId_user());

            if (success) {
                showSuccessMessage("Cicilan berhasil dibayar.");
                refreshData();
            } else {
                showErrorMessage("Gagal menyimpan cicilan. Silakan coba lagi.");
            }

        } catch (DateTimeParseException e) {
            showWarningMessage("Format tanggal tidak valid. Gunakan format YYYY-MM-DD.");
        } catch (Exception e) {
            showErrorMessage("Error memproses pembayaran: " + e.getMessage());
        }
    }

    private void clearSelection() {
        contractTable.clearSelection();
        selectedContract = null;
        selectedContractLabel.setText("Belum ada kontrak dipilih");
        paymentAmountLabel.setText("-");
        nextTenorLabel.setText("-");
        dateField.setText(LocalDate.now().toString());
        payButton.setEnabled(false);
        
        if (instalmentTableModel != null) {
            instalmentTableModel.setRowCount(0);
        }
    }

    private void refreshData() {
        loadContractData();
        if (selectedContract != null) {
            loadInstalmentData(selectedContract.getIdKontrak());
            selectedContract = contractController.getKontrakById(selectedContract.getIdKontrak());
            updateContractSelection();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && contractTableModel != null) {
            refreshData();
        }
    }

    // Utility methods for messages
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}