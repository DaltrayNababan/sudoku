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
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.adapters.SudokuListAdapter;
import com.gwnbs.sudoku.db.SudokuColumns;
import com.gwnbs.sudoku.db.SudokuDatabase;
import com.gwnbs.sudoku.game.FolderInfo;
import com.gwnbs.sudoku.game.SudokuGame;
import com.gwnbs.sudoku.utils.DialogMaker;
import com.gwnbs.sudoku.utils.Sound;
import com.gwnbs.sudoku.utils.Val;

import java.util.ArrayList;
import java.util.List;

/**
 * List of puzzles in folder.
 *
 * @author romario
 */
public class SudokuListActivity extends ThemedActivity {

    public static final String EXTRA_FOLDER_ID = "folder_id";

    public static final int MENU_ITEM_INSERT = Menu.FIRST;
    public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    public static final int MENU_ITEM_PLAY = Menu.FIRST + 3;
    public static final int MENU_ITEM_RESET = Menu.FIRST + 4;
    public static final int MENU_ITEM_RESET_ALL = Menu.FIRST + 5;
    public static final int MENU_ITEM_EDIT_NOTE = Menu.FIRST + 6;
    public static final int MENU_ITEM_FILTER = Menu.FIRST + 7;
    public static final int MENU_ITEM_SORT = Menu.FIRST + 8;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 9;
    //public static final int MENU_ITEM_FOLDERS = Menu.FIRST + 9;
    /*private static final int DIALOG_DELETE_PUZZLE = 0;
    private static final int DIALOG_RESET_PUZZLE = 1;
    private static final int DIALOG_RESET_ALL = 2;
    private static final int DIALOG_EDIT_NOTE = 3;
    private static final int DIALOG_FILTER = 4;
    private static final int DIALOG_SORT = 5; */
    private static final String FILTER_STATE_NOT_STARTED = "filter" + SudokuGame.GAME_STATE_NOT_STARTED;
    private static final String FILTER_STATE_PLAYING = "filter" + SudokuGame.GAME_STATE_PLAYING;
    private static final String FILTER_STATE_SOLVED = "filter" + SudokuGame.GAME_STATE_COMPLETED;
    private static final String SORT_TYPE = "sort_type";
    private static final String TAG = "SudokuListActivity";
    private long mFolderID;
    public static int LAST_POSITION = 0;

    // input parameters for dialogs
    public static long mDeletePuzzleID;
    public static long mResetPuzzleID;
    public static long mEditNotePuzzleID;
    //private TextView mEditNoteInput;
    private SudokuListFilter mListFilter;
    private SudokuListSorter mListSorter;
    private TextView textSudokuList, mFilterStatus;
    private FolderDetailLoader mFolderDetailLoader;
    private RecyclerView mRV;
    private SharedPreferences settings;
    private final List<SudokuListAdapter.SudokuModel> sudokuModels = new ArrayList<>();
    private static final String[] columnIndexes = new String[]{SudokuColumns.DATA, SudokuColumns.STATE,
            SudokuColumns.TIME, SudokuColumns.LAST_PLAYED, SudokuColumns.CREATED, SudokuColumns.PUZZLE_NOTE};
    private DialogMaker dialogMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sudoku_list);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dialogMaker = new DialogMaker(this);
        mFilterStatus = findViewById(R.id.filter_status);
        textSudokuList = findViewById(R.id.textSudokuList);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        mFolderDetailLoader = new FolderDetailLoader(getApplicationContext());
        mRV = findViewById(R.id.list);
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FOLDER_ID)) {
            mFolderID = intent.getLongExtra(EXTRA_FOLDER_ID, 0);
        } else {
            Log.d(TAG, "No 'folder_id' extra provided, exiting.");
            finish();
            return;
        }

        mListFilter = new SudokuListFilter(getApplicationContext());
        mListFilter.showStateNotStarted = settings.getBoolean(FILTER_STATE_NOT_STARTED, true);
        mListFilter.showStatePlaying = settings.getBoolean(FILTER_STATE_PLAYING, true);
        mListFilter.showStateCompleted = settings.getBoolean(FILTER_STATE_SOLVED, true);
        mListSorter = new SudokuListSorter(getApplicationContext());
        mListSorter.setSortType(settings.getInt(SORT_TYPE, SudokuListSorter.SORT_BY_CREATED));

        updateList();

        findViewById(R.id.imageMoreOnSudokuList).setOnClickListener(v -> {
            Sound.soundClick(SudokuListActivity.this);
            setSudokuListActionMenu(v);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFolderDetailLoader.destroy();
        LAST_POSITION = 0;
        dialogMaker.destroyDialog();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mDeletePuzzleID", mDeletePuzzleID);
        outState.putLong("mResetPuzzleID", mResetPuzzleID);
        outState.putLong("mEditNotePuzzleID", mEditNotePuzzleID);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mDeletePuzzleID = state.getLong("mDeletePuzzleID");
        mResetPuzzleID = state.getLong("mResetPuzzleID");
        mEditNotePuzzleID = state.getLong("mEditNotePuzzleID");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // the puzzle list is naturally refreshed when the window
        // regains focus, so we only need to update the title
        updateTitle();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateList();
        if (!settings.getString("theme", "sudoku").equals(TitleScreenActivity.THEME)
                || !settings.getString(Val.ORIENTATION, "portrait").equals(TitleScreenActivity.ORIENTATION))
            recreate();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // if there is no activity in history and back button was pressed, go
        // to FolderListActivity, which is the root activity.
        if (isTaskRoot() && keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent();
            i.setClass(this, FolderListActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void setSudokuListActionMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        Menu menu = popupMenu.getMenu();
        menu.add(0, MENU_ITEM_INSERT, 0, R.string.add_sudoku).setShortcut('0', 'a')
                .setIcon(R.drawable.ic_add);
        menu.add(0, MENU_ITEM_FILTER, 1, R.string.filter).setShortcut('1', 'f')
                .setIcon(R.drawable.ic_view);
        menu.add(0, MENU_ITEM_SORT, 1, R.string.sort).setShortcut('1', 'o')
                .setIcon(R.drawable.ic_sort);
        menu.add(0, MENU_ITEM_RESET_ALL, 2, R.string.reset_all_puzzles).setShortcut('2', 'r')
                .setIcon(R.drawable.ic_undo);
        menu.add(0, MENU_ITEM_SETTINGS, 3, R.string.settings).setShortcut('3', 's')
                .setIcon(R.drawable.ic_settings);
        // I'm not sure this one is ready for release
//		menu.add(0, MENU_ITEM_GENERATE, 3, R.string.generate_sudoku).setShortcut('4', 'g')
//		.setIcon(R.drawable.ic_add);

        // Generate any additional actions that can be performed on the
        // overall list. In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, SudokuListActivity.class), null,
                intent, 0, null);
        popupMenu.setOnMenuItemClickListener(item -> {
            Sound.soundClick(SudokuListActivity.this);
            Intent i;
            switch (item.getItemId()) {
                case MENU_ITEM_INSERT: {
                    // Launch activity to insert a new item
                    i = new Intent(SudokuListActivity.this, SudokuEditActivity.class);
                    i.setAction(Intent.ACTION_INSERT);
                    i.putExtra(SudokuEditActivity.EXTRA_FOLDER_ID, mFolderID);
                    startActivity(i);
                    return true;
                }
                case MENU_ITEM_SETTINGS:
                    i = new Intent(SudokuListActivity.this, GameSettingsActivity.class);
                    startActivity(i);
                    return true;
                case MENU_ITEM_FILTER:
                    setDialogFilter();
                    return true;
                case MENU_ITEM_SORT:
                    setDialogSort();
                    return true;
                case MENU_ITEM_RESET_ALL:
                    setDialogResetAll();
                    return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private final Runnable setDialogDeletePuzzle = new Runnable() {
        @Override
        public void run() {
            dialogMaker.dialog(true, R.drawable.ic_restore, "Puzzle", getString(R.string.delete_puzzle_confirm),
                    getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.VISIBLE, () -> {
                        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
                        long mostRecentId = settings.getLong(Val.MOST_RECENTLY, 0);
                        if (mDeletePuzzleID == mostRecentId) {
                            settings.edit().remove(Val.MOST_RECENTLY).apply();
                        }
                        mDatabase.deleteSudoku(mDeletePuzzleID);
                        mDatabase.close();
                        updateList();
                    }, () -> {
            });
        }
    };

    private final Runnable setDialogEditNote = new Runnable() {
        @Override
        public void run() {
            SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
            SudokuGame game = mDatabase.getSudoku(mEditNotePuzzleID);
            dialogMaker.dialogEdit(R.drawable.ic_edit_grey, getString(R.string.edit_note), game.getNote(), getString(R.string.save),
                    getString(R.string.no), () -> {
                        game.setNote(DialogMaker.EDIT_NAME);
                        mDatabase.updateSudoku(game);
                        mDatabase.close();
                        updateList();
                    }, () -> {
            }, getString(R.string.edit_note));
        }
    };

    private final Runnable setDialogResetPuzzle = new Runnable() {
        @Override
        public void run() {
            dialogMaker.dialog(true, R.drawable.ic_restore, "Puzzle", getString(R.string.reset_puzzle_confirm),
                    getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.VISIBLE, () -> {
                        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
                        SudokuGame game = mDatabase.getSudoku(mResetPuzzleID);
                        if (game != null) {
                            game.reset();
                            mDatabase.updateSudoku(game);
                            mDatabase.close();
                        }
                        updateList();
                    }, () -> {
            });
        }
    };

    private void setDialogFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(R.drawable.ic_view).setTitle(R.string.filter_by_gamestate)
                .setMultiChoiceItems(R.array.game_states, new boolean[]{
                        mListFilter.showStateNotStarted, mListFilter.showStatePlaying,
                        mListFilter.showStateCompleted,
                }, (dialog, whichButton, isChecked) -> {
                    switch (whichButton) {
                        case 0:
                            mListFilter.showStateNotStarted = isChecked;
                            break;
                        case 1:
                            mListFilter.showStatePlaying = isChecked;
                            break;
                        case 2:
                            mListFilter.showStateCompleted = isChecked;
                            break;
                    }
                })
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    settings.edit().putBoolean(FILTER_STATE_NOT_STARTED, mListFilter.showStateNotStarted)
                            .putBoolean(FILTER_STATE_PLAYING, mListFilter.showStatePlaying)
                            .putBoolean(FILTER_STATE_SOLVED, mListFilter.showStateCompleted)
                            .apply();
                    updateList();
                })
                .setOnDismissListener(dismissListener)
                .setNegativeButton(getString(R.string.no), (dialog, whichButton) -> {
                    // User clicked No, so do some stuff
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialog.show();
    }

    private void setDialogSort() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(R.drawable.ic_sort)
                .setTitle(R.string.sort_puzzles_by)
                .setSingleChoiceItems(R.array.game_sort, mListSorter.getSortType(),
                        (dialog, whichButton) -> mListSorter.setSortType(whichButton))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    settings.edit().putInt(SORT_TYPE, mListSorter.getSortType()).apply();
                    updateList();
                })
                .setOnDismissListener(dismissListener)
                .setNegativeButton(getString(R.string.no), (dialog, whichButton) -> {
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialog.show();
    }

    private void setDialogResetAll() {
        dialogMaker.dialog(true, R.drawable.ic_restore, getString(R.string.reset_all_puzzles),
                getString(R.string.reset_all_puzzles_confirm),
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.VISIBLE, () -> {
                    SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
                    List<SudokuGame> sudokuGames = mDatabase.getAllSudokuByFolder(mFolderID, mListSorter);
                    for (SudokuGame sudokuGame : sudokuGames) {
                        sudokuGame.reset();
                        mDatabase.updateSudoku(sudokuGame);
                    }
                    mDatabase.close();
                    updateList();
                }, () -> {
        });
    }

    DialogInterface.OnDismissListener dismissListener = dialog ->
            Sound.soundClick(SudokuListActivity.this);

    /**
     * Updates whole list.
     */
    private void updateList() {
        updateTitle();
        updateFilterStatus();
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        sudokuModels.clear();
        mRV.setAdapter(null);
        SudokuListAdapter sudokuListAdapter;
        Cursor cur = mDatabase.getSudokuList(mFolderID, mListFilter, mListSorter);
        cur.moveToFirst();
        if (cur.getCount() > 0) {
            while (!cur.isAfterLast()) {
                SudokuListAdapter.SudokuModel sudokuModel = new SudokuListAdapter.SudokuModel();
                sudokuModel.setSudokuID(cur.getInt(cur.getColumnIndex(SudokuColumns._ID)));
                sudokuModel.setData(cur.getString(cur.getColumnIndex(columnIndexes[0])));
                sudokuModel.setState(cur.getInt(cur.getColumnIndex(columnIndexes[1])));
                sudokuModel.setTime(cur.getLong(cur.getColumnIndex(columnIndexes[2])));
                sudokuModel.setLastPlayed(cur.getLong(cur.getColumnIndex(columnIndexes[3])));
                sudokuModel.setCreated(cur.getLong(cur.getColumnIndex(columnIndexes[4])));
                sudokuModel.setNote(cur.getString(cur.getColumnIndex(columnIndexes[5])));
                sudokuModels.add(sudokuModel);
                cur.moveToNext();
            }
            sudokuListAdapter = new SudokuListAdapter(sudokuModels,
                    this, cur, setDialogDeletePuzzle, setDialogEditNote, setDialogResetPuzzle);
            mRV.setAdapter(sudokuListAdapter);
        }
        cur.close();
        mDatabase.close();
        //mRV.smoothScrollToPosition(LAST_POSITION);
        mRV.scrollToPosition(LAST_POSITION);
    }

    private void updateFilterStatus() {
        if (mListFilter.showStateCompleted && mListFilter.showStateNotStarted && mListFilter.showStatePlaying) {
            mFilterStatus.setVisibility(View.GONE);
        } else {
            mFilterStatus.setText(getString(R.string.filter_active, mListFilter));
            mFilterStatus.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTitle() {
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        FolderInfo folder = mDatabase.getFolderInfo(mFolderID);
        //setTitle(folder.name);
        textSudokuList.setText(folder.name);
        mFolderDetailLoader.loadDetailAsync(mFolderID, folderInfo -> {
            if (folderInfo != null)
                textSudokuList.setText(HtmlCompat.fromHtml(folderInfo.name + " - " + folderInfo.getDetail(getApplicationContext()),
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
        });
        mDatabase.close();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LAST_POSITION = 0;
    }
}
