package model;

public class Task {
    private String title;
    private String description;
    private Employee assignee;   // optional
    private Group group;         // optional
    private TaskStatus status;

    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public void assignEmployee(Employee e) { this.assignee = e; }
    public void assignGroup(Group g) { this.group = g; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return title + " (" + status.getName() + ")";
    }

    public Employee getAssignee() { return assignee; }
    public Group getGroup() { return group; }
    public TaskStatus getStatus() { return status; }
}
