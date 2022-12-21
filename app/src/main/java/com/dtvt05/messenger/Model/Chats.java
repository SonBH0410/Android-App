package com.dtvt05.messenger.Model;

public class Chats {
    private String Message;
    private String From;
    private String To;
    private long Time;
    private boolean Seen;

    public Chats() {
    }

    public Chats(String message, String to, String from, long time, boolean seen) {
        Message = message;
        From = from;
        Time = time;
        Seen = seen;
        To = to;
    }

    public String getTo() {
        return To;
    }

    public void setTo(String to) {
        To = to;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long time) {
        Time = time;
    }

    public boolean isSeen() {
        return Seen;
    }

    public void setSeen(boolean seen) {
        Seen = seen;
    }
}
