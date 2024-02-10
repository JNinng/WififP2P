package top.ninng.wifip2p;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wifi P2P 处理器：发现、连接、建组
 */
public class WifiP2pHandler {

    private String TAG = "WifiP2p";
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private WifiP2pInfo p2pInfo;
    private BroadcastReceiver p2pBroadcastReceiver;
    private IntentFilter filter;
    /**
     * 广播监听参数
     */
    private Activity activity;
    private ArrayList<WifiP2pDevice> peers = new ArrayList<>();
    private ArrayList<Map<String, String>> peersShow = new ArrayList<>();
    private Connect connect;

    public WifiP2pHandler(WifiP2pManager p2pManager, WifiP2pManager.Channel channel, Activity activity, Connect connect) {
        this(p2pManager, channel, getDefaultFilter(), activity, connect);
    }

    public WifiP2pHandler(WifiP2pManager p2pManager, WifiP2pManager.Channel channel, IntentFilter filter, Activity activity, Connect connect) {
        this.p2pManager = p2pManager;
        this.channel = channel;
        this.filter = filter;
        this.activity = activity;
        this.connect = connect;
    }

    public static IntentFilter getDefaultFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }

    public interface Connect {

        /**
         * 连接到地址
         *
         * @param address 地址
         */
        void accepted(String address);

        /**
         * 发现设备
         *
         * @param peers 发现设备列表
         */
        void updatePeerList(List<Map<String, String>> peers);
    }

    /**
     * 创建组，成为组拥有者，接收
     */
    @SuppressLint("MissingPermission")
    public void beGroupOwner() {
        p2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "createGroup onSuccess => ");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "createGroup onFailure => " + reason);
            }
        });
    }

    /**
     * 成为组员，发送
     */
    @SuppressLint("MissingPermission")
    public void connect(String address, WifiP2pManager.ActionListener listener) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.wps.setup = WpsInfo.PBC;

        p2pManager.connect(channel, config, listener);
    }

    /**
     * 发现组
     */
    @SuppressLint("MissingPermission")
    public void discoverPeers() {
        p2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "discoverPeers onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "discoverPeers onFailure" + reason);
            }
        });
    }

    public String getHostAddress() {
        return p2pInfo.groupOwnerAddress.getHostAddress();
    }

    public BroadcastReceiver getP2pBroadcastReceiver() {
        return p2pBroadcastReceiver;
    }

    @SuppressLint("MissingPermission")
    public void init() {
        WifiP2pManager.PeerListListener peerListListener = wifiP2pDeviceList -> {
            peers.clear();
            peersShow.clear();
            Collection<WifiP2pDevice> deviceList = wifiP2pDeviceList.getDeviceList();
            peers.addAll(deviceList);

            for (WifiP2pDevice wifiP2pDevice : deviceList) {
                Map<String, String> wifiMap = new HashMap<>();
                wifiMap.put("name", wifiP2pDevice.deviceName);
                wifiMap.put("address", wifiP2pDevice.deviceAddress);
                peersShow.add(wifiMap);
            }
            if (connect != null) {
                connect.updatePeerList(peersShow);
            }
        };
        WifiP2pManager.ConnectionInfoListener connectionInfoListener = info -> {
            p2pInfo = info;
            if (connect != null) {
                connect.accepted(info.groupOwnerAddress.getHostAddress());
            }
        };
        p2pBroadcastReceiver = new WifiDirectBroadcastReceiver(
                p2pManager,
                channel,
                activity,
                peerListListener,
                connectionInfoListener);
    }

    /**
     * 是否连接
     *
     * @return
     */
    public boolean isConnect() {
        if (p2pInfo == null) {
            return false;
        }
        return p2pInfo.groupFormed;
    }

    public boolean isGroupOwner() {
        if (p2pInfo == null) {
            return false;
        }
        return p2pInfo.isGroupOwner;
    }

    public void stopConnect() {
        stopConnect(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "stopConnect onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "stopConnect onFailure: " + reason);
            }
        });
    }

    public void stopConnect(WifiP2pManager.ActionListener listener) {
        p2pManager.removeGroup(channel, listener);
    }

    public void stopDiscoverPeers() {
        stopDiscoverPeers(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "stopDiscoverPeers onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "stopDiscoverPeers onFailure: " + reason);
            }
        });
    }

    public void stopDiscoverPeers(WifiP2pManager.ActionListener listener) {
        p2pManager.stopPeerDiscovery(channel, listener);
    }
}
