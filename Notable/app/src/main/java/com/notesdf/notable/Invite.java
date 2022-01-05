package com.notesdf.notable;

import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Invite {
    private Chatroom chatroom;
    private String documentId;
    private String receiver;
    private boolean accepted;

    public Invite(){}

    public Invite(Chatroom chatroom, String documentId, String receiver) {
        this.chatroom = chatroom;
        this.documentId = documentId;
        this.receiver = receiver;
        this.accepted = false;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    public void setChatroom(Chatroom chatroom) {
        this.chatroom = chatroom;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    @Override
    public String toString() {
        return "Invite{" +
                "chatroom=" + chatroom +
                ", documentId='" + documentId + '\'' +
                ", receiver='" + receiver + '\'' +
                ", accepted=" + accepted +
                '}';
    }
}
