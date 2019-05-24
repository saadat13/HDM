package com.apps.saadat.hdm;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class DownloaderTask extends AsyncTask<String, String, String> {


    private static final int BUFFER = 1024 * 2;

    private Download downloadInfo;
    private Context context;
    private DownloadListAdapter.DownloadBar downloadBar;
    public boolean isCompleted = false;


    public DownloaderTask(Context context, Download downloadInfo,  DownloadListAdapter.DownloadBar downloadBar){
        this.downloadInfo = downloadInfo;
        if(downloadInfo == null) Toast.makeText(context, "download info is null", Toast.LENGTH_LONG).show();
        this.context = context;
        this.downloadBar = downloadBar;

    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "download has started...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(String... strings) {
        if(downloadInfo == null) cancel(true);
        FileOutputStream fos = null;
        Log.wtf("IN Back:", "doing....");
        if(!DLUtils.ConnectionDetector.isInternetAvailable(context))
            return context.getResources().getString(R.string.notConnectedToInternetMessage);
        try {
            URL url = new URL(downloadInfo.getUrl());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            File path = new File(downloadInfo.getSavePath());
            if(!path.exists())
                path.mkdirs();
            File file = new File(downloadInfo.getFullPath());
            if(file.exists()){
                if(file.length() == downloadInfo.getFileSize())
                    return context.getResources().getString(R.string.alreadyDownloadedMessage);
                else if(file.length() < downloadInfo.getFileSize()){
                    connection.setRequestProperty("Range", "bytes=" + file.length() + "-");
                    fos = new FileOutputStream(file, true);
                    String percent = String.valueOf(file.length() / downloadInfo.getFileSize());
                    String totalRead = String.valueOf(file.length());
                    publishProgress(percent, totalRead);
                }/*else if(file.length() > downloadInfo.getFileSize()){
                    return context.getResources().getString(R.string.corruptedFileMessage);
                }*/
            }else{
                file.createNewFile();
                fos = new FileOutputStream(file, false);
            }
            byte[] buffer = new byte[BUFFER];
            int count = 0;
            long totalSize = downloadInfo.getFileSize();
            long totalRead = file.length();
            downloadInfo.isDownloading = true;
            connection.connect();
            InputStream in = connection.getInputStream();
            while((count = in.read(buffer)) > 0 && !isCancelled()){
                fos.write(buffer, 0, count);
                fos.flush();
                totalRead += count;
                String progress = String.valueOf((int)(totalRead * 100 / totalSize));
                publishProgress(progress , FileUtils.getReadableSize(totalRead));
            }
            if(file.length() == downloadInfo.getFileSize())
                isCompleted = true;
            if(fos != null) fos.close();
            if(in != null) in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }

        downloadInfo.isDownloading = false;
        if(isCompleted) {
            downloadInfo.isComplete = true;
            return context.getResources().getString(R.string.doneMessage);
        } else
            return context.getResources().getString(R.string.pauseMessage);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        String totalLength = FileUtils.getReadableSize(downloadInfo.getFileSize());
        String percent = values[0];
        String totalRead = values[1];
        downloadBar.progressbar.setProgress(Integer.valueOf(percent));
        downloadBar.tv_percent.setText(percent + "%");
        downloadBar.tv_file_size.setText(totalRead + " / " + totalLength);
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        if(isCompleted){
            downloadBar.btn_resume.setEnabled(false);
            downloadBar.btn_stop.setEnabled(false);
            downloadBar.btn_open.setEnabled(true);
            downloadBar.sendCompleteMessageToUi();
        }
    }

}
