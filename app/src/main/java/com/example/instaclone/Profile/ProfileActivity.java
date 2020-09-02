package com.example.instaclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.instaclone.R;
import com.example.instaclone.Utils.ViewCommentsFragment;
import com.example.instaclone.Utils.ViewPostFragment;
import com.example.instaclone.Utils.ViewProfileFragment;
import com.example.instaclone.models.Photo;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener ,
        ViewPostFragment.OnCommentThreadSelectedListener{

    private static final String TAG = "ProfileActivity/DEBUG";


    /**
     * Notes: This method navigates to the ViewCommentsFragment
     * @param photo
     */
    @Override
    public void onCommentThreadSelectedListener(Photo photo)
    {
        Log.d(TAG, "\tonCommentThreadSelectedListener:  selected a comment thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();

        // Notes: Passing Photo to fragment
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        /* Notes: Fragments have a different stack than activites and doesn't keep track
            of own stack. We need to keep track of it
         */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }


    @Override
    public void onGridImageSelected(Photo photo, int activityNumber)
    {
        Log.d(TAG, "\tonGridImageSelected: selected an image gridview: " + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();

        // Notes: Passing Photo to fragment
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);

        fragment.setArguments(args);

        /* Notes: Fragments have a different stack than activites and doesn't keep track
            of own stack. We need to keep track of it
         */
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
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
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

        /*
            Notes: Get incoming intent from SearchActivity.java and check for
                what extras is given to navigate to the
                according Fragment (ProfileFragment or ViewProfileFragment)
         */
        Intent intent = getIntent();

        // Notes: Navigate to ViewProfileFragment
        if(intent.hasExtra(getString(R.string.calling_activity)))
        {
            Log.d(TAG, "\tinit: searching for user object attached as intent extra");

            if(intent.hasExtra(getString(R.string.intent_user)))
            {
                Log.d(TAG, "\tinit: inflating ViewProfileFragment");

                ViewProfileFragment fragment = new ViewProfileFragment();

                // Notes: Passing User to fragment
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.intent_user), intent.getParcelableExtra(getString(R.string.intent_user)));
                fragment.setArguments(args);

                /* Notes: Fragments have a different stack than activites and doesn't keep track
                    of own stack. We need to keep track of it
                 */
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.view_profile_fragment));
                transaction.commit();
            }
            else
            {
                Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
        // Notes: Navigate to ProfileFragment
        else
        {
            Log.d(TAG, "\tinit: inflating ProfileFragment");

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

}
