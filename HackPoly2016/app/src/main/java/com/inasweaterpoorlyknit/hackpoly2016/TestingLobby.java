package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayer;

import java.util.ArrayList;

public class TestingLobby extends AppCompatActivity implements
        YouTubePlayer.OnInitializedListener{

    private YouTubePlayerFragment playerFragment;
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    public YouTubePlayer player;

    private static final int SEARCH_CODE = 2;

    ArrayList<String> playlistSongIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_lobby);
        this.initialize();
    }

    public void initialize(){
        //playerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.playlist_fragment);
        //playerFragment.initialize(DeveloperKey.ANDROID_DEVELOPER_KEY, this);

        final FloatingActionButton searchButton = (FloatingActionButton) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchSong();
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if(!b){
            this.player = youTubePlayer;
                player.loadVideo("19GDcOuXqHo");
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

    public void searchSong() {
        Intent searchIntent = new Intent(this, SearchActivity.class);
        searchIntent.putExtra("song", "name");
        startActivityForResult(searchIntent, SEARCH_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            // Check which request we're responding to
            if (requestCode == SEARCH_CODE) {
                if(data.getExtras().containsKey("Song ID")){
                    String returnedVideoString = data.getStringExtra("Song ID");
                    //playlistSongIDs.add(returnedVideoString);
                    playerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.playlist_fragment);
                    playerFragment.initialize(DeveloperKey.ANDROID_DEVELOPER_KEY, this);
                }
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
