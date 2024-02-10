package top.ninng.demo.utils;

/**
 * @author OhmLaw
 */

public enum TaskStatus {
    /**
     * 新建状态
     */
    NEW(1),
    RUNNING(2),
    /**
     * 暂停
     */
    PAUSE(3),
    STOP(4),
    FINISH(5),
    ERROR(6),
    /**
     * 就绪态，已加入线程池
     */
    READY(7);
    int index;

    TaskStatus(int i) {
        this.index = i;
    }
}