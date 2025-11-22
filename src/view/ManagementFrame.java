package view;

import controller.UserController;
import view.panels.AdminPanel;
import view.panels.EmployeePanel;
import view.panels.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class ManagementFrame extends JFrame {

    private UserController userController;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    public ManagementFrame() {
        super("MediTask");
        this.userController = new UserController();

        buildPanel();

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Do stuff before close

                // Then close the application
                dispose();
                System.exit(0);
            }
        });
        this.setSize(1280, 720);
        this.setVisible(true);
    }

    private void buildPanel() {
        ImageIcon window_icon = new ImageIcon("src/assets/person.png");
        this.setIconImage(window_icon.getImage());

        AdminPanel adminPanel = new AdminPanel(this);
        EmployeePanel employeePanel = new EmployeePanel(this);
        LoginPanel loginPanel = new LoginPanel(this);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(loginPanel, "LoginPanel");
        cardPanel.add(adminPanel, "AdminPanel");
        cardPanel.add(employeePanel, "EmployeePanel");

        add(cardPanel);
    }

    public void showPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);
    }

    public void refreshPanels(String panelName) {
        cardPanel.removeAll();

        AdminPanel adminPanel = new AdminPanel(this);
        EmployeePanel employeePanel = new EmployeePanel(this);
        LoginPanel loginPanel = new LoginPanel(this);

        cardPanel.add(loginPanel, "LoginPanel");
        cardPanel.add(adminPanel, "AdminPanel");
        cardPanel.add(employeePanel, "EmployeePanel");

        cardPanel.revalidate();
        cardPanel.repaint();

        cardLayout.show(cardPanel, panelName);
    }

    public void reset() {
        userController = new UserController();

        refreshPanels("Login");
    }

}
