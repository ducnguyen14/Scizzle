package com.example.instaclone.Login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instaclone.R;
import com.example.instaclone.Utils.FirebaseMethods;
import com.example.instaclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity/DEBUG";


    private Context mContext;
    private String email, username, password;
    private EditText mEmail, mPassword, mUsername;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;


    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;


    private String append = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Notes: TODO - Context is already assigned in initWidgets()
        mContext = RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(mContext);

        Log.d(TAG, "onCreate: started");

        initWidgets();
        setupFirebaseAuth();
        init();
    }


    /**
     * Notes: This method initializes the button for register
     */
    private void init()
    {
        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                if(checkInputs(email, username, password))
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);

                    /*
                        Notes: Register/Sign up section. The auth state will change to
                            sign in if successful or signed out if unsuccessful
                     */
                    firebaseMethods.registerNewEmail(email, password, username);

                }


            }
        });
    }


    private boolean checkInputs(String email, String username, String password)
    {
        Log.d(TAG, "\tcheckInputs: checking inputs for null values.");
        if(isStringNull(email) || isStringNull(username) || isStringNull(password))
        {
            Toast.makeText(mContext, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }




    /**
     * Notes: This method is for initializing the activity widgets
     */
    private void initWidgets()
    {
        Log.d(TAG, "\tinitWidgets: Initializing Widgets");
        mEmail = (EditText) findViewById(R.id.input_email);
        mUsername = (EditText) findViewById(R.id.input_username);
        btnRegister = (Button) findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        loadingPleaseWait = (TextView) findViewById(R.id.loadingPleaseWait);
        mPassword = (EditText) findViewById(R.id.input_password);
        mContext = RegisterActivity.this;


        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);
    }


    private boolean isStringNull(String string)
    {
        Log.d(TAG, "\tisStringNull: checking string if null");

        if(string.equals(""))
        {
            return true;
        }
        else
        {
            return false;
        }

    }



    /**
     * ***************************** Firebase *****************************
     */


    /**
     * Notes: Check if @param username already exists in the database
     * @param username
     */
    private void checkIfUsernameExists(final String username)
    {
        Log.d(TAG, "\tcheckIfUsernameExists: Checking if " + username + "already exists");

        // Notes: TODO - I think there's a DatabaseReferece declared and initialized in setupFirebaseAuth()
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                // Notes: Looking for the node that contains the object we're looking for
                .child(getString(R.string.dbname_users))
                // Notes: Looking for field that is inside the object
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {

            /*
             * Notes: This method returns a DataSnapshot only if a match was found
             * @param snapshot
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Notes: Only returned a single item from the database
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    // Notes: Match Found - 1st Check - Make sure the username is not already taken
                    if(singleSnapshot.exists())
                    {
                        Log.d(TAG, "\tonDataChange: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());

                        /*
                            Notes: The username already exists. Use myRef.push()
                                to generate a random key. These keys are very long,
                                so we just want from 3-10 indices
                        */
                        append = myRef.push().getKey().substring(3, 10);
                        Log.d(TAG, "\tonDataChange: username already exists. Appending random string to name: " + append);

                    }
                }

                String mUsername = "";

                mUsername = username + append;

                // Notes: Add new user to the database
                firebaseMethods.addNewUser(email, mUsername, "", "", "");

                Toast.makeText(mContext, "Signup Successful. Sending verification email", Toast.LENGTH_SHORT).show();

                            /*
                                Notes: Firebase automatically signs in new user, we want to sign out user until
                                    user verified email.
                             */
                mAuth.signOut();





            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    /**
     * Notes: Setup the firebase auth object
     */
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "\tsetupFirebaseAuth: setting up firebase auth");

        /*
            Notes: FirebaseAuth and FirebaseDatabase works on an Instance basis,the same
                object is usable app-wide
         */
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // Notes: Reference to database
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

                    myRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            checkIfUsernameExists(username);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error)
                        {

                        }
                    });

                    // Notes: Navigating back to LoginActivity
                    finish();
                }
                else
                {
                    // Notes: User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
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
