package com.example.instaclone.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.instaclone.R;
import com.example.instaclone.models.User;
import com.example.instaclone.models.UserAccountSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods/DEBUG";

    private boolean debug = false;


    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;

    private Context mContext;

    public FirebaseMethods(Context context)
    {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mContext = context;


        if(mAuth.getCurrentUser() != null)
        {
            userID = mAuth.getCurrentUser().getUid();
        }

    }



    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot)
    {
        Log.d(TAG, "\tcheckIfUsernameExists: checking if " + username + " already exists.");

        User user = new User();

        // Notes - TODO - How to loop inside a datasnapshot
        if(debug == true)
        {
            Log.d(TAG, "\tcheckIfUsernameExists: datasnapshot: " + dataSnapshot);
            Log.d(TAG, "\tcheckIfUsernameExists: datasnapshot.getChildren(): " + dataSnapshot.getChildren());
            Log.d(TAG, "\tcheckIfUsernameExists: NODE = " + dataSnapshot.child(userID).toString());
            Log.d(TAG, "\tcheckIfUsernameExists: NODE = " + dataSnapshot.child(userID).getChildren().toString());

            for (DataSnapshot ds: dataSnapshot.getChildren())
            {
                Log.d(TAG, "\tcheckIfUsernameExists: ds: " + ds);
                User user2 = new User();
                Log.d(TAG, "\tcheckIfUsernameExists: getValue(User.class)" + ds.getValue(User.class).toString());
                Log.d(TAG, "\tcheckIfUsernameExists: getValue(User.class)" + ds.getValue(User.class).getUsername());
                user2 = ds.getValue(User.class);
//            user2.setUsername(ds.getValue(User.class).getUsername());
                Log.d(TAG, "\tcheckIfUsernameExists: username: " + user2.getUsername());
            }
        }


        // Notes: TODO - The loop below doesn't even go check the nodes
        /*
            Notes: Loop through DataSnapshot to check for same username.
                DataSnapshot allows us to see what's inside the database
                because it contains every node inside the database.
         */
        for (DataSnapshot ds: dataSnapshot.child(userID).getChildren())
        {
            Log.d(TAG, "\tcheckIfUsernameExists: datasnapshot: " + ds);

            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "\tcheckIfUsernameExists: username: " + user.getUsername());

            // Notes: TODO - Rewrite this line to make it easier to read
            if(StringManipulation.expandUsername(user.getUsername()).equals(username))
            {
                Log.d(TAG, "\tcheckIfUsernameExists: Found a match: " + user.getUsername());
                return true;
            }
        }

        return false;



    }


    /**
     * Notes: This method is for registering a new email
     *      and password to Firebase Authentication
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (!task.isSuccessful())
                        {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "\tcreateUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "\tcreateUserWithEmail:success");

                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "\tonComplete: Authstate changed " + userID);

                        }

                        // ...
                    }
                });
    }


    public void addNewUser(String email, String username, String description, String website, String profile_photo)
    {
        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));

        // Notes: DEBUG tools
        if(debug == true)
        {
            Log.d(TAG, "\taddNewUser: myRef = " + myRef.child(mContext.getString(R.string.dbname_users)).toString());
            Log.d(TAG, "\taddNewUser: myRef = " + myRef.child(mContext.getString(R.string.dbname_users)).child(userID).toString());
        }


        // Notes: Inserting user into Firebase Database.
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        // Notes: Universal Image Loader can handle an empty string, it will load the default image
        UserAccountSettings settings = new UserAccountSettings(description, username, 0, 0, 0, profile_photo, username, website);

        // Notes: Inserting user_account_settings into Firebase Database.
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);



    }





}
