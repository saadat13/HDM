package com.apps.saadat.hdm;



import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static final String SAVED_DOWNLOAD_LIST_NAME = "downloadList";
    public static final String SAVED_QUEUE_LIST_NAME = "queueList";
    public static final String SAVED_COMPLETED_LIST_NAME = "completedList";


    public static final String getReadableSize(long size){
        if(size < 1024)
            return String.valueOf(size);
        else if(size < 1024 * 1024)
            return String.format("%.2f KB", (float)size / 1024);
        else if(size < 1024 * 1024 * 1024)
            return String.format("%.2f MB", (float)size / (1024 * 1024));
        else if(size < (long)1024 * 1024 * 1024 * 1024)
            return String.format("%.2f GB", (float)size / (long)((1024 * 1024 * 1024)));
        else if(size < (long)1024 * 1024 * 1024 * 1024 * 1024)
            return String.format("%.2f TB", (float)size / ((long)1024 * 1024 * 1024 * 1024));
        else
            return String.valueOf(size);
    }

    public static void viewFile(Context context, String filePath) {
        Uri uri = Uri.parse("file://" + filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String dataAndType = getIntentDataAndType(filePath);
        intent.setDataAndType(uri, dataAndType);
//        intent.putExtra(Intent.EXTRA_TITLE, title);
        // Verify that the intent will resolve to an activity
        if (intent.resolveActivity(context.getApplicationContext().getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
//            Toast.makeText(SecondActivity.this, "No Application found", Toast.LENGTH_SHORT).show();
        }
    }


    private static String getIntentDataAndType(String filePath) {
        String exten = "";
        int i = filePath.lastIndexOf('.');
        // If the index position is greater than zero then get the substring.
        if (i > 0) {
            exten = filePath.substring(i + 1);
        }
        String mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(exten);
        mimeType = (mimeType == null) ? "*/*" : mimeType;
        return mimeType;
    }

    public static void saveDownloadsList(Context context,String fileName ,List<Download> downloadList){

        final String ROOT_PATH = context.getFilesDir().getPath();
        File path = new File(ROOT_PATH, fileName);
        if(path.exists()){
            path.delete();
        }

        for(Download download: downloadList){
            try(BufferedWriter writer =
                        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), StandardCharsets.UTF_8))){
                writer.append(download.getUrl());                           // URL
                writer.append(",");
                writer.append(download.getFilename());                      // NAME
                writer.append(",");
                writer.append(download.getSavePath());                      //PATH
                writer.append(",");
                writer.append(String.valueOf(download.getFileSize()));      //SIZE
                writer.append(",");
                writer.append(download.getDate());                          //DATE
                writer.newLine();
                writer.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static List<Download> loadDownloadsList(Context context, String fileName){
        List<Download> downloadList = new ArrayList<>();
        if(context == null) {
            Log.wtf("contex", "is nullllll");
            Log.println(Log.ERROR, "CONTEXT:", "CONTEXT IS NULL !!!!!");
            return downloadList;
        }
        final String ROOT_PATH = context.getFilesDir().getPath();
        File path = new File(ROOT_PATH, fileName);
        if(!path.exists())
            return null;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))){
            while(reader.ready()) {
                String line = reader.readLine();
                String[] args = line.split(",");
                Download download;

                String url = args[0];
                String name = args[1];
                String dlSavePath = args[2];
                String date = args[4];
                long size = Long.valueOf(args[3]);

                download = new Download(url);
                download.setFilename(name);
                download.setSavePath(dlSavePath);
                download.setFileSize(size);
                download.setDate(date);
                downloadList.add(download);
            }
        }catch (IOException | ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }

        return downloadList;
    }



}
