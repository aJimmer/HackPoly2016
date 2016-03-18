package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 * This class will handle all the Wifi P2P setup and communication
 * Created by Raymond Arias on 3/18/16.
 */
public class WifiP2pReceiver extends BroadcastReceiver{
    public static String logType = "WIFI P2p";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pDeviceList mDeviceList;
    private ArrayList<WifiP2pDevice> mPeerList;
    private ServerLobby serverLobby;
    private ClientMainActivity clientMainActivity;

    public WifiP2pReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, AppCompatActivity activity)
    {
        mManager = manager;
        mChannel = channel;
        mPeerList = new ArrayList<>();
        mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                //Do something with list of peers
                mPeerList.clear();
                mPeerList.addAll(peers.getDeviceList());
            }
        };
        if(activity instanceof ServerLobby)
            serverLobby = (ServerLobby)activity;
        else if(activity instanceof  ClientMainActivity)
            clientMainActivity = (ClientMainActivity)activity;


    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            //Check to see if Wifi P2P is enabled
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Log.d(logType, "WIFI P2P is running");
            }
            else
            {
                Log.d(logType, "WIFI P2P is not running");
            }
        }
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            //Call request peers to get new list of avaiable peers
            if(mManager != null)
            {
                mManager.requestPeers(mChannel, mPeerListListener);
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            //Respond to new connections or disconnections
            final NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo != null && networkInfo.isConnected())
            {
                //Open new connection

                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                        if(info.groupFormed)
                        {
                            Runnable networkTask = new Runnable() {
                                @Override
                                public void run() {
                                    if(info.isGroupOwner)
                                    {
                                        //Run WIFI P2p Server
                                    }
                                    else
                                    {
                                        //Run WIFI P2P Client
                                    }

                                }
                            };

                            Thread networkThread = new Thread(networkTask);
                            networkThread.start();
                        }
                        else
                        {
                            Log.d(logType, "Conection lost");
                        }
                    }
                });
            }

        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {

            //Respond to this device's wifi state changing
            WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d(logType, "This device name: " + thisDevice.deviceName);
            Log.d(logType, "This device addreess: " + thisDevice.deviceAddress);
        }


    }
}
