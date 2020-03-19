package cn.schoolwow.quickdao.entity;

import cn.schoolwow.quickdao.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Comment("人")
public class Person {
    @Id
    private long id;
    @ColumnType("varchar(64)")
    @Constraint(notNull = true,unique = true)
    private String lastName;
    @ColumnType("varchar(255)")
    private String firstName;
    @ColumnType("varchar(255)")
    private String address;
    @ColumnType("varchar(255)")
    private String city;

    @TableField(createdAt = true)
    private Timestamp createdAt;

    @TableField(updatedAt = true)
    private Timestamp updatedAt;

    private Order order;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
