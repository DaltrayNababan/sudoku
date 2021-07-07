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

package com.gwnbs.sudoku.gui.inputmethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.game.Cell;
import com.gwnbs.sudoku.game.CellCollection;
import com.gwnbs.sudoku.game.CellNote;
import com.gwnbs.sudoku.game.SudokuGame;
import com.gwnbs.sudoku.gui.HintsQueue;
import com.gwnbs.sudoku.gui.SudokuBoardView;
import com.gwnbs.sudoku.gui.SudokuPlayActivity;
import com.gwnbs.sudoku.utils.Sound;
import com.gwnbs.sudoku.utils.ThemeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents following type of number input workflow: Number buttons are displayed
 * in the sidebar, user selects one number and then fill values by tapping the cells.
 *
 * @author romario
 */
public class IMSingleNumber extends InputMethod {

    private static final int MODE_EDIT_VALUE = 0;
    private static final int MODE_EDIT_NOTE = 1;

    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = false;
    private boolean mBidirectionalSelection = true;
    private boolean mHighlightSimilar = true;

    private int mSelectedNumber = 1;
    private int mEditMode = MODE_EDIT_VALUE;

    private final Handler mGuiHandler;
    private Map<Integer, Button> mNumberButtons;
    private ImageButton mSwitchNumNoteButton;

    private SudokuPlayActivity.OnSelectedNumberChangedListener mOnSelectedNumberChangedListener = null;

    @SuppressLint("ClickableViewAccessibility")
    View.OnTouchListener mNumberButtonTouched = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Sound.soundClick(mContext);
            return false;
        }
        mSelectedNumber = (Integer) v.getTag();
        onSelectedNumberChanged();
        update();
        Sound.LOG("Single Number Buttons Touched");
        return true;
    };

    View.OnClickListener mNumberButtonClicked = v -> {
        mSelectedNumber = (Integer) v.getTag();
        onSelectedNumberChanged();
        update();
        Sound.LOG("Single Number Buttons Clicked");
    };

    private final CellCollection.OnChangeListener mOnCellsChangeListener = () -> {
        if (mActive) {
            update();
        }
    };

    public IMSingleNumber(Context context) {
        super();
        this.mContext = context;
        mGuiHandler = new Handler(Looper.getMainLooper());
    }

    public boolean getHighlightCompletedValues() {
        return mHighlightCompletedValues;
    }

    /**
     * If set to true, buttons for numbers, which occur in {@link CellCollection}
     * more than {@link CellCollection#SUDOKU_SIZE}-times, will be highlighted.
     *
     * @param highlightCompletedValues
     */
    @SuppressWarnings("JavaDoc")
    public void setHighlightCompletedValues(boolean highlightCompletedValues) {
        mHighlightCompletedValues = highlightCompletedValues;
    }

    public boolean getShowNumberTotals() {
        return mShowNumberTotals;
    }

    public void setShowNumberTotals(boolean showNumberTotals) {
        mShowNumberTotals = showNumberTotals;
    }

    public boolean getBidirectionalSelection() {
        return mBidirectionalSelection;
    }

    public void setBidirectionalSelection(boolean bidirectionalSelection) {
        mBidirectionalSelection = bidirectionalSelection;
    }

    public boolean getHighlightSimilar() {
        return mHighlightSimilar;
    }

    public void setHighlightSimilar(boolean highlightSimilar) {
        mHighlightSimilar = highlightSimilar;
    }

    public void setmOnSelectedNumberChangedListener(SudokuPlayActivity.OnSelectedNumberChangedListener l) {
        mOnSelectedNumberChangedListener = l;
    }

    @Override
    protected void initialize(Context context, IMControlPanel controlPanel,
                              SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
        super.initialize(context, controlPanel, game, board, hintsQueue);

        game.getCells().addOnChangeListener(mOnCellsChangeListener);
    }

    @Override
    public int getNameResID() {
        return R.string.single_number;
    }

    @Override
    public int getHelpResID() {
        return R.string.im_single_number_hint;
    }

    @Override
    public String getAbbrName() {
        return mContext.getString(R.string.single_number_abbr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected View createControlPanelView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View controlPanel = inflater.inflate(R.layout.im_single_number, null);

        mNumberButtons = new HashMap<>();
        mNumberButtons.put(1, controlPanel.findViewById(R.id.button_1));
        mNumberButtons.put(2, controlPanel.findViewById(R.id.button_2));
        mNumberButtons.put(3, controlPanel.findViewById(R.id.button_3));
        mNumberButtons.put(4, controlPanel.findViewById(R.id.button_4));
        mNumberButtons.put(5, controlPanel.findViewById(R.id.button_5));
        mNumberButtons.put(6, controlPanel.findViewById(R.id.button_6));
        mNumberButtons.put(7, controlPanel.findViewById(R.id.button_7));
        mNumberButtons.put(8, controlPanel.findViewById(R.id.button_8));
        mNumberButtons.put(9, controlPanel.findViewById(R.id.button_9));
        mNumberButtons.put(0, controlPanel.findViewById(R.id.button_clear));

        for (Integer num : mNumberButtons.keySet()) {
            Button b = mNumberButtons.get(num);
            assert b != null;
            b.setTag(num);
            b.setOnTouchListener(mNumberButtonTouched);
            b.setOnClickListener(mNumberButtonClicked);
        }

        mSwitchNumNoteButton = controlPanel.findViewById(R.id.switch_num_note);
        mSwitchNumNoteButton.setOnClickListener(v -> {
            Sound.soundClick(mContext);
            mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE : MODE_EDIT_VALUE;
            update();
        });

        return controlPanel;
    }

    @SuppressLint("SetTextI18n")
    private void update() {
        switch (mEditMode) {
            case MODE_EDIT_NOTE:
                mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_white);
                break;
            case MODE_EDIT_VALUE:
                mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_grey);
                break;
        }

        // TODO: sometimes I change background too early and button stays in pressed state
        // this is just ugly workaround
        mGuiHandler.postDelayed(() -> {
            for (Button b : mNumberButtons.values()) {
                if (b.getTag().equals(mSelectedNumber)) {
                    b.setTextAppearance(mContext, ThemeUtils.getCurrentThemeStyle(mContext, android.R.attr.textAppearanceLarge));
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
                    b.requestFocus();
                } else {
                    b.setTextAppearance(mContext, ThemeUtils.getCurrentThemeStyle(mContext, android.R.attr.textAppearanceButton));
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.DEFAULT);
                }
            }

            Map<Integer, Integer> valuesUseCount = null;
            if (mHighlightCompletedValues || mShowNumberTotals)
                valuesUseCount = mGame.getCells().getValuesUseCount();

            if (mHighlightCompletedValues) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
                    boolean selected = entry.getKey() == mSelectedNumber;
                    if (highlightValue && !selected) {
                        ThemeUtils.applyIMButtonStateToView(mNumberButtons.get(entry.getKey()), ThemeUtils.IMButtonStyle.ACCENT_HIGHCONTRAST);
                    }
                }
            }

            if (mShowNumberTotals) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    Button b = mNumberButtons.get(entry.getKey());
                    assert b != null;
                    if (!b.getTag().equals(mSelectedNumber))
                        b.setText(entry.getKey() + " (" + entry.getValue() + ")");
                    else
                        b.setText("" + entry.getKey());
                }
            }

            mBoard.setHighlightedValue(mBoard.isReadOnly() ? 0 : mSelectedNumber);
        }, 100);
    }

    @Override
    protected void onActivated() {
        update();
    }

    @Override
    protected void onCellSelected(Cell cell) {
        super.onCellSelected(cell);

        if (mBidirectionalSelection && cell != null) {
            int v = cell.getValue();
            if (v != 0 && v != mSelectedNumber) {
                mSelectedNumber = cell.getValue();
                update();
            }
        }

        mBoard.setHighlightedValue(mSelectedNumber);
    }

    private void onSelectedNumberChanged() {
        if (mBidirectionalSelection && mHighlightSimilar && mOnSelectedNumberChangedListener != null && !mBoard.isReadOnly()) {
            mOnSelectedNumberChangedListener.onSelectedNumberChanged(mSelectedNumber);
            mBoard.setHighlightedValue(mSelectedNumber);
        }
    }

    @Override
    protected void onCellTapped(Cell cell) {
        int selNumber = mSelectedNumber;

        switch (mEditMode) {
            case MODE_EDIT_NOTE:
                if (selNumber == 0) {
                    mGame.setCellNote(cell, CellNote.EMPTY);
                } else if (selNumber > 0 && selNumber <= 9) {
                    CellNote newNote = cell.getNote().toggleNumber(selNumber);
                    mGame.setCellNote(cell, newNote);
                    // if we toggled the note off we want to de-select the cell
                    if (!newNote.hasNumber(selNumber)) {
                        mBoard.clearCellSelection();
                    }
                }
                break;
            case MODE_EDIT_VALUE:
                if (selNumber >= 0 && selNumber <= 9) {
                    if (!Objects.requireNonNull(mNumberButtons.get(selNumber)).isEnabled()) {
                        // Number requested has been disabled but it is still selected. This means that
                        // this number can be no longer entered, however any of the existing fields
                        // with this number can be deleted by repeated touch
                        if (selNumber == cell.getValue()) {
                            mGame.setCellValue(cell, 0);
                            mBoard.clearCellSelection();
                        }
                    } else {
                        // Normal flow, just set the value (or clear it if it is repeated touch)
                        if (selNumber == cell.getValue()) {
                            selNumber = 0;
                            mBoard.clearCellSelection();
                        }
                        mGame.setCellValue(cell, selNumber);
                    }
                }
                break;
        }

    }

    @Override
    protected void onSaveState(IMControlPanelStatePersister.StateBundle outState) {
        outState.putInt("selectedNumber", mSelectedNumber);
        outState.putInt("editMode", mEditMode);
    }

    @Override
    protected void onRestoreState(IMControlPanelStatePersister.StateBundle savedState) {
        mSelectedNumber = savedState.getInt("selectedNumber", 1);
        mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
        if (isInputMethodViewCreated()) {
            update();
        }
    }

}
