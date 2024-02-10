package top.ninng.demo.service;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import top.ninng.demo.config.KeyValueConfig;
import top.ninng.demo.utils.EmptyCheck;

/**
 * 接收String消息
 *
 * @Author OhmLaw
 * @Date 2022/8/13 18:32
 * @Version 1.0
 */
public class DefaultStringReceiveService extends JobIntentService {

    private static String TAG = "DefaultStringReceiveService";
    private static int work = 4;
    private Thread heartbeatThread = null;
    private boolean heartbeat = false;

    @Override
    public void onCreate() {
        super.onCreate();
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
    public void onDestroy() {
        super.onDestroy();
        heartbeat = false;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onWork(intent);
    }

    protected void onWork(@NonNull Intent intent) {
        if (intent.getAction().equals(KeyValueConfig.ACTION_RECEIVE_STRING)) {
            Log.e(TAG, "run=>");

            ServerSocket serverSocket = null;
            Socket socket = null;
            InputStream inputStream = null;
            ByteArrayOutputStream byteArrayOutputStream = null;

            try {
                serverSocket = new ServerSocket(KeyValueConfig.DEFAULT_STRING_EXTRAS_GROUP_OWNER_PORT);
//                while (work-- > 0) {
                while (heartbeat) {
                    Log.i(TAG,
                            "阻塞等待...." + this + "\n\t" + work + " \n" + serverSocket.getInetAddress() + ":"
                                    + serverSocket.getLocalPort());
                    socket = serverSocket.accept();
                    Log.i(TAG, "客户端IP=>" + socket.getInetAddress());

                    inputStream = socket.getInputStream();
                    byteArrayOutputStream = new ByteArrayOutputStream();

                    int i;
                    while ((i = inputStream.read()) != -1) {
                        byteArrayOutputStream.write(i);
                    }

                    String content = byteArrayOutputStream.toString();
                    sendBroadcast("ReceiverString:" + content);
                }
                try {
                    Thread.sleep(500);
                    Log.d(TAG, "====");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(TAG, "socket=>" + e);
            } finally {
                if (EmptyCheck.notEmpty(socket)) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            Log.e(TAG, "close=>" + e);
                        }
                    }
                }
                if (EmptyCheck.notEmpty(serverSocket)) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close=>" + e);
                    }
                }
                if (EmptyCheck.notEmpty(inputStream)) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close=>" + e);
                    }
                }
                if (EmptyCheck.notEmpty(byteArrayOutputStream)) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close=>" + e);
                    }
                }
            }
        }
    }

    /**
     * 发送广播
     */
    protected void sendBroadcast(String content) {
        Intent intent = new Intent();
        intent.setAction(KeyValueConfig.STRING_RECEIVE_SERVICE_BROADCAST);
        intent.putExtra(KeyValueConfig.CONTENT, content);
        sendBroadcast(intent);
    }

    /**
     * 发送心跳广播
     */
    protected void sendHeartbeatBroadcast() {
        Intent intent = new Intent();
        intent.setAction(KeyValueConfig.STRING_RECEIVE_SERVICE_HEARTBEAT_BROADCAST);
        sendBroadcast(intent);
    }
}
