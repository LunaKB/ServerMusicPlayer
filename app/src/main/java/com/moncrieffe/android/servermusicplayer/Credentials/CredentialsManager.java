package com.moncrieffe.android.servermusicplayer.Credentials;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.moncrieffe.android.servermusicplayer.Database.CredentialsBaseHelper;
import com.moncrieffe.android.servermusicplayer.Database.CredentialsCursorWrapper;
import com.moncrieffe.android.servermusicplayer.Database.DbSchema.CredentialsTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CredentialsManager {
    private static CredentialsManager sCredentialsManager;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CredentialsManager get(Context context){
        if(sCredentialsManager == null){
            sCredentialsManager = new CredentialsManager(context);
        }
        return sCredentialsManager;
    }

    /* Constuctor */
    private CredentialsManager(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new CredentialsBaseHelper(mContext).getWritableDatabase();
    }

    /* Add, delete, update */
    public void addCredentials(Credentials credentials){
        ContentValues values = getContentValues(credentials);
        mDatabase.insert(CredentialsTable.NAME, null, values);
    }

    public void deleteCredentials(Credentials credentials){
        mDatabase.delete(CredentialsTable.NAME, CredentialsTable.Cols.ID + " = ?",
                new String[] {credentials.getID().toString()});
    }

    public void updateCredentials(Credentials credentials){
        String uuidString = credentials.getID().toString();
        ContentValues values = getContentValues(credentials);

        mDatabase.update(CredentialsTable.NAME, values,
                CredentialsTable.Cols.ID + " = ?", new String[]{uuidString});
    }

    /* Getters */
    public List<Credentials> getCredentials(){
        List<Credentials> customers = new ArrayList<>();

        CredentialsCursorWrapper cursor = queryCustomers(null, null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                customers.add(cursor.getCredentials());
                cursor.moveToNext();
            }
        }
        finally{
            cursor.close();
        }

        return customers;
    }

    public Credentials getCredentials(UUID id){
        CredentialsCursorWrapper cursor = queryCustomers(
                CredentialsTable.Cols.ID + " = ?",
                new String[] {id.toString()}
        );

        try{
            if(cursor.getCount() == 0){
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCredentials();
        }
        finally {
            cursor.close();
        }
    }

    /* SQLiteDatabase Methods */
    private static ContentValues getContentValues(Credentials credentials){
        ContentValues values = new ContentValues();
        values.put(CredentialsTable.Cols.ID, credentials.getID().toString());
        values.put(CredentialsTable.Cols.WEBADDRESS, credentials.getWebaddress());

        return values;
    }

    private CredentialsCursorWrapper queryCustomers(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CredentialsTable.NAME,
                null, // Columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new CredentialsCursorWrapper(cursor);
    }
}
