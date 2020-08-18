package com.example.instaclone.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaclone.R;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment/DEBUG";

    private ImageView mProfilePhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = (ImageView) view.findViewById(R.id.profile_photo);

        initImageLoader();
        setProfileImage();


        return view;
    }

    /**
     * Notes: Initialized the ImageLoader with its configurations
     */
    private void initImageLoader()
    {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }



    private void setProfileImage()
    {
        Log.d(TAG, "setProfileImage: setting profile image");

        String imgURL = "i.pinimg.com/originals/19/58/7f/19587f4696f74eeea6f387816b9bff88.jpg";
        UniversalImageLoader.setimage(imgURL,mProfilePhoto, null, "https://");

    }





}
