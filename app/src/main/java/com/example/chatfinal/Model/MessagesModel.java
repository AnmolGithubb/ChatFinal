package com.example.chatfinal.Model;

public class MessagesModel {
    String uid, message, imageUrl;
    Long timestamp;
    private String type;

    public MessagesModel() {
    }

    public MessagesModel(String uid, String message) {
        this.uid = uid;
        this.message = message;
    }

    public MessagesModel(String uid, String message, Long timestamp) {
        this.uid = uid;
        this.message = message;
        this.timestamp = timestamp;
    }

    public MessagesModel(String uid, String message, String imageUrl) {
        this.uid = uid;
        this.message = message;
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
