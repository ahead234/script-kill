package com.example.scriptbooking.dao;
import com.example.scriptbooking.util.DbErrors;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Review;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Page;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class ReviewDao {
    private static final String SELECT_DETAIL = "SELECT r.id,r.booking_id,r.user_id,r.rating,r.story_rating,r.dm_rating,r.atmosphere_rating," +
            "r.content,r.status,r.moderation_reason,r.moderated_by,r.created_at,r.updated_at,b.booking_code," +
            "s.title script_title,u.display_name user_display_name,m.display_name moderator_name " +
            "FROM review r JOIN booking b ON b.id=r.booking_id JOIN play_session ps ON ps.id=b.session_id " +
            "JOIN script_book s ON s.id=ps.script_id JOIN sys_user u ON u.id=r.user_id " +
            "LEFT JOIN sys_user m ON m.id=r.moderated_by ";

    public Optional<Review> findByBooking(long bookingId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DETAIL + "WHERE r.booking_id=?")) {
            statement.setLong(1, bookingId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("查询评价失败", e);
        }
    }

    public java.util.List<Review> findForUser(long userId) {
        java.util.List<Review> reviews = new java.util.ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DETAIL + "WHERE r.user_id=? ORDER BY r.created_at DESC")) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) reviews.add(map(rs)); }
            return reviews;
        } catch (SQLException e) {
            throw DbErrors.fail("查询我的评价失败", e);
        }
    }

    public java.util.List<Review> findPublishedByScript(long scriptId) {
        java.util.List<Review> reviews = new java.util.ArrayList<>();
        String sql = SELECT_DETAIL + "WHERE ps.script_id=? AND r.status='PUBLISHED' ORDER BY r.created_at DESC LIMIT 20";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, scriptId);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) reviews.add(map(rs)); }
            return reviews;
        } catch (SQLException e) {
            throw DbErrors.fail("查询公开评价失败", e);
        }
    }

    public Page<Review> findPage(String status, int page, int pageSize) {
        String state = status == null ? "" : status.trim();
        String where = "WHERE (?='' OR r.status=?) ";
        String query = SELECT_DETAIL + where + "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) FROM review r " + where;
        try (Connection connection = Database.getConnection()) {
            long total;
            try (PreparedStatement statement = connection.prepareStatement(count)) {
                statement.setString(1, state); statement.setString(2, state);
                try (ResultSet rs = statement.executeQuery()) { rs.next(); total = rs.getLong(1); }
            }
            java.util.List<Review> reviews = new java.util.ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, state); statement.setString(2, state);
                statement.setInt(3, pageSize); statement.setInt(4, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) { while (rs.next()) reviews.add(map(rs)); }
            }
            return new Page<>(reviews, page, pageSize, total);
        } catch (SQLException e) {
            throw DbErrors.fail("查询评价列表失败", e);
        }
    }

    public long insert(Review review) {
        String sql = "INSERT INTO review(booking_id,user_id,rating,story_rating,dm_rating,atmosphere_rating,content,status) " +
                "VALUES(?,?,?,?,?,?,?,'PENDING')";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindRatings(statement, review);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new BusinessException("未能取得评价编号");
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw DbErrors.fail("该预约已经评价过", e);
            throw DbErrors.fail("提交评价失败", e);
        }
    }

    public void update(Review review) {
        String sql = "UPDATE review SET rating=?,story_rating=?,dm_rating=?,atmosphere_rating=?,content=?,status='PENDING'," +
                "moderation_reason=NULL,moderated_by=NULL WHERE id=? AND user_id=? AND status='PENDING'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, review.getRating()); setNullableInt(statement, 2, review.getStoryRating());
            setNullableInt(statement, 3, review.getDmRating()); setNullableInt(statement, 4, review.getAtmosphereRating());
            statement.setString(5, review.getContent()); statement.setLong(6, review.getId()); statement.setLong(7, review.getUserId());
            if (statement.executeUpdate() == 0) throw new BusinessException("仅待审核评价允许修改");
        } catch (SQLException e) {
            throw DbErrors.fail("修改评价失败", e);
        }
    }

    public void moderate(Connection connection, long reviewId, String status, String reason, long moderatorId) throws SQLException {
        String sql = "UPDATE review SET status=?,moderation_reason=?,moderated_by=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status); statement.setString(2, reason);
            statement.setLong(3, moderatorId); statement.setLong(4, reviewId);
            if (statement.executeUpdate() == 0) throw new BusinessException("评价不存在");
        }
    }

    public long countPending() {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM review WHERE status='PENDING'");
             ResultSet rs = statement.executeQuery()) {
            rs.next(); return rs.getLong(1);
        } catch (SQLException e) {
            throw DbErrors.fail("统计评价失败", e);
        }
    }

    public java.util.Map<String, Long> countGroupByStatus() {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status, COUNT(*) FROM review GROUP BY status");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.put(rs.getString(1), rs.getLong(2));
            return result;
        } catch (SQLException e) {
            throw DbErrors.fail("统计评价状态失败", e);
        }
    }

    private void bindRatings(PreparedStatement statement, Review review) throws SQLException {
        statement.setLong(1, review.getBookingId()); statement.setLong(2, review.getUserId());
        statement.setInt(3, review.getRating()); setNullableInt(statement, 4, review.getStoryRating());
        setNullableInt(statement, 5, review.getDmRating()); setNullableInt(statement, 6, review.getAtmosphereRating());
        statement.setString(7, review.getContent());
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.TINYINT); else statement.setInt(index, value);
    }

    private Review map(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id")); review.setBookingId(rs.getLong("booking_id"));
        review.setUserId(rs.getLong("user_id")); review.setRating(rs.getInt("rating"));
        review.setStoryRating(nullableInt(rs, "story_rating")); review.setDmRating(nullableInt(rs, "dm_rating"));
        review.setAtmosphereRating(nullableInt(rs, "atmosphere_rating")); review.setContent(rs.getString("content"));
        review.setStatus(rs.getString("status")); review.setModerationReason(rs.getString("moderation_reason"));
        review.setModeratedBy(JdbcMapping.nullableLong(rs, "moderated_by"));
        review.setCreatedAt(JdbcMapping.dateTime(rs, "created_at")); review.setUpdatedAt(JdbcMapping.dateTime(rs, "updated_at"));
        review.setBookingCode(rs.getString("booking_code")); review.setScriptTitle(rs.getString("script_title"));
        review.setUserDisplayName(rs.getString("user_display_name")); review.setModeratorName(rs.getString("moderator_name"));
        return review;
    }

    private Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column); return rs.wasNull() ? null : value;
    }
}
