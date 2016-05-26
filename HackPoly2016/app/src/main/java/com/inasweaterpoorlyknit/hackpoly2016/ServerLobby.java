package com.inasweaterpoorlyknit.hackpoly2016;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;

public class ServerLobby extends AppCompatActivity implements YouTubePlayer.OnInitializedListener{

    private ArrayList<SongData> songList;
    private ArrayList<SongData> songList_History;

    private YouTubePlayer player;                   // the YouTube player fragment

    private ViewPager viewPager;    // view pager will link our three fragments
    private TabLayout tabLayout;    // the tabs that initiate the change between fragments

    private HistoryFragment historyFragment;       // fragment to display playlist history
    private PlaylistFragment playlistFragment;      // fragment to display the current playlist
    private SearchFragment searchFragment;          // fragment to allow searching and adding new songs

    private WifiP2pManager manager;                 //Wifi p2p manager for communication to clients
    private WifiP2pManager.Channel channel;         //Wifi p2p needed for manager
    private WifiP2pReceiver receiver;               //Broadcast reciever class that handles all communctionation bewtween servers and clients
    private IntentFilter intentFilter;              //Intent filter that listens for WIFI p2p events

    private String androidKey;                      // android developer key
    private Thread p2pThread;                       //thread that the wifi p2p runs on
    private ServerSocket serverSocket;              //Server socket that listens for clients

    private ListView playlistListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);

        // initialize our arrays to hold the song ids, titles, and thumbnails
        songList = new ArrayList<>();
        songList_History = new ArrayList<>();


        // four hardcoded songs to assist with debugging
        this.addSong("S-Xm7s9eGxU", "Erik Satie - Gymnopédie No.1", "https://i.ytimg.com/vi/S-Xm7s9eGxU/default.jpg");
        this.addSong("HyozVHz9Ml4", "Laurence Equilbey - Cantique de Jean Racine - opus 11 (In Paradisum)", "https://i.ytimg.com/vi/HyozVHz9Ml4/default.jpg");
        this.addSong("iqb60rxl96I", "Eluvium - Radio Ballet", "https://i.ytimg.com/vi/iqb60rxl96I/default.jpg");
        //this.addSong("KHlnKXBVFVg", "Wintercoats // Working on a Dream", "https://i.ytimg.com/vi/KHlnKXBVFVg/default.jpg");

        // initialize playlist fragment with current tracks
        playlistFragment = new PlaylistFragment();  // intialize playlist fragment
        //playlistFragment.addClickListener(songList);

        playlistFragment.setPlaylistAdapter(this, songList);




        // initialize search fragment
        searchFragment = new SearchFragment();

        // initialize history fragment
        historyFragment = new HistoryFragment();  // intialize history fragment
        //historyFragment.setPlaylistAdapter(this, historySongTitles, historyThumbnails);
        historyFragment.setPlaylistAdapter(this, songList_History);
        // initialize the viewPager to link to the three fragments(Playlist, Search, History)
        viewPager = (ViewPager) findViewById(R.id.server_viewpager);
        setupViewPager(viewPager);

        // initialize the tablayout with the info from viewPager
        tabLayout = (TabLayout) findViewById(R.id.server_tabs);
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
        registerReceiver();

        //Run Wifip2p server socket on new thread.
        Runnable serverP2PThread = new Runnable() {
            @Override
            public void run() {
                WIFIP2PServer();
            }
        };

        p2pThread = new Thread(serverP2PThread);
        p2pThread.start();
        //Discover every 3 seconds
        Runnable discoverTask = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //Every 3 seconds scan for new peers
                        Thread.sleep(3000);
                        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(WifiP2pReceiver.logType, "Discover Succeeded");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(WifiP2pReceiver.logType, "Discover Succeeded");

                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread discoverThread = new Thread(discoverTask);
        discoverThread.start();

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
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver);

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(WifiP2pReceiver.logType, "Party Disconnected");
                receiver.setAllConnections(false);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(WifiP2pReceiver.logType, "Party still running");
            }
        });
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
                outValue.println(songList.size());
                //outValue.println(playlistSongTitles.size()); // send size of songname Array
                for(int i = 0; i < songList.size(); i++)
                //for(int i = 0; i < playlistSongTitles.size(); i++)
                {
                    //outValue.println(playlistSongTitles.get(i));
                    outValue.println(songList.get(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {   // if the player is new and not just restored
            youTubePlayer.loadVideo(songList.get(0).songID);
            //youTubePlayer.loadVideo(playlistSongIDs.get(0)); // the first song on our debug list of songs
            this.player = youTubePlayer;
            player.setShowFullscreenButton(false);  // prev & next buttons currently disabled from our player

            playlistListView = playlistFragment.getPlaylistListView();
            playlistListView.setOnItemClickListener(new ListClickHandler());

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
                    if (songList.size() > 0) {   // if there are still songs to remove

                        songList_History.add(0, songList.remove(0));
                        //songList.remove(0);  // remove the top song id
                        if(!songList.isEmpty()){ // if there are more videos to load
                            player.loadVideo(songList.get(0).songID); // load the first video on the list
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

    public void addSong(String songID, String songTitle,Bitmap songThumbnail, String thumbnailStr){

        SongData song = new SongData();
        song.songID = songID;
        song.songTitle = songTitle;
        song.songThumbnail = songThumbnail;
        song.songThumbnailURL = thumbnailStr;

        songList.add(song);
        //If the player is not playing and the playlist is less than 1
        //play the song just added to list
        if (!player.isPlaying() && songList.size() <= 1) {
            player.loadVideo(songID);
        }
        playlistFragment.updateListView(); // notify playlistFragment of the changes
    }

    // add song function for our search fragment
    private void addSong(String songID, String songTitle, String songThumbnailURL){

        final SongData song = new SongData();

        song.songID = songID;
        song.songTitle = songTitle;
        song.songThumbnailURL = songThumbnailURL;

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
                song.songThumbnail = result; // add the song thumbnail to playlist
                playlistFragment.updateListView(); // notify playlistFragment of the changes
            }
        }
        new DownloadImageTask().execute(songThumbnailURL); // download the thumbnail for that song and set as bitmap for the imageview
        //DownloadImageTask imageTask = new DownloadImageTask();
        //song.songThumbnail = imageTask.doInBackground(songThumbnailURL);
        songList.add(song);
    }


    /**
     * Initialize all the components needed for WIFI P2P communication
     */
    public void registerReceiver()
    {
        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WifiP2pReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(receiver, intentFilter);
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(WifiP2pReceiver.logType, "Discover Succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(WifiP2pReceiver.logType, "Discover Failed");

            }
        });
        //receiver.createGroup();

    }

    public void p2pTest() {
        try {
            ServerSocket serverSocket = new ServerSocket(9812);
            while (true) {
                Socket socket = serverSocket.accept();
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                Log.d(WifiP2pReceiver.logType, br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that runs p2p server socket
     * and listens for all client sockets
     */
    public void WIFIP2PServer() {
        try {
            serverSocket = new ServerSocket(WifiP2pReceiver.PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                Log.d(WifiP2pReceiver.logType, socket.getRemoteSocketAddress().toString() + " socket Connnected");
                InputStream in = socket.getInputStream();

                int messageType = in.read();
                if(messageType == ClientMainActivity.ADD_NEW_SONG)
                {
                    //Add new song to playlist
                    InputStreamReader read = new InputStreamReader(in, "UTF-8");
                    BufferedReader br = new BufferedReader(read);
                    //read the data being sent from client.
                    final String songId = br.readLine();
                    final String songTitle = br.readLine();
                    final String songThumbnail = br.readLine();
                    final Bitmap thumbnail = getImage(songThumbnail);

                    final SongData song = new SongData();
                    song.songID = songId;
                    song.songTitle = songTitle;
                    song.songThumbnail = thumbnail;
                    song.songThumbnailURL = songThumbnail;

                    if (player != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addSong(song.songID, song.songTitle, song.songThumbnailURL);
                                playlistFragment.updateListView();
                            }
                        });

                    }
                    //Send playlist back to client
                    OutputStream out = socket.getOutputStream();
                    PrintStream outValue = new PrintStream(out);
                    //outValue.println(playlistSongTitles.size()); // send size of songname Array
                    outValue.println(songList.size());

                    for (int i = 0; i < songList.size(); i++) {
                        outValue.println(songList.get(i));
                    }

                }
                else if(messageType == ClientMainActivity.GET_PLAYLIST)
                {
                    //Send playlist back to client
                    OutputStream out = socket.getOutputStream();
                    PrintStream outValue = new PrintStream(out);

                    outValue.println(songList.size()); // send size of songname Array
                    for (int i = 0; i < songList.size(); i++) {
                        outValue.println(songList.get(i));
                    }

                }
                else if(messageType == ClientMainActivity.GET_NOW_PLAYING)
                {
                    //Return data about song playing now
                    String nowPlayingThumbnail = songList.get(0).songThumbnailURL;
                    String nowPlayingTitle = songList.get(0).songTitle;
                    OutputStream out = socket.getOutputStream();
                    PrintStream printStream = new PrintStream(out);
                    printStream.println(nowPlayingThumbnail);
                    printStream.println(nowPlayingTitle);

                }
                else if(messageType == ClientMainActivity.VOTE_SONG)
                {
                    //A client voted for song, do algorithm to process this
                }
                socket.close();
            }
        } catch (IOException e) {
            Log.d(WifiP2pReceiver.logType, "Server closed");
        }
    }
    /**
     * Get the bitmap image from the url passed in
     *
     * @param thumbnailURL
     * @return
     */
    public Bitmap getImage(String thumbnailURL) {
        Bitmap thumbnail = null;

        try {
            InputStream in = new java.net.URL(thumbnailURL).openStream(); // get an input stream from specified url
            thumbnail = BitmapFactory.decodeStream(in);   // decode the inputStream as a Bitmap
        } catch (Exception e) { // printe any errors
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return thumbnail;
    }


    /**
     * Create a voting dialog box when user presses a
     * entry in the song list
     * */
    public void createVotingDialog(int position) {

        final int itemPosition = position;
        AlertDialog.Builder votingDialog = new AlertDialog.Builder(this);
        votingDialog.setTitle("Vote for song");
        votingDialog.setPositiveButton("Vote Up", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Increment the vote counter for the song clicked on
                songList.get(itemPosition).incrementVote();
                Collections.sort(songList, sortByVote);
                playlistFragment.updateListView();
            }
        });
        votingDialog.setNegativeButton("Vote Down", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Decrement the vote counter for the song clicked on
                songList.get(itemPosition).decrementVote();
                Collections.sort(songList, sortByVote);
                playlistFragment.updateListView();
            }
        });
        AlertDialog dialog = votingDialog.create();
        dialog.show();
    }

    public Comparator<SongData> sortByVote = new Comparator<SongData>() {

        public int compare(SongData song1, SongData song2) {
            if (song1.getVoteCount() > song2.getVoteCount()) {
                return -1;
            } else if (song1.getVoteCount() < song2.getVoteCount()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    public class ListClickHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Context context = getApplicationContext();
            //Get the song title from the playlistSongTitles using the index = postion
            //position is the entry in the list clicked
            String chosenSong = songList.get(position).songTitle;
            CharSequence text = "Chosen song: " + chosenSong + " Vote Count: " + songList.get(position).getVoteCount();
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            createVotingDialog(position); //User Votes for the song, either a down vote or up vote
        }
    }
}