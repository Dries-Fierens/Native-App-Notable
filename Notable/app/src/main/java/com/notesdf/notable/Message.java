package com.notesdf.notable;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Message {
    private String messageUser;
    private String messageText;
    private String messageUserId;
    private long messageTime;
    private String chatGroup;

    public Message(){}

    public Message(String messageUser, String messageText, String messageUserId, String chatGroup) {
        this.messageUser = messageUser;
        this.messageText = messageText;
        Calendar cal = Calendar.getInstance();
        //cal.setTimeZone(TimeZone.getTimeZone("GMT" + 1));
        Date plus1 = cal.getTime();
        this.messageTime = plus1.getTime();
        this.messageUserId = messageUserId;
        this.chatGroup = chatGroup;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUserId() {
        return messageUserId;
    }

    public void setMessageUserId(String messageUserId) {
        this.messageUserId = messageUserId;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) { this.messageTime = messageTime; }

    public String getChatGroup() { return chatGroup; }

    public void setChatGroup(String chatGroup) { this.chatGroup = chatGroup; }

}
