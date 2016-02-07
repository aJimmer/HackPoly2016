package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayer;

public class SearchActivity extends AppCompatActivity implements
        YouTubePlayer.OnInitializedListener{

    private YouTubePlayerFragment playerFragment;
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    public YouTubePlayer player;

    private Object lock = new Object();
    public String newSongID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        playerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.search_player_fragment);
        playerFragment.initialize(DeveloperKey.ANDROID_DEVELOPER_KEY, this);

        final Button searchButton = (Button) findViewById(R.id.search_button);
        final EditText artistEditText = (EditText) findViewById(R.id.artist_text);
        final EditText songEditText = (EditText) findViewById(R.id.song_text);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable sendTask = new Runnable() {
                    @Override
                    public void run() {
                        String query = artistEditText.getText().toString() + " " + songEditText.getText().toString();

                        synchronized (lock) {
                            newSongID = Search.Search(query);
                            lock.notify();
                        }
                        if(newSongID != null){
                            Log.d("newSongID: ", "new song id is " + newSongID);
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
                        if(newSongID != null){
                            player.loadVideo(newSongID);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
