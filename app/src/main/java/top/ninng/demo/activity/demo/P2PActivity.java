package top.ninng.demo.activity.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.JobIntentService;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import top.ninng.demo.R;
import top.ninng.demo.adapter.MyAdapter;
import top.ninng.demo.config.KeyValueConfig;
import top.ninng.demo.service.DefaultFileReceiveJobIntent;
import top.ninng.demo.service.DefaultFileSendJobIntent;
import top.ninng.demo.service.DefaultStringReceiveService;
import top.ninng.demo.service.DefaultStringSendService;
import top.ninng.demo.service.SendService;
import top.ninng.demo.utils.EmptyCheck;
import top.ninng.demo.utils.Uri2FileUtils;
import top.ninng.wifip2p.WifiP2pHandler;

/**
 * @Author OhmLaw
 * @Date 2022/8/13 19:24
 * @Version 1.0
 */
public class P2PActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "WifiP2PActivity";
    private static DefaultStringSendService stringSendService;
    private static DefaultStringReceiveService defaultStringReceiveService;
    private static DefaultFileSendJobIntent defaultFileSendJobIntent;
    private static DefaultFileReceiveJobIntent defaultFileReceiveJobIntent;
    private static int JOB_ID_STRING_RECEIVE = 21;
    private static int JOB_ID_STRING_SEND = 22;
    private static int JOB_ID_FILE_RECEIVE = 41;
    private static int JOB_ID_FILE_SEND = 42;
    Button discoverBtn, createGroupBtn, stopConnectBtn, stopDiscoverPeersBtn, sendMessageBtn, sendFileBtn;
    EditText p2pInfoEditText, sendMessageEditText, logEditText;
    TextView stringReceiveStatus, fileReceiveStatus, sendServiceStatus;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pHandler wifiP2p;
    private ContentReceiver mReceiver;
    private Thread heartbeatThread = null;
    private boolean heartbeat = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5) {
            if (EmptyCheck.notEmpty(data)) {
                Uri uri = data.getData();
                String path = Uri2FileUtils.uriToFileApiQ(getApplicationContext(), uri).getPath();
//                wifiP2p.sendFile(path);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiP2p.getP2pBroadcastReceiver());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiP2p.getP2pBroadcastReceiver(), WifiP2pHandler.getDefaultFilter());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_p2p);

        doRegisterReceiver();
        initWifiP2p();
        initView();
        heartbeat = true;
        heartbeatThread = new Thread(() -> {
            while (heartbeat) {
                try {
                    Thread.sleep(2800);
                    Handler toastHandler = new Handler(Looper.getMainLooper());
                    toastHandler.post(() -> {
                        stringReceiveStatus.setText("✖");
                        fileReceiveStatus.setText("✖");
                        sendServiceStatus.setText("✖");
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        heartbeatThread.start();
        Intent intent = new Intent(getApplicationContext(), SendService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        heartbeat = false;
    }

    /**
     * 注册广播接收者
     */
    private void doRegisterReceiver() {
        mReceiver = new ContentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(KeyValueConfig.SEND_SERVICE_BROADCAST);
        filter.addAction(KeyValueConfig.SEND_SERVICE_HEARTBEAT_BROADCAST);
        filter.addAction(KeyValueConfig.STRING_RECEIVE_SERVICE_HEARTBEAT_BROADCAST);
        filter.addAction(KeyValueConfig.FILE_RECEIVE_SERVICE_HEARTBEAT_BROADCAST);
        filter.addAction(KeyValueConfig.STRING_RECEIVE_SERVICE_BROADCAST);
        filter.addAction(KeyValueConfig.FILE_RECEIVE_SERVICE_BROADCAST);
        filter.addAction(KeyValueConfig.STRING_SEND_SERVICE_BROADCAST);
        filter.addAction(KeyValueConfig.FILE_SEND_SERVICE_BROADCAST);
        registerReceiver(mReceiver, filter);
    }

    public void initView() {
        discoverBtn = findViewById(R.id.discoverBtn);
        createGroupBtn = findViewById(R.id.createGroup);
        stopConnectBtn = findViewById(R.id.stopConnect);
        stopDiscoverPeersBtn = findViewById(R.id.stopDiscoverBtn);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        sendFileBtn = findViewById(R.id.sendFile);

        sendMessageEditText = findViewById(R.id.sendMessageEditText);
        p2pInfoEditText = findViewById(R.id.p2pInfoEditText);
        mRecyclerView = findViewById(R.id.recyclerview);

        logEditText = findViewById(R.id.log);

        stringReceiveStatus = findViewById(R.id.stringReceiveStatus);
        fileReceiveStatus = findViewById(R.id.fileReceiveStatus);
        sendServiceStatus = findViewById(R.id.sendServiceStatus);

        discoverBtn.setOnClickListener(this);
        createGroupBtn.setOnClickListener(this);
        stopConnectBtn.setOnClickListener(this);
        stopDiscoverPeersBtn.setOnClickListener(this);
        sendMessageBtn.setOnClickListener(this);
        sendFileBtn.setOnClickListener(this);

        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
    }

    private void initWifiP2p() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, Looper.myLooper(), null);

        wifiP2p = new WifiP2pHandler(mManager, mChannel, this,
                new WifiP2pHandler.Connect() {
                    @Override
                    public void accepted(String address) {
                        Log.d(TAG, "accepted: isSend: " + wifiP2p.isConnect());
                        if (wifiP2p.isConnect() && wifiP2p.isGroupOwner()) {
                            // 如果是组拥有者没，应该做好监听，处理接收
                            defaultStringReceiveService = new DefaultStringReceiveService();
                            Intent stringIntent = new Intent();
                            stringIntent.setAction(KeyValueConfig.ACTION_RECEIVE_STRING);
//                            defaultFileReceiveJobIntent = new DefaultFileReceiveJobIntent();
//                            Intent fileIntent = new Intent();
//                            fileIntent.setAction(KeyValueConfig.ACTION_RECEIVE_FILE);

//                            JobIntentService.enqueueWork(getApplicationContext(), DefaultFileReceiveJobIntent.class, JOB_ID_FILE_RECEIVE, fileIntent);
                            JobIntentService.enqueueWork(getApplicationContext(), DefaultStringReceiveService.class, JOB_ID_STRING_RECEIVE, stringIntent);
                            Log.d(TAG, "accepted: 创建监听");
                        }
                        Toast.makeText(getApplicationContext(), "连接到：" + "-" + address + " " + wifiP2p.isConnect(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void updatePeerList(List<Map<String, String>> peers) {
                        // 发现的设备列表
                        mAdapter.setList(peers);
                        mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
                            @Override
                            public void OnItemClick(View view, int position) {
                                Map<String, String> data = mAdapter.getData(position);
                                wifiP2p.connect(data.get("address"), new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "连接成功");
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Log.d(TAG, "连接失败");
                                    }
                                });
                            }

                            @Override
                            public void OnItemLongClick(View view, int position) {

                            }
                        });
                    }
                });
        wifiP2p.init();
    }

    protected void sendBroadcast(String content) {
        Intent intent = new Intent();
        intent.setAction(KeyValueConfig.FILE_RECEIVE_SERVICE_BROADCAST);
        intent.putExtra(KeyValueConfig.CONTENT, content);
        sendBroadcast(intent);
    }

    public class ContentReceiver extends BroadcastReceiver {

        int i = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
            String c = intent.getStringExtra(KeyValueConfig.CONTENT);
            switch (intent.getAction()) {
                case KeyValueConfig.STRING_RECEIVE_SERVICE_HEARTBEAT_BROADCAST:
                    stringReceiveStatus.setText("✔");
                    break;
                case KeyValueConfig.FILE_RECEIVE_SERVICE_HEARTBEAT_BROADCAST:
                    fileReceiveStatus.setText("✔");
                    break;
                case KeyValueConfig.SEND_SERVICE_HEARTBEAT_BROADCAST:
                    sendServiceStatus.setText("✔");
                    break;
                case KeyValueConfig.SEND_SERVICE_BROADCAST:
                case KeyValueConfig.FILE_SEND_SERVICE_BROADCAST:
                    logEditText.append(c + ((i++ % 4 == 0) ? "\n" : " || "));
                    break;
                case KeyValueConfig.STRING_RECEIVE_SERVICE_BROADCAST:
                default:
                    if (c != null) {
                        logEditText.append(c + ((i++ % 4 == 0) ? "\n" : " || "));
                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.discoverBtn) {
            wifiP2p.discoverPeers();
        } else if (id == R.id.createGroup) {
            wifiP2p.beGroupOwner();
        } else if (id == R.id.stopConnect) {
            mAdapter.clear();
            wifiP2p.stopConnect();
        } else if (id == R.id.stopDiscoverBtn) {
            wifiP2p.stopDiscoverPeers();
        } else if (id == R.id.sendMessageBtn) {
            sendMessage(sendMessageEditText.getText().toString());
        } else if (id == R.id.sendFile) {
            sendFile();
        }
    }

    private void sendFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 5);
    }

    public void sendMessage(String message) {
        if (wifiP2p.isConnect()) {
            Bundle bundle = new Bundle();
            bundle.putString(KeyValueConfig.ACTION, KeyValueConfig.ACTION_SEND_STRING);
            Log.d(TAG, "sendMessage: " + wifiP2p.getHostAddress());
            bundle.putString(KeyValueConfig.EXTRAS_GROUP_OWNER_ADDRESS, wifiP2p.getHostAddress());
            bundle.putInt(KeyValueConfig.EXTRAS_GROUP_OWNER_PORT, KeyValueConfig.DEFAULT_STRING_EXTRAS_GROUP_OWNER_PORT);
            bundle.putString(KeyValueConfig.SEND_STRING_CONTEXT, message);
            bundle.putString(KeyValueConfig.CONTENT, "sendString");
            Intent intent = new Intent();
            intent.setAction(KeyValueConfig.MAIN_SEND_STRING_BROADCAST);
            intent.putExtras(bundle);
            sendBroadcast(intent);
        }
    }

    public void sendFile(String path) {
        logEditText.append("\n" + "已派发（File）：" + path + " =>>" + " || ");
    }
}
