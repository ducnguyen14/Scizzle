package com.example.instaclone.Share;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaclone.R;
import com.example.instaclone.Utils.Permissions;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment/DEBUG";

    // Notes: Constants
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    // Notes: Doesn't matter what the Request code is, as long as your consistent
    private static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: started");

        Button btnLaunchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: launching camera.");

                // Notes: This will tell us that we came from ShareActivity
                if(((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM)
                {
                    // Notes: Check camera permissions
                    if(((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0]))
                    {
                        Log.d(TAG, "\tonClick: starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }
                    // Notes: If the permission is not verified, then we restart ShareActivity and permissions will be asked again
                    else
                    {
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        // Notes: Clear the activity stack for navigating back to ShareActivity
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }


                }


            }
        });


        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE)
        {
            Log.d(TAG, "\tonActivityResult: done taking a photo");
            Log.d(TAG, "\tonActivityResult: attempting to navigate to final share screen.");

            // Notes: navigate to the final share screen to publish photo

        }



    }
}
