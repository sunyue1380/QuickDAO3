package cn.schoolwow.quickdao.database;

/**MySQL数据库*/
public class MySQLDatabase implements Database{

    @Override
    public String comment(String comment) {
        return "comment \""+comment+"\"";
    }

    @Override
    public String escape(String value) {
        return "`"+value+"`";
    }
}
