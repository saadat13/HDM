package com.apps.saadat.hdm;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Observable;

public class CompletedFragment extends SlideFragment {

    public static CompletedFragment newSlide(String slideTitle) {
        Bundle args = new Bundle();
        args.putString("title", slideTitle);
        CompletedFragment fragment = new CompletedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloads = FileUtils.loadDownloadsList(getContext(), FileUtils.SAVED_COMPLETED_LIST_NAME);
        if(downloads == null) downloads = new ArrayList<>();
        if(downloadBars == null) downloadBars =  new ArrayList<>();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshDisplay();
    }

    @Override
    public void onPause() {
        super.onPause();
        FileUtils.saveDownloadsList(getContext(),FileUtils.SAVED_COMPLETED_LIST_NAME, downloads);
    }

    @Override
    public void update(Observable observable, Object o){
        super.update(observable, o);
        FileUtils.saveDownloadsList(getContext(), FileUtils.SAVED_COMPLETED_LIST_NAME, downloads);
    }
}
