package com.example.scriptbooking.dao;
import com.example.scriptbooking.util.DbErrors;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Page;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;

public class UserDao {
    private static final String SELECT_USER = "SELECT u.id,u.username,u.password_hash,u.display_name,u.phone,u.status," +
            "GROUP_CONCAT(r.role_code ORDER BY r.id SEPARATOR ',') roles " +
            "FROM sys_user u LEFT JOIN sys_user_role ur ON ur.user_id=u.id " +
            "LEFT JOIN sys_role r ON r.id=ur.role_id ";

    public Optional<User> findByUsername(String username) {
        String sql = SELECT_USER + "WHERE u.username=? GROUP BY u.id,u.username,u.password_hash,u.display_name,u.phone,u.status";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("查询用户失败", e);
        }
    }

    public Optional<User> findById(long id) {
        String sql = SELECT_USER + "WHERE u.id=? GROUP BY u.id,u.username,u.password_hash,u.display_name,u.phone,u.status";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("查询用户失败", e);
        }
    }

    public Page<User> findPage(String keyword, String status, int page, int pageSize) {
        String term = keyword == null ? "" : keyword.trim();
        String state = status == null ? "" : status.trim();
        String where = " WHERE (?='' OR u.username LIKE CONCAT('%',?,'%') OR u.display_name LIKE CONCAT('%',?,'%')) " +
                "AND (?='' OR u.status=?) ";
        String sql = SELECT_USER + where + "GROUP BY u.id,u.username,u.password_hash,u.display_name,u.phone,u.status " +
                "ORDER BY u.id LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM sys_user u" + where;
        try (Connection connection = Database.getConnection()) {
            long total;
            try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                bindFilter(statement, term, state);
                try (ResultSet rs = statement.executeQuery()) { rs.next(); total = rs.getLong(1); }
            }
            java.util.List<User> users = new java.util.ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindFilter(statement, term, state);
                statement.setInt(6, pageSize);
                statement.setInt(7, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) users.add(map(rs));
                }
            }
            return new Page<>(users, page, pageSize, total);
        } catch (SQLException e) {
            throw DbErrors.fail("查询用户列表失败", e);
        }
    }

    /** 注册前校验手机号是否已被占用。 */
    public boolean existsByPhone(String phone) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM sys_user WHERE phone=?")) {
            statement.setString(1, phone);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("查询手机号失败", e);
        }
    }

    /**
     * 新增注册用户并授予 PLAYER 角色，两个写入放在同一事务中。
     */
    public long insert(User user) {
        String sql = "INSERT INTO sys_user(username,password_hash,display_name,phone,status) VALUES(?,?,?,?,'ACTIVE')";
        try (Connection connection = Database.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, user.getUsername());
                    statement.setString(2, user.getPasswordHash());
                    statement.setString(3, user.getDisplayName());
                    statement.setString(4, user.getPhone());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new BusinessException("未能取得新用户编号");
                        id = keys.getLong(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO sys_user_role(user_id,role_id) SELECT ?,id FROM sys_role WHERE role_code='PLAYER'")) {
                    statement.setLong(1, id);
                    statement.executeUpdate();
                }
                connection.commit();
                return id;
            } catch (SQLException e) {
                connection.rollback();
                if ("23000".equals(e.getSQLState())) {
                    throw new BusinessException("用户名或手机号已被注册", e);
                }
                throw DbErrors.fail("注册用户失败", e);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw DbErrors.fail("注册用户失败", e);
        }
    }

    public void updateStatus(long id, String status) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE sys_user SET status=? WHERE id=?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) throw new BusinessException("用户不存在");
        } catch (SQLException e) {
            throw DbErrors.fail("更新用户状态失败", e);
        }
    }

    private void bindFilter(PreparedStatement statement, String term, String status) throws SQLException {
        statement.setString(1, term);
        statement.setString(2, term);
        statement.setString(3, term);
        statement.setString(4, status);
        statement.setString(5, status);
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setDisplayName(rs.getString("display_name"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getString("status"));
        String roles = rs.getString("roles");
        user.setRoles(roles == null || roles.trim().isEmpty() ? new LinkedHashSet<>() : new LinkedHashSet<>(Arrays.asList(roles.split(","))));
        return user;
    }
}
