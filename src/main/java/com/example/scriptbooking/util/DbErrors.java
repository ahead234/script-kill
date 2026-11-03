package com.example.scriptbooking.util;

import com.example.scriptbooking.exception.BusinessException;

import java.sql.SQLException;

/**
 * 将 JDBC 异常转换为可操作提示。
 * 页面只展示错误类别与处理建议；SQL 细节与堆栈由 BusinessException 写入服务器日志，不输出到浏览器。
 */
public final class DbErrors {
    private DbErrors() {
    }

    public static BusinessException fail(String message, SQLException cause) {
        return new BusinessException(message + "：" + describe(cause), cause);
    }

    public static String describe(SQLException e) {
        String state = e.getSQLState();
        String message = e.getMessage() == null ? "" : e.getMessage();
        if ("28000".equals(state) || e.getErrorCode() == 1045) {
            return "数据库账号或密码错误（Access denied），请检查 db.properties 中的 db.username 与 db.password，或设置环境变量 DB_USERNAME / DB_PASSWORD";
        }
        if (e.getErrorCode() == 1049 || message.contains("Unknown database")) {
            return "数据库不存在，请先执行 sql/schema.sql 创建 script_booking 数据库";
        }
        if (message.contains("doesn't exist") || message.contains("Unknown table") || e.getErrorCode() == 1146) {
            return "数据表不存在，请先执行 sql/schema.sql 建表，再执行 sql/seed.sql 导入演示数据";
        }
        if ("08S01".equals(state) || message.contains("Communications link failure")) {
            return "无法连接 MySQL 服务，请确认 MySQL 服务已启动，且 db.url 中的主机与端口正确";
        }
        if (message.contains("Public Key Retrieval")) {
            return "认证插件要求公钥检索，请确认 db.url 含 allowPublicKeyRetrieval=true，或改用 caching_sha2_password 账号";
        }
        if (message.contains("time zone") || message.contains("character set")) {
            return "时区或字符集配置有误，请确认 db.url 含 serverTimezone=Asia/Shanghai 与 characterEncoding=UTF-8";
        }
        if (message.contains("SSL")) {
            return "SSL 握手失败，请在 db.url 中使用 useSSL=false（本地演示）或配置信任库";
        }
        return "请检查 src/main/resources/db.properties 配置与 MySQL 服务状态（详细原因见服务器日志）";
    }
}
