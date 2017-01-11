package com.moncrieffe.android.servermusicplayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.moncrieffe.android.servermusicplayer.HTTP.ServerFetcher;
import com.moncrieffe.android.servermusicplayer.Song.Song;
import com.moncrieffe.android.servermusicplayer.Song.SongDownloader;
import com.moncrieffe.android.servermusicplayer.Song.SongManager;
import com.squareup.picasso.Picasso;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Chaz-Rae on 9/6/2016.
 * RecyclerView for list of songs only
 * Media controller shows at bottom of RecyclerView
 * AsyncTask gets list of songs
 */
public class MusicListFragment extends Fragment {
    private static final String ARG_DIRECTORY = "directory";
    private static final String ARG_ID = "id";

    private String mDirectory;
    private RecyclerView mRecyclerView;
    private List<Song> mSongList = new ArrayList<>();
    private Callbacks mCallbacks;

    private ServerFetcher mServerFetcher;
    private ProgressDialog progressDialog;
    private SongDownloader<FileHolder> mSongDownloader;
    private MusicService mMusicSrv;

    public interface Callbacks{
        void onSongSelected(Song song);
    }

    /* Gets a new MusicListFragment for the songs in the specified directory */
    public static MusicListFragment newInstance(String directory, UUID id){
        Bundle args = new Bundle();
        args.putString(ARG_DIRECTORY, directory);
        args.putSerializable(ARG_ID, id);

        MusicListFragment fragment = new MusicListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /* Methods from support.v4.fragment */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDirectory = getArguments().getString(ARG_DIRECTORY);
        UUID id = (UUID)getArguments().getSerializable(ARG_ID);
        mServerFetcher = new ServerFetcher(id, getActivity());


        new FetchItemsTask().execute();

        Handler responseHandler = new Handler();
        mSongDownloader = new SongDownloader<>(responseHandler, getActivity());
        mSongDownloader.setSongDownloadListener(
                new SongDownloader.SongDownloadListener<FileHolder>(){

                    @Override
                    public void onSongDownloaded(FileHolder fileHolder, Song song) {
                        SongManager.get(getActivity()).updateSong(song);
                        mSongList = SongManager.get(getActivity()).getSongs(mDirectory);
                        fileHolder.bindFile(song.getTitle(), song.getArtist(), song.getAlbum(), song.getUrl());
                        fileHolder.setSongs(mSongList);
                    }
                }
        );
        mSongDownloader.start();
        mSongDownloader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.music_list_recyclerview);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return view;
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

    /* Recycler View Holder and Adapter */
    private class FileHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        private TextView mSongTitle;
        private TextView mSongArtist;
        private TextView mSongAlbum;
        private List<Song> mSongs;

        public void setSongs(List<Song> songs){
            mSongs = songs;
        }

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

                    FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
                    String songUrl = artwork.replace(" ", "%20");
                    mmr.setDataSource(songUrl);
                    byte[] artBytes = mmr.getEmbeddedPicture();
                    if(artBytes!=null){
                        Bitmap bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                        mImageView.setImageBitmap(bm);
                    }
                    mmr.release();

              /*      try {
                        Picasso.with(getActivity())
                                .load(artwork)
                                .resize(width, height)
                                .into(mImageView);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    } */
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        @Override
        public void onClick(View v) {
            try {
                int position = searchSongs(mSongTitle.getText().toString(), mSongs);
                mCallbacks.onSongSelected(mSongs.get(position));
            }
            catch (Exception e){
                Toast.makeText(
                        getActivity(),
                        "Error playing " + mSongTitle.getText().toString(),
                        Toast.LENGTH_SHORT)
                        .show();
                e.printStackTrace();
            }
        }
    }

    private int searchSongs(String title, List<Song> songs){
        for(Song s:songs){
            if(s.getTitle().equals(title)){
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
            String songTitle = mStrings.get(position).getTitle();
            String artistname = mStrings.get(position).getArtist();
            String albumname = mStrings.get(position).getAlbum();
            String artwork = mStrings.get(position).getUrl();

            holder.bindFile(songTitle, artistname, albumname, artwork);
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
