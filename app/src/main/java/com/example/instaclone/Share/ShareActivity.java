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
import androidx.viewpager.widget.ViewPager;

import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.Permissions;
import com.example.instaclone.Utils.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {

    // Notes: Constants
    private static final String TAG = "ShareActivity/DEBUG";
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;

    private Context mContext = ShareActivity.this;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Log.d(TAG, "onCreate: started");

        // Notes: Check to see if permissions are granted
        if(checkPermissionsArray(Permissions.PERMISSIONS))
        {
            setupViewPager();
        }
        else
        {
            // Notes: Need to verify permissions
            verifyPermissions(Permissions.PERMISSIONS);
        }


        // Notes: Set ups
//        setupBottomNavigationView();
    }


    /**
     * Notes: Return the current tab number
     *      0 = GalleryFragment
     *      1 = PhotoFragment
     * @return
     */
    public int getCurrentTabNumber()
    {
        return mViewPager.getCurrentItem();
    }


    /**
     * Notes: Setup viewpager to manage the tabs
     */
    private void setupViewPager()
    {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager(), 1);
        adapter.addFragment(new GalleryFragment());
        // Notes: TODO - Perhaps rename this to camerafragment
        adapter.addFragment(new PhotoFragment());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        // Notes: TODO - Perhaps change to CAMERA
        tabLayout.getTabAt(1).setText(getString(R.string.photo));




    }

    /**
     * Notes: This method retrieves incoming task from EditProfileFragment/Change Profile photo
     * @return
     */
    public int getTask()
    {
        // Notes: getFlags() should return 268435456, 0, or null
        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());

        return getIntent().getFlags();
    }


    public void verifyPermissions(String[] permissions)
    {
        Log.d(TAG, "\tverifyPermissions: verifying permissions.");

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
