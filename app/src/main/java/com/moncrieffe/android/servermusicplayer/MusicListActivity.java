package com.moncrieffe.android.servermusicplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class MusicListActivity extends SingleFragmentActivity{
    private static final String EXTRA_DIRECTORY = "directory";
    private static final String EXTRA_ID = "id";


    public static Intent newIntent(Context context, String directory, UUID id){
        Intent i = new Intent(context, MusicListActivity.class);
        i.putExtra(EXTRA_DIRECTORY, directory);
        i.putExtra(EXTRA_ID, id);
        return  i;
    }

    @Override
    protected Fragment createFragment() {
        String mDirectory = getIntent().getStringExtra(EXTRA_DIRECTORY);
        UUID mUUID = (UUID)getIntent().getSerializableExtra(EXTRA_ID);
        return MusicListFragment.newInstance(mDirectory, mUUID);
    }
}
