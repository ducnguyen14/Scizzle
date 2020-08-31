package com.example.instaclone.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.instaclone.R;
import com.example.instaclone.models.Comment;
import com.example.instaclone.models.UserAccountSettings;
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

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "CommentListAdapt/DEBUG";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;


    public CommentListAdapter(@NonNull Context context, @LayoutRes int resource,
                              @NonNull List<Comment> objects)
    {
        // Notes: Need to use super because we will be referencing items from the List<Comment> objects.
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    /**
     * Notes: Using the ViewHolder build pattern to view the images.
     *      The ViewHolder doesn’t load the all the widgets at once.
     *      It only loads a few of them to make the app faster (similar to RecyclerView)
     */
    private static class ViewHolder{
        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Notes: ViewHolder build pattern (Similar to RecyclerView)
        final ViewHolder holder;

        if(convertView == null)
        {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.username = (TextView) convertView.findViewById(R.id.comment_username);
            holder.timestamp = (TextView) convertView.findViewById(R.id.comment_time_posted);
            holder.reply = (TextView) convertView.findViewById(R.id.comment_reply);
            holder.like = (ImageView) convertView.findViewById(R.id.comment_like);
            holder.likes = (TextView) convertView.findViewById(R.id.comment_likes);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.comment_profile_image);

            /*
                Notes: Tag is a way you can store widgets in memory.
                    ViewHolder creates all the widgets, and tag will hold entire ViewHolder.
                    We store the view in memory and not putting it on the page so it doesn't cause the app
                    to slow down.
             */
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        /*
            Notes: The reason why in the constructor we passed imgURLs to the super so that
                we can access it via getItem(int) or else getItem(int) will return null
         */

        // Notes: Set the comment
        holder.comment.setText(getItem(position).getComment());

        // Notes: Set the timestamp difference
        String timestampDifference = getTimestampDifference(getItem(position));
        if(!timestampDifference.equals("0"))
        {
            holder.timestamp.setText(timestampDifference + " d");
        }
        else
        {
            holder.timestamp.setText("today");
        }




        // Notes: TODO - Prepare for debugging
        // Notes: Set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                // Notes: Looking for the node that contains the object we're looking for
                .child(mContext.getString(R.string.dbname_user_account_settings))
                // Notes: Looking for field that is inside the object
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Notes: If a match is found
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren())
                {
                    // Notes: Set username
                    holder.username.setText(
                            singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    // Notes: Set profile photo
                    imageLoader.displayImage(
                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

        try
        {
            // Notes: First comment of the photo is really the caption
            if(position == 0)
            {
                holder.like.setVisibility(View.GONE);
                holder.likes.setVisibility(View.GONE);
                holder.reply.setVisibility(View.GONE);
            }
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, "getView: NullPointerException: " + e.getMessage() );
        }


        return convertView;
    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(Comment comment){
        Log.d(TAG, "\tgetTimestampDifference: getting timestamp difference.");


        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;

        final String photoTimestamp = comment.getDate_created();

        try
        {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }
        catch (ParseException e)
        {
            Log.e(TAG, "\tgetTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }


}
