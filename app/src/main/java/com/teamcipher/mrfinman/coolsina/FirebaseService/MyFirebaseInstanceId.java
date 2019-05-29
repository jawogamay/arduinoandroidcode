package com.teamcipher.mrfinman.coolsina.FirebaseService;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceId extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("TOKEN", "Refreshed token: " + token);

        savePreference("Token", token);

    }

    private String getPreference(String key)
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("TOKEN",0);
        return preferences.getString(key,null);
    }

    private void savePreference(String key,String value)
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("TOKEN",0);
        SharedPreferences.Editor  editor = preferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

}
