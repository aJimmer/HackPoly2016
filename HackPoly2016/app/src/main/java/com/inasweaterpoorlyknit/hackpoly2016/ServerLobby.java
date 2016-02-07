package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ServerLobby extends AppCompatActivity implements WifiP2pManager.ChannelListener {


    public String clientString = "test";
    public WifiP2pManager wfManager;
    public WifiP2pManager.Channel wfChannel;
    public WifiBroadcastReciever receiver;
    public IntentFilter intentFilter;
    public static ArrayList peers = new ArrayList<>();
    private WifiP2pManager.PeerListListener peerListListener;
    public TextView textView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = (TextView)findViewById(R.id.serverText);
        /*intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        peerListListener =  new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                // Out with the old, in with the new.
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                // If an AdapterView is backed by this data, notify it
                // of the change.  For instance, if you have a ListView of available
                // peers, trigger an update.
                ////(WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
                if (peers.size() == 0) {
                    //Log.d(WiFiDirectActivity.TAG, "No devices found");
                    return;
                }

            }
        };


        wfManager.discoverPeers(wfChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //wfManager.



            }

            @Override
            public void onFailure(int reason) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TextView textView = (TextView) findViewById(R.id.serverText);

        wfManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        wfChannel = wfManager.initialize(this, Looper.getMainLooper(), null);
        receiver = new WifiBroadcastReciever(wfManager, wfChannel, this);

        connect();
    */
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try(Socket socket = new Socket("192.168.40.22", 9000)) {
                        textView.setText(socket.getInetAddress().getHostAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            };
        Thread thread = new Thread(run);
        thread.start();




    }


    public void connect()
    {

        ;

            final WifiP2pDevice device = (WifiP2pDevice)peers.get(0);
            WifiP2pConfig config =  new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            wfManager.connect(wfChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            System.out.println(device.deviceAddress + "Connected");
                            textView.setText(device.deviceAddress + "Connected");

                        }

                        @Override
                        public void onFailure(int reason) {
                            System.out.println(device.deviceAddress + "Failed");
                            textView.setText(device.deviceAddress + "Connected");

                        }
                    }

            );

    }



    @Override
    public void onChannelDisconnected() {

    }

    public void registerWfdReciever() {
        receiver = new WifiBroadcastReciever(wfManager,wfChannel,this);
        receiver.registerReciever();
    }






    

}
