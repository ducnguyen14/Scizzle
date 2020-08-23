package com.example.instaclone.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.Permissions;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {

    // Notes: Constants
    private static final String TAG = "ShareActivity/DEBUG";
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private Context mContext = ShareActivity.this;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "onCreate: started");

        // Notes: Check to see if permissions are granted
        if(checkPermissionsArray(Permissions.PERMISSIONS))
        {

        }
        else
        {
            // Notes: Need to verify permissions
            verifyPermissions(Permissions.PERMISSIONS);
        }


        // Notes: Set ups
//        setupBottomNavigationView();
    }


    public void verifyPermissions(String[] permissions)
    {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        // Notes:
        ActivityCompat.requestPermissions(ShareActivity.this, permissions, VERIFY_PERMISSIONS_REQUEST);
    }



    /**
     * Notes: Checks an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions)
    {
        Log.d(TAG, "\tcheckPermissionsArray: checking permissions array");

        for(int i = 0; i < permissions.length; i++)
        {
            String check = permissions[i];

            // Notes: Checks a single permission within the array of permissions
            if(!checkPermissions(check))
            {
                return false;
            }

        }


        return true;
    }


    /**
     * Notes: Checks a single permission if it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission)
    {
        Log.d(TAG, "\tcheckPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        // Notes: Permission is not granted
        if(permissionRequest != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "\tcheckPermissions: Permission was not granted for: " + permission);
            return false;
        }
        else
        {
            Log.d(TAG, "\tcheckPermissions: Permission was granted for: " + permission);
            return true;
        }



    }




    /**
     * Notes: BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "\tsetupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);

        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        // Notes: Highlighting the correct Icon when navigating
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);


    }

}
