package com.apps.saadat.hdm;


import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MultiSegment implements Observer{
    private static final int BUFFER_SIZE = 1024 * 2;
    private DownloadListAdapter.DownloadBar downloadBar;
    private long totalReads = 0L;
    private long totalSize = 0L;
    private Download download;
    private int finishedTasks = 0;
    ExecutorService executorService;
    List<DTask2> tasks = new ArrayList<>();

    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof Integer)
            totalReads += (int)o;
        else
            finishedTasks++;
        int percent = (int)(totalReads * 100 / totalSize);
        downloadBar.tv_percent.setText(percent + "%");
        downloadBar.progressbar.setProgress(percent);
        if(finishedTasks == 8){
            joinParts(download.filename, 8);
        }
    }

    public void startSegments(Download download, DownloadListAdapter.DownloadBar downloadBar, int numberOfParts){
        this.downloadBar = downloadBar;
        this.download = download;
        totalSize = download.fileSize;
        int i = 1;
        HashMap<Long, Long> map = separateFileSize(totalSize, numberOfParts);
        executorService = Executors.newFixedThreadPool(numberOfParts);
        for(Map.Entry entry : map.entrySet() ){
            DTask2 task = new DTask2(downloadBar.btn_open.getContext(),download, this);
            task.executeOnExecutor(executorService, String.valueOf(i++), String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            tasks.add(task);
        }

//        joinParts(download.filename, 8);
    }

    public void pauseSegments(){
        if(tasks == null) return;
        if(tasks.isEmpty()) return;
        for(DTask2 task : tasks){
            task.isCancelled = true;
            task.cancel(true);
            if(task.isCancelled()) Log.println(Log.ERROR, "CANCEL", "task is cancelled");
        }
        tasks.clear();
    }

    public void stopSegments(){
        //TODO must be written
    }


    public  HashMap<Long, Long> separateFileSize(long fileSize, int numberOfParts){
        HashMap<Long, Long> partInfo = new LinkedHashMap<>();
        long startPoint = 0;
        double partSize = (double)fileSize / numberOfParts;
        double floatingPart = partSize - (long)partSize;
        double remainingSize = 0.0d;
        if(floatingPart != 0.0d){
            if(partSize < 1){
                partInfo.put(0L, fileSize);
                return partInfo;
            }else /*if(partSize > 1)*/{
                partSize = partSize - floatingPart;
                remainingSize = floatingPart * numberOfParts;
            }
        }else{ /*all things is ok, there is no floating point*/ }

        if(remainingSize == 0d) {
            for (int i = 0; i < numberOfParts; i++) {
                partInfo.put(startPoint, (long)partSize);
                startPoint += partSize;
            }
        }else{
            for (int i = 0; i < numberOfParts - 1; i++) {
                partInfo.put(startPoint, (long)partSize);
                startPoint += partSize;
            }
            partInfo.put(startPoint, (long)(partSize + remainingSize));
        }

        return partInfo;

    }

    public  void test(HashMap<Long, Long> arr){
        Log.wtf("TAG((:", download.fileSize + "");
        long total = 0;
        for(Map.Entry<Long, Long> entry : arr.entrySet()){
            Log.wtf("TAG((:","start point: " + entry.getKey() + " end point : " + (entry.getKey() +  entry.getValue())
                    + " part size: " + entry.getValue());
            total += entry.getValue();
        }
        Log.wtf("TAG((:", "total size: " + total);
    }


    public void joinParts(String filename, int numberOfParts) {
        int count = 1;
        byte[] buffer = new byte[BUFFER_SIZE];
        FileOutputStream output = null;
        FileInputStream input = null;
        File outputPath = new File(Environment.getExternalStorageDirectory().toString() + "/HDM" );
        File inputPath = new File(Environment.getExternalStorageDirectory().toString() + "/HDMTemp/" + filename);
        File outputFile = new File(outputPath,filename);
        if(!outputPath.exists()) outputPath.mkdirs();
        try {
            outputFile.createNewFile();
            output = new FileOutputStream(outputFile, true);
            while (count <= numberOfParts) {
                File part = new File(inputPath, filename + ".part" + count);
                count++;
                if (part.exists()) {
                    input = new FileInputStream(part);
                    int readBytes = 0;
                    while ((readBytes = input.read(buffer)) > 0) {
                        output.write(buffer, 0 , readBytes);
                        output.flush();
                    }
                    input.close();
//                    part.delete();
                }else{
                    System.out.println("a part number #" + count + " not found!!!");
                    //                    Log.println(Log.ERROR,"Lost Part:","a part number #" + count + " not found!!!");
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(input != null) input.close();
                if(output != null) output.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


}

