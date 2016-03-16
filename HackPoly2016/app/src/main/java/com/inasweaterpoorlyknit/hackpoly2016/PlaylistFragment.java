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

/**
*   PlaylistFragment displays a playlist of songTitles and songThumbnails
*   This is used for the playlistFragment AND historyFragment of the ServerLobby
**/

public class PlaylistFragment extends Fragment {

    private ListView playlistListView; // list view to display the playlist
    private PlaylistAdapter playlistAdapter; // list view adapter that uses our modified rows

    private ArrayList<String> playlistTitles;    // Strings to hold the song titles
    private ArrayList<String> playlistThumbnails; // Strings to hold the thumbnail urls

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
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        playlistListView = (ListView) rootView.findViewById(R.id.playlist_fragment_list_view);  // access the listview from xml

        playlistAdapter = new PlaylistAdapter(getActivity(), playlistTitles, playlistThumbnails);   // initiailize the PlaylistAdapter with the ArrayList titles and ArrayList thumbnails urls

        playlistListView.setAdapter(playlistAdapter);   // set the adapter to our listview
        
        return rootView;    // Inflate the layout for this fragment
    }

    // used to pass the ArrayLists to the fragment
    @Override
    public void setArguments(Bundle bundle){
        super.setArguments(bundle);
        playlistTitles = bundle.getStringArrayList("songTitles");   // set the playlistTitles to the ArrayList passed in the bundle
        playlistThumbnails = bundle.getStringArrayList("songThumbnails");   // set the playlistThumbnails to the ArrayList passed in the bundle
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // function to update our list view
    public void notifyDataSetChanged(){
        playlistAdapter.notifyDataSetChanged(); // tell the playlistAdapater to update based on changes
    }

    // functions to check if the playlist adapter is initialized
    public boolean isAdapterInitialized(){
        return !(playlistAdapter == null);  // return false if playlistAdapter is null
    }
}
