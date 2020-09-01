package com.example.instaclone.Utils;

import android.content.Context;
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
import com.example.instaclone.models.Comment;
import com.example.instaclone.models.Like;
import com.example.instaclone.models.Photo;
import com.example.instaclone.models.User;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment/DEBUG";

    // Notes: Implemented in ProfileActivity, ProfileActivity will navigate us to ViewCommentsFragment
    public interface OnCommentThreadSelectedListener{
        // Notes: Passing the current photo to the ViewCommentsFragment
        void onCommentThreadSelectedListener(Photo photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;




    public ViewPostFragment()
    {
        super();

        /*
            Notes: An empty bundle can cause a NullPtrException when receiving from an interface. Need to always do setArguments to new
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
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes, mComments;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage, mComment;



    // Notes: Variables
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private String photoUsername = "";
    private String profilePhotoUrl = "";
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";




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
        mLikes = (TextView) view.findViewById(R.id.image_likes);
        mComment = (ImageView) view.findViewById(R.id.speech_bubble);
        mComments = (TextView) view.findViewById(R.id.image_comments_link);


        mHeart = new Heart(mHeartWhite, mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());

        try
        {
//            mPhoto = getPhotoFromBundle();

            UniversalImageLoader.setimage(getPhotoFromBundle().getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumFromBundle();

            String photo_id = getPhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    // Notes: Looking for field that is inside the object
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren())
                    {
                        // Notes: Creating and initializing photo from Firebase
                        Photo newPhoto = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        
                        newPhoto.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        newPhoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        // Notes: Getting the comment list for the photo
                        List<Comment> commentsList = new ArrayList<Comment>();

                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren())
                        {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            commentsList.add(comment);
                        }
                        newPhoto.setComments(commentsList);

                        mPhoto = newPhoto;

                        getPhotoDetails();
                        getLikesString();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    Log.d(TAG, "onCancelled: query cancelled");
                }
            });
        }
        catch(NullPointerException e)
        {
            // Notes: Bundle could be null
            Log.e(TAG, "\tonCreateView: NullPointerException: " + e.getMessage());

        }

        setupFirebaseAuth();
        setupBottomNavigationView();

        return view;
    }


    /**
     * Notes: Always need this method when we use interfaces
     * @param context
     */
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        try
        {
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        }
        catch(ClassCastException e)
        {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
    }



    /**
     * Notes: This method gets the string that is displayed on the post that
     *      says Liked by x,y,z
     */
    private void getLikesString()
    {
        Log.d(TAG, "\tgetLikesString: getting likes string");
        // Notes: If the currentUser liked they're own post, then the heart is red and they also appear in the LikesString

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        // Notes: TODO - Prepare for a lot of debugging

        // Notes: This query is responsible for finding all the 'userID' that are attached to the likes of a photo
        final Query query = reference
                // Notes: Looking for the node that contains the object we're looking for
                .child(getString(R.string.dbname_photos))
                // Notes: Looking for the node that contains the object we're looking for
                .child(mPhoto.getPhoto_id())
                // Notes: Looking for the node that contains the object we're looking for
                .child(getString(R.string.field_likes));

        // Notes: Look for all the likes in the photo
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                mUsers = new StringBuilder();

                // Notes: If a match is found for the particular photo for if there are likes or not
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    // Notes: Second query to find the 'userID'
                    Query query2 = reference
                            // Notes: Looking for the node that contains the object we're looking for
                            .child(getString(R.string.dbname_users))
                            // Notes: Looking for field that is inside the object
                            .orderByChild(getString(R.string.field_user_id)).equalTo(singleSnapshot.getValue(Like.class).getUser_id());

                    // Notes: Look for all the likes in the photo
                    query2.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {

                            // Notes: If a match is found for the particular photo for if there are likes or not
                            for(DataSnapshot singleSnapshot: snapshot.getChildren())
                            {
                                Log.d(TAG, "onDataChange: founnd like: " + singleSnapshot.getValue(User.class).getUsername());

                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(", ");
                            }

                            if(snapshot.exists())
                            {
                            }

                            String[] splitUsers = mUsers.toString().split(", ");

                            // Notes: Check if the current user liked their own photo
                            if(mUsers.toString().contains(mUserAccountSettings.getUsername() + ","))
                            {
                                // Notes: Use mLikedByCurrentUser to toggle the heart icon
                                mLikedByCurrentUser = true;
                            }
                            else
                            {
                                mLikedByCurrentUser = false;
                            }

                            int length = splitUsers.length;

                            if(length == 1)
                            {
                                mLikesString = "Liked by " + splitUsers[0];
                            }
                            if(length == 2)
                            {
                                mLikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                            }
                            if(length == 3)
                            {
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2];
                            }
                            if(length == 4)
                            {
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + splitUsers[3];
                            }
                            if(length > 4)
                            {
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (splitUsers.length - 3) + " others";
                            }


                            setupWidgets();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error)
                        {

                        }
                    });


                    /*
                        Notes: If there are likes, iterate through the likes and gather up all the
                            usernames of the users who liked the photo
                     */

                }


                if(snapshot.exists())
                {
                }

                // Notes: No likes case
                if(!snapshot.exists())
                {
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

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

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            // Notes: TODO - Prepare for a lot of debugging
            Query query = reference
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(getString(R.string.dbname_photos))
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(mPhoto.getPhoto_id())
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(getString(R.string.field_likes));

            // Notes: Look for all the likes in the photo
            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {

                    // Notes: Only way to enter this loop if there has been any likes on the photo
                    // Notes: If a match is found for the particular photo for if there are likes or not
                    for(DataSnapshot singleSnapshot: snapshot.getChildren())
                    {
                        String keyID = singleSnapshot.getKey();

                        // Notes: Case 1 - Then user already liked the photo --> Remove their like
                        if(mLikedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                            .equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        {
                            // Notes: Removing the correct liker --> the current user
                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            // Notes: Removing the correct liker --> the current user
                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();


                            mHeart.toggleLike();
                            getLikesString();
                        }

                        // Notes: Case 2 - The current user has not liked the photo
                        if(!mLikedByCurrentUser)
                        {
                            // Notes: Add new like
                            addNewLike();
                            break;
                        }

                    }


                    if(snapshot.exists())
                    {
                    }

                    // Notes: No match found
                    if(!snapshot.exists())
                    {
                        // Notes: Add new like
                        addNewLike();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });

            return true;
        }
    }


    private void addNewLike()
    {
        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());


        // Notes: Adding a like
        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        // Notes: Adding a like
        myRef.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);


        mHeart.toggleLike();
        getLikesString();
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
                // Notes: Looking for the node that contains the object we're looking for
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

//                setupWidgets();

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
        // Notes: Set likers
        mLikes.setText(mLikesString);
        // Notes: Set caption
        mCaption.setText(mPhoto.getCaption());

        if(mPhoto.getComments().size() > 0)
        {
           mComments.setText("View all " + mPhoto.getComments().size() + " comments");
        }
        else
        {
            mComments.setText("");
        }

        // Notes: View more comments
        mComments.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: Navigating to comments thread");
                // Notes: Passing the current photo to the ViewCommentsFragment
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        // Notes: Back Arrow
        mBackArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        // Notes: Navigating to make a comment
        mComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");
                // Notes: Passing the current photo to the ViewCommentsFragment
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });



        // Notes: if current user liked their own photos
        if(mLikedByCurrentUser)
        {
            // Notes: TODO - Do we even need this?
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);

            mHeartRed.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "\tonTouch: red heart touch detected");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
        // Notes: User didn't like their own photo
        else
        {
            // Notes: TODO - Do we even need this?
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);

            mHeartWhite.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "\tonTouch: white heart touch detected");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }






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

