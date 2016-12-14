package com.jim.pocketaccounter.utils.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jim.pocketaccounter.R;

import java.util.Calendar;

public class IntervalPickerFilterView extends LinearLayout {
    public IntervalPickerFilterView(Context context) {
        super(context);
        init(context);
    }

    public IntervalPickerFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IntervalPickerFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public IntervalPickerFilterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {

    }

    //one month view
    public class OneMonthView extends LinearLayout {
        private RecyclerView rvOneMonthDays;
        private TextView tvOneMonthTitle;
        private Calendar calendar;
        public OneMonthView(Context context) {
            super(context);
            init(context);
        }

        public OneMonthView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        public OneMonthView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context);
        }

        @SuppressLint("NewApi")
        public OneMonthView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            init(context);
        }
        void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.one_month_view, this, true);
            rvOneMonthDays = (RecyclerView) findViewById(R.id.rvOneMonthDays);
            tvOneMonthTitle = (TextView) findViewById(R.id.tvOneMonthTitle);
        }
        public void setMonth(Calendar calendar) {
            this.calendar = (Calendar) calendar.clone();
        }
    }

    //interval picker cell checked and unchecked
    public class IntervalPickerCell extends RelativeLayout {
        private RelativeLayout rlCurrentDateBg, rlIntervalCheckerCheckedBg;
        private TextView tvIntervalPickerDate, tvIntervalPickerWeekday;
        private int checkedColor = Color.parseColor("#0A10A1");
        private int uncheckedColor = Color.parseColor("#00000000");
        private boolean isCurrentDay = false;
        private boolean isChecked = false;
        public IntervalPickerCell(Context context) {
            super(context);
            init(context);
        }
        public IntervalPickerCell(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }
        public IntervalPickerCell(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context);
        }
        @SuppressLint("NewApi")
        public IntervalPickerCell(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            init(context);
        }
        void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.interval_picker_cell, this, true);
            rlCurrentDateBg = (RelativeLayout) findViewById(R.id.rlCurrentDateBg);
            rlIntervalCheckerCheckedBg = (RelativeLayout) findViewById(R.id.rlIntervalCheckerCheckedBg);
            tvIntervalPickerDate = (TextView) findViewById(R.id.tvIntervalPickerDate);
            tvIntervalPickerWeekday = (TextView) findViewById(R.id.tvIntervalPickerWeekday);
        }
        public void currentDay(boolean isCurrentDay) {
            this.isCurrentDay = isCurrentDay;
            if (isCurrentDay) {
                tvIntervalPickerDate.setTextColor(Color.WHITE);
                rlCurrentDateBg.setBackgroundResource(R.drawable.interval_picker_current_day_circle);
            } else {
                tvIntervalPickerDate.setTextColor(Color.parseColor("414141"));
                rlCurrentDateBg.setBackground(null);
            }
        }
        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
            if (isChecked)
                rlIntervalCheckerCheckedBg.setBackgroundColor(checkedColor);
            else
                rlIntervalCheckerCheckedBg.setBackgroundColor(uncheckedColor);
        }
        public boolean isCurrentDay() {
            return isCurrentDay;
        }
        public boolean isChecked() {
            return isChecked;
        }
    }
    //interface, to handling interval picker
    public interface IntervalPickedListener {
        public void onIntervalPicked(Calendar begin, Calendar end);
    }
}
