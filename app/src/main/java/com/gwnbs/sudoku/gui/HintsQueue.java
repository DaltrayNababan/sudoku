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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.gwnbs.sudoku.R;

import java.util.LinkedList;
import java.util.Queue;

public class HintsQueue {
    private static final String PREF_FILE_NAME = "hints";
    // TODO: should be persisted in activity's state
    private final Queue<Message> mMessages;
    private final AlertDialog mHintDialog;
    private final Context mContext;
    private final SharedPreferences mPrefs;
    private boolean mOneTimeHintsEnabled;
    private final TextView textTitle;
    private final TextView textMessage;

    public HintsQueue(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
        gameSettings.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (key.equals("show_hints")) {
                mOneTimeHintsEnabled = sharedPreferences.getBoolean("show_hints", true);
            }
        });
        mOneTimeHintsEnabled = gameSettings.getBoolean("show_hints", true);

        mHintDialog = new AlertDialog.Builder(context).create();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
        textTitle = view.findViewById(R.id.textTitle);
        textMessage = view.findViewById(R.id.textMessage);
        TextView textPositive = view.findViewById(R.id.textPositive);
        textPositive.setText(context.getString(R.string.close));
        view.findViewById(R.id.textNegative).setVisibility(View.GONE);
        ((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_info);
        textTitle.setText(context.getString(R.string.hint));
        textMessage.setText("");
        mHintDialog.setView(view);
        if (mHintDialog.getWindow() !=null)
            mHintDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        textPositive.setOnClickListener(v -> mHintDialog.dismiss());
        mHintDialog.setOnDismissListener(dialog -> processQueue());
        mMessages = new LinkedList<>();
    }

    private void addHint(Message hint) {
        synchronized (mMessages) {
            mMessages.add(hint);
        }
        synchronized (mHintDialog) {
            if (!mHintDialog.isShowing()) {
                processQueue();
            }
        }
    }

    private void processQueue() {
        Message hint;
        synchronized (mMessages) {
            hint = mMessages.poll();
        }
        if (hint != null) {
            showHintDialog(hint);
        }
    }

    private void showHintDialog(Message hint) {
        synchronized (mHintDialog) {
            textTitle.setText(mContext.getString(hint.titleResID));
            textMessage.setText(mContext.getText(hint.messageResID));
            mHintDialog.show();
        }
    }

    public void showHint(int titleResID, int messageResID) {
        Message hint = new Message();
        hint.titleResID = titleResID;
        hint.messageResID = messageResID;
        //hint.args = args;
        addHint(hint);
    }

    public void showOneTimeHint(String key, int titleResID, int messageResID) {
        if (mOneTimeHintsEnabled) {
            // FIXME: remove in future versions
            // Before 1.0.0, hintKey was created from messageResID. This ID has in 1.0.0 changed.
            // From 1.0.0, hintKey is based on key, to be backward compatible, check for old
            // hint keys.
            if (legacyHintsWereDisplayed()) {
                return;
            }

            String hintKey = "hint_" + key;
            if (!mPrefs.getBoolean(hintKey, false)) {
                showHint(titleResID, messageResID);
                Editor editor = mPrefs.edit();
                editor.putBoolean(hintKey, true);
                editor.apply();
            }
        }
    }

    public boolean legacyHintsWereDisplayed() {
        return mPrefs.getBoolean("hint_2131099727", false) &&
                mPrefs.getBoolean("hint_2131099730", false) &&
                mPrefs.getBoolean("hint_2131099726", false) &&
                mPrefs.getBoolean("hint_2131099729", false) &&
                mPrefs.getBoolean("hint_2131099728", false);
    }

    public void resetOneTimeHints() {
        Editor editor = mPrefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * This should be called when activity is paused.
     */
    public void pause() {
        // get rid of WindowLeakedException in logcat
        if (mHintDialog != null) {
            mHintDialog.cancel();
        }
    }

    private static class Message {
        int titleResID;
        int messageResID;
    }
}
