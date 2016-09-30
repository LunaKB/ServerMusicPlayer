package com.moncrieffe.android.servermusicplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;


/**
 * Created by Chaz-Rae on 9/7/2016.
 */
public class DirectoryMenuActivity extends SingleFragmentActivity {
    private static final String EXTRA_ID = "id";

    public static Intent newIntent(Context context, UUID id){
        Intent i = new Intent(context, DirectoryMenuActivity.class);
        i.putExtra(EXTRA_ID, id);
        return i;
    }


    @Override
    protected Fragment createFragment() {
        UUID id = (UUID)getIntent().getSerializableExtra(EXTRA_ID);
        return DirectoryMenuFragment.newInstance(id);
    }
}
