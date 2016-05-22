package com.inasweaterpoorlyknit.hackpoly2016;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Connor on 3/18/2016.
 */
public class HistoryFragment extends Fragment {

    private ListView historyListView; // list view to display the playlist
    private PlaylistAdapter historyAdapter;
    private boolean adapterSet = false;

    public HistoryFragment() {
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

        historyListView = (ListView) rootView.findViewById(R.id.playlist_fragment_list_view);  // access the listview from xml

        if(adapterSet){
            historyListView.setAdapter(historyAdapter);
        }

        return rootView;    // Inflate the layout for this fragment
    }

    public void setPlaylistAdapter(Activity context, ArrayList<String> songTitles, ArrayList<Bitmap> thumbnails){
        this.historyAdapter = new PlaylistAdapter(context, songTitles, thumbnails);
        adapterSet = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void updateListView(){
        this.historyAdapter.notifyDataSetChanged();
    }
}
