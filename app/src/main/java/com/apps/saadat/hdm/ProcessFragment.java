package com.apps.saadat.hdm;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ProcessFragment extends SlideFragment {


    public static ProcessFragment newSlide(String slideTitle) {
        Bundle args = new Bundle();
        args.putString("title", slideTitle);
        ProcessFragment fragment = new ProcessFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloads = FileUtils.loadDownloadsList(getContext(), FileUtils.SAVED_DOWNLOAD_LIST_NAME);
        if(downloads == null) downloads = new ArrayList<>();
        if(downloadBars == null) downloadBars = new ArrayList<>();

    }



    @Override
    public void startAll(){
        for(DownloadListAdapter.DownloadBar d : adapter.getDownloadBarList()){
            if(d.getDownloadInfo().getIsComplete() || d.getDownloadInfo().isDownloading) continue;
            d.btn_resume.performClick();
        }
//        Toast.makeText(getContext(), "is here", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addNewDownload() {
        DLUtils.addNewDownload(getContext(),(Observer) this ,FileUtils.SAVED_DOWNLOAD_LIST_NAME, downloads);
    }

    @Override
    public void update(Observable observable, Object o){
        super.update(observable, o);
        FileUtils.saveDownloadsList(getContext(), FileUtils.SAVED_DOWNLOAD_LIST_NAME, downloads);
    }




}
