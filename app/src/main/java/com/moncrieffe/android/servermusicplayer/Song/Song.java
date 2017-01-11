package com.moncrieffe.android.servermusicplayer.Song;

/**
 * Created by Chaz-Rae on 9/13/2016.
 */
public class Song {
    private String mUrl;
    private String mTitle;
    private String mDirectory;
    private String mArtist;
    private String mAlbum;

    public Song(String url, String title, String directory, String artist, String album){
        mUrl = url;
        mTitle = title;
        mDirectory = directory;
        mArtist = artist;
        mAlbum = album;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDirectory() {
        return mDirectory;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }
}
