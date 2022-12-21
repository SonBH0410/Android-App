package com.dtvt05.messenger.Model;

public class Users {
    private String DisplayName;
    private String Email;
    private String PhoneNumber;
    private String Avatar;
    private String Status;
    private String Password;

    public Users() {
    }

    public Users(String avatar, String displayName, String email, String phoneNumber, String status, String password) {
        DisplayName = displayName;
        Email = email;
        PhoneNumber = phoneNumber;
        Avatar = avatar;
        Status = status;
        Password = password;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password2) {
        Password = password2;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
