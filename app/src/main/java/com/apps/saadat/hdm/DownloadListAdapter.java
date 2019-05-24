package com.apps.saadat.hdm;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;

public class DownloadListAdapter extends ArrayAdapter {

    Context context;
    List<Download> downloads;
    List<DownloadBar> downloadBarList;
    Observer fragmentObserver;
    LayoutInflater inflater;

    public DownloadListAdapter(@NonNull Context context, Observer fragmentObserver ,List<Download> downloads) {
        super(context, R.layout.layout_downloadbar, downloads);
        this.context = context;
        this.downloads = downloads;
        this.fragmentObserver = fragmentObserver;
        downloadBarList = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DownloadBar viewHolder;
        if(convertView == null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_downloadbar, parent, false);
            viewHolder = new DownloadBar(fragmentObserver , convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (DownloadBar) convertView.getTag();
        }
        viewHolder.fill(position);
        if(!downloadBarList.contains(viewHolder))
            downloadBarList.add(viewHolder);
        return convertView;
    }

    public List<DownloadBar> getDownloadBarList() {
        return downloadBarList;
    }

    class DownloadBar extends Observable implements View.OnClickListener, View.OnLongClickListener {

        Button btn_resume, btn_stop, btn_open;
        TextView tv_file_name, tv_percent, tv_file_size;
        CheckBox cb_select;
        ProgressBar progressbar;
        RelativeLayout parentLayout;
        DownloaderTask task;
        ExecutorService defExecutor = MyThreadPool.executor;
        View view;
        Download downloadInfo;


        public DownloadBar(Observer fragmentObserver , View convertView) {
            addObserver(fragmentObserver);
            view = convertView;
        }

        public void sendCompleteMessageToUi(){
            setChanged();
            DownloadBar.this.notifyObservers(DownloadBar.this);
        }

        public void fill(int position) {
            downloadInfo = downloads.get(position);
            parentLayout = view.findViewById(R.id.barParentPanel);
            btn_resume = view.findViewById(R.id.btn_resume);
            btn_stop = view.findViewById(R.id.btn_stop);
            btn_open = view.findViewById(R.id.btn_open);
            tv_file_name = view.findViewById(R.id.tv_file_name);
            tv_percent = view.findViewById(R.id.tv_percent);
            tv_file_size = view.findViewById(R.id.tv_file_size);
            progressbar = view.findViewById(R.id.progressbar);
            cb_select = view.findViewById(R.id.cb_select);
            cb_select.setChecked(downloadInfo.isSelected);
            cb_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    downloadInfo.isSelected = isChecked;
                }
            });

            Drawable progressDrawable = progressbar.getProgressDrawable().mutate();
            progressDrawable.setColorFilter(view.getContext().getResources().getColor(R.color.progressbarColor), android.graphics.PorterDuff.Mode.SRC_IN);
            progressbar.setProgressDrawable(progressDrawable);

            tv_file_name.setText(downloadInfo.filename);
            tv_percent.setText(downloadInfo.getPercent() + "%");
            progressbar.setProgress(Integer.valueOf(downloadInfo.getPercent()));

//            ((TextView)view.findViewById(R.id.tv_speed)).setText("#" + (position + 1));

            boolean isComplete = downloadInfo.getIsComplete();
            btn_resume.setEnabled(!isComplete);
            btn_stop.setEnabled(!isComplete);
            btn_open.setEnabled(isComplete);

            btn_resume.setOnClickListener(this);
            btn_stop.setOnClickListener(this);
            btn_open.setOnClickListener(this);
            parentLayout.setOnLongClickListener(this);
            parentLayout.setOnClickListener(this);

            progressbar.setMax(100);

            if (downloadInfo.isAutoDownload) {
                btn_resume.performClick();
                downloadInfo.isAutoDownload = false;
            }

        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == btn_resume.getId()) {
                if (task == null) {
                    task = new DownloaderTask(view.getContext(), downloadInfo, DownloadBar.this);
                    task.executeOnExecutor(defExecutor);                    // EXECUTING...
                    btn_resume.setText(R.string.pause);
                } else {
                    task.cancel(true);
                    task = null;
                    btn_resume.setText(R.string.start);
                }
            } else if (id == btn_stop.getId()) {

                File file = new File(downloadInfo.savePath, downloadInfo.filename);
                if (file.delete())
                    Toast.makeText(view.getContext(), "download has been successfully stopped!", Toast.LENGTH_SHORT).show();
                progressbar.setProgress(0);
                btn_resume.setText(R.string.start);
                tv_file_size.setText(downloadInfo.getReadableSize());
                tv_percent.setText("0%");
                progressbar.invalidate();
                tv_percent.invalidate();
                tv_file_size.invalidate();
            } else if (id == btn_open.getId()) {
                if (!btn_open.isEnabled()) return;
                FileUtils.viewFile(view.getContext(), downloadInfo.getFullPath());
            }
        }

        // this section is initializing and setting right menu click

        private PopupMenu initRightClickMenu(View view) {
            PopupMenu rightClickMenu = new PopupMenu(view.getContext(), tv_file_name);
            rightClickMenu.inflate(R.menu.menu_long_click_on_bar);
            MenuItem item_open = rightClickMenu.getMenu().findItem(R.id.item_open);
//            MenuItem item_addToQ = rightClickMenu.getMenu().findItem(R.id.item_add_toQ);
            MenuItem item_redown = rightClickMenu.getMenu().findItem(R.id.item_redownload);
            MenuItem item_move_to_up = rightClickMenu.getMenu().findItem(R.id.move_to_up);
            MenuItem item_move_to_down = rightClickMenu.getMenu().findItem(R.id.move_to_down);
            MenuItem item_delete = rightClickMenu.getMenu().findItem(R.id.item_delete);
            MenuItem item_prop = rightClickMenu.getMenu().findItem(R.id.item_prop);
            RightClickMenuListener listener = new RightClickMenuListener();
            item_open.setOnMenuItemClickListener(listener);
//            item_addToQ.setOnMenuItemClickListener(listener);
            item_redown.setOnMenuItemClickListener(listener);
            item_delete.setOnMenuItemClickListener(listener);
            item_prop.setOnMenuItemClickListener(listener);
            item_move_to_up.setOnMenuItemClickListener(listener);
            item_move_to_down.setOnMenuItemClickListener(listener);

            return rightClickMenu;
        }


        @Override
        public boolean onLongClick(View view) {
            if(view.getId() == parentLayout.getId()){
                initRightClickMenu(view).show(); //   ==== popupmenu.show()
//                cb_select.setChecked(true);
//                downloadInfo.isSelected = true;
            }

            return true;
        }

        private class RightClickMenuListener implements MenuItem.OnMenuItemClickListener{
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                final Context context = btn_open.getContext();
                final File file = new File(downloadInfo.getFullPath());
                switch (id){
                    case R.id.item_open:
                        if(downloadInfo.isComplete){
                            if(file.exists())
                                FileUtils.viewFile(context, downloadInfo.getFullPath());
                            else
                                Toast.makeText(context, context.getText(R.string.fileNotExistMessage), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, context.getText(R.string.filenotdownloadedCompletely), Toast.LENGTH_SHORT).show();
                        }
                        break;
//                    case R.id.item_add_toQ:
//                        break;

                    case R.id.move_to_up:
                        swapItem(downloads.indexOf(downloadInfo), true);
                        notifyDataSetInvalidated();
                        break;
                    case R.id.move_to_down:
                        swapItem(downloads.indexOf(downloadInfo), false);
                        notifyDataSetInvalidated();
                        break;
                    case R.id.item_redownload:
                        break;
                    case R.id.item_delete:
                        final Dialog deleteDialog = new Dialog(context);
                        deleteDialog.setContentView(R.layout.layout_delete_alert_dialog);
                        final List<Download> selecteds = findSelectedDownloads();
                        ((TextView)deleteDialog.findViewById(R.id.tv_delete_question)).setText(R.string.delete_alert_message);
                        deleteDialog.setTitle(R.string.delete_alert);
                        deleteDialog.show();
                        deleteDialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                boolean deleteFromMemory = ((CheckBox)deleteDialog.findViewById(R.id.cb_delete_from_memory)).isChecked();
                                if(deleteFromMemory)
                                {
                                    List<File> files = findSelectedFiles(selecteds);
                                    for(File file1: files)
                                        file1.delete();
                                }
                                for(Download download : selecteds) {
                                    setChanged();
                                    DownloadBar.this.notifyObservers(download); // delete download
                                }
                                deleteDialog.dismiss();
                            }
                        });
                        deleteDialog.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(deleteDialog.isShowing()) deleteDialog.dismiss();
                            }
                        });
                        break;
                    case R.id.item_prop:
                        Dialog dialog = new Dialog(context);
                        dialog.setTitle(R.string.properties);
                        dialog.setContentView(R.layout.layout_properties);
                        ((EditText)dialog.findViewById(R.id.prop_tv_name)).setText(downloadInfo.filename);
                        ((EditText)dialog.findViewById(R.id.prop_tv_url)).setText(downloadInfo.url);
                        ((TextView)dialog.findViewById(R.id.prop_tv_path)).setText(downloadInfo.getFullPath());
                        ((TextView)dialog.findViewById(R.id.prop_tv_size)).setText(downloadInfo.getReadableSize());
                        ((TextView)dialog.findViewById(R.id.prop_tv_date)).setText(downloadInfo.date);
                        ((TextView)dialog.findViewById(R.id.prop_tv_stat)).setText(downloadInfo.getPercent() + "%");
                        ((Button)dialog.findViewById(R.id.prop_copy_url)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                if(clipboardManager == null) return;
                                ClipData url = ClipData.newPlainText("url", downloadInfo.url);
                                clipboardManager.setPrimaryClip(url);
                                Toast.makeText(context, R.string.url_copy_message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.show();
                        break;
                    case R.id.barParentPanel:
                        downloadInfo.isSelected = !downloadInfo.isSelected;
                        cb_select.setChecked(downloadInfo.isSelected);
                        break;
                    default: break;
                }
                return true;
            }
        }

        private List<Download> findSelectedDownloads() {
            List<Download> selecteds = new ArrayList<>();
            for(Download download : downloads){
                if(download.isSelected)
                    selecteds.add(download);
            }
            return selecteds;
        }

        private List<File> findSelectedFiles(List<Download> downloads){
            List<File> files = new ArrayList<>();
            for(Download download : downloads){
                files.add(new File(download.getFullPath()));
            }
            return files;
        }

        public void swapItem(int itemPosition, boolean direction){
            // to up is true
            // to down is false
            if(itemPosition == 0 && direction) return;
            if((itemPosition == downloads.size() - 1) && !direction) return;

            Download temp = downloads.get(itemPosition);
            if(direction){
                downloads.set(itemPosition, downloads.get(itemPosition - 1));
                downloads.set(itemPosition -1 , temp);
            }else{
                downloads.set(itemPosition, downloads.get(itemPosition + 1));
                downloads.set(itemPosition + 1, temp);
            }

        }

        public Download getDownloadInfo() {
            return downloadInfo;
        }

        public DownloaderTask getTask() {
            return task;
        }

        public void setDefExecutor(ExecutorService defExecutor) {
            this.defExecutor = defExecutor;
        }
    }
}
