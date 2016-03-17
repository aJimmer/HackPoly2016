package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ClientMainActivity extends AppCompatActivity {
    
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.songrequest";
    public String songRequest;
    Button sendRequest;
    Button connectToHost;
    private ListView clientList;
    private ArrayList<String> songList;
    private ArrayAdapter<String> listAdapter;
    SharedPreferences prefs;
    private String returnedVideoID;
    private String returnedVideoTitle;
    private String returnVideoThumbnail;
    public String ipStr;
    TextView hostDisplay;

    private static final int SEARCH_CODE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);
        ipStr ="";
        clientList = (ListView)findViewById(R.id.client_list);
        songList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songList);
        clientList.setAdapter(listAdapter);
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

    public void sendMessage(String songId, String songName, String songThumbNail) {
        Socket socket = null;
        try {
            socket = new Socket(ipStr, 9000);
            //Send song id and song name to sever
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os);
            out.println(songId);
            out.println(songName);
            out.println(songThumbNail);
            //Recieve Playlist from server
            InputStream in = socket.getInputStream();
            InputStreamReader read = new InputStreamReader(in, "UTF-8");
            BufferedReader br = new BufferedReader(read);
            int playlistSize = Integer.parseInt(br.readLine());
            songList.clear();
            for(int i = 0; i < playlistSize; i++)
            {
                songList.add(br.readLine());

            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listAdapter.notifyDataSetChanged();
                }
            });
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
                    returnVideoThumbnail = data.getStringExtra("Song Thumbnail");
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            sendMessage(returnedVideoID, returnedVideoTitle, returnVideoThumbnail);
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

        //Listen for response on different port
        DatagramSocket responseSocket = new DatagramSocket(9820, getBroadCastAddress());
        byte[] recieveBuffer = new byte[20];
        DatagramPacket recievePacket = new DatagramPacket(recieveBuffer, recieveBuffer.length);
        responseSocket.receive(recievePacket);
        byte []ipData = new byte[recievePacket.getLength()];
        for(int i = 0; i < recievePacket.getLength(); i ++)
        {
            ipData[i] = recieveBuffer[i];
        }
        ipStr = new String(ipData);
        ipStr = flipIpAddress(ipStr);
        responseSocket.close();

        Log.d("Network", ipStr);

        socket.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //hostDisplay.setText(ipStr);
                CharSequence text = "Connected to " + ipStr;
                Toast.makeText(ClientMainActivity.this, text, Toast.LENGTH_SHORT).show();
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
    public String flipIpAddress(String receivedString)
    {
        if(receivedString == null)
        {
            return receivedString;
        }
        String [] ipValues = receivedString.split(Pattern.quote("."));

        return ipValues[3] + "." + ipValues[2] + "." + ipValues[1] + "." + ipValues[0];
    }



}
