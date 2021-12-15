package com.notesdf.notable;

public class Complaint {
    private String email;
    private String subject;
    private String description;
    private String userId;

    public Complaint(String email, String subject, String description, String userId) {
        this.email = email;
        this.subject = subject;
        this.description = description;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }
}
