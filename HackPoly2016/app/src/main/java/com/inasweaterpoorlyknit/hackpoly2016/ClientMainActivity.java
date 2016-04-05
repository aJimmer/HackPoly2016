package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.regex.Pattern;

public class ClientMainActivity extends AppCompatActivity {
    
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.songrequest";
    public String songRequest;
    Button sendRequest;
    Button connectToHost;

    //private ListView PLAYLISTTITLES;
    //private ArrayList<String> songList;
    //private ArrayAdapter<String> listAdapter;

    private CardView nowPlayingCard;
    private ImageView nowPlayingThumbnailView;
    private TextView nowPlayingText;
    private Bitmap nowPlayingThumbnail;
    private String nowPlayingThumbnailURL;

    private ArrayList<String> playlistTitles;
    private ArrayList<Bitmap> playlistThumbnails;
    private ArrayList<String> thumbnailURLs;
    private ArrayList<String> historyTitles;
    private ArrayList<Bitmap> historyThumbnails;
    private HashMap<String, Bitmap> thumbnailsDownloaded;

    private ViewPager viewPager;    // view pager will link our three fragments
    private TabLayout tabLayout;    // the tabs that initiate the change between fragments

    private HistoryFragment historyFragment;       // fragment to display playlist history
    private PlaylistFragment playlistFragment;      // fragment to display the current playlist
    private SearchFragment searchFragment;          // fragment to allow searching and adding new songs
    private DiscoverPartyFragment discoverPartyFragment;

    SharedPreferences prefs;
    private String returnedVideoID;
    private String returnedVideoTitle;
    private String returnVideoThumbnail;

    public String ipStr;
    TextView hostDisplay;
    private String hostAddress;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pReceiver receiver;
    private IntentFilter  intentFilter;

    public static final int GET_PLAYLIST = 1;
    //private static final int SEARCH_CODE = 2;
    public static final int ADD_NEW_SONG = 3;
    public static final int VOTE_SONG = 4;
    public static final int GET_NOW_PLAYING = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        playlistTitles = new ArrayList<>();
        playlistThumbnails = new ArrayList<>();
        thumbnailURLs = new ArrayList<>();
        historyTitles = new ArrayList<>();
        historyThumbnails = new ArrayList<>();
        thumbnailsDownloaded = new HashMap<>();

        // initialize playlist fragment with current tracks
        playlistFragment = new PlaylistFragment();  // intialize playlist fragment
        playlistFragment.setPlaylistAdapter(this, playlistTitles, playlistThumbnails);

        // initialize search fragment
        searchFragment = new SearchFragment();

        // initialize history fragment
        historyFragment = new HistoryFragment();  // intialize history fragment
        historyFragment.setPlaylistAdapter(this, historyTitles, historyThumbnails);

        // initialize discovery party fragment
        discoverPartyFragment = new DiscoverPartyFragment();

        // initialize the viewPager to link to the three fragments(Playlist, Search, History)
        viewPager = (ViewPager) findViewById(R.id.client_viewpager);
        setupViewPager(viewPager);

        // initialize the tablayout with the info from viewPager
        tabLayout = (TabLayout) findViewById(R.id.client_tabs);
        tabLayout.setupWithViewPager(viewPager);

        debugCardView();    // displaying the card view with hard coded data

        ipStr ="";
        hostAddress = null;

        registerReceiver();

        nowPlayingCard = (CardView)findViewById(R.id.client_card);
        nowPlayingThumbnailView = (ImageView)nowPlayingCard.findViewById(R.id.cardThumbail);
        nowPlayingText = (TextView)nowPlayingCard.findViewById(R.id.now_playing_song_title);
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
    }

    //   setupViewPager is a layout manager that allows us to flip left and right through fragments
    //   we initialize it with our PlaylistFragment, SearchFragment, and HistoryFragment
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager()); // initialize a viewPagerAdapter
        adapter.addFragment(playlistFragment, getResources().getString(R.string.title_playlist));   // Add the playlist,
        adapter.addFragment(searchFragment, getResources().getString(R.string.title_search));       // search, and
        adapter.addFragment(historyFragment, getResources().getString(R.string.title_history));     // history fragments
        adapter.addFragment(discoverPartyFragment, getResources().getString(R.string.title_discover_party));
        viewPager.setAdapter(adapter);  // set the adapter to our viewPager
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

    /**
     *
     * @param songId
     * @param songName
     * @param songThumbnail
     * @param songThumbnailURL
     */
    public void addSong(String songId, String songName, Bitmap songThumbnail, String songThumbnailURL) {
        getNowPlaying();

        Socket socket = null;
        try {
            socket = new Socket(hostAddress, WifiP2pReceiver.PORT);
            //Send song id and song name to sever
            OutputStream os = socket.getOutputStream();
            os.write(ClientMainActivity.ADD_NEW_SONG);
            PrintStream out = new PrintStream(os);
            out.println(songId);
            out.println(songName);
            out.println(songThumbnailURL);

            //Recieve Playlist from server
            InputStream in = socket.getInputStream();
            InputStreamReader read = new InputStreamReader(in, "UTF-8");
            BufferedReader br = new BufferedReader(read);
            int playlistSize = Integer.parseInt(br.readLine());
            playlistTitles.clear();
            thumbnailURLs.clear();
            for(int i = 0; i < playlistSize; i++)
            {
                playlistTitles.add(br.readLine());
                //Get the thumbnailURL from server
                String thumbnailURL = br.readLine();
                //Add to arraylist of URLS and check if the thumbnail has
                //been downloaded already
                thumbnailURLs.add(thumbnailURL);
                if (!thumbnailsDownloaded.containsKey(thumbnailURL)) {
                    Bitmap thumbnail = getImage(thumbnailURL);
                    //Put the bitmap associated the thumbnail URL is the hashmap
                    thumbnailsDownloaded.put(thumbnailURL, thumbnail);
                }
            }
            playlistThumbnails.clear();
            //Go through every string value in the new thumbnailURL list
            //and add the Bitmaps associated with these URLS to the
            //playlistThumbnails arraylist
            for (int i = 0; i < thumbnailURLs.size(); i++) {
                Bitmap tempThumbnail = thumbnailsDownloaded.get(thumbnailURLs.get(i));
                playlistThumbnails.add(tempThumbnail);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //listAdapter.notifyDataSetChanged();
                    playlistFragment.updateListView();
                }
            });

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getNowPlaying()
    {
        Socket socket = null;
        try {
            socket = new Socket(hostAddress, WifiP2pReceiver.PORT);
            //Notify server client wants the song now playing
            OutputStream os = socket.getOutputStream();
            os.write(ClientMainActivity.GET_NOW_PLAYING);

            InputStream in = socket.getInputStream();
            InputStreamReader ins = new InputStreamReader(in, "UTF-8");
            BufferedReader br = new BufferedReader(ins);

            //Get the bitmap and song title
            final String thumbnailURL = br.readLine();
            final String nowPlayingSongTitle = br.readLine();
            if(!nowPlayingThumbnailURL.equals(thumbnailURL)){
                historyTitles.add(0, nowPlayingText.getText().toString()); // remove the top song title and place it in front of historySongTitles
                historyThumbnails.add(0, nowPlayingThumbnail); // remove the top song thumbnail and place it in front of historyThumbnails
                nowPlayingThumbnailURL = thumbnailURL;

                final Bitmap nowPlayingThumb = getImage(thumbnailURL);
                socket.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateNowPlaying(nowPlayingThumb, nowPlayingSongTitle);
                        historyFragment.updateListView();   // update history's list view
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the thumbnail and song title for now playing card
     * @param thumbnail
     * @param songTitle
     */
    public void updateNowPlaying(Bitmap thumbnail, String songTitle)
    {
        nowPlayingThumbnail = thumbnail;
        nowPlayingThumbnailView.setImageBitmap(nowPlayingThumbnail);
        nowPlayingText.setText(songTitle);
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

    /**
     * Initialize all the components needed for wifi p2p
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

    }

    /**
     * Gets the ip address from wifi p2p connection establishment
     * @param groupOwnerIpAddress
     */
    public void setHostIpAddress(String groupOwnerIpAddress) {
        this.hostAddress = groupOwnerIpAddress;
        Log.d(WifiP2pReceiver.logType, "Server's address: " + hostAddress);
    }
    public void debugCardView()
    {
        nowPlayingThumbnailURL = "https://i.ytimg.com/vi/S-Xm7s9eGxU/default.jpg";

        //debug cardView
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final CardView cardView =(CardView)findViewById(R.id.client_card);

                final ImageView cardImage = (ImageView)cardView.findViewById(R.id.cardThumbail);
                final Bitmap testThumb = getImage("https://i.ytimg.com/vi/S-Xm7s9eGxU/default.jpg");
                nowPlayingThumbnail = testThumb;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cardImage.setImageBitmap(testThumb);
                    }
                });

            }
        };
        Thread debugThread = new Thread(runnable);
        debugThread.start();
    }
    public Bitmap getImage(String thumbnailUrl) {
        Bitmap thumbnail = null;
        try {
            InputStream in = new java.net.URL(thumbnailUrl).openStream();
            thumbnail = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnail;
    }
}
