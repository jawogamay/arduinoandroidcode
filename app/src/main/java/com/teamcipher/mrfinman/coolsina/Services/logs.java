package com.teamcipher.mrfinman.coolsina.Services;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcipher.mrfinman.coolsina.Model.Logs;
import com.teamcipher.mrfinman.coolsina.Model.MessageResponse;
import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.R;
import com.teamcipher.mrfinman.coolsina.Singleton.CurrentUser;
import com.teamcipher.mrfinman.coolsina.Singleton.UserList;
import com.teamcipher.mrfinman.coolsina.Utils.ApiClient;
import com.teamcipher.mrfinman.coolsina.Utils.ApiInterface;
import com.teamcipher.mrfinman.coolsina.Utils.utils;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;

public class logs extends Service {
    private boolean isLeak,isFire,isSend, isShow;
    private int countFlame = 0,countLeak =0;
    private String userId;
    private ArrayList<User> allUsers;
    private User currentUser;
    private String PpmValue = "",device="",TAG ="NEXMO";
    private String[] deviceValue = {"",""};
    @Override
    public void onCreate() {
        super.onCreate();

        //while()

        currentUser = CurrentUser.getInstance().getCurrentUser();
        device = currentUser.getDeviceId();
        popUsers();
        final ScheduledExecutorService scheduleTaskExecutorCheck = Executors.newScheduledThreadPool(5);
        final ScheduledExecutorService scheduleTaskExecutorFlameLeak = Executors.newScheduledThreadPool(5);


        ScheduledExecutorService scheduleTaskExecutorFlameLeakSendSms = Executors.newScheduledThreadPool(5);

        // This schedule a runnable task every 1 minutes
        scheduleTaskExecutorCheck.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.e("DEVICE",CurrentUser.getInstance().getCurrentUser().getDeviceId()+" "+ CurrentUser.getInstance().getCurrentUser().getLname());
                currentUser = CurrentUser.getInstance().getCurrentUser();
                device = CurrentUser.getInstance().getCurrentUser().getDeviceId();
               monitorData();

            }
        }, 0, 1, TimeUnit.SECONDS);

        // This schedule a runnable task every 2 minutes
        scheduleTaskExecutorFlameLeak.scheduleAtFixedRate(new Runnable() {
            public void run() {
                final String msg = "From : Cool-Sina\n\nEmergency Alert! Gas leak has been detected! Device : "+device+".";
                String msgFlame = "From : Cool-Sina\n\nEmergency Alert! Fire has been detected! Device : "+device+".";
                if (isLeak)
                {

                    if (!isSend)
                    {
                        log();
                        isSend = true;

                        for(User u : UserList.getInstance().getUserlist())
                        {
                            sendSmS(u.getPhonNumber(),msg);
                        }

                    }
                    else
                    {
                        countLeak++;
                        if (countLeak >= 30)
                        {
                            log();
                            countLeak = 0;
                            for(User u : allUsers)
                            {
                                sendSmS(u.getPhonNumber(),msg);
                            }
                            return;
                        }
                    }
                }
                if (isFire)
                {

                    if (!isShow)
                    {
                        Log.d("TEST","1st");
                        logFlame();
                        for(User u : UserList.getInstance().getUserlist())
                        {
                            sendSmS(u.getPhonNumber(),msgFlame);
                        }
                        isShow = true;
                        return;
                    }
                    else
                    {
                        countFlame++;
                        if (countFlame >= 30)
                        {
                            logFlame();
                            countFlame = 0;
                            for(User u : UserList.getInstance().getUserlist())
                            {
                                sendSmS(u.getPhonNumber(),msgFlame);
                            }
                            return;
                        }

                    }
                }

            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    private void logFlame() {
        final DatabaseReference dbLogs = FirebaseDatabase.getInstance().getReference("Logs");
        String index = dbLogs.push().getKey();
        Calendar cal = GregorianCalendar.getInstance();
        String date = utils.dateComplete.format(cal.getTime())+" ,"+utils.time.format(cal.getTime());
        Logs log = new Logs(device,date,"Warning! Emergency Flame has been detected to the Device "+ device);
        dbLogs.child(index).setValue(log);
    }

    private void log() {

        final DatabaseReference dbLogs = FirebaseDatabase.getInstance().getReference("Logs");
        Calendar cal = GregorianCalendar.getInstance();
        String date = utils.dateComplete.format(cal.getTime())+" ,"+utils.time.format(cal.getTime());

        String index = dbLogs.push().getKey();
        Logs log = new Logs(device,date,"Warning! Emergency Gas leak has been detected to the Device "+device +" with the pressure "+PpmValue+"ppm.");
        dbLogs.child(index).setValue(log);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void monitorData()
    {
        final DatabaseReference databaseReferenceGasLeak = FirebaseDatabase.getInstance().getReference("gasleaklevel");
        databaseReferenceGasLeak.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long gaslevel = (long)dataSnapshot.getValue();

                int value = (int) gaslevel;
                if (value >= 400)
                {
                    isLeak = true;
                }
                else
                {
                    isLeak = false;
                }
                PpmValue = ""+value;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference databaseReferenceFlame = FirebaseDatabase.getInstance().getReference("flame");
        databaseReferenceFlame.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long isFlame = (long) dataSnapshot.getValue();
                if (isFlame == 1)
                {
                    isFire = false;
                }
                else
                {
                    isFire = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private String getPref(String key)
    {
        SharedPreferences prefs = getSharedPreferences("Credentials", MODE_PRIVATE);
        return prefs.getString(key, null);
    }
    private void sendSmS(String phone,String msg)
    {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<MessageResponse> call = apiInterface.getMessageResponse(utils.KEY,utils.SECRET,"Cool Sina",phone,msg);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, retrofit2.Response<MessageResponse> response) {
                String displayResult = "";
                try {
                    Log.d(TAG, String.valueOf(response.code()));
                    if (response.code() == 200) {
                        Log.d(TAG, response.body().toString());
                        Log.d(TAG, response.body().getMessages().toString());
                        Log.d(TAG, response.body().getMessageCount());
                        for (int i = 0; i < response.body().getMessageCount().length(); i++) {
                            Log.d(TAG, response.body().getMessages()[i].getTo());
                            Log.d(TAG, response.body().getMessages()[i].getMessageId());
                            Log.d(TAG, response.body().getMessages()[i].getStatus());
                            Log.d(TAG, response.body().getMessages()[i].getRemainingBalance());
                            Log.d(TAG, response.body().getMessages()[i].getMessagePrice());
                            Log.d(TAG, response.body().getMessages()[i].getNetwork());

                            displayResult = "TO: " + response.body().getMessages()[i].getTo() + "\n"
                                    + "Message-id: " + response.body().getMessages()[i].getMessageId() + "\n"
                                    + "Status: " + response.body().getMessages()[i].getStatus() + "\n"
                                    + "Remaining balance: " + response.body().getMessages()[i].getRemainingBalance() + "\n"
                                    + "Message price: " + response.body().getMessages()[i].getMessagePrice() + "\n"
                                    + "Network: " + response.body().getMessages()[i].getNetwork() + "\n\n";

                        }

                        //messageAreaTV.setText(displayResult);
                        Log.e(TAG, displayResult);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        });
        /*
        AndroidNetworking.get("http://192.168.8.101/CoolSina/send.php?pnumber="+phone+"&message="+msg+"")
                        .setTag("test")
                        .setPriority(Priority.LOW)
                        .build()
                        .getAsJSONArray(new JSONArrayRequestListener() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // do anything with response
                                Log.d("TEST",response.toString());
                            }
                            @Override
                            public void onError(ANError error) {
                                // handle error
                                Log.d("TEST","MY_ERROR "+error.toString());
                            }
                        });
        Log.d("TEST","METHOD SEND");*/
    }

    private void popUsers()
    {
        allUsers = new ArrayList<>();
        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("Users");
        dbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dtSnp : dataSnapshot.getChildren())
                {
                    User u = dtSnp.getValue(User.class);
                    allUsers.add(u);


                    Log.e("USERS",u.getLname()+" "+u.getPhonNumber());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public User getCurrentUser()
    {
      /*  for(User u: UserList.getInstance().getUserlist())
        {
            if(u.getEmail().equals(getPref("EMAIL")))
                return u;
        }*/
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(""+getPref("USER"));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return null;
    }
}
