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
    private Integer partySize;
    private String contactName;
    private String contactPhone;
    private String playerNote;

    private PlaySession playSession;
    private List<Booking> bookings;
    private Booking booking;

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

    public PlaySession getPlaySession() {
        return playSession;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setId(Long id) {
        this.id = id;
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
