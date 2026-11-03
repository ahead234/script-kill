package com.example.scriptbooking.dao;
import com.example.scriptbooking.util.DbErrors;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.ScriptBook;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Page;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class ScriptBookDao {
    private static final String SELECT_COLUMNS = "SELECT s.id,s.script_code,s.title,s.genre,s.min_players,s.max_players," +
            "s.duration_minutes,s.difficulty,s.tags,s.description,s.cover_path,s.status,s.created_at,s.updated_at," +
            "COALESCE((SELECT AVG(r.rating) FROM review r JOIN booking b ON b.id=r.booking_id JOIN play_session ps ON ps.id=b.session_id " +
            "WHERE ps.script_id=s.id AND r.status='PUBLISHED'),0) average_rating," +
            "(SELECT COUNT(*) FROM review r JOIN booking b ON b.id=r.booking_id JOIN play_session ps ON ps.id=b.session_id " +
            "WHERE ps.script_id=s.id AND r.status='PUBLISHED') review_count FROM script_book s ";

    public Page<ScriptBook> findPage(String keyword, String genre, String status, int page, int pageSize, boolean publicOnly) {
        String term = value(keyword);
        String type = value(genre);
        String state = publicOnly ? "ACTIVE" : value(status);
        String where = "WHERE (?='' OR s.title LIKE CONCAT('%',?,'%') OR s.script_code LIKE CONCAT('%',?,'%')) " +
                "AND (?='' OR s.genre=?) AND (?='' OR s.status=?) ";
        String query = SELECT_COLUMNS + where + "ORDER BY s.status,s.updated_at DESC LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) FROM script_book s " + where;
        try (Connection connection = Database.getConnection()) {
            long total;
            try (PreparedStatement statement = connection.prepareStatement(count)) {
                bindFilter(statement, term, type, state);
                try (ResultSet rs = statement.executeQuery()) { rs.next(); total = rs.getLong(1); }
            }
            java.util.List<ScriptBook> items = new java.util.ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                bindFilter(statement, term, type, state);
                statement.setInt(8, pageSize);
                statement.setInt(9, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) items.add(map(rs));
                }
            }
            return new Page<>(items, page, pageSize, total);
        } catch (SQLException e) {
            throw DbErrors.fail("查询剧本列表失败", e);
        }
    }

    public Optional<ScriptBook> findById(long id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COLUMNS + "WHERE s.id=?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("查询剧本失败", e);
        }
    }

    public boolean codeExists(String code, long excludedId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM script_book WHERE script_code=? AND id<>?")) {
            statement.setString(1, code);
            statement.setLong(2, excludedId);
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw DbErrors.fail("校验剧本编码失败", e);
        }
    }

    public long countAll() {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM script_book");
             ResultSet rs = statement.executeQuery()) {
            rs.next(); return rs.getLong(1);
        } catch (SQLException e) {
            throw DbErrors.fail("统计剧本失败", e);
        }
    }

    public java.util.Map<String, Long> countGroupByGenre() {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT genre, COUNT(*) FROM script_book GROUP BY genre ORDER BY COUNT(*) DESC, genre");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.put(rs.getString(1), rs.getLong(2));
            return result;
        } catch (SQLException e) {
            throw DbErrors.fail("统计剧本题材失败", e);
        }
    }

    public long insert(ScriptBook book) {
        String sql = "INSERT INTO script_book(script_code,title,genre,min_players,max_players,duration_minutes,difficulty,tags,description,cover_path,status) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindWrite(statement, book);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new BusinessException("未能取得新剧本编号");
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw DbErrors.fail("剧本编码已存在", e);
            throw DbErrors.fail("新增剧本失败", e);
        }
    }

    public void update(ScriptBook book) {
        String sql = "UPDATE script_book SET script_code=?,title=?,genre=?,min_players=?,max_players=?,duration_minutes=?," +
                "difficulty=?,tags=?,description=?,cover_path=?,status=? WHERE id=?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindWrite(statement, book);
            statement.setLong(12, book.getId());
            if (statement.executeUpdate() == 0) throw new BusinessException("剧本不存在");
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw DbErrors.fail("剧本编码已存在", e);
            throw DbErrors.fail("修改剧本失败", e);
        }
    }

    public void updateStatus(long id, String status) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE script_book SET status=? WHERE id=?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) throw new BusinessException("剧本不存在");
        } catch (SQLException e) {
            throw DbErrors.fail("更新剧本状态失败", e);
        }
    }

    public java.util.List<ScriptBook> findActiveOptions() {
        java.util.List<ScriptBook> result = new java.util.ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COLUMNS + "WHERE s.status='ACTIVE' ORDER BY s.title");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.add(map(rs));
            return result;
        } catch (SQLException e) {
            throw DbErrors.fail("查询可用剧本失败", e);
        }
    }

    private void bindFilter(PreparedStatement statement, String term, String genre, String status) throws SQLException {
        statement.setString(1, term); statement.setString(2, term); statement.setString(3, term);
        statement.setString(4, genre); statement.setString(5, genre);
        statement.setString(6, status); statement.setString(7, status);
    }

    private void bindWrite(PreparedStatement statement, ScriptBook book) throws SQLException {
        statement.setString(1, book.getScriptCode()); statement.setString(2, book.getTitle());
        statement.setString(3, book.getGenre()); statement.setInt(4, book.getMinPlayers());
        statement.setInt(5, book.getMaxPlayers()); statement.setInt(6, book.getDurationMinutes());
        statement.setString(7, book.getDifficulty()); statement.setString(8, book.getTags());
        statement.setString(9, book.getDescription()); statement.setString(10, book.getCoverPath());
        statement.setString(11, book.getStatus());
    }

    private ScriptBook map(ResultSet rs) throws SQLException {
        ScriptBook book = new ScriptBook();
        book.setId(rs.getLong("id")); book.setScriptCode(rs.getString("script_code"));
        book.setTitle(rs.getString("title")); book.setGenre(rs.getString("genre"));
        book.setMinPlayers(rs.getInt("min_players")); book.setMaxPlayers(rs.getInt("max_players"));
        book.setDurationMinutes(rs.getInt("duration_minutes")); book.setDifficulty(rs.getString("difficulty"));
        book.setTags(rs.getString("tags")); book.setDescription(rs.getString("description"));
        book.setCoverPath(rs.getString("cover_path")); book.setStatus(rs.getString("status"));
        book.setCreatedAt(JdbcMapping.dateTime(rs, "created_at")); book.setUpdatedAt(JdbcMapping.dateTime(rs, "updated_at"));
        book.setAverageRating(rs.getBigDecimal("average_rating")); book.setReviewCount(rs.getLong("review_count"));
        return book;
    }

    private String value(String input) { return input == null ? "" : input.trim(); }
}
