package model;

public class TaskStatus {
    private String name;

    public TaskStatus(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}
