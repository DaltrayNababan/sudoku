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
 * Modified by Daltray Nababan. Deprecated class and method of PreferenceActivity are changed
 */

package com.gwnbs.sudoku.gui;

import android.os.Bundle;
import android.view.View;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.utils.AndroidUtils;
import com.gwnbs.sudoku.utils.DialogMaker;
import com.gwnbs.sudoku.utils.Sound;
import com.gwnbs.sudoku.utils.ThemeUtils;
import com.gwnbs.sudoku.utils.Val;

import java.util.Objects;
//Modified by Daltray Nababan, June 2021. https://gwnbs.com

public class GameSettingsActivity extends ThemedActivity {

    private long mTimestampWhenApplyingTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTimestampWhenApplyingTheme = System.currentTimeMillis();
        super.onCreate(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new GameSettingFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ThemeUtils.sTimestampOfLastThemeUpdate > mTimestampWhenApplyingTheme) {
            recreate();
        }
    }

    public static class GameSettingFragment extends PreferenceFragmentCompat {

        public GameSettingFragment() {}

        private CheckBoxPreference mHighlightSimilarNotesPreference;
        private DialogMaker dialogMaker;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.game_settings, rootKey);

            dialogMaker = new DialogMaker(requireContext());

            mHighlightSimilarNotesPreference = findPreference("highlight_similar_notes");
            CheckBoxPreference highlightSimilarCellsPreference = findPreference("highlight_similar_cells");
            assert highlightSimilarCellsPreference != null;
            highlightSimilarCellsPreference.setOnPreferenceChangeListener(mHighlightSimilarCellsChanged);
            mHighlightSimilarNotesPreference.setEnabled(highlightSimilarCellsPreference.isChecked());

            ((ListPreference) Objects.requireNonNull(findPreference("theme")))
                    .setOnPreferenceChangeListener(((preference, newValue) -> {
                        requireActivity().recreate();
                        return true;
                    }));

            ((ListPreference) Objects.requireNonNull(findPreference(Val.ORIENTATION)))
                    .setOnPreferenceChangeListener(((preference, newValue) -> {
                        requireActivity().recreate();
                        return true;
                    }));

            ((Preference) Objects.requireNonNull(findPreference("show_hints")))
                    .setOnPreferenceChangeListener(mShowHintsChanged);

            ((Preference) Objects.requireNonNull(findPreference("appInfo")))
                    .setOnPreferenceClickListener(preference -> {
                aboutDialog();
                return true;
            });
        }

        private final Preference.OnPreferenceChangeListener mShowHintsChanged = (preference, newValue) -> {
            boolean newVal = (Boolean) newValue;
            HintsQueue hm = new HintsQueue(getContext());
            if (newVal) {
                hm.resetOneTimeHints();
            }
            return true;
        };

        private final Preference.OnPreferenceChangeListener mHighlightSimilarCellsChanged = (preference, newValue) -> {
            mHighlightSimilarNotesPreference.setEnabled((Boolean) newValue);
            return true;
        };

        private void aboutDialog() {
            Sound.soundClick(requireContext());
            String about = getString(R.string.send_me_bugs) + "<br/><br/>" + getString(R.string.sound_effect)
                    + " https://www.zapsplat.com" + "<br/><br/>" + getString(R.string.homepage) +
                    "<br/>opensudoku@moire.org" + "<br/><br/><b><u>" + getString(R.string.current_developers_label)
                    + "</u></b><br/>" + getString(R.string.current_developers) + "<br/><br/><b><u>" + getString(R.string.contributors_label)
                    + "</u></b><br/>" + getString(R.string.contributors) + "<br/><br/><b><u>" + getString(R.string.old_developers_label)
                    + "</u></b><br/>" + getString(R.string.old_developers) + "<br/><br/><b><u>" + getString(R.string.localization_contributors_label)
                    + "</u></b><br/>" + getString(R.string.localization_contributors);
            String versionName = AndroidUtils.getAppVersionName(requireContext());
            dialogMaker.dialog(true, R.drawable.ic_pastime, getString(R.string.app_name) + " " + versionName, about, getString(R.string.yes), getString(R.string.no),
                    View.VISIBLE, View.GONE, () -> { }, () -> { });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            dialogMaker.destroyDialog();
        }
    }
}
