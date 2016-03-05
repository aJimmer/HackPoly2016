package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
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

        currentPlaylistTitles = new ArrayList<>();
        currentPlaylistTitles.add("Current Song 1 by Artist 1");
        currentPlaylistTitles.add("Current Song 2 by Artist 2");
        currentPlaylistTitles.add("Current Song 3 by Artist 3");
        currentPlaylistTitles.add("Current Song 4 by Artist 4");
        currentPlaylistTitles.add("Current Song 5 by Artist 5");
        currentPlaylistTitles.add("Current Song 6 by Artist 6");
        currentPlaylistTitles.add("Current Song 7 by Artist 7");
        currentPlaylistTitles.add("Current Song 8 by Artist 8");
        currentPlaylistTitles.add("Current Song 9 by Artist 9");
        currentPlaylistTitles.add("Current Song 10 by Artist 10");

        playlistListView.setAdapter(new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, currentPlaylistTitles));
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
