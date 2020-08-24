package com.example.instaclone.Share;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaclone.R;
import com.example.instaclone.Utils.FilePaths;
import com.example.instaclone.Utils.FileSearch;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment/DEBUG";

    // Notes: widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;

    // Notes: Variables
    private ArrayList<String> directories;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        Log.d(TAG, "onCreateView: started");

        galleryImage = (ImageView) view.findViewById(R.id.galleryImageView);
        gridView = (GridView) view.findViewById(R.id.gridView);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        directories = new ArrayList<>();


        ImageView shareClose = (ImageView) view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: closing the gallery fragment.");

                // Notes: Finishes activity/close fragment and takes us back to where we were before
                getActivity().finish();
            }
        });

        TextView nextScreen = (TextView) view.findViewById(R.id.tvNext);

        nextScreen.setOnClickListener(new View.OnClickListener()
        {
            // Notes: Navigating to confirmation page
            @Override
            public void onClick(View v) {
                Log.d(TAG, "\tonClick: navigating to the final share screen.");

                // Notes: TODO - Need to write logic to handle coming from ShareActivity or EditProfileFragment
            }
        });

        init();


        return view;
    }


    private void init()
    {
        FilePaths filePaths = new FilePaths();

        // Notes: (Default root directory) Check for other folders inside "/storage/emulated/0/pictures)"
        if(FileSearch.getDirectoryPaths(filePaths.PICTURES) != null)
        {
            // Notes: Make a list of directories that's inside PICTURES directory
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }

        directories.add(filePaths.CAMERA);


        // Notes: Creating adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, directories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        directorySpinner.setAdapter(adapter);

        // Notes: Spinner will display all directories that's inside PICTURES and CAMERA
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "\tonItemSelected: selected: " + directories.get(position));

                // Notes: Setup image grid for directory chosen
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });


    }





}
