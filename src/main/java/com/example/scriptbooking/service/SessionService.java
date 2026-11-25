package com.example.scriptbooking.service;

import com.example.scriptbooking.dao.BookingDao;
import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.dao.RoomDao;
import com.example.scriptbooking.dao.ScriptBookDao;
import com.example.scriptbooking.dao.SessionDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.PlaySession;
import com.example.scriptbooking.model.ScriptBook;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.CodeGenerator;
import com.example.scriptbooking.util.Database;
import com.example.scriptbooking.util.Sets;
import com.example.scriptbooking.util.Validators;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class SessionService {
    private static final Set<String> EDITABLE_STATUSES = Sets.of("DRAFT", "OPEN", "CLOSED", "IN_PROGRESS", "COMPLETED");
    private final SessionDao sessionDao = new SessionDao();
    private final ScriptBookDao scriptDao = new ScriptBookDao();
    private final RoomDao roomDao = new RoomDao();
    private final BookingDao bookingDao = new BookingDao();
    private final OperationLogDao logDao = new OperationLogDao();

    public long save(PlaySession session, User operator, String ip) {
        validate(session);
        ScriptBook script = scriptDao.findById(session.getScriptId()).orElseThrow(() -> new BusinessException("剧本不存在"));
        if ("OPEN".equals(session.getStatus()) && !"ACTIVE".equals(script.getStatus())) {
            throw new BusinessException("停用剧本不能创建或开放可预约场次");
        }
        if (session.getCapacity() < script.getMinPlayers() || session.getCapacity() > script.getMaxPlayers()) {
            throw new BusinessException("场次容量必须在剧本人数范围内");
        }
        PlaySession current = null;
        if (session.getId() != 0) {
            current = sessionDao.findById(session.getId()).orElseThrow(() -> new BusinessException("场次不存在"));
            if ("CANCELLED".equals(current.getStatus()) || "COMPLETED".equals(current.getStatus())) {
                throw new BusinessException("已取消或已结束场次不能修改");
            }
            if (session.getCapacity() < current.getBookedCount()) throw new BusinessException("容量不能小于当前有效预约人数");
            session.setBookedCount(current.getBookedCount());
        }
        boolean creating = session.getId() == 0;
        // 房间档案中登记过、且处于检修或停用的房间不允许安排新场次；换房间时同样校验
        if (creating || !session.getRoomName().equals(current.getRoomName())) {
            ensureRoomBookable(session.getRoomName());
        }
        if (sessionDao.roomConflict(session.getId(), session.getRoomName(), session.getStartTime(), session.getEndTime())) {
            throw new BusinessException("该房间在所选时间段已有其他场次");
        }
        if (creating && (session.getSessionCode() == null || session.getSessionCode().trim().isEmpty())) {
            session.setSessionCode(CodeGenerator.sessionCode());
        }
        long id;
        if (creating) id = sessionDao.insert(session); else { sessionDao.update(session); id = session.getId(); }
        logDao.insert(operator.getId(), creating ? "SESSION_CREATE" : "SESSION_UPDATE", "SESSION", id,
                "场次编号=" + session.getSessionCode(), ip);
        return id;
    }

    public void cancel(long sessionId, User operator, String ip) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PlaySession session = sessionDao.findById(connection, sessionId, true)
                        .orElseThrow(() -> new BusinessException("场次不存在"));
                if ("CANCELLED".equals(session.getStatus())) throw new BusinessException("场次已取消");
                if ("COMPLETED".equals(session.getStatus())) throw new BusinessException("已结束场次不能取消");
                int affected = bookingDao.cancelActiveBySession(connection, sessionId);
                if (session.getBookedCount() > 0) sessionDao.changeBookedCount(connection, sessionId, -session.getBookedCount());
                sessionDao.updateStatus(connection, sessionId, "CANCELLED");
                logDao.insert(connection, operator.getId(), "SESSION_CANCEL", "SESSION", sessionId,
                        "取消场次并处理" + affected + "条有效预约", ip);
                connection.commit();
            } catch (Exception e) {
                rollback(connection);
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw new BusinessException("取消场次失败", e);
            }
        } catch (SQLException e) {
            throw new BusinessException("取消场次失败", e);
        }
    }

    /** 房间档案里登记过的房间，只有“可用”状态才能被排期占用。 */
    private void ensureRoomBookable(String roomName) {
        roomDao.findByName(roomName).filter(room -> !room.isBookable()).ifPresent(room -> {
            throw new BusinessException("房间「" + room.getRoomName() + "」当前为" + room.getStatusLabel() + "状态，不能安排场次");
        });
    }

    private void validate(PlaySession session) {
        session.setSessionCode(Validators.optional(session.getSessionCode(), "场次编号", 30));
        Validators.positive((int) session.getScriptId(), "剧本");
        Validators.timeRange(session.getStartTime(), session.getEndTime());
        Validators.positive(session.getCapacity(), "场次容量");
        session.setRoomName(Validators.optional(session.getRoomName(), "房间", 60));
        session.setDmName(Validators.optional(session.getDmName(), "DM", 60));
        if (session.getUnitPrice() == null) session.setUnitPrice(BigDecimal.ZERO);
        if (session.getUnitPrice().signum() < 0) throw new BusinessException("单价不能小于0");
        session.setStatus(Validators.oneOf(session.getStatus(), EDITABLE_STATUSES, "场次状态"));
        session.setRemark(Validators.optional(session.getRemark(), "备注", 500));
    }

    private void rollback(Connection connection) {
        try { connection.rollback(); } catch (SQLException ignored) { }
    }
}
