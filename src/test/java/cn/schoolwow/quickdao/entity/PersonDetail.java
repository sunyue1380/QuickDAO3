package cn.schoolwow.quickdao.entity;

import cn.schoolwow.quickdao.annotation.ColumnType;
import cn.schoolwow.quickdao.annotation.Comment;
import cn.schoolwow.quickdao.annotation.Constraint;
import cn.schoolwow.quickdao.annotation.Ignore;

/**人详细信息*/
@Ignore
public class PersonDetail {
    @Comment("姓")
    @ColumnType("varchar(64)")
    @Constraint(notNull = true,unique = true)
    private String lastName;

    @Comment("名")
    @ColumnType("varchar(255)")
    private String firstName;

    @Comment("地址")
    @ColumnType("varchar(255)")
    private String address;

    @Comment("城市")
    @ColumnType("varchar(255)")
    private String city;

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
}
