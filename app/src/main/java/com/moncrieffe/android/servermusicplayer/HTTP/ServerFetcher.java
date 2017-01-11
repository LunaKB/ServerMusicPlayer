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

    /* Gets the directories from the server */
    public void getDirectories(){
        mReadDirectory = mRun
                .new ReadDirectory(mUUID, mContext);
        mReadDirectory.run();
    }

    /* Puts the directories into the database */
    public void setDirectories(){
        List<String> strings = mReadDirectory.getList();
        List<Directory> directories = mDirectoryManager.getDirectories();

        /*
            If the directory list is empty, add all received directories
         */
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
                    /* If the string and directory name are the same, do nothing */
                    if (strings.get(i).equals(directories.get(i).getName())) {}
                }
                catch (Exception e){}

                try {
                    /* If the string list is larger than the directory list,
                        add the string to the directory database
                     */
                    if (directories.get(i).equals(null)) {}
                }
                catch (Exception e){
                    mDirectoryManager.addDirectory(new Directory(strings.get(i)));
                }

                try {
                    /* If the directory list is larger than the string list,
                        delete the directory entry from the database
                     */
                    if(strings.get(i).equals(null)){}
                }
                catch (Exception e){
                    mDirectoryManager.deleteDirectory(directories.get(i));
                }

                try {
                    /* If the string and directory name are not the same,
                        delete the directory from the database and add the
                        string
                     */
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

    /* Gets the songs in a specified directory */
    public void getFiles(String directory_name){
        mReadFile = mRun
                .new ReadFile(mUUID, mContext, directory_name);
        mReadFile.run();
    }

    /* Puts the songs into the database */
    public void setFiles(String directory_name){
        List<String> strings = mReadFile.getList();
        List<Song> songList = mSongManager.getSongs(directory_name);
        Credentials c = CredentialsManager.get(mContext).getCredentials(mUUID);

        /* If the song list is empty, add all strings */
        if(songList.size() == 0){
            for (int i = 0; i < strings.size(); i++) {
                String url = formatUrl(c.getWebaddress(), directory_name, strings.get(i));
                Song song = new Song(url, "", directory_name, "", "");//, "");
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
                String url = formatUrl(c.getWebaddress(), directory_name, strings.get(i));
                try {
                    /* If the url is the same as the song url, do nothing */
                    if (url.equals(songList.get(i).getUrl())) {}
                }
                catch (Exception e){}

                try {
                    /* If the song list is larger than the strings list,
                        add the song url to the database
                     */
                    if (songList.get(i).equals(null)) {}
                }
                catch (Exception e){
                    Song song = new Song(url, "", directory_name, "", "");//, "");
                    mSongManager.addSong(song);
                }

                try {
                    /* If the string list is larger than the song list,
                        delete the song from the database
                     */
                    if(strings.get(i).equals(null)){}
                }
                catch (Exception e){
                    mSongManager.deleteSong(songList.get(i));
                }

                try {
                    /* If the url and song url are not the same,
                        delete the song from the database
                         and add the url
                     */
                    if(!url.equals(songList.get(i).getUrl())){
                        mSongManager.deleteSong(songList.get(i));

                        Song song = new Song(url, "", directory_name, "", "");//, "");
                        mSongManager.addSong(song);
                    }
                }
                catch (Exception e){}
            }
        }
    }

    /* Returns a complete url string for the file, assumes webAddress ends with a / */
    private String formatUrl(String webAddress, String directoryName, String fileName){
        return webAddress + directoryName + "/" + fileName;
    }
}
