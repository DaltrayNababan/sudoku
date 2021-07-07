package com.gwnbs.sudoku.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.gwnbs.sudoku.utils.ScreenSet;
import com.gwnbs.sudoku.utils.ThemeUtils;

public class ThemedActivity extends AppCompatActivity {
    //private final int mThemeId = 0;
    //private long mTimestampWhenApplyingTheme = 0;
    private SharedPreferences prefs;
    private String value = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.setThemeFromPreferences(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
        //mTimestampWhenApplyingTheme = System.currentTimeMillis();
        ScreenSet.requestOrientation(this);
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        value = prefs.getString("theme", "opensudoku");
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        if (ThemeUtils.sTimestampOfLastThemeUpdate > mTimestampWhenApplyingTheme) {
            recreate();
        }
    } */

    @Override
    protected void onRestart() {
        super.onRestart();
        String newValue = prefs.getString("theme", "opensudoku");
        if (!newValue.equals(value))
            recreate();
    }
}
