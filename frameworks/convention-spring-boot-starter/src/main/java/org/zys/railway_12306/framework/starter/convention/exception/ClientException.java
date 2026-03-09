package org.zys.railway_12306.framework.starter.convention.exception;

import org.zys.railway_12306.framework.starter.convention.errorcode.BaseErrorCode;
import org.zys.railway_12306.framework.starter.convention.errorcode.IErrorCode;

public class ClientException extends AbstractException {
  public ClientException(IErrorCode errorCode) {
    this(null, null, errorCode);
  }

  public ClientException(String message) {
    this(message, null, BaseErrorCode.CLIENT_ERROR);
  }

  public ClientException(String message, IErrorCode errorCode) {
    this(message, null, errorCode);
  }

  public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
    super(message, throwable, errorCode);
  }

  @Override
  public String toString() {
    return "客户端异常{" +
            "code='" + errorCode + "'," +
            "message='" + errorMessage + "'" +
            '}';
  }
}
