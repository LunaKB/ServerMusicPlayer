package com.moncrieffe.android.servermusicplayer.Database;

/**
 * Created by Chaz-Rae on 9/7/2016.
 */
public class DbSchema {
    public static final class CredentialsTable{
        public static final String NAME = "credentials";

        public static final class Cols{
            public static final String ID = "id";
            public static final String WEBADDRESS = "webaddress";
        }
    }

    public static final class DirectoryTable{
        public static final String NAME = "directory";

        public static final class Cols{
            public static final String DIRECTORY = "directory";
        }
    }

    public static final class SongsTable{
        public static final String NAME = "songs";

        public static final class Cols{
            public static final String URL = "url";
            public static final String DIRECTORY = "directory";
            public static final String NAME = "name";
            public static final String ARTIST = "artist";
            public static final String ALBUM = "album";
            public static final String ARTWORK = "artwork";
        }
    }
}
