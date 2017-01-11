package com.moncrieffe.android.servermusicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.moncrieffe.android.servermusicplayer.Directory.Directory;
import com.moncrieffe.android.servermusicplayer.Directory.DirectoryManager;
import com.moncrieffe.android.servermusicplayer.HTTP.ServerFetcher;
import com.moncrieffe.android.servermusicplayer.Song.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by Chaz-Rae on 9/7/2016.
 *
 */
public class DirectoryMenuActivity extends AppCompatActivity
        implements MediaController.MediaPlayerControl, MusicListFragment.Callbacks{
    private static final String EXTRA_ID = "id";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private UUID mUUID;
    private List<Directory> mDirectories = new ArrayList<>();
    private ServerFetcher mServerFetcher;
    private String mCurrentSubtitle = "";

    private MusicService mMusicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController mController;
    private boolean paused=false, playbackPaused=false;

    public static Intent newIntent(Context context, UUID id){
        Intent i = new Intent(context, DirectoryMenuActivity.class);
        i.putExtra(EXTRA_ID, id);
        return i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_tabs);
        mUUID = (UUID)getIntent().getSerializableExtra(EXTRA_ID);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mDirectories);
        mViewPager = (ViewPager) findViewById(R.id.menu_viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        setController();

        mServerFetcher = new ServerFetcher(
                UUID.fromString("e8eabaf8-de77-4d16-acae-7c7269cc5d5e"),
                DirectoryMenuActivity.this
        );
        new FetchItemsTask().execute();


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused){
            setController();
            paused = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!mMusicSrv.isPng()) {
            mController.hide();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mMusicSrv.isPng()) {
            mMusicSrv.hideController();
        }
        mMusicSrv = null;
        DirectoryMenuActivity.this.stopService(playIntent);
        DirectoryMenuActivity.this.unbindService(musicConnection);
    }

    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            mMusicSrv = binder.getService();
            //pass list
            mMusicSrv.setList(DirectoryMenuActivity.this, mUUID);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    /* Media Player Controller methods */
    private void setController(){
        if(mController == null) {
            mController = new MusicController(DirectoryMenuActivity.this);
        }
        mController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        mController.setMediaPlayer(this);
        mController.setAnchorView(mViewPager);
        mController.setEnabled(true);
    }

    //play next
    private void playNext(){
        mMusicSrv.playNext();
        if(playbackPaused){
            playbackPaused=false;
            setController();
        }

        mCurrentSubtitle = mMusicSrv.getCurrentPlayingSongName();
        updateSubtitle(mCurrentSubtitle);
    }

    //play previous
    private void playPrev(){
        mMusicSrv.playPrev();
        if(playbackPaused){
            playbackPaused=false;
            setController();
        }

        mCurrentSubtitle = mMusicSrv.getCurrentPlayingSongName();
        updateSubtitle(mCurrentSubtitle);
    }

    @Override
    public void start() {
        mMusicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        mMusicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(mMusicSrv!=null && musicBound && mMusicSrv.isPng()) {
            return mMusicSrv.getDur();
        }
        else{
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if(mMusicSrv!=null && musicBound && mMusicSrv.isPng()) {
            return mMusicSrv.getPosn();
        }
        else{
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        mMusicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(mMusicSrv != null && musicBound ){
            if(mMusicSrv.isPng()){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public void updateSubtitle(String subtitle){
        try {
            String fullSubtitle = "Now Playing: " + subtitle;
            AppCompatActivity activity = DirectoryMenuActivity.this;
            //noinspection ConstantConditions
            activity.getSupportActionBar().setSubtitle(fullSubtitle);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSongSelected(Song song) {
        mMusicSrv.setController(mController);
        mMusicSrv.setSong(song);
        mMusicSrv.playSong();
        if(playbackPaused){
            playbackPaused = false;
        }
        updateSubtitle(song.getTitle());
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private List<Directory> mD = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, List<Directory> directories) {
            super(fm);
            mD = directories;
        }

        public void setList(List<Directory> list){
            mD = list;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            return MusicListFragment.newInstance(mD.get(position).getName(), mUUID);
        }

        @Override
        public int getCount() {
            return mD.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                default: return mD.get(position).getName();
            }
        }
    }

    private void setupAdapter(){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mDirectories);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    /* Gets Directories in Background */
    private class FetchItemsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try{
                mServerFetcher.getDirectories();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mServerFetcher.setDirectories();
            mDirectories = DirectoryManager.get(DirectoryMenuActivity.this).getDirectories();
            setupAdapter();
        }
    }
}
