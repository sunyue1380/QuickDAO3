package cn.schoolwow.quickdao.exception;

/**包装SQL异常为运行时异常*/
public class SQLRuntimeException extends RuntimeException{

    public SQLRuntimeException(Throwable cause) {
        super(cause);
    }
}
