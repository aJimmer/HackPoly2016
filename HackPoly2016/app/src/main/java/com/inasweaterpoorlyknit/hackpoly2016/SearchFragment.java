package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.services.youtube.model.SearchResult;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private Button searchButton;
    private ListView searchListView;
    private FloatingActionButton addToPlaylistButton;

    private final Object lock = new Object(); // a lock object used for synchronization with task
    public List<SearchResult> searchResults; // list to hold the search results from youtube's search api

    public ArrayList<String> searchTitles;
    public ArrayList<Bitmap> searchThumbnails;

    private long numSearchResults;

    private PlaylistAdapter resultsAdapter;

    private String webKey;

    private int selectedVideoIndex;  // index of the video being played

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

        numSearchResults = Search.NUMBER_OF_VIDEOS_TO_RETURN;

        selectedVideoIndex = -1;
        searchTitles = new ArrayList<>();
        searchThumbnails = new ArrayList<>();

        searchEditText = (EditText) rootView.findViewById(R.id.search_fragment_edit_text);

        // input method manager to control when the keyboard is active
        final InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        searchButton = (Button) rootView.findViewById(R.id.search_fragment_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                // tast for thread, so we can access networks outside of the main thread
                Runnable searchTask = new Runnable() {
                    @Override
                    public void run() {
                        String query = searchEditText.getText().toString();

                        // this synchronized lock ensures we don't display the search results until they are found
                        synchronized (lock) {
                            // only search for results if a webKey was obtained from .properties file
                            if(webKey != null) {
                                // calling our search function to access YouTube's api and return the search results
                                searchResults = Search.Search(query, webKey);
                            } else {
                                Log.d("webKey: ", "failed to initialize");
                            }
                            lock.notify();
                        }

                        // debug info to ensure our search was successful
                        if(searchResults.get(0).getId().getVideoId() != null){
                            Log.d("newSongID: ", "new song ID is " + searchResults.get(0).getId().getVideoId());
                        } else {
                            Log.d("newSongID: ", "couldn't get new song id");
                        }
                    }
                };

                // creating a thread to search for the videos
                Thread threadObj = new Thread(searchTask);
                threadObj.start();

                // ensuring that we do not access the searchResults until the search has finished
                synchronized(lock) {
                    try {
                        // have the object wait until it is notified
                        lock.wait();

                        if(searchResults != null) { // if there are results to return
                            searchTitles.clear();  // first clear the result Titles
                            searchThumbnails.clear(); // and the result Thumbnails
                            // for each searchResult, set it in the result Titles
                            for (SearchResult searchResult : searchResults) {
                                searchTitles.add(searchResult.getSnippet().getTitle());
                                new DownloadThumbnailTask().execute(searchResult.getSnippet().getThumbnails().getDefault().getUrl());
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // if an item in the list is clicked, save it's current index in the list view
        searchListView = (ListView) rootView.findViewById(R.id.search_fragment_list_view);
        resultsAdapter = new PlaylistAdapter(getActivity(), searchTitles, searchThumbnails);
        searchListView.setAdapter(resultsAdapter);
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedVideoIndex = i;
            }
        });

        addToPlaylistButton = (FloatingActionButton) rootView.findViewById(R.id.add_to_playlist_button);
        addToPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // only do something if the user actually searched for a video
                if (selectedVideoIndex >= 0) {
                    // if user selected any video add to playlist
                    ((ServerLobby) getActivity()).addSong(searchResults.get(selectedVideoIndex).getId().getVideoId(),
                            searchTitles.get(selectedVideoIndex),
                            searchThumbnails.get(selectedVideoIndex));
                    Toast.makeText(view.getContext(), "Song added to current playlist.", Toast.LENGTH_SHORT).show();
                } else {
                    // inform user they must search for a video first
                    Toast.makeText(view.getContext(), "Search for a video first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // if the user says they are done editing, search for results
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                if (actionID == EditorInfo.IME_ACTION_DONE) {
                    searchButton.performClick();
                    return true;
                }
                return false;
            }
        });

        try{
            AssetManager assetManager = getActivity().getAssets();
            Properties prop = new Properties();
            String propFileName = "developerKey.properties";
            InputStream inputStream = assetManager.open(propFileName);
            if(inputStream != null){
                prop.load(inputStream);
                inputStream.close();
                webKey = prop.getProperty("webBrowserKey");
            } else{
                throw new FileNotFoundException("property file '" + propFileName + "'not found in the classpath");
            }
        } catch (Exception e){
            System.out.println("Exception: " + e);
        }
        
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

    // AsyncTask to download the thumbnail images
    public class DownloadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        // downloads any number of URLs in the background
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];    // save the first url
            Bitmap thumbnail = null;          // thumbnail, set to null
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream(); // get an input stream from specified url
                thumbnail = BitmapFactory.decodeStream(in);   // decode the inputStream as a Bitmap
            } catch (Exception e) { // printe any errors
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return thumbnail;   // return the thumbnail
        }

        // called after bitmap is loaded and returned from doInBackground()
        protected void onPostExecute(Bitmap result) {
            searchThumbnails.add(result); // add the song thumbnail to playlist
            resultsAdapter.notifyDataSetChanged();
        }
    }
}
