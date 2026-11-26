package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.BookingDao;
import com.example.scriptbooking.dao.SessionDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Booking;
import com.example.scriptbooking.model.PlaySession;
import com.example.scriptbooking.service.BookingService;

import java.util.List;

/**
 * 玩家预约：提交预约、我的预约、预约详情、取消预约。
 */
public class BookingAction extends BaseAction {
    private final BookingDao bookingDao = new BookingDao();
    private final SessionDao sessionDao = new SessionDao();
    private final BookingService bookingService = new BookingService();

    private Long sessionId;
    private Long id;
    private Long targetSessionId;
    private Integer partySize;
    private String contactName;
    private String contactPhone;
    private String playerNote;

    private PlaySession playSession;
    private List<Booking> bookings;
    private Booking booking;
    private List<PlaySession> candidates;

    public String create() throws Exception {
        return isPost() ? submit() : showForm();
    }

    private String showForm() throws Exception {
        PlaySession found = sessionDao.findById(requiredLong(sessionId, "sessionId")).orElse(null);
        if (found == null) {
            return notFound();
        }
        if (!found.isBookable()) {
            flash("danger", "该场次当前不可预约");
            return redirectTo("/sessions/detail?id=" + found.getId());
        }
        playSession = found;
        setPageTitle("提交预约");
        return SUCCESS;
    }

    private String submit() throws Exception {
        try {
            long bookingId = bookingService.create(currentUser().getId(), requiredLong(sessionId, "sessionId"),
                    requiredInt(partySize, "partySize"), contactName, contactPhone, playerNote, clientIp());
            flash("success", "预约成功，系统已为您锁定名额");
            return redirectTo("/bookings/detail?id=" + bookingId);
        } catch (BusinessException e) {
            setError(e.getMessage());
            try {
                playSession = sessionDao.findById(requiredLong(sessionId, "sessionId")).orElse(null);
            } catch (BusinessException ignored) {
                // 场次参数无效时保留空表单，错误提示已经写入 error
            }
            setPageTitle("提交预约");
            return INPUT;
        }
    }

    public String list() {
        bookings = bookingDao.findForUser(currentUser().getId());
        setPageTitle("我的预约");
        return SUCCESS;
    }

    public String detail() throws Exception {
        Booking found = bookingDao.findById(requiredLong(id, "id")).orElse(null);
        if (found == null) {
            return notFound();
        }
        if (found.getUserId() != currentUser().getId()) {
            return forbid();
        }
        booking = found;
        setPageTitle("预约详情");
        return SUCCESS;
    }

    public String cancel() throws Exception {
        try {
            bookingService.cancelByPlayer(requiredLong(id, "id"), currentUser().getId(), clientIp());
            flash("success", "预约已取消，名额已释放");
        } catch (BusinessException e) {
            flash("danger", e.getMessage());
        }
        return redirectTo("/my/bookings");
    }

    /**
     * 改期入口：GET 渲染可选场次，POST 提交。
     * 归属校验与规则判定都在这里做一次预检，真正的规则仍以 BookingService 为准（前端不承担校验）。
     */
    public String reschedule() throws Exception {
        return isPost() ? submitReschedule() : showRescheduleForm();
    }

    private String showRescheduleForm() throws Exception {
        Booking found = bookingDao.findById(requiredLong(id, "id")).orElse(null);
        if (found == null) {
            return notFound();
        }
        if (found.getUserId() != currentUser().getId()) {
            return forbid();
        }
        if (!found.isReschedulable()) {
            flash("danger", "该预约当前不可改期：仅已确认、未改过期且距开演 2 小时以上的预约可以改期");
            return redirectTo("/bookings/detail?id=" + found.getId());
        }
        fillRescheduleForm(found);
        return SUCCESS;
    }

    private String submitReschedule() throws Exception {
        long bookingId = requiredLong(id, "id");
        try {
            bookingService.reschedule(bookingId, currentUser().getId(),
                    requiredLong(targetSessionId, "targetSessionId"), clientIp());
            flash("success", "改期成功，新的场次与名额已生效");
            return redirectTo("/bookings/detail?id=" + bookingId);
        } catch (BusinessException e) {
            setError(e.getMessage());
            Booking found = bookingDao.findById(bookingId).orElse(null);
            if (found == null) {
                return notFound();
            }
            fillRescheduleForm(found);
            return INPUT;
        }
    }

    private void fillRescheduleForm(Booking found) {
        booking = found;
        List<PlaySession> available = sessionDao.findAvailableByScript(found.getScriptId());
        available.removeIf(session -> session.getId() == found.getSessionId());
        candidates = available;
        setPageTitle("申请改期");
    }

    public PlaySession getPlaySession() {
        return playSession;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public Booking getBooking() {
        return booking;
    }

    public List<PlaySession> getCandidates() {
        return candidates;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTargetSessionId(Long targetSessionId) {
        this.targetSessionId = targetSessionId;
    }

    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void setPlayerNote(String playerNote) {
        this.playerNote = playerNote;
    }
}
