package com.apps.saadat.hdm;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class DTask2 extends AsyncTask<String, Integer, String> {


    Context context;

    private static final int BUFFER = 1024 * 2;
    private Download downloadInfo;
    public boolean isCancelled = false;
    public boolean isFinished = false;
    public class Bridge extends Observable{
        @Override
        public synchronized void setChanged() {
            super.setChanged();
        }
    }
    public Bridge bridge;


    public DTask2(Context context, final Download downloadInfo, Observer observer){
        this.context = context;
        this.downloadInfo = downloadInfo;
        bridge = new Bridge();
        bridge.addObserver(observer);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        int partNumber = Integer.valueOf(strings[0]);
        long startPoint = Long.valueOf(strings[1]);
        long limit = Long.valueOf(strings[2]);
        FileOutputStream fos = null;
        Log.wtf("IN Back:", "doing....");
        File file = null;
        try {
            URL url = new URL(downloadInfo.getUrl());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            File path = new File(Environment.getExternalStorageDirectory().toString() + "/HDMTemp/" + downloadInfo.filename);
            if(!path.exists()) path.mkdirs();
            file = new File(path, downloadInfo.getFilename() + ".part" + partNumber);
//            if(file.exists()){
//                if(file.length() == limit) {
//                    isFinished = true;
//                    return "file is already downloaded";
//                }
//                else if(file.length() < limit){
//                    connection.setRequestProperty("Range", "bytes=" + (startPoint + file.length()) + "-");
//                    fos = new FileOutputStream(file,true);
//                }else if(file.length() > limit){
//                    return "file has been corrupted!";
//                }
//            }else{
                file.createNewFile();
                connection.setRequestProperty("Range", "bytes=" + startPoint + "-");
                fos = new FileOutputStream(file,false);
//            }
            connection.connect();
            byte[] buffer = new byte[BUFFER];
            long totalRead = file.length();
            int count = 0;
            InputStream in = connection.getInputStream();
            while(file.length() <= limit && (count = in.read(buffer)) > 0){
                fos.write(buffer, 0, count);
                publishProgress(count);
            }

            if(fos != null) fos.close();
            if(in != null) in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "part #" + partNumber + "paused";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int totalRead = values[0];
        bridge.setChanged();
        bridge.notifyObservers(totalRead);
    }
    @Override
    protected void onPostExecute(String result) {
        Log.println(Log.ERROR, "MY TAG :::", result);
        bridge.setChanged();
        bridge.notifyObservers("");
    }
}
