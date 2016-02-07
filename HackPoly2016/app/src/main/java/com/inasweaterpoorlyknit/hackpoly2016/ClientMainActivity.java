package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.PrintStream;
import java.io.OutputStream;

public class ClientMainActivity extends AppCompatActivity {
    private static final int SEARCH_CODE = 2;
    String foundSongID;
    
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.songrequest";
    public String songRequest;
    Button sendRequest;
    Button connectToHost;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        sendRequest = (Button)findViewById(R.id.sendRequest);

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), HostMainActivity.class);
                EditText editText = (EditText) findViewById(R.id.songBox);
                songRequest = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);

                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        sendMessage(songRequest);
                    }
                };
                Thread newThread = new Thread(task);
                newThread.start();
            }
        });

        final FloatingActionButton searchButton = (FloatingActionButton) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchSong();
            }
        });
    }

    public void searchSong() {
        Intent searchIntent = new Intent(this, SearchActivity.class);
        searchIntent.putExtra("foundSong", foundSongID);
        startActivityForResult(searchIntent, SEARCH_CODE);
    }

    public void sendMessage(String str){
        //Scanner userInput = new Scanner(System.in);
        //String message;
        Socket socket = null;
        try {
            socket = new Socket("192.168.43.87", 9000);
            //message = userInput.nextLine();
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os);
            //System.out.println("Client >> " + str);
            out.println(str);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
