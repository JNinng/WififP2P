package top.ninng.demo.task;

import android.util.Log;

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
 * @Author OhmLaw
 * @Date 2022/10/4 16:23
 * @Version 1.0
 */
public class SocketSendTask {

    public interface Callback {
        void callback();
    }

    public static class FileSendTask implements Runnable {

        private static final String TAG = "SendFileTask";
        private static int I = 0;

        int index = 0;
        String host;
        int port;
        String filePath;

        boolean stop = false;
        boolean pause = false;
        float updateCycle = 0.1F;
        SocketCallback callback;

        public FileSendTask(String host, int port, String filePath) {
            this.index = I++;
            this.host = host;
            this.port = port;
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            // TODO 保存暂态的传输
            // TODO 更好的读写逻辑
            Log.d(TAG, "file initRun");
            File file = new File(filePath);
            long fileLength = file.length();
            FileTransfer fileTransfer = new FileTransfer(
                    file.getName(),
                    fileLength,
                    MD5Utils.fileGetMD5(file));

            Socket socket = null;
            OutputStream outputStream = null;
            ObjectOutputStream objectOutputStream = null;
            InputStream inputStream = null;


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
                float newProgress = 0F;
                float oldProgress = 0F;
                byte[] buf = new byte[1024];
                while (!stop) {
                    while (!pause && !stop && (aSendLength = inputStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, aSendLength);
                        total += aSendLength;

                        newProgress = (int) ((total * 100) / fileLength);
                        if (EmptyCheck.notEmpty(callback)) {
                            if ((newProgress - oldProgress > updateCycle || newProgress >= 100)) {
                                callback.onProgress(index, newProgress);
                                oldProgress = newProgress;
                                break;
                            }
                        }
                    }
                }
                Log.d(TAG, "file ok");
                if (EmptyCheck.notEmpty(callback)) {
                    callback.onFinish(index);
                }
            } catch (IOException e) {
                Log.e(TAG, "bind=>" + e);
                if (EmptyCheck.notEmpty(callback)) {
                    callback.onError(index, e);
                }
            } finally {
                if (EmptyCheck.notEmpty(socket)) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "socket_close=>" + e);
                        if (EmptyCheck.notEmpty(callback)) {
                            callback.onError(index, e);
                        }
                    }
                }
                if (EmptyCheck.notEmpty(inputStream)) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "inputStream_close=>" + e);
                        if (EmptyCheck.notEmpty(callback)) {
                            callback.onError(index, e);
                        }
                    }
                }
                if (EmptyCheck.notEmpty(outputStream)) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "outputStream_close=>" + e);
                        if (EmptyCheck.notEmpty(callback)) {
                            callback.onError(index, e);
                        }
                    }
                }
                if (EmptyCheck.notEmpty(objectOutputStream)) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "objectOutputStream_close=>" + e);
                        if (EmptyCheck.notEmpty(callback)) {
                            callback.onError(index, e);
                        }
                    }
                }
            }
        }

        public void setCallback(SocketCallback callback) {
            this.callback = callback;
        }
    }

    public static class StringSendTask implements Runnable {

        private static final String TAG = "SendString";
        private static int I = 0;

        int index = 0;
        String host;
        int port;
        String content;
        SocketCallback callback;

        public StringSendTask(String host, int port, String content) {
            this.host = host;
            this.port = port;
            this.content = content;
        }

        public SocketCallback getCallback() {
            return callback;
        }

        public void setCallback(SocketCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            Log.d(TAG, "initRun" + content);
            Socket socket = null;
            OutputStream outputStream = null;

            socket = new Socket();
            try {
                socket.bind(null);
                socket.connect(new InetSocketAddress(host, port), KeyValueConfig.SOCKET_TIMEOUT);

                outputStream = socket.getOutputStream();
                outputStream.write(content.getBytes());

                Log.d(TAG, "send ok");
                if (EmptyCheck.notEmpty(callback)) {
                    Log.d(TAG, "callback onFinish 0");
                    callback.onFinish(index);
                }
            } catch (IOException e) {
                Log.e(TAG, "onBind=>" + e);
                if (EmptyCheck.notEmpty(callback)) {
                    callback.onError(index, e);
                }
            } finally {
                if (EmptyCheck.notEmpty(socket)) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "socket_close=>" + e);
                    }
                }
                if (EmptyCheck.notEmpty(outputStream)) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "outputStream_close=>" + e);
                    }
                }
            }
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
