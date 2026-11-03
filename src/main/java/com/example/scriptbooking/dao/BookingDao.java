package com.example.scriptbooking.dao;
import com.example.scriptbooking.util.DbErrors;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Booking;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Page;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class BookingDao {
    private static final String SELECT_DETAIL = "SELECT b.id,b.booking_code,b.user_id,b.session_id,b.party_size,b.contact_name,b.contact_phone," +
            "b.status,b.player_note,b.created_at,b.updated_at,u.display_name user_display_name,u.username," +
            "ps.session_code,ps.start_time,ps.end_time,ps.room_name,ps.dm_name,ps.unit_price,s.title script_title,r.id review_id " +
            "FROM booking b JOIN sys_user u ON u.id=b.user_id JOIN play_session ps ON ps.id=b.session_id " +
            "JOIN script_book s ON s.id=ps.script_id LEFT JOIN review r ON r.booking_id=b.id ";

    public long insert(Connection connection, Booking booking) throws SQLException {
        String sql = "INSERT INTO booking(booking_code,user_id,session_id,party_size,contact_name,contact_phone,status,player_note) " +
                "VALUES(?,?,?,?,?,?,'CONFIRMED',?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, booking.getBookingCode()); statement.setLong(2, booking.getUserId());
            statement.setLong(3, booking.getSessionId()); statement.setInt(4, booking.getPartySize());
            statement.setString(5, booking.getContactName()); statement.setString(6, booking.getContactPhone());
            statement.setString(7, booking.getPlayerNote());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new BusinessException("未能取得新预约编号");
            }
        }
    }

    public boolean activeExists(Connection connection, long userId, long sessionId) throws SQLException {
        String sql = "SELECT 1 FROM booking WHERE user_id=? AND session_id=? AND status IN ('CONFIRMED','CHECKED_IN') LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId); statement.setLong(2, sessionId);
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        }
    }

    public Optional<Booking> findById(long id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DETAIL + "WHERE b.id=?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapDetail(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("查询预约失败", e);
        }
    }

    public Optional<Booking> findRaw(Connection connection, long id) throws SQLException {
        return findRaw(connection, id, false);
    }

    public Optional<Booking> findForUpdate(Connection connection, long id) throws SQLException {
        return findRaw(connection, id, true);
    }

    private Optional<Booking> findRaw(Connection connection, long id, boolean forUpdate) throws SQLException {
        String sql = "SELECT id,booking_code,user_id,session_id,party_size,contact_name,contact_phone,status,player_note,created_at,updated_at " +
                "FROM booking WHERE id=?" + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRaw(rs)) : Optional.empty();
            }
        }
    }

    public java.util.List<Booking> findForUser(long userId) {
        java.util.List<Booking> result = new java.util.ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DETAIL + "WHERE b.user_id=? ORDER BY b.created_at DESC")) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) result.add(mapDetail(rs));
            }
            return result;
        } catch (SQLException e) {
            throw DbErrors.fail("查询我的预约失败", e);
        }
    }

    public Page<Booking> findPage(String keyword, String status, int page, int pageSize) {
        String term = value(keyword);
        String state = value(status);
        String where = "WHERE (?='' OR b.booking_code LIKE CONCAT('%',?,'%') OR s.title LIKE CONCAT('%',?,'%') " +
                "OR u.display_name LIKE CONCAT('%',?,'%') OR u.username LIKE CONCAT('%',?,'%')) AND (?='' OR b.status=?) ";
        String query = SELECT_DETAIL + where + "ORDER BY b.created_at DESC LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) FROM booking b JOIN sys_user u ON u.id=b.user_id JOIN play_session ps ON ps.id=b.session_id " +
                "JOIN script_book s ON s.id=ps.script_id " + where;
        try (Connection connection = Database.getConnection()) {
            long total;
            try (PreparedStatement statement = connection.prepareStatement(count)) {
                bindFilter(statement, term, state);
                try (ResultSet rs = statement.executeQuery()) { rs.next(); total = rs.getLong(1); }
            }
            java.util.List<Booking> bookings = new java.util.ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                bindFilter(statement, term, state);
                statement.setInt(8, pageSize); statement.setInt(9, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) bookings.add(mapDetail(rs));
                }
            }
            return new Page<>(bookings, page, pageSize, total);
        } catch (SQLException e) {
            throw DbErrors.fail("查询预约列表失败", e);
        }
    }

    public void updateStatus(Connection connection, long id, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE booking SET status=? WHERE id=?")) {
            statement.setString(1, status); statement.setLong(2, id);
            if (statement.executeUpdate() == 0) throw new BusinessException("预约不存在");
        }
    }

    public int cancelActiveBySession(Connection connection, long sessionId) throws SQLException {
        String sql = "UPDATE booking SET status='CANCELLED_BY_STORE' WHERE session_id=? AND status IN ('CONFIRMED','CHECKED_IN')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            return statement.executeUpdate();
        }
    }

    public long countByStatus(String status) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM booking WHERE status=?")) {
            statement.setString(1, status);
            try (ResultSet rs = statement.executeQuery()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) {
            throw DbErrors.fail("统计预约失败", e);
        }
    }

    public long countAll() {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM booking");
             ResultSet rs = statement.executeQuery()) {
            rs.next(); return rs.getLong(1);
        } catch (SQLException e) {
            throw DbErrors.fail("统计预约失败", e);
        }
    }

    public java.util.Map<String, Long> countGroupByStatus() {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status, COUNT(*) FROM booking GROUP BY status");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.put(rs.getString(1), rs.getLong(2));
            return result;
        } catch (SQLException e) {
            throw DbErrors.fail("统计预约状态失败", e);
        }
    }

    public java.util.Map<String, Long> countByDay(java.time.LocalDate from, java.time.LocalDate to) {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') d, COUNT(*) c FROM booking " +
                "WHERE created_at >= ? AND created_at < ? GROUP BY d ORDER BY d";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(from));
            statement.setDate(2, java.sql.Date.valueOf(to.plusDays(1)));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) result.put(rs.getString(1), rs.getLong(2));
            }
            return result;
        } catch (SQLException e) {
            throw DbErrors.fail("统计预约趋势失败", e);
        }
    }

    public java.util.Map<String, Long> countByScript(int limit) {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        String sql = "SELECT s.title, COUNT(*) c FROM booking b JOIN play_session ps ON ps.id=b.session_id " +
                "JOIN script_book s ON s.id=ps.script_id GROUP BY s.id, s.title ORDER BY c DESC, s.title LIMIT ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) result.put(rs.getString(1), rs.getLong(2));
            }
            return result;
        } catch (SQLException e) {
            throw DbErrors.fail("统计剧本热度失败", e);
        }
    }

    private void bindFilter(PreparedStatement statement, String term, String state) throws SQLException {
        statement.setString(1, term); statement.setString(2, term); statement.setString(3, term);
        statement.setString(4, term); statement.setString(5, term);
        statement.setString(6, state); statement.setString(7, state);
    }

    private Booking mapRaw(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getLong("id")); booking.setBookingCode(rs.getString("booking_code"));
        booking.setUserId(rs.getLong("user_id")); booking.setSessionId(rs.getLong("session_id"));
        booking.setPartySize(rs.getInt("party_size")); booking.setContactName(rs.getString("contact_name"));
        booking.setContactPhone(rs.getString("contact_phone")); booking.setStatus(rs.getString("status"));
        booking.setPlayerNote(rs.getString("player_note")); booking.setCreatedAt(JdbcMapping.dateTime(rs, "created_at"));
        booking.setUpdatedAt(JdbcMapping.dateTime(rs, "updated_at"));
        return booking;
    }

    private Booking mapDetail(ResultSet rs) throws SQLException {
        Booking booking = mapRaw(rs);
        booking.setUserDisplayName(rs.getString("user_display_name")); booking.setUsername(rs.getString("username"));
        booking.setSessionCode(rs.getString("session_code")); booking.setScriptTitle(rs.getString("script_title"));
        booking.setStartTime(JdbcMapping.dateTime(rs, "start_time")); booking.setEndTime(JdbcMapping.dateTime(rs, "end_time"));
        booking.setRoomName(rs.getString("room_name")); booking.setDmName(rs.getString("dm_name"));
        booking.setUnitPrice(rs.getBigDecimal("unit_price")); booking.setReviewId(JdbcMapping.nullableLong(rs, "review_id"));
        return booking;
    }

    private String value(String input) { return input == null ? "" : input.trim(); }
}
