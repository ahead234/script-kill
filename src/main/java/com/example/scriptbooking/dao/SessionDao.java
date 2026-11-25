package com.example.scriptbooking.dao;
import com.example.scriptbooking.util.DbErrors;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.PlaySession;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Page;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SessionDao {
    private static final String SELECT_COLUMNS = "SELECT ps.id,ps.session_code,ps.script_id,ps.start_time,ps.end_time,ps.capacity," +
            "ps.booked_count,ps.room_name,ps.dm_name,ps.unit_price,ps.status,ps.remark," +
            "s.title script_title,s.genre script_genre,s.min_players script_min_players,s.max_players script_max_players " +
            "FROM play_session ps JOIN script_book s ON s.id=ps.script_id ";

    public Page<PlaySession> findPage(SessionQuery query, int page, int pageSize) {
        // 日期参数必须绑定为 DATE/NULL：绑定空字符串时 MySQL 会报 Incorrect DATE value: ''
        String where = "WHERE (? IS NULL OR DATE(ps.start_time)=?) " +
                "AND (?='' OR s.title LIKE CONCAT('%',?,'%') OR ps.session_code LIKE CONCAT('%',?,'%')) " +
                "AND (?='' OR ps.status=?) ";
        String querySql = SELECT_COLUMNS + where + "ORDER BY ps.start_time DESC LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM play_session ps JOIN script_book s ON s.id=ps.script_id " + where;
        try (Connection connection = Database.getConnection()) {
            long total;
            try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                bindFilter(statement, query);
                try (ResultSet rs = statement.executeQuery()) { rs.next(); total = rs.getLong(1); }
            }
            List<PlaySession> sessions = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(querySql)) {
                // bindFilter 返回下一个待绑定下标，分页参数紧接在筛选条件之后，不再写死 8、9
                int next = bindFilter(statement, query);
                statement.setInt(next, pageSize);
                statement.setInt(next + 1, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) sessions.add(map(rs));
                }
            }
            return new Page<>(sessions, page, pageSize, total);
        } catch (SQLException e) {
            throw DbErrors.fail("查询场次列表失败", e);
        }
    }

    public Optional<PlaySession> findById(long id) {
        try (Connection connection = Database.getConnection()) {
            return findById(connection, id, false);
        } catch (SQLException e) {
            throw DbErrors.fail("查询场次失败", e);
        }
    }

    public Optional<PlaySession> findById(Connection connection, long id, boolean forUpdate) throws SQLException {
        String sql = SELECT_COLUMNS + "WHERE ps.id=?" + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<PlaySession> findAvailableByScript(long scriptId) {
        String sql = SELECT_COLUMNS + "WHERE ps.script_id=? AND ps.status='OPEN' AND ps.start_time>NOW() " +
                "AND ps.booked_count<ps.capacity ORDER BY ps.start_time LIMIT 20";
        List<PlaySession> sessions = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, scriptId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) sessions.add(map(rs));
            }
            return sessions;
        } catch (SQLException e) {
            throw DbErrors.fail("查询可预约场次失败", e);
        }
    }

    public List<PlaySession> findUpcoming(int limit) {
        String sql = SELECT_COLUMNS + "WHERE ps.status='OPEN' AND ps.start_time>NOW() AND s.status='ACTIVE' " +
                "ORDER BY ps.start_time LIMIT ?";
        List<PlaySession> sessions = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) sessions.add(map(rs));
            }
            return sessions;
        } catch (SQLException e) {
            throw DbErrors.fail("查询即将开始的场次失败", e);
        }
    }

    public boolean roomConflict(long excludedId, String roomName, LocalDateTime start, LocalDateTime end) {
        if (roomName == null || roomName.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM play_session WHERE id<>? AND room_name=? AND status<>'CANCELLED' " +
                "AND start_time<? AND end_time>? LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, excludedId);
            statement.setString(2, roomName);
            statement.setTimestamp(3, Timestamp.valueOf(end));
            statement.setTimestamp(4, Timestamp.valueOf(start));
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw DbErrors.fail("检查房间占用失败", e);
        }
    }

    /**
     * 统计某房间尚未结束的有效场次数量。
     * 房间要停用或转检修时，若还有 OPEN/IN_PROGRESS 场次，必须先处理这些排期。
     */
    public long countUnfinishedByRoom(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) return 0;
        String sql = "SELECT COUNT(*) FROM play_session WHERE room_name=? AND status IN ('OPEN','IN_PROGRESS') AND end_time>NOW()";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomName.trim());
            try (ResultSet rs = statement.executeQuery()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) {
            throw DbErrors.fail("统计房间排期失败", e);
        }
    }

    public long insert(PlaySession session) {
        String sql = "INSERT INTO play_session(session_code,script_id,start_time,end_time,capacity,booked_count,room_name,dm_name,unit_price,status,remark) " +
                "VALUES(?,?,?,?,?,0,?,?,?,?,?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindWrite(statement, session);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new BusinessException("未能取得新场次编号");
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw DbErrors.fail("场次编号已存在或数据不合法", e);
            throw DbErrors.fail("新增场次失败", e);
        }
    }

    public void update(PlaySession session) {
        String sql = "UPDATE play_session SET session_code=?,script_id=?,start_time=?,end_time=?,capacity=?," +
                "room_name=?,dm_name=?,unit_price=?,status=?,remark=? WHERE id=? AND status NOT IN ('CANCELLED','COMPLETED')";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindWrite(statement, session);
            statement.setLong(11, session.getId());
            if (statement.executeUpdate() == 0) throw new BusinessException("场次不存在、已取消或已结束");
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw DbErrors.fail("场次编号已存在或数据不合法", e);
            throw DbErrors.fail("修改场次失败", e);
        }
    }

    public void updateStatus(Connection connection, long id, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE play_session SET status=? WHERE id=?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) throw new BusinessException("场次不存在");
        }
    }

    public void changeBookedCount(Connection connection, long id, int delta) throws SQLException {
        String sql = "UPDATE play_session SET booked_count=booked_count+? WHERE id=? " +
                "AND booked_count+? BETWEEN 0 AND capacity";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, delta);
            statement.setLong(2, id);
            statement.setInt(3, delta);
            if (statement.executeUpdate() == 0) throw new BusinessException("场次容量变更失败，请刷新后重试");
        }
    }

    public long countByStatus(String status) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM play_session WHERE status=?")) {
            statement.setString(1, status);
            try (ResultSet rs = statement.executeQuery()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) {
            throw DbErrors.fail("统计场次失败", e);
        }
    }

    public Map<String, Long> countGroupByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status, COUNT(*) FROM play_session GROUP BY status");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.put(rs.getString(1), rs.getLong(2));
            return result;
        } catch (SQLException e) {
            throw DbErrors.fail("统计场次状态失败", e);
        }
    }

    /** 绑定筛选条件，返回下一个可用占位符下标（8）。 */
    private int bindFilter(PreparedStatement statement, SessionQuery query) throws SQLException {
        int index = 1;
        if (query.getDate() == null) {
            statement.setNull(index++, Types.DATE);
            statement.setNull(index++, Types.DATE);
        } else {
            java.sql.Date date = java.sql.Date.valueOf(query.getDate());
            statement.setDate(index++, date);
            statement.setDate(index++, date);
        }
        statement.setString(index++, query.getKeyword());
        statement.setString(index++, query.getKeyword());
        statement.setString(index++, query.getKeyword());
        statement.setString(index++, query.getStatus());
        statement.setString(index++, query.getStatus());
        return index;
    }

    private void bindWrite(PreparedStatement statement, PlaySession session) throws SQLException {
        statement.setString(1, session.getSessionCode()); statement.setLong(2, session.getScriptId());
        statement.setTimestamp(3, Timestamp.valueOf(session.getStartTime())); statement.setTimestamp(4, Timestamp.valueOf(session.getEndTime()));
        statement.setInt(5, session.getCapacity()); statement.setString(6, session.getRoomName());
        statement.setString(7, session.getDmName()); statement.setBigDecimal(8, session.getUnitPrice());
        statement.setString(9, session.getStatus()); statement.setString(10, session.getRemark());
    }

    private PlaySession map(ResultSet rs) throws SQLException {
        PlaySession session = new PlaySession();
        session.setId(rs.getLong("id")); session.setSessionCode(rs.getString("session_code"));
        session.setScriptId(rs.getLong("script_id")); session.setScriptTitle(rs.getString("script_title"));
        session.setScriptGenre(rs.getString("script_genre")); session.setScriptMinPlayers(rs.getInt("script_min_players"));
        session.setScriptMaxPlayers(rs.getInt("script_max_players")); session.setStartTime(JdbcMapping.dateTime(rs, "start_time"));
        session.setEndTime(JdbcMapping.dateTime(rs, "end_time")); session.setCapacity(rs.getInt("capacity"));
        session.setBookedCount(rs.getInt("booked_count")); session.setRoomName(rs.getString("room_name"));
        session.setDmName(rs.getString("dm_name")); session.setUnitPrice(rs.getBigDecimal("unit_price"));
        session.setStatus(rs.getString("status")); session.setRemark(rs.getString("remark"));
        return session;
    }
}
