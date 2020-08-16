package com.example.instaclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.instaclone.HomeActivity;
import com.example.instaclone.NotificationActivity;
import com.example.instaclone.ProfileActivity;
import com.example.instaclone.R;
import com.example.instaclone.SearchActivity;
import com.example.instaclone.ShareActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel/DEBUG";

    /**
     * Notes: BottomNavigationView setup
     */
    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx)
    {
//        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
//        bottomNavigationViewEx.setTextVisibility(false);

        // Notes: Temporary solution to indicate which icon is the current on the bottom navigation bar
        bottomNavigationViewEx.setTextVisibility(true);

    }



    public static void enableNavigation(final Context context, BottomNavigationViewEx view)
    {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    // Notes: ACTIVITY_NUM = 0
                    case R.id.ic_home:
                        Intent intent1 = new Intent(context, HomeActivity.class);
                        context.startActivity(intent1);
                        break;

                    // Notes: ACTIVITY_NUM = 1
                    case R.id.ic_search:
                        Intent intent2 = new Intent(context, SearchActivity.class);
                        context.startActivity(intent2);
                        break;

                    // Notes: ACTIVITY_NUM = 2
                    case R.id.ic_add:
                        Intent intent3 = new Intent(context, ShareActivity.class);
                        context.startActivity(intent3);
                        break;

                    // Notes: ACTIVITY_NUM = 3
                    case R.id.ic_notification:
                        Intent intent4 = new Intent(context, NotificationActivity.class);
                        context.startActivity(intent4);
                        break;

                    // Notes: ACTIVITY_NUM = 4
                    case R.id.ic_profile:
                        Intent intent5 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent5);
                        break;

                }


                return false;
            }
        });
    }



}
