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
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

public class LevelEXP {

    private final SharedPreferences prefs;
    private int requireEXP;
    private String levelIcon = "";
    public String levelTitle = "";

    public LevelEXP(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getLevel() {
        return prefs.getInt(Val.LEVEL, 0);
    }

    public void setEXP(int exp) {
        prefs.edit().putInt(Val.EXPERIENCE, getEXP() + exp).apply();
    }

    public int getEXP() {
        return prefs.getInt(Val.EXPERIENCE, 0);
    }

    public int getRequireEXP() {
        return requireEXP;
    }

    public void setRequireEXP(int requireEXP) {
        this.requireEXP = requireEXP;
    }

    public int getMaxLevel() {
        return 60;
    }

    @SuppressLint("SetTextI18n")
    public void setLevel(TextView tvLevel) {
        if (getLevel() < getMaxLevel()) {
            setAllRequireNeeded();
            if (getEXP() == getRequireEXP() || getEXP() >= getRequireEXP()) {
                prefs.edit().putInt(Val.LEVEL, getLevel() + 1).apply();
                prefs.edit().putInt(Val.EXPERIENCE, getEXP() - getRequireEXP()).apply();
                Sound.LOG("REMAIN = " + (getEXP() - getRequireEXP()));
            }
        }
        tvLevel.setText("Lv. " + getLevel());
        Sound.LOG("CURRENT EXP = " + getEXP());
        Sound.LOG("REQUIRE EXP = " + getRequireEXP());
        Sound.LOG("LEVEL = " + getLevel());
    }

    private void setAllRequireNeeded() {
        setRequireNeeded(0, 500);
        setRequireNeeded(1, 1500);
        setRequireNeeded(2,2000);
        setRequireNeeded(3,2000);
        setRequireNeeded(4,2000);
        setRequireNeeded(5,2500);
        setRequireNeeded(6,2500);
        setRequireNeeded(7,2500);
        setRequireNeeded(8,2500);
        setRequireNeeded(9,2500);
        setRequireNeeded(10,3000);
        setRequireNeeded(11,3000);
        setRequireNeeded(12,3000);
        setRequireNeeded(13,3000);
        setRequireNeeded(14,3000);
        setRequireNeeded(15,3500);
        setRequireNeeded(16,3500);
        setRequireNeeded(17,3500);
        setRequireNeeded(18,3500);
        setRequireNeeded(19,3500);
        setRequireNeeded(20,4000);
        setRequireNeeded(21,4000);
        setRequireNeeded(22,4000);
        setRequireNeeded(23,4000);
        setRequireNeeded(24,4000);
        setRequireNeeded(25,4500);
        setRequireNeeded(26,4500);
        setRequireNeeded(27,4500);
        setRequireNeeded(28,4500);
        setRequireNeeded(29,4500);
        setRequireNeeded(30,5000);
        setRequireNeeded(31,5000);
        setRequireNeeded(32,5000);
        setRequireNeeded(33,5000);
        setRequireNeeded(34,5000);
        setRequireNeeded(35,5500);
        setRequireNeeded(36,5500);
        setRequireNeeded(37,5500);
        setRequireNeeded(38,5500);
        setRequireNeeded(39,5500);
        setRequireNeeded(40,6000);
        setRequireNeeded(41,6000);
        setRequireNeeded(42,6000);
        setRequireNeeded(43,6000);
        setRequireNeeded(44,6000);
        setRequireNeeded(45,6500);
        setRequireNeeded(46,6500);
        setRequireNeeded(47,6500);
        setRequireNeeded(48,6500);
        setRequireNeeded(49,6500);
        setRequireNeeded(50,7000);
        setRequireNeeded(51,7000);
        setRequireNeeded(52,7000);
        setRequireNeeded(53,7000);
        setRequireNeeded(54,7000);
        setRequireNeeded(55,7500);
        setRequireNeeded(56,7500);
        setRequireNeeded(57,7500);
        setRequireNeeded(58,7500);
        setRequireNeeded(59,8000);
    }

    public void setRequireNeeded(int currentLevel, int require) {
        if (getLevel() == currentLevel) {
            setRequireEXP(require);
        }
    }

    public void setLevelLogo(TextView textLevelIcon) {
        switch (getLevel()) {
            case 0 :
                levelIcon = "BE"; levelTitle = "Beginner";
                break;
            case 1: case 2: case 3: case 4: case 5:
                levelIcon = "AP"; levelTitle = "Apprentice";
                break;
            case 6: case 7: case 8: case 9: case 10:
                levelIcon = "AV"; levelTitle = "Advanced";
                break;
            case 11: case 12: case 13: case 14: case 15:
                levelIcon = "EX"; levelTitle = "Experienced";
                break;
            case 16: case 17: case 18: case 19: case 20:
                levelIcon = "UD"; levelTitle = "Undoubted";
                break;
            case 21: case 22: case 23: case 24: case 25:
                levelIcon = "WR"; levelTitle = "Winner";
                break;
            case 26: case 27: case 28: case 29: case 30:
                levelIcon = "EL"; levelTitle = "Elite";
                break;
            case 31: case 32: case 33: case 34: case 35:
                levelIcon = "CP"; levelTitle = "Champion";
                break;
            case 36: case 37: case 38: case 39: case 40:
                levelIcon = "MA"; levelTitle = "Master";
                break;
            case 41: case 42: case 43: case 44: case 45:
                levelIcon = "GM"; levelTitle = "Grand Master";
                break;
            case 46: case 47: case 48: case 49: case 50:
                levelIcon = "SR"; levelTitle = "Super";
                break;
            case 51: case 52: case 53: case 54: case 55: case 56: case 57: case 58: case 59:
                levelIcon = "LG"; levelTitle = "Legend";
                break;
            case 60:
                levelIcon = "SE"; levelTitle = "Sudoku Elder";
                break;
        }
        textLevelIcon.setText(levelIcon);
    }

    public String getAllLevelTitle = "<b>BE</b> - Beginner (0)<br/><b>AP</b> - Apprentice (1-5)<br/><b>AV</b> - Advanced (6-10)<br/>" +
            "<b>EX</b> - Experienced (11-15)<br/><b>UD</b> - Undoubted (16-20)<br/><b>WR</b> - Winner (21-25)<br/><b>EL</b> - Elite (26-30)<br/>" +
            "<b>CP</b> - Champion (31-35)<br/><b>MA</b> - Master (36-40)<br/><b>GM</b> - Grandmaster (41-45)<br/><b>SR</b> - Super (46-50)<br/>" +
            "<b>LG</b> - Legend (51-59)<br/><b>SE</b> - Sudoku Elder (60)<br/>";

    public int getCurrentRequireEXP() {
        setAllRequireNeeded();
        return getRequireEXP() - getEXP();
    }
}
