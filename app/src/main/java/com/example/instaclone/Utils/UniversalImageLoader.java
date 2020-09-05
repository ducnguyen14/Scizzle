package com.example.instaclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.instaclone.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class UniversalImageLoader {
    private static final int defaultImage = R.drawable.ic_profile;
    private Context mContext;

    public UniversalImageLoader(Context context)
    {
        mContext= context;
    }

    /**
     * Notes: The way the universal image loader works is that it has an instance and the instance can be accessed anywhere on the application.
     *      We set a bunch of settings one time and it can be used app-wide
     * @return
     */
    public ImageLoaderConfiguration getConfig()
    {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImage)
                // Notes: Default image shows if original image is unable to load
                .showImageForEmptyUri(defaultImage)
                // Notes: Default image shows if db image retrieval failed or on null input
                .showImageOnFail(defaultImage)
                // Notes: If the image is rotated, it will be rotated back to its original orientation
                .considerExifParams(true)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();


        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();


        return configuration;

    }

    /**
     * Notes: This method is for setting a single image on a layout.
     *      This method can be used to set images that are static.
     *      It can't be used if the images are being changed in the Fragment/Activity - OR if they are being set in a
     *      list or gridview!!!
     */
    public static void setimage(String imgURL, ImageView image, final ProgressBar mProgressBar, String append)
    {
        // Notes: Format = append + imgURL

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String imageUri, View view)
            {
                // Notes: We want progress bar visible when loading starts
                if(mProgressBar != null)
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason)
            {
                if(mProgressBar != null)
                {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
            {
                if(mProgressBar != null)
                {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view)
            {
                if(mProgressBar != null)
                {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

    }




}
