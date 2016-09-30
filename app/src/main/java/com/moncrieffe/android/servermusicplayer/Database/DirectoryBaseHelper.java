package com.moncrieffe.android.servermusicplayer.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.moncrieffe.android.servermusicplayer.Database.DbSchema.DirectoryTable;

/**
 * Created by Chaz-Rae on 9/11/2016.
 */
public class DirectoryBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "directoryBase.db";

    public DirectoryBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DirectoryTable.NAME+ "(" +
                "_id integer primary key autoincrement, " +
                DirectoryTable.Cols.DIRECTORY + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
