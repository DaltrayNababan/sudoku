/*
 * Copyright (C) 2009 Roman Masek
 *
 * This file is part of OpenSudoku.
 *
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.gwnbs.sudoku.gui;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.colorpicker.ColorPickerDialog;
import com.gwnbs.sudoku.utils.Sound;
import com.gwnbs.sudoku.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Preference} that allows for setting and previewing a custom Sudoku Board theme.
 */
public class CustomThemeGroup extends PreferenceGroup implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SudokuBoardView mBoard;
    private Dialog mDialog;
    private Dialog mCopyFromExistingThemeDialog;
    private final SharedPreferences mGameSettings;
    public static final String[] colorKeys = {"custom_theme_colorPrimary", "custom_theme_colorPrimaryDark",
            "custom_theme_colorAccent", "custom_theme_colorButtonNormal", "custom_theme_lineColor",
            "custom_theme_sectorLineColor", "custom_theme_textColor", "custom_theme_textColorReadOnly",
            "custom_theme_textColorNote", "custom_theme_backgroundColor", "custom_theme_backgroundColorSecondary",
            "custom_theme_backgroundColorReadOnly", "custom_theme_backgroundColorTouched", "custom_theme_backgroundColorSelected",
            "custom_theme_backgroundColorHighlighted"};
    private RecyclerView rvColorPicker;
    private final List<ColorPickerModel> modelList = new ArrayList<>();

    public CustomThemeGroup(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.preferenceScreenStyle);
        mGameSettings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected boolean isOnSameScreenAsChildren() {
        return false;
    }

    @Override
    protected void onClick() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }

        showDialog();
    }

    @SuppressLint("InflateParams")
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.yes, null);
        View viewCustom = LayoutInflater.from(getContext()).inflate(R.layout.custom_theme_layout, null);
        prepareSudokuPreviewView(viewCustom);
        setCustomViewLayout(viewCustom);
        builder.setView(viewCustom);

        //mGameSettings.registerOnSharedPreferenceChangeListener(this);

        mDialog = builder.create();
        mDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        mDialog.setOnDismissListener((dialog) -> {
            mGameSettings.unregisterOnSharedPreferenceChangeListener(this);
            mDialog = null;
            commitLightThemeOrDarkThemeChanges();
            modelList.clear();
        });
        mDialog.show();
    }

    private void setCustomViewLayout(View viewCustom) {
        rvColorPicker = viewCustom.findViewById(R.id.rvColorPicker);
        rvColorPicker.setHasFixedSize(true);
        TextView textCopyExisting = viewCustom.findViewById(R.id.textCopyExisting);
        TextView textFromSingleColor = viewCustom.findViewById(R.id.textFromSingleColor);
        setList();
        textCopyExisting.setOnClickListener(v -> showCopyFromExistingThemeDialog());
        textFromSingleColor.setOnClickListener(v -> showCreateFromSingleColorDialog());
    }

    void setList() {
        modelList.clear();
        setupColorList(modelList);
        rvColorPicker.setAdapter(new ColorPickerAdapter(modelList));
    }

    void setupColorList(List<ColorPickerModel> modelList) {
        modelList.add(new ColorPickerModel(getStr(R.string.app_color_primary_title),
                getStr(R.string.app_color_primary_summary), getColorKey("custom_theme_colorPrimary", R.attr.colorPrimary)));
        modelList.add(new ColorPickerModel(getStr(R.string.app_color_secondary_title),
                getStr(R.string.app_color_secondary_summary), getColorKey("custom_theme_colorPrimaryDark", R.attr.colorPrimaryDark)));
        modelList.add(new ColorPickerModel(getStr(R.string.app_color_accent_title),
                getStr(R.string.app_color_accent_summary), getColorKey("custom_theme_colorAccent", R.attr.colorAccent)));
        modelList.add(new ColorPickerModel(getStr(R.string.app_color_button_title),
                getStr(R.string.app_color_button_summary), getColorKey("custom_theme_colorButtonNormal", R.attr.colorButtonNormal)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_lineColor),
                getStr(R.string.default_lineColor_summary), getColorKey("custom_theme_lineColor", R.attr.lineColor)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_sectorLineColor),
                getStr(R.string.default_sectorLineColor_summary), getColorKey("custom_theme_sectorLineColor", R.attr.sectorLineColor)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_textColor),
                getStr(R.string.default_textColor_summary), getColorKey("custom_theme_textColor", R.attr.textColor)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_textColorReadOnly),
                getStr(R.string.default_textColorReadOnly_summary), getColorKey("custom_theme_textColorReadOnly", R.attr.textColorReadOnly)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_textColorNote),
                getStr(R.string.default_textColorNote_summary), getColorKey("custom_theme_textColorNote", R.attr.textColorNote)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_backgroundColor),
                getStr(R.string.default_backgroundColor_summary), getColorKey("custom_theme_backgroundColor", R.attr.backgroundColor)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_backgroundColorSecondary),
                getStr(R.string.default_backgroundColorSecondary_summary), getColorKey("custom_theme_backgroundColorSecondary", R.attr.backgroundColorSecondary)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_backgroundColorReadOnly),
                getStr(R.string.default_backgroundColorReadOnly_summary), getColorKey("custom_theme_backgroundColorReadOnly", R.attr.backgroundColorReadOnly)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_backgroundColorTouched),
                getStr(R.string.default_backgroundColorTouched_summary), getColorKey("custom_theme_backgroundColorTouched", R.attr.backgroundColorTouched)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_backgroundColorSelected),
                getStr(R.string.default_backgroundColorSelected_summary), getColorKey("custom_theme_backgroundColorSelected", R.attr.backgroundColorSelected)));
        modelList.add(new ColorPickerModel(getStr(R.string.default_backgroundColorHighlighted),
                getStr(R.string.default_backgroundColorHighlighted_summary), getColorKey("custom_theme_backgroundColorHighlighted", R.attr.backgroundColorHighlighted)));
    }

    private void commitLightThemeOrDarkThemeChanges() {
        SharedPreferences.Editor settingsEditor = mGameSettings.edit();
        settingsEditor.putString("theme", "custom");
        settingsEditor.apply();
        ThemeUtils.sTimestampOfLastThemeUpdate = System.currentTimeMillis();
        callChangeListener(null);
    }

    private void showCopyFromExistingThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.select_theme);
        builder.setNegativeButton(android.R.string.cancel, null);

        String[] themeNames = getContext().getResources().getStringArray(R.array.theme_names);
        String[] themeNamesWithoutCustomTheme = Arrays.copyOfRange(themeNames, 0, themeNames.length - 1);
        builder.setItems(themeNamesWithoutCustomTheme, (dialog, which) -> {
            copyFromExistingThemeIndex(which);
            mCopyFromExistingThemeDialog.dismiss();
        });

        mCopyFromExistingThemeDialog = builder.create();
        mCopyFromExistingThemeDialog.setOnDismissListener((dialog) -> mCopyFromExistingThemeDialog = null);
        mCopyFromExistingThemeDialog.show();
    }

    private void copyFromExistingThemeIndex(int which) {
        String theme = getContext().getResources().getStringArray(R.array.theme_codes)[which];
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(getContext(),
                ThemeUtils.getThemeResourceIdFromString(theme));

        int[] attributes = {R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent,
                R.attr.colorButtonNormal, R.attr.lineColor, R.attr.sectorLineColor, R.attr.textColor,
                R.attr.textColorReadOnly, R.attr.textColorNote, R.attr.backgroundColor,
                R.attr.backgroundColorSecondary, R.attr.backgroundColorReadOnly, R.attr.backgroundColorTouched,
                R.attr.backgroundColorSelected, R.attr.backgroundColorHighlighted
        };

        TypedArray themeColors = themeWrapper.getTheme().obtainStyledAttributes(attributes);
        for (int i = 0; i < attributes.length; i++) {
            saveFromExistingTheme(colorKeys[i], themeColors, i);
            Sound.LOG("COLORS = " + mGameSettings.getInt(colorKeys[i], Color.GRAY));
        }
        updateThemePreview();
        setList();
    }

    void saveFromExistingTheme(String key, TypedArray typedArray, int i) {
        mGameSettings.edit().putInt(key, typedArray.getColor(i, Color.GRAY)).apply();
    }

    void saveFromSingleColor(String key, int color) {
        mGameSettings.edit().putInt(key, color).apply();
    }

    void colorPickerDialog(String key, int defaultColor, String title, View view) {
        ColorPickerDialog colorDialog = new ColorPickerDialog(getContext(),
                getColorKey(key, defaultColor), title);
        colorDialog.setAlphaSliderVisible(false);
        colorDialog.setHexValueEnabled(false);
        colorDialog.setOnColorChangedListener(color -> {
            mGameSettings.edit().putInt(key, color).apply();
            view.setBackgroundColor(color);
            updateThemePreview();
        });
        colorDialog.show();
    }

    private void showCreateFromSingleColorDialog() {
        ColorPickerDialog colorDialog = new ColorPickerDialog(getContext(),
                mGameSettings.getInt("custom_theme_colorPrimary", Color.WHITE),
                getStr(R.string.create_from_single_color));
        colorDialog.setAlphaSliderVisible(false);
        colorDialog.setHexValueEnabled(false);
        colorDialog.setOnColorChangedListener(this::createCustomThemeFromSingleColor);
        colorDialog.show();
    }

    private void createCustomThemeFromSingleColor(int colorPrimary) {
        double whiteContrast = ColorUtils.calculateContrast(colorPrimary, Color.WHITE);
        double blackContrast = ColorUtils.calculateContrast(colorPrimary, Color.BLACK);
        boolean isLightTheme = whiteContrast < blackContrast;

        float[] colorAsHSL = new float[3];
        ColorUtils.colorToHSL(colorPrimary, colorAsHSL);

        float[] tempHSL = colorAsHSL.clone();
        tempHSL[0] = (colorAsHSL[0] + 180f) % 360.0f;
        int colorAccent = ColorUtils.HSLToColor(tempHSL);

        tempHSL = colorAsHSL.clone();
        tempHSL[2] += isLightTheme ? -0.1f : 0.1f;
        int colorPrimaryDark = ColorUtils.HSLToColor(tempHSL);

        int textColor = isLightTheme ? Color.BLACK : Color.WHITE;
        int backgroundColor = isLightTheme ? Color.WHITE : Color.BLACK;

        saveFromSingleColor(colorKeys[0], colorPrimary);
        saveFromSingleColor(colorKeys[1], colorPrimaryDark);
        saveFromSingleColor(colorKeys[2], colorAccent);
        saveFromSingleColor(colorKeys[3], isLightTheme ? Color.LTGRAY : Color.DKGRAY);
        saveFromSingleColor(colorKeys[4], colorPrimaryDark);
        saveFromSingleColor(colorKeys[5], colorPrimaryDark);
        saveFromSingleColor(colorKeys[6], textColor);
        saveFromSingleColor(colorKeys[7], textColor);
        saveFromSingleColor(colorKeys[8], textColor);
        saveFromSingleColor(colorKeys[9], backgroundColor);
        saveFromSingleColor(colorKeys[10], backgroundColor);
        saveFromSingleColor(colorKeys[11], ColorUtils.setAlphaComponent(colorPrimaryDark, 64));
        saveFromSingleColor(colorKeys[12], colorAccent);
        saveFromSingleColor(colorKeys[13], colorPrimaryDark);
        saveFromSingleColor(colorKeys[14], colorPrimary);
        for (int i = 0; i < 15; i++) {
            Sound.LOG("SINGLE COLORS = " +
                    mGameSettings.getInt(colorKeys[i], Color.GRAY));
        }
        updateThemePreview();
        setList();
    }

    int getColorKey(String key, int defColor) {
        return mGameSettings.getInt(key, defColor);
    }

    public void onActivityDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (mCopyFromExistingThemeDialog != null && mCopyFromExistingThemeDialog.isShowing()) {
            mCopyFromExistingThemeDialog.dismiss();
        }
    }

    private void prepareSudokuPreviewView(View view) {
        mBoard = view.findViewById(R.id.sudoku_board);
        mBoard.setOnCellSelectedListener((cell) -> {
            if (cell != null) {
                mBoard.setHighlightedValue(cell.getValue());
            } else {
                mBoard.setHighlightedValue(0);
            }
        });
        ThemeUtils.prepareSudokuPreviewView(mBoard);
        updateThemePreview();
    }

    private void updateThemePreview() {
        String themeName = mGameSettings.getString("theme", "opensudoku");
        ThemeUtils.applyThemeToSudokuBoardViewFromContext(themeName, mBoard, getContext());
    }

    private void quantizeCustomAppColorPreferences() {
        SharedPreferences.Editor settingsEditor = mGameSettings.edit();
        settingsEditor.putInt(colorKeys[0], ThemeUtils.findClosestMaterialColor(mGameSettings.getInt(colorKeys[0], Color.GRAY)));
        settingsEditor.putInt(colorKeys[1], ThemeUtils.findClosestMaterialColor(mGameSettings.getInt(colorKeys[1], Color.GRAY)));
        settingsEditor.putInt(colorKeys[2], ThemeUtils.findClosestMaterialColor(mGameSettings.getInt(colorKeys[2], Color.WHITE)));
        settingsEditor.putInt(colorKeys[3], ThemeUtils.findClosestMaterialColor(mGameSettings.getInt(colorKeys[3], Color.GRAY)));
        settingsEditor.apply();
        ThemeUtils.sTimestampOfLastThemeUpdate = System.currentTimeMillis();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.contains("custom_theme_color")) {
            quantizeCustomAppColorPreferences();
        }
        updateThemePreview();
    }

    String getStr(int r) {
        return getContext().getString(r);
    }

    class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ColorPickerHolder> {

        private final List<ColorPickerModel> colorPickerModels;

        public ColorPickerAdapter(List<ColorPickerModel> colorPickerModels) {
            this.colorPickerModels = colorPickerModels;
        }

        @NonNull
        @Override
        public ColorPickerAdapter.ColorPickerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ColorPickerHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.color_picker_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ColorPickerAdapter.ColorPickerHolder holder, int position) {
            holder.bindColorList(colorPickerModels.get(position));
        }

        @Override
        public int getItemCount() {
            return colorPickerModels.size();
        }

        class ColorPickerHolder extends RecyclerView.ViewHolder{

            private final TextView textAppColor, textAppSumary;
            private final View viewAppColor;

            public ColorPickerHolder(@NonNull View itemView) {
                super(itemView);

                textAppColor = itemView.findViewById(R.id.textAppColor);
                textAppSumary = itemView.findViewById(R.id.textAppColorSummary);
                viewAppColor = itemView.findViewById(R.id.viewAppColor);
            }

            void bindColorList(ColorPickerModel colorPickerModel) {
                textAppColor.setText(colorPickerModel.getAppColorStr());
                textAppSumary.setText(colorPickerModel.getAppColorSummary());
                viewAppColor.setBackgroundColor(colorPickerModel.getColor());

                itemView.setOnClickListener(v -> {
                    colorPickerDialog(colorKeys[getAdapterPosition()],
                            colorPickerModel.getColor(), colorPickerModel.getAppColorStr(), viewAppColor);
                    Sound.LOG("COLOR KEY = " + colorKeys[getAdapterPosition()]);
                });
            }
        }
    }

    static class ColorPickerModel {

        private final String appColorStr;
        private final String appColorSummary;
        private final int color;

        public ColorPickerModel(String appColorStr, String appColorSummary, int color) {
            this.appColorStr = appColorStr;
            this.appColorSummary = appColorSummary;
            this.color = color;
        }

        public String getAppColorStr() {
            return appColorStr;
        }

        public String getAppColorSummary() {
            return appColorSummary;
        }

        public int getColor() {
            return color;
        }
    }
}
