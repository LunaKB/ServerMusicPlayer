package com.moncrieffe.android.servermusicplayer.Database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.moncrieffe.android.servermusicplayer.Credentials.Credentials;
import com.moncrieffe.android.servermusicplayer.Database.DbSchema.CredentialsTable;

import java.util.UUID;

/**
 * Created by Chaz-Rae on 9/7/2016.
 */
public class CredentialsCursorWrapper extends CursorWrapper {
    public CredentialsCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Credentials getCredentials(){
        String id = getString(getColumnIndex(CredentialsTable.Cols.ID));
        String webaddress = getString(getColumnIndex(CredentialsTable.Cols.WEBADDRESS));

        Credentials c = new Credentials(UUID.fromString(id), webaddress);
        return c;

    }
}