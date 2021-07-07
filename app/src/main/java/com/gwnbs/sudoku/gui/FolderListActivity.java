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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.adapters.FolderListAdapter;
import com.gwnbs.sudoku.db.SudokuDatabase;
import com.gwnbs.sudoku.game.FolderInfo;
import com.gwnbs.sudoku.utils.DialogMaker;
import com.gwnbs.sudoku.utils.Sound;
import com.gwnbs.sudoku.utils.ThemeUtils;
import com.gwnbs.sudoku.utils.Val;

import java.util.ArrayList;
import java.util.List;

/**
 * List of puzzle's folder. This activity also serves as root activity of application.
 *
 * @author romario
 */
//Modified by Daltray Nababan, June 2021. https://gwnbs.com
public class FolderListActivity extends ThemedActivity {

    public static final int MENU_ITEM_ADD = Menu.FIRST;
    public static final int MENU_ITEM_RENAME = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    //public static final int MENU_ITEM_ABOUT = Menu.FIRST + 3;
    public static final int MENU_ITEM_EXPORT = Menu.FIRST + 4;
    public static final int MENU_ITEM_EXPORT_ALL = Menu.FIRST + 5;
    public static final int MENU_ITEM_IMPORT = Menu.FIRST + 6;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 7;
    public static final int MENU_ITEM_GET_MORE_PUZZLE = Menu.FIRST + 8;

    private static final int OPEN_FILE = 1;
    private RecyclerView mRV;
    private Menu mMenu;
    private final List<FolderListAdapter.FolderModel> folderModels = new ArrayList<>();

    // input parameters for dialogs
    //private TextView mAddFolderNameInput;
    //private TextView mRenameFolderNameInput;
    public static long mRenameFolderID;
    public static long mDeleteFolderID;
    private FolderListAdapter folderListAdapter;
    private DialogMaker dialogMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder_list);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        dialogMaker = new DialogMaker(this);
        ImageView imageMore = findViewById(R.id.imageMoreOnFolderList);
        imageMore.setColorFilter(ThemeUtils.getTintColor(this));
        imageMore.setOnClickListener(v -> {
            Sound.soundClick(FolderListActivity.this);
            setMoreAction(v);
        });

        mRV = findViewById(R.id.list);
        getSudokuFolderList();
        registerForContextMenu(mRV);
    }

    private void getSudokuFolderList() {
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        Cursor cursor = mDatabase.getFolderList();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {
                FolderListAdapter.FolderModel folderModel = new FolderListAdapter.FolderModel();
                folderModel.setId(cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
                folderModel.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                folderModel.setDetail(cursor.getString(cursor.getColumnIndexOrThrow("created")));
                folderModels.add(folderModel);
                cursor.moveToNext();
            }
            folderListAdapter = new FolderListAdapter(folderModels, this,
                    setDialogDeleteFolder(), setDialogRenameFolder());
            mRV.setAdapter(folderListAdapter);
        }
        cursor.close();
        mDatabase.close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateList();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (!settings.getString("theme", "sudoku").equals(TitleScreenActivity.THEME)
                || !settings.getString(Val.ORIENTATION, "portrait").equals(TitleScreenActivity.ORIENTATION))
            recreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        folderListAdapter.destroyLoader();
        dialogMaker.destroyDialog();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mRenameFolderID", mRenameFolderID);
        outState.putLong("mDeleteFolderID", mDeleteFolderID);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mRenameFolderID = state.getLong("mRenameFolderID");
        mDeleteFolderID = state.getLong("mDeleteFolderID");
    }

    private void setMoreAction(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        Menu menu = popupMenu.getMenu();
        menu.add(0, MENU_ITEM_ADD, 0, R.string.add_folder)
                .setShortcut('3', 'a')
                .setIcon(R.drawable.ic_add);
        menu.add(0, MENU_ITEM_IMPORT, 0, R.string.import_file)
                .setShortcut('8', 'i')
                .setIcon(R.drawable.ic_cloud_download);
        menu.add(0, MENU_ITEM_EXPORT_ALL, 1, R.string.export_all_folders)
                .setShortcut('7', 'e')
                .setIcon(R.drawable.ic_share);
        menu.add(0, MENU_ITEM_SETTINGS, 2, R.string.settings)
                .setShortcut('6', 's')
                .setIcon(R.drawable.ic_settings);
        menu.add(0, MENU_ITEM_GET_MORE_PUZZLE, 3, R.string.get_more_puzzles_online)
                .setShortcut('9', 'g')
                .setIcon(R.drawable.ic_list_logo);

        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, FolderListActivity.class), null, intent, 0, null);
        mMenu = menu;
        popupMenu.setOnMenuItemClickListener(item -> {
            Sound.soundClick(FolderListActivity.this);
            Intent intent1;
            switch (item.getItemId()) {
                case MENU_ITEM_ADD:
                    setDialogAddFolder();
                    return true;
                case MENU_ITEM_IMPORT:
                    intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent1.addCategory(Intent.CATEGORY_OPENABLE);
                    intent1.setType("*/*");
                    resultLauncher.launch(intent1);
                    return true;
                case MENU_ITEM_EXPORT_ALL:
                    startActivity(new Intent(this, SudokuExportActivity.class)
                            .putExtra(SudokuExportActivity.EXTRA_FOLDER_ID, SudokuExportActivity.ALL_FOLDERS));
                    return true;
                case MENU_ITEM_SETTINGS:
                    startActivity(new Intent(this, GameSettingsActivity.class));
                    return true;
                case MENU_ITEM_GET_MORE_PUZZLE:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://opensudoku.moire.org/"))
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    return true;
            }
            return false;
        });
        popupMenu.show();
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        startActivity(new Intent(this, SudokuImportActivity.class)
                                .setData(data.getData()));
                    }
                }
            });

    private void setDialogAddFolder() {
        dialogMaker.dialogEdit(R.drawable.ic_add, getString(R.string.add_folder), "", getString(R.string.save),
                getString(R.string.no), () -> {
                    SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
                    mDatabase.insertFolder(DialogMaker.EDIT_NAME, System.currentTimeMillis());
                    mDatabase.close();
                    updateList();
                }, () -> { }, getString(R.string.folder_name));
    }

    private Runnable setDialogRenameFolder() {
        return () -> {
            SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
            FolderInfo folder = mDatabase.getFolderInfo(mRenameFolderID);
            mDatabase.close();
            String folderName = folder != null ? folder.name : "";
            dialogMaker.dialogEdit(R.drawable.ic_edit_grey, getString(R.string.rename_folder_title) + " " + folderName,
                    folderName, getString(R.string.save), getString(R.string.no), () -> {
                        SudokuDatabase db = new SudokuDatabase(getApplicationContext());
                        db.updateFolder(mRenameFolderID, DialogMaker.EDIT_NAME);
                        db.close();
                        updateList();
                    }, () -> { }, getString(R.string.folder_name));
        };
    }

    private Runnable setDialogDeleteFolder() {
        return () -> {
            SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
            FolderInfo folder = mDatabase.getFolderInfo(mDeleteFolderID);
            mDatabase.close();
            String folderName = folder != null ? folder.name : "";
            dialogMaker.dialog(true, R.drawable.ic_delete, getString(R.string.delete_folder_title) + " " + folderName,
                    getString(R.string.delete_folder_confirm), getString(R.string.yes), getString(R.string.no),
                    View.VISIBLE, View.VISIBLE, () -> {
                        SudokuDatabase db = new SudokuDatabase(getApplicationContext());
                        db.deleteFolder(mDeleteFolderID);
                        db.close();
                        updateList();
                    }, () -> {
            });
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_FILE && resultCode == RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                Intent i = new Intent(this, SudokuImportActivity.class);
                i.setData(uri);
                startActivity(i);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int STORAGE_PERMISSION_CODE = 1;
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onOptionsItemSelected(mMenu.findItem(MENU_ITEM_IMPORT));
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateList() {
        folderModels.clear();
        mRV.setAdapter(null);
        folderListAdapter = null;
        getSudokuFolderList();
    }
}
