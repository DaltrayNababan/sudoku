package com.gwnbs.sudoku.game.command;

import com.gwnbs.sudoku.game.CellCollection;

public class FillInNotesWithAllValuesCommand extends AbstractMultiNoteCommand {

    public FillInNotesWithAllValuesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldNotes.clear();
        saveOldNotes();

        cells.fillInNotesWithAllValues();
    }
}
