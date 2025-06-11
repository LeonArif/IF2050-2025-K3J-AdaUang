package views;

import controllers.AuthController;
import controllers.ContractController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import models.Contract;
import models.User;

public class ContractView extends JPanel {
    private final ContractController contractController;
    private final AuthController authController;
    private JTable contractTable;
    private DefaultTableModel tableModel;
    private ImageIcon optionIcon;
    
    // Komponen untuk fungsionalitas pop-up
    private JLayeredPane layeredPane;
    private JPanel viewDetailPanel;
    private JPanel addContractPanel;
    private JPanel overlayPanel;
    private JTextField namaField, totalField, tenorField;
    

    // Label di dalam panel detail
    private JLabel detailIdKontrak, detailNamaPeminjam, detailUsername, detailTotalPinjaman,
                   detailTenor, detailJumlahBayar, detailStatus, detailTanggal, detailBranch, 
                   detailJumlahBayarTotal, detailCicilanPerBulan;;


    public ContractView(AuthController authController) {
        this.authController = authController;
        this.contractController = new ContractController();
        this.optionIcon = createIcon("/assets/option_icon.png", 18, 18);

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(248, 249, 251));
        setPreferredSize(new Dimension(1000, 700));
        setName("kontrak"); 

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1000, 700));
        add(layeredPane, BorderLayout.CENTER);

        JPanel mainContentPanel = createMainContentPanel();
        mainContentPanel.setBounds(0, 0, 1000, 700);
        layeredPane.add(mainContentPanel, JLayeredPane.DEFAULT_LAYER);

        overlayPanel = new JPanel();
        overlayPanel.setOpaque(false);
        overlayPanel.setBackground(new Color(0, 0, 0, 128));
        overlayPanel.setVisible(false);
        overlayPanel.setBounds(0, 0, 1000, 700); 
        overlayPanel.addMouseListener(new MouseAdapter() {});
        layeredPane.add(overlayPanel, JLayeredPane.MODAL_LAYER);

        addContractPanel = createAddContractPanel();
        viewDetailPanel = createViewDetailPanel();
        // Set bounds untuk panels
        addContractPanel.setBounds(650, 0, 350, 700);
        viewDetailPanel.setBounds(650, 0, 350, 700);
        layeredPane.add(addContractPanel, JLayeredPane.POPUP_LAYER);
        layeredPane.add(viewDetailPanel, JLayeredPane.POPUP_LAYER);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = getSize();
                layeredPane.setSize(size);
                mainContentPanel.setBounds(0, 0, size.width, size.height);
                overlayPanel.setBounds(0, 0, size.width, size.height);
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });
        
        // PENTING: Force layout setelah semua komponen dibuat
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(true); 
        panel.setBackground(new Color(248, 249, 251)); 
        panel.setBorder(new EmptyBorder(24, 32, 32, 32));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(true); 
        topPanel.setBackground(new Color(248, 249, 251)); 
        JLabel title = new JLabel("Manajemen Kontrak");
        title.setFont(new Font("Montserrat", Font.BOLD, 32));
        title.setForeground(new Color(39, 49, 157));
        topPanel.add(title, BorderLayout.WEST);

        JButton addButton = new JButton("+ Tambah Kontrak");
        addButton.setFont(new Font("Montserrat", Font.BOLD, 15));
        addButton.setBackground(new Color(43, 70, 191));
        addButton.setForeground(Color.WHITE);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setFocusPainted(false);
        addButton.setBorder(new EmptyBorder(10, 25, 10, 25));
        addButton.setOpaque(true); 
        addButton.addActionListener(e -> showAddContractPanel());
        topPanel.add(addButton, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createTableContainer(), BorderLayout.CENTER);
        
        loadTableData();
        return panel;
    }

    private JPanel createTableContainer() {
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        
        String[] columns = {"ID", "Nama Peminjam", "Username", "Total Pinjaman", "Cabang", "Status", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 6; }
        };
        contractTable = new JTable(tableModel);
        setupTableStyle();
        
        JScrollPane scrollPane = new JScrollPane(contractTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tableContainer.add(contractTable.getTableHeader(), BorderLayout.NORTH);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        return tableContainer;
    }

    private void setupTableStyle() {
        Color borderColor = new Color(220, 220, 220);
        contractTable.setRowHeight(45);
        contractTable.setFont(new Font("Montserrat", Font.PLAIN, 15));
        contractTable.setSelectionBackground(new Color(235, 240, 255));
        contractTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        contractTable.setShowGrid(true);
        contractTable.setGridColor(borderColor);
        contractTable.setIntercellSpacing(new Dimension(0, 0));

        int statusColumnIndex = 5;
        int actionColumnIndex = 6;
        
        contractTable.getColumnModel().getColumn(statusColumnIndex).setCellRenderer(new StatusCellRenderer());
        contractTable.getColumnModel().getColumn(actionColumnIndex).setCellRenderer(new ActionButtonRenderer());
        contractTable.getColumnModel().getColumn(actionColumnIndex).setCellEditor(new ActionButtonEditor());
        
        contractTable.getColumnModel().getColumn(actionColumnIndex).setPreferredWidth(60);
        contractTable.getColumnModel().getColumn(actionColumnIndex).setMaxWidth(60);
        
        JTableHeader header = contractTable.getTableHeader();
        header.setPreferredSize(new Dimension(100, 50));
        header.setFont(new Font("Montserrat", Font.BOLD, 14));
        header.setBackground(new Color(245, 247, 250));
        header.setForeground(new Color(100, 100, 100));
        header.setBorder(BorderFactory.createLineBorder(borderColor));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for(int i = 0; i < contractTable.getColumnCount(); i++){
            if(i != statusColumnIndex && i != actionColumnIndex) {
                 contractTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            List<Contract> contracts = contractController.getAllContracts();
            
            // Debug: print jumlah kontrak
            System.out.println("Loading " + contracts.size() + " contracts");
            
            for (Contract contract : contracts) {
                tableModel.addRow(new Object[]{
                    contract.getId_kontrak(),
                    contract.getNama_user(),
                    contract.getUsername(),
                    contract.getFormattedTotal(),
                    contract.getBranch(),
                    contract.isStatus() ? "Active" : "Inactive",
                    ""
                });
            }
            
            // Force repaint table
            SwingUtilities.invokeLater(() -> {
                contractTable.revalidate();
                contractTable.repaint();
            });
            
        } catch (Exception e) {
            System.err.println("Error loading table data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    class ActionButtonRenderer extends DefaultTableCellRenderer {
        public ActionButtonRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setIcon(optionIcon);
            setText("");
            return this;
        }
    }
    
    class ActionButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow;
        public ActionButtonEditor() {
            super(new JCheckBox());
            button = new JButton(optionIcon);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(e -> {
                fireEditingStopped();
                showPopupMenu(currentRow);
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return button;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    class StatusCellRenderer extends DefaultTableCellRenderer {
        public StatusCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setText(value.toString());
            label.setOpaque(true); 
            
            // Atur warna status
            if ("Active".equalsIgnoreCase(value.toString())) {
                label.setBackground(new Color(212, 243, 223));
                label.setForeground(new Color(27, 156, 87));
            } else {
                label.setBackground(new Color(252, 228, 236));
                label.setForeground(new Color(214, 62, 89));
            }
            return label;
        }
    }
    private JPanel createViewDetailPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new MatteBorder(0, 1, 0, 0, new Color(220, 220, 220)));
        panel.setVisible(false);

        ImageIcon closeIcon = createIcon("/assets/close_icon.png", 16, 16);
        JButton closeButton = new JButton(closeIcon);
        closeButton.setBounds(305, 15, 30, 30);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(e -> hideSidePanels());
        panel.add(closeButton);

        JLabel titleLabel = new JLabel("Detail Kontrak");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 22));
        titleLabel.setForeground(new Color(39, 49, 157));
        titleLabel.setBounds(30, 40, 250, 30);
        panel.add(titleLabel);

        int yPos = 90;
        detailIdKontrak = addDetailRow(panel, "ID Kontrak", yPos); yPos += 55;
        detailNamaPeminjam = addDetailRow(panel, "Nama Peminjam", yPos); yPos += 55;
        detailUsername = addDetailRow(panel, "Username Staff (Pembuat)", yPos); yPos += 55;
        detailBranch = addDetailRow(panel, "Cabang", yPos); yPos += 55;
        detailTotalPinjaman = addDetailRow(panel, "Total Pinjaman", yPos); yPos += 55;
        detailTenor = addDetailRow(panel, "Tenor", yPos); yPos += 55;
        detailJumlahBayar = addDetailRow(panel, "Jumlah Terbayar", yPos); yPos += 55;

        // Tambahkan dua row baru di bawah jumlah terbayar:
        detailJumlahBayarTotal = addDetailRow(panel, "Jumlah Bayar Total", yPos); yPos += 55;
        detailCicilanPerBulan = addDetailRow(panel, "Cicilan per Bulan", yPos); yPos += 55;

        detailTanggal = addDetailRow(panel, "Tanggal Pinjam", yPos); yPos += 55;
        detailStatus = addDetailRow(panel, "Status", yPos);

        return panel;
    }
    private void showViewDetailPanel(int contractId) {
        Contract contract = contractController.getContractById(contractId);
        if (contract == null) {
            JOptionPane.showMessageDialog(this, "Gagal memuat detail kontrak.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        detailIdKontrak.setText(String.valueOf(contract.getId_kontrak()));
        detailNamaPeminjam.setText(contract.getNama_user());
        detailUsername.setText(contract.getUsername());
        detailTotalPinjaman.setText(contract.getFormattedTotal());
        detailTenor.setText(contract.getTenor() + " Bulan");
        detailJumlahBayar.setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(contract.getJumlah_bayar()));

        // Tambahan: tampilkan jumlah bayar total dan cicilan per bulan
        detailJumlahBayarTotal.setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(contract.getJumlah_bayar_bunga()));
        detailCicilanPerBulan.setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(contract.getCicilan_per_bulan()));

        detailStatus.setText(contract.isStatus() ? "Aktif" : "Tidak Aktif");
        detailTanggal.setText(new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")).format(contract.getTanggal_pinjam()));
        detailBranch.setText(contract.getBranch());

        showSidePanel(viewDetailPanel);
    }

    private void showPopupMenu(int row) {
        int contractId = (int) tableModel.getValueAt(row, 0);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem viewDetailItem = new JMenuItem("Lihat Detail Kontrak");
        viewDetailItem.addActionListener(e -> showViewDetailPanel(contractId));
        menu.add(viewDetailItem);
        Rectangle cellRect = contractTable.getCellRect(row, 6, true);
        menu.show(contractTable, cellRect.x - menu.getPreferredSize().width + cellRect.width, cellRect.y);
    }

    private void showAddContractPanel() {
        namaField.setText("");
        totalField.setText("");
        tenorField.setText("");
        showSidePanel(addContractPanel);
    }

    private void showSidePanel(JPanel panelToShow) {
        hideSidePanels();
        Dimension size = layeredPane.getSize();
        overlayPanel.setBounds(0, 0, size.width, size.height);
        int panelWidth = 350;
        panelToShow.setBounds(size.width - panelWidth, 0, panelWidth, size.height);
        overlayPanel.setVisible(true);
        panelToShow.setVisible(true);
        layeredPane.moveToFront(overlayPanel);
        layeredPane.moveToFront(panelToShow);
    }

    private void hideSidePanels() {
        overlayPanel.setVisible(false);
        viewDetailPanel.setVisible(false);
        addContractPanel.setVisible(false);
    }

    private void handleSubmitContract() {
        try {
            String nama = namaField.getText().trim();
            String totalStr = totalField.getText().trim();
            String tenorStr = tenorField.getText().trim();
            if (nama.isEmpty() || totalStr.isEmpty() || tenorStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field wajib diisi.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int total = Integer.parseInt(totalStr);
            int tenor = Integer.parseInt(tenorStr);
            User currentUser = authController.getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Sesi tidak valid, silakan login ulang.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean success = contractController.addContract(nama, total, tenor, currentUser.getId_user());
            if (success) {
                JOptionPane.showMessageDialog(this, "Kontrak baru berhasil ditambahkan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadTableData();
                hideSidePanels();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambah kontrak.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total Pinjaman dan Tenor harus berupa angka.", "Format Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel createAddContractPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new MatteBorder(0, 1, 0, 0, new Color(220, 220, 220)));
        panel.setVisible(false);

        ImageIcon closeIcon = createIcon("/assets/close_icon.png", 16, 16);
        JButton closeButton = new JButton(closeIcon);
        closeButton.setBounds(305, 15, 30, 30);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(e -> hideSidePanels());
        panel.add(closeButton);

        JLabel titleLabel = new JLabel("Tambah Kontrak Baru");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 22));
        Color titleColor = new Color(39, 49, 157);
        titleLabel.setForeground(titleColor);
        titleLabel.setBounds(30, 40, 280, 30);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.WHITE);
        panel.add(titleLabel);

        int yPos = 90;
        namaField = addFormField(panel, "Nama Peminjam", yPos);
        yPos += 75;
        totalField = addFormField(panel, "Total Pinjaman (Rp)", yPos);
        yPos += 75;
        tenorField = addFormField(panel, "Tenor (Bulan)", yPos);
        yPos += 75;

        JButton saveButton = new JButton("Tambahkan Kontrak");
        saveButton.setFont(new Font("Montserrat", Font.BOLD, 16));
        saveButton.setBackground(new Color(38, 57, 196)); 
        saveButton.setForeground(Color.WHITE);
        saveButton.setBounds(30, yPos, 280, 45);
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(e -> handleSubmitContract());
        panel.add(saveButton);
        return panel;
    }

    private JTextField addFormField(JPanel parent, String title, int y) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setBounds(30, y, 280, 20);
        parent.add(titleLabel);

        JTextField textField = new JTextField();
        textField.setFont(new Font("Montserrat", Font.PLAIN, 16));
        textField.setBounds(30, y + 25, 280, 40);
        parent.add(textField);

        return textField;
    }

    private JLabel addDetailRow(JPanel parent, String title, int y) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setBounds(30, y, 280, 20);
        parent.add(titleLabel);

        JLabel valueLabel = new JLabel("-");
        valueLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        valueLabel.setForeground(new Color(50, 50, 50));
        valueLabel.setBounds(30, y + 20, 280, 25);
        parent.add(valueLabel);

        return valueLabel;
    }

    private ImageIcon createIcon(String path, int width, int height) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            System.err.println("Error: Icon resource not found at path: " + path);
            return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        }
        return new ImageIcon(new ImageIcon(resource).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            loadTableData(); // Auto refresh saat panel ditampilkan
        }
    }
}