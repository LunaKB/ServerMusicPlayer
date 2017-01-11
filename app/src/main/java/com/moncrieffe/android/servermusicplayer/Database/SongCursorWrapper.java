package com.moncrieffe.android.servermusicplayer.Database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.moncrieffe.android.servermusicplayer.Database.DbSchema.SongsTable;
import com.moncrieffe.android.servermusicplayer.Song.Song;

/**
 * Created by Chaz-Rae on 9/13/2016.
 */
public class SongCursorWrapper extends CursorWrapper{
    public SongCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Song getSong(){
        String url = getString(getColumnIndex(SongsTable.Cols.URL));
        String title = getString(getColumnIndex(SongsTable.Cols.TITLE));
        String directory = getString(getColumnIndex(SongsTable.Cols.DIRECTORY));
        String artist = getString(getColumnIndex(SongsTable.Cols.ARTIST));
        String album = getString(getColumnIndex(SongsTable.Cols.ALBUM));

        Song s = new Song(url, title, directory, artist, album);
        return s;
    }
}
