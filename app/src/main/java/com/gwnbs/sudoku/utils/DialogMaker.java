package com.gwnbs.sudoku.utils;
/*
    Created by Daltray Nababan, June 2021
    https://gwnbs.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

import com.gwnbs.sudoku.R;

public class DialogMaker {

    private final Context context;
    public static String EDIT_NAME = "";
    public AlertDialog dialog;
    public AlertDialog dialogEdit;
    private final SharedPreferences prefs;

    public DialogMaker(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void dialog(boolean cancelable, int image, String title, String message, String positiveStr, String negativeStr,
                       int positiveVisibility, int negativeVisibility, Runnable runPositive, Runnable runNegative) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(cancelable);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
        TextView textTitle = view.findViewById(R.id.textTitle);
        TextView textMessage = view.findViewById(R.id.textMessage);
        TextView textPositive = view.findViewById(R.id.textPositive);
        TextView textNegative = view.findViewById(R.id.textNegative);
        ImageView imageIcon = view.findViewById(R.id.imageIcon);
        imageIcon.setImageResource(image);
        textTitle.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));
        textMessage.setText(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY));
        textPositive.setVisibility(positiveVisibility);
        textNegative.setVisibility(negativeVisibility);
        textPositive.setText(positiveStr);
        textNegative.setText(negativeStr);
        builder.setView(view);
        dialog = builder.create();
        if (dialog.getWindow() !=null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        textNegative.setOnClickListener(v -> {
            dialog.dismiss();
            runNegative.run();
        });
        textPositive.setOnClickListener(v -> {
            dialog.dismiss();
            runPositive.run();
        });
        dialog.setOnDismissListener(dialog1 -> Sound.soundClick(context));
        dialog.show();
    }

    public void dialogEdit(int image, String title, String editText, String positiveStr, String negativeStr,
                           Runnable runPositive, Runnable runNegative, String hint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_folder, null);
        TextView textTitle = view.findViewById(R.id.textTitle);
        EditText editName = view.findViewById(R.id.name);
        editName.setHint(hint);
        editName.setText(editText);
        TextView textPositive = view.findViewById(R.id.textPositive);
        TextView textNegative = view.findViewById(R.id.textNegative);
        ImageView imageIcon = view.findViewById(R.id.imageIcon);
        imageIcon.setImageResource(image);
        textTitle.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));
        textPositive.setText(positiveStr);
        textNegative.setText(negativeStr);
        builder.setView(view);
        dialogEdit = builder.create();
        if (dialogEdit.getWindow() !=null)
            dialogEdit.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        textNegative.setOnClickListener(v -> {
            dialogEdit.dismiss();
            runNegative.run();
        });
        textPositive.setOnClickListener(v -> {
            dialogEdit.dismiss();
            EDIT_NAME = editName.getText().toString().trim();
            runPositive.run();
        });
        dialogEdit.setOnDismissListener(dialog1 -> {
            EDIT_NAME = "";
            Sound.soundClick(context);
        });
        dialogEdit.show();
    }

    public void dialogTheme(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View viewTheme = LayoutInflater.from(context).inflate(R.layout.dialog_theme, null);
        RadioButton radioThemeSudoku = viewTheme.findViewById(R.id.radioThemeSudoku);
        RadioButton radioThemeOrange = viewTheme.findViewById(R.id.radioThemeOrange);
        RadioButton radioThemeForest = viewTheme.findViewById(R.id.radioThemeForest);
        RadioButton radioThemeDark = viewTheme.findViewById(R.id.radioThemeDark);
        RadioButton radioThemeLight = viewTheme.findViewById(R.id.radioThemeLight);
        String theme = prefs.getString("theme", "sudoku");
        switch (theme) {
            case "sudoku" :
                radioThemeSudoku.setChecked(true);
                break;
            case "orange" :
                radioThemeOrange.setChecked(true);
                break;
            case "forest" :
                radioThemeForest.setChecked(true);
                break;
            case "dark" :
                radioThemeDark.setChecked(true);
                break;
            case "light" :
                radioThemeLight.setChecked(true);
                break;
        }
        builder.setView(viewTheme);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() !=null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        ((RadioGroup) viewTheme.findViewById(R.id.themeGroup)).setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == radioThemeSudoku.getId())
                applyNewTheme("sudoku");
            else if (checkedId == radioThemeOrange.getId())
                applyNewTheme("orange");
            else if (checkedId == radioThemeForest.getId())
                applyNewTheme("forest");
            else if (checkedId == radioThemeDark.getId())
                applyNewTheme("dark");
            else applyNewTheme("light");
        });
        viewTheme.findViewById(R.id.textApply).setOnClickListener(v -> {
            Sound.soundClick(activity);
            dialog.dismiss();
            if (!prefs.getString("theme", "sudoku").equals(theme))
                activity.recreate();
        });
        dialog.show();
    }

    void applyNewTheme(String themeName) {
        prefs.edit().putString("theme", themeName).apply();
    }

    public void destroyDialog() {
        if (dialog !=null)
            dialog.dismiss();
        if (dialogEdit !=null)
            dialogEdit.dismiss();
    }
}
