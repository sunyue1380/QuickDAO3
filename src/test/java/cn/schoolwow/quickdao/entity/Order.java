package cn.schoolwow.quickdao.entity;

import cn.schoolwow.quickdao.annotation.Comment;
import cn.schoolwow.quickdao.annotation.Constraint;
import cn.schoolwow.quickdao.annotation.ForeignKey;
import cn.schoolwow.quickdao.annotation.Id;

@Comment("订单")
public class Order {
    @Comment("自增id")
    @Id
    private long id;

    @Comment("订单id")
    @Constraint(notNull = true,check = "#{orderNo} > 0")
    private int orderNo;

    @Comment("所属人")
    @ForeignKey(table = Person.class)
    @Constraint(notNull = true,check = "#{personId} > 0")
    private long personId;

    private Person person;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }
    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
