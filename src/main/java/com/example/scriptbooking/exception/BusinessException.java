package com.example.scriptbooking.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 可向页面安全展示的业务异常。
 * 带有 cause 时，真实异常只写入服务器日志，不在浏览器中输出 SQL、堆栈或内部路径。
 */
public class BusinessException extends RuntimeException {
    private static final Logger LOGGER = Logger.getLogger(BusinessException.class.getName());

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        if (cause != null) {
            LOGGER.log(Level.SEVERE, message + " -> " + cause.getMessage(), cause);
        }
    }
}
