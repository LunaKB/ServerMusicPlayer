package com.moncrieffe.android.servermusicplayer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moncrieffe.android.servermusicplayer.Directory.Directory;
import com.moncrieffe.android.servermusicplayer.Directory.DirectoryManager;
import com.moncrieffe.android.servermusicplayer.HTTP.ServerFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Chaz-Rae on 9/11/2016.
 */
public class DirectoryMenuFragment extends Fragment {
    private static final String ARG_ID = "id";
    private UUID mUUID;
    private RecyclerView mRecyclerView;
    private List<Directory> mDirectories = new ArrayList<>();
    private ServerFetcher mServerFetcher;

    public static DirectoryMenuFragment newInstance(UUID id){
        Bundle args = new Bundle();
        args.putSerializable(ARG_ID, id);

        DirectoryMenuFragment fragment = new DirectoryMenuFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ftp, container, false);

        mRecyclerView = (RecyclerView)v.findViewById(R.id.ftp_recyclerview);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUUID = (UUID)getArguments().getSerializable(ARG_ID);
        mServerFetcher = new ServerFetcher(
                UUID.fromString("e8eabaf8-de77-4d16-acae-7c7269cc5d5e"),
                getActivity()
        );
        new FetchItemsTask().execute();
    }

    /* Puts Directories List into RecyclerView */
    private void setupAdapter(){
        if(isAdded()){
            mRecyclerView.setAdapter(new FileAdapter(mDirectories));
        }
    }

    /* Gets Directories in Background */
    private class FetchItemsTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                mServerFetcher.getDirectories();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mServerFetcher.setDirectories();
            mDirectories = DirectoryManager.get(getActivity()).getDirectories();
            setupAdapter();
        }
    }

    /* Recycler View Holder and Adapter */
    private class FileHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mTextView;

        public FileHolder(LayoutInflater inflater, ViewGroup container){
            super(inflater.inflate(R.layout.list_item_file, container, false));
            mTextView = (TextView) itemView.findViewById(R.id.list_item_directory);
            mTextView.setOnClickListener(this);
        }

        public void bindFile(String name){
            mTextView.setText(name);
        }

        @Override
        public void onClick(View v) {
            String directoryName = mTextView.getText().toString();
            Intent i = MusicListActivity
                    .newIntent(getActivity(),
                            directoryName,
                            UUID.fromString("e8eabaf8-de77-4d16-acae-7c7269cc5d5e"));
            startActivity(i);
        }
    }

    private class FileAdapter extends RecyclerView.Adapter<FileHolder>{
        private List<Directory> mStrings;

        public FileAdapter(List<Directory> files){
            mStrings = files;
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new FileHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            String filename = mStrings.get(position).getName();
            holder.bindFile(filename);
        }

        @Override
        public int getItemCount() {
            return mStrings.size();
        }
    }
}
