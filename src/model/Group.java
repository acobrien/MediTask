package model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String name;
    private List<Employee> members = new ArrayList<>();

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {}

    public void addEmployee(Employee e) { members.add(e); }

    @Override
    public String toString() { return name; }

    public List<Employee> getMembers() { return members; }
}
