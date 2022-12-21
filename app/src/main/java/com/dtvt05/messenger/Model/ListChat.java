package com.dtvt05.messenger.Model;


public class ListChat {
    private String Avatar;
    private String DisplayName;
    private String LastMessage;
    private long Time;

    public ListChat() {
    }

    public ListChat(String avatar, String displayName, String lastMessage, long time) {
        this.Avatar = avatar;
        this.DisplayName = displayName;
        this.LastMessage = lastMessage;
        this.Time = time;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long time) {
        Time = time;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getLastMessage() {
        return LastMessage;
    }

    public void setLastMessage(String lastMessage) {
        LastMessage = lastMessage;
    }
}
