package com.teamcipher.mrfinman.coolsina;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;/*
import com.nexmo.client.NexmoClient;
import com.nexmo.client.NexmoClientException;
import com.nexmo.client.sms.SmsSubmissionResponse;
import com.nexmo.client.sms.SmsSubmissionResponseMessage;
import com.nexmo.client.sms.messages.TextMessage;*/
import com.teamcipher.mrfinman.coolsina.GenActivity.ActivityLogin;
import com.teamcipher.mrfinman.coolsina.GenActivity.ActivityRegistration;
import com.teamcipher.mrfinman.coolsina.Model.Logs;
import com.teamcipher.mrfinman.coolsina.Model.MessageResponse;
import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.Services.logs;
import com.teamcipher.mrfinman.coolsina.Singleton.UserList;
import com.teamcipher.mrfinman.coolsina.UserActivity.ActivityProfile;
import com.teamcipher.mrfinman.coolsina.Utils.ApiClient;
import com.teamcipher.mrfinman.coolsina.Utils.ApiInterface;
import com.teamcipher.mrfinman.coolsina.Utils.utils;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

import static com.teamcipher.mrfinman.coolsina.Utils.utils.KEY;
import static com.teamcipher.mrfinman.coolsina.Utils.utils.SECRET;

public class MainActivity extends AppCompatActivity {
    private int counter = 0;
    public String TAG = "NEXMO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //sendSms();
        //Populate User login details
        populateUser();
        //Delay / Splash screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                finish();
            }
        }, 3000);
    }

    public void sendSms()
    {


    }

    //Preference -> store and Get value save on your phone
    private String getPreference(String key)
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("credentials",0);
        return preferences.getString(key,null);
    }
    private void savePreference(String key,String value)
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("credentials",0);
        SharedPreferences.Editor  editor = preferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    private void clearPreferences() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("credentials",0);
        SharedPreferences.Editor  editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    private void populateUser()
    {
        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("Users");
        dbUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dtsnp: dataSnapshot.getChildren())
                {
                    User u = dtsnp.getValue(User.class);
                    UserList.getInstance().getUserlist().add(u);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
