package cn.schoolwow.quickdao.builder.sql.dql;

import cn.schoolwow.quickdao.dao.condition.AbstractCondition;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteDQLSQLBuilder extends AbstractDQLSQLBuilder{

    public SQLiteDQLSQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public PreparedStatement update(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("update "+query.quickDAOConfig.database.escape(query.entity.tableName)+" ");
        builder.append(query.setBuilder.toString());
        builder.append(" " + query.whereBuilder.toString());

        PreparedStatement ps = connection.prepareStatement(builder.toString().replace(AbstractCondition.mainTableAlias+".",""));
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        for (Object parameter : query.updateParameterList) {
            setParameter(parameter,ps,query.parameterIndex++,builder);
        }
        addMainTableParameters(ps,query,builder);
        logger.debug("[批量更新]执行SQL:{}", builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement delete(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("delete from "+query.quickDAOConfig.database.escape(query.entity.tableName));
        builder.append(" " + query.whereBuilder.toString().replace(AbstractCondition.mainTableAlias+".",""));

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        logger.debug("[批量删除]执行SQL:{}", builder.toString());
        return ps;
    }
}