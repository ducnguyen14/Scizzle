package com.example.instaclone.Utils;

import android.content.Context;
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

import com.example.instaclone.R;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "MainfeedListAdapter/DEBUG";

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
            holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.profile_image);
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

        /*
            Notes: The reason why in the constructor we passed objects to the super so that
                we can access it via getItem(int) or else getItem(int) will return null
         */

        // Notes: Set values to the widgets
        holder.username.setText(getItem(position).getUsername());
        holder.email.setText(getItem(position).getEmail());

        // Notes: Need to query user_account_settings for the profile photo
        // Notes: TODO - Original Code - Doesn't work because on Firebase, user_account_settings does NOT have the attribute userID
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference
////                // Notes: Looking for the node that contains the object we're looking for
////                .child(mContext.getString(R.string.dbname_user_account_settings))
////                // Notes: Looking for field that is inside the object
////                .orderByChild(mContext.getString(R.string.field_user_id))
////                .equalTo(getItem(position).getUser_id());

        // Notes: TODO - Temporary substitute
        Query query = reference
                // Notes: Looking for the node that contains the object we're looking for
                .child(mContext.getString(R.string.dbname_user_account_settings))
                // Notes: Looking for field that is inside the object
                .orderByChild(mContext.getString(R.string.field_username))
                .equalTo(getItem(position).getUsername());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Notes: If a match is found
                // Notes: TODO - Original code doesn't work
//                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren())
//                {
//
//                    ImageLoader imageLoader = ImageLoader.getInstance();
//
//                    // Notes: Set profile photo
//                    imageLoader.displayImage(
//                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
//                            holder.profileImage);
//                }

                // Notes: TODO - Temporary substitute
                // Notes: if the DataSnapshot does not exists (No match found)
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "\tonDataChange: Setting Profile Picture!!! --> " + singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo());


                    // Notes: Original Code
                    ImageLoader imageLoader = ImageLoader.getInstance();

                    // Notes: Set profile photo
                    imageLoader.displayImage(
                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);

//                    // Notes: Temporary Substitute
//                    UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
//                    ImageLoader.getInstance().init(universalImageLoader.getConfig());
//                    // Notes: Set profile photo
//                    ImageLoader.getInstance().displayImage(
//                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
//                            holder.profileImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
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
                            getLikesString();
                        }

                        // Notes: Case 2 - The current user has not liked the photo
                        if(!mHolder.likeByCurrentUser)
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


}
