package com.example.home.gpsdistrictwars;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    Button bRegister;
    TextView tvLogin;
    EditText etEmail, etPassword;
    FirebaseAuth mAuth;
    private static final String TAG = "!RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bRegister = (Button) findViewById(R.id.bRegister);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        mAuth = FirebaseAuth.getInstance();

        bRegister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

        etEmail.setText("Aaron_Verbeem@hotmail.com");
        etPassword.setText("123456");
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bRegister:
                createUser();
                break;
            case R.id.tvLogin:
                startActivity(new Intent(this, LoginActivity.class));
                this.finish();
                break;
        }
    }

    private void userLogin(){
        startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    private void sendEmailVerification(){
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null){
            mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "onComplete: email has been sent");
                        mAuth.signOut();
                        userLogin();
                    } else {
                        Log.d(TAG, "onComplete: failed to send email verification");
                    }
                }
            });
        }
    }

    private void createUser(){
        if (etPassword.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty()){
            Log.d(TAG, "createUser: Email or password is empty");
        } else if (etPassword.getText().toString().length() < 5){
            Log.d(TAG, "createUser: password is less than 5 characters");
        } else if (!isValidEmail(etEmail.getText().toString())){
            Log.d(TAG, "createUser: email is invalid");
        }
        else {
            mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "createUser: user has been created");
                        sendEmailVerification();
                    } else {
                        Log.d(TAG, "createUser: user was not created");

                    }
                }
            });
        }
    }
}
