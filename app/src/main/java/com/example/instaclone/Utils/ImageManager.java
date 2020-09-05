package com.example.instaclone.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageManager {
    private static final String TAG = "ImageManager/DEBUG";

    public static Bitmap getBitmap(String imgUrl)
    {
        // Notes: Creating a file that reference the image in memory
        File imageFile = new File(imgUrl);

        FileInputStream fis = null;
        Bitmap bitmap = null;

        try
        {
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        }
        catch(FileNotFoundException e)
        {
            Log.e(TAG, "getBitmap: FileNotFoundException: " + e.getMessage());
        }
        finally
        {
            try
            {
                fis.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "getBitmap: IOException: " + e.getMessage());
            }
        }

        return bitmap;
    }

    /**
     * Notes: This method converts Bitmap to ByteArray, we have the choice to choose the quality from 0-100%
     * @param bm
     * @param quality
     * @return
     */
    public static byte[] getBytesFromBitmap(Bitmap bm, int quality)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }



}
