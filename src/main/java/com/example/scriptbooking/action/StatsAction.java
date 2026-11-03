package com.example.scriptbooking.action;

import com.example.scriptbooking.model.Metric;
import com.example.scriptbooking.service.AdminService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 管理后台报表数据接口。通过 Struts 的 stream 结果输出 JSON，供报表页图表读取。
 */
public class StatsAction extends BaseAction {
    private final AdminService adminService = new AdminService();

    private String from;
    private String to;
    private InputStream jsonStream;

    @Override
    public String execute() {
        Map<String, Object> stats = adminService.dashboard(parseDate(from), parseDate(to));
        jsonStream = new ByteArrayInputStream(buildJson(stats).getBytes(StandardCharsets.UTF_8));
        response().setHeader("Cache-Control", "no-store");
        return SUCCESS;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String buildJson(Map<String, Object> stats) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"summary\":{");
        number(json, "openSessions", stats.get("openSessions"), false);
        number(json, "confirmedBookings", stats.get("confirmedBookings"), false);
        number(json, "checkedInBookings", stats.get("checkedInBookings"), false);
        number(json, "pendingReviews", stats.get("pendingReviews"), false);
        number(json, "totalBookings", stats.get("totalBookings"), false);
        number(json, "scriptCount", stats.get("scriptCount"), true);
        json.append("},");
        metrics(json, "trend", stats.get("trend"), false);
        metrics(json, "bookingStatus", stats.get("bookingStatus"), false);
        metrics(json, "scriptDemand", stats.get("scriptDemand"), false);
        metrics(json, "reviewStatus", stats.get("reviewStatus"), false);
        metrics(json, "sessionStatus", stats.get("sessionStatus"), true);
        json.append("}");
        return json.toString();
    }

    private void number(StringBuilder json, String key, Object value, boolean last) {
        long number = value instanceof Number ? ((Number) value).longValue() : 0L;
        json.append('"').append(key).append("\":").append(number);
        if (!last) json.append(',');
    }

    @SuppressWarnings("unchecked")
    private void metrics(StringBuilder json, String key, Object value, boolean last) {
        List<Metric> items = value instanceof List ? (List<Metric>) value : Collections.<Metric>emptyList();
        json.append('"').append(key).append("\":[");
        for (int i = 0; i < items.size(); i++) {
            Metric metric = items.get(i);
            json.append("{\"label\":\"").append(escape(metric.getLabel())).append('"')
                    .append(",\"count\":").append(metric.getCount())
                    .append(",\"percent\":").append(metric.getPercent())
                    .append(",\"color\":\"").append(escape(metric.getColor())).append("\"}");
            if (i < items.size() - 1) json.append(',');
        }
        json.append(']');
        if (!last) json.append(',');
    }

    private String escape(String value) {
        if (value == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (c == '\n') {
                out.append("\\n");
            } else if (c == '\r') {
                out.append("\\r");
            } else if (c < 0x20) {
                out.append(String.format("\\u%04x", (int) c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public InputStream getJsonStream() {
        return jsonStream;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
