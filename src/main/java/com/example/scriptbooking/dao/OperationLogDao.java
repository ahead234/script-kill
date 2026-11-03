package com.example.scriptbooking.dao;
import com.example.scriptbooking.util.DbErrors;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.OperationLog;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Page;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OperationLogDao {
    public void insert(Connection connection, Long operatorId, String action, String entityType, Long entityId, String detail, String ip) throws SQLException {
        String sql = "INSERT INTO sys_operation_log(operator_id,action,entity_type,entity_id,detail,ip_address) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (operatorId == null) statement.setNull(1, java.sql.Types.BIGINT); else statement.setLong(1, operatorId);
            statement.setString(2, action); statement.setString(3, entityType);
            if (entityId == null) statement.setNull(4, java.sql.Types.BIGINT); else statement.setLong(4, entityId);
            statement.setString(5, sanitize(detail)); statement.setString(6, ip);
            statement.executeUpdate();
        }
    }

    public void insert(Long operatorId, String action, String entityType, Long entityId, String detail, String ip) {
        try (Connection connection = Database.getConnection()) {
            insert(connection, operatorId, action, entityType, entityId, detail, ip);
        } catch (SQLException e) {
            throw DbErrors.fail("记录操作日志失败", e);
        }
    }

    public Page<OperationLog> findPage(String keyword, int page, int pageSize) {
        String term = keyword == null ? "" : keyword.trim();
        String where = "WHERE (?='' OR l.action LIKE CONCAT('%',?,'%') OR l.entity_type LIKE CONCAT('%',?,'%') " +
                "OR u.display_name LIKE CONCAT('%',?,'%')) ";
        String query = "SELECT l.id,l.operator_id,u.display_name operator_name,l.action,l.entity_type,l.entity_id,l.detail,l.ip_address,l.created_at " +
                "FROM sys_operation_log l LEFT JOIN sys_user u ON u.id=l.operator_id " + where + "ORDER BY l.created_at DESC LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) FROM sys_operation_log l LEFT JOIN sys_user u ON u.id=l.operator_id " + where;
        try (Connection connection = Database.getConnection()) {
            long total;
            try (PreparedStatement statement = connection.prepareStatement(count)) {
                bind(statement, term);
                try (ResultSet rs = statement.executeQuery()) { rs.next(); total = rs.getLong(1); }
            }
            java.util.List<OperationLog> logs = new java.util.ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                bind(statement, term); statement.setInt(5, pageSize); statement.setInt(6, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) { while (rs.next()) logs.add(map(rs)); }
            }
            return new Page<>(logs, page, pageSize, total);
        } catch (SQLException e) {
            throw DbErrors.fail("查询操作日志失败", e);
        }
    }

    private void bind(PreparedStatement statement, String term) throws SQLException {
        statement.setString(1, term); statement.setString(2, term); statement.setString(3, term); statement.setString(4, term);
    }

    private String sanitize(String detail) {
        if (detail == null) return null;
        String sanitized = detail.replaceAll("(?i)(password|token|secret)\\s*[=:]\\s*[^,;\\s]+", "$1=[REDACTED]");
        return sanitized.length() > 1000 ? sanitized.substring(0, 1000) : sanitized;
    }

    private OperationLog map(ResultSet rs) throws SQLException {
        OperationLog log = new OperationLog();
        log.setId(rs.getLong("id")); log.setOperatorId(JdbcMapping.nullableLong(rs, "operator_id"));
        log.setOperatorName(rs.getString("operator_name")); log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type")); log.setEntityId(JdbcMapping.nullableLong(rs, "entity_id"));
        log.setDetail(rs.getString("detail")); log.setIpAddress(rs.getString("ip_address"));
        log.setCreatedAt(JdbcMapping.dateTime(rs, "created_at"));
        return log;
    }
}
