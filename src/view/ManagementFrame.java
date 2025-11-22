package view;

import controller.UserController;

import javax.swing.*;
import java.awt.*;

public class ManagementFrame extends JFrame {

    private UserController userController;

    public ManagementFrame() {
        super("MediTask");
        this.userController = new UserController();

        ImageIcon window_icon = new ImageIcon("src/assets/person.png");
        this.setIconImage(window_icon.getImage());

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Do stuff

                // Then close the application
                dispose();
                System.exit(0);
            }
        });
        this.setSize(1280, 720);
        this.setVisible(true);
    }

    // Static Helper Methods Below

    public static void buildButton(JButton button) {
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.LIGHT_GRAY);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }

    public static void buildTextfield(JTextField textfield) {
        textfield.setPreferredSize(new Dimension(120, 30));
        textfield.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textfield.setBackground(Color.DARK_GRAY);
        textfield.setForeground(Color.LIGHT_GRAY);
        textfield.setFont(new Font("SansSerif", Font.BOLD, 14));
    }

    public static void buildRadioButton(JRadioButton radio) {
        radio.setBackground(new Color(22, 22, 24));
        radio.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        radio.setForeground(Color.LIGHT_GRAY);
        radio.setFont(new Font("SansSerif", Font.BOLD, 14));
        radio.setFocusPainted(false);
        radio.setBorderPainted(false);
    }

}
