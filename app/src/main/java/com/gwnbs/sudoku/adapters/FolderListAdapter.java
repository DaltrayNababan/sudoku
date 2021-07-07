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
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.gui.FolderDetailLoader;
import com.gwnbs.sudoku.gui.FolderListActivity;
import com.gwnbs.sudoku.gui.SudokuExportActivity;
import com.gwnbs.sudoku.gui.SudokuListActivity;
import com.gwnbs.sudoku.utils.Sound;

import java.util.List;

public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.FolderListHolder> {

    private final List<FolderModel> folderModels;
    private final Context context;
    private final Runnable runDeleteFolder, runRenameFolder;
    public FolderDetailLoader mDetailLoader;

    public FolderListAdapter(List<FolderModel> folderModels, Context context, Runnable runDeleteFolder,
                             Runnable runRenameFolder) {
        this.folderModels = folderModels;
        this.context = context;
        this.runDeleteFolder = runDeleteFolder;
        this.runRenameFolder = runRenameFolder;
        mDetailLoader = new FolderDetailLoader(context);
    }

    @NonNull
    @Override
    public FolderListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FolderListHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FolderListHolder holder, int position) {
        holder.bindFolder(folderModels.get(position));
    }

    @Override
    public int getItemCount() {
        return folderModels.size();
    }

    class FolderListHolder extends RecyclerView.ViewHolder {

        private final TextView name, detail;

        public FolderListHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            detail = itemView.findViewById(R.id.detail);
        }

        void bindFolder(FolderModel folderModel) {
            name.setText(folderModel.getName());
            name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pastime, 0, 0, 0);
            detail.setText(folderModel.getDetail());
            detail.setText(context.getString(R.string.loading));
            mDetailLoader.loadDetailAsync(folderModel.getId(), folderInfo -> {
                if (folderInfo != null)
                    detail.setText(HtmlCompat.fromHtml(folderInfo.getDetail(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
            });

            itemView.setOnClickListener(v -> {
                Sound.soundClick(context);
                Intent i = new Intent(context, SudokuListActivity.class);
                i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, folderModel.getId());
                context.startActivity(i);
                SudokuListAdapter.sudokuFolderName = folderModel.getName();
            });

            itemView.setOnLongClickListener(v -> {
                Sound.soundClick(context);
                createMenu(v, folderModel.getId());
                return false;
            });
        }
    }

    private void createMenu(View v, long id) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        Menu menu = popupMenu.getMenu();
        menu.add(0, FolderListActivity.MENU_ITEM_EXPORT, 0, R.string.export_folder);
        menu.add(0, FolderListActivity.MENU_ITEM_RENAME, 1, R.string.rename_folder);
        if (id < 1 || id > 4)
            menu.add(0, FolderListActivity.MENU_ITEM_DELETE, 2, R.string.delete_folder);
        popupMenu.setOnMenuItemClickListener(item -> {
            Sound.soundClick(context);
            switch (item.getItemId()) {
                case FolderListActivity.MENU_ITEM_EXPORT:
                    Intent intent = new Intent();
                    intent.setClass(context, SudokuExportActivity.class);
                    intent.putExtra(SudokuExportActivity.EXTRA_FOLDER_ID, id);
                    context.startActivity(intent);
                    break;
                case FolderListActivity.MENU_ITEM_RENAME:
                    FolderListActivity.mRenameFolderID = id;
                    runRenameFolder.run();
                    break;
                case FolderListActivity.MENU_ITEM_DELETE:
                    FolderListActivity.mDeleteFolderID = id;
                    runDeleteFolder.run();
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    public static class FolderModel {

        private long id;
        private String name, detail;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }

    public void destroyLoader() {
        mDetailLoader.destroy();
    }
}
