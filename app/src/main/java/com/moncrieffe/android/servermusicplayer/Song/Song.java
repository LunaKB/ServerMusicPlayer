package com.moncrieffe.android.servermusicplayer.Song;

/**
 * Created by Chaz-Rae on 9/13/2016.
 */
public class Song {
    private String mUrl;
    private String mName;
    private String mDirectory;
    private String mArtist;
    private String mAlbum;
    private String mArtwork;

    public Song(String url, String name, String directory, String artist, String album, String artwork){
        mUrl = url;
        mName = name;
        mDirectory = directory;
        mArtist = artist;
        mAlbum = album;
        mArtwork = artwork;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getName() {
        return mName;
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
