package com.moncrieffe.android.servermusicplayer.Directory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.moncrieffe.android.servermusicplayer.Database.DbSchema.DirectoryTable;
import com.moncrieffe.android.servermusicplayer.Database.DirectoryBaseHelper;
import com.moncrieffe.android.servermusicplayer.Database.DirectoryCursorWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaz-Rae on 9/11/2016.
 */
public class DirectoryManager {
    private static DirectoryManager sDirectoryManager;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static DirectoryManager get(Context context){
        if(sDirectoryManager == null){
            sDirectoryManager = new DirectoryManager(context);
        }
        return sDirectoryManager;
    }

    /* Constuctor */
    private DirectoryManager(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new DirectoryBaseHelper(mContext).getWritableDatabase();
    }

    /* Add, delete, update */
    public void addDirectory(Directory directory){
        ContentValues values = getContentValues(directory);
        mDatabase.insert(DirectoryTable.NAME, null, values);
    }

    public void deleteDirectory(Directory directory){
        mDatabase.delete(DirectoryTable.NAME, DirectoryTable.Cols.DIRECTORY + " = ?",
                new String[] {directory.getName()});
    }

    public void updateDirectory(Directory directory){
        String name = directory.getName();
        ContentValues values = getContentValues(directory);

        mDatabase.update(DirectoryTable.NAME, values,
                DirectoryTable.Cols.DIRECTORY + " = ?", new String[]{name});
    }

    /* Getters */
    public List<Directory> getDirectories(){
        List<Directory> directories = new ArrayList<>();

        DirectoryCursorWrapper cursor = queryDirectory(null, null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                directories.add(cursor.getDirectory());
                cursor.moveToNext();
            }
        }
        finally{
            cursor.close();
        }

        return directories;
    }

    public Directory getDirectory(String name){
        DirectoryCursorWrapper cursor = queryDirectory(
                DirectoryTable.Cols.DIRECTORY + " = ?",
                new String[] {name}
        );

        try{
            if(cursor.getCount() == 0){
                return null;
            }

            cursor.moveToFirst();
            return cursor.getDirectory();
        }
        finally {
            cursor.close();
        }
    }


    /* SQLiteDatabase Methods */
    private static ContentValues getContentValues(Directory directory){
        ContentValues values = new ContentValues();
        values.put(DirectoryTable.Cols.DIRECTORY, directory.getName());

        return values;
    }

    private DirectoryCursorWrapper queryDirectory(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                DirectoryTable.NAME,
                null, // Columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new DirectoryCursorWrapper(cursor);
    }
}
