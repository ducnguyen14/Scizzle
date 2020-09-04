package com.example.instaclone.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaclone.R;
import com.example.instaclone.Utils.MainfeedListAdapter;
import com.example.instaclone.models.Comment;
import com.example.instaclone.models.Like;
import com.example.instaclone.models.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment/DEBUG";

    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mListView = (ListView) view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();


        return view;
    }

    /**
     * Notes: This method retrieves a list of all the userIDs that the current user is following
     */
    private void getFollowing()
    {
        Log.d(TAG, "\tgetFollowing: searching for following");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                // Notes: Looking for the node that contains the object we're looking for
                .child(getString(R.string.dbname_following))
                // Notes: Looking for the node that contains the object we're looking for
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Notes: Match found
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                    // Notes: Add to following list
                    mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }

                // Notes: Get the photos
                getPhotos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


    private void getPhotos()
    {
        Log.d(TAG, "getPhotos: getting photos");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < mFollowing.size(); i++)
        {
            final int count = i;

            // Notes: Looping through the list of following to retrive their photos
            Query query = reference
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(getString(R.string.dbname_user_photos))
                    // Notes: Looking for the node that contains the object we're looking for
                    .child(mFollowing.get(i))
                    // Notes: Looking for field that is inside the object
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                    {

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

                        // Notes: Step 3) Add photo object to list of photos to be displayed on MainFeedListView
                        mPhotos.add(photo);
                    }

                    // Notes: When we got all our list items we were looking for, we've reached the end
                    if(count >= mFollowing.size() -1)
                    {
                        // Notes: Display our photos
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private void displayPhotos()
    {
        if(mPhotos != null)
        {
            Collections.sort(mPhotos, new Comparator<Photo>()
            {
                /**
                 * Notes: Sorting photo in terms of date
                 * @param o1
                 * @param o2
                 * @return
                 */

                @Override
                public int compare(Photo o1, Photo o2)
                {
                    return o2.getDate_created().compareTo(o1.getDate_created());
                }
            });

            mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPhotos);
            mListView.setAdapter(mAdapter);
        }
    }




}
