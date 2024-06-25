package com.capstone.project.swipepaws.Matched;

import java.util.ArrayList;
import java.util.List;

public class Users {
    private String userId;
    private String name, profileImageUrl1, bio, city;
    private List<String> likedUserIds; // Add a List to store liked user IDs

    // Adjusted constructor to set the userId using the document ID
    public Users(String userId, String name, String profileImageUrl1, String bio, String city) {
        this.userId = userId;
        this.name = name;
        this.profileImageUrl1 = profileImageUrl1;
        this.bio = bio;
        this.city = city;
        this.likedUserIds = new ArrayList<>(); // Initialize the likedUserIds list
    }


    // Constructor for profile image URL
    public Users(String profileImageUrl) {
        this.profileImageUrl1 = profileImageUrl;
    }

    public String getCity() {
        return city;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl1;
    }

    public void setProfileImageUrl1(String profileImageUrl) {
        this.profileImageUrl1 = profileImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Add methods to handle likedUserIds
    public List<String> getLikedUserIds() {
        return likedUserIds;
    }

    public void addLikedUserId(String likedUserId) {
        likedUserIds.add(likedUserId);
    }

    public void removeLikedUserId(String likedUserId) {
        likedUserIds.remove(likedUserId);
    }
}
