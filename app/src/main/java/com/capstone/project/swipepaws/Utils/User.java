package com.capstone.project.swipepaws.Utils;

import java.io.Serializable;

public class User implements Serializable {
    private String phone_number;
    private String email;
    private String username;
    private String profileImageUrl1;
    private String profileImageUrl2;

    private String dogBio;
    private String profileImageUrl3;
    private String profileImageUrl4;
    private double latitude;
    private double longitude;
    private String city;
    private boolean firstLogin; // Added firstLogin attribute

    public User() {
        // Default constructor required for Firestore
    }

    public User(String phone_number, String email, String username, String profileImageUrl1, String profileImageUrl2, String profileImageUrl3 ,String profileImageUrl4 , double latitude,
                double longitude, String city, boolean firstLogin) {

        this.phone_number = phone_number;
        this.email = email;
        this.username = username;
        this.profileImageUrl1 = profileImageUrl1;
        this.profileImageUrl2 = profileImageUrl2;
        this.profileImageUrl3 = profileImageUrl3;
        this.profileImageUrl4 = profileImageUrl4;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.firstLogin = firstLogin;
    }

    public User(String username, String email, String mobileNumber) {
        this.username = username;
        this.email = email;
        this.phone_number = mobileNumber;
        this.profileImageUrl1 = "";
        this.profileImageUrl2 = "";
        this.profileImageUrl3 = "";
        this.profileImageUrl4 = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.city = "";
        this.firstLogin = true;
    }


    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDogBio() {
        return dogBio;
    }

    public void setDogBio(String dogBio) {
        this.dogBio = dogBio;
    }
    public String getProfileImageUrl1() {
        return profileImageUrl1;
    }

    public void setProfileImageUrl1(String profileImageUrl1) {
        this.profileImageUrl1 = profileImageUrl1;
    }
    public String getProfileImageUrl2() {
        return profileImageUrl2;
    }

    public void setProfileImageUrl2(String profileImageUrl2) {
        this.profileImageUrl2 = profileImageUrl2;
    }
    public String getProfileImageUrl3() {
        return profileImageUrl3;
    }

    public void setProfileImageUrl3(String profileImageUrl3) {
        this.profileImageUrl3 = profileImageUrl3;
    }
    public String getProfileImageUrl4() {
        return profileImageUrl4;
    }

    public void setProfileImageUrl4(String profileImageUrl4) {
        this.profileImageUrl4 = profileImageUrl4;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                ", phone_number='" + phone_number + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
