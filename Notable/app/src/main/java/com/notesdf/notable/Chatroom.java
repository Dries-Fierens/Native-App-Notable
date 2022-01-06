package com.notesdf.notable;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Chatroom implements Parcelable {
    private String adminId;
    private String groupName;
    private ArrayList<String> users;

    public Chatroom(){}

    public Chatroom(String adminId, String groupName, ArrayList<String> users) {
        this.adminId = adminId;
        this.groupName = groupName;
        this.users = users;
    }

    protected Chatroom(Parcel in) {
        adminId = in.readString();
        groupName = in.readString();
        users = in.createStringArrayList();
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

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

    @Override
    public String toString() {
        return "Chatroom{" +
                "adminId='" + adminId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", users=" + users +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(adminId);
        parcel.writeString(groupName);
        parcel.writeStringList(users);
    }
}
