package com.gwnbs.sudoku.adapters;
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
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.db.SudokuColumns;
import com.gwnbs.sudoku.game.CellCollection;
import com.gwnbs.sudoku.game.SudokuGame;
import com.gwnbs.sudoku.gui.GameTimeFormat;
import com.gwnbs.sudoku.gui.SudokuBoardView;
import com.gwnbs.sudoku.gui.SudokuEditActivity;
import com.gwnbs.sudoku.gui.SudokuListActivity;
import com.gwnbs.sudoku.gui.SudokuPlayActivity;
import com.gwnbs.sudoku.utils.Sound;
import com.gwnbs.sudoku.utils.ThemeUtils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class SudokuListAdapter extends RecyclerView.Adapter<SudokuListAdapter.FolderListHolder> {

    private final List<SudokuModel> sudokuModels;
    private final Context context;
    private final Cursor cursor;
    private final Runnable runDeletePuzzle, runEditPuzzle, runResetPuzzle;
    private final GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
    public static final String SUDOKU_NAME = "puzzle_name";
    public static final String SUDOKU_FOLDER_NAME = "puzzle_folder_name";
    public static String sudokuFolderName = "";

    public SudokuListAdapter(List<SudokuModel> sudokuModels, Context context, Cursor cursor,
                             Runnable runDeletePuzzle, Runnable runEditPuzzle, Runnable runResetPuzzle) {
        this.sudokuModels = sudokuModels;
        this.context = context;
        this.cursor = cursor;
        this.runDeletePuzzle = runDeletePuzzle;
        this.runEditPuzzle = runEditPuzzle;
        this.runResetPuzzle = runResetPuzzle;
    }

    @NonNull
    @Override
    public FolderListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FolderListHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sudoku_list_item, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull FolderListHolder holder, int position) {
        holder.bindSudokuList(sudokuModels.get(position));
    }

    @Override
    public int getItemCount() {
        return sudokuModels.size();
    }

    class FolderListHolder extends RecyclerView.ViewHolder {

        private final TextView puzzleNumber, state, time, lastPlayed, created, note;
        private final SudokuBoardView sudokuBoardView;

        public FolderListHolder(@NonNull View itemView) {
            super(itemView);

            puzzleNumber = itemView.findViewById(R.id.puzzleNumber);
            state = itemView.findViewById(R.id.state);
            time = itemView.findViewById(R.id.time);
            lastPlayed = itemView.findViewById(R.id.last_played);
            created = itemView.findViewById(R.id.created);
            note = itemView.findViewById(R.id.note);
            sudokuBoardView = itemView.findViewById(R.id.sudoku_board);
        }

        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        void bindSudokuList(SudokuModel sudokuModel) {
            int id = sudokuModel.getSudokuID(), newID;
            if (id > 0 && id < 301) newID = id;
            else if (id > 300 && id < 601) newID = id - 300;
            else if (id > 600 && id < 901) newID = id - 600;
            else if (id > 900 && id < 1001) newID = id - 900;
            else newID = getAdapterPosition() + 1;
            puzzleNumber.setText("Puzzle " + newID);
            String data = sudokuModel.getData();
            // TODO: still can be faster, I don't have to call initCollection and read notes
            CellCollection cells = null;
            try {
                cells = CellCollection.deserialize(data);
            } catch (Exception e) {
                long sudokuID = cursor.getLong(cursor.getColumnIndex(SudokuColumns._ID));
                Log.e("TAG", String.format("Exception occurred when deserializing puzzle with id %s.", sudokuID), e);
            }
            sudokuBoardView.setReadOnly(true);
            sudokuBoardView.setFocusable(false);
            sudokuBoardView.setCells(cells);
            ThemeUtils.applyThemeToSudokuBoardViewFromContext(ThemeUtils.getCurrentThemeFromPreferences(context),
                    sudokuBoardView, context);

            String stateString = null;
            int gameState = sudokuModel.getState();
            switch (gameState) {
                case SudokuGame.GAME_STATE_COMPLETED:
                    stateString = context.getString(R.string.solved);
                    break;
                case SudokuGame.GAME_STATE_PLAYING:
                    stateString = context.getString(R.string.playing);
                    break;
            }
            state.setVisibility(stateString == null ? View.GONE : View.VISIBLE);
            state.setText(stateString);

            long timeIndex = sudokuModel.getTime();
            String timeString = null;
            if (timeIndex != 0) {
                timeString = mGameTimeFormatter.format(timeIndex);
            }
            time.setVisibility(timeString == null ? View.GONE : View.VISIBLE);
            time.setText(timeString);

            long lastPlayedIndex = sudokuModel.getLastPlayed();
            String lastPlayedString = null;
            if (lastPlayedIndex != 0) {
                lastPlayedString = context.getString(R.string.last_played_at,
                        getDateAndTimeForHumans(lastPlayedIndex));
            }
            lastPlayed.setVisibility(lastPlayedString == null ? View.GONE : View.VISIBLE);
            lastPlayed.setText(lastPlayedString);

            long createdIndex = sudokuModel.getCreated();
            String createdString = null;
            if (createdIndex != 0) {
                createdString = context.getString(R.string.created_at,
                        getDateAndTimeForHumans(createdIndex));
            }
            // TODO: when GONE, note is not correctly aligned below last_played
            created.setVisibility(createdString == null ? View.GONE : View.VISIBLE);
            created.setText(createdString);

            String noteIndex = sudokuModel.getNote();
            if (noteIndex == null || noteIndex.trim().equals("")) {
                note.setVisibility(View.GONE);
            } else {
                note.setText(noteIndex);
            }
            note.setVisibility((noteIndex == null || noteIndex.trim().equals("")) ? View.GONE : View.VISIBLE);
            note.setText(noteIndex);

            itemView.setOnClickListener(v -> {
                Sound.soundClick(context);
                playSudoku(sudokuModel.getSudokuID(), getAdapterPosition(), puzzleNumber.getText().toString());
            });

            itemView.setOnLongClickListener(v -> {
                Sound.soundClick(context);
                createMenu(v, sudokuModel.getSudokuID(), getAdapterPosition(), puzzleNumber.getText().toString());
                return false;
            });
        }
    }

    private void playSudoku(long id, int position, String puzzleName) {
        SudokuListActivity.LAST_POSITION = position;
        Intent i = new Intent(context, SudokuPlayActivity.class);
        //todo cek itemID may not right when loaded gameplay
        i.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, id);
        i.putExtra(SUDOKU_NAME, puzzleName);
        i.putExtra(SUDOKU_FOLDER_NAME, sudokuFolderName);
        context.startActivity(i);
        Sound.LOG("Sudoku ID = " + id + "Sudoku Name = " + puzzleName);
    }

    private void createMenu(View v, long id, int position, String puzzleName) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        Menu menu = popupMenu.getMenu();
        menu.add(0, SudokuListActivity.MENU_ITEM_PLAY, 0, R.string.play_puzzle);
        menu.add(0, SudokuListActivity.MENU_ITEM_EDIT_NOTE, 1, R.string.edit_note);
        menu.add(0, SudokuListActivity.MENU_ITEM_RESET, 2, R.string.reset_puzzle);
        if (id < 1 || id > 400) {
            menu.add(0, SudokuListActivity.MENU_ITEM_EDIT, 3, R.string.edit_puzzle);
            menu.add(0, SudokuListActivity.MENU_ITEM_DELETE, 4, R.string.delete_puzzle);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            Sound.soundClick(context);
            switch (item.getItemId()) {
                case SudokuListActivity.MENU_ITEM_PLAY:
                    playSudoku(id, position, puzzleName);
                    return true;
                case SudokuListActivity.MENU_ITEM_EDIT:
                    Intent i = new Intent(context, SudokuEditActivity.class);
                    i.setAction(Intent.ACTION_EDIT);
                    i.putExtra(SudokuEditActivity.EXTRA_SUDOKU_ID, id);
                    context.startActivity(i);
                    SudokuListActivity.LAST_POSITION = position;
                    return true;
                case SudokuListActivity.MENU_ITEM_DELETE:
                    SudokuListActivity.mDeletePuzzleID = id;
                    runDeletePuzzle.run();
                    if (position !=0)
                        SudokuListActivity.LAST_POSITION = position - 1;
                    return true;
                case SudokuListActivity.MENU_ITEM_EDIT_NOTE:
                    SudokuListActivity.mEditNotePuzzleID = id;
                    runEditPuzzle.run();
                    SudokuListActivity.LAST_POSITION = position;
                    return true;
                case SudokuListActivity.MENU_ITEM_RESET:
                    SudokuListActivity.mResetPuzzleID = id;
                    runResetPuzzle.run();
                    SudokuListActivity.LAST_POSITION = position;
                    return true;
            }
            return false;
        });
        popupMenu.show();
    }

    public static class SudokuModel {

        private int sudokuID, state;
        private long time, lastPlayed, created;
        private String data, note;

        public int getSudokuID() {
            return sudokuID;
        }

        public void setSudokuID(int sudokuID) {
            this.sudokuID = sudokuID;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getLastPlayed() {
            return lastPlayed;
        }

        public void setLastPlayed(long lastPlayed) {
            this.lastPlayed = lastPlayed;
        }

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }


    private final DateFormat mDateTimeFormatter = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.SHORT);
    private final DateFormat mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);

    private String getDateAndTimeForHumans(long datetime) {
        Date date = new Date(datetime);
        //Date now = new Date(System.currentTimeMillis());
        //Date today = new Date(now.getYear(), now.getMonth(), now.getDate());
        Calendar now = new GregorianCalendar();
        now.setTime(date);
        Calendar today = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DATE));
        //Date yesterday = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24));

        if (now.after(today)) {
            return context.getString(R.string.at_time, mTimeFormatter.format(date));
        /*} else if (date.after(yesterday)) {
            return context.getString(R.string.yesterday_at_time, mTimeFormatter.format(date));*/
        } else {
            return context.getString(R.string.on_date, mDateTimeFormatter.format(date));
        }
    }
}
