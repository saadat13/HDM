package com.apps.saadat.hdm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener,NavigationView.OnNavigationItemSelectedListener,View.OnClickListener,SearchView.OnQueryTextListener, android.support.v7.widget.SearchView.OnQueryTextListener {

    public static final int PROCESSING_TAB = 2;
    public static final int QUEUE_TAB = 1;
    public static final int COMPLETED_TAB = 0;

    public static int CURRENT_TAB = PROCESSING_TAB;

    public static final String DEFAULT_PATH = Environment.getExternalStorageDirectory().toString() + "/" +  "HDM";

    ViewPager viewPager;
    TabLayout tabs;
    List<SlideFragment> slides;
    SlideFragmentAdapter adapter;
    ProcessFragment processTab;
    QueueFragment queueTab;
    static CompletedFragment completedTab;
    DrawerLayout drawerLayout;
    NavigationView nav;
    FloatingActionButton fab;
    android.support.v7.widget.SearchView searcher;
    ImageView btn_sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        setLocale(preferences.getString("Lang", "en"));
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        nav = findViewById(R.id.nav_view);
        nav.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.hamber3);
        }

        fab = findViewById(R.id.fab);
        searcher = findViewById(R.id.search_view);
        btn_sort = findViewById(R.id.btn_sort);
        searcher.setOnQueryTextListener(this);



        MyThreadPool.initExecutor();
        MyThreadPool.initSingleExecutor();

        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        tabs = findViewById(R.id.tab_layout);

        slides = new ArrayList<>();

        processTab = ProcessFragment.newSlide("Processing");
        queueTab = QueueFragment.newSlide("Queue");
        completedTab = CompletedFragment.newSlide("Completed");


        slides.add(completedTab);
        slides.add(queueTab);
        slides.add(processTab);


        adapter = new SlideFragmentAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(PROCESSING_TAB);

        tabs.getTabAt(PROCESSING_TAB).setText(R.string.processing);
        tabs.getTabAt(QUEUE_TAB).setText(R.string.queue);
        tabs.getTabAt(COMPLETED_TAB).setText(R.string.completed);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CURRENT_TAB = tabs.getSelectedTabPosition();
                final SlideFragment currentSlide = slides.get(CURRENT_TAB);
                currentSlide.addNewDownload();
            }
        });
    }





    public static void moveToCompleted(DownloadListAdapter.DownloadBar downloadBar){
        completedTab.downloads.add(downloadBar.getDownloadInfo());
        completedTab.refreshDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        MenuItem add = menu.findItem(R.id.om_newDownload);
        MenuItem addFake = menu.findItem(R.id.om_addFake);
        MenuItem startAll = menu.findItem(R.id.om_startAll); // ???
        MenuItem stopAll = menu.findItem(R.id.om_stopAll);
        MenuItem selectAll = menu.findItem(R.id.om_selectAll);
        MenuItem deselectAll = menu.findItem(R.id.om_deselectAll);
        MenuItem invertSelection = menu.findItem(R.id.om_invertSelection);
        MenuItem clear = menu.findItem(R.id.om_clearAll);
        menu.add("youtube").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(MainActivity.this, Youtube.class));
                return false;
            }
        });


        add.setOnMenuItemClickListener(this);
        startAll.setOnMenuItemClickListener(this);
        stopAll.setOnMenuItemClickListener(this);
        selectAll.setOnMenuItemClickListener(this);
        deselectAll.setOnMenuItemClickListener(this);
        invertSelection.setOnMenuItemClickListener(this);
        clear.setOnMenuItemClickListener(this);
        addFake.setOnMenuItemClickListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.btn_import:
                break;
            case R.id.btn_export:
                break;
            case R.id.btn_scheduler:
                break;
            case R.id.btn_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.btn_about:
                AlertDialog.Builder aboutUsDialog = new AlertDialog.Builder(this);
                aboutUsDialog.setTitle(R.string.aboutUs)
                        .setMessage(this.getString(R.string.about_text))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(R.string.confirm, null)
                        .show();
                break;
            default: break;
        }
        drawerLayout.closeDrawers();
        return true;
    }

    /**
     * this method is to handle sort button events
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sort:
                break;
            default:break;
        }
    }

    /**
     * to handle search view events
     * @param keyword
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String keyword) {

        return true;
    }

    @Override
    public boolean onQueryTextChange(String keyword) {
        CURRENT_TAB = tabs.getSelectedTabPosition();
        final SlideFragment currentSlide = slides.get(CURRENT_TAB);
        currentSlide.search(keyword);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class SlideFragmentAdapter extends FragmentPagerAdapter{

        public SlideFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return slides.get(position);
        }

        @Override
        public int getCount() {
            return slides.size();
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();
        CURRENT_TAB = tabs.getSelectedTabPosition();
        final SlideFragment currentSlide = slides.get(CURRENT_TAB);
        switch (id){
            case R.id.om_newDownload:
                currentSlide.addNewDownload();
                break;
            case R.id.om_startAll:
//                Toast.makeText(getApplicationContext(), "size:" + currentSlide.adapter.getDownloadBarsList().size(), Toast.LENGTH_SHORT).show();
                currentSlide.startAll();
                break;
            case R.id.om_stopAll:
                currentSlide.stopAll();
                break;
            case R.id.om_clearAll:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Clear Alert")
                        .setMessage("Do you want to clear all items of " + currentSlide.getSlideTitle() + " tab?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                currentSlide.downloads.clear();
                                currentSlide.downloadBars.clear();
                                currentSlide.refreshDisplay();
                                currentSlide.update(null, null);
                            }
                        }).show();
                break;
            case R.id.om_selectAll:
                currentSlide.selectAll();
                currentSlide.refreshDisplay();
                break;
            case R.id.om_deselectAll:
                currentSlide.deselectAll();
                currentSlide.refreshDisplay();
                break;
            case R.id.om_invertSelection:
                currentSlide.invertSelection();
                currentSlide.refreshDisplay();
                break;
            case R.id.om_addFake:
                currentSlide.addFakeDownload(10);
                currentSlide.refreshDisplay();
                break;
            default:break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode==RESULT_CANCELED)
        {
            // action cancelled
        }
        if(resultCode==RESULT_OK)
        {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        slides.get(PROCESSING_TAB).stopAll();
        slides.get(QUEUE_TAB).stopAll();
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }
}
