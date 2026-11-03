package com.example.scriptbooking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlaySession {
    private long id;
    private String sessionCode;
    private long scriptId;
    private String scriptTitle;
    private String scriptGenre;
    private int scriptMinPlayers;
    private int scriptMaxPlayers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int capacity;
    private int bookedCount;
    private String roomName;
    private String dmName;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private String status;
    private String remark;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
    public long getScriptId() { return scriptId; }
    public void setScriptId(long scriptId) { this.scriptId = scriptId; }
    public String getScriptTitle() { return scriptTitle; }
    public void setScriptTitle(String scriptTitle) { this.scriptTitle = scriptTitle; }
    public String getScriptGenre() { return scriptGenre; }
    public void setScriptGenre(String scriptGenre) { this.scriptGenre = scriptGenre; }
    public int getScriptMinPlayers() { return scriptMinPlayers; }
    public void setScriptMinPlayers(int scriptMinPlayers) { this.scriptMinPlayers = scriptMinPlayers; }
    public int getScriptMaxPlayers() { return scriptMaxPlayers; }
    public void setScriptMaxPlayers(int scriptMaxPlayers) { this.scriptMaxPlayers = scriptMaxPlayers; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getBookedCount() { return bookedCount; }
    public void setBookedCount(int bookedCount) { this.bookedCount = bookedCount; }
    public int getRemainingCapacity() { return Math.max(0, capacity - bookedCount); }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getDmName() { return dmName; }
    public void setDmName(String dmName) { this.dmName = dmName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public boolean isBookable() { return "OPEN".equals(status) && startTime != null && startTime.isAfter(LocalDateTime.now()) && getRemainingCapacity() > 0; }
}
