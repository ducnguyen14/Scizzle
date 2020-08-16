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
//        bottomNavigationViewEx.setTextVisibility(false);

        // Notes: Temporary solution to indicate which icon is the current on the bottom navigation bar
        bottomNavigationViewEx.setTextVisibility(true);

    }

}
