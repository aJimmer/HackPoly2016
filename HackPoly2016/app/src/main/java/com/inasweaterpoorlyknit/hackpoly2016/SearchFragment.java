package com.inasweaterpoorlyknit.hackpoly2016;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.api.services.youtube.model.SearchResult;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public List<SearchResult> searchResults; // list to hold the search results from youtube's search api
    private Object lock = new Object(); // a lock object used for synchronization with task
    private String webKey;


    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // accessing our private developerKey.properties folder to hide our personal developer keys
        // this is so our dev keys will not be hosted on github
        // place a developerKey.properties file in your assets folder with a webBrowserKey and androidKey
        // values if you want this to wo
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        try{
            AssetManager assetManager = this.getContext().getAssets();
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

        final Button searchButton = (Button)view.findViewById(R.id.searchButtonFrag); // button to get YouTube search results
        final EditText songEditText = (EditText) view.findViewById(R.id.songTextFrag); // editText for getting song from user
        final EditText artistEditText = (EditText)view.findViewById(R.id.artistTextFrag); // editText for getting artist from user
        final ListView searchListView = (ListView) view.findViewById(R.id.listViewFrag); // listView to show search results
        final ArrayList<String> songNames = new ArrayList<>();
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, songNames);
        searchListView.setAdapter(listAdapter);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songNames.add(songEditText.getText().toString());
                listAdapter.notifyDataSetChanged();
                /*// test for thread, so we can access networks outside of the main thread

                // concatenate the artist and song names from the user
                // NOTE: Maybe not have it be two things. Unnecessary but might look better?
                String query = artistEditText.getText().toString() + " " + songEditText.getText().toString();

                // this synchronized lock ensures we don't display the search results until they are found
                synchronized (lock) {
                    // only search for results if a webKey was obtained from .properties file
                    if(webKey != null) {
                        // calling our search function to access YouTube's api and return the search results
                        searchResults = Search.Search(query, webKey);
                    } else {
                        Log.d("webKey: ", "failed to initialize");
                    }
                    //.get(0).getId().getVideoId();
                    //.getSnippet().getThumbnails().getStandard();
                    // tell the waiting object to continue
                    lock.notify();
                }

                // debug info to ensure our search was successful
                if(searchResults.get(0).getId().getVideoId() != null){
                    Log.d("newSongID: ", "new song id is " + searchResults.get(0).getId().getVideoId());
                } else {
                    Log.d("newSongID: ", "couldn't get new song id");
                }*/
            }
            });
        return inflater.inflate(R.layout.fragment_search, container, false);
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }
}
