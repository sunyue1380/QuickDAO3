package cn.schoolwow.quickdao.dao.condition.subCondition;

import cn.schoolwow.quickdao.domain.SubQuery;

public class SQLiteSubCondition extends AbstractSubCondition{
    public SQLiteSubCondition(SubQuery subQuery) {
        super(subQuery);
    }

    @Override
    public SubCondition rightJoin() {
        throw new UnsupportedOperationException("SQLite目前不支持右外连接和全外连接!");
    }
}
