package com.example.scriptbooking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Booking {
    private long id;
    private String bookingCode;
    private long userId;
    private long sessionId;
    private Long originalSessionId;
    private int partySize;
    private int rescheduleCount;
    private String contactName;
    private String contactPhone;
    private String status;
    private String playerNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userDisplayName;
    private String username;
    private String sessionCode;
    private long scriptId;
    private String scriptTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomName;
    private String dmName;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private Long reviewId;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }
    public Long getOriginalSessionId() { return originalSessionId; }
    public void setOriginalSessionId(Long originalSessionId) { this.originalSessionId = originalSessionId; }
    public int getRescheduleCount() { return rescheduleCount; }
    public void setRescheduleCount(int rescheduleCount) { this.rescheduleCount = rescheduleCount; }

    /**
     * 是否允许自助改期：已确认、未改过期、且距开演还有 2 小时以上。
     * 规则放在模型里，列表页、详情页与 Action 共用同一判定，避免三处各写一遍。
     */
    public boolean isReschedulable() {
        return "CONFIRMED".equals(status) && rescheduleCount < 1 && startTime != null
                && LocalDateTime.now().isBefore(startTime.minusHours(2));
    }
    public int getPartySize() { return partySize; }
    public void setPartySize(int partySize) { this.partySize = partySize; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPlayerNote() { return playerNote; }
    public void setPlayerNote(String playerNote) { this.playerNote = playerNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
    public long getScriptId() { return scriptId; }
    public void setScriptId(long scriptId) { this.scriptId = scriptId; }
    public String getScriptTitle() { return scriptTitle; }
    public void setScriptTitle(String scriptTitle) { this.scriptTitle = scriptTitle; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getDmName() { return dmName; }
    public void setDmName(String dmName) { this.dmName = dmName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice; }
    public BigDecimal getTotalPrice() { return unitPrice.multiply(BigDecimal.valueOf(partySize)); }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
}
