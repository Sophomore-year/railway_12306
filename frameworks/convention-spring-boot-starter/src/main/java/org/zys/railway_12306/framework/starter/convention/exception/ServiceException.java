package org.zys.railway_12306.framework.starter.convention.exception;

import org.zys.railway_12306.framework.starter.convention.errorcode.BaseErrorCode;
import org.zys.railway_12306.framework.starter.convention.errorcode.IErrorCode;

/*
* 服务端异常
* */
public class ServiceException extends AbstractException {
    public ServiceException(String message) {
        this(message, null, BaseErrorCode.SERVICE_ERROR);
    }

    public ServiceException(IErrorCode errorCode) {
        this(null, errorCode);
    }

    public ServiceException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ServiceException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "服务端异常{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }
}
