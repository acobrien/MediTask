package model;

public class Employee implements Comparable<Employee> {
    private String username;
    private String password;

    private int id;
    private String firstName;
    private String lastName;
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private double salary;

    private String hireDate;
    private String birthDate;
    private String department;
    private String role;

    @Override
    public int compareTo(Employee employee) {
        return this.username.compareTo(employee.username);
    }

    public Employee(String username, String password, int id, String firstName, String lastName, String streetAddress, String city, String state, String country, double salary, String hireDate, String birthDate, String department, String role) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.country = country;
        this.salary = salary;
        this.hireDate = hireDate;
        this.birthDate = birthDate;
        this.department = department;
        this.role = role;
    }

    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getUsername() { return username; }
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getStreetAddress() { return streetAddress; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public double getSalary() { return salary; }
    public String getHireDate() { return hireDate; }
    public String getBirthDate() { return birthDate; }
    public String getDepartment() { return department; }


    @Override
    public String toString() {
        return firstName + " " + lastName + " - " + department;
    }

}
