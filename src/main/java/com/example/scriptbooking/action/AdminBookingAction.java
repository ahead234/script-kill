package com.example.scriptbooking.action;

import com.example.scriptbooking.dao.BookingDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Booking;
import com.example.scriptbooking.service.BookingService;

/**
 * 后台预约接待：列表筛选、预约详情、到店/完成/未到店状态流转。
 */
public class AdminBookingAction extends BaseAction {
    private final BookingDao bookingDao = new BookingDao();
    private final BookingService bookingService = new BookingService();

    private String keyword;
    private String status;
    private Long id;

    private Object result;
    private Booking booking;

    public String list() {
        result = bookingDao.findPage(keyword, status, pageNo(), 12);
        setPageTitle("预约管理");
        return SUCCESS;
    }

    public String detail() throws Exception {
        Booking found = bookingDao.findById(requiredLong(id, "id")).orElse(null);
        if (found == null) {
            return notFound();
        }
        booking = found;
        setPageTitle("预约详情");
        return SUCCESS;
    }

    public String changeStatus() throws Exception {
        if (!isPost()) {
            return methodNotAllowed();
        }
        long bookingId;
        try {
            bookingId = requiredLong(id, "id");
            bookingService.changeStatus(bookingId, status, currentUser(), clientIp());
            flash("success", "预约状态已更新");
        } catch (BusinessException e) {
            bookingId = 0;
            flash("danger", e.getMessage());
        }
        return redirectTo(bookingId == 0 ? "/admin/bookings" : "/admin/bookings/detail?id=" + bookingId);
    }

    public Object getResult() {
        return result;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
