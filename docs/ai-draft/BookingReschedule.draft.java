package com.example.scriptbooking.service;

import com.example.scriptbooking.util.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 助手生成的"预约改期"初稿（原样留档，未参与编译）。
 * 保留它的目的是让审查有实物依据：下面 5 处问题都是真实存在的，不是事后补写的。
 */
public class BookingRescheduleDraft {

    /**
     * 把预约改到新场次。
     * AI 原话："直接把 booking.session_id 改掉即可，players 看到的时间自然就变了。"
     */
    public Map<String, Object> reschedule(long bookingId, long targetSessionId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();

            // 问题 1：SQL 由字符串拼接，targetSessionId 直接进语句
            String sql = "UPDATE booking SET session_id=" + targetSessionId
                    + " WHERE id=" + bookingId;
            int rows = stmt.executeUpdate(sql);

            // 问题 2：只改预约表，没有把原场次的 booked_count 减回去、新场次的加上去
            // 问题 3：整段没有开启事务，两条语句之间失败会留下不一致数据
            result.put("success", rows > 0);
            result.put("message", "改期成功");
            stmt.close();
            conn.close();
        } catch (Exception e) {
            // 问题 4：异常只打印堆栈，前端拿不到可读提示，日志也没有上下文
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }
        return result;
    }

    /**
     * AI 建议的"权限判断"：先按 id 查预约，再比较 userId。
     * 但它把这段判断写在了 JSP 里（见 rooms.draft.jsp 同类写法），
     * 并且这里的方法签名根本没有传入当前登录用户。
     */
    public boolean canReschedule(long bookingId, long userId) {
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT user_id, status FROM booking WHERE id=" + bookingId);
            if (rs.next()) {
                // 问题 5：没有判断 status，也没有判断"是否已改期过""距开演是否不足 2 小时"
                return rs.getLong("user_id") == userId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
