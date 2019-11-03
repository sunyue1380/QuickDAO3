package cn.schoolwow.quickdao.dao.condition;

import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.util.StringUtil;

public class SQLServerCondition extends AbstractCondition{

    public SQLServerCondition(Query query) {
        super(query);
    }

    @Override
    public Condition addLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        query.whereBuilder.append("charindex(?,t."+query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field))+" ) >0 and ");
        query.parameterList.add(value.toString());
        return this;
    }

    @Override
    public Condition limit(long offset, long limit) {
        if(query.orderByBuilder.length()==0){
            throw new IllegalArgumentException("SQL Server的分页操作必须包含order子句!");
        }
        query.limit = "offset "+offset+" rows " + " fetch next "+limit+" rows only";
        return this;
    }

    @Override
    public Condition page(int pageNum, int pageSize) {
        if(query.orderByBuilder.length()==0){
            throw new IllegalArgumentException("SQL Server的分页操作必须包含order子句!");
        }
        query.limit = "offset "+(pageNum - 1) * pageSize+" rows " + " fetch next "+pageSize+" rows only";
        query.pageVo = new PageVo<>();
        query.pageVo.setPageSize(pageSize);
        query.pageVo.setCurrentPage(pageNum);
        return this;
    }
}
