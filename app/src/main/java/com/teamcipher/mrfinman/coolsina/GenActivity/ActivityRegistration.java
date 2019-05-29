package com.teamcipher.mrfinman.coolsina.GenActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.teamcipher.mrfinman.coolsina.Adaptors.DeviceAdaptor;
import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.R;
import com.teamcipher.mrfinman.coolsina.Utils.message;
import com.teamcipher.mrfinman.coolsina.Utils.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.teamcipher.mrfinman.coolsina.Utils.utils.getStringValue;

public class ActivityRegistration extends AppCompatActivity {
    private Context ctx;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String devId = "";
    private EditText txtDevId, txtfname,txtlname,txtphoneNo,txtaddress, txtEmail,txtPassword,txtRePassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initialize();
        initializeFirebase();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    //Initialization of the component
    private void initialize() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        onheader("Registration");
        ctx = this;
        ButterKnife.bind(this);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPword);
        txtaddress = findViewById(R.id.txtAddress);
        txtfname = findViewById(R.id.txtfname);
        txtlname = findViewById(R.id.txtlname);
        txtaddress = findViewById(R.id.txtAddress);
        txtphoneNo = findViewById(R.id.txtphoneNum);
        txtRePassword = findViewById(R.id.txtRe_p_word);
        txtDevId = findViewById(R.id.txtDevId);

        loadUserId();
        loadToken();
    }
    //Generate Unique ID of the phone during on the 1st installation of the application
    private void loadToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                         return;
                        }

                        String token = task.getResult().getToken();
                        devId = token;

                    }
                });

    }
    //Method generated to execute backpress redirect back to login
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ctx,ActivityLogin.class);
        startActivity(intent);
        finish();
    }
    //get token/unique ID from cache
    private void loadUserId() {
        devId = getPreference("TOKEN");
        txtDevId.setEnabled(false);
    }


    //Method to set title header text
    private void onheader(String registration) {
        setTitle(registration);
    }
    //Method execute when Sign in click redirect back to login
    @OnClick(R.id.lblSignIn)
    public void onclickSignIn(View view)
    {
        startActivity(new Intent(this,ActivityLogin.class));
        finish();
    }
    //Method to execute when choose device
    //And set the choosen device to the textbox
    @OnClick(R.id.btnpick)
    public void  clickPick(View view)
    {
        final Dialog dialog = new Dialog(ctx);
        dialog.setContentView(R.layout.device_choose);
        ListView listView = dialog.findViewById(R.id.listViewDevices);
        final ArrayList<String> deviceList = new ArrayList<>();
        deviceList.add("CoolSina-Dev1");

        DeviceAdaptor adaptor = new DeviceAdaptor(ctx,deviceList);
        listView.setAdapter(adaptor);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                txtDevId.setText(""+deviceList.get(i));
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    //Method execute when register button click
    @OnClick(R.id.btnRegister)
    public void onclickRegister(View view)
    {
        //Validate Inputs
        if (validate())
        {
            //Firebase code check email and password if already exist
            firebaseAuth.createUserWithEmailAndPassword(txtEmail.getText().toString().trim(),txtPassword.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful())
                            {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    message.error("User with this email already exist!",ctx);
                                }
                                else
                                {
                                    message.error(""+task.getException(),ctx);
                                }
                            }
                            else
                            {
                                //Execute add child/ new user to the user table
                                String uId = firebaseAuth.getUid().toString();
                                User u = new User(getStringValue(txtDevId), getStringValue( txtfname), getStringValue( txtlname),"63"+ getStringValue( txtphoneNo), getStringValue(txtEmail), getStringValue(txtaddress));
                                databaseReference.child(uId).setValue(u);
                                message.success("Successfully Registered!",ctx);
                                savePreference("device",getStringValue(txtDevId));
                                Intent intent = new Intent(ctx,ActivityLogin.class);
                                intent.putExtra("email",txtEmail.getText().toString());
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
        }
    }

    //Method to validate input
    private boolean validate() {
        if (TextUtils.isEmpty(txtlname.getText().toString()))
        {
            message.error("Lastname must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(txtfname.getText().toString()))
        {
            message.error("Firstname must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(txtphoneNo.getText().toString()))
        {
            message.error("Phone number must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(txtEmail.getText().toString()))
        {
            message.error("Email must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(txtaddress.getText().toString()))
        {
            message.error("Address must not be empty!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(txtPassword.getText().toString()))
        {
            message.error("Please enter password!",ctx);
            return false;
        }
        if (TextUtils.isEmpty(txtRePassword.getText().toString()))
        {
            message.error("Please re-enter password!",ctx);
            return false;
        }
        if (!(txtRePassword.getText().toString().equals(txtPassword.getText().toString())))
        {
            message.warning("Password not match!",ctx);
            return false;
        }
        return true;
    }
    //Same explaination
    private String getPreference(String key)
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("TOKEN",0);
        return preferences.getString(key,null);
    }

    private void savePreference(String key,String value)
    {
        SharedPreferences.Editor editor = getSharedPreferences("Credentials", MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }


}
