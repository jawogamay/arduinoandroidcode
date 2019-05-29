package com.teamcipher.mrfinman.coolsina.Model;

public class device {
    public String flame;
    public String gasleaklevel;
    public String message;

    public device(String flame, String gasleaklevel, String message) {
        this.flame = flame;
        this.gasleaklevel = gasleaklevel;
        this.message = message;
    }

    public device() {
    }

    public String getFlame() {
        return flame;
    }

    public void setFlame(String flame) {
        this.flame = flame;
    }

    public String getGasleaklevel() {
        return gasleaklevel;
    }

    public void setGasleaklevel(String gasleaklevel) {
        this.gasleaklevel = gasleaklevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
