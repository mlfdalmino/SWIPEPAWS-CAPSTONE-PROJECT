package com.capstone.project.swipepaws.Matched;

import com.google.firebase.Timestamp;

public class Post {
    private String id; // Unique identifier for the post, corresponding to the Firestore document ID
    private String content; // The content of the post
    private String author; // Username of the author of the post
    private String userId; // UserID of the author
    private Timestamp timestamp; // Timestamp for when the post was created

    // No-argument constructor required for Firebase to deserialize documents to Post objects
    public Post() {
    }

    // Constructor that accepts content and author; sets timestamp to current time
    // This constructor can be used when creating a new post before saving to Firestore
    public Post(String content, String author) {
        this.content = content;
        this.author = author;
        // Initialize timestamp to current time when creating a post
        // Note: You might want to set the timestamp directly in Firestore instead to ensure consistency
        this.timestamp = new Timestamp(new java.util.Date());
    }

    // Getters and setters for each field

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id; // Set the Firestore document ID as the post's ID
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
