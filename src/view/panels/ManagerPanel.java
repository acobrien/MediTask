package view.panels;

import view.ManagementFrame;
import model.*;
import controller.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;

public class ManagerPanel extends JPanel {

    private ManagementFrame frame;

    private JComboBox<Employee> employeeBox;
    private JComboBox<Group> groupBox;
    private JComboBox<TaskStatus> statusBox;

    private JTextField taskTitleField;
    private JTextArea taskDescField;

    private JComboBox<Task> taskDropdown;

    // --- NEW COMPONENTS ---
    private JComboBox<TaskStatus> updateStatusBox;
    // ----------------------

    private JComboBox<String> filterBox;

    private JButton logoutButton;
    private JButton viewDetailsButton;

    private boolean updatingTaskDropdown = false;

    public ManagerPanel(ManagementFrame frame) {
        this.frame = frame;
        buildPanel();
        loadData();
        setupInteractiveLogic();
    }

    private void buildPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Outer padding for the whole window

        // --- Top Header (Logout) ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> frame.refreshPanels("LoginPanel"));
        topBar.add(logoutButton);
        add(topBar, BorderLayout.NORTH);

        // --- Main Content (Split Pane) ---

        // 1. Left Panel: Creation Form
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Create New Task"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        leftPanel.add(new JLabel("Title:"), gbc);

        taskTitleField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        leftPanel.add(taskTitleField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHEAST;
        leftPanel.add(new JLabel("Description:"), gbc);

        taskDescField = new JTextArea(5, 20);
        taskDescField.setLineWrap(true);
        taskDescField.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(taskDescField);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 0.3; // Allow height growth
        gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(descScroll, gbc);

        // Reset constraints for standard rows
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;

        // Separator
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        leftPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Assign Employee
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        leftPanel.add(new JLabel("Assign Employee:"), gbc);

        employeeBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        leftPanel.add(employeeBox, gbc);

        // Assign Group
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        leftPanel.add(new JLabel("Assign Group:"), gbc);

        groupBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        leftPanel.add(groupBox, gbc);

        // View Details Button (Small button under assignments)
        viewDetailsButton = new JButton("View Selected Details");
        viewDetailsButton.addActionListener(e -> showDetails());
        gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        leftPanel.add(viewDetailsButton, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(new JLabel("Initial Status:"), gbc);

        statusBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        leftPanel.add(statusBox, gbc);

        // Spacer to push create button to bottom
        gbc.gridx = 0; gbc.gridy = 7; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(Box.createGlue(), gbc);

        // Create Button
        JButton createButton = new JButton("Create Task");
        createButton.setFont(createButton.getFont().deriveFont(Font.BOLD, 14f));
        createButton.setPreferredSize(new Dimension(200, 45));
        createButton.addActionListener(e -> createTask());

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        leftPanel.add(createButton, gbc);


        // 2. Right Panel: Task List & Filter
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Existing Tasks"));

        GridBagConstraints rhs = new GridBagConstraints();
        rhs.insets = new Insets(5, 5, 5, 5);

        // Filter Row
        rhs.gridx = 0; rhs.gridy = 0; rhs.anchor = GridBagConstraints.EAST;
        rightPanel.add(new JLabel("Filter View:"), rhs);

        filterBox = new JComboBox<>(new String[]{"All Tasks", "My Tasks"});
        filterBox.addActionListener(e -> refreshTaskDropdown());
        rhs.gridx = 1; rhs.gridy = 0; rhs.weightx = 1.0; rhs.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(filterBox, rhs);

        // Task Dropdown Label
        rhs.gridx = 0; rhs.gridy = 1; rhs.weightx = 0; rhs.fill = GridBagConstraints.NONE;
        rightPanel.add(new JLabel("Select Task:"), rhs);

        // Task Dropdown
        taskDropdown = new JComboBox<>();
        taskDropdown.addActionListener(e -> {
            if (updatingTaskDropdown) return;

            Object selected = taskDropdown.getSelectedItem();
            if (selected instanceof PlaceholderTask) return;

            showSelectedTaskDetails();
        });
        rhs.gridx = 1; rhs.gridy = 1; rhs.weightx = 1.0; rhs.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(taskDropdown, rhs);

        // --- NEW: Update Status Section ---
        rhs.gridx = 0; rhs.gridy = 2; rhs.weightx = 0; rhs.fill = GridBagConstraints.NONE;
        rightPanel.add(new JLabel("Update Status:"), rhs);

        JPanel updatePanel = new JPanel(new BorderLayout(5, 0));
        updateStatusBox = new JComboBox<>();
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateTaskStatus());

        updatePanel.add(updateStatusBox, BorderLayout.CENTER);
        updatePanel.add(updateButton, BorderLayout.EAST);

        rhs.gridx = 1; rhs.gridy = 2; rhs.weightx = 1.0; rhs.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(updatePanel, rhs);
        // ----------------------------------

        // Spacer to push content to top
        rhs.gridx = 0; rhs.gridy = 3; rhs.weighty = 1.0; rhs.gridwidth = 2; rhs.fill = GridBagConstraints.BOTH;
        rightPanel.add(Box.createGlue(), rhs);


        // --- Final Assembly ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400); // Give the form a bit more room
        splitPane.setResizeWeight(0.4);

        add(splitPane, BorderLayout.CENTER);
    }

    private void loadData() {
        // Employees
        employeeBox.addItem(null);
        frame.getEmployeeController().getEmployees().values()
                .forEach(employeeBox::addItem);

        // Groups
        groupBox.addItem(null);
        frame.getGroupController().getGroups()
                .forEach(groupBox::addItem);

        // Statuses
        frame.getTaskController().getStatuses()
                .forEach(statusBox::addItem);
        frame.getTaskController().getStatuses()
                .forEach(updateStatusBox::addItem);

        // Populate the dropdown immediately
        refreshTaskDropdown();

        // Friendly null rendering
        ListCellRenderer<Object> friendlyRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String text = (value == null) ? "Select" : value.toString();
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        };
        employeeBox.setRenderer((ListCellRenderer)friendlyRenderer);
        groupBox.setRenderer((ListCellRenderer)friendlyRenderer);
    }

    /**
     * Sets up logic to ensure mutual exclusivity.
     * If an Employee is selected, Group is deselected, and vice versa.
     */
    private void setupInteractiveLogic() {
        employeeBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && employeeBox.getSelectedItem() != null) {
                // Remove listener from group temporarily to prevent recursion loops
                // or simply set selected item to null
                groupBox.setSelectedItem(null);
            }
        });

        groupBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && groupBox.getSelectedItem() != null) {
                employeeBox.setSelectedItem(null);
            }
        });
    }

    private static class PlaceholderTask extends Task {
        public PlaceholderTask() { super("","",null); }
        @Override public String toString() { return " — Select a Task — "; }
    }

    private void refreshTaskDropdown() {
        updatingTaskDropdown = true;

        taskDropdown.removeAllItems();
        taskDropdown.addItem(new PlaceholderTask());  // <-- dummy choice

        Employee currentUser = frame.getEmployeeController().getCurrentUser();
        String filter = (String) filterBox.getSelectedItem();

        for (Task t : frame.getTaskController().getTasks()) {
            if ("All Tasks".equals(filter)) {
                taskDropdown.addItem(t);
            } else if ("My Tasks".equals(filter)) {
                boolean assignedToMe = t.getAssignee() != null && t.getAssignee().equals(currentUser);
                boolean inMyGroup = t.getGroup() != null && t.getGroup().getMembers().contains(currentUser);

                if (assignedToMe || inMyGroup) {
                    taskDropdown.addItem(t);
                }
            }
        }

        updatingTaskDropdown = false;
    }

    private void showSelectedTaskDetails() {
        Task selected = (Task) taskDropdown.getSelectedItem();
        if (selected == null) return;

        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(selected.getTitle()).append("\n");
        details.append("Description: ").append(selected.getDescription()).append("\n");
        details.append("Status: ").append(selected.getStatus()).append("\n");
        if (selected.getAssignee() != null) {
            details.append("Assigned to: ").append(selected.getAssignee()).append("\n");
        }
        if (selected.getGroup() != null) {
            details.append("Group: ").append(selected.getGroup()).append("\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        scrollPane.setBorder(null);

        JOptionPane.showMessageDialog(this, scrollPane, "Task Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- NEW METHOD: Update Task Status ---
    private void updateTaskStatus() {
        Task selectedTask = (Task) taskDropdown.getSelectedItem();
        TaskStatus newStatus = (TaskStatus) updateStatusBox.getSelectedItem();

        if (selectedTask == null) {
            JOptionPane.showMessageDialog(this, "Please select a task first.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newStatus == null) {
            JOptionPane.showMessageDialog(this, "Please select a status.", "No Status Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedTask.setStatus(newStatus);
        // Repaint the dropdown so the text (which includes the status) updates visually
        taskDropdown.repaint();

        JOptionPane.showMessageDialog(this, "Status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    // ---------------------------------------

    private void createTask() {
        updatingTaskDropdown = true;

        String title = taskTitleField.getText().trim();
        String desc = taskDescField.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task title required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        TaskStatus status = (TaskStatus) statusBox.getSelectedItem();
        Employee e = (Employee) employeeBox.getSelectedItem();
        Group g = (Group) groupBox.getSelectedItem();

        // Validation: Ensure exactly one assignment target is selected
        if (e == null && g == null) {
            JOptionPane.showMessageDialog(this, "Please select either an Employee OR a Group.", "Missing Assignment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Note: (e != null && g != null) is technically impossible due to setupInteractiveLogic(),
        // but good to keep for data integrity.
        if (e != null && g != null) {
            JOptionPane.showMessageDialog(this, "Task cannot be assigned to both Employee and Group.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create logic
        Task t = frame.getTaskController().createTask(title, desc, status);

        if (e != null) t.assignEmployee(e);
        if (g != null) t.assignGroup(g);

        // Add to dropdown immediately
        refreshTaskDropdown();  // <-- ensures the dropdown is updated with filters applied

        // Reset inputs
        taskTitleField.setText("");
        taskDescField.setText("");

        updatingTaskDropdown = false;
    }

    private void showDetails() {
        Employee selectedEmp = (Employee) employeeBox.getSelectedItem();
        Group selectedGroup = (Group) groupBox.getSelectedItem();

        if (selectedEmp == null && selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Please select an Employee or Group to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedEmp != null) {
            // Show Employee Popup
            showEmployeePopup(selectedEmp);
        } else {
            // Show Group Popup
            showGroupPopup(selectedGroup);
        }
    }

    private void showEmployeePopup(Employee selected) {
        // Fetch fresh data
        Employee e = frame.getEmployeeController().getEmployees().get(selected.getUsername());
        if (e == null) e = selected; // Fallback

        StringBuilder details = new StringBuilder();
        details.append("Individual Employee\n\n");
        details.append("Name: ").append(e.getFirstName()).append(" ").append(e.getLastName()).append("\n");
        details.append("Birth Date: ").append(e.getBirthDate()).append("\n");
        details.append("Address: ").append(e.getStreetAddress()).append(", ").append(e.getCity()).append(", ").append(e.getState()).append(", ").append(e.getCountry()).append("\n\n");

        details.append("ID: ").append(e.getId()).append("\n");
        details.append("Username: ").append(e.getUsername()).append("\n");
        details.append("Password: ").append(e.getPassword()).append("\n\n");

        details.append("Department: ").append(e.getDepartment()).append("\n");
        details.append("Role: ").append(e.getRole()).append("\n");
        details.append("Hire Date: ").append(e.getHireDate()).append("\n");
        details.append("Salary: ").append(e.getSalary()).append("\n");

        JOptionPane.showMessageDialog(this, details.toString(), "Employee Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showGroupPopup(Group selected) {
        // Fetch fresh data if needed, assuming 'selected' contains members
        StringBuilder details = new StringBuilder();
        details.append("Group Name: ").append(selected.toString()).append("\n\n");

        if(selected.getMembers() == null || selected.getMembers().isEmpty()) {
            details.append("Members: None");
        }
        else {
            details.append("Members (").append(selected.getMembers().size()).append("):\n");
            for(Employee member : selected.getMembers()) {
                details.append(" - ").append(member.getFirstName()).append(" ").append(member.getLastName())
                        .append(" (").append(member.getUsername()).append(")\n");
            }
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setOpaque(false);

        // Using a scroll pane inside the OptionPane in case the group is very large
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        scrollPane.setBorder(null);

        JOptionPane.showMessageDialog(this, scrollPane, "Group Details", JOptionPane.INFORMATION_MESSAGE);
    }
}