package com.example.instaclone.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.instaclone.R;
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

public class UserListAdapter extends ArrayAdapter<User>{
    private static final String TAG = "UserListAdapter/DEBUG";


    private LayoutInflater mInflater;
    private List<User> mUsers = null;
    private int layoutResource;
    private Context mContext;


    public UserListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        // Notes: Need to use super because we will be referencing items from the List<User> objects.
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mUsers = objects;

    }

    /**
     * Notes: Using the ViewHolder build pattern to view the images.
     *      The ViewHolder doesn’t load the all the widgets at once. Widgets are loaded into memory.
     *      It only loads a few of them to make the app faster (similar to RecyclerView)
     */
    private static class ViewHolder{
        TextView username, email;
        CircleImageView profileImage;
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
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.profile_image);

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
//                    ImageLoader imageLoader = ImageLoader.getInstance();
//
//                    // Notes: Set profile photo
//                    imageLoader.displayImage(
//                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
//                            holder.profileImage);

//                    // Notes: Temporary Substitute
                    UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
                    ImageLoader.getInstance().init(universalImageLoader.getConfig());
                    // Notes: Set profile photo
                    ImageLoader.getInstance().displayImage(
                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

        return convertView;
    }

}
