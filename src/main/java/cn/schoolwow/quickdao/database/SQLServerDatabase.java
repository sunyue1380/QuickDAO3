package cn.schoolwow.quickdao.database;

public class SQLServerDatabase implements Database{
    @Override
    public String comment(String comment) {
        return "";
    }

    @Override
    public String escape(String value) {
        return "\"" +value + "\"";
    }
}
