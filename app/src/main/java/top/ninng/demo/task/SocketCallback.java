package top.ninng.demo.task;

/**
 * @Author OhmLaw
 * @Date 2022/10/6 16:34
 * @Version 1.0
 */
public abstract class SocketCallback {

    /**
     * 任务出错
     *
     * @param index
     * @param e
     */
    abstract public void onError(int index, Exception e);

    /**
     * 任务完成
     *
     * @param index
     */
    abstract public void onFinish(int index);

    /**
     * 任务进度
     *
     * @param index
     * @param progress 百分比
     */
    abstract public void onProgress(int index, float progress);

    /**
     * 获取结果
     *
     * @param index
     * @param result
     */
    abstract public void setResult(int index, String result);
}
