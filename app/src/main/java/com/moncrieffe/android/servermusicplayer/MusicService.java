package com.moncrieffe.android.servermusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.Toast;

import com.moncrieffe.android.servermusicplayer.Song.Song;
import com.moncrieffe.android.servermusicplayer.Song.SongManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Chaz-Rae on 9/8/2016.
 *
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    private static final int NOTIFY_ID = 1;

    private final IBinder mMusicBind = new MusicBinder();
    private MediaPlayer mMediaPlayer;
    private MediaController mController;
    private AudioManager mAudioManager;

    private SongManager mSongManager;
    private List<Song> mSongs = new ArrayList<>();
    private Song mCurrentSong;

    private String mSongTitle = "";
    private UUID mUUID;
    private int mPosition;


    private Context mContext;

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    private BroadcastReceiver onAudioBecomingNoisy = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
                mController.dispatchKeyEvent(keyEvent);
                mMediaPlayer.pause();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
        initAudioManager();
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(onAudioBecomingNoisy, intentFilter);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (mMediaPlayer != null){
            mMediaPlayer.release();
        }
        unregisterReceiver(onAudioBecomingNoisy);
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
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mController.show(0);

        Intent notIntent = DirectoryMenuActivity.newIntent(this, mUUID);

        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.notification_play)
                .setTicker(mSongTitle)
                .setOngoing(true)
                .setContentTitle("Now Playing")
                .setContentText(mSongTitle);
        Notification not = builder.build();

        not.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFY_ID, not);

   //     Intent onAudioBecomingNoisy = new Intent("AUDIO_BECOMING_NOISY");
   //     LocalBroadcastManager.getInstance(this).sendBroadcast(onAudioBecomingNoisy);
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

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    public void playSong(){
        mMediaPlayer.reset();
        //get song

        if(mSongs.size() == 0 || (!mSongs.get(0).getDirectory().equals(mCurrentSong.getDirectory()))) {
            mSongs = mSongManager.getSongs(mCurrentSong.getDirectory());
        }

        for (Song s : mSongs) {
            if (s.getUrl().equals(mCurrentSong.getUrl())) {
                mPosition = mSongs.indexOf(s);
                break;
            }
        }

        String playUri = mSongs.get(mPosition).getUrl();
        playUri = playUri.replace(" ", "%20");
        mSongTitle = mSongs.get(mPosition).getTitle();

        try {
            mMediaPlayer.setDataSource(playUri);
        }
        catch (Exception e){
            Toast.makeText(this, "Error setting data source", Toast.LENGTH_SHORT).show();
        }
        mMediaPlayer.prepareAsync();
    }

    public void initMusicPlayer(){
        mMediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void initAudioManager(){
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        );

    }

    public void setSong(Song song){
        mCurrentSong = song;
    }

    public void setList(Context context, UUID uuid){
        mSongManager = SongManager.get(context);
        mUUID = uuid;
        mContext = context;
    }

    public String getCurrentPlayingSongName(){
        return mSongs.get(mPosition).getTitle();
    }

    public void setController(MediaController controller){
        mController = controller;
    }

    public void hideController(){
        mController.hide();
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
        mCurrentSong = mSongs.get(mPosition);
        playSong();
    }

    public void playNext(){
        mPosition++;
        if(mPosition >= mSongs.size()){
            mPosition = 0;
        }
        mCurrentSong = mSongs.get(mPosition);
        playSong();
    }


}
