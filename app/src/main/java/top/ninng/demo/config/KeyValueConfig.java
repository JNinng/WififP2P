package top.ninng.demo.config;

import android.os.Message;

/**
 * 键（key）相关的约定
 *
 * @Author OhmLaw
 * @Date 2022/10/3 23:15
 * @Version 1.0
 */
public class KeyValueConfig {

    /**
     * 正文key
     */
    public static final String CONTENT = "context";
    /**
     * 任务类型
     */
    public static final String SEND_STRING = "send_string";
    public static final String RECEIVE_STRING = "receive_string";
    public static final String SEND_FILE = "send_file";
    public static final String RECEIVE_FILE = "receive_file";

    /**
     * {@link Message}传输相关
     * <p>
     * 消息类型{@link Message.what}
     * <p>
     * 请求订阅消息
     */
    public static final int REGISTER_MESSAGE = 0;
    /**
     * 标识（id），Activity、Service实例唯一标识
     */
    public static final String ID = "activity_service_id";
    /**
     * 类似Linux权限的数据列
     * 1 2 4 组合： 1 2 3 4 5 6 7
     * 1 2 4 8组合：1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
     * ni = 2^(i-1) i>0
     */
    public static final String MESSAGE_TYPE = "message_type";
    /**
     * 一个任务ID {@link TASK_ID} 由 {@link ID}+{@link SEPARATOR}+{@link TASK_ID} 组成，
     * 分隔符：{@link SEPARATOR}
     * 如：x4x_g456s
     * <p>
     * 用于识别任务及其发布者
     */
    public static final String TASK_ID = "task_id";
    /**
     * 任务的后缀
     */
    public static final String TASK_SUFFIX = "task_suffix";
    /**
     * 分隔符
     */
    public static final String SEPARATOR = "_";
    /**
     * 返回：ok,error
     * <p>
     * 同时应返回key为{@link CONTENT}的具体信息
     */
    public static final String TASK_RESPONSE = "task_response";
    public static final int SOCKET_TIMEOUT = 10000;
    public static final String ACTION = "action";
    public static final String ACTION_SEND_FILE = "top.ninng.wifip2p.SEND_FILE";
    public static final String ACTION_SEND_STRING = "top.ninng.wifip2p.SEND_STRING";
    public static final String ACTION_RECEIVE_FILE = "top.ninng.wifip2p.RECEIVE_FILE";
    public static final String ACTION_RECEIVE_STRING = "top.ninng.wifip2p.RECEIVE_STRING";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static final String SEND_FILE_PATH = "send_file_uri";
    public static final String SEND_STRING_CONTEXT = "send_context";
    public static final String MAIN_SEND_STRING_BROADCAST = "top.ninng.wifip2p.main.SEND_STRING";
    public static final String MAIN_SEND_FILE_BROADCAST = "top.ninng.wifip2p.main.SEND_FILE";
    public static final String SEND_SERVICE_BROADCAST
            = "top.ninng.wifip2p.service.SEND";
    public static final String SEND_SERVICE_HEARTBEAT_BROADCAST
            = "top.ninng.wifip2p.service.SEND_HEARTBEAT";
    public static final String STRING_RECEIVE_SERVICE_HEARTBEAT_BROADCAST
            = "top.ninng.wifip2p.service.STRING_RECEIVE_HEARTBEAT";
    public static final String FILE_RECEIVE_SERVICE_HEARTBEAT_BROADCAST
            = "top.ninng.wifip2p.service.FILE_RECEIVE_HEARTBEAT";
    public static final String STRING_RECEIVE_SERVICE_BROADCAST
            = "top.ninng.wifip2p.service.STRING_RECEIVE";
    public static final String FILE_RECEIVE_SERVICE_BROADCAST
            = "top.ninng.wifip2p.service.FILE_RECEIVE";
    public static final String STRING_SEND_SERVICE_BROADCAST
            = "top.ninng.wifip2p.service.STRING_SEND";
    public static final String FILE_SEND_SERVICE_BROADCAST
            = "top.ninng.wifip2p.service.FILE_SEND";
    public static final String RECEIVE_SERVICE_BROADCAST
            = "top.ninng.wifip2p.service.RECEIVE";
    public static final int DEFAULT_STRING_EXTRAS_GROUP_OWNER_PORT = 4001;
    public static final int DEFAULT_FILE_EXTRAS_GROUP_OWNER_PORT = 4002;

}
