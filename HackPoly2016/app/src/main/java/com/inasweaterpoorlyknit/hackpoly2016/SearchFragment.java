package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private Button searchButton;
    private ListView searchListView;

    public ArrayList<String> searchTitles;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        searchEditText = (EditText) rootView.findViewById(R.id.search_fragment_edit_text);

        searchButton = (Button) rootView.findViewById(R.id.search_fragment_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // search
            }
        });

        searchListView = (ListView) rootView.findViewById(R.id.search_fragment_list_view);

        searchTitles = new ArrayList<>();
        searchTitles.add("Song 1 by Artist 1");
        searchTitles.add("Song 2 by Artist 2");
        searchTitles.add("Song 3 by Artist 3");
        searchTitles.add("Song 4 by Artist 4");
        searchTitles.add("Song 5 by Artist 5");
        searchTitles.add("Song 6 by Artist 6");
        searchTitles.add("Song 7 by Artist 7");
        searchTitles.add("Song 8 by Artist 8");
        searchTitles.add("Song 9 by Artist 9");
        searchTitles.add("Song 10 by Artist 10");
        // Inflate the layout for this fragment

        searchListView.setAdapter(new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, searchTitles));
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
