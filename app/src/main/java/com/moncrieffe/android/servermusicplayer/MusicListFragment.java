package com.moncrieffe.android.servermusicplayer;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.moncrieffe.android.servermusicplayer.Credentials.Credentials;
import com.moncrieffe.android.servermusicplayer.Credentials.CredentialsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.moncrieffe.android.servermusicplayer.HTTP.ServerFetcher;
import com.moncrieffe.android.servermusicplayer.Song.Song;
import com.moncrieffe.android.servermusicplayer.Song.SongDownloader;
import com.moncrieffe.android.servermusicplayer.Song.SongManager;
import com.moncrieffe.android.servermusicplayer.MusicService.MusicBinder;
import com.squareup.picasso.Picasso;

/**
 * Created by Chaz-Rae on 9/6/2016.
 * RecyclerView for list of songs only
 * Media controller shows at bottom of RecyclerView
 * AsyncTask gets list of songs
 */
public class MusicListFragment extends Fragment implements MediaController.MediaPlayerControl{
    private static final String ARG_DIRECTORY = "directory";
    private static final String ARG_ID = "id";

    private String mDirectory;
    private UUID mUUID;
    private RecyclerView mRecyclerView;
    private List<Song> mSongList = new ArrayList<>();
    private Credentials mCredentials;

    private MusicService mMusicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController mController;
    private boolean paused=false, playbackPaused=false;

    private ServerFetcher mServerFetcher;
    private ProgressDialog progressDialog;
    private SongDownloader<FileHolder> mSongDownloader;

    /* Gets a new MusicListFragment for the songs in the specified directory */
    public static MusicListFragment newInstance(String directory, UUID id){
        Bundle args = new Bundle();
        args.putString(ARG_DIRECTORY, directory);
        args.putSerializable(ARG_ID, id);

        MusicListFragment fragment = new MusicListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /* Methods from support.v4.fragment */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mDirectory = getArguments().getString(ARG_DIRECTORY);
        mUUID = (UUID)getArguments().getSerializable(ARG_ID);
        mCredentials = CredentialsManager.get(getActivity()).getCredentials(mUUID);
        mServerFetcher = new ServerFetcher(mUUID, getActivity());


        new FetchItemsTask().execute();

        Handler responseHandler = new Handler();
        mSongDownloader = new SongDownloader<>(responseHandler, getActivity());
        mSongDownloader.setSongDownloadListener(
                new SongDownloader.SongDownloadListener<FileHolder>(){

                    @Override
                    public void onSongDownloaded(FileHolder fileHolder, Song song) {
                        SongManager.get(getActivity()).updateSong(song);
                        fileHolder.bindFile(song.getName(), song.getArtist(), song.getAlbum(), song.getArtwork());
                    }
                }
        );
        mSongDownloader.start();
        mSongDownloader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_ftp, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.ftp_recyclerview);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();
        setController();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onPrepareReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));
    }

    @Override
    public void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    public void onStop() {
        mController.hide();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        getActivity().stopService(playIntent);
        mMusicSrv = null;
        mSongDownloader.clearQueue();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSongDownloader.quit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_ftp, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end:
                getActivity().stopService(playIntent);
                mMusicSrv = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /* Above methods for support.v4.fragment */

    // Connects to the music service
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            mMusicSrv = binder.getService();
            //pass list
            mMusicSrv.setList(mSongList, mCredentials.getWebaddress(), mDirectory);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    /* Media Player Controller methods */
    private void setController(){
        if(mController == null) {
            mController = new MusicController(getActivity());
        }
        mController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        mController.setMediaPlayer(this);
        mController.setAnchorView(mRecyclerView);
        mController.setEnabled(true);
    }

    //play next
    private void playNext(){
        mMusicSrv.playNext();
        if(playbackPaused){
            playbackPaused=false;
            setController();
        }
      //  updateSubtitle(mMusicSrv.getCurrentPlayingSongName());
    }

    //play previous
    private void playPrev(){
        mMusicSrv.playPrev();
        if(playbackPaused){
            playbackPaused=false;
            setController();
        }
     //   updateSubtitle(mMusicSrv.getCurrentPlayingSongName());
    }

    @Override
    public void start() {
        mMusicSrv.go();
     //   updateSubtitle(mMusicSrv.getCurrentPlayingSongName());
    }

    @Override
    public void pause() {
        playbackPaused=true;
        mMusicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(mMusicSrv!=null && musicBound && mMusicSrv.isPng()) {
            return mMusicSrv.getDur();
        }
        else{
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if(mMusicSrv!=null && musicBound && mMusicSrv.isPng()) {
            return mMusicSrv.getPosn();
        }
        else{
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        mMusicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(mMusicSrv != null && musicBound){
            return mMusicSrv.isPng();
        }
        else{
            return false;
        }
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
    /* Above methods for Media Player Controller */

    /* Displays name of currently playing song */
    private void updateSubtitle(String subtitle){
        try {
            subtitle = subtitle.replace(".mp3", "");
            String fullSubtitle = "Now Playing: " + subtitle;
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setSubtitle(fullSubtitle);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /* Puts Songs List into RecyclerView */
    private void setupAdapter(){
        if(isAdded()){
            mRecyclerView.setAdapter(new FileAdapter(mSongList));
        }
    }

    /* Gets Songs in Background */
    private class FetchItemsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(),"Loading...",
                    "Loading server files, please wait...", false, false);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try{
                mServerFetcher.getFiles(mDirectory);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mServerFetcher.setFiles(mDirectory);
            mSongList = SongManager.get(getActivity()).getSongs(mDirectory);
            setupAdapter();
            if(progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            // When music player has been prepared, show controller
            mController.show(0);
            updateSubtitle(mMusicSrv.getCurrentPlayingSongName());
        }
    };

    /* Recycler View Holder and Adapter */
    private class FileHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        private TextView mSongTitle;
        private TextView mSongArtist;
        private TextView mSongAlbum;
        private List<Song> mSongs;

        public FileHolder(LayoutInflater inflater, ViewGroup container, List<Song> songs){
            super(inflater.inflate(R.layout.list_item_music, container, false));
            itemView.setOnClickListener(this);
            mImageView = (ImageView)itemView.findViewById(R.id.song_image);
            mSongTitle = (TextView)itemView.findViewById(R.id.song_title);
            mSongArtist = (TextView)itemView.findViewById(R.id.song_artist);
            mSongAlbum = (TextView)itemView.findViewById(R.id.song_album);
            mSongs = songs;
        }

        public void bindFile(String name, String artist, String album, final String artwork){
            mSongTitle.setText(name);
            mSongArtist.setText(artist);
            mSongAlbum.setText(album);

            final ViewTreeObserver observer = mImageView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = mImageView.getWidth();
                    int height = mImageView.getHeight();
                    try {
                        Picasso.with(getActivity())
                                .load(artwork)
                                .resize(width, height)
                                .into(mImageView);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        @Override
        public void onClick(View v) {
            try {
                int position = searchSongs(mSongTitle.getText().toString(), mSongs);

                mMusicSrv.setSong(position);
                mMusicSrv.playSong();
            }
            catch (Exception e){
                Toast.makeText(
                        getActivity(),
                        "Error playing " + mSongTitle.getText().toString(),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private int searchSongs(String name, List<Song> songs){
        for(Song s:songs){
            if(s.getName().equals(name)){
                return songs.indexOf(s);
            }
        }
        return -1;
    }

    private class FileAdapter extends RecyclerView.Adapter<FileHolder>{
        private List<Song> mStrings;

        public FileAdapter(List<Song> files){
            mStrings = files;
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new FileHolder(inflater, parent, mStrings);
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            String filename = mStrings.get(position).getName();
            String artistname = mStrings.get(position).getArtist();
            String albumname = mStrings.get(position).getAlbum();
            String artwork = mStrings.get(position).getArtwork();

            holder.bindFile(filename, artistname, albumname, artwork);
            mSongDownloader.queueSong(holder, mStrings.get(position));
            preloadSong(position);
        }

        private void preloadSong(int position){
            final int songBufferSize = 10;

            int startIndex = Math.max(position - songBufferSize, 0); //Starting index must be >= 0
            int endIndex = Math.min(position + songBufferSize, mStrings.size() - 1); //Ending index must be <= number of galleryItems - 1

            List<Song> songs = mStrings.subList(startIndex, endIndex);
            mSongDownloader.preloadSongs(songs);
        }

        @Override
        public int getItemCount() {
            return mStrings.size();
        }
    }
}
