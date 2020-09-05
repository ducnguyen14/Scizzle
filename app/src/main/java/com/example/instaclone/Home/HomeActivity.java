package com.example.instaclone.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.instaclone.Login.LoginActivity;
import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.MainfeedListAdapter;
import com.example.instaclone.Utils.SectionsPagerAdapter;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.example.instaclone.Utils.ViewCommentsFragment;
import com.example.instaclone.models.Photo;
import com.example.instaclone.models.UserAccountSettings;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;


public class HomeActivity extends AppCompatActivity implements
        MainfeedListAdapter.OnLoadMoreItemsListener {

    @Override
    public void onLoadMoreItems()
    {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");

        // Notes: Android has a default way of assigning tags
        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());

        if(fragment != null)
        {
            fragment.displayMorePhotos();
        }
    }

    // Notes: Constants
    private static final String TAG = "HomeActivity/DEBU";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;
    private Context mContext = HomeActivity.this;


    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    // Notes: Widgets
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "onCreate: Starting");


        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);



        // Notes: Set ups
        setupFirebaseAuth();
        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();



        // Notes: TODO - Temporary Sign Out
//        mAuth.signOut();

    }


    public void onCommentThreadSelected(Photo photo, String calling_activity)
    {
        Log.d(TAG, "\tonCommentThreadSelected: selected a comment thread");
        ViewCommentsFragment fragment = new ViewCommentsFragment();

        // Notes: Passing Photo to fragment
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity), calling_activity);
        fragment.setArguments(args);

        /* Notes: Fragments have a different stack than activites and doesn't keep track
            of own stack. We need to keep track of it
         */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }


    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);

        /*
            Notes: FrameLayout is what the CommentFragment will be inserted into.
                The error before was that there was no FrameLayout to inflate the Fragment,
                it was trying to inflate through a viewpager which does not work.
         */
        mFrameLayout.setVisibility(View.VISIBLE);
    }


    public void showLayout(){
        Log.d(TAG, "hideLayout: showing layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }


    /**
     * Notes: If the back button was pressed, and user navigated away from the CommentThread
     *      and the FrameLayout is Visibile, we want to hide the FrameLayout. We want to show the
     *      MainFeed RelativeLayout
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(mFrameLayout.getVisibility() == View.VISIBLE)
        {
            showLayout();
        }
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

        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);

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
        mViewPager.setAdapter(adapter);

        // Notes: Setting tabLayout to viewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

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

        mViewPager.setCurrentItem(HOME_FRAGMENT);

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