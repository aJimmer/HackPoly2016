package com.inasweaterpoorlyknit.hackpoly2016;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
*   PlaylistFragment displays a playlist of songTitles and songThumbnails
*   This is used for the playlistFragment AND historyFragment of the ServerLobby
**/

public class PlaylistFragment extends Fragment {

    private ListView playlistListView; // list view to display the playlist
    private PlaylistAdapter playlistAdapter;
    private View rootView;
    private Activity context;
    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // accomplish most tasks only when the view is created in onCreateView

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        this.playlistListView = (ListView) rootView.findViewById(R.id.playlist_fragment_list_view);  // access the listview from xml
        this.playlistListView.setAdapter(this.playlistAdapter);

        return rootView;    // Inflate the layout for this fragment
    }

    public void setPlaylistAdapter(Activity context,  ArrayList<SongData> songList){

        this.playlistAdapter = new PlaylistAdapter(context,  songList);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Activity getContext() {
        return context = playlistAdapter.getContext();
    }

    public void updateListView() {

        this.playlistAdapter.notifyDataSetChanged();
    }
    public ListView getPlaylistListView() {

        return playlistListView;
    }

}
