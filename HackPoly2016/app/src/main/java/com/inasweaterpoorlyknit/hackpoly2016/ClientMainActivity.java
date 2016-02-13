package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubePlayerFragment;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.PrintStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

public class ClientMainActivity extends AppCompatActivity {
    
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.songrequest";
    public String songRequest;
    Button sendRequest;
    Button connectToHost;
    SharedPreferences prefs;
    private String returnedVideoID;
    private String returnedVideoTitle;
    public String ipStr;
    TextView hostDisplay;

    private static final int SEARCH_CODE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);
        ipStr ="";

        hostDisplay = (TextView)findViewById(R.id.hostDisplay);

        FloatingActionButton search_button = (FloatingActionButton)findViewById(R.id.find_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        connectToHost = (Button)findViewById(R.id.connectToHost);



        connectToHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.ipBox);
                ipStr = editText.getText().toString();

                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        //testConnection();
                        try {
                            readBroadcast();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Thread newThread = new Thread(task);
                newThread.start();
            }
        });

        final FloatingActionButton findButton = (FloatingActionButton) findViewById(R.id.find_button);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchSong();
            }
        });
    }

    public void searchSong() {
        Intent searchIntent = new Intent(this, SearchActivity.class);
        searchIntent.putExtra("song", "name");
        startActivityForResult(searchIntent, SEARCH_CODE);
    }

    public void testConnection(){
        Socket socket = null;
        try{
            socket = new Socket(ipStr, 9000);
            socket.close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hostDisplay.setText(ipStr);
                }
            });
        }catch (IOException e){
            e.printStackTrace();
            //hostDisplay.setText("Could not connect");
        }
    }

    public void sendMessage(String str){
        //Scanner userInput = new Scanner(System.in);
        //String message;
        Socket socket = null;
        try {
            socket = new Socket(ipStr, 9000);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            // Check which request we're responding to
            if (requestCode == SEARCH_CODE) {
                if(data.getExtras().containsKey("Song ID")){
                    returnedVideoID = data.getStringExtra("Song ID");
                    returnedVideoTitle = data.getStringExtra("Song Title");
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            sendMessage(returnedVideoID);
                            //sendMessage(returnedVideoTitle);
                        }
                    };
                    Thread newThread = new Thread(task);
                    newThread.start();
                }
            }
        }
    }
    public void readBroadcast() throws IOException {
        String ipAddress = getLocalIpAddress();
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        byte []sendData = ipAddress.getBytes();
        //send Data to server
        DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length,
                getBroadCastAddress(), 9821);
        socket.send(sendPack);

        //Listen for response
        DatagramSocket responseSocket = new DatagramSocket(9821, getBroadCastAddress());
        byte []recieveBuffer = new byte[1024];
        DatagramPacket recievePacket = new DatagramPacket(recieveBuffer, recieveBuffer.length);
        responseSocket.receive(recievePacket);
        ipStr = new String(recieveBuffer);

        Log.d("Network", ipStr);
        responseSocket.close();
        socket.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hostDisplay.setText(ipStr);
            }
        });

    }

    public InetAddress getBroadCastAddress() throws IOException
    {
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if(dhcp == null)
        {
            return null;
        }
        int broadCast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte []quads = new byte[4];
        for(int k = 0; k < 4; k++)
        {
            quads[k] = (byte) (broadCast >> (k*8));
        }
        return InetAddress.getByAddress(quads);

    }
    public String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        byte [] ipByteArray = BigInteger.valueOf(ip).toByteArray();
        String ipString;
        ipString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        return  ipString;
    }


}
