package com.example.instaclone.Login;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instaclone.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity/DEBUG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started");
    }
}
