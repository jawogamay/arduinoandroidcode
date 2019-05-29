package com.teamcipher.mrfinman.coolsina.Model;

public class User {
    public String lname;
    public String fname;
    public String phonNumber;
    public String email;
    public String address;
    public String deviceId;


    public User()
    {

    }

    public User(String deviceId, String fname, String lname, String phonNumber, String email, String address) {
        this.deviceId = deviceId;
        this.fname = fname;
        this.lname = lname;
        this.phonNumber = phonNumber;
        this.email = email;
        this.address = address;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPhonNumber() {
        return phonNumber;
    }

    public void setPhonNumber(String phonNumber) {
        this.phonNumber = phonNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
