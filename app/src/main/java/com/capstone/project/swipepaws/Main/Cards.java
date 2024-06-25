package com.capstone.project.swipepaws.Main;


import java.util.List;

public class Cards {
    private String userId;
    private String dogName;
    private String breed;
    private String dogGender;
    private String profileImageUrl1;
    private String profileImageUrl2;
    private String profileImageUrl3;
    private String profileImageUrl4;
    private String profileImageUrl5;
    private String profileImageUrl6;
    private String city;
    private Integer distance;
    private String bio;
    private List<String> temperament; // Add this line




    public Cards(String userId, String dogName, String breed, String dogGender, String profileImageUrl1, String profileImageUrl2, String profileImageUrl3, String profileImageUrl4, String profileImageUrl5, String profileImageUrl6, String city, Integer distance, String bio, List<String> temperament) {
        this.userId = userId;
        this.dogName = dogName;
        this.breed = breed;
        this.dogGender = dogGender;
        this.profileImageUrl1 = profileImageUrl1;
        this.profileImageUrl2 = profileImageUrl2;
        this.profileImageUrl3 = profileImageUrl3;
        this.profileImageUrl4 = profileImageUrl4;
        this.profileImageUrl5 = profileImageUrl5;
        this.profileImageUrl6 = profileImageUrl6;
        this.city = city;
        this.distance = distance;
        this.bio = bio;
        this.temperament = temperament; // Assign it here
    }






    public String getDogName() {
        return dogName;
    }
    public void setDogName(String dogName) {
        this.dogName = dogName;
    }


    public List<String> getTemperament() {
        return temperament;
    }

    public String getBreed() {
        return breed;
    }
    public void setBreed(String breed) {
        this.breed = breed;
    }




    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }




    public String getDogGender() {
        return dogGender;
    }
    public void setDogGender(String dogGender) {
        this.dogGender = dogGender;
    }




    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setTemperament(List<String> temperament) {
        this.temperament = temperament;
    }


    public Integer getDistance() {
        return distance;
    }
    public void setDistance(Integer distance) {
        this.distance = distance;
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


    public String getProfileImageUrl5() {
        return profileImageUrl5;
    }
    public void setProfileImageUrl5(String profileImageUrl5) {
        this.profileImageUrl5 = profileImageUrl5;
    }


    public String getProfileImageUrl6() {
        return profileImageUrl6;
    }
    public void setProfileImageUrl6(String profileImageUrl6) {
        this.profileImageUrl6 = profileImageUrl6;
    }
}

