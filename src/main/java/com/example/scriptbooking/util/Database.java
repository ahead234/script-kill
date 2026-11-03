package com.example.scriptbooking.util;

import com.example.scriptbooking.exception.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/script_booking?useUnicode=true&characterEncoding=UTF-8" +
            "&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    private static final Properties PROPERTIES = new Properties();

    static {
        String externalPath = System.getProperty("scriptbooking.config");
        try (InputStream input = externalPath == null
                ? Database.class.getClassLoader().getResourceAsStream("db.properties")
                : new java.io.FileInputStream(externalPath)) {
            if (input != null) {
                PROPERTIES.load(input);
            }
            Class.forName(value("DB_DRIVER", "db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(
                    value("DB_URL", "db.url", DEFAULT_URL),
                    value("DB_USERNAME", "db.username", "root"),
                    value("DB_PASSWORD", "db.password", ""));
        } catch (SQLException e) {
            throw new BusinessException("数据库连接失败：" + DbErrors.describe(e), e);
        }
    }

    private static String value(String envName, String propertyName, String defaultValue) {
        String env = System.getenv(envName);
        return env == null || env.trim().isEmpty() ? PROPERTIES.getProperty(propertyName, defaultValue) : env;
    }
}
