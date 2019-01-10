package com.example.home.gpsdistrictwars;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "!MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser == null){
            Log.d(TAG, "onCreate: No User is logged in");
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        }

        if (isServiceOK()){
            init();
        }





    }

    private void init(){
        Button bMap = (Button) findViewById(R.id.bMap);
        Button bLogout = (Button) findViewById(R.id.bLogout);
        bLogout.setOnClickListener(this);
        bMap.setOnClickListener(this);

    }

    public boolean isServiceOK(){
        Log.d(TAG, "isServiceOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS){
            Log.d(TAG, "isServiceOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "IsServiceOK: An error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bMap:
                startActivity(new Intent(this, MapsActivity.class));
                this.finish();
                break;
            case R.id.bLogout:
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                this.finish();
                Log.d(TAG, "onClick: user has been logged out");
                break;
        }
    }
}
