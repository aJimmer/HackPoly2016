package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.api.services.youtube.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements
        YouTubePlayer.OnInitializedListener{

    private YouTubePlayerFragment playerFragment;
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    public YouTubePlayer player;

    private Object lock = new Object();
    public List<SearchResult> searchResults;
    public ArrayList<String> resultTitles = new ArrayList<>();

    private int playingVideoIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        playerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.search_player_fragment);
        playerFragment.initialize(DeveloperKey.ANDROID_DEVELOPER_KEY, this);

        final Button searchButton = (Button) findViewById(R.id.search_button);
        final EditText artistEditText = (EditText) findViewById(R.id.artist_text);
        final EditText songEditText = (EditText) findViewById(R.id.song_text);
        final ListView searchListView = (ListView) findViewById(R.id.search_list_view);
        final FloatingActionButton returnButton = (FloatingActionButton) findViewById(R.id.return_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable sendTask = new Runnable() {
                    @Override
                    public void run() {
                        String query = artistEditText.getText().toString() + " " + songEditText.getText().toString();

                        synchronized (lock) {
                            searchResults = Search.Search(query);
                                    //.get(0).getId().getVideoId();
                                    //.getSnippet().getThumbnails().getStandard();
                            lock.notify();
                        }
                        if(searchResults.get(0).getId().getVideoId() != null){
                            Log.d("newSongID: ", "new song id is " + searchResults.get(0).getId().getVideoId());
                        } else {
                            Log.d("newSongID: ", "couldn't get new song id");
                        }
                    }
                };
                Thread threadObj = new Thread(sendTask);
                threadObj.start();
                synchronized(lock) {
                    try {
                        lock.wait();
                        player.loadVideo(searchResults.get(0).getId().getVideoId());
                        playingVideoIndex = 0;
                        if(searchResults != null) {
                            resultTitles.clear();
                            for (SearchResult searchResult : searchResults) {
                                resultTitles.add(searchResult.getSnippet().getTitle());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, resultTitles);
                            searchListView.setAdapter(adapter);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                player.loadVideo(searchResults.get(i).getId().getVideoId());
                playingVideoIndex = i;
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String msg = intent.getStringExtra("song");
                if (msg.contentEquals("name")) {
                    intent.putExtra("Song ID", searchResults.get(playingVideoIndex).getId().getVideoId());
                    intent.putExtra("Song Title", resultTitles.get(playingVideoIndex));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if(!b){
            this.player = youTubePlayer;
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if(youTubeInitializationResult.isUserRecoverableError()){
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = "onInitializationFailure of YouTubeFragment";
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }
}
