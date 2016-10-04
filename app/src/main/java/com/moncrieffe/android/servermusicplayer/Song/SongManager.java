package com.moncrieffe.android.servermusicplayer.Song;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.moncrieffe.android.servermusicplayer.Database.DbSchema.SongsTable;
import com.moncrieffe.android.servermusicplayer.Database.SongBaseHelper;
import com.moncrieffe.android.servermusicplayer.Database.SongCursorWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaz-Rae on 9/13/2016.
 */
public class SongManager {
    private static SongManager sSongManager;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static SongManager get(Context context){
        if(sSongManager == null){
            sSongManager = new SongManager(context);
        }
        return sSongManager;
    }

    /* Constructor */
    private SongManager(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new SongBaseHelper(mContext).getWritableDatabase();
    }

    /* Add, delete, update */
    public void addSong(Song song){
        ContentValues values = getContentValues(song);
        mDatabase.insert(SongsTable.NAME, null, values);
    }

    public void deleteSong(Song song){
        mDatabase.delete(SongsTable.NAME, SongsTable.Cols.URL + " = ?",
                new String[]{song.getUrl()});
    }

    public void updateSong(Song song){
        String url = song.getUrl();
        ContentValues values = getContentValues(song);

        mDatabase.update(SongsTable.NAME, values,
                SongsTable.Cols.URL + " = ?", new String[]{url});
    }

    /* Getters */
    public List<Song> getSongs(String directory){
        List<Song> songs = new ArrayList<>();
        SongCursorWrapper cursor = querySongs(
                SongsTable.Cols.DIRECTORY + " = ?", new String[]{directory});

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                songs.add(cursor.getSong());
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }
        return songs;
    }

    public Song getSong(String url){
        SongCursorWrapper cursor = querySongs(
                SongsTable.Cols.URL + " = ?",
                new String[]{url}
        );

        try{
            if(cursor.getCount() == 0){
                return null;
            }

            cursor.moveToFirst();
            return cursor.getSong();
        }
        finally {
            cursor.close();
        }
    }

    /* SQLiteDatabase Methods */
    private static ContentValues getContentValues(Song song){
        ContentValues values = new ContentValues();
        values.put(SongsTable.Cols.URL, song.getUrl());
        values.put(SongsTable.Cols.TITLE, song.getTitle());
        values.put(SongsTable.Cols.DIRECTORY, song.getDirectory());
        values.put(SongsTable.Cols.ARTIST, song.getArtist());
        values.put(SongsTable.Cols.ALBUM, song.getAlbum());
        values.put(SongsTable.Cols.ARTWORK, song.getArtwork());

        return values;
    }

    private SongCursorWrapper querySongs(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                SongsTable.NAME,
                null, // Columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new SongCursorWrapper(cursor);
    }
}