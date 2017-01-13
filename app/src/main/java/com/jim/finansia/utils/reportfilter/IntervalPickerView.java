package com.jim.finansia.utils.reportfilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.R;
import com.lantouzi.wheelview.WheelView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IntervalPickerView extends LinearLayout {
    private CircleReportFilterView crfvFilterCenterCirlce;
    private TextView tvIntervalPickerFrom, tvIntervalPickerTo, ivDatePickCancel, ivDatePickOk;
    private WheelView wpIntervalPickerFrom, wpIntervalPickerTo;
    private SimpleDateFormat format = new SimpleDateFormat("dd LLL, yyyy");
    private Calendar begin, end;
    private List<String> fromDays, toDays;
    private IntervalPickListener listener;
    public IntervalPickerView(Context context) {
        super(context);
        init(context);
    }

    public IntervalPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IntervalPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public IntervalPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.interval_picker_view, this, true);
        tvIntervalPickerFrom = (TextView) findViewById(R.id.tvIntervalPickerFrom);
        tvIntervalPickerTo = (TextView) findViewById(R.id.tvIntervalPickerTo);
        wpIntervalPickerFrom = (WheelView) findViewById(R.id.wpIntervalPickerFrom);
        wpIntervalPickerTo = (WheelView) findViewById(R.id.wpIntervalPickerTo);
        crfvFilterCenterCirlce = (CircleReportFilterView) findViewById(R.id.crfvFilterCenterCirlce);
        crfvFilterCenterCirlce.setListener(new CircleReportFilterView.IntervlCircleTickListener() {
            @Override
            public void onIntervalCircleTick(Calendar begin, Calendar end, boolean dayPickMode) {
                tvIntervalPickerFrom.setText(getContext().getString(R.string.c_interval)+": "+format.format(begin.getTime()));
                tvIntervalPickerTo.setText(getContext().getString(R.string.do_interval)+": "+format.format(end.getTime()));
                if (IntervalPickerView.this.begin.compareTo(begin) != 0) {
                    int begMax = begin.getActualMaximum(Calendar.DAY_OF_MONTH);
                    fromDays = new ArrayList<>();
                    for (int i = 0; i < begMax; i++)
                        fromDays.add(String.valueOf(i + 1));
                    wpIntervalPickerFrom.setItems(fromDays);
                    wpIntervalPickerFrom.setMaxSelectableIndex(begMax - 1);
                }
                if (IntervalPickerView.this.end.compareTo(end) != 0) {
                    int endMax = end.getActualMaximum(Calendar.DAY_OF_MONTH);
                    toDays = new ArrayList<>();
                    for (int i = 0; i < endMax; i++)
                        toDays.add(String.valueOf(i + 1));
                    wpIntervalPickerTo.setItems(toDays);
                    wpIntervalPickerTo.setMaxSelectableIndex(endMax - 1);
                }
                if (!dayPickMode) {
                    Log.d("sss", "onIntervalCircleTick: entered");
                    wpIntervalPickerFrom.selectIndex(begin.get(Calendar.DAY_OF_MONTH) - 1);
                    wpIntervalPickerTo.selectIndex(end.get(Calendar.DAY_OF_MONTH) - 1);
                }
                IntervalPickerView.this.begin = (Calendar) begin.clone();
                IntervalPickerView.this.end = (Calendar) end.clone();
                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(20);
            }
        });
        begin = (Calendar) crfvFilterCenterCirlce.getBegin().clone();
        end = (Calendar) crfvFilterCenterCirlce.getEnd().clone();
        tvIntervalPickerFrom.setText(getContext().getString(R.string.c_interval)+": "+format.format(begin.getTime()));
        tvIntervalPickerTo.setText(getContext().getString(R.string.do_interval)+": "+format.format(end.getTime()));

        final int begMax = begin.getActualMaximum(Calendar.DAY_OF_MONTH);
        fromDays = new ArrayList<>();
        for (int i = 0; i < begMax; i++)
            fromDays.add(String.valueOf(i+1));
        wpIntervalPickerFrom.setItems(fromDays);
        wpIntervalPickerFrom.selectIndex(0);
        wpIntervalPickerFrom.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onWheelItemChanged(WheelView wheelView, int position) {
                crfvFilterCenterCirlce.setDay(position, CircleReportFilterView.BEGIN);
            }

            @Override
            public void onWheelItemSelected(WheelView wheelView, int position) {
                crfvFilterCenterCirlce.setDay(position, CircleReportFilterView.BEGIN);
            }
        });

        int endMax = end.getActualMaximum(Calendar.DAY_OF_MONTH);
        toDays = new ArrayList<>();
        for (int i = 0; i < endMax; i++)
            toDays.add(String.valueOf(i+1));
        wpIntervalPickerTo.setItems(toDays);
        wpIntervalPickerTo .selectIndex(0);
        wpIntervalPickerTo.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onWheelItemChanged(WheelView wheelView, int position) {
                Log.d("sss", "onWheelItemChanged: " + position);
                crfvFilterCenterCirlce.setDay(position, CircleReportFilterView.END);
            }

            @Override
            public void onWheelItemSelected(WheelView wheelView, int position) {
                Log.d("sss", "onWheelItemSelected: " + position);
                crfvFilterCenterCirlce.setDay(position, CircleReportFilterView.END);
            }
        });
        ivDatePickCancel = (TextView) findViewById(R.id.ivDatePickCancel);
        ivDatePickOk = (TextView) findViewById(R.id.ivDatePickOk);
        ivDatePickCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onCancelPick();
            }
        });
        ivDatePickOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onIntervalPick(begin, end);
            }
        });
    }

    public void setListener(IntervalPickListener listener) {
        this.listener = listener;
        if (this.listener != null)
            listener.onIntervalPick(begin, end);
    }

    public void saveState() {
        crfvFilterCenterCirlce.saveState();
    }

    public interface IntervalPickListener {
        public void onIntervalPick(Calendar begin, Calendar end);
        public void onCancelPick();
    }

}
