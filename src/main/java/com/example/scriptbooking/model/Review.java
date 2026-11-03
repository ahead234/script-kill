package com.example.scriptbooking.model;

import java.time.LocalDateTime;

public class Review {
    private long id;
    private long bookingId;
    private long userId;
    private int rating;
    private Integer storyRating;
    private Integer dmRating;
    private Integer atmosphereRating;
    private String content;
    private String status;
    private String moderationReason;
    private Long moderatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String bookingCode;
    private String scriptTitle;
    private String userDisplayName;
    private String moderatorName;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getBookingId() { return bookingId; }
    public void setBookingId(long bookingId) { this.bookingId = bookingId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public Integer getStoryRating() { return storyRating; }
    public void setStoryRating(Integer storyRating) { this.storyRating = storyRating; }
    public Integer getDmRating() { return dmRating; }
    public void setDmRating(Integer dmRating) { this.dmRating = dmRating; }
    public Integer getAtmosphereRating() { return atmosphereRating; }
    public void setAtmosphereRating(Integer atmosphereRating) { this.atmosphereRating = atmosphereRating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getModerationReason() { return moderationReason; }
    public void setModerationReason(String moderationReason) { this.moderationReason = moderationReason; }
    public Long getModeratedBy() { return moderatedBy; }
    public void setModeratedBy(Long moderatedBy) { this.moderatedBy = moderatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public String getScriptTitle() { return scriptTitle; }
    public void setScriptTitle(String scriptTitle) { this.scriptTitle = scriptTitle; }
    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
    public String getModeratorName() { return moderatorName; }
    public void setModeratorName(String moderatorName) { this.moderatorName = moderatorName; }
}
