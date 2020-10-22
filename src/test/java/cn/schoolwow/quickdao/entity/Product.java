package cn.schoolwow.quickdao.entity;

import cn.schoolwow.quickdao.annotation.*;

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

    @Comment("所属人")
    @ForeignKey(table = Person.class)
    @Constraint(notNull = true,check = "#{personId} > 0",defaultValue = "1")
    private long personId;

    @TableField(createdAt = true)
    private Date publishTime;

    private Person person;

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

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
