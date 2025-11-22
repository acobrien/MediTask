package controller;

import model.Employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.TreeMap;

public class EmployeeController {

    private TreeMap<String, Employee> employees = new TreeMap<>();
    private TreeMap<String, Employee> managers = new TreeMap<>(); // A subset of employees, distinct from laborers
    private TreeMap<String, Employee> laborers = new TreeMap<>(); // A subset of employees, distinct from managers

    public EmployeeController() {
        loadEmployees();
    }

    private void loadEmployees() {
        System.out.println("Loading employees from src/data/employees.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader("src/data/employees.csv"))) {
            String line = reader.readLine(); // Skip header
            if (line == null) return;

            while ((line = reader.readLine()) != null) {
                // Handle completely empty lines
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",", -1); // -1 keeps empty fields

                // Require username, password, role
                String username = safe(cols, 0);
                String password = safe(cols, 1);
                String firstName = safe(cols, 3);
                String lastName  = safe(cols, 4);
                String department = safe(cols, 12);
                String role      = safe(cols, 13);

                if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || department.isEmpty() || role.isEmpty()) {
                    System.out.println("Skipping invalid row: missing required fields");
                    continue;
                }

                // Optional fields with default fallback
                int id = parseIntSafe(safe(cols, 2));
                String street    = safe(cols, 5);
                String city      = safe(cols, 6);
                String state     = safe(cols, 7);
                String country   = safe(cols, 8);
                double salary = parseDoubleSafe(safe(cols, 9));
                String hireDate = safe(cols, 10);
                String birthDate = safe(cols, 11);

                Employee e = new Employee(username, password, id, firstName, lastName,
                        street, city, state, country, salary, hireDate, birthDate, department, role);

                // Add to master list
                employees.put(username, e);

                // Add to subsets
                if (role.equalsIgnoreCase("Manager")) {
                    managers.put(username, e);
                }
                else if (role.equalsIgnoreCase("Laborer")) {
                    laborers.put(username, e);
                }
            }

        }
        catch (Exception e) {
            System.out.println("Failed to load employees: " + e.getMessage());
        }
    }

    // Return -1 if invalid, 0 if a valid Manager, or 1 if a valid Laborer
    public int validateLogin(String username, String password) {
        // Look up the employee
        Employee e = employees.get(username);
        if (e == null) return -1;

        // Check password
        if (!e.getPassword().equals(password)) return -1;

        // Determine role
        String role = e.getRole();
        if (role.equalsIgnoreCase("Manager")) return 0;
        if (role.equalsIgnoreCase("Laborer")) return 1;

        // Should never happen, but safe fallback
        return -1;
    }

    // Helper Methods

    private String safe(String[] arr, int index) {
        return (index < arr.length) ? arr[index].trim() : "";
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }

    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0.0; }
    }

}
