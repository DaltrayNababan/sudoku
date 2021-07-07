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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.adapters.SudokuListAdapter;
import com.gwnbs.sudoku.db.SudokuDatabase;
import com.gwnbs.sudoku.game.Cell;
import com.gwnbs.sudoku.game.SudokuGame;
import com.gwnbs.sudoku.gui.inputmethod.IMControlPanel;
import com.gwnbs.sudoku.gui.inputmethod.IMControlPanelStatePersister;
import com.gwnbs.sudoku.gui.inputmethod.IMNumpad;
import com.gwnbs.sudoku.gui.inputmethod.IMPopup;
import com.gwnbs.sudoku.gui.inputmethod.IMSingleNumber;
import com.gwnbs.sudoku.utils.DialogMaker;
import com.gwnbs.sudoku.utils.LevelEXP;
import com.gwnbs.sudoku.utils.Sound;
import com.gwnbs.sudoku.utils.ThemeUtils;
import com.gwnbs.sudoku.utils.Val;

//Modified by Daltray Nababan, June 2021. https://gwnbs.com
public class SudokuPlayActivity extends ThemedActivity {

    public static final String EXTRA_SUDOKU_ID = "sudoku_id";
    /*public static final int MENU_ITEM_RESTART = Menu.FIRST;
    public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 1;
    public static final int MENU_ITEM_FILL_IN_NOTES = Menu.FIRST + 2;
    public static final int MENU_ITEM_HELP = Menu.FIRST + 6;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 8;
    public static final int MENU_ITEM_SET_CHECKPOINT = Menu.FIRST + 9;
    public static final int MENU_ITEM_UNDO_TO_CHECKPOINT = Menu.FIRST + 10;
    public static final int MENU_ITEM_UNDO_TO_BEFORE_MISTAKE = Menu.FIRST + 11; */
    public static final int MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES = Menu.FIRST + 3;
    private SudokuGame mSudokuGame;
    private Handler mGuiHandler;
    private ViewGroup mRootLayout;
    private SudokuBoardView mSudokuBoard;
    private TextView mTimeLabel;
    //private Menu mOptionsMenu;
    private IMControlPanel mIMControlPanel;
    private IMControlPanelStatePersister mIMControlPanelStatePersister;
    private IMPopup mIMPopup;
    private IMSingleNumber mIMSingleNumber;
    private IMNumpad mIMNumpad;
    private boolean mShowTime = true;
    private GameTimer mGameTimer;
    private final GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
    private boolean mFullScreen;
    private boolean mFillInNotesEnabled = false;
    HintsQueue mHintsQueue;
    private SharedPreferences gameSettings;
    private TextView textHint, textPoint, textLevel, textLevelIcon;
    private ImageView imageUndo;
    private InterstitialAd interstitialAd;
    private static int puzzleScore = 100;
    private AdView adView;
    private LevelEXP levelEXP;
    private int currentLevel = 0;
    private String levelStr = "";
    private long NEXT_ID = 0;
    private DialogMaker dialogMaker;
    private int seconds = 0;
    private int visibility;
    private BottomSheetDialog bottomMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //strictMode();
        super.onCreate(savedInstanceState);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = displaymetrics.heightPixels;
        if ((width == 240 || width == 320) && (height == 240 || height == 320)) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mFullScreen = true;
        }
        setContentView(R.layout.sudoku_play);
        levelEXP = new LevelEXP(this);
        gameSettings = getPrefs();
        currentLevel = gameSettings.getInt(Val.LEVEL, 0);
        mRootLayout = findViewById(R.id.root_layout);
        mSudokuBoard = findViewById(R.id.sudoku_board);
        mTimeLabel = findViewById(R.id.time_label);
        mHintsQueue = new HintsQueue(this);
        mGuiHandler = new Handler(Looper.getMainLooper());
        ImageView imageMore = findViewById(R.id.imageMore);
        imageUndo = findViewById(R.id.imageUndo);
        textHint = findViewById(R.id.imageHint);
        textPoint = findViewById(R.id.textPoint);
        textLevel = findViewById(R.id.textLevel);
        textLevelIcon = findViewById(R.id.textLevelIcon);
        levelEXP.setLevel(textLevel);
        levelEXP.setLevelLogo(textLevelIcon);
        adView = findViewById(R.id.adView);
        dialogMaker = new DialogMaker(this);
        // create sudoku game instance
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        if (savedInstanceState == null) {
            // activity runs for the first time, read game from database
            long mSudokuGameID = getIntent().getLongExtra(EXTRA_SUDOKU_ID, 0);
            mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        } else {
            // activity has been running before, restore its state
            mSudokuGame = new SudokuGame();
            mSudokuGame.restoreState(savedInstanceState);
        }
        mDatabase.close();
        mGameTimer = new GameTimer(mShowTime, mTimeLabel, mGameTimeFormatter, mSudokuGame, textPoint);
        if (savedInstanceState !=null) {
            mGameTimer.restoreState(savedInstanceState);
        }
        // save our most recently played sudoku
        gameSettings.edit().putLong(Val.MOST_RECENTLY, mSudokuGame.getId()).apply();

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
            mSudokuGame.start();
        } else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();
        }

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
            mSudokuBoard.setReadOnly(true);
        }

        mSudokuBoard.setGame(mSudokuGame);
        mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);
        mHintsQueue.showOneTimeHint("welcome", R.string.welcome, R.string.first_run_hint);
        mIMControlPanel = findViewById(R.id.input_methods);
        mIMControlPanel.initialize(mSudokuBoard, mSudokuGame, mHintsQueue);
        mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);
        mIMPopup = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
        mIMSingleNumber = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
        mIMNumpad = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);

        Cell cell = mSudokuGame.getLastChangedCell();
        if (cell != null && !mSudokuBoard.isReadOnly())
            mSudokuBoard.moveCellSelectionTo(cell.getRowIndex(), cell.getColumnIndex());
        else
            mSudokuBoard.moveCellSelectionTo(0, 0);

        if (mSudokuGame.getState() != SudokuGame.GAME_STATE_PLAYING) {
            imageUndo.setEnabled(false);
            textHint.setEnabled(false);
        }

        imageUndo.setOnClickListener(v -> {
            if (mSudokuGame.hasSomethingToUndo()) {
                Sound.soundUndo(this);
                mSudokuGame.undo();
                selectLastChangedCell();
            } else {
                click();
                toastM(getApplicationContext(), getString(R.string.nothing_undo));
            }
        });

        updateHintValue();
        //setScore(mTimeLabel, textPoint);

        TextView textPuzzleName = findViewById(R.id.textPuzzleName);
        String puzzleName = getIntent().getStringExtra(SudokuListAdapter.SUDOKU_NAME);
        String folderName = getIntent().getStringExtra(SudokuListAdapter.SUDOKU_FOLDER_NAME);
        if (puzzleName !=null && folderName !=null) {
            textPuzzleName.setText(String.format("%s - %s", puzzleName, folderName));
        }
        textHint.setOnClickListener(v -> setDialogHint());
        imageMore.setOnClickListener(this::moreMenu);
        textPoint.setOnClickListener(v -> setDialogPointInfo());
        textLevel.setOnClickListener(v -> setDialogLevelInfo());
        textLevelIcon.setOnClickListener(v -> setDialogIconTitle());

        loadBannerAds();
        timers(10000, this::loadInterstitialAd, () -> { });
    }

    private void moreMenu(View v) {
        click();
        if (bottomMenu == null) {
            bottomMenu = new BottomSheetDialog(this);
            View viewMenu = getLayoutInflater().inflate(R.layout.bottom_more, findViewById(R.id.bottomMenuRoot));
            TextView textFillInNotes = viewMenu.findViewById(R.id.textFillInNotes);
            TextView textFillAllNotesWithValue = viewMenu.findViewById(R.id.textFillAllNotes);
            TextView textClearAllNotes = viewMenu.findViewById(R.id.textClearAllNotes);
            TextView textSetCheckPoint = viewMenu.findViewById(R.id.textSetCheckPoint);
            TextView textUndoCheckPoint = viewMenu.findViewById(R.id.textUndoCheckPoint);
            TextView textUndoBeforeMistake = viewMenu.findViewById(R.id.textUndoBeforeMistake);
            TextView textRestart = viewMenu.findViewById(R.id.textRestart);
            TextView textSettings = viewMenu.findViewById(R.id.textSettings);
            TextView textHelp = viewMenu.findViewById(R.id.textHelp);
            if (mFillInNotesEnabled)
                textFillInNotes.setVisibility(View.VISIBLE);
            if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
                textClearAllNotes.setEnabled(true);
                if (mFillInNotesEnabled) {
                    textFillInNotes.setEnabled(true);
                }
                textFillAllNotesWithValue.setEnabled(true);
                textUndoCheckPoint.setEnabled(mSudokuGame.hasUndoCheckpoint());
            } else {
                textClearAllNotes.setEnabled(false);
                if (mFillInNotesEnabled) {
                    textFillInNotes.setEnabled(false);
                }
                textFillAllNotesWithValue.setEnabled(false);
                textUndoCheckPoint.setEnabled(false);
                textUndoBeforeMistake.setEnabled(false);
            }
            textFillInNotes.setOnClickListener(onBottomViewClicked(() -> mSudokuGame.fillInNotes()));
            textFillAllNotesWithValue.setOnClickListener(onBottomViewClicked(() -> mSudokuGame.fillInNotesWithAllValues()));
            textClearAllNotes.setOnClickListener(onBottomViewClicked(this::setDialogClearNotes));
            textSetCheckPoint.setOnClickListener(onBottomViewClicked(() -> mSudokuGame.setUndoCheckpoint()));
            textUndoCheckPoint.setOnClickListener(onBottomViewClicked(this::setDialogUndoToCheckpoint));
            textUndoBeforeMistake.setOnClickListener(onBottomViewClicked(this::setDialogUndoToBeforeMistake));
            textRestart.setOnClickListener(onBottomViewClicked(this::setDialogRestart));
            textSettings.setOnClickListener(onBottomViewClicked(() -> {
                getPrefs().edit().putBoolean(Val.SETTINGS_LAUNCH, true).apply();
                startActivity(new Intent(SudokuPlayActivity.this, GameSettingsActivity.class));
            }));
            textHelp.setOnClickListener(onBottomViewClicked(() ->
                    dialogMaker.dialog(true, R.drawable.ic_info, getString(R.string.help), getString(R.string.help_text),
                    getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.GONE, () -> { }, () -> {
            })));
            viewMenu.findViewById(R.id.imageClose).setOnClickListener(v1 -> bottomMenu.dismiss());
            bottomMenu.setContentView(viewMenu);
            FrameLayout layout = bottomMenu.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (layout !=null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(layout);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setPeekHeight(getResources().getDisplayMetrics().heightPixels);
            }
        }
        bottomMenu.show();
    }

    private View.OnClickListener onBottomViewClicked(Runnable runnable) {
        return v -> {
            bottomMenu.dismiss();
            runnable.run();
        };
    }

    private void timers(long millis, Runnable runFinish, Runnable runUntilFinish) {
        new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                seconds = (int) millisUntilFinished / 1000;
                runUntilFinish.run();
            }
            @Override
            public void onFinish() {
                runFinish.run();
            }
        }.start();
    }

    //Occurs when puzzle is solved.
    private final SudokuGame.OnPuzzleSolvedListener onSolvedListener = new SudokuGame.OnPuzzleSolvedListener() {
        @Override
        public void onPuzzleSolved() {
            mGameTimer.stop();
            imageUndo.setEnabled(false);
            textHint.setEnabled(false);
            mSudokuBoard.setReadOnly(true);
            mIMControlPanel.setEnabled(false);
            int totalScore = gameSettings.getInt(Val.TOTAL_SCORE, 0);
            gameSettings.edit().putInt(Val.TOTAL_SCORE, totalScore + puzzleScore).apply();
            setLevel();
            if (levelEXP.getLevel() == currentLevel + 1)
                dialogLevelUp();
            else setDialogWellDone();
        }
    };

    private void dialogLevelUp() {
        Sound.soundSuccess(this);
        String message = "<b>" + getString(R.string.congratulations) + "</b>!" + " "
                + getString(R.string.you_are_now) + " <big><b>" + levelEXP.getLevel() + "</b></big>";
        dialogMaker.dialog(false, R.drawable.ic_celebration, "Level UP", message, getString(R.string.yes),
                getString(R.string.no), View.VISIBLE, View.GONE, this::setDialogWellDone, () -> {
        });
    }

    private void setLevel() {
        if (levelEXP.getLevel() < levelEXP.getMaxLevel()) {
            levelEXP.setEXP(puzzleScore * 10);
            levelEXP.setLevel(textLevel);
            levelEXP.setLevelLogo(textLevelIcon);
        }
    }

    private void showInterstitialAd() {
        interstitialAd.show(SudokuPlayActivity.this);
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                interstitialAd = null;
            }
            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                interstitialAd = null;
            }
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                setDialogAfterWellDone();
            }
        });
    }

    private void setNextID() {
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        NEXT_ID = getIntent().getLongExtra(EXTRA_SUDOKU_ID, 0 ) + 1;
        SudokuGame sudoku = mDatabase.getSudoku(NEXT_ID);
        visibility = sudoku != null ? View.VISIBLE : View.GONE;
        mDatabase.close();
    }

    private void getNextSudoku() {
        TitleScreenActivity.setNewExtra(this, NEXT_ID);
        Intent i = getIntent();
        i.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, NEXT_ID);
        i.putExtra(SudokuListAdapter.SUDOKU_NAME, "Puzzle " + TitleScreenActivity.newID);
        i.putExtra(SudokuListAdapter.SUDOKU_FOLDER_NAME, TitleScreenActivity.folderName);
        finish();
        startActivity(i);
    }

    private void setDialogAfterWellDone() {
        Sound.soundSuccess(this);
        setNextID();
        if (levelEXP.getLevel() != levelEXP.getMaxLevel())
            levelStr = "<br/><br/>+" + (puzzleScore * 10) + " XP<br/>" + levelEXP.getCurrentRequireEXP()
                + " " + getString(R.string.more_to_level) + " " + (levelEXP.getLevel() + 1);
        else levelStr = "";
        dialogMaker.dialog(false, R.drawable.ic_pastime, getString(R.string.well_done), getString(R.string.congrats, mGameTimeFormatter.format(mSudokuGame.getTime())
                        + " " + getString(R.string.by_score) + " " + puzzleScore + ". " + setScoreGrade() + levelStr),
                getString(R.string.continue_play), getString(R.string.close), visibility, View.VISIBLE, this::getNextSudoku, this::onBackPressed);
    }

    private final OnSelectedNumberChangedListener onSelectedNumberChangedListener = new OnSelectedNumberChangedListener() {
        @Override
        public void onSelectedNumberChanged(int number) {
            if (number != 0) {
                Cell cell = mSudokuGame.getCells().findFirstCell(number);
                mSudokuBoard.setHighlightedValue(number);
                if (cell != null) {
                    mSudokuBoard.moveCellSelectionTo(cell.getRowIndex(), cell.getColumnIndex());
                } else {
                    mSudokuBoard.clearCellSelection();
                }
            } else {
                mSudokuBoard.clearCellSelection();
            }
        }
    };

    private void click() {
        Sound.soundClick(this);
    }

    public void updateHintValue() {
        textHint.setText(String.valueOf(gameSettings.getInt(Val.HINT, 10)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // read game settings
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int screenPadding = gameSettings.getInt("screen_border_size", 0);
        mRootLayout.setPadding(screenPadding, screenPadding, screenPadding, screenPadding);
        mFillInNotesEnabled = gameSettings.getBoolean("fill_in_notes_enabled", false);

        String theme = gameSettings.getString("theme", "opensudoku");
        if (theme.equals("custom") || theme.equals("custom_light")) {
            ThemeUtils.applyCustomThemeToSudokuBoardViewFromContext(mSudokuBoard,
                    getApplicationContext());
        }

        mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean("highlight_wrong_values", true));
        mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean("highlight_touched_cell", true));

        boolean highlightSimilarCells = gameSettings.getBoolean("highlight_similar_cells", true);
        boolean highlightSimilarNotes = gameSettings.getBoolean("highlight_similar_notes", true);
        if (highlightSimilarCells) {
            mSudokuBoard.setHighlightSimilarCell(highlightSimilarNotes ?
                    SudokuBoardView.HighlightMode.NUMBERS_AND_NOTES :
                    SudokuBoardView.HighlightMode.NUMBERS);
        } else {
            mSudokuBoard.setHighlightSimilarCell(SudokuBoardView.HighlightMode.NONE);
        }

        mSudokuGame.setRemoveNotesOnEntry(gameSettings.getBoolean("remove_notes_on_input", false));
        mShowTime = gameSettings.getBoolean("show_time", true);
        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();
            mGameTimer.start();
        }
        mTimeLabel.setVisibility(mShowTime ? View.VISIBLE : View.INVISIBLE);
        mIMPopup.setEnabled(gameSettings.getBoolean("im_popup", true));
        mIMSingleNumber.setEnabled(gameSettings.getBoolean("im_single_number", true));
        mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));
        mIMNumpad.setMoveCellSelectionOnPress(gameSettings.getBoolean("im_numpad_move_right", false));
        mIMPopup.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMPopup.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        mIMSingleNumber.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMSingleNumber.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        mIMSingleNumber.setBidirectionalSelection(gameSettings.getBoolean("bidirectional_selection", true));
        mIMSingleNumber.setHighlightSimilar(gameSettings.getBoolean("highlight_similar", true));
        mIMSingleNumber.setmOnSelectedNumberChangedListener(onSelectedNumberChangedListener);
        mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMNumpad.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));

        mIMControlPanel.activateFirstInputMethod(); // make sure that some input method is activated
        mIMControlPanelStatePersister.restoreState(mIMControlPanel);

        if (!mSudokuBoard.isReadOnly()) {
            mSudokuBoard.invokeOnCellSelected();
        }
        updateTime();
        if (adView !=null)
            adView.resume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // empty space at the top of the screen). This is desperate workaround.
            if (mFullScreen) {
                mGuiHandler.postDelayed(() -> {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    mRootLayout.requestLayout();
                }, 1000);
            }
        }
    }

    private void saveGame() {
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        mDatabase.updateSudoku(mSudokuGame);
        mGameTimer.stop();
        mIMControlPanel.pause();
        mIMControlPanelStatePersister.saveState(mIMControlPanel);
        mDatabase.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // we will save game to the database as we might not be able to get back
        saveGame();
        if (adView !=null)
            adView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView !=null)
            adView.destroy();
        dialogMaker.destroyDialog();
        if (bottomMenu !=null)
            bottomMenu.dismiss();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mGameTimer.stop();
        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.pause();
        }
        mSudokuGame.saveState(outState);
        mGameTimer.saveState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (getPrefs().getBoolean(Val.SETTINGS_LAUNCH, false))
            restartActivity();
        getPrefs().edit().putBoolean(Val.SETTINGS_LAUNCH, false).apply();
        if (RewardActivity.rewardItem !=null) {
            Sound.soundReward(this);
            toastM(getApplicationContext(), getString(R.string.you_get_10));
            updateHintValue();
            RewardActivity.rewardItem = null;
        }
    }

    public SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void restartActivity() {
        recreate();
    }

    @SuppressLint("SetTextI18n")
    private void setDialogWellDone() {
        Sound.soundSuccess(SudokuPlayActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        builder.setCancelable(false);
        TextView textTitle = view.findViewById(R.id.textTitle);
        TextView textMessage = view.findViewById(R.id.textMessage);
        TextView textPositive = view.findViewById(R.id.textPositive);
        TextView textNegative = view.findViewById(R.id.textNegative);
        ImageView imageIcon = view.findViewById(R.id.imageIcon);
        imageIcon.setImageResource(R.drawable.ic_info);
        textTitle.setText(getString(R.string.well_done));
        builder.setView(view);
        AlertDialog dialogWellDone = builder.create();
        if (dialogWellDone.getWindow() !=null)
            dialogWellDone.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        String str = getString(R.string.congrats, mGameTimeFormatter.format(mSudokuGame.getTime()))
                + " " + getString(R.string.by_score) + " " + puzzleScore + ". " + setScoreGrade();
        if (levelEXP.getLevel() != levelEXP.getMaxLevel())
            levelStr = "\n\n+" + (puzzleScore * 10) + " XP\n" + levelEXP.getCurrentRequireEXP()
                    + " " + getString(R.string.more_to_level) + " " + (levelEXP.getLevel() + 1);
        else levelStr = "";
        str = str + levelStr;
        if (interstitialAd !=null) {
            textPositive.setVisibility(View.GONE);
            textNegative.setVisibility(View.GONE);
            textMessage.setText(str + getString(R.string.an_ad_will));
            String finalStr = str;
            timers(5500, () -> {
                showInterstitialAd();
                dialogWellDone.dismiss();
            }, () -> textMessage.setText(finalStr + getString(R.string.an_ad_will_appear)
                    + " " + seconds + " " + getString(R.string.seconds)));
        } else {
            setNextID();
            textPositive.setVisibility(visibility);
            textMessage.setText(str);
            textNegative.setText(getString(R.string.close));
            textPositive.setText(getString(R.string.continue_play));
            textPositive.setOnClickListener(v -> {
                click();
                dialogWellDone.dismiss();
                getNextSudoku();
            });
            textNegative.setOnClickListener(v -> {
                click();
                dialogWellDone.dismiss();
                onBackPressed();
            });
        }
        dialogWellDone.show();
    }

    private void setDialogRestart() {
        dialogMaker.dialog(true, R.drawable.ic_restore, getString(R.string.app_name), getString(R.string.restart_confirm),
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.VISIBLE, () -> {
                    mSudokuGame.reset();
                    mSudokuGame.start();
                    mSudokuBoard.setReadOnly(false);
                    mGameTimer.start();
                    imageUndo.setEnabled(true);
                    textHint.setEnabled(true);
                    mIMControlPanel.setEnabled(true);
                    setScore(mTimeLabel, textPoint);
                }, () -> {
        });
    }

    private void setDialogClearNotes() {
        dialogMaker.dialog(true, R.drawable.ic_delete, getString(R.string.app_name), getString(R.string.clear_all_notes_confirm),
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.VISIBLE, () -> mSudokuGame.clearAllNotes(), () -> {
        });
    }

    private void setDialogUndoToCheckpoint() {
        dialogMaker.dialog(true, R.drawable.ic_undo, getString(R.string.app_name), getString(R.string.undo_to_checkpoint_confirm),
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.VISIBLE, () -> {
                    mSudokuGame.undoToCheckpoint();
                    selectLastChangedCell();
                }, () -> {
        });
    }

    private void setDialogUndoToBeforeMistake() {
        dialogMaker.dialog(true, R.drawable.ic_undo, getString(R.string.app_name), getString(R.string.undo_to_before_mistake_confirm),
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.VISIBLE, () -> {
                    mSudokuGame.undoToBeforeMistake();
                    selectLastChangedCell();
                }, () -> {
        });
    }

    private void setDialogPuzzleNotSolved() {
        dialogMaker.dialog(true, 0, getString(R.string.app_name), getString(R.string.puzzle_not_solved),
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.GONE, () -> { }, () -> {
        });
    }

    private void setDialogHint() {
        if (gameSettings.getInt(Val.HINT, 10) > 0) {
            if (gameSettings.getBoolean(Val.SHOW_HINT_DIALOG, true)) {
                dialogMaker.dialog(true, R.drawable.ic_hint, getString(R.string.app_name), getString(R.string.hint_confirm),
                        getString(R.string.yes), getString(R.string.never_ask), View.VISIBLE, View.VISIBLE, this::displayHint, () -> {
                    displayHint();
                    gameSettings.edit().putBoolean(Val.SHOW_HINT_DIALOG, false).apply();
                });
            } else
                displayHint();
        } else setDialogNoMoreHint();
    }

    private void displayHint() {
        Cell cell = mSudokuBoard.getSelectedCell();
        if (cell != null && cell.isEditable()) {
            if (mSudokuGame.isSolvable()) {
                mSudokuGame.solveCell(cell, this, textHint);
            } else {
                setDialogPuzzleNotSolved();
            }
        } else {
            setDialogCannotGiveHint();
        }
    }

    private void setDialogNoMoreHint() {
        click();
        dialogMaker.dialog(true, R.drawable.ic_hint, getString(R.string.out_of_hint), getString(R.string.watch_video_for),
                getString(R.string.watch), getString(R.string.no), View.VISIBLE, View.VISIBLE, () ->
                        startActivity(new Intent(SudokuPlayActivity.this, RewardActivity.class)),
                () -> {
        });
    }

    private void setDialogIconTitle() {
        click();
        dialogMaker.dialog(true, R.drawable.ic_title, getString(R.string.sudoku_title), levelEXP.getAllLevelTitle,
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.GONE, () -> {}, () -> {
        });
    }

    private void setDialogLevelInfo() {
        click();
        levelEXP.setLevelLogo(textLevelIcon);
        String title = "Level " + levelEXP.getLevel() + " " + getString(R.string.of_) +  " 60";
        String message;
        if (levelEXP.getLevel() < 60)
            message = getString(R.string.you_are) + " <big><b>" + levelEXP.levelTitle + "</b></big><br/>" + levelEXP.getCurrentRequireEXP()
                    + " " + getString(R.string.more_to_level) + " " + (levelEXP.getLevel() + 1);
        else if (levelEXP.getLevel() == 60) {
            message = "<big><b>" + levelEXP.levelTitle + "</b></big>, " + getString(R.string.you_have_reached);
        } else message = "";
        dialogMaker.dialog(true, R.drawable.ic_level, title, message,
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.GONE, () -> {}, () -> {
        });
    }

    private void loadBannerAds() {
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                adView.setVisibility(View.GONE);
            }
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadInterstitialAd() {
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/8691691433",
                new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        super.onAdLoaded(interstitialAd);
                        interstitialAd = ad;
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        interstitialAd = null;
                    }
                });
    }

    private void setDialogPointInfo() {
        click();
        dialogMaker.dialog(true, R.drawable.ic_poin, getString(R.string.point), getString(R.string.point_info),
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.GONE, () -> { }, () -> {
        });
    }

    private void setDialogCannotGiveHint() {
        Sound.soundCannot(this);
        dialogMaker.dialog(true, R.drawable.ic_hint, getString(R.string.app_name), getString(R.string.cannot_give_hint),
                getString(R.string.yes), getString(R.string.no), View.VISIBLE, View.GONE, () -> { }, () -> {
        });
    }

    private void selectLastChangedCell() {
        Cell cell = mSudokuGame.getLastChangedCell();
        if (cell != null)
            mSudokuBoard.moveCellSelectionTo(cell.getRowIndex(), cell.getColumnIndex());
    }

    /**
     * Update the time of game-play.
     */
    private void updateTime() {
        if (mShowTime) {
            //setTitle(mGameTimeFormatter.format(mSudokuGame.getTime()));
            mTimeLabel.setVisibility(View.VISIBLE);
        } else {
            mTimeLabel.setVisibility(View.GONE);
            //mTimeLabel.setText(getString(R.string.app_name));
        }
        mTimeLabel.setText(mGameTimeFormatter.format(mSudokuGame.getTime()));
        setScore(mTimeLabel, textPoint);
    }

    private static void setScore(TextView mTimeLabel, TextView textPoint) {
        String timeLabel = mTimeLabel.getText().toString();
        if (timeLabel.length() > 5) {
            puzzleScore = 25;
            return;
        }
        int minute = Integer.parseInt(timeLabel.substring(0, 2));
        if (minute < 6)
            puzzleScore = 100;
        else {
            if (minute == 6) puzzleScore = 95;
            if (minute == 7) puzzleScore = 90;
            if (minute == 8) puzzleScore = 85;
            if (minute == 9) puzzleScore = 80;
            if (minute == 10) puzzleScore = 75;
            if (minute == 11) puzzleScore = 70;
            if (minute == 12) puzzleScore = 65;
            if (minute == 13) puzzleScore = 60;
            if (minute == 14) puzzleScore = 55;
            if (minute == 15) puzzleScore = 50;
            if (minute == 16) puzzleScore = 45;
            if (minute == 17) puzzleScore = 40;
            if (minute == 18) puzzleScore = 35;
            if (minute == 19) puzzleScore = 30;
            if (minute > 19) puzzleScore = 25;
        }
        textPoint.setText(String.valueOf(puzzleScore));
    }

    private String setScoreGrade() {
        String grade = "";
        int ps = puzzleScore;
        if (ps > 24 && ps < 51) {
            grade = getString(R.string.bad_enough);
        } else if (ps > 50 && ps < 61) {
            grade = getString(R.string.not_so_bad);
        } else if (ps > 60 && ps < 71) {
            grade = getString(R.string.good_enough);
        } else if (ps > 70 && ps < 81) {
            grade = getString(R.string.impressive);
        } else if (ps > 80 && ps < 100) {
            grade = getString(R.string.awesome);
        } else if (ps == 100) {
            grade = getString(R.string.perfect);
        }
        return grade;
    }

    public interface OnSelectedNumberChangedListener {
        void onSelectedNumberChanged(int number);
    }

    // This class implements the game clock.  All it does is update the
    // status each tick.
    private static class GameTimer extends Timer {

        private final boolean mShowTime;
        private final TextView mTimeLabel;
        private final GameTimeFormat gameTimeFormat;
        private final SudokuGame mSudokuGame;
        private final TextView textPoint;

        public GameTimer(boolean mShowTime, TextView mTimeLabel, GameTimeFormat gameTimeFormat,
                         SudokuGame mSudokuGame, TextView textPoint) {
            super(1000);
            this.mShowTime = mShowTime;
            this.mTimeLabel = mTimeLabel;
            this.gameTimeFormat = gameTimeFormat;
            this.mSudokuGame = mSudokuGame;
            this.textPoint = textPoint;
        }

        @Override
        protected boolean step(int count, long time) {
            //updateTime();
            if (mShowTime) {
                mTimeLabel.setVisibility(View.VISIBLE);
            } else {
                mTimeLabel.setVisibility(View.GONE);
            }
            mTimeLabel.setText(gameTimeFormat.format(mSudokuGame.getTime()));
            setScore(mTimeLabel, textPoint);
            // Run until explicitly stopped.
            return false;
        }
    }

    public static void toastM(Context c, String message) {
        Toast toast = Toast.makeText(c.getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
