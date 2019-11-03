package cn.schoolwow.quickdao.annotation;

/**
 * 外键级联策略
 */
public enum ForeignKeyOption {
    RESTRICT("RESTRICT"),
    NOACTION("NO ACTION"),
    SETNULL("SET NULL"),
    SETDEFAULT("SET DEFAULT"),
    CASCADE("CASCADE");

    private String operation;

    ForeignKeyOption(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
