package view.panels;

import view.ManagementFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * ManagerPanel - provides basic management UI for:
 *  - viewing employees (loaded from EmployeeController via reflection)
 *  - creating tasks and assigning them to users or groups
 *  - creating/managing groups and statuses
 *
 * This implementation intentionally keeps task/group/status models inside the panel
 * (simple in-memory models) to avoid modifying existing controller classes.
 *
 * Note: We use reflection to read EmployeeController.employees and Employee fields
 *       so the rest of your project does not need changes.
 */
public class ManagerPanel extends JPanel {

    private ManagementFrame managementFrame;

    // Employee UI
    private DefaultListModel<String> employeeListModel;
    private JList<String> employeeList;

    // Task UI
    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;

    // Status UI
    private DefaultListModel<String> statusListModel;
    private JList<String> statusList;

    // Groups UI
    private DefaultListModel<Group> groupListModel;
    private JList<Group> groupList;

    // simple maps for quick lookup
    private Map<String, Group> groups = new TreeMap<>();
    private List<String> statuses = new ArrayList<>();

    // For task id generation
    private int nextTaskId = 1;

    public ManagerPanel(ManagementFrame managementFrame) {
        this.managementFrame = managementFrame;
        initModels();
        buildPanel();
        loadEmployeesFromController();
    }

    private void initModels() {
        employeeListModel = new DefaultListModel<>();
        taskListModel = new DefaultListModel<>();
        statusListModel = new DefaultListModel<>();
        groupListModel = new DefaultListModel<>();

        // default statuses if none are added by admin
        statuses.add("Open");
        statuses.add("In-Progress");
        statuses.add("Complete");
        for (String s : statuses) statusListModel.addElement(s);

        // default admin group
        Group adminGroup = new Group("Admins");
        groups.put(adminGroup.name, adminGroup);
        groupListModel.addElement(adminGroup);
    }

    private void buildPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Title
        JLabel title = new JLabel("Manager Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        // Main split panes
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.25);

        // Left: Employees
        JPanel left = buildEmployeesPanel();
        mainSplit.setLeftComponent(left);

        // Right side split: center (tasks) + right (groups/statuses)
        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setResizeWeight(0.6);

        JPanel center = buildTasksPanel();
        rightSplit.setLeftComponent(center);

        JPanel rightPanel = buildGroupsAndStatusesPanel();
        rightSplit.setRightComponent(rightPanel);

        mainSplit.setRightComponent(rightSplit);

        add(mainSplit, BorderLayout.CENTER);
    }

    // -----------------------
    // Employees panel
    // -----------------------
    private JPanel buildEmployeesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Employees"));

        employeeList = new JList<>(employeeListModel);
        employeeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sc = new JScrollPane(employeeList);
        p.add(sc, BorderLayout.CENTER);

        JPanel btns = new JPanel(new GridLayout(0, 1, 4, 4));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadEmployeesFromController());
        btns.add(refreshBtn);

        JButton detailsBtn = new JButton("Show Details");
        detailsBtn.addActionListener(e -> showSelectedEmployeeDetails());
        btns.add(detailsBtn);

        p.add(btns, BorderLayout.SOUTH);

        return p;
    }

    private void showSelectedEmployeeDetails() {
        String sel = employeeList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an employee first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Attempt to load more fields from Employee via reflection
        try {
            Object emp = findEmployeeObjectByUsername(sel);
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Employee object not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String username = getFieldString(emp, "username");
            String firstName = getFieldString(emp, "firstName");
            String lastName = getFieldString(emp, "lastName");
            String street = getFieldString(emp, "streetAddress");
            String city = getFieldString(emp, "city");
            String state = getFieldString(emp, "state");
            String country = getFieldString(emp, "country");
            String salary = getFieldString(emp, "salary");
            String role = getFieldString(emp, "role");
            String department = getFieldString(emp, "department");
            String hireDate = getFieldString(emp, "hireDate");
            String birthDate = getFieldString(emp, "birthDate");

            StringBuilder sb = new StringBuilder();
            sb.append("username: ").append(username).append("\n");
            sb.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
            sb.append("Role: ").append(role).append("\n");
            sb.append("Department: ").append(department).append("\n");
            sb.append("Salary: ").append(salary).append("\n");
            sb.append("Address: ").append(street).append(", ").append(city).append(", ").append(state).append(", ").append(country).append("\n");
            sb.append("Hire Date: ").append(hireDate).append("\n");
            sb.append("Birth Date: ").append(birthDate).append("\n");

            JOptionPane.showMessageDialog(this, sb.toString(), "Employee Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to retrieve details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -----------------------
    // Tasks panel
    // -----------------------
    private JPanel buildTasksPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Tasks"));

        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Task t = (Task) value;
                String text = String.format("[%d] %s - %s", t.id, t.title, t.status);
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        JScrollPane sc = new JScrollPane(taskList);
        p.add(sc, BorderLayout.CENTER);

        // Creation form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(6, 6, 6, 6));

        JTextField titleField = new JTextField();
        JTextArea descField = new JTextArea(3, 20);
        JTextField assigneeField = new JTextField(); // can be username or group name prefixed with "group:"
        JComboBox<String> statusCombo = new JComboBox<>();
        refreshStatusCombo(statusCombo);

        // Assignment dropdown helpers
        JComboBox<String> assignToCombo = new JComboBox<>();
        assignToCombo.addItem("-- assign to --");
        assignToCombo.addItem("User (select below)");
        assignToCombo.addItem("Group (select below)");

        JButton addTaskBtn = new JButton("Create Task");
        addTaskBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String desc = descField.getText().trim();
            String assignee = assigneeField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // simple validation: if assignee empty that's okay (unassigned)
            Task tsk = new Task(nextTaskId++, title, desc, assignee, status == null ? "Open" : status);
            taskListModel.addElement(tsk);

            // clear fields
            titleField.setText("");
            descField.setText("");
            assigneeField.setText("");
        });

        // Controls to change status of selected task
        JButton cycleStatusBtn = new JButton("Next Status");
        cycleStatusBtn.addActionListener(e -> {
            Task sel = taskList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Select a task first.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // cycle to next available status in our statuses list
            int idx = statuses.indexOf(sel.status);
            if (idx < 0) idx = 0;
            idx = (idx + 1) % statuses.size();
            sel.status = statuses.get(idx);
            taskList.repaint();
        });

        JButton assignSelectedToEmployeeBtn = new JButton("Assign selected -> Selected employee");
        assignSelectedToEmployeeBtn.addActionListener(e -> {
            Task selTask = taskList.getSelectedValue();
            String selEmployee = employeeList.getSelectedValue();
            if (selTask == null || selEmployee == null) {
                JOptionPane.showMessageDialog(this, "Select both a task and an employee.", "Selection needed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selTask.assignee = selEmployee;
            taskList.repaint();
        });

        JButton assignSelectedToGroupBtn = new JButton("Assign selected -> Selected group");
        assignSelectedToGroupBtn.addActionListener(e -> {
            Task selTask = taskList.getSelectedValue();
            Group selGroup = groupList.getSelectedValue();
            if (selTask == null || selGroup == null) {
                JOptionPane.showMessageDialog(this, "Select both a task and a group.", "Selection needed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selTask.assignee = "group:" + selGroup.name;
            taskList.repaint();
        });

        JButton markCompleteBtn = new JButton("Mark Complete");
        markCompleteBtn.addActionListener(e -> {
            Task sel = taskList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Select a task first.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // set to last status if present, otherwise "Complete"
            if (statuses.contains("Complete")) sel.status = "Complete";
            else sel.status = statuses.get(statuses.size()-1);
            taskList.repaint();
        });

        // Layout the form components
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(Box.createVerticalStrut(4));
        form.add(new JLabel("Description:"));
        form.add(new JScrollPane(descField));
        form.add(Box.createVerticalStrut(4));
        form.add(new JLabel("Assignee (username OR group:<groupname>):"));
        form.add(assigneeField);
        form.add(Box.createVerticalStrut(4));
        form.add(new JLabel("Status:"));
        form.add(statusCombo);
        form.add(Box.createVerticalStrut(6));
        JPanel row = new JPanel(new GridLayout(1, 2, 4, 4));
        row.add(addTaskBtn);
        row.add(markCompleteBtn);
        form.add(row);
        form.add(Box.createVerticalStrut(6));
        form.add(cycleStatusBtn);
        form.add(assignSelectedToEmployeeBtn);
        form.add(assignSelectedToGroupBtn);

        p.add(form, BorderLayout.SOUTH);

        return p;
    }

    private void refreshStatusCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        for (String s : statuses) combo.addItem(s);
    }

    // -----------------------
    // Groups & Statuses panel
    // -----------------------
    private JPanel buildGroupsAndStatusesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groups & Statuses"));

        // Split vertically for statuses (top) and groups (bottom)
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.35);

        // Statuses panel
        JPanel statusesPanel = new JPanel(new BorderLayout());
        statusesPanel.setBorder(BorderFactory.createTitledBorder("Task Statuses"));

        statusList = new JList<>(statusListModel);
        JScrollPane ssc = new JScrollPane(statusList);
        statusesPanel.add(ssc, BorderLayout.CENTER);

        JPanel stBtns = new JPanel(new GridLayout(1, 3, 4, 4));
        JTextField newStatusField = new JTextField();
        JButton addStatusBtn = new JButton("Add");
        addStatusBtn.addActionListener(e -> {
            String s = newStatusField.getText().trim();
            if (s.isEmpty()) return;
            if (!statuses.contains(s)) {
                statuses.add(s);
                statusListModel.addElement(s);
                newStatusField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Status already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
            }
        });
        JButton removeStatusBtn = new JButton("Remove");
        removeStatusBtn.addActionListener(e -> {
            String sel = statusList.getSelectedValue();
            if (sel == null) return;
            if (sel.equalsIgnoreCase("Open") || sel.equalsIgnoreCase("In-Progress") || sel.equalsIgnoreCase("Complete")) {
                // allow removal but warn
                int res = JOptionPane.showConfirmDialog(this, "This is a common default status. Remove anyway?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (res != JOptionPane.YES_OPTION) return;
            }
            statuses.remove(sel);
            statusListModel.removeElement(sel);
        });

        stBtns.add(newStatusField);
        stBtns.add(addStatusBtn);
        stBtns.add(removeStatusBtn);
        statusesPanel.add(stBtns, BorderLayout.SOUTH);

        // Groups panel
        JPanel groupsPanel = new JPanel(new BorderLayout());
        groupsPanel.setBorder(BorderFactory.createTitledBorder("Groups"));

        groupList = new JList<>(groupListModel);
        groupList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Group g = (Group) value;
                return super.getListCellRendererComponent(list, g.name + " (" + g.members.size() + ")", index, isSelected, cellHasFocus);
            }
        });
        JScrollPane gsc = new JScrollPane(groupList);
        groupsPanel.add(gsc, BorderLayout.CENTER);

        // group controls
        JPanel gControls = new JPanel();
        gControls.setLayout(new BoxLayout(gControls, BoxLayout.Y_AXIS));
        JTextField newGroupField = new JTextField();
        JButton addGroupBtn = new JButton("Create Group");
        addGroupBtn.addActionListener(e -> {
            String name = newGroupField.getText().trim();
            if (name.isEmpty()) return;
            if (groups.containsKey(name)) {
                JOptionPane.showMessageDialog(this, "Group already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Group g = new Group(name);
            groups.put(name, g);
            groupListModel.addElement(g);
            newGroupField.setText("");
        });

        JButton deleteGroupBtn = new JButton("Delete Group");
        deleteGroupBtn.addActionListener(e -> {
            Group sel = groupList.getSelectedValue();
            if (sel == null) return;
            if (sel.name.equals("Admins")) {
                JOptionPane.showMessageDialog(this, "Cannot delete the default Admins group.", "Protected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            groups.remove(sel.name);
            groupListModel.removeElement(sel);
        });

        JPanel assignPanel = new JPanel(new GridLayout(1, 2, 4, 4));
        JButton addMemberBtn = new JButton("Add Selected Employee");
        addMemberBtn.addActionListener(e -> {
            Group sel = groupList.getSelectedValue();
            String emp = employeeList.getSelectedValue();
            if (sel == null || emp == null) {
                JOptionPane.showMessageDialog(this, "Select both a group and an employee.", "Selection required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!sel.members.contains(emp)) {
                sel.members.add(emp);
                groupList.repaint();
            }
        });
        JButton removeMemberBtn = new JButton("Remove Selected Employee");
        removeMemberBtn.addActionListener(e -> {
            Group sel = groupList.getSelectedValue();
            String emp = employeeList.getSelectedValue();
            if (sel == null || emp == null) {
                JOptionPane.showMessageDialog(this, "Select both a group and an employee.", "Selection required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            sel.members.remove(emp);
            groupList.repaint();
        });
        assignPanel.add(addMemberBtn);
        assignPanel.add(removeMemberBtn);

        gControls.add(new JLabel("New group name:"));
        gControls.add(newGroupField);
        gControls.add(addGroupBtn);
        gControls.add(deleteGroupBtn);
        gControls.add(Box.createVerticalStrut(6));
        gControls.add(assignPanel);

        groupsPanel.add(gControls, BorderLayout.SOUTH);

        split.setTopComponent(statusesPanel);
        split.setBottomComponent(groupsPanel);

        p.add(split, BorderLayout.CENTER);

        // Bottom quick-actions (example: assign selected employee to Admins)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton promoteToAdminBtn = new JButton("Add selected emp -> Admins");
        promoteToAdminBtn.addActionListener(e -> {
            String emp = employeeList.getSelectedValue();
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Select an employee first", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Group admins = groups.get("Admins");
            if (!admins.members.contains(emp)) {
                admins.members.add(emp);
                groupList.repaint();
            }
        });
        bottom.add(promoteToAdminBtn);

        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    // -----------------------
    // Reflection helpers to read EmployeeController employees map and Employee fields
    // -----------------------
    private void loadEmployeesFromController() {
        employeeListModel.clear();
        try {
            Object controller = managementFrame.getEmployeeController();
            if (controller == null) return;

            Field employeesField = controller.getClass().getDeclaredField("employees");
            employeesField.setAccessible(true);
            Object rawMap = employeesField.get(controller);

            if (rawMap instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) rawMap;
                // keys are usernames; we will display keys
                List<String> keys = new ArrayList<>();
                for (Object k : map.keySet()) {
                    keys.add(String.valueOf(k));
                }
                Collections.sort(keys);
                for (String k : keys) employeeListModel.addElement(k);
            } else {
                System.err.println("employees field not a Map instance.");
            }
        } catch (NoSuchFieldException nsfe) {
            System.err.println("No employees field found on EmployeeController: " + nsfe.getMessage());
        } catch (Exception ex) {
            System.err.println("Failed to load employees: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Find Employee object by username using reflection on controller.employees
    private Object findEmployeeObjectByUsername(String username) {
        try {
            Object controller = managementFrame.getEmployeeController();
            Field employeesField = controller.getClass().getDeclaredField("employees");
            employeesField.setAccessible(true);
            Object rawMap = employeesField.get(controller);
            if (rawMap instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) rawMap;
                return map.get(username);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private String getFieldString(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(obj);
            return val == null ? "" : String.valueOf(val);
        } catch (NoSuchFieldException nsfe) {
            // not all fields exist; return empty
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    // -----------------------
    // Inner model classes
    // -----------------------
    private static class Task {
        int id;
        String title;
        String description;
        String assignee; // username or "group:<groupname>"
        String status;

        Task(int id, String title, String description, String assignee, String status) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.assignee = assignee;
            this.status = status;
        }

        @Override
        public String toString() {
            return String.format("[%d] %s (%s) -> %s", id, title, status, (assignee == null || assignee.isEmpty()) ? "unassigned" : assignee);
        }
    }

    private static class Group {
        String name;
        Set<String> members = new TreeSet<>();

        Group(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
