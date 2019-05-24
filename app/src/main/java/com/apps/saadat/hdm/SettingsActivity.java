package com.apps.saadat.hdm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    SharedPreferences.Editor editor;
    SharedPreferences preferences;
    // variables
    boolean isVibrationOn = true;
    String defLang;
    String defSavePath;
    String[] langs = new String[]{"", ""};

    //
    Switch sw_vibration;
    SeekBar sb_simul;
    Spinner sp_lang;
    Button btn_apply;
    Button btn_browse;
    EditText input_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        initUiObjects();
        loadSettings();
    }


    private void initUiObjects(){
        sp_lang = findViewById(R.id.sp_lang);
        refreshLangList();
        btn_apply = findViewById(R.id.btn_apply);
        btn_browse = findViewById(R.id.btn_browse);
        input_path  = findViewById(R.id.st_input_path);
        sw_vibration = findViewById(R.id.sw_vibration);
        btn_browse.setOnClickListener(this);
        btn_apply.setOnClickListener(this);
        sp_lang.setOnItemSelectedListener(this);
    }

    private void refreshLangList(){
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, langs);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        sp_lang.setAdapter(adapter);
    }

    private void edit(){
        editor.putString("Lang", defLang);
        editor.putString("SavePath", defSavePath);
        editor.putBoolean("isVibrationOn", isVibrationOn);
        editor.apply();
    }


    private void init(){
        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        editor = preferences.edit();
    }

    private void loadSettings() {

        defLang =  preferences.getString("Lang", Locale.getDefault().getDisplayLanguage());
        defSavePath =  preferences.getString("SavePath", Environment.getExternalStorageDirectory().toString() + "/HDM");
        isVibrationOn = preferences.getBoolean("isVibrationOn", false);

        if(defLang.equals("fa")) {
            langs = new String[]{"انگلیسی", "فارسی"};
            sp_lang.scrollTo(1, 0);
        } else {
            langs = new String[]{"English", "Persian"};
            sp_lang.scrollTo(0, 0);
        }
        refreshLangList();
        input_path.setText(defSavePath);
        sw_vibration.setChecked(isVibrationOn);

    }
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_apply:
                if(!defLang.equals(langs[sp_lang.getSelectedItemPosition()])) {
                    AlertDialog.Builder apply_alert = new AlertDialog.Builder(this);
                    apply_alert.setTitle(this.getString(R.string.apply_alert_title))
                            .setMessage(SettingsActivity.this.getResources().getString(R.string.apply_alert))
                            .setNegativeButton(this.getString(R.string.cancel), null)
                            .setPositiveButton(this.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    edit();
                                    setLocale(defLang);
                                    restartApp();
                                    Toast.makeText(SettingsActivity.this, getResources().getText(R.string.applySuccessMessage), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                }else {
                    edit();
                    Toast.makeText(SettingsActivity.this, getResources().getText(R.string.applySuccessMessage), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_browse:
                final FileChooser fc = new FileChooser(this);
                fc.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        input_path.setText(fc.finalDir);
                    }
                });
                fc.show();
                break;
            default:break;
        }
    }


    public void restartApp(){
        Intent i = this.getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if(position == 0){
            defLang = "en";
        }else{
            defLang = "fa";
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
