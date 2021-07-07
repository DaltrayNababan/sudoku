package com.gwnbs.sudoku.gui.exporting;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;

import com.gwnbs.sudoku.db.SudokuColumns;
import com.gwnbs.sudoku.db.SudokuDatabase;
import com.gwnbs.sudoku.utils.Const;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.Executors;

/**
 * Must be created on GUI thread.
 *
 * @author romario
 */
public class FileExportTask {

    private final Context mContext;

    private OnExportFinishedListener mOnExportFinishedListener;

    public FileExportTask(Context context) {
        mContext = context;
    }

    /*public OnExportFinishedListener getOnExportFinishedListener() {
        return mOnExportFinishedListener;
    } */

    public void setOnExportFinishedListener(OnExportFinishedListener listener) {
        mOnExportFinishedListener = listener;
    }

    public void execute(FileExportTaskParams params) {
        Executors.newSingleThreadExecutor().execute(() -> {
            final FileExportTaskResult res = saveToFile(params);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (mOnExportFinishedListener != null) {
                    mOnExportFinishedListener.onExportFinished(res);
                }
            });
        });
    }

    private FileExportTaskResult saveToFile(FileExportTaskParams par) {
        if (par.folderID == null && par.sudokuID == null) {
            throw new IllegalArgumentException("Exactly one of folderID and sudokuID must be set.");
        } else if (par.folderID != null && par.sudokuID != null) {
            throw new IllegalArgumentException("Exactly one of folderID and sudokuID must be set.");
        }

        if (par.file == null) {
            throw new IllegalArgumentException("Filename must be set.");
        }

        long start = System.currentTimeMillis();

        FileExportTaskResult result = new FileExportTaskResult();
        result.successful = false;
        result.filename = par.filename;

        SudokuDatabase database = null;
        Cursor cursor = null;
        Writer writer = null;
        try {
            database = new SudokuDatabase(mContext);
            boolean generateFolders;
            if (par.folderID != null) {
                cursor = database.exportFolder(par.folderID);
                generateFolders = true;
            } else {
                cursor = database.exportFolder(par.sudokuID);
                generateFolders = false;
            }

            XmlSerializer serializer = Xml.newSerializer();
            writer = new BufferedWriter(new OutputStreamWriter(par.file));
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "opensudoku");
            serializer.attribute("", "version", "2");

            long currentFolderId = -1;
            while (cursor.moveToNext()) {
                if (generateFolders && currentFolderId != cursor.getLong(cursor.getColumnIndex("folder_id"))) {
                    // next folder
                    if (currentFolderId != -1) {
                        serializer.endTag("", "folder");
                    }
                    currentFolderId = cursor.getLong(cursor.getColumnIndex("folder_id"));
                    serializer.startTag("", "folder");
                    attribute(serializer, "name", cursor, "folder_name");
                    attribute(serializer, "created", cursor, "folder_created");
                }

                String data = cursor.getString(cursor.getColumnIndex(SudokuColumns.DATA));
                if (data != null) {
                    serializer.startTag("", "game");
                    attribute(serializer, "created", cursor, SudokuColumns.CREATED);
                    attribute(serializer, "state", cursor, SudokuColumns.STATE);
                    attribute(serializer, "time", cursor, SudokuColumns.TIME);
                    attribute(serializer, "last_played", cursor, SudokuColumns.LAST_PLAYED);
                    attribute(serializer, "data", cursor, SudokuColumns.DATA);
                    attribute(serializer, "note", cursor, SudokuColumns.PUZZLE_NOTE);
                    attribute(serializer, "command_stack", cursor, SudokuColumns.COMMAND_STACK);
                    serializer.endTag("", "game");
                }
            }
            if (generateFolders && currentFolderId != -1) {
                serializer.endTag("", "folder");
            }

            serializer.endTag("", "opensudoku");
            serializer.endDocument();
        } catch (IOException e) {
            Log.e(Const.TAG, "Error while exporting file.", e);
            result.successful = false;
            return result;
        } finally {
            if (cursor != null) cursor.close();
            if (database != null) database.close();
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e(Const.TAG, "Error while exporting file.", e);
                    result.successful = false;
                    return result;
                }
            }

        }

        long end = System.currentTimeMillis();

        Log.i(Const.TAG, String.format("Exported in %f seconds.",
                (end - start) / 1000f));

        result.successful = true;
        return result;
    }

    private void attribute(XmlSerializer serializer, String attributeName, Cursor cursor, String columnName)
            throws IllegalArgumentException, IllegalStateException, IOException {
        String value = cursor.getString(cursor.getColumnIndex(columnName));
        if (value != null) {
            serializer.attribute("", attributeName, value);
        }
    }

    public interface OnExportFinishedListener {
        /**
         * Occurs when export is finished.
         *
         * @param result The result of the export
         */
        void onExportFinished(FileExportTaskResult result);
    }

}
