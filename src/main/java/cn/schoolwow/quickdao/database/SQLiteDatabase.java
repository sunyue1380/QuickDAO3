package cn.schoolwow.quickdao.database;

public class SQLiteDatabase implements Database{
    @Override
    public String comment(String comment) {
        return "/* "+comment+" */";
    }

    @Override
    public String escape(String value) {
        return "`"+value+"`";
    }
}
