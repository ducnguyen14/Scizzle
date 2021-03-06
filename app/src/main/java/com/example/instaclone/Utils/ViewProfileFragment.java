package com.example.instaclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.instaclone.Profile.AccountSettingsActivity;
import com.example.instaclone.Profile.ProfileActivity;
import com.example.instaclone.R;
import com.example.instaclone.models.Comment;
import com.example.instaclone.models.Like;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Notes: This fragment is for viewing another profile that is NOT
 *      the current user logged in. Current user's profile is ProfileFragment.java
 */

public class ViewProfileFragment extends Fragment
{
    private static final String TAG = "ViewProfileFrag/DEBUG";

    // Notes: Implemented in ProfileActivity, ProfileActivity will navigate us to ViewPostFragment
    public interface OnGridImageSelectedListener
    {
        /*
            Notes: Using an activityNumber because ViewPostFragment is accessible from many different
                places (Profile, Homefeed, clicking on a picture, searchfeed, etc). activityNumber
                allows us to mark the bottom navigation view bar accordingly to keep track of which activity
                we are at.
         */
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;






    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;




    // Notes: Widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription, mFollow, mUnfollow;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private ImageView mBackArrow;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView editProfile;

    // Notes: Variables
    private User mUser;
    private Context mContext;
    private int mFollowersCount = 0;
    private int mFollowingCount = 0;
    private int mPostsCount = 0;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);

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
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mFollow = (TextView) view.findViewById(R.id.follow);
        mUnfollow = (TextView) view.findViewById(R.id.unFollow);
        editProfile = (TextView) view.findViewById(R.id.textEditProfile);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mContext = getActivity();
        Log.d(TAG, "onCreateView: stared.");


        try
        {
            mUser = getUserFromBundle();
            init();
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, "onCreateView: NullPtrException: " + e.getMessage());
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();

            // Notes: Navigate back to whatever we were doing previously
            getActivity().getSupportFragmentManager().popBackStack();
        }

        // Notes: Setups
        setupBottomNavigationView();

        setupFirebaseAuth();

        isFollowing();
        getFollowingCount();
        getFollowersCount();
        getPostsCount();

        // Notes: TODO - redo the follow, following, unfollow methods, poor designing
        mFollow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: now following: " + mUser.getUsername());

                /*
                    Notes: TODO - Use this:
                        private FirebaseDatabase mFirebaseDatabase;
                        private DatabaseReference myRef;
                        mFirebaseDatabase = FirebaseDatabase.getInstance();
                        myRef = mFirebaseDatabase.getReference();
                 */

                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());


                setFollowing();

            }
        });

        mUnfollow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: now unfollowing: " + mUser.getUsername());

                                /*
                    Notes: TODO - Use this:
                        private FirebaseDatabase mFirebaseDatabase;
                        private DatabaseReference myRef;
                        mFirebaseDatabase = FirebaseDatabase.getInstance();
                        myRef = mFirebaseDatabase.getReference();
                 */

                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();


                setUnfollowing();
            }
        });




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


    private void init()
    {
        // Notes: Set the profile widgets
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_username))
                .equalTo(mUser.getUsername());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Notes: A match is found
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings.class).toString());

                    UserSettings settings = new UserSettings();
                    settings.setUser(mUser);
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));

                    setProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        // Notes: Get the users profile photos
        Query query2 = reference
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());

        query2.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ArrayList<Photo> photos = new ArrayList<>();
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    // Notes: Error -   com.google.firebase.database.DatabaseException: Expected a List while deserializing, but got a class java.util.HashMap
//                    photos.add(singleSnapshot.getValue(Photo.class));

                    // Notes: (Solution) Type cast the snapshot to a hashmap and then add the fields manually to the photo

                    // Notes: Step 1) Type cast snapshot into hashmap
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    // Notes: Step 2) Add fields manually to photo object
                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> comments = new ArrayList<Comment>();

                    for(DataSnapshot datasnapshot: singleSnapshot
                            .child(getString(R.string.field_comments))
                            .getChildren())
                    {
                        // Notes: Getting individual comment
                        Comment comment = new Comment();
                        comment.setUser_id(datasnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(datasnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(datasnapshot.getValue(Comment.class).getDate_created());

                        // Notes: Adding to a list of comments
                        comments.add(comment);
                    }

                    // Notes: Adding comments to the Photo
                    photo.setComments(comments);


                    List<Like> likesList = new ArrayList<Like>();
                    for(DataSnapshot datasnapshot: singleSnapshot
                            .child(getString(R.string.field_likes))
                            .getChildren())
                    {
                        // Notes: Getting individual likes
                        Like like = new Like();
                        like.setUser_id(datasnapshot.getValue(Like.class).getUser_id());

                        // Notes: Adding to a list of likes
                        likesList.add(like);
                    }

                    photo.setLikes(likesList);

                    // Notes: Step 3) Add photo object to list of photos to be displayed on gridview
                    photos.add(photo);

                }

                setupImageGrid(photos);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

    }

    private void isFollowing()
    {
        Log.d(TAG, "\tisFollowing: Checking if following this users.");
        setUnfollowing();


        // Notes: Set follow status
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Notes: A match is found
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue().toString());
                    setFollowing();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });


    }


    private void getFollowersCount()
    {
        mFollowersCount = 0;

        // Notes: Set follow status
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Notes: A match is found
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found follower: " + singleSnapshot.getValue().toString());
                    mFollowersCount++;
                }

                // Notes: Update the followers number on Profile
                mFollowers.setText(String.valueOf(mFollowersCount));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }


    private void getFollowingCount()
    {
        mFollowingCount = 0;

        // Notes: Set follow status
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Notes: A match is found
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found following user: " + singleSnapshot.getValue().toString());
                    mFollowingCount++;
                }

                // Notes: Update the following number on Profile
                mFollowing.setText(String.valueOf(mFollowingCount));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }


    private void getPostsCount()
    {
        mPostsCount = 0;

        // Notes: Set follow status
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Notes: A match is found
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: post #: " + singleSnapshot.getValue().toString());
                    mPostsCount++;
                }

                // Notes: Update the posts number on Profile
                mPosts.setText(String.valueOf(mPostsCount));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }




    private void setFollowing()
    {
        Log.d(TAG, "\tsetFollowing: updating UI for following this user");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);
    }

    private void setUnfollowing()
    {
        Log.d(TAG, "\tsetFollowing: updating UI for unfollowing this user");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.GONE);
    }

    private void setCurrentUsersProfile()
    {
        Log.d(TAG, "\tsetFollowing: updating UI for showing this user their own profile");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);
    }


    private void setupImageGrid(final ArrayList<Photo> photos)
    {
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


        // Notes: Setting onClickListener onto the gridview items
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Notes: Use the interface OnGridImageSelectedListener to navigate to the ViewPostFragment
                mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
            }
        });
    }




    private User getUserFromBundle()
    {
        Log.d(TAG, "\tgetUserFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            return bundle.getParcelable(getString(R.string.intent_user));
        }
        else
        {
            return null;
        }

    }



    /**
     * Notes: Always need this method when we use interfaces
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context)
    {
        try
        {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }
        catch (ClassCastException e)
        {
            Log.e(TAG, "\tonAttach: ClassCastException: " + e.getMessage());
        }


        super.onAttach(context);

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


        mBackArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
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
