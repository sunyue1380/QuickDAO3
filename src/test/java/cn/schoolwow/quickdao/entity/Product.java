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

    @Comment("商品类别")
    private String type;

    @Comment("商品价格")
    private int price;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }
}
