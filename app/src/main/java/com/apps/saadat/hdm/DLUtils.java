package com.apps.saadat.hdm;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


import static com.apps.saadat.hdm.MainActivity.DEFAULT_PATH;

public class DLUtils extends Observable {

    private  Download download;

    @SuppressLint("HandlerLeak")


    public  void addNewDownload(final Context context, Observer observer, String saveListName, final List<Download> downloads){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_newdownload);
//        dialog.setCancelable(true);
        dialog.setTitle(R.string.newdownload);
        dialog.show();
        Button btn_browse = dialog.findViewById(R.id.btn_browes);
        final Button btn_ok = dialog.findViewById(R.id.btn_ok);
        final Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        final EditText input_url = ((TextInputLayout) dialog.findViewById(R.id.input_url)).getEditText();
        final EditText input_path = ((TextInputLayout)dialog.findViewById(R.id.input_path)).getEditText();
        final ProgressBar pb = dialog.findViewById(R.id.cb_validity);
        final CheckBox cb_auto_down = dialog.findViewById(R.id.auto_download_checkbox);
        addObserver((Observer) observer);
        pb.setVisibility(View.GONE);
        btn_ok.setEnabled(false);
        input_path.setText(DEFAULT_PATH);
        btn_ok.setEnabled(false);

        pasteUrlFromClipboard(context, input_url);
        final String url = input_url.getText().toString().trim();
        if(!url.isEmpty()){
            if ((isLinkValid(url))) {                                           // checking validity of url
                download = new Download(url);
                btn_ok.setEnabled(true);
            }else {                                                            // url is invalid
                btn_ok.setEnabled(false);
                pb.setVisibility(View.GONE);
                Toast.makeText(context, R.string.addressIsInvalid, Toast.LENGTH_SHORT).show();
            }
        }
        input_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //

            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String url = input_url.getText().toString().trim();
                if (url.isEmpty()) {
                    input_url.setError("enter url");
                } else {
                    pb.setVisibility(View.VISIBLE);
                    if ((isLinkValid(url))) {                                           // checking validity of url
                        btn_ok.setEnabled(true);
                        pb.setVisibility(View.GONE);
                    }else {                                                            // url is invalid
                        btn_ok.setEnabled(false);
                        pb.setVisibility(View.GONE);
                        Toast.makeText(context, R.string.addressIsInvalid, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String savepath = input_path.getText().toString();
                download = new Download(url);
                download = DLUtils.getDownloadInfoFromUrl(download.url);
//                if(!isDuplicate(download, downloads)) {
                boolean autoDown = cb_auto_down.isChecked();
                if(autoDown) download.isAutoDownload = true;
                downloads.add(download);
                setChanged();
                DLUtils.this.notifyObservers();
                dialog.dismiss();
//                }else{
//                    Toast.makeText(context, R.string.fileIsDuplicate, Toast.LENGTH_SHORT).show();
//                    dialog.dismiss();
//                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dialog.isShowing())
                    dialog.dismiss();
            }
        });

        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FileChooser chooser = new FileChooser(context);
                chooser.show();
                chooser.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        input_path.setText(chooser.finalDir);
                    }
                });
            }
        });

    }

    private void pasteUrlFromClipboard(Context context, EditText editText) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.getText() != null) {
                editText.setText(clipboard.getText());
            }
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if(clipboard == null) return;
            if(clipboard.hasPrimaryClip()) {
                if(clipboard.getPrimaryClip().getItemAt(0) == null) return;
                android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                if (item.getText() != null) {
                    editText.setText(item.getText());
                }
            }
        }
    }

    private static boolean isDuplicate(Download download, List<Download> downloads) {
        for(Download d : downloads){
            if(d.equals(download))
                return true;
        }
        return false;
    }


    public  boolean isLinkValid(String strUrl) {
        return URLUtil.isValidUrl(strUrl);
    }

    public static Download getDownloadInfoFromUrl(final String strUrl){
        final Download info = new Download(strUrl);
        MyThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(strUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            Log.wtf("response code", "res:" + connection.getResponseCode());
                    connection.connect();
                    info.setFileSize(connection.getContentLength());
                    info.setFilename(URLUtil.guessFileName(strUrl, null, null));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        return info;
    }

    public static class ConnectionDetector {

        public static boolean isInternetAvailable(Context context) {
            return isConnectingToInternet(context) || isConnectingToWifi(context);
        }

        private  static boolean isConnectingToInternet(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }

            }
            return false;
        }

        private static boolean isConnectingToWifi(Context context) {
            ConnectivityManager connManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWifi != null) {
                if (mWifi.getState() == NetworkInfo.State.CONNECTED)
                    return true;
            }
            return false;
        } }
}
