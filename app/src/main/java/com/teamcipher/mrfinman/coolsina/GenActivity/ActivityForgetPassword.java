package com.teamcipher.mrfinman.coolsina.GenActivity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.teamcipher.mrfinman.coolsina.R;
import com.teamcipher.mrfinman.coolsina.UserActivity.ActivityDashBoard;
import com.teamcipher.mrfinman.coolsina.Utils.message;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.teamcipher.mrfinman.coolsina.Utils.message.success;
import static com.teamcipher.mrfinman.coolsina.Utils.utils.enableTxt;
import static com.teamcipher.mrfinman.coolsina.Utils.utils.getStringValue;

public class ActivityForgetPassword extends AppCompatActivity {
    private EditText txtEmail;
    private Context ctx;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        ButterKnife.bind(this);
        initialization();
    }

    private void initialization() {
        onheader();
        txtEmail = findViewById(R.id.txtResetEmail);
        ctx = this;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    //Menu on upper header <- button executes backpress
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    //Change the header title, icon to <-
    private void onheader() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        setTitle("Forget Password");
    }
    //Execute onclick of button send reset password
    @OnClick(R.id.btnSendPassword)
    public void onclickSend(View view)
    {
        if (getStringValue(txtEmail) != "")
        {
            firebaseAuth.sendPasswordResetEmail(getStringValue(txtEmail)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        message.success("Successfuly sent password to your email",ctx);
                        message.success("Please check your email",ctx);
                        onBackPressed();
                    }
                    else
                    {
                        message.error(""+task.getException().toString(),ctx);
                    }
                }
            });
        }
        else
        {
            success("Empty field!",ctx);
        }
    }
}
