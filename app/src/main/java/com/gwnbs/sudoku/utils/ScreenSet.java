package com.gwnbs.sudoku.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;

import androidx.preference.PreferenceManager;

public class ScreenSet {

    @SuppressLint("SourceLockedOrientationActivity")
    public static void requestOrientation(Activity act) {
        String orientation = PreferenceManager.getDefaultSharedPreferences(act)
                .getString(Val.ORIENTATION, "portrait");
        if (orientation.equals("landscape")) {
            act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
