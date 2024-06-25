package com.capstone.project.swipepaws.Matched;

import com.google.firebase.Timestamp;

public class Comment {
    private String id; // The ID of the comment document in Firestore
    private String content; // The text content of the comment
    private String author; // The display name of the author
    private String userId; // The user ID of the author
    private Timestamp timestamp; // The timestamp when the comment was created

    // Default constructor required for Firestore data mapping
    public Comment() {}

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
