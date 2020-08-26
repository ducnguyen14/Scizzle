package com.example.instaclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.example.instaclone.Utils.GridImageAdapter;
import com.example.instaclone.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    // Notes: Constants
    private static final String TAG = "GalleryFragment/DEBUG";
    private static final int NUM_GRID_COLUMNS = 3;




    // Notes: widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;

    // Notes: Variables
    private ArrayList<String> directories;
    // Notes: Prepend for the Universal Image Loader
    private String mAppend = "file:/";
    private String mSelectedImage;




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

        // Notes: Close Button
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

        // Notes: Navigate to the NextActivity
        TextView nextScreen = (TextView) view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener()
        {
            // Notes: Navigating to confirmation page
            @Override
            public void onClick(View v) {
                Log.d(TAG, "\tonClick: navigating to the final confirmation share screen.");

                Intent intent = new Intent(getActivity(), NextActivity.class);
                intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                startActivity(intent);

                // Notes: TODO - Need to write logic to handle coming from ShareActivity or EditProfileFragment
            }
        });

        init();


        return view;
    }


    private void init()
    {
        FilePaths filePaths = new FilePaths();

        // NOTES: TODO - Take a look at why is there a .thumbnail directory in PICTURES (This gives Null ptr Exception)
        // Notes: (Default root directory) Check for other folders inside "/storage/emulated/0/pictures)"
        if(FileSearch.getDirectoryPaths(filePaths.PICTURES) != null)
        {
            // Notes: Make a list of directories that's inside PICTURES directory
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }

        directories.add(filePaths.CAMERA);


        // Notes: Shorter name of the directories
        ArrayList<String> directoryNames =  new ArrayList<>();
        for(int i = 0; i < directories.size(); i++)
        {
            int length_of_string = directories.get(i).length();
            int index = directories.get(i).lastIndexOf("/");
            // Notes: TODO - Figure out how to only get the directory of CAMERA
            String dir = directories.get(i).substring(index + 1, length_of_string);
//            String dir = directories.get(i).substring(index);
            Log.d(TAG, "\t\t\tinit: dir = " + dir);
            directoryNames.add(dir);
        }


        // Notes: Creating adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, directoryNames);
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
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }


    private void setupGridView(String selectedDirectory)
    {
        Log.d(TAG, "\tsetupGridView: directory chosen: " + selectedDirectory);
        // Notes: All image URLs from directory chosen
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);


        // Notes: Grid images should have the same height as width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;

        // Notes: Set the grid column width
        gridView.setColumnWidth(imageWidth);


        /*
            Notes: Use the GridImageAdapter to adapt the images to gridview.
                Need to prepend for the Universal Image Loader
        */
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, mAppend, imgURLs);

        // Notes: Set adapter to gridview
        gridView.setAdapter(adapter);


        // Notes: Set the first image to be displayed when the activity fragment view is inflated
        // Notes: DEBUG - Array out of bounds
        if(imgURLs.size() > 0)
        {
            setImage(imgURLs.get(0), galleryImage, mAppend);
            mSelectedImage = imgURLs.get(0);
        }


        // Notes: Changing the iamgeview when an image is clicked on in the gridview
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "\tonItemClick: selected an image: " + imgURLs.get(position));

                // Notes: DEBUG - Array out of bounds
                if(imgURLs.size() > 0)
                {
                    setImage(imgURLs.get(position), galleryImage, mAppend);
                    mSelectedImage = imgURLs.get(position);
                }


            }
        });


    }


    private void setImage(String imgURL, ImageView image, String append)
    {
        Log.d(TAG, "\tsetImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();

        // Notes: TODO - Need to init the ImageLoader
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        imageLoader.init(universalImageLoader.getConfig());

        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String imageUri, View view)
            {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });



    }



}
