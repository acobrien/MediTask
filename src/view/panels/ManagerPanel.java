package view.panels;

import view.ManagementFrame;
import model.*;
import controller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class ManagerPanel extends JPanel {

    private ManagementFrame frame;

    private JComboBox<Employee> employeeBox;
    private JComboBox<Group> groupBox;
    private JComboBox<TaskStatus> statusBox;

    private JTextField taskTitleField;
    private JTextArea taskDescField;

    private DefaultListModel<Task> taskListModel = new DefaultListModel<>();
    private JList<Task> taskList = new JList<>(taskListModel);

    private JComboBox<String> filterBox;

    private JButton viewDetailsButton;

    public ManagerPanel(ManagementFrame frame) {
        this.frame = frame;
        buildPanel();
        loadData();
        setupInteractiveLogic();
    }

    private void buildPanel() {
        setLayout(new BorderLayout());

        // ----- Left: Task creation panel -----
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Create Task"));

        // Task title
        JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
        titlePanel.add(new JLabel("Title:"), BorderLayout.WEST);
        taskTitleField = new JTextField();
        titlePanel.add(taskTitleField, BorderLayout.CENTER);
        leftPanel.add(titlePanel);
        leftPanel.add(Box.createVerticalStrut(10));

        // Description
        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        taskDescField = new JTextArea(4, 20);
        descPanel.add(new JScrollPane(taskDescField), BorderLayout.CENTER);
        leftPanel.add(descPanel);
        leftPanel.add(Box.createVerticalStrut(10));

        // Assignment panel
        JPanel assignPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        assignPanel.add(new JLabel("Assign Employee:"));
        employeeBox = new JComboBox<>();
        assignPanel.add(employeeBox);

        assignPanel.add(new JLabel("Assign Group:"));
        groupBox = new JComboBox<>();
        assignPanel.add(groupBox);

        assignPanel.add(new JLabel("Status:"));
        statusBox = new JComboBox<>();
        assignPanel.add(statusBox);

        leftPanel.add(assignPanel);
        leftPanel.add(Box.createVerticalStrut(10));

        // View Details button (Popup)
        viewDetailsButton = new JButton("View Selected Details");
        viewDetailsButton.addActionListener(e -> showDetails());
        leftPanel.add(viewDetailsButton);
        leftPanel.add(Box.createVerticalStrut(20));

        // Create button
        JButton createButton = new JButton("Create Task");
        createButton.addActionListener(e -> createTask());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        createButton.setPreferredSize(new Dimension(200, 40));
        buttonPanel.add(createButton);
        leftPanel.add(buttonPanel);

        // ----- Right: Task list panel -----
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Tasks"));

        // Filter combo box
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterBox = new JComboBox<>(new String[]{"All Tasks", "My Tasks"});
        filterBox.addActionListener(e -> refreshTaskList());
        filterPanel.add(filterBox);
        rightPanel.add(filterPanel, BorderLayout.NORTH);

        // Task list
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(taskList);
        rightPanel.add(listScroll, BorderLayout.CENTER);

        // ----- Split pane -----
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.4);

        add(splitPane, BorderLayout.CENTER);
    }

    private void loadData() {
        // Employees
        employeeBox.addItem(null); // Default empty option
        frame.getEmployeeController().getEmployees().values()
                .forEach(employeeBox::addItem);

        // Groups
        groupBox.addItem(null); // Default empty option
        frame.getGroupController().getGroups()
                .forEach(groupBox::addItem);

        // Statuses
        frame.getTaskController().getStatuses()
                .forEach(statusBox::addItem);

        // Tasks
        frame.getTaskController().getTasks()
                .forEach(taskListModel::addElement);

        // Friendly null rendering for Dropdowns
        ListCellRenderer<Object> friendlyRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String text;
                if (value == null) {
                    text = "Select";
                } else {
                    text = value.toString();
                }
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

    private void refreshTaskList() {
        taskListModel.clear();

        Employee currentUser = frame.getEmployeeController().getCurrentUser();
        String filter = (String) filterBox.getSelectedItem();

        for (Task t : frame.getTaskController().getTasks()) {
            if ("All Tasks".equals(filter)) {
                taskListModel.addElement(t);
            } else if ("My Tasks".equals(filter)) {
                boolean assignedToMe = t.getAssignee() != null && t.getAssignee().equals(currentUser);
                boolean inMyGroup = t.getGroup() != null && t.getGroup().getMembers().contains(currentUser);

                if (assignedToMe || inMyGroup) {
                    taskListModel.addElement(t);
                }
            }
        }
    }

    private void createTask() {
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

        taskListModel.addElement(t);

        // Reset inputs
        taskTitleField.setText("");
        taskDescField.setText("");
        // Dropdowns remain selected per user workflow preference,
        // or you can reset them:
        // employeeBox.setSelectedItem(null);
        // groupBox.setSelectedItem(null);
        JOptionPane.showMessageDialog(this, "Task created successfully!");
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