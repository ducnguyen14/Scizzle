package com.example.instaclone.Utils;

import android.util.Log;

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

        // Notes: Temporary solution to icons not showing up in the bottom navigation bar
        bottomNavigationViewEx.setTextVisibility(true);
        bottomNavigationViewEx.setIconVisibility(true);

    }

}
