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

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.gwnbs.sudoku.R;

public class Sound {

    public Sound() { }

    public static void soundReward(Context context) {
        if (getSound(context)) {
            MediaPlayer mpp = MediaPlayer.create(context, R.raw.reward_ok);
            mpp.start();
            mpp.setOnCompletionListener(mp -> {
                mp.reset();
                mp.release();
            });
        }
    }

    public static void soundClick(Context context) {
        if (getSound(context)) {
            MediaPlayer mpp = MediaPlayer.create(context, R.raw.tap);
            mpp.start();
            mpp.setOnCompletionListener(mp -> {
                mp.reset();
                mp.release();
            });
        }
    }

    public static void soundSuccess(Context context) {
        if (getSound(context)) {
            MediaPlayer mpp = MediaPlayer.create(context, R.raw.success);
            mpp.start();
            mpp.setOnCompletionListener(mp -> {
                mp.reset();
                mp.release();
            });
        }
    }

    public static void soundFilled(Context context) {
        if (getSound(context)) {
            MediaPlayer mpp = MediaPlayer.create(context, R.raw.show_number);
            mpp.start();
            mpp.setOnCompletionListener(mp -> {
                mp.reset();
                mp.release();
            });
        }
    }

    public static void soundUndo(Context context) {
        if (getSound(context)) {
            MediaPlayer mpp = MediaPlayer.create(context, R.raw.undo);
            mpp.start();
            mpp.setOnCompletionListener(mp -> {
                mp.reset();
                mp.release();
            });
        }
    }

    public static void soundCannot(Context context) {
        if (getSound(context)) {
            MediaPlayer mpp = MediaPlayer.create(context, R.raw.cannot);
            mpp.start();
            mpp.setOnCompletionListener(mp -> {
                mp.reset();
                mp.release();
            });
        }
    }

    private static boolean getSound(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Val.GAME_SOUND, true);
    }

    public static void LOG(String str) {
        Log.v("GWNBS_SUDOKU", str);
    }
}
