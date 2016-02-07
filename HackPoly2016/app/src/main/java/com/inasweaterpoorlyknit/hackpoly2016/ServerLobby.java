package com.inasweaterpoorlyknit.hackpoly2016;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerLobby extends AppCompatActivity {

    public String clientString = "test";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        TextView textView = (TextView)findViewById(R.id.serverText);
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
                        //updateText(clientString);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = (TextView) findViewById(R.id.serverText);
                                textView.setText(clientString);
                            }
                        });
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
    public void updateText(String msg){
        TextView textView = (TextView)findViewById(R.id.serverText);
        textView.setText(clientString);
    }

}
