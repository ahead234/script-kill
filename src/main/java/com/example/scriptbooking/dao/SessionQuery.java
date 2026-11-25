package com.example.scriptbooking.dao;

import java.time.LocalDate;

/**
 * 场次列表的筛选条件。
 *
 * 重构前 findPage 用 (date, keyword, status, page, pageSize) 五个位置参数，
 * SQL 里 9 个占位符靠 setInt(8)、setInt(9) 手工对号，加一个条件就要重排全部下标，
 * 调用方也容易把 keyword 与 status 传反。抽成对象后：筛选条件与分页参数彻底分开，
 * 占位符下标由 bindFilter 逐个推进，不再出现魔法数字。
 */
public class SessionQuery {
    private final LocalDate date;
    private final String keyword;
    private final String status;

    public SessionQuery(LocalDate date, String keyword, String status) {
        this.date = date;
        this.keyword = trimToEmpty(keyword);
        this.status = trimToEmpty(status);
    }

    /** 无条件查询（对照页与默认列表使用）。 */
    public static SessionQuery none() {
        return new SessionQuery(null, "", "");
    }

    public LocalDate getDate() {
        return date;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getStatus() {
        return status;
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
