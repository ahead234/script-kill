package com.example.scriptbooking.model;

import java.time.LocalDateTime;

/**
 * 房间实体：剧本杀门店可排期的物理房间。
 * 与 play_session.room_name 通过名称关联，新增场次时用状态校验房间是否可用。
 */
public class Room {
    private long id;
    private String roomCode;
    private String roomName;
    private int capacity;
    private String location;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** 列表页展示用：把枚举值翻译成中文，避免在 JSP 里堆 c:choose。 */
    public String getStatusLabel() {
        if ("ACTIVE".equals(status)) return "可用";
        if ("MAINTENANCE".equals(status)) return "检修";
        if ("DISABLED".equals(status)) return "停用";
        return status;
    }

    /** 只有可用房间允许被新场次占用。 */
    public boolean isBookable() {
        return "ACTIVE".equals(status);
    }
}
