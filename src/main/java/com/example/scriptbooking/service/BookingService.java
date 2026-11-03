package com.example.scriptbooking.service;

import com.example.scriptbooking.dao.BookingDao;
import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.dao.SessionDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Booking;
import com.example.scriptbooking.model.PlaySession;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.CodeGenerator;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Sets;
import com.example.scriptbooking.util.Validators;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Set;

public class BookingService {
    private static final Set<String> STAFF_TARGETS = Sets.of("CHECKED_IN", "COMPLETED", "NO_SHOW");
    private final BookingDao bookingDao = new BookingDao();
    private final SessionDao sessionDao = new SessionDao();
    private final OperationLogDao logDao = new OperationLogDao();

    public long create(long userId, long sessionId, int partySize, String contactName, String contactPhone,
                       String note, String ip) {
        Validators.positive(partySize, "预约人数");
        String name = Validators.required(contactName, "联系人", 60);
        String phone = Validators.phone(contactPhone);
        String playerNote = Validators.optional(note, "玩家备注", 500);
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PlaySession session = sessionDao.findById(connection, sessionId, true)
                        .orElseThrow(() -> new BusinessException("场次不存在"));
                if (!"OPEN".equals(session.getStatus()) || !session.getStartTime().isAfter(LocalDateTime.now())) {
                    throw new BusinessException("该场次当前不可预约");
                }
                if (partySize > session.getScriptMaxPlayers()) throw new BusinessException("预约人数超过剧本最多人数");
                if (partySize > session.getRemainingCapacity()) throw new BusinessException("剩余容量不足");
                if (bookingDao.activeExists(connection, userId, sessionId)) throw new BusinessException("您已预约过该场次");
                Booking booking = new Booking();
                booking.setBookingCode(CodeGenerator.bookingCode()); booking.setUserId(userId);
                booking.setSessionId(sessionId); booking.setPartySize(partySize); booking.setContactName(name);
                booking.setContactPhone(phone); booking.setPlayerNote(playerNote);
                long id = bookingDao.insert(connection, booking);
                sessionDao.changeBookedCount(connection, sessionId, partySize);
                logDao.insert(connection, userId, "BOOKING_CREATE", "BOOKING", id,
                        "场次=" + session.getSessionCode() + ", 人数=" + partySize, ip);
                connection.commit();
                return id;
            } catch (Exception e) {
                rollback(connection);
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw new BusinessException("创建预约失败，请稍后重试", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("创建预约失败，请稍后重试", e);
        }
    }

    public void cancelByPlayer(long bookingId, long userId, String ip) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking snapshot = bookingDao.findRaw(connection, bookingId)
                        .orElseThrow(() -> new BusinessException("预约不存在"));
                PlaySession session = sessionDao.findById(connection, snapshot.getSessionId(), true)
                        .orElseThrow(() -> new BusinessException("场次不存在"));
                Booking booking = bookingDao.findForUpdate(connection, bookingId)
                        .orElseThrow(() -> new BusinessException("预约不存在"));
                if (booking.getSessionId() != session.getId()) throw new BusinessException("预约关联场次已变更，请重试");
                if (booking.getUserId() != userId) throw new BusinessException("无权取消他人的预约");
                if (!"CONFIRMED".equals(booking.getStatus())) throw new BusinessException("当前预约状态不允许取消");
                if (!session.getStartTime().isAfter(LocalDateTime.now())) throw new BusinessException("场次开始后不能取消预约");
                bookingDao.updateStatus(connection, bookingId, "CANCELLED_BY_PLAYER");
                sessionDao.changeBookedCount(connection, booking.getSessionId(), -booking.getPartySize());
                logDao.insert(connection, userId, "BOOKING_CANCEL_PLAYER", "BOOKING", bookingId, "玩家取消预约", ip);
                connection.commit();
            } catch (Exception e) {
                rollback(connection);
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw new BusinessException("取消预约失败", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("取消预约失败", e);
        }
    }

    public void changeStatus(long bookingId, String targetStatus, User operator, String ip) {
        Validators.oneOf(targetStatus, STAFF_TARGETS, "预约状态");
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking snapshot = bookingDao.findRaw(connection, bookingId)
                        .orElseThrow(() -> new BusinessException("预约不存在"));
                PlaySession session = sessionDao.findById(connection, snapshot.getSessionId(), true)
                        .orElseThrow(() -> new BusinessException("场次不存在"));
                Booking booking = bookingDao.findForUpdate(connection, bookingId)
                        .orElseThrow(() -> new BusinessException("预约不存在"));
                if (booking.getSessionId() != session.getId()) throw new BusinessException("预约关联场次已变更，请重试");
                int capacityDelta = 0;
                if ("CHECKED_IN".equals(targetStatus)) {
                    if (!"CONFIRMED".equals(booking.getStatus())) throw new BusinessException("仅已确认预约可以登记到店");
                } else if ("NO_SHOW".equals(targetStatus)) {
                    if (!"CONFIRMED".equals(booking.getStatus())) throw new BusinessException("仅已确认预约可以标记未到店");
                    if (LocalDateTime.now().isBefore(session.getStartTime())) throw new BusinessException("场次开始前不能标记未到店");
                    capacityDelta = -booking.getPartySize();
                } else {
                    if (!"CHECKED_IN".equals(booking.getStatus())) throw new BusinessException("仅已到店预约可以完成");
                    if (LocalDateTime.now().isBefore(session.getEndTime())) throw new BusinessException("场次结束前不能标记完成");
                    capacityDelta = -booking.getPartySize();
                }
                bookingDao.updateStatus(connection, bookingId, targetStatus);
                if (capacityDelta != 0) sessionDao.changeBookedCount(connection, session.getId(), capacityDelta);
                logDao.insert(connection, operator.getId(), "BOOKING_STATUS_" + targetStatus, "BOOKING", bookingId,
                        "预约状态由" + booking.getStatus() + "变更为" + targetStatus, ip);
                connection.commit();
            } catch (Exception e) {
                rollback(connection);
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw new BusinessException("更新预约状态失败", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("更新预约状态失败", e);
        }
    }

    private void rollback(Connection connection) {
        try { connection.rollback(); } catch (SQLException ignored) { }
    }
}
