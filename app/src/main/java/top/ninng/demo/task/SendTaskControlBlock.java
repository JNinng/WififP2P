package top.ninng.demo.task;

/**
 * @author OhmLaw
 */
public class SendTaskControlBlock {

    /**
     * key
     */
    String taskId;
    Runnable taskRunnable;

    public SendTaskControlBlock(String taskId, Runnable runnable) {
        this.taskId = taskId;
        this.taskRunnable = runnable;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Runnable getTaskRunnable() {
        return taskRunnable;
    }

    public void setTaskRunnable(Runnable taskRunnable) {
        this.taskRunnable = taskRunnable;
    }
}