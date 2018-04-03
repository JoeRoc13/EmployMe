package com.jrocaberte.employme.Cards;

/**
 * Created by Joey on 2/18/18.
 */

public class Cards {
    private String userId;
    private String name;
    private String profileImageUrl;

    public Cards (String userId, String name, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) {  this.userId = userId; }

    public String getName() { return name;  }
    public void setName(String name) { this.name = name; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

}
