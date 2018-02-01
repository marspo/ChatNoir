package com.example.martinjonovski.chatnoir;

/**
 * Created by Martin Jonovski on 10/25/2017.
 */

public class Messages {

    private String message, type;
    long time;
    boolean seen;
    private String from;

    public Messages() {
    }

    public Messages(String message, boolean seen, long time, String type, String form) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.from = form;

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isSeen() {
        return seen;
    }

    public String getFrom() {
        return from;
    }

    public void setForm(String from) {
        this.from = from;
    }
}
