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
    private String mArtwork;

    public Song(String url, String title, String directory, String artist, String album, String artwork){
        mUrl = url;
        mTitle = title;
        mDirectory = directory;
        mArtist = artist;
        mAlbum = album;
        mArtwork = artwork;
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

    public String getArtwork() {
        return mArtwork;
    }
}
