package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private ArrayList<String> playlistSongIDs;      // current playlist's song IDs
    private ArrayList<String> playlistSongTitles;   // current playlist's song titles
    private ArrayList<Bitmap> playlistThumbnails;   // current playlist's song thumbnails

    private ArrayList<String> historySongTitles;    // previous playlist song titles
    private ArrayList<Bitmap> historyThumbnails;    // previous playlist song thumbnails

    private YouTubePlayer player;                   // the YouTube player fragment

    private ViewPager viewPager;    // view pager will link our three fragments
    private TabLayout tabLayout;    // the tabs that initiate the change between fragments
    
    private HistoryFragment historyFragment;       // fragment to display playlist history
    private PlaylistFragment playlistFragment;      // fragment to display the current playlist
    private SearchFragment searchFragment;          // fragment to allow searching and adding new songs

    private String androidKey;                      // android developer key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);

        // initialize our arrays to hold the song ids, titles, and thumbnails
        playlistSongIDs = new ArrayList<>();
        playlistSongTitles = new ArrayList<>();
        playlistThumbnails = new ArrayList<>();
        historySongTitles = new ArrayList<>();
        historyThumbnails = new ArrayList<>();

        // four hardcoded songs to assist with debugging
        this.addSong("S-Xm7s9eGxU", "Erik Satie - Gymnopédie No.1", "https://i.ytimg.com/vi/S-Xm7s9eGxU/default.jpg");
        this.addSong("HyozVHz9Ml4", "Laurence Equilbey - Cantique de Jean Racine - opus 11 (In Paradisum)", "https://i.ytimg.com/vi/HyozVHz9Ml4/default.jpg");
        this.addSong("iqb60rxl96I", "Eluvium - Radio Ballet", "https://i.ytimg.com/vi/iqb60rxl96I/default.jpg");
        this.addSong("KHlnKXBVFVg", "Wintercoats // Working on a Dream", "https://i.ytimg.com/vi/KHlnKXBVFVg/default.jpg");

        // initialize playlist fragment with current tracks
        playlistFragment = new PlaylistFragment();  // intialize playlist fragment
        playlistFragment.setPlaylistAdapter(this, playlistSongTitles, playlistThumbnails);


        // initialize search fragment
        searchFragment = new SearchFragment();

        // initialize history fragment
        historyFragment = new HistoryFragment();  // intialize history fragment
        historyFragment.setPlaylistAdapter(this, historySongTitles, historyThumbnails);

        // initialize the viewPager to link to the three fragments(Playlist, Search, History)
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // initialize the tablayout with the info from viewPager
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // initialize our YouTube player through a youTubePlayerFragment
        // all initialization is handled in the functions: OnInitializationSuccess and OnInitializationFailure
        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);

        // accessing our private developerKey.properties folder to hide our personal developer keys
        // this is so our dev keys will not be hosted on github
        // place a developerKey.properties file in your assets folder with a androidKey
        // value if you want this to work
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

        //Run UDP server socket on new thread[since there should be no networking on main thread]
        Runnable serverUDPThread = new Runnable() {
            @Override
            public void run() {
                try {
                    udpServer();
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

    //   setupViewPager is a layout manager that allows us to flip left and right through fragments
    //   we initialize it with our PlaylistFragment, SearchFragment, and HistoryFragment
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager()); // initialize a viewPagerAdapter
        adapter.addFragment(playlistFragment, getResources().getString(R.string.title_playlist));   // Add the playlist,
        adapter.addFragment(searchFragment, getResources().getString(R.string.title_search));       // search, and
        adapter.addFragment(historyFragment, getResources().getString(R.string.title_history));     // history fragments
        viewPager.setAdapter(adapter);  // set the adapter to our viewPager
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
                final String songId = br.readLine();
                final String songTitle = br.readLine();
                final String songThumbnail = br.readLine();

                if (player != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addSong(songId, songTitle, songThumbnail);
                        }
                    });
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
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {   // if the player is new and not just restored
            youTubePlayer.loadVideo(playlistSongIDs.get(0)); // the first song on our debug list of songs
            this.player = youTubePlayer;
            player.setShowFullscreenButton(false);  // prev & next buttons currently disabled from our player
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
                public void onVideoEnded() {    // when video ends...
                    if (playlistSongIDs.size() > 0) {   // if there are still songs to remove
                        playlistSongIDs.remove(0);  // remove the top song id
                        historySongTitles.add(0, playlistSongTitles.remove(0)); // remove the top song title and place it in front of historySongTitles
                        historyThumbnails.add(0, playlistThumbnails.remove(0)); // remove the top song thumbnail and place it in front of historyThumbnails
                        if(!playlistSongIDs.isEmpty()){ // if there are more videos to load
                            player.loadVideo(playlistSongIDs.get(0)); // load the first video on the list
                        }
                        playlistFragment.updateListView();
                        historyFragment.updateListView();
                    } else {
                        Log.d("LIST IS EMPTY", "serverMSG"); // else, print that our list is empty to debug log
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
        // do nothing
    }

    public void addSong(String songID, String songTitle, Bitmap songThumbnail){
        playlistSongIDs.add(songID);    // add the songID to playlist
        playlistSongTitles.add(songTitle); // add the song title to playlist
        playlistThumbnails.add(songThumbnail); // add the song thumbnail to playlist
        if(!player.isPlaying()){
            player.loadVideo(songID);
        }
        playlistFragment.updateListView(); // notify playlistFragment of the changes
    }

    // add song function for our search fragment
    private void addSong(String songID, String songTitle, String songThumbnailURL){
        playlistSongIDs.add(songID);    // add the songID to playlist
        playlistSongTitles.add(songTitle); // add the song title to playlist

        // AsyncTask to download the thumbnail images
        class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
            // downloads any number of URLs in the background
            protected Bitmap doInBackground(String... urls) {
                String urlDisplay = urls[0];    // save the first url
                Bitmap thumbnail = null;          // thumbnail, set to null
                try {
                    InputStream in = new java.net.URL(urlDisplay).openStream(); // get an input stream from specified url
                    thumbnail = BitmapFactory.decodeStream(in);   // decode the inputStream as a Bitmap
                } catch (Exception e) { // printe any errors
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return thumbnail;   // return the thumbnail
            }

            // called after bitmap is loaded and returned from doInBackground()
            protected void onPostExecute(Bitmap result) {
                playlistThumbnails.add(result); // add the song thumbnail to playlist
                playlistFragment.updateListView(); // notify playlistFragment of the changes
            }
        }
        new DownloadImageTask().execute(songThumbnailURL);  // download the thumbnail for that song and set as bitmap for the imageview
    }
}
