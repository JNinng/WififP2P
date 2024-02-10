package top.ninng.demo.service;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import top.ninng.demo.config.KeyValueConfig;
import top.ninng.demo.model.FileTransfer;
import top.ninng.demo.utils.EmptyCheck;

/**
 * 接收File
 *
 * @Author OhmLaw
 * @Date 2022/8/11 14:07
 * @Version 1.0
 */
public class DefaultFileReceiveJobIntent extends JobIntentService {

    private static String TAG = "DefaultFileReceiveService";
    private OnProgressChangListener onProgressChangListener = null;
    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;
    private InputStream inputStream = null;
    private ObjectInputStream objectInputStream = null;
    private FileOutputStream fileOutputStream = null;
    private FileTransfer fileTransfer = null;
    private File newFile = null;
    private Thread heartbeatThread = null;
    private boolean heartbeat = false;

    public interface OnProgressChangListener {

        //当传输进度发生变化时
        void onProgressChanged(FileTransfer fileTransfer, int progress);

        //当传输结束时
        void onTransferFinished(File file);
    }

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
        if (intent.getAction().equals(KeyValueConfig.ACTION_RECEIVE_FILE)) {
            Log.e(TAG, "run=>");
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(KeyValueConfig.DEFAULT_FILE_EXTRAS_GROUP_OWNER_PORT));

                while (true) {
                    Log.i(TAG, "阻塞等待...." + this);
                    clientSocket = serverSocket.accept();
                    Log.i(TAG, "客户端IP=>" + clientSocket.getInetAddress());

                    inputStream = clientSocket.getInputStream();
                    objectInputStream = new ObjectInputStream(inputStream);
                    fileTransfer = (FileTransfer) objectInputStream.readObject();
                    String fileName = fileTransfer.getFileName();
                    newFile = new File(Environment.getExternalStorageDirectory()
                            + "/" + "top.ninng.wifip2p" + "/" + fileName);
                    fileOutputStream =
                            new FileOutputStream(newFile);
                    File dirs = new File(newFile.getParent());

                    if (!dirs.exists()) {
                        if (!dirs.mkdirs()) {
                            Log.e(TAG, "dirCreate=>");
                        }
                    }
                    if (!newFile.createNewFile()) {
                        Log.e(TAG, "createNewFile=>");
                    }
                    Log.i(TAG, "待接收文件=>" + fileName);

                    byte[] buf = new byte[1024];
                    long fileLength = fileTransfer.getFileLength();
                    int len;
                    long total = 0;
                    int progress;
                    while ((len = inputStream.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, len);
                        total += len;
                        progress = (int) ((total * 100) / fileLength);
                        Log.d(TAG, "进度：" + progress + "%");
                        if (onProgressChangListener != null) {
                            onProgressChangListener.onProgressChanged(fileTransfer, progress);
                        }
                    }

                    sendBroadcast("ReceiverFile: " + fileName);
                }
            } catch (IOException e) {
                Log.e(TAG, "serverSocket=>" + e);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "readObject=>" + e);
            } finally {
                if (EmptyCheck.notEmpty(clientSocket)) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "clientSocket_close=>" + e);
                    }
                }
                if (EmptyCheck.notEmpty(serverSocket)) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "serverSocket_close=>" + e);
                    }
                }
                if (EmptyCheck.notEmpty(inputStream)) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "inputStream_close=>");
                    }
                }
                if (EmptyCheck.notEmpty(fileOutputStream)) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "fileOutputStream_close");
                    }
                }
                if (EmptyCheck.notEmpty(objectInputStream)) {
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "objectInputStream_close=>" + e);
                    }
                }
                Log.d(TAG, "onHandleWork: end!");
            }
        }
    }

    /**
     * 发送广播
     */
    protected void sendBroadcast(String content) {
        Intent intent = new Intent();
        intent.setAction(KeyValueConfig.FILE_RECEIVE_SERVICE_BROADCAST);
        intent.putExtra(KeyValueConfig.CONTENT, content);
        sendBroadcast(intent);
    }

    /**
     * 发送心跳广播
     */
    protected void sendHeartbeatBroadcast() {
        Intent intent = new Intent();
        intent.setAction(KeyValueConfig.FILE_RECEIVE_SERVICE_HEARTBEAT_BROADCAST);
        sendBroadcast(intent);
    }

    public void setOnProgressChangListener(OnProgressChangListener onProgressChangListener) {
        this.onProgressChangListener = onProgressChangListener;
    }
}
