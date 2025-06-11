package views;

import controllers.AuthController;
import controllers.ContractController;
import controllers.InstalmentController;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import models.Cicilan;
import models.Contract;
import models.User;

public class InstalmentView extends JPanel {
    private final InstalmentController instalmentController;
    private final ContractController contractController;
    private final AuthController authController;
    
    // Components untuk daftar kontrak
    private JTable kontrakTable;
    private DefaultTableModel kontrakTableModel;
    
    // Components untuk cicilan kontrak yang dipilih
    private JTable cicilanTable;
    private DefaultTableModel cicilanTableModel;
    
    // Components untuk tambah cicilan
    private JTextField inputTenor, inputTanggal;
    private JLabel labelKontrakTerpilih, labelJumlahCicilan, labelTenorSelanjutnya;
    private JButton buttonBayar, buttonCancel;
    
    // Data kontrak yang sedang dipilih
    private Contract selectedKontrak = null;

    public InstalmentView(AuthController authController) {
        this.authController = authController;
        this.instalmentController = new InstalmentController();
        this.contractController = new ContractController();

        setLayout(null);
        setBackground(new Color(248, 249, 251));
        setPreferredSize(new Dimension(900, 600));
        setVisible(true);
        setName("cicilan");

        initializeComponents();
        loadKontrakData();
    }

    private void initializeComponents() {
        JLabel titleLabel = new JLabel("Pembayaran Cicilan");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 20)); 
        titleLabel.setForeground(new Color(39, 49, 157)); 
        titleLabel.setBounds(20, 8, 300, 22);
        add(titleLabel);

        JLabel kontrakLabel = new JLabel("1. Pilih Kontrak");
        kontrakLabel.setFont(new Font("Montserrat", Font.BOLD, 13)); 
        kontrakLabel.setForeground(new Color(30, 30, 30));
        kontrakLabel.setBounds(20, 35, 180, 16); 
        add(kontrakLabel);

        String[] kontrakColumns = {"ID", "Nama", "Total", "Cicilan/Bln", "Tenor", "Status"};
        kontrakTableModel = new DefaultTableModel(kontrakColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        kontrakTable = new JTable(kontrakTableModel);
        kontrakTable.setRowHeight(20); 
        kontrakTable.setFont(new Font("Montserrat", Font.PLAIN, 10)); 
        kontrakTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 10));
        kontrakTable.getTableHeader().setBackground(new Color(245, 245, 245));
        kontrakTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        kontrakTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleKontrakSelection();
            }
        });

        JScrollPane kontrakScroll = new JScrollPane(kontrakTable);
        kontrakScroll.setBounds(20, 55, 800, 85); 
        kontrakScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(kontrakScroll);

        JLabel cicilanLabel = new JLabel("2. Detail Cicilan");
        cicilanLabel.setFont(new Font("Montserrat", Font.BOLD, 13)); 
        cicilanLabel.setForeground(new Color(30, 30, 30));
        cicilanLabel.setBounds(20, 150, 180, 16); 
        add(cicilanLabel);

        labelKontrakTerpilih = new JLabel("Belum ada kontrak dipilih");
        labelKontrakTerpilih.setFont(new Font("Montserrat", Font.ITALIC, 10)); 
        labelKontrakTerpilih.setForeground(new Color(100, 100, 100));
        labelKontrakTerpilih.setBounds(20, 168, 350, 14);
        add(labelKontrakTerpilih);

        String[] cicilanColumns = {"Tenor", "Jumlah", "Tanggal", "Staff"};
        cicilanTableModel = new DefaultTableModel(cicilanColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cicilanTable = new JTable(cicilanTableModel);
        cicilanTable.setRowHeight(20); 
        cicilanTable.setFont(new Font("Montserrat", Font.PLAIN, 10));
        cicilanTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 10));
        cicilanTable.getTableHeader().setBackground(new Color(245, 245, 245));

        // Perbesar tabel cicilan ke bawah lagi
        JScrollPane cicilanScroll = new JScrollPane(cicilanTable);
        cicilanScroll.setBounds(20, 185, 800, 280);
        cicilanScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(cicilanScroll);

        JLabel tambahLabel = new JLabel("3. Tambah Pembayaran");
        tambahLabel.setFont(new Font("Montserrat", Font.BOLD, 13)); 
        tambahLabel.setForeground(new Color(30, 30, 30));
        tambahLabel.setBounds(20, 475, 170, 16);
        add(tambahLabel);

        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBounds(20, 493, 400, 112);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(6, 6, 6, 6) 
        ));
        add(formPanel);

        JLabel tenorLabel = new JLabel("Tenor ke-");
        tenorLabel.setFont(new Font("Montserrat", Font.PLAIN, 10));
        tenorLabel.setBounds(5, 5, 50, 16);
        formPanel.add(tenorLabel);

        labelTenorSelanjutnya = new JLabel("-");
        labelTenorSelanjutnya.setFont(new Font("Montserrat", Font.BOLD, 10));
        labelTenorSelanjutnya.setForeground(new Color(39, 49, 157));
        labelTenorSelanjutnya.setBounds(60, 5, 60, 16);
        formPanel.add(labelTenorSelanjutnya);

        JLabel jumlahLabel = new JLabel("Jumlah:");
        jumlahLabel.setFont(new Font("Montserrat", Font.PLAIN, 10));
        jumlahLabel.setBounds(5, 25, 40, 16); 
        formPanel.add(jumlahLabel);

        labelJumlahCicilan = new JLabel("-");
        labelJumlahCicilan.setFont(new Font("Montserrat", Font.BOLD, 10));
        labelJumlahCicilan.setForeground(new Color(39, 49, 157));
        labelJumlahCicilan.setBounds(50, 25, 120, 16);
        formPanel.add(labelJumlahCicilan);

        JLabel tanggalLabel = new JLabel("Tanggal:");
        tanggalLabel.setFont(new Font("Montserrat", Font.PLAIN, 10));
        tanggalLabel.setBounds(5, 45, 45, 16);
        formPanel.add(tanggalLabel);

        inputTanggal = new JTextField(LocalDate.now().toString());
        inputTanggal.setBounds(50, 45, 120, 16); 
        formPanel.add(inputTanggal);

        buttonBayar = new JButton("Bayar");
        buttonBayar.setBounds(5, 75, 70, 25);
        buttonBayar.setBackground(new Color(40, 167, 69));
        buttonBayar.setForeground(Color.WHITE);
        buttonBayar.setFont(new Font("Montserrat", Font.BOLD, 9)); 
        buttonBayar.setFocusPainted(false);
        buttonBayar.setOpaque(true);
        buttonBayar.setBorderPainted(false);
        buttonBayar.setEnabled(false);
        formPanel.add(buttonBayar);

        buttonCancel = new JButton("Batal");
        buttonCancel.setBounds(80, 75, 70, 25); 
        buttonCancel.setBackground(new Color(220, 53, 69));
        buttonCancel.setForeground(Color.WHITE);
        buttonCancel.setFont(new Font("Montserrat", Font.BOLD, 9));
        buttonCancel.setFocusPainted(false);
        buttonCancel.setOpaque(true);
        buttonCancel.setBorderPainted(false);
        formPanel.add(buttonCancel);

        // Event listeners
        buttonBayar.addActionListener(e -> handleBayar());
        buttonCancel.addActionListener(e -> clearSelection());
    }

    private void loadKontrakData() {
        try {
            kontrakTableModel.setRowCount(0);
            
            List<Contract> kontrakList = contractController.getAllKontrak();
            System.out.println("Loaded " + kontrakList.size() + " contracts");
            
            for (Contract kontrak : kontrakList) {
                String status = kontrak.isStatus() ? "Belum Lunas" : "Lunas";
                kontrakTableModel.addRow(new Object[]{
                    kontrak.getIdKontrak(),
                    kontrak.getNamaPeminjam(),
                    String.format("%,d", kontrak.getTotal()),
                    String.format("%,d", kontrak.getCicilanPerBulan()),
                    kontrak.getTenor() + "x",
                    status
                });
            }
            
        } catch (Exception e) {
            System.err.println("Error loading kontrak data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleKontrakSelection() {
        int selectedRow = kontrakTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int idKontrak = (Integer) kontrakTableModel.getValueAt(selectedRow, 0);
                selectedKontrak = contractController.getKontrakById(idKontrak);
                
                if (selectedKontrak != null) {
                    updateKontrakSelection();
                    loadCicilanData(idKontrak);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateKontrakSelection() {
        if (selectedKontrak != null) {
            labelKontrakTerpilih.setText("Kontrak: " + selectedKontrak.getNamaPeminjam() + 
                                       " (ID: " + selectedKontrak.getIdKontrak() + ")");
            labelJumlahCicilan.setText("Rp " + String.format("%,d", selectedKontrak.getCicilanPerBulan()));
            
            int tenorSelanjutnya = calculateNextTenor(selectedKontrak.getIdKontrak());
            if (tenorSelanjutnya > selectedKontrak.getTenor()) {
                labelTenorSelanjutnya.setText("LUNAS");
                buttonBayar.setEnabled(false);
            } else {
                labelTenorSelanjutnya.setText(String.valueOf(tenorSelanjutnya));
                buttonBayar.setEnabled(true);
            }
        }
    }

    private int calculateNextTenor(int idKontrak) {
        try {
            return instalmentController.getNextTenor(idKontrak);
        } catch (Exception e) {
            return 1;
        }
    }

    private void loadCicilanData(int idKontrak) {
        try {
            cicilanTableModel.setRowCount(0);
            
            List<Cicilan> cicilanList = instalmentController.getCicilanByKontrak(idKontrak);
            
            for (Cicilan cicilan : cicilanList) {
                cicilanTableModel.addRow(new Object[]{
                    "Tenor ke-" + cicilan.getTenor(),
                    "Rp " + String.format("%,d", cicilan.getJumlahCicilan()),
                    cicilan.getTanggalCicilan(),
                    "Staff ID: " + cicilan.getIdStaff()
                });
            }
            
        } catch (Exception e) {
            System.err.println("Error loading cicilan data: " + e.getMessage());
        }
    }

    private void handleBayar() {
        if (selectedKontrak == null) {
            JOptionPane.showMessageDialog(this, "Silakan pilih kontrak terlebih dahulu.");
            return;
        }

        try {
            int tenor = calculateNextTenor(selectedKontrak.getIdKontrak());
            LocalDate tanggal = LocalDate.parse(inputTanggal.getText().trim());
            int jumlah = selectedKontrak.getCicilanPerBulan();

            User currentUser = authController.getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "User tidak dikenali.");
                return;
            }

            int idStaff = currentUser.getId_user();
            boolean success = instalmentController.tambahCicilan(
                selectedKontrak.getIdKontrak(), jumlah, tenor, tanggal, idStaff);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Cicilan berhasil dibayar.");
                
                // Refresh semua data untuk update status
                loadKontrakData(); // Refresh daftar kontrak
                loadCicilanData(selectedKontrak.getIdKontrak()); // Refresh cicilan
                
                // Re-select kontrak untuk update status display
                selectedKontrak = contractController.getKontrakById(selectedKontrak.getIdKontrak());
                updateKontrakSelection();
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Gagal menyimpan cicilan. Periksa tenor atau status kontrak.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Input tidak valid. Pastikan tanggal dalam format YYYY-MM-DD.");
        }
    }

    private void clearSelection() {
        kontrakTable.clearSelection();
        selectedKontrak = null;
        labelKontrakTerpilih.setText("Belum ada kontrak dipilih");
        labelJumlahCicilan.setText("-");
        labelTenorSelanjutnya.setText("-");
        inputTanggal.setText(LocalDate.now().toString());
        buttonBayar.setEnabled(false);
        cicilanTableModel.setRowCount(0);
    }
    

    public void refreshKontrakData() {
        loadKontrakData();
    }


    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            loadKontrakData(); // Auto refresh saat panel ditampilkan
        }
    }
}
