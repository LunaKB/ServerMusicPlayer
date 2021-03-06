package com.moncrieffe.android.servermusicplayer.Credentials;

import java.util.UUID;

public class Credentials {
    private UUID mID;
    private String mWebaddress;

    public Credentials(String  web){
        mID = UUID.fromString("e8eabaf8-de77-4d16-acae-7c7269cc5d5e");
        mWebaddress = web;
    }

    public Credentials(UUID id, String web){
        mID = id;
        mWebaddress = web;
    }

    public UUID getID() {
        return mID;
    }

    public String getWebaddress() {
        return mWebaddress;
    }

}
