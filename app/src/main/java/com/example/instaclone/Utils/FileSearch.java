package com.example.instaclone.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Notes: This class is for searching a directory and getting a List that's everything inside a directory
 */
public class FileSearch {


    /**
     * Notes: Searches a directory and return a list of all directories contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);


        File[] listfiles = file.listFiles();
        for(int i = 0; i < listfiles.length; i++)
        {
            if(listfiles[i].isDirectory())
            {
                // Notes: Creating a list of directories that's inside a directory
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }

        // Notes: Return a list of directories that are inside a directory
        return pathArray;
    }

    /**
     * Notes: Searches a directory and return a list of all files contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);


        File[] listfiles = file.listFiles();
        for(int i = 0; i < listfiles.length; i++)
        {
            if(listfiles[i].isFile())
            {
                // Notes: Creating a list of files that's inside a directory
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }

        // Notes: Return a list of files that are inside a directory
        return pathArray;
    }


}
