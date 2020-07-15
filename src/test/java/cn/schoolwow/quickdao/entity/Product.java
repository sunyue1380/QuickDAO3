package cn.schoolwow.quickdao.entity;

import cn.schoolwow.quickdao.annotation.Comment;
import cn.schoolwow.quickdao.annotation.Id;
import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.TableField;

import java.util.Date;

public class Product {
    @Id(strategy = IdStrategy.IdGenerator)
    private long id;

    @Comment("商品名称")
    private String name;

    @TableField(createdAt = true)
    private Date publishTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }
}
