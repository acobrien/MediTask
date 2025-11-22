package controller;

import model.*;
import java.util.ArrayList;
import java.util.List;

public class TaskController {

    private List<Task> tasks = new ArrayList<>();
    private List<TaskStatus> statuses = new ArrayList<>();

    public TaskController() {
        statuses.add(new TaskStatus("Open"));
        statuses.add(new TaskStatus("In-Progress"));
        statuses.add(new TaskStatus("Complete"));
    }

    public Task createTask(String title, String desc, TaskStatus status) {
        Task t = new Task(title, desc, status);
        tasks.add(t);
        return t;
    }

    public List<Task> getTasks() { return tasks; }
    public List<TaskStatus> getStatuses() { return statuses; }

    public TaskStatus addStatus(String name) {
        TaskStatus s = new TaskStatus(name);
        statuses.add(s);
        return s;
    }
}
