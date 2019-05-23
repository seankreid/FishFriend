package com.fishfriend.fishfriend_final;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    public String id;
    private EditText mEmail_editTxt;
    private EditText mPassword_editTxt;

    private Button mSignIn_btn;
    private Button mRegister_btn;
    private Button mBack_btn;

    private ProgressBar mProgress_bar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();

        mEmail_editTxt = (EditText) findViewById(R.id.email_editText);
        mPassword_editTxt = (EditText) findViewById(R.id.password_editText);

        mSignIn_btn = (Button) findViewById(R.id.signin_btn);
        mRegister_btn = (Button) findViewById(R.id.register_btn);
        mBack_btn = (Button) findViewById(R.id.login);
        Button button;
        mProgress_bar = (ProgressBar) findViewById(R.id.loading_progressBar);

        mSignIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEmpty()) return;
                inProgress(true);
                mAuth.signInWithEmailAndPassword(mEmail_editTxt.getText().toString(),
                        mPassword_editTxt.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(SignInActivity.this, "User signed in",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                intent.putExtra("ID",id);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                id = mAuth.getUid();
                                finish();
                                return;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        inProgress(false);
                        Toast.makeText(SignInActivity.this, "Sign in failed!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mRegister_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEmpty()) return;
                inProgress(true);
                mAuth.createUserWithEmailAndPassword(mEmail_editTxt.getText().toString(),
                        mPassword_editTxt.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(SignInActivity.this, "User registered successfully!",
                                        Toast.LENGTH_LONG).show();
                                inProgress(false);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        inProgress(false);
                        Toast.makeText(SignInActivity.this, "Registration failed."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        mBack_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); return;
            }
        });

    }

    private void inProgress(boolean x){
        if(x){
            mProgress_bar.setVisibility(View.VISIBLE);
            mBack_btn.setEnabled(false);
            mSignIn_btn.setEnabled(false);
            mRegister_btn.setEnabled(false);
        }else{
            mProgress_bar.setVisibility(View.GONE);
            mBack_btn.setEnabled(true);
            mSignIn_btn.setEnabled(true);
            mRegister_btn.setEnabled(true);
        }
    }

    private boolean isEmpty(){
        if(TextUtils.isEmpty(mEmail_editTxt.getText().toString())){
            mEmail_editTxt.setError("REQUIRED!");
            return true;
        }
        if(TextUtils.isEmpty(mPassword_editTxt.getText().toString())){
            mPassword_editTxt.setError("REQUIRED!");
            return true;
        }
        return false;
    }

}