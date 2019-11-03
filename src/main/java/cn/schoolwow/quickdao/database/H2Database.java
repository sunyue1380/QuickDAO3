package cn.schoolwow.quickdao.database;

public class H2Database implements Database{
    @Override
    public String comment(String comment) {
        return "";
    }

    @Override
    public String escape(String value) {
        return "`"+value+"`";
    }
}
