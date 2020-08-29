package com.example.instaclone.Utils;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaclone.R;
import com.example.instaclone.models.Photo;
import com.example.instaclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment/DEBUG";

    public ViewPostFragment()
    {
        super();

        /*
            Notes: An empty bundle can cause a NullPtrException. Need to always do setArguments to new
                bundle in the constructor when passing information through a bundle. (We passed arguments
                through bundle from OnGridImageSelectedListener interface)

         */
        setArguments(new Bundle());
    }

    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;



    // Notes: Widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage;



    // Notes: Variables
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private String photoUsername = "";
    private String profilePhotoUrl = "";
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted);
        mEllipses = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);


        // Notes: Hard code for now visibility, database will do it later
        mHeartRed.setVisibility(View.GONE);
        mHeartWhite.setVisibility(View.VISIBLE);


        mHeart = new Heart(mHeartWhite, mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());

        try
        {
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setimage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumFromBundle();

        }
        catch(NullPointerException e)
        {
            // Notes: Bundle could be null
            Log.e(TAG, "\tonCreateView: NullPointerException: " + e.getMessage());

        }

        setupFirebaseAuth();
        setupBottomNavigationView();
        getPhotoDetails();
//        setupWidgets();

        testToggle();
        return view;
    }



    private void testToggle()
    {
        mHeartRed.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "\tonTouch: red heart touch detected");
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mHeartWhite.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "\tonTouch: white heart touch detected");
                return mGestureDetector.onTouchEvent(event);
            }
        });


    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        // Notes: Database Queries and adding likes and removing likes will go here

        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {

            Log.d(TAG, "onDoubleTap: double tap detected");

            mHeart.toggleLike();

            return true;
        }
    }


    private void getPhotoDetails()
    {
        Log.d(TAG, "\tgetPhotoDetails: retrieving photo details");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        // Notes: TODO - Original Code - Doesn't work because on Firebase, user_account_settings does NOT have the attribute userID
//        Query query = reference
//                .child(getString(R.string.dbname_user_account_settings))
//                .orderByChild(getString(R.string.field_user_id)).equalTo(mPhoto.getUser_id());

        // Notes: TODO - Temporary substitute
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Notes: If a match is found
                // Notes: TODO - Original code doesn't work
//                for(DataSnapshot singleSnapshot: snapshot.getChildren())
//                {
//                    Log.d(TAG, "onDataChange: \n\n\tHERE");
//
//                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
//
//                }

                // Notes: TODO - Temporary substitute
                // Notes: if the DataSnapshot does not exists (No match found)
                if(snapshot.exists())
                {
                    mUserAccountSettings = snapshot.getValue(UserAccountSettings.class);
                }

                setupWidgets();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }


    private void setupWidgets()
    {
        String timestampDiff = getTimestampDifference();
        if(!timestampDiff.equals("0"))
        {
            // Notes: TODO _ add to string.xml
            mTimestamp.setText(timestampDiff + " days ago");
        }
        else
        {
            mTimestamp.setText("Today");
        }

        // Notes: Set Profile photo
        UniversalImageLoader.setimage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
        // Notes: Set username
        mUsername.setText(mUserAccountSettings.getUsername());

    }


    private String getTimestampDifference()
    {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;

        final String photoTimestamp = mPhoto.getDate_created();

        try
        {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }
        catch (ParseException e)
        {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }



    /**
     * Notes: Retrieve the activity number from the incoming bundle from ProfileActivity interface OnGridImageSelectedListener
     * @return
     */
    private int getActivityNumFromBundle()
    {
        Log.d(TAG, "\tgetPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            return bundle.getInt(getString(R.string.activity_number));
        }
        else
        {
            return 0;
        }
    }


    /**
     * Notes: Retrieve the photo from the incoming bundle from ProfileActivity interface OnGridImageSelectedListener
     * @return
     */
    private Photo getPhotoFromBundle()
    {
        Log.d(TAG, "\tgetPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            return bundle.getParcelable(getString(R.string.photo));
        }
        else
        {
            return null;
        }
    }


    /**
     * Notes: BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "\tsetupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);

        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity() ,bottomNavigationView);

        // Notes: Highlighting the correct Icon when navigating
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
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

