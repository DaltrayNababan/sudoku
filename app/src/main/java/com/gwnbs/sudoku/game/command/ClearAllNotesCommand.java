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

package com.gwnbs.sudoku.game.command;

import com.gwnbs.sudoku.game.CellNote;
import com.gwnbs.sudoku.game.Cell;
import com.gwnbs.sudoku.game.CellCollection;

public class ClearAllNotesCommand extends AbstractMultiNoteCommand {

    public ClearAllNotesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldNotes.clear();
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = cells.getCell(r, c);
                CellNote note = cell.getNote();
                if (!note.isEmpty()) {
                    mOldNotes.add(new NoteEntry(r, c, note));
                    cell.setNote(new CellNote());
                }
            }
        }
    }
}
