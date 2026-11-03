package com.example.scriptbooking.util;

import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public final class WebUtil {
    private WebUtil() {
    }

    public static long longParam(HttpServletRequest request, String name) {
        try {
            return Long.parseLong(request.getParameter(name));
        } catch (NumberFormatException e) {
            throw new BusinessException("参数 " + name + " 不正确");
        }
    }

    public static int intParam(HttpServletRequest request, String name) {
        try {
            return Integer.parseInt(request.getParameter(name));
        } catch (NumberFormatException e) {
            throw new BusinessException("参数 " + name + " 不正确");
        }
    }

    public static Integer optionalInt(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("参数 " + name + " 不正确");
        }
    }

    public static int page(HttpServletRequest request) {
        String value = request.getParameter("page");
        if (value == null || value.trim().isEmpty()) return 1;
        try {
            return Math.max(1, Math.min(100000, Integer.parseInt(value)));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static LocalDateTime dateTime(HttpServletRequest request, String name) {
        try {
            return LocalDateTime.parse(request.getParameter(name));
        } catch (DateTimeParseException | NullPointerException e) {
            throw new BusinessException("时间参数 " + name + " 不正确");
        }
    }

    public static LocalDate optionalDate(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期参数 " + name + " 不正确");
        }
    }

    public static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("currentUser");
    }

    public static void flash(HttpServletRequest request, String type, String message) {
        request.getSession().setAttribute("flashType", type);
        request.getSession().setAttribute("flashMessage", message);
    }

    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
