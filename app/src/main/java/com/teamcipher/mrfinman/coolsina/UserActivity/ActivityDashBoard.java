package com.teamcipher.mrfinman.coolsina.UserActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcipher.mrfinman.coolsina.GenActivity.ActivityLogin;
import com.teamcipher.mrfinman.coolsina.Model.User;
import com.teamcipher.mrfinman.coolsina.R;
import com.teamcipher.mrfinman.coolsina.Services.logs;
import com.teamcipher.mrfinman.coolsina.Singleton.CurrentUser;
import com.teamcipher.mrfinman.coolsina.Singleton.UserList;
import com.teamcipher.mrfinman.coolsina.UserFragments.fragment_logs;
import com.teamcipher.mrfinman.coolsina.UserFragments.fragment_main;

import org.json.JSONArray;

public class ActivityDashBoard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Context ctx;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        initialization();
    }
    //Initialize all component
    private void initialization() {
        ctx = this;
        toolbar = findViewById(R.id.toolbar);
        navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawer = findViewById(R.id.drawer_layout);
        firebaseAuth = FirebaseAuth.getInstance();

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fragmentRedirection(new fragment_main());
        loadUser();
    }
    //Load user details set value to the navigation on the left corner the name and email of the current user
    private void loadUser() {
        View header = navigationView.getHeaderView(0);
        final TextView txtFullname =header.findViewById(R.id.user_fulname);
        final TextView txtEmail = header.findViewById(R.id.user_email);

        //Getting value from firebase
        DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference("Users");
        String userID = getPref("USER");
        DatabaseReference user = userDBRef.child(userID);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user_login = dataSnapshot.getValue(User.class);
                String fullname = user_login.getLname()+", "+user_login.getFname();
                String email = user_login.getEmail();
                savePreference("Device",user_login.getDeviceId());
                CurrentUser.getInstance().setCurrentUser(user_login);
                txtFullname.setText(fullname);
                txtEmail.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        txtFullname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ctx,ActivityProfile.class));
            }
        });
        txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ctx,ActivityProfile.class));
            }
        });

    }

    @Override
    public void onBackPressed() {
    }

    private String getPref(String key)
    {
        SharedPreferences prefs = getSharedPreferences("Credentials", MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    //Generated code for the navigation menu upon click and redirected to the specified page/UI
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {//nav_main
            fragmentRedirection(new fragment_main());
        }
        else if (id == R.id.nav_logs) {//nav_main
            fragmentRedirection(new fragment_logs());
        }
        else if (id == R.id.nav_logout) {//nav_main
            logout();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //Sabotable
    private void logout()
    {
        try
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmation");
            builder.setMessage("Are you sure you want to end session?.");
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            clearPref();
                            startActivity(new Intent(ctx, ActivityLogin.class));
                            //finish();

                        }
                    });

            builder.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder.create();
            alert11.show();

        }
        catch (Exception ex)
        {
            Toast.makeText(ctx, ""+ex, Toast.LENGTH_SHORT).show();
        }
    }
    //Method to pass fragments/semi page and performed change of the UI the pass fragment
    public void fragmentRedirection(Fragment ctx)
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, ctx)
                .addToBackStack(null)
                .commit();
    }

    private void clearPref()
    {
        SharedPreferences settings = ctx.getSharedPreferences("Credentials", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }

    private void savePreference(String key,String value)
    {
        SharedPreferences.Editor editor = getSharedPreferences("Credentials", MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }
}
