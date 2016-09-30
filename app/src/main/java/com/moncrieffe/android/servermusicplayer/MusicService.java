package com.moncrieffe.android.servermusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.moncrieffe.android.servermusicplayer.Song.Song;

import java.util.List;

/**
 * Created by Chaz-Rae on 9/8/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final int NOTIFY_ID = 1;
    private final IBinder mMusicBind = new MusicBinder();
    private MediaPlayer mMediaPlayer;
    private List<Song> mSongs;
    private int mPosition;
    private String mUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        mPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        // Broadcast intent to activity to let it know the media player has been prepared
        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mMediaPlayer.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }



    public void playSong(){
        mMediaPlayer.reset();
        //get song
        String playUri = mUrl + mSongs.get(mPosition).getName();
        playUri = playUri.replace(" ", "%20");
        try {
            mMediaPlayer.setDataSource(playUri);
        }
        catch (Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mMediaPlayer.prepareAsync();
    }

    public void setSong(int songIndex){
        mPosition = songIndex;
    }

    public void initMusicPlayer(){
        mMediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setList(List<Song> songs, String webAddress, String directory){
        mSongs = songs;
        mUrl = webAddress + directory + "/";
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    /* Media Controller methods*/
    public int getPosn(){
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mMediaPlayer.getDuration();
    }

    public boolean isPng(){
        return mMediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mMediaPlayer.pause();
    }

    public void seek(int posn){
        mMediaPlayer.seekTo(posn);
    }

    public void go(){
        mMediaPlayer.start();

    }

    public void playPrev(){
        mPosition--;
        if(mPosition < 0){
            mPosition = mSongs.size()-1;
        }
        playSong();
    }

    public void playNext(){
        mPosition++;
        if(mPosition >= mSongs.size()){
            mPosition = 0;
        }
        playSong();
    }

    public String getCurrentPlayingSongName(){
        return mSongs.get(mPosition).getName();
    }
}
