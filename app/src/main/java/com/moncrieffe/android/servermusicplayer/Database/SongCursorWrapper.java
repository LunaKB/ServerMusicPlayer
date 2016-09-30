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
        String name = getString(getColumnIndex(SongsTable.Cols.NAME));
        String directory = getString(getColumnIndex(SongsTable.Cols.DIRECTORY));
        String artist = getString(getColumnIndex(SongsTable.Cols.ARTIST));
        String album = getString(getColumnIndex(SongsTable.Cols.ALBUM));
        String  artwork = getString(getColumnIndex(SongsTable.Cols.ARTWORK));

        Song s = new Song(url, name, directory, artist, album, artwork);
        return s;
    }
}
