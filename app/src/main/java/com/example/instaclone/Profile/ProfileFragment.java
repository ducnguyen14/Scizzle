package com.example.instaclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.instaclone.Login.LoginActivity;
import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.FirebaseMethods;
import com.example.instaclone.Utils.GridImageAdapter;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.example.instaclone.models.Photo;
import com.example.instaclone.models.User;
import com.example.instaclone.models.UserAccountSettings;
import com.example.instaclone.models.UserSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.lang.reflect.Array;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment
{
    private static final String TAG = "ProfileFragment/DEBUG";
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;



    // Notes: Widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;

    private Context mContext;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mPosts = (TextView) view.findViewById(R.id.tvPosts);
        mFollowers = (TextView) view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView) view.findViewById(R.id.tvFollowing);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressbar);
        gridView = (GridView) view.findViewById(R.id.gridView);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(mContext);
        TextView editProfile = (TextView) view.findViewById(R.id.textEditProfile);
        Log.d(TAG, "onCreateView: stared.");



        // Notes: Setups
        setupBottomNavigationView();
        setupToolbar();

        setupFirebaseAuth();

        setupGridView();


        editProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: navigating to " + mContext.getString(R.string.edit_profile_fragment));

                // Notes: Need to navigate to AccountSettingsActivity, then to EditProfileFragment
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);

                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                // Notes: Don't call finish() because we want to be able to navigate back to this activity
            }
        });



        return view;
    }


    public void setupGridView()
    {
        Log.d(TAG, "setupGridView: Setting up image grid");

        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    photos.add(singleSnapshot.getValue(Photo.class));
                }

                // Notes: Set up image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMNS;

                gridView.setColumnWidth(imageWidth);


                // Notes: Getting the imageURLs pathways
                ArrayList<String> imgUrls = new ArrayList<String>();
                for(int i = 0; i < photos.size(); i++)
                {
                    imgUrls.add(photos.get(i).getImage_path());
                }

                // Notes: Setting images to the grid
                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, "", imgUrls);
                gridView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

    }



    private void setProfileWidgets(UserSettings userSettings)
    {
        Log.d(TAG, "\tsetProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());

//        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        // Notes: If the image is null, UniversalImageLoader will set default image
        UniversalImageLoader.setimage(settings.getProfile_photo(), mProfilePhoto, null, "");



        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mProgressBar.setVisibility(View.GONE);

    }


    private void setupToolbar()
    {

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: navigating to account settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);


            }
        });
    }


    /**
     * Notes: BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "\tsetupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);

        BottomNavigationViewHelper.enableNavigation(mContext, getActivity() ,bottomNavigationView);

        // Notes: Highlighting the correct Icon when navigating
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);


    }


/**
 * ***************************** Firebase *****************************
 */


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
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();



        // Notes: Checks if a user auth state has changed --> Signed in or signed out
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();

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


        // Notes: Read or write to the database
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Notes: Retrieve user's info from database
                // Notes: TODO - Rewrite this line for easier read
                setProfileWidgets(mFirebaseMethods.getUserSettings(snapshot));

                // Notes: Retrieve user's images from database

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });



    }

    @Override
    public void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);


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
