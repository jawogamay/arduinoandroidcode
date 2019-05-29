package com.teamcipher.mrfinman.coolsina.UserFragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcipher.mrfinman.coolsina.Model.Logs;
import com.teamcipher.mrfinman.coolsina.Model.MessageResponse;
import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.Model.device;
import com.teamcipher.mrfinman.coolsina.R;
import com.teamcipher.mrfinman.coolsina.Singleton.CurrentUser;
import com.teamcipher.mrfinman.coolsina.Singleton.UserList;
import com.teamcipher.mrfinman.coolsina.Utils.ApiClient;
import com.teamcipher.mrfinman.coolsina.Utils.ApiInterface;
import com.teamcipher.mrfinman.coolsina.Utils.message;
import com.teamcipher.mrfinman.coolsina.Utils.utils;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.nitri.gauge.Gauge;
import retrofit2.Call;
import retrofit2.Callback;

import static android.content.Context.MODE_PRIVATE;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class fragment_main extends Fragment {

    private ImageView imgStatus,imgFireStatus;
    private TextView lblValue,lbldevice;
    private Gauge leakGauge;
    private Context ctx;
    private  String deviceName;
    private  CFAlertDialog.Builder builder;

    private boolean isLeak,isFire,isSend, isShow,isLogFire,isLogLeak;
    private int countFlame = 0,countLeak =0;
    private String userId;
    private ArrayList<User> allUsers;
    private User currentUser;
    private String PpmValue = "",device="",TAG="NEXMO";

    public fragment_main() {
        // Required empty public constructor
    }
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fragment_main, container, false);
        initialization(view);
        onlogs();
        closeKeyboard();
        return view;
    }

    private void onlogs() {
        currentUser = CurrentUser.getInstance().getCurrentUser();
        device = getPref("Device");
        popUsers();
        final ScheduledExecutorService scheduleTaskExecutorCheck = Executors.newScheduledThreadPool(5);
        final ScheduledExecutorService scheduleTaskExecutorFlameLeak = Executors.newScheduledThreadPool(5);

        // This schedule a runnable task every 1 minutes
        scheduleTaskExecutorCheck.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.e("DEVICE",getPref("Device")+" "+ CurrentUser.getInstance().getCurrentUser().getLname());
                currentUser = CurrentUser.getInstance().getCurrentUser();
                device = getPref("Device");
                monitorData();

            }
        }, 0, 1, TimeUnit.SECONDS);

        final String msg = "From : Cool-Sina\n\nEmergency Alert! Gas leak has been detected! Device : "+device+".";
        final String msgFlame = "From : Cool-Sina\n\nEmergency Alert! Fire has been detected! Device : "+device+".";
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (isLeak)
                {
                    if (!isLogLeak)
                    {
                        log();
                        isLogLeak = true;
                        Log.e("LOG",msg);
                        for(User u : UserList.getInstance().getUserlist())
                        {
                            ///Log.e("MESSAGE",msg);
                            sendSmS(u.getPhonNumber(),msg);
                        }
                    }
                    else
                    {
                        countLeak++;
                        if (countLeak == 30)
                        {
                            log();
                            countLeak = 0;
                            for(User u : allUsers)
                            {
                                //Log.e("MESSAGE",msg);
                                sendSmS(u.getPhonNumber(),msg);
                            }
                        }
                    }
                }
                if (isFire)
                {

                    if (!isLogFire)
                    {
                        Log.e("ISFIRE","TRUE "+UserList.getInstance().getUserlist().size());
                        logFlame();
                        for(User u : UserList.getInstance().getUserlist())
                        {
                            sendSmS(u.getPhonNumber(),msgFlame);
                        }
                        isLogFire = true;
                    }
                    else
                    {
                        Log.e("ISFIRE","FALSE");
                        countFlame++;
                        if (countFlame == 30)
                        {
                            logFlame();
                            countFlame = 0;
                            for(User u : UserList.getInstance().getUserlist())
                            {
                                sendSmS(u.getPhonNumber(),msgFlame);
                            }
                        }
                        Log.e("ISFIRE","FALSE "+countFlame);
                    }
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);

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
        SharedPreferences prefs = ctx.getSharedPreferences("Credentials", MODE_PRIVATE);
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
        //Log.e("MESSAGE",msg);
        //http://192.168.43.175:81
        //http://192.168.8.101
        /*AndroidNetworking.get("http://192.168.8.101/CoolSina/send.php?pnumber="+phone+"&message="+msg+"")
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
                });*/
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

    private void initialization(View view) {
        ctx = getContext();
        getActivity().setTitle("Gas Leak Monitor");
        leakGauge = view.findViewById(R.id.leak_gauge);
        imgStatus = view.findViewById(R.id.status_icon);
        lblValue = view.findViewById(R.id.lblValue);
        lbldevice = view.findViewById(R.id.lbldevice);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        loadGauge();
    }

    private void closeKeyboard() {
        View view = getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadGauge() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkValue();
            }
        }, 0, 1000);
    }

    private void checkValue() {
        DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference("Users");
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference user = userDBRef.child(userID);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user_login = dataSnapshot.getValue(User.class);
                lbldevice.setText(""+user_login.getDeviceId());
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

                if (isFlame == 0)
                {
                    playNotification();
                    utils.vibrate(ctx);
                    if (!isShow)
                    {
                        showDialogue();
                        isShow = true;
                    }
                    else
                    {
                        ScheduledExecutorService scheduleTaskExecutorCheckSendSms = Executors.newScheduledThreadPool(5);
                        scheduleTaskExecutorCheckSendSms.scheduleAtFixedRate(new Runnable() {
                            @Override
                            public void run() {
                                showDialogue();
                            }
                        }, 0, 30, TimeUnit.SECONDS);
                    }
                }
                else
                {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference databaseReferenceGasLeak = FirebaseDatabase.getInstance().getReference("gasleaklevel");
        databaseReferenceGasLeak.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long gaslevel = (long)dataSnapshot.getValue();

                int value = (int) gaslevel;
                if (value >= 400)
                {
                    lblValue.setTextColor(getResources().getColor(R.color.Red));
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_red));
                    utils.vibrate(ctx);
                    playNotification();
                    isLeak= true;
                    //log();



                }
                else if (value <= (400 / 2))
                {
                    lblValue.setTextColor(getResources().getColor(R.color.LawnGreen));
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_gray));
                    stopNotification();
                    isLeak = false;
                }
                else
                {
                    lblValue.setTextColor(getResources().getColor(R.color.LawnGreen));
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_green));
                    stopNotification();
                    isLeak = false;

                }

                leakGauge.moveToValue(value);
                lblValue.setText(value+" ppm");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void playNotification()
    {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(ctx, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void stopNotification()
    {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(ctx, notification);
            r.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialogue()
    {
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(getContext());
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
        builder.setTitle("WARNING!");
        builder.setHeaderView(R.layout.alert_message_leak);
        builder.addButton("OK", -1, -1, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }
}
