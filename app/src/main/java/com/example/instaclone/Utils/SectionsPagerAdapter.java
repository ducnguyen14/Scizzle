package com.example.instaclone.Utils;

import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *  Notes: This class stores fragments for tabs so that
 *     layout_center_viewpager.xml can to swap out fragments for its tabs
 */


public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "SectionsPagerAdapter/DEBUG";

    private final List<Fragment> mFragmentList = new ArrayList<>();

    public SectionsPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }


    public void addFragment(Fragment fragment)
    {
        mFragmentList.add(fragment);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}
