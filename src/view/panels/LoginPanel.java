package view.panels;

import view.ManagementFrame;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private ManagementFrame managementFrame;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginPanel(ManagementFrame managementFrame) {
        this.managementFrame = managementFrame;
        buildPanel();
    }

    private void buildPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Title row
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Username Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Username:"), gbc);

        // Username Field
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Password Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);

        // Password Field
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(loginButton, gbc);
    }

    private void handleLogin() {
        String usernameText = usernameField.getText();
        String passwordText = new String(passwordField.getPassword());

        switch(managementFrame.getEmployeeController().validateLogin(usernameText, passwordText)) {
            case -1:
                JOptionPane.showMessageDialog(null, "Invalid username or password");
                break;
            case 0:
                managementFrame.showPanel("ManagerPanel");
                break;
            case 1:
                managementFrame.showPanel("LaborerPanel");
                break;
        }
    }
}
