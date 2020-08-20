package com.example.instaclone.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaclone.Login.LoginActivity;
import com.example.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignOutFragment extends Fragment {
    private static final String TAG = "SignOutFragment/DEBUG";

    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar mProgressBar;
    private TextView tvSignout, tvSigningOut;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_signout, container, false);
        tvSignout = (TextView) view.findViewById(R.id.tvConfirmSignout);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvSigningOut = (TextView) view.findViewById(R.id.tvSigningOut);

        Button btnConfirmSignout = (Button) view.findViewById(R.id.btnConfirmSignout);

        mProgressBar.setVisibility(View.GONE);
        tvSigningOut.setVisibility(View.GONE);

        setupFirebaseAuth();

        btnConfirmSignout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: attempting to sign out");

                mProgressBar.setVisibility(View.VISIBLE);
                tvSigningOut.setVisibility(View.VISIBLE);

                mAuth.signOut();
                getActivity().finish();

            }
        });




        return view;
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

                    Log.d(TAG, "\tonAuthStateChanged: navigating back to login screen");
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    /*
                        Notes: If user presses back button to try to get into the app again after
                            signing out, we need to clear the activity stack by setting an
                            intent flag to prevent the user
                     */
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


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
