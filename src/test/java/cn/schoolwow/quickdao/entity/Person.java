package cn.schoolwow.quickdao.entity;

import cn.schoolwow.quickdao.annotation.*;

import java.time.LocalDateTime;

@Comment("人")
public class Person {
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    @ColumnType("varchar(32)")
    @Constraint(notNull = true)
    @TableField(function = "md5(concat('salt#',#{password}))")
    private String password;
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
    private LocalDateTime createdAt;

    @TableField(updatedAt = true)
    private LocalDateTime updatedAt;

    private Order order;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
