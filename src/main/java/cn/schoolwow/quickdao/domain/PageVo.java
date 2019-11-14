package cn.schoolwow.quickdao.domain;

import java.io.Serializable;
import java.util.List;

/**分页对象*/
public class PageVo<T> implements Serializable {
    /**列表*/
    private List<T> list;
    /**总记录数*/
    private long totalSize;
    /**总页数*/
    private int totalPage;
    /**每页个数*/
    private int pageSize;
    /**当前页*/
    private int currentPage;
    /**是否还有下一页*/
    private boolean hasMore;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
