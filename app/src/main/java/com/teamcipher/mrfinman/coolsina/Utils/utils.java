package com.teamcipher.mrfinman.coolsina.Utils;

import android.content.Context;
import android.os.Vibrator;
import android.widget.EditText;

import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.Singleton.UserList;

import java.text.SimpleDateFormat;

public class utils {
    public static SimpleDateFormat time = new SimpleDateFormat("h:mm a");
    public static SimpleDateFormat dateComplete = new SimpleDateFormat("dd MMMM, yyyy");
    public static String KEY = "9c9350e6",SECRET = "8Eo5CgibNgBHEs7K";

    public static void vibrate(Context ctx)
    {

        Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator.hasVibrator()) {

            vibrator.vibrate(500); // for 500 ms

        }
    }
    public static String getStringValue(EditText txt)
    {
        return txt.getText().toString().trim();
    }

    public static void disableTxt(EditText[] txts)
    {
        for(EditText txt: txts)
            txt.setEnabled(false);
    }

    public static void enableTxt(EditText[] txts)
    {
        for(EditText txt: txts)
            txt.setEnabled(true);
    }



}
