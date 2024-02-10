package top.ninng.demo.p2p;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import top.ninng.demo.task.SendTaskControlBlock;
import top.ninng.demo.task.SocketCallback;
import top.ninng.demo.task.SocketSendTask;

/**
 * @Author OhmLaw
 * @Date 2022/10/5 23:46
 * @Version 1.0
 */
public class SendServiceThreadPool implements Runnable {

    private static final String TAG = "SendThreadPool";

    private static final Object sLock = new Object();
    private static final ThreadFactory nameThreadFactory = new ThreadFactoryBuilder().setNamePrefix(
            "SendManagementService".concat("_")).build();
    private ThreadPoolExecutor threadPoolExecutor;
    private ArrayList<SendTaskControlBlock> taskControlBlocList;
    private Map<String, Integer> indexTaskIdMap;
    private SocketCallback socketCallback;
    /**
     * 就绪任务索引
     */
    private int index = 0;

    public SendServiceThreadPool() {
        taskControlBlocList = new ArrayList<>();
        threadPoolExecutor = new ThreadPoolExecutor(
                8,
                16,
                100,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(512),
                nameThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        indexTaskIdMap = new HashMap<>();
        socketCallback = new SocketCallback() {
            @Override
            public void onError(int index, Exception e) {
            }

            @Override
            public void onFinish(int index) {
            }

            @Override
            public void onProgress(int index, float progress) {
            }

            @Override
            public void setResult(int index, String result) {

            }

        };
    }

    public void addFileSendTask(String taskId, String host, int port, String filePath) {
        addFileSendTask(taskId, host, port, filePath, socketCallback);
    }

    public void addFileSendTask(String taskId, String host, int port, String filePath, SocketCallback callback) {
        SocketSendTask.FileSendTask fileSendTask = new SocketSendTask.FileSendTask(host, port, filePath);
        fileSendTask.setCallback(callback);
        synchronized (sLock) {
            fileSendTask.setIndex(taskControlBlocList.size());
            this.taskControlBlocList.add(new SendTaskControlBlock(taskId,
                    fileSendTask));
            indexTaskIdMap.put(taskId, taskControlBlocList.size() - 1);
        }
    }

    public void addStringSendTask(String taskId, String host, int port, String content) {
        addStringSendTask(taskId, host, port, content, socketCallback);
    }

    public void addStringSendTask(String taskId, String host, int port, String content, SocketCallback callback) {
        SocketSendTask.StringSendTask stringSendTask = new SocketSendTask.StringSendTask(host, port, content);
        stringSendTask.setCallback(callback);
        synchronized (sLock) {
            stringSendTask.setIndex(taskControlBlocList.size());
            this.taskControlBlocList.add(new SendTaskControlBlock(taskId,
                    stringSendTask));
            indexTaskIdMap.put(taskId, taskControlBlocList.size() - 1);
        }
    }

    @Override
    public void run() {
        SendTaskControlBlock sendTaskControlBlock;
        while (true) {
            synchronized (sLock) {
                if (taskControlBlocList.size() > index) {
                    sendTaskControlBlock = taskControlBlocList.get(index);
                    threadPoolExecutor.submit(sendTaskControlBlock.getTaskRunnable());
                    index++;

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Interrupted=>" + e);
                    }

                    Log.d(TAG, "\n----");
                    for (SendTaskControlBlock taskControlBlock1 : taskControlBlocList) {
                        Log.d(TAG, "taskControlBlocList => "
                                + taskControlBlock1.getTaskId()
                                + ":"
                                + ((taskControlBlock1.getTaskRunnable() instanceof SocketSendTask.StringSendTask) ? "sting" : "file")
                                + ":");
                    }
                    Log.d(TAG, "----\n");
                }
            }
        }
    }
}
