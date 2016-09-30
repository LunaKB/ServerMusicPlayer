package com.moncrieffe.android.servermusicplayer.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.moncrieffe.android.servermusicplayer.Database.DbSchema.CredentialsTable;

/**
 * Created by Chaz-Rae on 9/7/2016.
 */
public class CredentialsBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "credentialsBase.db";

    public CredentialsBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + CredentialsTable.NAME+ "(" +
                "_id integer primary key autoincrement, " +
                CredentialsTable.Cols.ID + ", " +
                CredentialsTable.Cols.WEBADDRESS + ")"
        );
    }

    @Override
    public void  onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
