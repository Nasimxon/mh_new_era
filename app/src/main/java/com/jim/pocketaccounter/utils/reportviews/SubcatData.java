package com.jim.pocketaccounter.utils.reportviews;

import java.util.List;

public class SubcatData {
    private float percent;
    private String text;
    private int color;
    private String id;
    private List<Double> amounts;
    private String icon;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    public float getPercent() { return percent; }
    public void setPercent(float percent) { this.percent = percent; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<Double> getAmounts() { return amounts; }
    public void setAmounts(List<Double> amounts) { this.amounts = amounts; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
