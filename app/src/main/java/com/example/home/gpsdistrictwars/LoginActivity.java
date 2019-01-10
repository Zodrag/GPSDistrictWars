package com.example.home.gpsdistrictwars;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.home.gpsdistrictwars.RegisterActivity.isValidEmail;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button bLogin;
    TextView tvSignUp, tvForgetPassword;
    EditText etEmail, etPassword;
    private static final String TAG = "!LoginActivity";
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bLogin = (Button) findViewById(R.id.bLogin);
        tvSignUp = (TextView) findViewById(R.id.tvSignUp);
        tvForgetPassword = (TextView) findViewById(R.id.tvForgetPassword);
        tvForgetPassword.setOnClickListener(this);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);

        etEmail.setText("Aaron_Verbeem@hotmail.com");
        etPassword.setText("123456");

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    private void logTheUserIn(){
        startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    private void loginUser(){
        if (etPassword.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty()){
            Log.d(TAG, "createUser: Email or password is empty");
        } else if (etPassword.getText().toString().length() < 5){
            Log.d(TAG, "createUser: password is less than 5 characters");
        } else if (!isValidEmail(etEmail.getText().toString())){
            Log.d(TAG, "createUser: email is invalid");
        } else {
            mAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "loginUser: user has been logged in");
                        if (isEmailVerified()){
                            logTheUserIn();
                        }
                    } else {
                        Log.d(TAG, "invalid email or password");
                    }
                }
            });
        }
    }

    private Boolean isEmailVerified(){
        mUser = mAuth.getCurrentUser();
        assert mUser != null;
        if (mUser.isEmailVerified()){
            Log.d(TAG, "isEmailVerified: email is verified");
            return true;
        } else {
            Log.d(TAG, "isEmailVerified: email has not been verified yet");
            Toast.makeText(this, "Please verify your email address", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            return false;
        }
    }

    private Boolean checkEmailAddress(){
        if (etEmail.getText().toString().isEmpty()){
            Log.d(TAG, "checkEmailAddress: Email or password is empty");
            return false;
        } else if (etPassword.getText().toString().length() < 5){
            Log.d(TAG, "checkEmailAddress: password is less than 5 characters");
            return false;
        } else if (!isValidEmail(etEmail.getText().toString())){
            Log.d(TAG, "checkEmailAddress: email is invalid");
            return false;
        }
        return true;
    }

    private void sendPasswordResetEmail(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (checkEmailAddress()) {
            auth.sendPasswordResetEmail(etEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: password reset email has been sent");
                                Toast.makeText(LoginActivity.this, "password reset email has been sent to your email", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "onComplete: password reset email could not be sent");
                            }
                        }
                    });
        }
        else {
            Toast.makeText(this, "Enter the email address you forget password to above", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bLogin:
                loginUser();
                break;
            case R.id.tvSignUp:
                startActivity(new Intent(this, RegisterActivity.class));
                this.finish();
                break;
            case R.id.tvForgetPassword:
                sendPasswordResetEmail();
                break;
        }
    }
}
