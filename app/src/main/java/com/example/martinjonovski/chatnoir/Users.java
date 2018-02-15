package com.example.martinjonovski.chatnoir;

/**
 * Created by Martin Jonovski on 10/24/2017.
 */

public class Users {

    public String image;
    public String image_thumb;
    public String name;
    public String status;
    private String uid;
    private String online;

    private boolean myMessage = false;

    public Users() {
    }

    public Users(String image, String image_thumb, String name, String status) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.image_thumb = image_thumb;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    @Override
    public int hashCode() {
        return image.hashCode() + image_thumb.hashCode() + name.hashCode() + status.hashCode();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object obj) {
        Users newU = (Users) obj;
        if (newU != null) {
            if (this.getName().equals(newU.getName())) {
                if (this.getStatus().equals(newU.getStatus()))
                    return true;
            }
        }
        return false;
    }

    public boolean isMyMessage() {
        return myMessage;
    }

    public void setMyMessage(boolean myMessage) {
        this.myMessage = myMessage;
    }
}
