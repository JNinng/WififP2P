package top.ninng.demo.model;

import java.io.Serializable;

/**
 * @Author OhmLaw
 * @Date 2022/8/9 15:32
 * @Version 1.0
 */
public class FileTransfer implements Serializable {

    private String fileName;
    private long fileLength;
    private String md5;

    public FileTransfer() {
    }

    public FileTransfer(String fileName, long fileLength, String md5) {
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.md5 = md5;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
