package com.apps.saadat.hdm;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SlideFragment extends android.support.v4.app.Fragment implements Observer{

    protected DownloadListAdapter adapter;
    protected List<DownloadListAdapter.DownloadBar> downloadBars;
    protected List<Download> downloads;
    protected ListView listView;
    protected DLUtils DLUtils = new DLUtils();
    protected String slideTitle;

    public String getSlideTitle() {
        return slideTitle;
    }

    public static SlideFragment newSlide(String slideTitle) {
        Bundle args = new Bundle();
        args.putString("title", slideTitle);
        SlideFragment fragment = new SlideFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        slideTitle = bundle.getString("title");
        downloadBars = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_slide_fragment, container, false);
        listView = rootView.findViewById(R.id.fragment_listview);
        refreshDisplay();
        return rootView;
    }

    public void addNewDownload(){
        // implemented in child class
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    public  void refreshDisplay(){
        adapter = new DownloadListAdapter(getContext(), this,downloads);
        listView.setAdapter(adapter);
    }

    public void refreshDisplay(List<Download> downloadList){
        adapter = new DownloadListAdapter(getContext(), this,downloadList);
        listView.setAdapter(adapter);
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable != null && o != null) {
            if (o instanceof Download) {
                downloads.remove(o);
                refreshDisplay();
            } else if (o instanceof DownloadListAdapter.DownloadBar) {
                downloadBars.remove(o);
                downloads.remove(((DownloadListAdapter.DownloadBar) o).getDownloadInfo());
                MainActivity.moveToCompleted((DownloadListAdapter.DownloadBar) o);
                refreshDisplay();
            }
        }
        refreshDisplay();
//        }else if(o instanceof String){
//            refreshDisplay();
//            listView.performItemClick(null, adapter.getPosition(o), listView.getItemIdAtPosition(adapter.getPosition(o)) );
//        }
    }

    /**
     * searching in names, urls, sizes
     * @param keyword
     */
    public void search(String keyword){
        if(keyword.isEmpty()) {
            refreshDisplay(downloads);
            return;
        }
        List<Download> downloadList = new ArrayList<>();
        for(Download download : downloads){
            if(download.getFilename().contains(keyword))
                downloadList.add(download);
        }
        refreshDisplay(downloadList);
    }


    public void addFakeDownload(int n) {
        for(int i = 0 ; i < n ; i++){
            Download download = new Download("url" + (i + 1));
            download.setFilename("file #" + (i + 1));
            download.setFileSize(10000 + 100 * ( i + 1));
            download.setDate(new Date(System.currentTimeMillis()).toString());
            download.setSavePath(Environment.getExternalStorageDirectory() + "/HDM");
            downloads.add(download);
        }
    }



    public void selectAll(){
        for(Download download : downloads){
            boolean isSelected = download.isSelected;
            if(!isSelected) download.isSelected = true;
        }
    }

    public void deselectAll(){
        for(Download download : downloads){
            boolean isSelected = download.isSelected;
            if(isSelected) download.isSelected = false;
        }
    }

    public void invertSelection(){
        for(Download download : downloads){
            boolean isSelected = download.isSelected;
            download.isSelected = !isSelected;
        }
    }

    public void startAll(){
        // must implemented in child class
    }

    public void stopAll(){
        for(DownloadListAdapter.DownloadBar d : adapter.downloadBarList){
            if(!d.btn_resume.isEnabled() || d.getTask() == null || !d.getDownloadInfo().isDownloading) continue;
            d.btn_resume.performClick();
        }
    }
}
