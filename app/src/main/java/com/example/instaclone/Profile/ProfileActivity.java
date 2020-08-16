package com.example.instaclone.Profile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity/DEBUG";
    private static final int ACTIVITY_NUM = 4;
    private Context mContext = ProfileActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Log.d(TAG, "onCreate: started");

//        setupBottomNavigationView();
        setupToolbar();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    /**
     * Notes: BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);

        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        // Notes: Highlighting the correct Icon when navigating
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);


    }


    private void setupToolbar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

        // Notes: Set menu to Toolbar via code, NOT XML
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() 
        {
            @Override
            public boolean onMenuItemClick(MenuItem item) 
            {
                Log.d(TAG, "onMenuItemClick: clicked menu item: " + item);

                switch (item.getItemId())
                {
                    case R.id.profileMenu:
                        Log.d(TAG, "onMenuItemClick: navigating to Profile Preferences.");
                        break;
                }

                return false;
            }
        });
    }





}
