package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
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

public class ServerLobby extends AppCompatActivity implements YouTubePlayer.OnInitializedListener{

    private TabLayout tabLayout;
    private ViewPager viewPager;

    // private ListView listView;
    // private ArrayAdapter<String> listAdapter;
    private ArrayList<String> playlistSongIDs;
    private ArrayList<String> playlistSongTitles;
    private ArrayList<String> historySongTitles;
    private YouTubePlayer player;

    private PlaylistFragment historyFragment;
    private PlaylistFragment playlistFragment;
    private SearchFragment searchFragment;

    private int index;

    private String androidKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);

        // our arrays to hold the song ids and titles of the current playlist
        playlistSongIDs = new ArrayList<>();
        playlistSongTitles = new ArrayList<>();
        historySongTitles = new ArrayList<>();

        // four hardcoded songs to assist with debugging
        playlistSongIDs.add("S-Xm7s9eGxU");
        playlistSongTitles.add("Erik Satie - Gymnopédie No.1");
        playlistSongIDs.add("HyozVHz9Ml4");
        playlistSongTitles.add("Laurence Equilbey - Cantique de Jean Racine - opus 11 (In Paradisum");
        playlistSongIDs.add("iqb60rxl96I");
        playlistSongTitles.add("Eluvium - Radio Ballet");
        playlistSongIDs.add("KHlnKXBVFVg");
        playlistSongTitles.add("Wintercoats // Working on a Dream");

        // initialize playlist fragment with current tracks
        playlistFragment = new PlaylistFragment();
        Bundle playlistFragArgs = new Bundle();
        playlistFragArgs.putStringArrayList("songTitles", playlistSongTitles);
        playlistFragment.setArguments(playlistFragArgs);

        // initialize search fragment
        searchFragment = new SearchFragment();

        // initialize history fragment
        historyFragment = new PlaylistFragment();
        Bundle historyFragArgs = new Bundle();
        historyFragArgs.putStringArrayList("songTitles", historySongTitles);
        historyFragment.setArguments(historyFragArgs);

        // initialize the viewPager to link to the three fragments(Playlist, Search, History)
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // initialize the tablayout with the info from viewPager
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //listView = (ListView)findViewById(R.id.server_list);
        //listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songNames);
        //listView.setAdapter(listAdapter);

        // initialize current index of video to zero
        index = 0;
        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);

        // accessing our private developerKey.properties folder to hide our personal developer keys
        // this is so our dev keys will not be hosted on github
        // place a developerKey.properties file in your assets folder with a androidKey
        // values if you want this to work
        try {
            // getting our properties file in our asset folder
            AssetManager assetManager = getAssets();
            Properties prop = new Properties();
            String propFileName = "developerKey.properties";
            InputStream inputStream = assetManager.open(propFileName);
            // only set our android key string if we successfully opened the file
            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
                androidKey = prop.getProperty("androidKey");
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "'not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        // only initialize our youTubePlayerFragment if our androidKey was obtained
        if (androidKey != null) {
            youTubePlayerFragment.initialize(androidKey, this);
        } else {
            Log.d("androidKey: ", "failed to initialize");
        }

        //Run UDP server socket on new thread
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
        //Run TCP server socket on new thread.
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

    }

    /*
    *   setupViewPager is a layout manager that allows us to flip left and right through fragments
    *   we initialize it with our PlaylistFragment, SearchFragment, and HistoryFragment
    */

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(playlistFragment, getResources().getString(R.string.title_playlist));
        adapter.addFragment(searchFragment, getResources().getString(R.string.title_search));
        adapter.addFragment(historyFragment, getResources().getString(R.string.title_history));
        viewPager.setAdapter(adapter);
    }

    /**
     * Runs UDP Server socket that listens for new clients trying to connect
     * to party
     *
     * @throws IOException
     */
    public void udpServer() throws IOException {

        String localIpAddress = getLocalIpAddress();


        while (true) {
            DatagramSocket socket = new DatagramSocket(9821, getBroadCastAddress());
            socket.setBroadcast(true);

            //Recieve data about connected client
            byte[] receiveDate = new byte[10];
            DatagramPacket receivePacket = new DatagramPacket(receiveDate, receiveDate.length);
            socket.receive(receivePacket);
            Log.d("Server", "New Client connected");
            //send Client, the server's ip address
            DatagramSocket sendSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(localIpAddress.getBytes(), localIpAddress.length(),
                    getBroadCastAddress(), 9820);

            sendSocket.send(sendPacket);
            sendSocket.close();
            socket.close();


            Log.d("Broadcasting", "Broadcast " + localIpAddress + " to network");


        }


    }

    /**
     * Returns the this device's ip address. Which is sent to
     * clients as a way for them to set up TCP sockets
     *
     * @return
     * @throws UnknownHostException
     */
    public String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();
        String ipString;
        ipString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        return ipString;
    }

    /**
     * Returns the broadcast address for the current network
     *
     * @return
     * @throws IOException
     */
    public InetAddress getBroadCastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) {
            return null;
        }
        int broadCast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) (broadCast >> (k * 8));
        }
        return InetAddress.getByAddress(quads);

    }

    /**
     * Method which runs the tcp server socket.
     * This socket waits for messages from the clients
     */
    public void runTCPSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(9000));
            while (true) {
                Socket socket = serverSocket.accept();
                InputStream in = socket.getInputStream();
                InputStreamReader read = new InputStreamReader(in, "UTF-8");
                BufferedReader br = new BufferedReader(read);
                //read the data being sent from client.
                String yCode = br.readLine();
                String songTitle = br.readLine();

                if (player != null) {

                    playlistSongIDs.add(yCode);
                    playlistSongTitles.add(songTitle);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //listAdapter.notifyDataSetChanged();
                        }
                    });
                    Log.d(yCode, "from client");
                    //Log.d(songTitle, "song Name");
                    if (!player.isPlaying()) {
                        player.loadVideo(playlistSongIDs.get(0));
                        playlistSongIDs.remove(0);
                    }
                }
                //Send playlist back to client
                OutputStream out = socket.getOutputStream();
                PrintStream outValue = new PrintStream(out);
                outValue.println(playlistSongTitles.size()); // send size of songname Array
                for(int i = 0; i < playlistSongTitles.size(); i++)
                {
                    outValue.println(playlistSongTitles.get(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            youTubePlayer.loadVideo("S-Xm7s9eGxU");
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

                }

                @Override
                public void onVideoEnded() {
                    if (playlistSongIDs.size() > 0) {
                        playlistSongIDs.remove(0);
                        historySongTitles.add(0, playlistSongTitles.remove(0));
                        if(!playlistSongIDs.isEmpty()){
                            player.loadVideo(playlistSongIDs.get(0));
                        }
                        playlistFragment.notifyDataSetChanged();
                        if(historyFragment.isAdapterInitialized()) {
                            historyFragment.notifyDataSetChanged();
                        }
                    } else {
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

    public void addSong(String songID, String songTitle){
        playlistSongIDs.add(songID);
        playlistSongTitles.add(songTitle);
        playlistFragment.notifyDataSetChanged();
        if(historyFragment.isAdapterInitialized()) {
            historyFragment.notifyDataSetChanged();
        }
    }
}
