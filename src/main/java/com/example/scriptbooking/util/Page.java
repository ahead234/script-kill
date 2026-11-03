package com.example.scriptbooking.util;

import java.util.Collections;
import java.util.List;

public class Page<T> {
    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final long total;

    public Page(List<T> items, int page, int pageSize, long total) {
        this.items = items == null ? Collections.emptyList() : items;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotal() { return total; }
    public int getTotalPages() { return total == 0 ? 1 : (int) Math.ceil(total / (double) pageSize); }
    public boolean isHasPrevious() { return page > 1; }
    public boolean isHasNext() { return page < getTotalPages(); }
}
