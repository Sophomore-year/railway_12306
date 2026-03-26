package org.zys.railway_12306.framework.starter.convention.exception;

import org.zys.railway_12306.framework.starter.convention.errorcode.BaseErrorCode;
import org.zys.railway_12306.framework.starter.convention.errorcode.IErrorCode;

/**
 * 远程服务调用异常
 *
 * @author SUM
 * @date 2026/03/26
 */
public class RemoteException extends AbstractException {

    public RemoteException(String message) {
        this(message, null, BaseErrorCode.REMOTE_ERROR);
    }

    public RemoteException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public RemoteException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "远程服务调用异常{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }
}
