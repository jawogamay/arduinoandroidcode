package com.teamcipher.mrfinman.coolsina.GenActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.R;
import com.teamcipher.mrfinman.coolsina.Singleton.CurrentUser;
import com.teamcipher.mrfinman.coolsina.Singleton.UserList;
import com.teamcipher.mrfinman.coolsina.UserActivity.ActivityDashBoard;
import com.teamcipher.mrfinman.coolsina.Utils.message;
import com.teamcipher.mrfinman.coolsina.Utils.utils;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.teamcipher.mrfinman.coolsina.Utils.utils.getStringValue;

public class ActivityLogin extends AppCompatActivity {
    private Context ctx;
    private EditText txtEmail,txtPassword;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //get data from phone/data save from phone w/ the key USER
        if (getPreference("USER") != null)
        {
            getUser();
            startActivity(new Intent(this, ActivityDashBoard.class));
            finish();
        }

        initialization();
        checkLogin();
    }
    //Execute if ever  during registration return back to login, automatic set the username to the email register
    private void checkLogin() {
        if (bundle != null)
            txtEmail.setText(""+bundle.getString("email"));
    }
    //Initialize Component
    private void initialization() {
        ctx = this;
        ButterKnife.bind(this);
        bundle = getIntent().getExtras();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(ctx);
        txtEmail  = findViewById(R.id.txtemail);
        txtPassword = findViewById(R.id.txtpassword);

    }

    //Method execute during Login button click
    @OnClick(R.id.btn_login)
    public void login(View view)
    {
        //Show dialogue
        progressDialog.setTitle("Authenticating");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        if (validate())
        {
            progressDialog.show();
            String email = txtEmail.getText().toString().trim();
            final String password = txtPassword.getText().toString().trim();
            //Firebase code to check if username and password exist in the User Authentication
            //Redirected to the specified Activity/ to the dashboard
            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful())
                            {
                                if (password.length() < 6) {
                                    message.error("Password must be minimum of 6 characters!",ctx);
                                    txtPassword.setError("");
                                    progressDialog.hide();
                                } else{
                                    message.error("Authentication failed!",ctx);
                                    progressDialog.hide();
                                }
                            }
                            else
                            {
                                progressDialog.hide();
                                message.success("Successfully Logged In!",ctx);
                                savePreference("USER",firebaseAuth.getUid());
                                savePreference("EMAIL",getStringValue(txtEmail));

                                startActivity(new Intent(ctx, ActivityDashBoard.class));
                                finish();


                            }
                        }
                    });
        }
    }
    //Just to redirect to Forgot password page/UI when Forgot password click
    @OnClick(R.id.lblforgotpassword)
    public void onclickForgetPass(View view)
    {
        startActivity(new Intent(getApplicationContext(),ActivityForgetPassword.class));
    }

    @Override
    public void onBackPressed() {

    }
    //Validate the text input if it is not null
    private boolean validate() {
        if (TextUtils.isEmpty(txtEmail.getText().toString()))
        {
            message.error("Email must not be empty!",ctx);
            return false;
        }else if (TextUtils.isEmpty(txtPassword.getText().toString()))
        {
            message.error("Password must not be empty!",ctx);
            return false;
        }
        return true;
    }
    //Method to redirect to Registration when Sign up click
    @OnClick(R.id.lblSignUp)
    public void signup(View view)
    {
        startActivity(new Intent(ctx,ActivityRegistration.class));
        finish();
    }
    //Get the specified data from the cache of the phone
    //Credentials there is the name of file store in phone
    private String getPreference(String key)
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Credentials",0);
        return preferences.getString(key,null);
    }
    //Save data to phone/cache
    private void savePreference(String key,String value)
    {
        SharedPreferences.Editor editor = getSharedPreferences("Credentials", MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }
    //Get the current user login
    //Firebase code find from the table users the current logon user
    public void getUser()
    {
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users").child(getPreference("USER"));
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                CurrentUser.getInstance().setCurrentUser(u);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
