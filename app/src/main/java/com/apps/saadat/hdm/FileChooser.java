package com.apps.saadat.hdm;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FileChooser extends Dialog implements View.OnClickListener, ListView.OnItemClickListener {

    private static final int REQ_CODE = 1 ;
    public static String RootDir = Environment.getExternalStorageDirectory().toString();

    static List<String> files;
    File path = new File(RootDir);
    ArrayAdapter<String> adapter;
    private ListView listView;
    private Button btn_select, btn_cancel, btn_sd, btn_inter;
    private TextView tv_cur_dir;
    private ImageView btn_back, btn_forward, btn_newfolder;


    public static String CUR_DIR = RootDir;
    public static String LAST_DIR = CUR_DIR;

    public static String finalDir = CUR_DIR;

    public FileChooser(@NonNull Context context) {
        super(context);
        setContentView(R.layout.layout_filechooser);
        setTitle(context.getText(R.string.chooseADirectory));

        files = new ArrayList<>();

        btn_select = findViewById(R.id.fc_btn_select);
        btn_cancel = findViewById(R.id.fc_btn_cancel);
        btn_sd = findViewById(R.id.fc_btn_sdcard);
        btn_inter = findViewById(R.id.fc_btn_internal_storage);
        btn_back = findViewById(R.id.btn_back);
        btn_forward = findViewById(R.id.btn_forward);
        btn_newfolder = findViewById(R.id.btn_newfolder);
        tv_cur_dir = findViewById(R.id.fc_tv_cur_dir);
        listView = findViewById(R.id.file_chooser_list_view);

        btn_select.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_inter.setOnClickListener(this);
        btn_sd.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        btn_forward.setOnClickListener(this);
        btn_newfolder.setOnClickListener(this);
        listView.setOnItemClickListener(this);

//        tv_cur_dir.setText(CUR_DIR);

        refreshDisplay();
    }



    private void refreshDisplay(){
//        if(listView == null) return;
        path = new File(CUR_DIR);
        File[] listFiles = path.listFiles();
        if(listFiles == null || path.isFile()) return;
        files.clear();
        for(int i = 0 ; i < listFiles.length ; i++){
            if(listFiles[i].isDirectory())
                files.add(listFiles[i].getName());
        }
        if(files.isEmpty()) files.add(getContext().getString(R.string.fc_no_directory));
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, files);
        listView.setAdapter(adapter);
        tv_cur_dir.setText(CUR_DIR);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fc_btn_select:
                finalDir = CUR_DIR;
                dismiss();
                break;
            case R.id.fc_btn_cancel:
                dismiss();
                break;
            case R.id.fc_btn_internal_storage:
                CUR_DIR = Environment.getExternalStorageDirectory().toString();
                refreshDisplay();
                break;
            case R.id.fc_btn_sdcard:
//                Intent intent = new Intent();
//                intent.setType("*/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                activity.startActivityForResult(Intent.createChooser(intent, "choose a folder"), REQ_CODE);
                break;
            case R.id.btn_back:
                if(CUR_DIR.length() < 2) break;
                LAST_DIR = CUR_DIR;
                CUR_DIR = CUR_DIR.substring(0, CUR_DIR.lastIndexOf("/"));
                refreshDisplay();
                break;
            case R.id.btn_forward:
                CUR_DIR = LAST_DIR;
                refreshDisplay();
                break;
            case R.id.btn_newfolder:
                final Dialog newFolderDialog = new Dialog(getContext());
                newFolderDialog.setContentView(R.layout.foldername_dialog_input);
                newFolderDialog.setTitle(R.string.createFolder);
                final EditText editText = newFolderDialog.findViewById(R.id.nf_input_name);
                ((Button)newFolderDialog.findViewById(R.id.nf_btn_cancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        newFolderDialog.dismiss();
                    }
                });

                ((Button)newFolderDialog.findViewById(R.id.nf_btn_ok)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = editText.getText().toString().trim();
                        if(name.isEmpty())
                            Toast.makeText(getContext(), "Enter a name for folder!!!", Toast.LENGTH_SHORT).show();
                        else{
                            File file = new File(CUR_DIR, name);
                            if(file.mkdir()) {
                                Toast.makeText(getContext(), "directory created successfully!", Toast.LENGTH_SHORT).show();
                                newFolderDialog.dismiss();
                                refreshDisplay();
                                listView.setSelectionFromTop(adapter.getPosition(name), 0);
                            }
                        }
                    }
                });
                newFolderDialog.show();
                break;

            default:break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(files.get(i).equals("There is no directory here")) return;
        CUR_DIR += "/" + files.get(i);
        LAST_DIR = CUR_DIR;
        refreshDisplay();
    }
}
