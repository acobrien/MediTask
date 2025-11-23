package view.panels;

import view.ManagementFrame;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LaborerPanel extends JPanel {

    private ManagementFrame frame;

    private JComboBox<Task> taskDropdown;
    private JComboBox<TaskStatus> updateStatusBox;
    private JButton logoutButton;
    private JButton viewDetailsButton;
    private JButton updateButton;

    // Helper flag to prevent listeners firing during data reload
    private boolean updatingTaskDropdown = false;

    public LaborerPanel(ManagementFrame frame) {
        this.frame = frame;
        buildPanel();
        refreshData();
    }

    private void buildPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top Header ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel welcomeLabel = new JLabel("Laborer Dashboard");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> frame.refreshPanels("LoginPanel"));

        // Add a spacer to push the title to the left (optional) or just keep right aligned
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(welcomeLabel, BorderLayout.WEST);
        headerContainer.add(topBar, BorderLayout.EAST);

        topBar.add(logoutButton);

        add(headerContainer, BorderLayout.NORTH);

        // --- Main Content (Center Panel) ---
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("My Assigned Tasks"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Select Task Row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        contentPanel.add(new JLabel("Select Task:"), gbc);

        taskDropdown = new JComboBox<>();

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        contentPanel.add(taskDropdown, gbc);

        // 2. View Details Button (Placed next to dropdown or below)
        viewDetailsButton = new JButton("View Task Details");
        viewDetailsButton.addActionListener(e -> showSelectedTaskDetails());
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        contentPanel.add(viewDetailsButton, gbc);

        // 3. Update Status Row
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        contentPanel.add(new JLabel("Change Status:"), gbc);

        JPanel statusPanel = new JPanel(new BorderLayout(5, 0));
        updateStatusBox = new JComboBox<>();
        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> handleUpdateStatus());

        statusPanel.add(updateStatusBox, BorderLayout.CENTER);
        statusPanel.add(updateButton, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        contentPanel.add(statusPanel, gbc);

        // 4. Filler to push content up
        gbc.gridx = 0; gbc.gridy = 2; gbc.weighty = 1.0;
        contentPanel.add(Box.createGlue(), gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    // Refresh the list of tasks assigned to the user
    private void refreshData() {
        updatingTaskDropdown = true;

        // 1. Load Statuses if empty
        if (updateStatusBox.getItemCount() == 0) {
            frame.getTaskController().getStatuses().forEach(updateStatusBox::addItem);
        }

        // 2. Load Tasks
        taskDropdown.removeAllItems();
        taskDropdown.addItem(new PlaceholderTask());

        Employee currentUser = frame.getEmployeeController().getCurrentUser();
        if (currentUser != null) {
            for (Task t : frame.getTaskController().getTasks()) {
                boolean isAssignedDirectly = t.getAssignee() != null && t.getAssignee().equals(currentUser);
                boolean isAssignedToGroup = t.getGroup() != null && t.getGroup().getMembers().contains(currentUser);

                if (isAssignedDirectly || isAssignedToGroup) {
                    taskDropdown.addItem(t);
                }
            }
        }

        updatingTaskDropdown = false;
    }

    private void handleUpdateStatus() {
        Task selectedTask = (Task) taskDropdown.getSelectedItem();
        TaskStatus newStatus = (TaskStatus) updateStatusBox.getSelectedItem();

        if (selectedTask == null || selectedTask instanceof PlaceholderTask) {
            JOptionPane.showMessageDialog(this, "Please select a task first.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newStatus == null) {
            JOptionPane.showMessageDialog(this, "Please select a status.", "No Status Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Update logic
        selectedTask.setStatus(newStatus);

        // Repaint to show the new status text in the dropdown immediately
        taskDropdown.repaint();

        JOptionPane.showMessageDialog(this, "Task updated to: " + newStatus);
    }

    private void showSelectedTaskDetails() {
        Task selected = (Task) taskDropdown.getSelectedItem();
        if (selected == null || selected instanceof PlaceholderTask) {
            JOptionPane.showMessageDialog(this, "Please select a task first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(selected.getTitle()).append("\n\n");
        details.append("Description:\n").append(selected.getDescription()).append("\n\n");
        details.append("Current Status: ").append(selected.getStatus()).append("\n");

        if (selected.getAssignee() != null) {
            details.append("Assigned to: ").append(selected.getAssignee().getUsername()).append(" (You)\n");
        }
        if (selected.getGroup() != null) {
            details.append("Assigned Group: ").append(selected.getGroup().getName()).append("\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 250));
        scrollPane.setBorder(null);

        JOptionPane.showMessageDialog(this, scrollPane, "Task Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // Helper class for the "Select a Task" default option
    private static class PlaceholderTask extends Task {
        public PlaceholderTask() {
            super("", "", null);
        }

        @Override
        public String toString() {
            return " — Select a Task — ";
        }
    }

}
