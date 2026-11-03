package com.example.scriptbooking.service;

import com.example.scriptbooking.dao.BookingDao;
import com.example.scriptbooking.dao.OperationLogDao;
import com.example.scriptbooking.dao.ReviewDao;
import com.example.scriptbooking.dao.ScriptBookDao;
import com.example.scriptbooking.dao.SessionDao;
import com.example.scriptbooking.dao.UserDao;
import com.example.scriptbooking.exception.BusinessException;
import com.example.scriptbooking.model.Metric;
import com.example.scriptbooking.model.User;
import com.example.scriptbooking.util.Sets;
import com.example.scriptbooking.util.Validators;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminService {
    private static final Set<String> USER_STATUSES = Sets.of("ACTIVE", "DISABLED");

    private static final int TREND_DAYS = 6;
    private static final int MAX_TREND_DAYS = 366;
    private static final int DONUT_RADIUS = 54;
    private static final int BAR_MAX_HEIGHT = 118;
    private static final int STACK_WIDTH = 320;

    private static final String[] RANK_COLORS = {"#2f6558", "#4f8577", "#6c9b8d", "#8fb3a6", "#b3cbc1"};
    private static final String TREND_COLOR = "#3f6578";

    private static final Map<String, String> BOOKING_LABELS = labels(
            new String[]{"CONFIRMED", "CHECKED_IN", "COMPLETED", "NO_SHOW", "CANCELLED_BY_PLAYER", "CANCELLED_BY_STORE"},
            new String[]{"待到店", "已到店", "已完成", "未到店", "玩家取消", "店家取消"});
    private static final Map<String, String> BOOKING_COLORS = labels(
            new String[]{"CONFIRMED", "CHECKED_IN", "COMPLETED", "NO_SHOW", "CANCELLED_BY_PLAYER", "CANCELLED_BY_STORE"},
            new String[]{"#3f6578", "#8b661f", "#2f6558", "#a84646", "#98a29d", "#7d8782"});
    private static final Map<String, String> REVIEW_LABELS = labels(
            new String[]{"PENDING", "PUBLISHED", "HIDDEN"},
            new String[]{"待审核", "已发布", "已隐藏"});
    private static final Map<String, String> REVIEW_COLORS = labels(
            new String[]{"PENDING", "PUBLISHED", "HIDDEN"},
            new String[]{"#8b661f", "#2f6558", "#a84646"});
    private static final Map<String, String> SESSION_LABELS = labels(
            new String[]{"DRAFT", "OPEN", "CLOSED", "IN_PROGRESS", "COMPLETED", "CANCELLED"},
            new String[]{"草稿", "开放预约", "已关闭", "进行中", "已完成", "已取消"});
    private static final Map<String, String> SESSION_COLORS = labels(
            new String[]{"DRAFT", "OPEN", "CLOSED", "IN_PROGRESS", "COMPLETED", "CANCELLED"},
            new String[]{"#98a29d", "#2f6558", "#7d8782", "#8b661f", "#3f6578", "#a84646"});

    private final UserDao userDao = new UserDao();
    private final SessionDao sessionDao = new SessionDao();
    private final BookingDao bookingDao = new BookingDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final ScriptBookDao scriptDao = new ScriptBookDao();
    private final OperationLogDao logDao = new OperationLogDao();

    public Map<String, Object> dashboard() {
        return dashboard(null, null);
    }

    /**
     * 报表数据。日期区间只作用于预约趋势，其余指标为当前快照；未传区间时默认展示近 7 日。
     */
    public Map<String, Object> dashboard(LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(TREND_DAYS) : from;
        if (start.isAfter(end)) start = end.minusDays(TREND_DAYS);
        if (ChronoUnit.DAYS.between(start, end) > MAX_TREND_DAYS) start = end.minusDays(MAX_TREND_DAYS);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("openSessions", sessionDao.countByStatus("OPEN"));
        data.put("confirmedBookings", bookingDao.countByStatus("CONFIRMED"));
        data.put("checkedInBookings", bookingDao.countByStatus("CHECKED_IN"));
        data.put("pendingReviews", reviewDao.countPending());
        data.put("totalBookings", bookingDao.countAll());
        data.put("scriptCount", scriptDao.countAll());

        List<Metric> bookingStatus = toMetrics(bookingDao.countGroupByStatus(), BOOKING_LABELS, BOOKING_COLORS);
        applyDonut(bookingStatus);
        data.put("bookingStatus", bookingStatus);

        List<Metric> sessionStatus = toMetrics(sessionDao.countGroupByStatus(), SESSION_LABELS, SESSION_COLORS);
        applyStack(sessionStatus, STACK_WIDTH);
        data.put("sessionStatus", sessionStatus);

        List<Metric> reviewStatus = toMetrics(reviewDao.countGroupByStatus(), REVIEW_LABELS, REVIEW_COLORS);
        applyStack(reviewStatus, STACK_WIDTH);
        data.put("reviewStatus", reviewStatus);

        List<Metric> scriptDemand = toMetrics(bookingDao.countByScript(5), null, null);
        scaleHeights(scriptDemand, BAR_MAX_HEIGHT);
        data.put("scriptDemand", scriptDemand);

        List<Metric> genreMix = toMetrics(scriptDao.countGroupByGenre(), null, null);
        scaleHeights(genreMix, BAR_MAX_HEIGHT);
        data.put("genreMix", genreMix);

        data.put("trend", toTrend(bookingDao.countByDay(start, end), start, end));
        return data;
    }

    public void changeUserStatus(long userId, String status, User operator, String ip) {
        Validators.oneOf(status, USER_STATUSES, "用户状态");
        if (userId == operator.getId() && "DISABLED".equals(status)) throw new BusinessException("不能禁用当前登录账号");
        userDao.updateStatus(userId, status);
        logDao.insert(operator.getId(), "USER_STATUS_" + status, "USER", userId, "用户状态变更为" + status, ip);
    }

    private List<Metric> toMetrics(Map<String, Long> raw, Map<String, String> labels, Map<String, String> colors) {
        List<Metric> metrics = new ArrayList<>();
        if (raw == null) return metrics;
        long total = 0;
        for (Long value : raw.values()) total += value == null ? 0L : value;
        int index = 0;
        for (Map.Entry<String, Long> entry : raw.entrySet()) {
            String key = entry.getKey();
            long count = entry.getValue() == null ? 0L : entry.getValue();
            Metric metric = new Metric();
            metric.setLabel(pick(labels, key, key));
            metric.setCount(count);
            metric.setPercent(total == 0 ? 0 : (int) Math.round(count * 100.0 / total));
            metric.setColor(pick(colors, key, RANK_COLORS[index % RANK_COLORS.length]));
            metrics.add(metric);
            index++;
        }
        return metrics;
    }

    private List<Metric> toTrend(Map<String, Long> raw, LocalDate start, LocalDate end) {
        List<Metric> metrics = new ArrayList<>();
        long max = 1;
        if (raw != null) {
            for (Long value : raw.values()) max = Math.max(max, value == null ? 0L : value);
        }
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            String key = day.toString();
            long count = raw == null || raw.get(key) == null ? 0L : raw.get(key);
            Metric metric = new Metric();
            metric.setLabel(key.substring(5));
            metric.setCount(count);
            metric.setHeight((int) Math.round(BAR_MAX_HEIGHT * count * 1.0 / max));
            metric.setColor(TREND_COLOR);
            metrics.add(metric);
        }
        return metrics;
    }

    private void scaleHeights(List<Metric> metrics, int maxHeight) {
        long max = 1;
        for (Metric metric : metrics) max = Math.max(max, metric.getCount());
        for (Metric metric : metrics) {
            metric.setHeight((int) Math.round(maxHeight * metric.getCount() * 1.0 / max));
            metric.setWidth((int) Math.round(STACK_WIDTH * metric.getCount() * 1.0 / max));
        }
    }

    private void applyDonut(List<Metric> metrics) {
        double circumference = 2 * Math.PI * DONUT_RADIUS;
        double used = 0;
        for (Metric metric : metrics) {
            double length = circumference * metric.getPercent() / 100.0;
            metric.setDash(String.format(Locale.US, "%.2f %.2f", length, Math.max(circumference - length, 0)));
            metric.setOffset((int) Math.round(-used));
            used += length;
        }
    }

    private void applyStack(List<Metric> metrics, int totalWidth) {
        int x = 0;
        for (Metric metric : metrics) {
            int width = (int) Math.round(totalWidth * metric.getPercent() / 100.0);
            metric.setWidth(width);
            metric.setOffset(x);
            x += width;
        }
    }

    private static String pick(Map<String, String> source, String key, String fallback) {
        if (source == null) return fallback;
        String value = source.get(key);
        return value == null ? fallback : value;
    }

    private static Map<String, String> labels(String[] keys, String[] values) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keys.length && i < values.length; i++) map.put(keys[i], values[i]);
        return map;
    }
}
