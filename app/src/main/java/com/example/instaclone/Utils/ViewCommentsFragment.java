package com.example.instaclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaclone.R;
import com.example.instaclone.models.Comment;
import com.example.instaclone.models.Like;
import com.example.instaclone.models.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewCommentsFragment extends Fragment {
    private static final String TAG = "ViewCommentsFrag/DEBUG";

    public ViewCommentsFragment()
    {
        super();

        /*
            Notes: An empty bundle can cause a NullPtrException when receiving from an interface. Need to always do setArguments to new
                bundle in the constructor when passing information through a bundle. (We passed arguments
                through bundle from OnCommentThreadSelectedListener interface)

         */
        setArguments(new Bundle());
    }

    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    // Notes: Widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    // Notes: Variables
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mCheckMark = (ImageView) view.findViewById(R.id.ivPostComment);
        mComment = (EditText) view.findViewById(R.id.comment);
        mListView = (ListView) view.findViewById(R.id.listView);
        mComments = new ArrayList<>();

        // Notes: Bug - somehow somewhere along the way, context gets lost, created this var as a solution so that context is constant
        mContext = getActivity();

        try
        {
            mPhoto = getPhotoFromBundle();
            setupFirebaseAuth();

        }
        catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage() );
        }



        return view;
    }

    private void setupWidgets()
    {
        Log.d(TAG, "\tsetupWidgets: started");

        // Notes: Set comment list to adapter
        CommentListAdapter adapter = new CommentListAdapter(mContext,
                R.layout.layout_comment, mComments);

        // Notes: Set adapter to ListView
        mListView.setAdapter(adapter);

        // Notes: Post a new comment
        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Notes: TODO - Rewrite this line for better understanding --> If comment has content
                if(!mComment.getText().toString().equals(""))
                {
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                }
                else
                {
                    Toast.makeText(mContext, "you can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Notes: Back Navigation
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


    }

    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if(view != null)
        {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void addNewComment(String newComment)
    {
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        // Notes: Generating a random keyID for comment
        String commentID = myRef.push().getKey();

        // Notes: Creating a new comment
        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Notes: insert into photos node
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        // Notes: insert into user_photos node
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

    }

    private String getTimestamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return sdf.format(new Date());
    }

    /**
     * Notes: Retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if(bundle != null)
        {
            return bundle.getParcelable(mContext.getString(R.string.photo));
        }
        else
        {
            return null;
        }
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


         if(mPhoto.getComments().size() == 0)
         {
             mComments.clear();

             // Notes: The caption is the first comment
             Comment firstComment = new Comment();
             firstComment.setComment(mPhoto.getCaption());
             firstComment.setUser_id(mPhoto.getUser_id());
             firstComment.setDate_created(mPhoto.getDate_created());
             mComments.add(firstComment);

             mPhoto.setComments(mComments);

             setupWidgets();

         }


        /*
            Notes: Add an onChildEventListener to the Photo node to trigger the query whenever an update has been made to comments.
                This onChild EventListener will constantly listen whenever a change has been made to the node and will update the
                attributes of the photo.
         */
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                // Notes: addChildEventListener gets called whenever a change has been made to the comment node inside the Photo object, inside the Photo Node.
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
                    {
                        Log.d(TAG, "\tonChildAdded: child added");
                        // Notes: Need to requery the Photo to get the updated comments
                        Query query = myRef
                                .child(mContext.getString(R.string.dbname_photos))
                                // Notes: Looking for field that is inside the object
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                                {
                                    // Notes: Error -   com.google.firebase.database.DatabaseException: Expected a List while deserializing, but got a class java.util.HashMap
                                    // photos.add(singleSnapshot.getValue(Photo.class));

                                    // Notes: (Solution) Type cast the snapshot to a hashmap and then add the fields manually to the photo

                                    // Notes: Step 1) Type cast snapshot into hashmap
                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                    // Notes: Step 2) Add fields manually to photo object
                                    photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());


                                    // Notes: Get a list of comments for the Photo

                                    //Notes: Clear the list of comments so no comments gets repeated whenever this ChildEventListener is triggered
                                    mComments.clear();

                                    // Notes: The caption is the first comment
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());
                                    mComments.add(firstComment);

                                    for(DataSnapshot datasnapshot: singleSnapshot
                                            .child(mContext.getString(R.string.field_comments))
                                            .getChildren())
                                    {
                                        // Notes: Getting individual comment
                                        Comment comment = new Comment();
                                        comment.setUser_id(datasnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(datasnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(datasnapshot.getValue(Comment.class).getDate_created());

                                        // Notes: Adding to a list of comments
                                        mComments.add(comment);
                                    }

                                    // Notes: Adding comments to the Photo
                                    photo.setComments(mComments);

                                    mPhoto = photo;

                                    setupWidgets();

                                    // Notes: TODO - Get likes for the comment
                    //                    List<Like> likesList = new ArrayList<Like>();
                    //                    for(DataSnapshot datasnapshot: singleSnapshot
                    //                            .child(mContext.getString(R.string.field_likes))
                    //                            .getChildren())
                    //                    {
                    //                        // Notes: Getting individual likes
                    //                        Like like = new Like();
                    //                        like.setUser_id(datasnapshot.getValue(Like.class).getUser_id());
                    //
                    //                        // Notes: Adding to a list of likes
                    //                        likesList.add(like);
                    //                    }


                                }



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }
                        });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        Log.d(TAG, "\tsetupFirebaseAuth: finished");
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
