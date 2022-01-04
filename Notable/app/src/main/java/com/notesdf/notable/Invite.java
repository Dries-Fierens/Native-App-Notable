package com.notesdf.notable;

import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Invite {
    private Chatroom chatroom;
    private boolean accepted;

    public Invite(Chatroom chatroom) {
        this.chatroom = chatroom;
        this.accepted = false;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    public void setChatroom(Chatroom chatroom) {
        this.chatroom = chatroom;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
