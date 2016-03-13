package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {

    private ListView playlistListView;

    private ArrayList<String> currentPlaylistTitles;
    private ArrayList<String> playlistThumbnails;
    private PlaylistAdapter playlistAdapter;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        playlistListView = (ListView) rootView.findViewById(R.id.playlist_fragment_list_view);

        playlistAdapter = new PlaylistAdapter(getActivity(), currentPlaylistTitles, playlistThumbnails);

        playlistListView.setAdapter(playlistAdapter);
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void setArguments(Bundle bundle){
        super.setArguments(bundle);
        currentPlaylistTitles = bundle.getStringArrayList("songTitles");
        playlistThumbnails = bundle.getStringArrayList("songThumbnails");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void notifyDataSetChanged(){
        playlistAdapter.notifyDataSetChanged();
    }

    public boolean isAdapterInitialized(){
        return !(playlistAdapter == null);
    }
}
