package com.example.instaclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.GridImageAdapter;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity/DEBUG";
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;




    private Context mContext = ProfileActivity.this;
    private ProgressBar mProgressBar;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Log.d(TAG, "onCreate: started");

        // Notes: Set ups
        setupBottomNavigationView();
        setupToolbar();
        setupActivityWidgets();
        setProfileImage();


        tempGridSetup();
    }

    private void tempGridSetup()
    {
        ArrayList<String> imgURLs = new ArrayList<>();
        imgURLs.add("https://wallpaperaccess.com/full/387093.png");
        imgURLs.add("https://i.redd.it/ol09140q55x31.jpg");
        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcT0K4Pl-nI2xpNMlIUz7uWVcCT3O6Yb9j2hTw&usqp=CAU");
        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcTy7QTi5x9Le0J8oa9TqlHng10akIjGwTZU6g&usqp=CAU");
        imgURLs.add("https://wallpaperaccess.com/full/9818.jpg");
        imgURLs.add("https://www.pcclean.io/wp-content/uploads/2019/04/680642.png");

        setupImageGrid(imgURLs);

    }

    private void setupImageGrid(ArrayList<String> imgURLs)
    {
        GridView gridView = (GridView) findViewById(R.id.gridView);


        // Notes: Fixing the scaling issue for when the images load
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        // Notes: Setting the default width for the grid columns
        gridView.setColumnWidth(imageWidth);


        // Notes: layout_grid_imageview is what gets recycled over and over
        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);

        gridView.setAdapter(adapter);


    }


    private void setProfileImage()
    {
        Log.d(TAG, "setProfileImage: setting profile photo");
        String imgURL = "i.pinimg.com/originals/19/58/7f/19587f4696f74eeea6f387816b9bff88.jpg";
        UniversalImageLoader.setimage(imgURL,profilePhoto, mProgressBar, "https://");
    }

    private void setupActivityWidgets()
    {
        // Notes: Hiding progressbar
        mProgressBar = (ProgressBar) findViewById(R.id.profileProgressbar);
        mProgressBar.setVisibility(View.GONE);

        profilePhoto = (ImageView) findViewById(R.id.profile_photo);
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

        ImageView profileMenu = (ImageView) findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: navigating to account settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);

            }
        });
    }





}
