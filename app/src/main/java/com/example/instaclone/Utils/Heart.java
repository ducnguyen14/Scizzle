package com.example.instaclone.Utils;

import android.util.Log;
import android.widget.ImageView;

/**
 * Notes: This Class will handle toggling of the heart icon
 */
public class Heart {
    private static final String TAG = "Heart/DEBUG";

    public ImageView heartWhite, heartRed;

    public Heart(ImageView heartWhite, ImageView heartRed) {
        this.heartWhite = heartWhite;
        this.heartRed = heartRed;
    }

    public void toggleLike()
    {
        Log.d(TAG, "\ttoggleLike: toggling heart");
    }


}
