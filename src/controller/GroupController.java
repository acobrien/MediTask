package controller;

import model.*;
import view.ManagementFrame;

import java.util.ArrayList;
import java.util.List;

public class GroupController {

    private List<Group> groups = new ArrayList<>();

    public GroupController(ManagementFrame managementFrame) {
        Group admins = new Group("Admins");
        for (Employee e : managementFrame.getEmployeeController().getManagers().values()) {
            admins.addEmployee(e);
        }
        groups.add(admins);
    }

    public Group createGroup(String name) {
        Group g = new Group(name);
        groups.add(g);
        return g;
    }

    public List<Group> getGroups() {
        return groups;
    }
}
