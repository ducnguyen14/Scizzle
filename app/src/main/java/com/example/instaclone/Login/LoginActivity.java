package com.example.instaclone.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instaclone.Home.HomeActivity;
import com.example.instaclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity/DEBUG";


    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mPleaseWait = (TextView) findViewById(R.id.pleaseWait);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mContext = LoginActivity.this;
        Log.d(TAG, "onCreate: started");


        mPleaseWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);


        setupFirebaseAuth();
        init();

    }



    private boolean isStringNull(String string)
    {
        Log.d(TAG, "\tisStringNull: checking string if null");

        if(string.equals(""))
        {
            return true;
        }
        else
        {
            return false;
        }

    }







    /**
     * ***************************** Firebase *****************************
     */


    /**
     * Notes: This method handles Login
     */
    private void init()
    {
        Log.d(TAG, "\tinit: started");

        // Notes: Initialize the button for logging in
        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: attempting to log in.");

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                // Notes: Checking if fields are null
                if(isStringNull(email) == true || isStringNull(password) == true)
                {
                    Toast.makeText(mContext, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mPleaseWait.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);


                    // Notes: Sign In
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    FirebaseUser user = mAuth.getCurrentUser();


                                    if (!task.isSuccessful())
                                    {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "\tsignInWithEmail:failure", task.getException());
                                        Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();

                                        mPleaseWait.setVisibility(View.GONE);
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        // Notes: Possible NullPointerException for email
                                        try
                                        {
                                            // Notes: Checking if email was verified
                                            if(user.isEmailVerified())
                                            {
                                                Log.d(TAG, "\tonComplete: success. Email is verified");
                                                Intent intent = new Intent(mContext, HomeActivity.class);
                                                startActivity(intent);
                                            }
                                            else
                                            {
                                                Toast.makeText(mContext, "Email is not verified\nCheck email inbox", Toast.LENGTH_SHORT).show();
                                                mPleaseWait.setVisibility(View.GONE);
                                                mProgressBar.setVisibility(View.GONE);

                                                // Notes: Signing out to make sure user needs to verify email first
                                                mAuth.signOut();
                                            }

                                        }
                                        catch (NullPointerException e)
                                        {
                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                        }

                                    }

                                    // ...
                                }
                            });



                }
            }
        });


        /*
            Notes: Register new user section. User will still need to come back to
                LoginActivity after creating an account, therefore do not use finish()
         */
        TextView linkSignUp = (TextView) findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "\tonClick: navigating to register screen");
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivity(intent);
            }
        });



        /*
            Notes: Navigating to HomeActivity if user is authenticated and logged in
         */
        if(mAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);
            finish();
        }

    }





    /**
     * Notes: Setup the firebase auth object
     */
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "\tsetupFirebaseAuth: setting up firebase auth");

        /*
            Notes: FirebaseAuth works on an Instance basis,the same FirebaseAuth
                object is usable app-wide
         */
        mAuth = FirebaseAuth.getInstance();

        // Notes: Checks if a user auth state has changed --> Signed in or signed out
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if(user != null)
                {
                    // Notes: User is signed in
                    Log.d(TAG, "\tonAuthStateChanged: signed in: " + user.getUid());
                }
                else
                {
                    // Notes: User is signed out
                    Log.d(TAG, "\tonAuthStateChanged: signed out");
                }
            }
        };
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }



}
