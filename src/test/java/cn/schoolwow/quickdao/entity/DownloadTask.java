package cn.schoolwow.quickdao.entity;

/**下载任务类*/
public class DownloadTask {
    /**文件路径*/
    private String filePath;

    /**文件大小*/
    private long fileSize;

    /**文件备注*/
    private String remark;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
