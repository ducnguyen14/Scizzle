package com.example.instaclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
//import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.instaclone.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class GridImageAdapter extends ArrayAdapter<String>{

    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    /*
        Notes: Will use the UniversalImageLoader inside this class, so we will need
            to have the file extension we will be extending.

     */
    private String mAppend;
    private ArrayList<String> imgUrls;


    public GridImageAdapter(Context mContext, int layoutResource, String mAppend, ArrayList<String> imgUrls) {
        // Notes: Need to use super because we will be referencing items from the ArrayList<String> imgUrls.
        super(mContext, layoutResource, imgUrls);
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = mContext;
        this.layoutResource = layoutResource;
        this.mAppend = mAppend;
        this.imgUrls = imgUrls;
    }

    /**
     * Notes: Using the ViewHolder build pattern to view the images.
     *      The ViewHolder doesn’t load the all the widgets at once. Widgets are loaded into memory.
     *      It only loads a few of them to make the app faster (similar to RecyclerView)
     */
    private static class ViewHolder
    {
        SquareImageView image;
        ProgressBar mProgressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Notes: ViewHolder build pattern (Similar to RecyclerView)
        final ViewHolder holder;

        if(convertView == null)
        {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.gridImageViewProgressBar);
            holder.image = (SquareImageView) convertView.findViewById(R.id.gridImageView);

            /*
                Notes: Tag is a way you can store widgets in memory.
                    ViewHolder creates all the widgets, and tag will hold entire ViewHolder.
                    We store the view in memory and not putting it on the page so it doesn't cause the app
                    to slow down.
             */
            convertView.setTag(holder);

        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        /*
            Notes: The reason why in the constructor we passed imgURLs to the super so that
            we can access it via getItem(int) or else getItem(int) will return null
         */
        String imgURL = getItem(position);


        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mAppend + imgURL, holder.image, new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String imageUri, View view)
            {
                // Notes: We want progress bar visible when loading starts
                if(holder.mProgressBar != null)
                {
                    holder.mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason)
            {
                if(holder.mProgressBar != null)
                {
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
            {
                if(holder.mProgressBar != null)
                {
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view)
            {
                if(holder.mProgressBar != null)
                {
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }
        });


        return convertView;
    }
}
