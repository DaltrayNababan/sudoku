package com.gwnbs.sudoku.game.command;

import com.gwnbs.sudoku.game.CellCollection;

public class FillInNotesCommand extends AbstractMultiNoteCommand {

    public FillInNotesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldNotes.clear();
        saveOldNotes();

        cells.fillInNotes();
    }
}
