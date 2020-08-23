package com.example.instaclone.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaclone.R;
import com.example.instaclone.Utils.FirebaseMethods;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.example.instaclone.dialogs.ConfirmPasswordDialog;
import com.example.instaclone.models.User;
import com.example.instaclone.models.UserAccountSettings;
import com.example.instaclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener{


    // Notes: Interface implementation
    @Override
    public void onConfirmPassword(String password)
    {
        // Notes: TODO - delete this log, not good to show password
        Log.d(TAG, "\tonConfirmPassword: got the password: " + password);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "\tUser re-authenticated.");

                            // Notes: Check to see if the new email is present in the database --> Use method fetchProvidersForEmail(String email)
                            // Notes: Step 2
//                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener();
                            mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task)
                                {
                                    // Notes: The email already exists
                                    if(task.isSuccessful())
                                    {
                                        try
                                        {
                                            // Notes: If size = 1, that means we retrieved something
                                            if(task.getResult().getSignInMethods().size() == 1)
                                            {
                                                Log.d(TAG, "\tonComplete: that email is already in use");
                                                Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                            }
                                            // Notes: Unable to find an email
                                            else
                                            {
                                                Log.d(TAG, "onComplete: That email is available");

                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                                // Notes: Update email (Step 3)
                                                user.updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getActivity(), "Email updated", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());

                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                        catch(NullPointerException e)
                                        {
                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                        }

                                    }
                                }
                            });





                        }
                        else
                        {
                            Log.d(TAG, "\tonComplete: re-authentication failed");
                        }

                    }
                });



    }




    private static final String TAG = "EditProfileFrag/DEBUG";



    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //Notes: EditProfile Fragment widgets
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    // Notes: Variables
    private UserSettings mUserSettings;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);

        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUsername = (EditText) view.findViewById(R.id.username);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mDescription = (EditText) view.findViewById(R.id.description);
        mEmail = (EditText) view.findViewById(R.id.email);
        mPhoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods = new FirebaseMethods(getActivity());

        // Notes: This method will also sets the widgets, profile pic, etc.
        setupFirebaseAuth();

        // Notes: Back arrow for navigating back to ProfileActivity
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: navigating back to ProfileActivity");
                /*
                    Notes: Because we are in a fragment, we need to use getActivity()
                        The activity in this case is the AccountSettingsActivity
                 */
                getActivity().finish();
            }
        });


        ImageView checkmark = (ImageView) view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: attempting to save changes.");
                saveProfileSettings();
            }
        });





        return view;
    }

    /**
     * Notes: Retrieves the data contained in the widgets and submits it to the database.
     *      before submitting, it will check to make sure the username and email chosen are unique.
     */
    private void saveProfileSettings()
    {

        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());


        // Notes: case1 - If the user made a change to their username
        if(!mUserSettings.getUser().getUsername().equals(username))
        {
            checkIfUsernameExists(username);
        }
        // Notes: case2 - If the user made a change to their email
        if(!mUserSettings.getUser().getEmail().equals(email))
        {
            /*
                Notes: Step 1 - Reauthenticate
                            - Confirm the password and email
            */

            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));

            // Notes: This allows passing variables from interface directly to fragment
            dialog.setTargetFragment(EditProfileFragment.this, 1);

            /*
                Notes: Step 2 - Check if email already is registered
                            - Use method fetchProvidersForEmail(String email)
                       Step 3 - Change the email
                            - Submit the new email to the database and authentication
             */
        }


        /*
            Notes: Rest of the settings section that do not require uniqueness (except phone number)
         */

        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName))
        {
            // Notes: Update displayname
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
        }
        if(!mUserSettings.getSettings().getWebsite().equals(website))
        {
            // Notes: Update website
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0);

        }
        if(!mUserSettings.getSettings().getDescription().equals(description))
        {
            // Notes: Update description
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0);

        }
        if(!Long.toString(mUserSettings.getUser().getPhone_number()).equals(Long.toString(phoneNumber)))
        {
            // Notes: Update phone number
            // Notes: TODO - Check for uniqueness for phone number
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);

        }






    }


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

                // Notes: if the DataSnapshot does not exists (No match found)
                if(!snapshot.exists())
                {
                    // Notes: Add the username
                    mFirebaseMethods.updateUsername(username);

                    Toast.makeText(getActivity(), "Saving username.", Toast.LENGTH_SHORT).show();

                }

                // Notes: Only returned a single item from the database
                for(DataSnapshot singleSnapshot: snapshot.getChildren())
                {
                    // Notes: Match Found
                    if(singleSnapshot.exists())
                    {
                        Log.d(TAG, "\tonDataChange: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username already exists.", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }



    private void setProfileWidgets(UserSettings userSettings)
    {
        Log.d(TAG, "\tsetProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());

        mUserSettings = userSettings;



//        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        // Notes: If the image is null, UniversalImageLoader will set default image
        UniversalImageLoader.setimage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

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
        userID = mAuth.getCurrentUser().getUid();


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
                // Notes: Retrieve user's info from database
                // Notes: TODO - Rewrite this line for easier read
                setProfileWidgets(mFirebaseMethods.getUserSettings(snapshot));

                // Notes: Retrieve user's images from database

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
