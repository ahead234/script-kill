package com.example.scriptbooking.dao;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Room;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.DbErrors;
import com.example.scriptbooking.util.Page;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 房间数据访问。
 * 所有 SQL 均使用 PreparedStatement 占位符，分页参数由 bindFilter 返回的下标续接，
 * 避免像改造前那样写死 setInt(4)/setInt(5) 这类魔法下标。
 */
public class RoomDao {
    private static final String SELECT_COLUMNS =
            "SELECT id,room_code,room_name,capacity,location,status,note,created_at,updated_at FROM room ";

    public Page<Room> findPage(String keyword, String status, int page, int pageSize) {
        String term = value(keyword);
        String state = value(status);
        String where = "WHERE (?='' OR room_name LIKE CONCAT('%',?,'%') OR room_code LIKE CONCAT('%',?,'%')) " +
                "AND (?='' OR status=?) ";
        String querySql = SELECT_COLUMNS + where + "ORDER BY status,room_code LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM room " + where;
        try (Connection connection = Database.getConnection()) {
            long total;
            try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                bindFilter(statement, term, state);
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    total = rs.getLong(1);
                }
            }
            List<Room> rooms = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(querySql)) {
                int next = bindFilter(statement, term, state);
                statement.setInt(next, pageSize);
                statement.setInt(next + 1, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) rooms.add(map(rs));
                }
            }
            return new Page<>(rooms, page, pageSize, total);
        } catch (SQLException e) {
            throw DbErrors.fail("查询房间列表失败", e);
        }
    }

    public Optional<Room> findById(long id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COLUMNS + "WHERE id=?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("查询房间失败", e);
        }
    }

    /** 按房间名查档案；场次保存时用它判断房间是否存在、是否可排期。 */
    public Optional<Room> findByName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) return Optional.empty();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COLUMNS + "WHERE room_name=?")) {
            statement.setString(1, roomName.trim());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("查询房间失败", e);
        }
    }

    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COLUMNS + "ORDER BY status,room_code");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) rooms.add(map(rs));
            return rooms;
        } catch (SQLException e) {
            throw DbErrors.fail("查询房间失败", e);
        }
    }

    /** 唯一性校验：排除自身，新增时 excludedId 传 0。列名固定，不接受外部传入，避免拼接注入。 */
    public boolean codeExists(String code, long excludedId) {
        return exists("SELECT 1 FROM room WHERE room_code=? AND id<>?", code, excludedId);
    }

    public boolean nameExists(String name, long excludedId) {
        return exists("SELECT 1 FROM room WHERE room_name=? AND id<>?", name, excludedId);
    }

    private boolean exists(String sql, String value, long excludedId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.setLong(2, excludedId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw DbErrors.fail("校验房间信息失败", e);
        }
    }

    public long insert(Room room) {
        String sql = "INSERT INTO room(room_code,room_name,capacity,location,status,note) VALUES(?,?,?,?,?,?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindWrite(statement, room);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new BusinessException("未能取得新房间编号");
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw DbErrors.fail("房间编号或名称已存在", e);
            throw DbErrors.fail("新增房间失败", e);
        }
    }

    public void update(Room room) {
        String sql = "UPDATE room SET room_code=?,room_name=?,capacity=?,location=?,status=?,note=? WHERE id=?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindWrite(statement, room);
            statement.setLong(7, room.getId());
            if (statement.executeUpdate() == 0) throw new BusinessException("房间不存在");
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) throw DbErrors.fail("房间编号或名称已存在", e);
            throw DbErrors.fail("修改房间失败", e);
        }
    }

    public void updateStatus(long id, String status) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE room SET status=? WHERE id=?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) throw new BusinessException("房间不存在");
        } catch (SQLException e) {
            throw DbErrors.fail("更新房间状态失败", e);
        }
    }

    private int bindFilter(PreparedStatement statement, String term, String status) throws SQLException {
        int index = 1;
        statement.setString(index++, term);
        statement.setString(index++, term);
        statement.setString(index++, term);
        statement.setString(index++, status);
        statement.setString(index++, status);
        return index;
    }

    private void bindWrite(PreparedStatement statement, Room room) throws SQLException {
        statement.setString(1, room.getRoomCode());
        statement.setString(2, room.getRoomName());
        statement.setInt(3, room.getCapacity());
        statement.setString(4, room.getLocation());
        statement.setString(5, room.getStatus());
        statement.setString(6, room.getNote());
    }

    private Room map(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setRoomCode(rs.getString("room_code"));
        room.setRoomName(rs.getString("room_name"));
        room.setCapacity(rs.getInt("capacity"));
        room.setLocation(rs.getString("location"));
        room.setStatus(rs.getString("status"));
        room.setNote(rs.getString("note"));
        room.setCreatedAt(JdbcMapping.dateTime(rs, "created_at"));
        room.setUpdatedAt(JdbcMapping.dateTime(rs, "updated_at"));
        return room;
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
