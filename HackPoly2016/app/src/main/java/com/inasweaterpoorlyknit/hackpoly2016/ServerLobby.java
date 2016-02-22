package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

public class ServerLobby extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {

    public ListView listView;
    public ArrayList<String> songId;
    public ArrayList<String> songNames;
    public ArrayAdapter<String> listAdapter;
    public static YouTubePlayer player;
    public int index;

    private String androidKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);
        songId = new ArrayList<>();
        songNames = new ArrayList<>();
        songNames.add("Test");
        listView = (ListView)findViewById(R.id.server_list);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songNames);
        listView.setAdapter(listAdapter);
        index = 0;


        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);


        // accessing our private developerKey.properties folder to hide our personal developer keys
        // this is so our dev keys will not be hosted on github
        // place a developerKey.properties file in your assets folder with a androidKey
        // values if you want this to work
        try{
            // getting our properties file in our asset folder
            AssetManager assetManager = getAssets();
            Properties prop = new Properties();
            String propFileName = "developerKey.properties";
            InputStream inputStream = assetManager.open(propFileName);
            // only set our android key string if we successfully opened the file
            if(inputStream != null){
                prop.load(inputStream);
                inputStream.close();
                androidKey = prop.getProperty("androidKey");
            } else{
                throw new FileNotFoundException("property file '" + propFileName + "'not found in the classpath");
            }
        } catch (Exception e){
            System.out.println("Exception: " + e);
        }

        // only initialize our youTubePlayerFragment if our androidKey was obtained
        if(androidKey != null) {
            youTubePlayerFragment.initialize(androidKey, this);
        } else {
            Log.d("androidKey: ", "failed to initialize");
        }

        //TextView textView = (TextView)findViewById(R.id.serverText);
        Runnable serverUDPThread = new Runnable() {
            @Override
            public void run() {
                try {
                    udpServer();
                    //runTCPSocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
        Runnable serverTCPThread = new Runnable() {
            @Override
            public void run() {
                runTCPSocket();

            }
        };

        Thread tcpThread = new Thread(serverTCPThread);
        Thread thread = new Thread(serverUDPThread);
        thread.start();
        tcpThread.start();

        //textView.setText(clientString);
    }
    /*public void updateText(String msg){
        TextView textView = (TextView)findViewById(R.id.serverText);
        textView.setText(clientString);
    }*/
    public void udpServer() throws IOException {

        String localIpAddress = getLocalIpAddress();


        while(true) {
            DatagramSocket socket = new DatagramSocket(9821, getBroadCastAddress());
            socket.setBroadcast(true);

            //Recieve data about connected client
            byte []recieveDate = new byte[10];
            DatagramPacket receivePacket = new DatagramPacket(recieveDate, recieveDate.length);
            socket.receive(receivePacket);
            Log.d("Server", "New Client connected");
            //send Client, the server's ip address
            DatagramSocket sendSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(localIpAddress.getBytes(), localIpAddress.length(),
                    getBroadCastAddress(), 9820);

           sendSocket.send(sendPacket);
           sendSocket.close();
           socket.close();


            Log.d("Broadcasting", "Broadcast " + localIpAddress +" to network");
            //socket.close();

        }


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
    public void runTCPSocket()
    {
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
                String songTitle = br.readLine();

                if(player != null) {

                    songId.add(yCode);
                    songNames.add(songTitle);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter.notifyDataSetChanged();
                        }
                    });
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
