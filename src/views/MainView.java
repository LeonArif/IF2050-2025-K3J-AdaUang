package views;

import config.AppConstants;
import controllers.AuthController;
import controllers.UserController;
import java.awt.*;
import javax.swing.*;
import models.User;

public class MainView extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private AuthController authController;
    private UserController userController;
    private User currentUser;

    public MainView(AuthController authController) {
        this.authController = authController;
        this.userController = new UserController();
        this.currentUser = authController.getCurrentUser();

        setTitle("AdaUang - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(new ImageIcon(getClass().getResource("/assets/icon.png")).getImage());

        SidebarView.SidebarListener sidebarListener = new SidebarView.SidebarListener() {
            @Override
            public void menuSelected(String menuName) {
                switchPanel(menuName);
            }

            @Override
            public void logoutRequested() {
                handleLogout();
            }
        };

        SidebarView sidebar = new SidebarView(currentUser, sidebarListener);
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new ContractView(authController), "kontrak");
        mainPanel.add(new InstalmentView(authController), "cicilan");
        mainPanel.add(new AgingReportView(authController), "aging-report");

        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            mainPanel.add(new UserManagementView(userController, authController), "user_management");
        }

        add(mainPanel, BorderLayout.CENTER);

        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            cardLayout.show(mainPanel, "user_management");
        } else {
            cardLayout.show(mainPanel, "kontrak");
        }
    }

    private void handleLogout() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin logout?",
            "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            authController.logout();
            new LoginView(authController).setVisible(true);
            this.dispose();
        }
    }

    private void switchPanel(String menuName) {
        boolean panelExists = false;
        for (Component comp : mainPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(menuName)) {
                panelExists = true;
                break;
            }
        }
        if (panelExists) {
            cardLayout.show(mainPanel, menuName);
            mainPanel.revalidate(); 
            mainPanel.repaint();    
        } else {
            System.err.println("Panel dengan nama '" + menuName + "' tidak ditemukan.");
        }
    }
}
