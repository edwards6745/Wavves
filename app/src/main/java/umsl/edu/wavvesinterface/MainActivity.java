package umsl.edu.wavvesinterface;


import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import umsl.edu.wavvesinterface.database.Audio;
import umsl.edu.wavvesinterface.database.Song;

public class MainActivity extends AppCompatActivity implements SongListFragment.SongListingViewDataSource {

    private Audio mAudio;
    private MediaPlayerService player;
    private String[] mPermissions;
    private int mRequestCode;
    private int[] mGrantResults;
    private ArrayList<Song> songList;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final String Broadcast_PLAY_NEW_AUDIO = "umsl.edu.wavvesinterface.audioplayer.PlayNewAudio";
    private Song mSongData;
    boolean serviceBound = false;
    private SongListFragment mSongFrag;
    private SongModel mSongModel;
    private ViewPager mPager;

    @Override
    public ArrayList<Song> getAudio() {
        return mSongModel.getAudio();
    }

    private SongListHolder mSongHolder;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions(this, mPermissions, mRequestCode);
        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createPageView();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        createPageView();
    }

    public void createPageView() {
        setContentView(R.layout.activity_main);
        if(mSongModel == null) {
            mSongModel = new SongModel(this);
        }

        if(mSongFrag == null) {
            mSongFrag = new SongListFragment();
        }
        mSongFrag.setDataSource(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        songList = new ArrayList<>();
        loadAudio();
        setupViewPager(mPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(mPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mAudio = new Audio();
    }

    private void setupViewPager(ViewPager viewPager) {
        SongAdapter adapter = new SongAdapter(songList, this, getSupportFragmentManager());
        adapter.addFrag(new SongListFragment(ContextCompat.getColor(this, R.color.cardview_dark_background)), "Songs");
        adapter.addFrag(new SongListFragment(ContextCompat.getColor(this, R.color.cardview_dark_background)), "Artists");
        adapter.addFrag(new SongListFragment(ContextCompat.getColor(this, R.color.cardview_dark_background)), "Albums");
        viewPager.setAdapter(adapter);
    }

    public void requestPermissions(Activity thisActivity, String[] mPermissions, int mRequestCode) {
        if (ContextCompat.checkSelfPermission(thisActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Save to audioList
                songList.add(new Song(data, title, album, artist));
            }
        }
        cursor.close();
    }
}
