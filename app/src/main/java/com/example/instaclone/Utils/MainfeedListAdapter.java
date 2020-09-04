package com.example.instaclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.instaclone.Home.HomeActivity;
import com.example.instaclone.Profile.ProfileActivity;
import com.example.instaclone.R;
import com.example.instaclone.models.Comment;
import com.example.instaclone.models.Like;
import com.example.instaclone.models.Photo;
import com.example.instaclone.models.User;
import com.example.instaclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "MainfedListAdapt/DEBUG";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";


    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects)
    {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Notes: Using the ViewHolder build pattern to view the images.
     *      The ViewHolder doesn’t load the all the widgets at once. Widgets are loaded into memory.
     *      It only loads a few of them to make the app faster (similar to RecyclerView)
     */
    private static class ViewHolder{
        CircleImageView mprofileImage;
        String likesString;
        TextView username, timeDetla, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user  = new User();
        StringBuilder users;
        String mLikesString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Notes: ViewHolder build pattern (Similar to RecyclerView)
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timeDetla = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            /*
                Notes: The reason why in the constructor we passed objects to the super so that
                    we can access it via getItem(int) or else getItem(int) will return null
            */
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            /*
                Notes: Tag is a way you can store widgets in memory.
                    ViewHolder creates all the widgets, and tag will hold entire ViewHolder.
                    We store the view in memory and not putting it on the page so it doesn't cause the app
                    to slow down.
             */
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Notes: Get the current users username (Need for checking likes strings)
        getCurrentUsername();

        getLikesString(holder);

        /*
            Notes: The reason why in the constructor we passed objects to the super so that
                we can access it via getItem(int) or else getItem(int) will return null
         */

        // Notes: Set the comments
        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText("View all " + comments.size() + " comments");
        holder.comments.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: loading comment thread for " + getItem(position).getPhoto_id());

                ((HomeActivity)mContext).onCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));

                // Notes: If we navigate to the Comment Thread, we want to hide the ViewPager Layout and display the FrameLayout
                ((HomeActivity)mContext).hideLayout();
            }
        });

        // Notes: Set the time stamp of the post
        String timestampDifference = getTimestampDifference(getItem(position));
        if(!timestampDifference.equals("0"))
        {
            holder.timeDetla.setText(timestampDifference + " Days Ago");
        }
        else
        {
            holder.timeDetla.setText("Today");
        }

        // Notes: Set post image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);

        // Notes: Get the Profile Image and Username
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        // Notes: TODO - Prepare for a lot of debugging

        // Notes: TODO - BUG - Original Code
        // Notes: This query is responsible for finding the profile image and username of a photo
//        final Query query = reference
//                // Notes: Looking for the node that contains the object we're looking for
//                .child(mContext.getString(R.string.dbname_user_account_settings))
//                .orderByChild(mContext.getString(R.string.field_user_id))
//                .equalTo(getItem(position).getUser_id());

        // Notes: TODO - Temporary solution
        // Notes: This query is responsible for finding the profile image and username of a photo
        final Query query = reference
                // Notes: Looking for the node that contains the object we're looking for
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .child(getItem(position).getUser_id());

        // Notes: Look for all the likes in the photo
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Notes: TODO -  Original Code Error
                // Notes: If a match is found
//                for(DataSnapshot singleSnapshot: snapshot.getChildren())
//                {
////                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
//                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
//
//                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
//                    holder.username.setOnClickListener(new View.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(View v)
//                        {
//                            Log.d(TAG, "onClick: navigating to profile of: " +
//                                    holder.user.getUsername());
//
//                            Intent intent = new Intent(mContext, ProfileActivity.class);
//                            intent.putExtra(mContext.getString(R.string.calling_activity),
//                                    mContext.getString(R.string.home_activity));
//                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
//                            mContext.startActivity(intent);
//                        }
//                    });
//
//                    // Notes: Set the profile image
//                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
//                            holder.mprofileImage);
//                    holder.mprofileImage.setOnClickListener(new View.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(View v)
//                        {
//                            Log.d(TAG, "onClick: navigating to profile of: " +
//                                    holder.user.getUsername());
//
//                            Intent intent = new Intent(mContext, ProfileActivity.class);
//                            intent.putExtra(mContext.getString(R.string.calling_activity),
//                                    mContext.getString(R.string.home_activity));
//                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
//                            mContext.startActivity(intent);
//                        }
//                    });
//
//
//                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
//                    holder.comment.setOnClickListener(new View.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(View v) {
//                            ((HomeActivity)mContext).onCommentThreadSelected(getItem(position), holder.settings);
//
//                            //another thing?
//                        }
//                    });
//                }

                // Notes: TODO - Temporary Substitute
                if(snapshot.exists())
                {
                    Log.d(TAG, "onDataChange: found user: "
                            + snapshot.getValue(UserAccountSettings.class).getUsername());

                    // Notes: Set username
                    holder.username.setText(snapshot.getValue(UserAccountSettings.class).getUsername());
                    // Notes: Navigate to ProfileActivity if username was clicked
                    holder.username.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Log.d(TAG, "\tonClick: navigating to profile of: " +
                                    holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    // Notes: Set profile photo
                    imageLoader.displayImage(snapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.mprofileImage);
                    // Notes: Navigate to ProfileActivity if profile photo was clicked
                    holder.mprofileImage.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Log.d(TAG, "\tonClick: navigating to profile of: " +
                                    holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    // Notes: Set the settings icon on top left
                    holder.settings = snapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            ((HomeActivity)mContext).onCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));

                            // Notes: If we navigate to the Comment Thread, we want to hide the ViewPager Layout and display the FrameLayout
                            ((HomeActivity)mContext).hideLayout();
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        // Notes: Get the user object
        // Notes: TODO - Prepare for a lot of debugging
        // Notes: This query is responsible for finding the User
        final Query userQuery = mReference
                // Notes: Looking for the node that contains the object we're looking for
                .child(mContext.getString(R.string.dbname_users))
                // Notes: Looking for field that is inside the object
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                // Notes: If a match is found
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });



        return convertView;
    }


    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        ViewHolder mHolder;

        public GestureListener(ViewHolder holder)
        {
            mHolder = holder;
        }

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
                    .child(mContext.getString(R.string.dbname_photos))
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(mHolder.photo.getPhoto_id())
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(mContext.getString(R.string.field_likes));

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
                        if(mHolder.likeByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        {
                            // Notes: Removing the correct liker --> the current user
                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            // Notes: Removing the correct liker --> the current user
                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();


                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }

                        // Notes: Case 2 - The current user has not liked the photo
                        if(!mHolder.likeByCurrentUser)
                        {
                            // Notes: Add new like
                            addNewLike(mHolder);
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
                        addNewLike(mHolder);
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


    private void addNewLike(final ViewHolder holder)
    {
        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());


        // Notes: Adding a like
        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        // Notes: Adding a like
        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);


        holder.heart.toggleLike();
        getLikesString(holder);
    }



    private void getCurrentUsername()
    {
        Log.d(TAG, "\tgetCurrentUsername: retrieving user account settings");


        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        // Notes: TODO - Prepare for a lot of debugging

        // Notes: This query is responsible for finding the username
        final Query query = reference
                // Notes: Looking for the node that contains the object we're looking for
                .child(mContext.getString(R.string.dbname_users))
                // Notes: Looking for field that is inside the object
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                // Notes: If a match is found
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

    }


    /**
     * Notes: This method gets the string that is displayed on the post that
     *      says Liked by x,y,z
     */
    private void getLikesString(final ViewHolder holder)
    {
        Log.d(TAG, "\tgetLikesString: getting likes string");
        // Notes: If the currentUser liked they're own post, then the heart is red and they also appear in the LikesString

        try
        {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            // Notes: TODO - Prepare for a lot of debugging

            // Notes: This query is responsible for finding all the 'userID' that are attached to the likes of a photo
            final Query query = reference
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(mContext.getString(R.string.dbname_photos))
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(holder.photo.getPhoto_id())
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(mContext.getString(R.string.field_likes));

            // Notes: Look for all the likes in the photo
            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    holder.users = new StringBuilder();

                    // Notes: If a match is found for the particular photo for if there are likes or not
                    for(DataSnapshot singleSnapshot: snapshot.getChildren())
                    {
                        // Notes: Second query to find the 'userID'
                        Query query2 = reference
                                // Notes: Looking for the node that contains the object we're looking for
                                .child(mContext.getString(R.string.dbname_users))
                                // Notes: Looking for field that is inside the object
                                .orderByChild(mContext.getString(R.string.field_user_id)).equalTo(singleSnapshot.getValue(Like.class).getUser_id());

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

                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(", ");
                                }

                                if(snapshot.exists())
                                {
                                }

                                String[] splitUsers = holder.users.toString().split(", ");

                                // Notes: Check if the current user liked their own photo
                                if(holder.users.toString().contains(currentUsername + ","))
                                {
                                    // Notes: Use mLikedByCurrentUser to toggle the heart icon
                                    holder.likeByCurrentUser = true;
                                }
                                else
                                {
                                    holder.likeByCurrentUser = false;
                                }

                                int length = splitUsers.length;

                                if(length == 1)
                                {
                                    holder.likesString = "Liked by " + splitUsers[0];
                                }
                                if(length == 2)
                                {
                                    holder.likesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                                }
                                if(length == 3)
                                {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + " and " + splitUsers[2];
                                }
                                if(length == 4)
                                {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + splitUsers[3];
                                }
                                if(length > 4)
                                {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + (splitUsers.length - 3) + " others";
                                }


                                setupLikesString(holder, holder.likesString);

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
                        holder.likesString = "";
                        holder.likeByCurrentUser = false;

                        setupLikesString(holder, holder.likesString);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });
        }
        catch(NullPointerException e)
        {
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage() );
            holder.likesString = "";
            holder.likeByCurrentUser = false;

            setupLikesString(holder, holder.likesString);
        }


    }

    private void setupLikesString(final ViewHolder holder, String likesString){
        Log.d(TAG, "\tsetupLikesString: likes string:" + holder.likesString);


        // Notes: if current user liked the photo
        if(holder.likeByCurrentUser)
        {
            Log.d(TAG, "\tsetupLikesString: photo is liked by current user");

            // Notes: TODO - Do we even need this?
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);

            holder.heartRed.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "\tonTouch: red heart touch detected");
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        // Notes: User didn't like photo
        else
        {
            Log.d(TAG, "setupLikesString: photo is not liked by current user");

            // Notes: TODO - Do we even need this?
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);

            holder.heartWhite.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "\tonTouch: white heart touch detected");
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(likesString);
    }

    /**
     * Notes: Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(Photo photo)
    {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;

        final String photoTimestamp = photo.getDate_created();

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


}
