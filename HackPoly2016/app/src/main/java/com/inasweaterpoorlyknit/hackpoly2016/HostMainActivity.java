package com.inasweaterpoorlyknit.hackpoly2016;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class HostMainActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener{
    public static String API_Key = "AIzaSyCOX5sjc9q9FK3eHwBipWqNR1WRfr7maUw";
    public String clientString;
    public ArrayList<String> list;
    public ListView listView;
    public Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_lobby);
        clientString = "";
        list = new ArrayList<>();
        list.add("0");
        listView = (ListView)findViewById(R.id.listView);
        TextView textView =(TextView) findViewById(R.id.hostLobby);
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
                        clientString = br.readLine();



                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
        Thread thread = new Thread(serverThread);
        thread.start();

        textView.setText(clientString);

        YouTubePlayerFragment youTubePlayerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);
        youTubePlayerFragment.initialize(API_Key, this);


    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo("nCgQDjiotG0");
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
    public void onBackPressed()
    {
    }

}
