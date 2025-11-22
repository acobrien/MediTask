package view;

import controller.EmployeeController;
import controller.GroupController;
import controller.TaskController;
import view.panels.ManagerPanel;
import view.panels.LaborerPanel;
import view.panels.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class ManagementFrame extends JFrame {

    private EmployeeController employeeController;
    private TaskController taskController;
    private GroupController groupController;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    public ManagementFrame() {
        super("MediTask");
        this.employeeController = new EmployeeController();
        this.taskController = new TaskController();
        this.groupController = new GroupController(this);

        buildPanel();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Do stuff before close

                // Then close the application
                dispose();
                System.exit(0);
            }
        });
        setSize(1280, 720);
        refreshPanels("LoginPanel");
        setVisible(true);
    }

    private void buildPanel() {
        ImageIcon window_icon = new ImageIcon("src/assets/person.png");
        this.setIconImage(window_icon.getImage());

        ManagerPanel managerPanel = new ManagerPanel(this);
        LaborerPanel laborerPanel = new LaborerPanel(this);
        LoginPanel loginPanel = new LoginPanel(this);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(loginPanel, "LoginPanel");
        cardPanel.add(managerPanel, "ManagerPanel");
        cardPanel.add(laborerPanel, "LaborerPanel");

        add(cardPanel);
    }

    public void refreshPanels(String panelName) {
        cardPanel.removeAll();

        ManagerPanel managerPanel = new ManagerPanel(this);
        LaborerPanel laborerPanel = new LaborerPanel(this);
        LoginPanel loginPanel = new LoginPanel(this);

        cardPanel.add(loginPanel, "LoginPanel");
        cardPanel.add(managerPanel, "ManagerPanel");
        cardPanel.add(laborerPanel, "LaborerPanel");

        cardPanel.revalidate();
        cardPanel.repaint();

        cardLayout.show(cardPanel, panelName);
    }

    public EmployeeController getEmployeeController() {
        return employeeController;
    }

    public TaskController getTaskController() { return taskController; }
    public GroupController getGroupController() { return groupController; }

}
