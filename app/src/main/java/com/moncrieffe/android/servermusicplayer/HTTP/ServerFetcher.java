package com.moncrieffe.android.servermusicplayer.HTTP;

import android.content.Context;

import com.moncrieffe.android.servermusicplayer.Credentials.Credentials;
import com.moncrieffe.android.servermusicplayer.Credentials.CredentialsManager;
import com.moncrieffe.android.servermusicplayer.Directory.Directory;
import com.moncrieffe.android.servermusicplayer.Directory.DirectoryManager;
import com.moncrieffe.android.servermusicplayer.Song.Song;
import com.moncrieffe.android.servermusicplayer.Song.SongManager;

import java.util.List;
import java.util.UUID;

/**
 * Created by Chaz-Rae on 9/27/2016.
 */
public class ServerFetcher {
    private RunnableFunctions mRun;
    private RunnableFunctions.ReadDirectory mReadDirectory;
    private RunnableFunctions.ReadFile mReadFile;
    private DirectoryManager mDirectoryManager;
    private SongManager mSongManager;
    private UUID mUUID;
    private Context mContext;

    public ServerFetcher(UUID id, Context context){
        mRun = new RunnableFunctions();
        mUUID = id;
        mContext = context;
        mDirectoryManager = DirectoryManager.get(context);
        mSongManager = SongManager.get(context);
    }

    public void getDirectories(){
        mReadDirectory = mRun
                .new ReadDirectory(mUUID, mContext);
        mReadDirectory.run();
    }

    public void setDirectories(){
        List<String> strings = mReadDirectory.getList();
        List<Directory> directories = mDirectoryManager.getDirectories();

        if(directories.size() == 0) {
            for (int i = 0; i < strings.size(); i++) {
                Directory directory = new Directory(strings.get(i));
                mDirectoryManager.addDirectory(directory);
            }
        }
        else{
            int max;

            if(strings.size() >= directories.size()){
                max = strings.size();
            }
            else{
                max = directories.size();
            }

            for(int i = 0; i < max; i++){
                try {
                    if (strings.get(i).equals(directories.get(i).getName())) {}
                }
                catch (Exception e){}

                try {
                    if (directories.get(i).equals(null)) {}
                }
                catch (Exception e){
                    mDirectoryManager.addDirectory(new Directory(strings.get(i)));
                }

                try {
                    if(strings.get(i).equals(null)){}
                }
                catch (Exception e){
                    mDirectoryManager.deleteDirectory(directories.get(i));
                }

                try {
                    if(!strings.get(i).equals(directories.get(i).getName())){
                        mDirectoryManager.deleteDirectory(directories.get(i));

                        Directory directory = new Directory(strings.get(i));
                        mDirectoryManager.addDirectory(directory);
                    }
                }
                catch (Exception e){}

            }
        }
    }

    public void getFiles(String directory_name){
        mReadFile = mRun
                .new ReadFile(mUUID, mContext, directory_name);
        mReadFile.run();
    }

    public void setFiles(String directory_name){
        List<String> strings = mReadFile.getList();
        List<Song> songList = mSongManager.getSongs(directory_name);
        Credentials c = CredentialsManager.get(mContext).getCredentials(mUUID);

        if(songList.size() == 0){
            for (int i = 0; i < strings.size(); i++) {
                String url = c.getWebaddress() + directory_name + "/" + strings.get(i);
                Song song = new Song(url, strings.get(i), directory_name, "", "", "");
                mSongManager.addSong(song);
            }
        }
        else{
            int max;

            if(strings.size() >= songList.size()){
                max = strings.size();
            }
            else{
                max = songList.size();
            }

            for(int i = 0; i < max; i++){
                try {
                    if (strings.get(i).equals(songList.get(i).getName())) {}
                }
                catch (Exception e){}

                try {
                    if (songList.get(i).equals(null)) {}
                }
                catch (Exception e){
                    String url = c.getWebaddress() + directory_name + "/" + strings.get(i);

                    Song song = new Song(url, strings.get(i), directory_name, "", "", "");
                    mSongManager.addSong(song);
                }

                try {
                    if(strings.get(i).equals(null)){}
                }
                catch (Exception e){
                    mSongManager.deleteSong(songList.get(i));
                }

                try {
                    if(!strings.get(i).equals(songList.get(i).getName())){
                        String url = c.getWebaddress() + directory_name + "/" + strings.get(i);
                        mSongManager.deleteSong(songList.get(i));



                        Song song = new Song(url, strings.get(i), directory_name, "", "", "");
                        mSongManager.addSong(song);
                    }
                }
                catch (Exception e){}

            }
        }
    }
}
