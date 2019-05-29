package com.teamcipher.mrfinman.coolsina.UserActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.R;
import com.teamcipher.mrfinman.coolsina.Utils.message;
import com.teamcipher.mrfinman.coolsina.Utils.utils;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.teamcipher.mrfinman.coolsina.Utils.utils.disableTxt;
import static com.teamcipher.mrfinman.coolsina.Utils.utils.enableTxt;
import static com.teamcipher.mrfinman.coolsina.Utils.utils.getStringValue;

public class ActivityProfile extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText txtLname,txtFname,txtEmail,txtAddress,txtPhonN,txtDevice;
    private EditText[] Editexes;
    private Context ctx;
    private TextView lblfullname;
    private Button btnUpdate;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initialization();
    }
    //Initialize component
    private void initialization() {
        ctx = this;
        onheader();
        ButterKnife.bind(this);
        auth = FirebaseAuth.getInstance();
        txtLname = findViewById(R.id.txPLname);
        txtFname = findViewById(R.id.txPFname);
        txtEmail = findViewById(R.id.txPemail);
        txtAddress = findViewById(R.id.txPaddress);
        txtPhonN = findViewById(R.id.txPpnumber);
        txtDevice = findViewById(R.id.txPdeviceId);
        lblfullname = findViewById(R.id.lblFullname);
        btnUpdate = findViewById(R.id.btnUpdate);
        Editexes = new EditText[] {txtLname,txtFname,txtEmail,txtAddress,txtPhonN,txtDevice};
        userId = getPref("USER");
        onloadUser();

    }

    @OnClick(R.id.btnUpdate)
    public void onUpdate(View view)
    {
        if (validate())
        {
            User u = new User();
            u.setLname(getStringValue(txtLname));
            u.setFname(getStringValue(txtFname));
            u.setEmail(getStringValue(txtEmail));
            u.setPhonNumber(getStringValue(txtPhonN));
            u.setDeviceId(getStringValue(txtDevice));
            u.setAddress(getStringValue(txtAddress));

            DatabaseReference dbUserUpdate = FirebaseDatabase.getInstance().getReference("Users");
            DatabaseReference user = dbUserUpdate.child(userId);
            user.setValue(u);
            message.success("Successfuly Updated!",ctx);
            btnUpdate.setVisibility(View.GONE);
            disableTxt(Editexes);
        }
    }

    private boolean validate() {
        if (TextUtils.isEmpty(getStringValue(txtLname)))
        {
            message.error("Lastname must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(getStringValue(txtFname)))
        {
            message.error("Firstname must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(getStringValue(txtPhonN)))
        {
            message.error("Phone number must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(getStringValue(txtEmail)))
        {
            message.error("Email must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(getStringValue(txtAddress)))
        {
            message.error("Address must not be empty!",ctx);
            return false;
        }
        return true;
    }


    private String getPref(String key)
    {
        SharedPreferences prefs = getSharedPreferences("Credentials", MODE_PRIVATE);
        return prefs.getString(key, null);
    }
    private void onloadUser() {


        DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference user = userDBRef.child(userId);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user_login = dataSnapshot.getValue(User.class);
                String fullname = user_login.getLname()+", "+user_login.getFname();

               lblfullname.setText(fullname);
                txtLname.setText(""+user_login.getLname());
                txtFname.setText(""+user_login.getFname());
                txtAddress.setText(""+user_login.getAddress());
                txtEmail.setText(""+user_login.getEmail());
                txtPhonN.setText(""+user_login.getPhonNumber());
                txtDevice.setText(""+user_login.getDeviceId());
                disableTxt(Editexes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu_setting, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        else if (id == R.id.action_settings)
        {
            enableTxt(Editexes);
            btnUpdate.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void onheader() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        setTitle("Profile");
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,ActivityDashBoard.class));
        finish();
    }
}
