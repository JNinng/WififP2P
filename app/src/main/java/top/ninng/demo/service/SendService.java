package top.ninng.demo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import java.time.LocalTime;

import top.ninng.demo.config.KeyValueConfig;
import top.ninng.demo.p2p.SendServiceThreadPool;
import top.ninng.demo.task.SocketCallback;

/**
 * 发送String信息
 *
 * @Author OhmLaw
 * @Date 2022/8/11 22:03
 * @Version 1.0
 */
public class SendService extends Service {

    private static String TAG = "SendService";
    private boolean heartbeat = false;
    private Thread heartbeatThread = null;
    private SendServiceThreadPool threadPool;
    private Thread runThread;
    private ContentReceiver mReceiver;

    public class ContentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "receiveMessage=>");
            switch (intent.getAction()) {
                case KeyValueConfig.MAIN_SEND_STRING_BROADCAST:
                    Bundle data = intent.getExtras();
                    Log.d(TAG, "receiveMessage=>" + data.getString(KeyValueConfig.ACTION));

                    if (KeyValueConfig.ACTION_SEND_STRING.equals(data.getString(KeyValueConfig.ACTION)) ||
                            KeyValueConfig.ACTION_SEND_FILE.equals(data.getString(KeyValueConfig.ACTION))) {
                        onWork(data);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        threadPool = new SendServiceThreadPool();
        runThread = new Thread(threadPool);
        runThread.start();
        mReceiver = new ContentReceiver();
        IntentFilter filter = new IntentFilter(KeyValueConfig.MAIN_SEND_STRING_BROADCAST);
        registerReceiver(mReceiver, filter);
        filter.addAction(KeyValueConfig.SEND_SERVICE_BROADCAST);
        heartbeat = true;
        heartbeatThread = new Thread(() -> {
            while (heartbeat) {
                sendHeartbeatBroadcast();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        heartbeatThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 发送心跳广播
     */
    protected void sendHeartbeatBroadcast() {
        Intent intent = new Intent();
        intent.setAction(KeyValueConfig.SEND_SERVICE_HEARTBEAT_BROADCAST);
        sendBroadcast(intent);
    }

    public void receiveMessage(Message msg) {
        Bundle data = msg.getData();

        if (KeyValueConfig.ACTION_SEND_STRING.equals(data.getString(KeyValueConfig.ACTION)) ||
                KeyValueConfig.ACTION_SEND_FILE.equals(data.getString(KeyValueConfig.ACTION))) {
            onWork(data);
        }
    }

    @SuppressWarnings("all")
    public void onWork(Bundle bundle) {
        new Thread(() -> {
            String host = bundle.getString(KeyValueConfig.EXTRAS_GROUP_OWNER_ADDRESS);
            int port = bundle.getInt(KeyValueConfig.EXTRAS_GROUP_OWNER_PORT);
            if (KeyValueConfig.ACTION_SEND_STRING.equals(bundle.get(KeyValueConfig.ACTION))) {
                String content = bundle.getString(KeyValueConfig.SEND_STRING_CONTEXT);
                threadPool.addStringSendTask(String.valueOf(LocalTime.now().getNano()), host, port, content, new SocketCallback() {
                    @Override
                    public void onError(int index, Exception e) {

                    }

                    @Override
                    public void onFinish(int index) {
                        sendBroadcast("已发出（String）：" + content);
                    }

                    @Override
                    public void onProgress(int index, float progress) {

                    }

                    @Override
                    public void setResult(int index, String result) {

                    }
                });
                sendBroadcast("已派发（String）：" + content + " =>>");
            } else if (KeyValueConfig.ACTION_SEND_FILE.equals(bundle.get(KeyValueConfig.ACTION))) {
                String path = bundle.getString(KeyValueConfig.SEND_FILE_PATH);
                threadPool.addFileSendTask(String.valueOf(LocalTime.now().getNano()), host, port, path);
                sendBroadcast("已派发（File）：" + path + " =>>");
            }
        }).start();
    }

    /**
     * 发送广播
     */
    protected void sendBroadcast(String content) {
        Intent intent = new Intent();
        intent.setAction(KeyValueConfig.SEND_SERVICE_BROADCAST);
        intent.putExtra(KeyValueConfig.CONTENT, content);
        sendBroadcast(intent);
    }
}
