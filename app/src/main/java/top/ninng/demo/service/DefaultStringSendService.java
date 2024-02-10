package top.ninng.demo.service;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import top.ninng.demo.config.KeyValueConfig;
import top.ninng.demo.utils.EmptyCheck;

/**
 * {@link WifiP2p} 默认string发送实现
 *
 * @Author OhmLaw
 * @Date 2022/8/13 18:20
 * @Version 1.0
 */
public class DefaultStringSendService extends JobIntentService {

    private static String TAG = "DefaultStringSendService";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onWork(intent);
    }

    protected void onWork(@NonNull Intent intent) {
        Log.e(TAG, "onWork:");
        if (intent.getAction().equals(KeyValueConfig.ACTION_SEND_STRING)) {

            Log.e(TAG, "run=>");

            String host = intent.getStringExtra(KeyValueConfig.EXTRAS_GROUP_OWNER_ADDRESS);
            int post = intent.getIntExtra(KeyValueConfig.EXTRAS_GROUP_OWNER_PORT, KeyValueConfig.DEFAULT_STRING_EXTRAS_GROUP_OWNER_PORT);
            String sendString = intent.getStringExtra(KeyValueConfig.SEND_STRING_CONTEXT);

            Socket socket = new Socket();
            OutputStream outputStream = null;

            try {
                socket.bind(null);
                socket.connect(new InetSocketAddress(host, post), KeyValueConfig.SOCKET_TIMEOUT);

                outputStream = socket.getOutputStream();
                outputStream.write(sendString.getBytes());

                sendBroadcast("已发出（String）:" + sendString);
            } catch (IOException e) {
                Log.e(TAG, "bind=>" + e);
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
                if (EmptyCheck.notEmpty(outputStream)) {
                    try {
                        outputStream.close();
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
}
