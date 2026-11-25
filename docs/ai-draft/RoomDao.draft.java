// AI 初稿（未加技术基线约束时的原始输出，仅作审查留档，不参与编译）
// 生成工具：WorkBuddy 内置 AI 助手（DeepSeek-V4.1-Flash）
// 原始 prompt：「用 Java 写一个房间管理的 DAO，支持分页查询和新增房间」
package com.example.scriptbooking.dao;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {
    private static RoomDao instance;
    private static final String URL = "jdbc:mysql://localhost:3306/script_booking?useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "root123";

    private RoomDao() {}

    public static RoomDao getInstance() {
        if (instance == null) {
            instance = new RoomDao();
        }
        return instance;
    }

    public List<Room> findPage(String keyword, int page, int pageSize) {
        List<Room> list = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM room WHERE room_name LIKE '%" + keyword + "%'"
                    + " ORDER BY id DESC LIMIT " + (page - 1) * pageSize + "," + pageSize;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getLong("id"));
                room.setRoomName(rs.getString("roomName"));
                room.setCapacity(rs.getInt("capacity"));
                list.add(room);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void save(Room room) {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO room(room_name,capacity) VALUES('"
                    + room.getRoomName() + "'," + room.getCapacity() + ")");
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
