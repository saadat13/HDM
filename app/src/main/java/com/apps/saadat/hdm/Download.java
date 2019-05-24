package com.apps.saadat.hdm;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Download {
    String url;
    String filename = " ";
    String savePath = Environment.getExternalStorageDirectory().toString() + "/" +  "HDM";
    
    long fileSize = 1L;
    String date = "";
    boolean isComplete;
    boolean isDownloading;
    boolean isAutoDownload = false;
    public boolean isSelected = false;
    public List<Long> positions = new ArrayList<>(8);


    public Download(String url){
        this.url = url;
    }

    public String getFullPath(){
        return savePath + "/" + filename;
    }


    public String getPercent() {
        File file = new File(savePath, filename);
        return String.valueOf((int)(file.length() * 100 / fileSize));
    }

    public String getReadableTD() {
        File file = new File(savePath, filename);
        return FileUtils.getReadableSize(file.length());
    }

    public String getReadableSize() {
        return FileUtils.getReadableSize(fileSize);
    }


    public void setFileSize(long fileSize){
        this.fileSize = fileSize;
    }


    public void setDate(String date){
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return filename;
    }

    public boolean getIsComplete(){
        File file = new File(getFullPath());
        return (file.length() == fileSize);
    }

    @Override
    public boolean equals(Object object){
        if(object == this)
            return true;
        if(!(object instanceof Download))
            return false;
        Download other = (Download)object;
        return other.getUrl().equals(this.url) &&
                other.getFilename().equals(this.filename) &&
                other.getFileSize() == this.fileSize &&
                other.getSavePath().equals(this.savePath);
    }
}
