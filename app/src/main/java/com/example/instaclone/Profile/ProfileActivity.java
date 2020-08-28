package com.example.instaclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import androidx.fragment.app.FragmentTransaction;

import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.GridImageAdapter;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.example.instaclone.ViewPostFragment;
import com.example.instaclone.models.Photo;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements ProfileFragment.OnGridImageSelectedListener{
    private static final String TAG = "ProfileActivity/DEBUG";



    @Override
    public void onGridImageSelected(Photo photo, int activityNumber)
    {
        Log.d(TAG, "\tonGridImageSelected: selected an image gridview: " + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();

        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);

        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();

    }






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

        init();

    }

    /**
     * Notes: This method navigates to the profile_fragment
     */
    private void init()
    {
        Log.d(TAG, "\tinit: inflating " + getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();

        /* Notes: Fragments have a different stack than activites and doesn't keep track
            of own stack. We need to keep track of it
         */
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();


    }

}
