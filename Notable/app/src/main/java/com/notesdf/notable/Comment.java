package com.notesdf.notable;

import android.net.Uri;

import java.util.Calendar;
import java.util.Date;

public class Comment {
    private String commentUser;
    private String commentText;
    private String commentUserId;
    private long commentTime;
    private float x;
    private float y;
    private String image;
    private String adminId;
    private String chatGroup;

    public Comment(){}

    public Comment(String commentUser, String commentText, String commentUserId, float x, float y, String image, String adminId, String chatGroup) {
        this.commentUser = commentUser;
        this.commentText = commentText;
        this.x = x;
        this.y = y;
        Calendar cal = Calendar.getInstance();
        //cal.setTimeZone(TimeZone.getTimeZone("GMT" + 1));
        Date plus1 = cal.getTime();
        this.commentTime = plus1.getTime();
        this.commentUserId = commentUserId;
        this.image = image;
        this.adminId = adminId;
        this.chatGroup = chatGroup;
    }

    public String getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(String commentUser) {
        this.commentUser = commentUser;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentUserId() {
        return commentUserId;
    }

    public void setCommentUserId(String commentUserId) {
        this.commentUserId = commentUserId;
    }

    public long getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(long commentTime) { this.commentTime = commentTime; }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getChatGroup() {
        return chatGroup;
    }

    public void setChatGroup(String chatGroup) {
        this.chatGroup = chatGroup;
    }
}
