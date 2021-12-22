package com.notesdf.notable;

import java.util.ArrayList;

public class Chatroom {
    private String adminId;
    private String groupName;
    private ArrayList<String> users;

    public Chatroom(){}

    public Chatroom(String adminId, String groupName, ArrayList<String> users) {
        this.adminId = adminId;
        this.groupName = groupName;
        this.users = users;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }
}
