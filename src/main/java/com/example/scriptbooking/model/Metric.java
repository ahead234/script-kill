package com.example.scriptbooking.model;

/**
 * 工作台图表使用的单个数据点。
 * 百分比、像素尺寸和环形图 dash 参数都在服务端计算，JSP 只负责渲染，避免页面出现复杂运算。
 */
public class Metric {
    private String label;
    private long count;
    private int percent;
    private int height;
    private int width;
    private int offset;
    private String color;
    private String dash;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDash() {
        return dash;
    }

    public void setDash(String dash) {
        this.dash = dash;
    }
}
