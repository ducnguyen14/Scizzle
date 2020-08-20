package com.example.instaclone.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.instaclone.Login.LoginActivity;
import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.SectionsPagerAdapter;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;


public class HomeActivity extends AppCompatActivity {

    // Notes: Constants
    private static final String TAG = "HomeActivity/DEBUG";
    private static final int ACTIVITY_NUM = 0;
    private Context mContext = HomeActivity.this;


    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "onCreate: Starting");



        // Notes: Set ups
        setupFirebaseAuth();
        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();



        // Notes: TODO - Temporary Sign Out
//        mAuth.signOut();

    }


    /**
     * Notes: Initialized the ImageLoader with its configurations
     */
    private void initImageLoader()
    {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
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

    /**
        Notes: Setup for adding the 3 tabs: Camera, Home, Messages
     */
    private void setupViewPager()
    {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager(), 1);

        // Notes: Need to add fragments in this order
        adapter.addFragment(new CameraFragment()); // Notes: Index 0
        adapter.addFragment(new HomeFragment()); // Notes: Index 1
        adapter.addFragment(new MessagesFragment()); // Notes: Index 2

        // Notes: Setting ViewPager to Adapter
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        // Notes: Setting tabLayout to viewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Notes: Setting icons according to their tabs
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_tea_logo);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_messages);

    }




    /**
     * ***************************** Firebase *****************************
     */


    /**
     * Notes: Checks to see if the @param 'user' is logged in
     */
    private void checkCurrentUser(FirebaseUser user)
    {
        Log.d(TAG, "\tcheckCurrentUser: checking if user is logged in");

        if(user == null)
        {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
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

                // Notes: Check if the user is logged in
                checkCurrentUser(user);
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

        // Notes: For whatever reason we may start this activity, we always check the user
        checkCurrentUser(mAuth.getCurrentUser());

    }

    @Override
    public void onStop()
    {
        super.onStop();

        if(mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }







}