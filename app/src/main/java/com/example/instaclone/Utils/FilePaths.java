package com.example.instaclone.Utils;

import android.os.Environment;

public class FilePaths {

    // Notes: storage/emulated/0
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/Camera";

    // Notes: Location of where photos will be stored on Firebase Storage
    public String FIREBASE_IMAGE_STORAGE = "photos/users/";




}
