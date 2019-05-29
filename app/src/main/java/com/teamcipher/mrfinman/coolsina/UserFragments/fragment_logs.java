package com.teamcipher.mrfinman.coolsina.UserFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcipher.mrfinman.coolsina.Adaptors.LogsAdaptor;
import com.teamcipher.mrfinman.coolsina.Model.Logs;
import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.R;
import com.teamcipher.mrfinman.coolsina.Singleton.CurrentUser;
import com.teamcipher.mrfinman.coolsina.Singleton.UserList;
import com.teamcipher.mrfinman.coolsina.Utils.utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class fragment_logs extends Fragment {

    private RecyclerView recyclerView;
    private Context ctx;
    private EditText txtSearch;
    private ArrayList<Logs> logsList = new ArrayList<>();
    private View view;
    private LogsAdaptor adaptor;

    private boolean isLeak,isFire,isSend, isShow;
    private int countFlame = 0,countLeak =0;
    private String userId;
    private ArrayList<User> allUsers;
    private User currentUser;
    private String PpmValue = "",device="";

    public fragment_logs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fragment_logs, container, false);
        initialization();
        //onlogs();
        return view;
    }

    private void onlogs() {
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
                final String msg = "Emergency Alert! Gas leak has been detected! Device : "+device+".";
                String msgFlame = "Emergency Alert! Fire has been detected! Device : "+device+".";
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
        Log.d("TEST","METHOD SEND");
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
    private void initialization() {
        getActivity().setTitle("Logs");
        ctx = getContext();
        recyclerView = view.findViewById(R.id.recyclerView);
        txtSearch = view.findViewById(R.id.txtlogSearch);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        populate();
        onsearch();
    }

    private void filter(String str)
    {
        ArrayList<Logs> filterLogs = new ArrayList<>();
        for(Logs log : logsList)
        {
            if (log.getDeviceId() != null)
            {
                if (log.getDeviceId().toLowerCase().contains(str.toLowerCase()) || log.getMessage().toLowerCase().contains(str.toLowerCase()) || log.getDateTime().toLowerCase().contains(str.toLowerCase()) )
                    filterLogs.add(log);

            }

            /*if (log.getDeviceId().toLowerCase().contains(str.toLowerCase()) || log.getMessage().toLowerCase().contains(str.toLowerCase()) || log.getDateTime().toLowerCase().contains(str.toLowerCase()))
                filterLogs.add(log);*/
        }
        adaptor.filter(filterLogs);
    }

    private void onsearch() {
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
    }

    private void populate()
    {
        try {
            logsList.clear();
            final DatabaseReference dbLogsItems = FirebaseDatabase.getInstance().getReference("Logs");
            final DatabaseReference dbLogs = FirebaseDatabase.getInstance().getReference("Logs");
            dbLogs.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dtLogs : dataSnapshot.getChildren())
                    {
                        Logs log = dtLogs.getValue(Logs.class);
                        logsList.add(log);
                    }
                    adaptor = new LogsAdaptor(logsList,ctx);
                    Collections.reverse(logsList);
                    recyclerView.setAdapter(adaptor);
                    adaptor.notifyDataSetChanged();


                    adaptor.setClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int pos = recyclerView.indexOfChild(view);
                            Logs log = logsList.get(pos);
                            showDetails(log);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch (Exception ex)
        {
            //Toast.makeText(ctx, ""+ex, Toast.LENGTH_SHORT).show();
        }

    }

    private void showDetails(Logs log) {
        Dialog dialog = new Dialog(ctx);
        dialog.setContentView(R.layout.log_details);
        TextView lblDevice,lblDesc,lblDate;
        lblDate = dialog.findViewById(R.id.lblDateTime);
        lblDesc = dialog.findViewById(R.id.lblDesc);
        lblDevice = dialog.findViewById(R.id.lblDevice);

        lblDate.setText(""+log.getDateTime());
        lblDesc.setText(""+log.getMessage());
        lblDevice.setText(""+log.getDeviceId());
        dialog.show();
    }


}
