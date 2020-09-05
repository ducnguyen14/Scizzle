package com.example.instaclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instaclone.R;
import com.example.instaclone.Utils.FirebaseMethods;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NextActivity extends AppCompatActivity {
    private static final String TAG = "NextActivity/DEBUG";

    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    // Notes: Widgets
    private EditText mCaption;

    // Notes: Variables
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    private Intent intent;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = (EditText) findViewById(R.id.caption);

        setupFirebaseAuth();


        // Notes: Back Arrow
        ImageView backArrow = (ImageView) findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: closing the activity.");

                // Notes: Finishes activity/close fragment and takes us back to where we were before
                finish();
            }
        });

        // Notes: Navigate to the NextActivity
        TextView share = (TextView) findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener()
        {
            // Notes: Navigating to confirmation page
            @Override
            public void onClick(View v) {
                Log.d(TAG, "\tonClick: navigating to the final confirmation share screen.");

                // Notes: Upload Image to firebase
                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();

                String caption = mCaption.getText().toString();



                // Notes: Intent comes from GalleryFragment
                if(intent.hasExtra(getString(R.string.selected_image)))
                {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl, null);

                }
                // Notes: Intent comes from PhotoFragment (Camera)
                else if(intent.hasExtra(getString(R.string.selected_bitmap)))
                {
                    bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, null, bitmap);


                }




            }
        });

        setImage();

    }



    private void someMethod()
    {
        /*
            Step 1)
            Create a data model for photos

            Step 2)
            Add properies to the photo objects (caption, date, imageURL, photo_id, tags, user_id)

            Step 3)
            Count the number of photos that the user already has

            Step 4)
            a) Upload the photo to Firebase Storage
            b) Insert into 'photos' node
            c) Insert into 'user_photos' node
         */
    }





    /**
     * Notes: Gets the image URL from the incoming intent and displays the
     *      chosen image.
     */
    private void setImage()
    {
        intent = getIntent();
        ImageView image = (ImageView) findViewById(R.id.imageShare);

        // Notes: Intent comes from GalleryFragment
        if(intent.hasExtra(getString(R.string.selected_image)))
        {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "\tsetImage: got new image url: " + imgUrl);
            // Notes: Universal Image Loader can handle null strings and set default image
            UniversalImageLoader.setimage(imgUrl, image, null, mAppend);
        }
        // Notes: Intent comes from PhotoFragment (Camera)
        else if(intent.hasExtra(getString(R.string.selected_bitmap)))
        {
            bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "\tsetImage: got new bitmap");
            image.setImageBitmap(bitmap);
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
        Log.d(TAG, "\tonDataChange: image count: " + imageCount);


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


        // Notes: Read or write to the database
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                imageCount = mFirebaseMethods.getImageCount(snapshot);
                Log.d(TAG, "\tonDataChange: image count: " + imageCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });



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
