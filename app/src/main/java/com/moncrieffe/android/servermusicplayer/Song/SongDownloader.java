package com.moncrieffe.android.servermusicplayer.Song;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Chaz-Rae on 9/28/2016.
 */
public class SongDownloader<T> extends HandlerThread {
    private static final String TAG = "SongDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRELOAD = 1;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private Handler mRequestCache;
    private ConcurrentMap<T, Song> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private SongDownloadListener<T> mSongDownloadListener;
    private LruCache<String, Song> mCache;

    private Context mContext;
    private ProgressDialog mProgressDialog;

    public interface SongDownloadListener<T>{
        void onSongDownloaded(T target, Song song);
    }

    public void setSongDownloadListener(SongDownloadListener<T> listener){
        mSongDownloadListener = listener;
    }

    public SongDownloader(Handler responseHandler, Context context) {
        super(TAG);
        mResponseHandler = responseHandler;

        int memClass = ( (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE ) ).getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;

        mCache = new LruCache<>(cacheSize);
        mContext = context;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    try {
                        mProgressDialog = ProgressDialog.show(mContext, "Loading...",
                                "Loading file data, please wait...", false, false);
                        T target = (T) msg.obj;
                        Log.i(TAG, "Got a request for: " + mRequestMap.get(target));
                        handleRequest(target);
                        mProgressDialog.dismiss();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };

        mRequestCache = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_PRELOAD){
                    try {
                        List<Song> target = (List<Song>) msg.obj;
                        Log.i(TAG, "Got a request for song list");
                        preloadSongData(target);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueSong(T target, Song song){
        Log.i(TAG, "Got a song " + song.getName());

        if(song == null){
            mRequestMap.remove(target);
        }
        else{
            mRequestMap.put(target, song);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    public void clearQueue(){
        mResponseHandler.removeMessages(MESSAGE_DOWNLOAD);
        mCache.evictAll();
    }

    public void preloadSongs(List<Song> songs){
        mRequestCache.obtainMessage(MESSAGE_PRELOAD, songs)
                .sendToTarget();
    }

    private void handleRequest(final T target){
        try{
            final Song song = mRequestMap.get(target);
            final Song updatedSong;
            if(song == null){
                return;
            }

            updatedSong = downloadSongData(song);
            Log.i(TAG, "Song updated");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target) != song ||mHasQuit){
                        return;
                    }
                    mRequestMap.remove(target);
                    mSongDownloadListener.onSongDownloaded(target, updatedSong);
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private Song downloadSongData(Song song){
        Song updatedSong;

        if(mCache.get(song.getUrl()) != null){
            updatedSong = mCache.get(song.getUrl());
        }
        else {
            FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
            String songUrl = song.getUrl().replace(" ", "%20");
            mmr.setDataSource(songUrl);
            String artist = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);

            String url = song.getUrl().replace(song.getName(), "");
            url = url.replace(" ", "%20");
            String artwork = url + album.replace(" ", "%20") + "-image.jpg";

            mmr.release();


            updatedSong = new Song(song.getUrl(), song.getName(), song.getDirectory(), artist, album, artwork);
            mCache.put(updatedSong.getUrl(), updatedSong);
            Log.i(TAG, "Song updated");
        }

        return updatedSong;
    }

    private void preloadSongData(List<Song> songs){
        for(Song s:songs){
            if(mCache.get(s.getUrl()) == null && mHasQuit){
                FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
                String songUrl = s.getUrl().replace(" ", "%20");
                mmr.setDataSource(songUrl);
                String artist = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
                String album = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);

                String url = s.getUrl().replace(s.getName(), "");
                url = url.replace(" ", "%20");
                String artwork = url + album.replace(" ", "%20") + "-image.jpg";

                mmr.release();


                Song updatedSong = new Song(s.getUrl(), s.getName(), s.getDirectory(), artist, album, artwork);
                mCache.put(updatedSong.getUrl(), updatedSong);
                Log.i(TAG, "Cache preloaded with " + updatedSong.getName());
            }
        }
    }
}
