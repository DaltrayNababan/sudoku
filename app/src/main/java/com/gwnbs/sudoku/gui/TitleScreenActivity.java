package com.gwnbs.sudoku.gui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.adapters.SudokuListAdapter;
import com.gwnbs.sudoku.db.SudokuDatabase;
import com.gwnbs.sudoku.game.SudokuGame;
import com.gwnbs.sudoku.utils.DialogMaker;
import com.gwnbs.sudoku.utils.Sound;
import com.gwnbs.sudoku.utils.Val;

public class TitleScreenActivity extends ThemedActivity {

    private SharedPreferences gameSettings;
    private TextView mResumeButton;
    public static String THEME = "";
    public static String ORIENTATION = "";
    private DialogMaker dialogMaker;
    private static final String FIRST_USER = "FirstUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        gameSettings = PreferenceManager.getDefaultSharedPreferences(this);
        dialogMaker = new DialogMaker(this);

        if (gameSettings.getBoolean(FIRST_USER, true)) {
            try {
                initialize();
            } finally {
                gameSettings.edit().putBoolean(FIRST_USER, false).apply();
                dialogMaker.dialog(false, R.drawable.ic_pastime, getString(R.string.welcome),
                        getString(R.string.welcome_message), getString(R.string.yes),
                        getString(R.string.no), View.VISIBLE, View.GONE, () -> {
                        }, () -> {
                });
            }
        } else {
            initialize();
        }
    }

    private void initialize() {
        mResumeButton = findViewById(R.id.resume_button);
        TextView mSudokuListButton = findViewById(R.id.sudoku_lists_button);
        TextView mSettingsButton = findViewById(R.id.settings_button);

        setupResumeButton();

        mSudokuListButton.setOnClickListener((view) -> {
            click();
            startActivity(new Intent(this, FolderListActivity.class));
        });
        mSettingsButton.setOnClickListener((view) -> {
            click();
            startActivity(new Intent(this, GameSettingsActivity.class));
        });

        THEME = gameSettings.getString("theme", "sudoku");
        ORIENTATION = gameSettings.getString(Val.ORIENTATION, "portrait");
        boolean showSudokuFolderListOnStartup = gameSettings.getBoolean("show_sudoku_lists_on_startup", false);
        if (showSudokuFolderListOnStartup) {
            startActivity(new Intent(this, FolderListActivity.class));
        }
    }

    private boolean canResume(long mSudokuGameID) {
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        SudokuGame mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        if (mSudokuGame != null) {
            return mSudokuGame.getState() != SudokuGame.GAME_STATE_COMPLETED;
        }
        mDatabase.close();
        return false;
    }

    private void click() {
        Sound.soundClick(this);
    }

    private void setupResumeButton() {
        long id = gameSettings.getLong(Val.MOST_RECENTLY, 0);
        if (canResume(id)) {
            setNewExtra(this, id);
            mResumeButton.setVisibility(View.VISIBLE);
            mResumeButton.setOnClickListener((view) -> {
                click();
                Intent i = new Intent(TitleScreenActivity.this, SudokuPlayActivity.class);
                i.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, id);
                i.putExtra(SudokuListAdapter.SUDOKU_NAME, "Puzzle " + newID);
                i.putExtra(SudokuListAdapter.SUDOKU_FOLDER_NAME, folderName);
                startActivity(i);
            });
        } else {
            mResumeButton.setVisibility(View.GONE);
        }
    }

    public static long newID;
    public static String folderName = "";

    public static void setNewExtra(Context context, long id) {
        if (id > 0 && id < 301) {
            newID = id;
            folderName = context.getString(R.string.difficulty_easy);
        } else if (id > 300 && id < 601) {
            newID = id - 300;
            folderName = context.getString(R.string.difficulty_medium);
        } else if (id > 600 && id < 901) {
            newID = id - 600;
            folderName = context.getString(R.string.difficulty_hard);
        } else if (id > 900 && id < 1001) {
            newID = id - 900;
            folderName = context.getString(R.string.difficulty_very_hard);
        } else {
            newID = id;
            folderName = "Generated";
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setupResumeButton();
        if (!gameSettings.getString("theme", "sudoku").equals(THEME)
                || !gameSettings.getString(Val.ORIENTATION, "portrait").equals(ORIENTATION))
            recreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialogMaker.destroyDialog();
    }
}
