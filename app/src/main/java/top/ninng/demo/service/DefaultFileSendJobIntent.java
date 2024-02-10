package top.ninng.demo.service;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import top.ninng.demo.config.KeyValueConfig;
import top.ninng.demo.model.FileTransfer;
import top.ninng.demo.utils.EmptyCheck;
import top.ninng.demo.utils.MD5Utils;

/**
 * 发送File
 *
 * @Author OhmLaw
 * @Date 2022/8/9 14:18
 * @Version 1.0
 */
public class DefaultFileSendJobIntent extends JobIntentService {

    private static String TAG = "DefaultFileTransferService";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        Log.e(TAG, "run=>");

        Socket socket = null;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        InputStream inputStream = null;
        File file = null;

        long fileLength;
        if (intent.getAction().equals(KeyValueConfig.ACTION_SEND_FILE)) {
            String host = intent.getStringExtra(KeyValueConfig.EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getIntExtra(KeyValueConfig.EXTRAS_GROUP_OWNER_PORT,
                    KeyValueConfig.DEFAULT_FILE_EXTRAS_GROUP_OWNER_PORT);

            String filePath = intent.getStringExtra(KeyValueConfig.SEND_FILE_PATH);
            file = new File(filePath);

            FileTransfer fileTransfer = new FileTransfer();
            fileTransfer.setFileName(file.getName());
            fileLength = file.length();
            fileTransfer.setFileLength(fileLength);
            fileTransfer.setMd5(MD5Utils.fileGetMD5(file));

            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect(new InetSocketAddress(host, port), KeyValueConfig.SOCKET_TIMEOUT);

                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);

                objectOutputStream.writeObject(fileTransfer);

                inputStream = new FileInputStream(file);
                int aSendLength = 0;
                long total = 0;
                byte[] buf = new byte[1024];
                while ((aSendLength = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, aSendLength);
                    total += aSendLength;
                    int progress = (int) ((total * 100) / fileLength);
                    Log.d(TAG, "进度：" + progress + "%");
                }
                Log.d(TAG, "send file ok! : " + file.getPath());

                sendBroadcast("已发出（File）:" + file.getName());
            } catch (IOException e) {
                Log.e(TAG, "bind=>" + e);
            } finally {
                if (EmptyCheck.notEmpty(socket)) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "socket_close=>" + e);
                    }
                }
                if (EmptyCheck.notEmpty(inputStream)) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "inputStream_close=>" + e);
                    }
                }
                if (EmptyCheck.notEmpty(outputStream)) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "outputStream_close=>" + e);
                    }
                }
                if (EmptyCheck.notEmpty(objectOutputStream)) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "objectOutputStream_close=>" + e);
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
        intent.setAction(KeyValueConfig.FILE_SEND_SERVICE_BROADCAST);
        intent.putExtra(KeyValueConfig.CONTENT, content);
        sendBroadcast(intent);
    }
}
