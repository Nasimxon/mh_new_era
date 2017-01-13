package com.jim.finansia.utils.reportviews;

public class ArcShapeWithRotationAngle {
    private float startAngle, sweepAngle;
    private int type = ReportPieView.NORMAL;
    private float percent = 0.0f;
    public float getPercent() { return percent; }
    public void setPercent(float percent) { this.percent = percent; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public float getStartAngle() { return startAngle; }
    public void setStartAngle(float startAngle) { this.startAngle = startAngle; }
    public float getSweepAngle() { return sweepAngle; }
    public void setSweepAngle(float sweepAngle) { this.sweepAngle = sweepAngle; }
}
