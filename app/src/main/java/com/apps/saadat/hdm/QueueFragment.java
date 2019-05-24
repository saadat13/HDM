package com.apps.saadat.hdm;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;


public class QueueFragment extends SlideFragment{


    public static QueueFragment newSlide(String slideTitle) {
        Bundle args = new Bundle();
        args.putString("title", slideTitle);
        QueueFragment fragment = new QueueFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloads = FileUtils.loadDownloadsList(getContext(), FileUtils.SAVED_QUEUE_LIST_NAME);
        if(downloadBars == null)
            downloadBars = new ArrayList<>();
        if(downloads == null)
            downloads = new ArrayList<>();
    }

    @Override
    public void addNewDownload() {
        DLUtils.addNewDownload(getContext(),this ,FileUtils.SAVED_QUEUE_LIST_NAME, downloads);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    public void startAll(){
        for(DownloadListAdapter.DownloadBar downloadBar : adapter.getDownloadBarList()){
            if(downloadBar.getDownloadInfo().isComplete || downloadBar.getDownloadInfo().isDownloading) continue;
            downloadBar.setDefExecutor(MyThreadPool.singleExecutor);
            downloadBar.btn_resume.performClick();
        }
    }

    @Override
    public void update(Observable observable, Object o){
        super.update(observable, o);
        FileUtils.saveDownloadsList(getContext(), FileUtils.SAVED_QUEUE_LIST_NAME, downloads);
    }



}
