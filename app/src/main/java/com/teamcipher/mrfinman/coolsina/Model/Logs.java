package com.teamcipher.mrfinman.coolsina.Model;

import java.util.Date;

public class Logs {
    private String deviceId;
    private String dateTime;
    private String message;

    public Logs() {
    }

    public Logs(String deviceId, String dateTime, String message) {
        this.deviceId = deviceId;
        this.dateTime = dateTime;
        this.message = message;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
