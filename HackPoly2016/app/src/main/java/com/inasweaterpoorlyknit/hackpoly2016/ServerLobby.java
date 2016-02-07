package com.inasweaterpoorlyknit.hackpoly2016;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerLobby extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {

    public ListView listView;
    public ArrayList<String> songId;
    public ArrayList<String> songNames;
    public static YouTubePlayer player;
    public int index;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);
        listView = (ListView)findViewById(R.id.serverText);

        songId = new ArrayList<>();
        //songNames = new ArrayList<>();
        index = 0;


        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);
        youTubePlayerFragment.initialize(DeveloperKey.ANDROID_DEVELOPER_KEY, this);
        //TextView textView = (TextView)findViewById(R.id.serverText);
        Runnable serverThread = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(9000));
                    while(true)
                    {
                        Socket socket = serverSocket.accept();
                        InputStream in = socket.getInputStream();
                        InputStreamReader read = new InputStreamReader(in, "UTF-8");
                        BufferedReader br = new BufferedReader(read);
                        String yCode = br.readLine();
                        //String songTitle = br.readLine();

                        if(player != null) {

                            songId.add(yCode);
                            //songNames.add(songTitle);
                            Log.d(yCode, "from client");
                            //Log.d(songTitle, "song Name");
                            if (!player.isPlaying()) {
                                player.loadVideo(songId.get(0));
                                songId.remove(0);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };


        Thread thread = new Thread(serverThread);
        thread.start();

        //textView.setText(clientString);
    }
    /*public void updateText(String msg){
        TextView textView = (TextView)findViewById(R.id.serverText);
        textView.setText(clientString);
    }*/

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            youTubePlayer.loadVideo("yIWmRbHDhGw");
            this.player = youTubePlayer;
            player.setShowFullscreenButton(false);
            player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {

                @Override
                public void onLoading() {
                }

                @Override
                public void onLoaded(String s) {

                }

                @Override
                public void onAdStarted() {

                }

                @Override
                public void onVideoStarted() {
                    updateListView();
                }

                @Override
                public void onVideoEnded() {
                    if(songId.size()> 0) {
                        String id = songId.remove(0);
                        //songNames.remove(0);
                        player.loadVideo(id);
                    }else{
                        Log.d("LIST IS EMPTY", "serverMSG");
                    }

                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {

                }
            });


        }
    }



    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    public void updateListView(){

    }
}
