package com.jim.finansia.utils.reportviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.jim.finansia.R;
import com.jim.finansia.utils.GetterAttributColors;
import java.util.List;

public class TableView extends RelativeLayout {
    private RecyclerView rvTableView;
    private List<TableViewData> datas;
    private OnTableRowClickListener listener;
    public static final int BY_DATE = 0, BY_INCOME = 1, BY_EXPENSE = 2;
    private int sortingType = BY_DATE;
    private TableAdapter adapter;
    private boolean orderAsc = true;
    public TableView(Context context) {
        super(context);
        init(context);
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public TableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.table_view, this, true);
        rvTableView = (RecyclerView) findViewById(R.id.rvTableView);
        rvTableView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void setDatas(List<TableViewData> datas) {
        this.datas = datas;
        adapter = new TableAdapter(this.datas);
        rvTableView.setAdapter(adapter);
    }

    public void setSortingType(int sortingType) {
        this.sortingType = sortingType;
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    public void setListener(OnTableRowClickListener listener) {
        this.listener = listener;
    }

    private class TableAdapter extends RecyclerView.Adapter<TableView.ViewHolder> {
        private List<TableViewData> result;
        public TableAdapter(List<TableViewData> result) {
            this.result = result;
        }

        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final TableView.ViewHolder view, final int position) {
            if (position == 0) {
                view.tvFirstColumn.setText(getResources().getString(R.string.date));
                RelativeLayout.LayoutParams firstLp = (LayoutParams) view.tvFirstColumn.getLayoutParams();
                firstLp.addRule(RelativeLayout.CENTER_VERTICAL);
                firstLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                view.tvFirstColumn.setLayoutParams(firstLp);
                view.rlSecondContainer.setBackgroundColor(Color.parseColor("#EAF7E6"));
                view.tvSecondColumn.setText(getResources().getString(R.string.income));
                RelativeLayout.LayoutParams secondLp = (LayoutParams) view.tvSecondColumn.getLayoutParams();
                secondLp.addRule(RelativeLayout.CENTER_IN_PARENT);
                view.tvSecondColumn.setLayoutParams(secondLp);
                view.rlThirdContainer.setBackgroundColor(Color.parseColor("#F6E6E6"));
                view.tvThirdColumn.setText(getResources().getString(R.string.expanse));
                RelativeLayout.LayoutParams thirdLp = (LayoutParams) view.tvThirdColumn.getLayoutParams();
                thirdLp.addRule(RelativeLayout.CENTER_IN_PARENT);
                view.tvThirdColumn.setLayoutParams(thirdLp);
                switch (sortingType) {
                    case BY_DATE:
                        view.ivFirstArrow.setVisibility(View.VISIBLE);
                        if (orderAsc)
                            view.ivFirstArrow.setImageResource(R.drawable.sorting_triangle_down);
                        else
                            view.ivFirstArrow.setImageResource(R.drawable.sorting_triangle_up);
                        view.ivSecondArrow.setVisibility(View.GONE);
                        view.ivThirdArrow.setVisibility(View.GONE);
                        break;
                    case BY_INCOME:
                        view.ivFirstArrow.setVisibility(View.GONE);
                        view.ivSecondArrow.setVisibility(View.VISIBLE);
                        if (orderAsc)
                            view.ivSecondArrow.setImageResource(R.drawable.sorting_triangle_down);
                        else
                            view.ivSecondArrow.setImageResource(R.drawable.sorting_triangle_up);
                        view.ivThirdArrow.setVisibility(View.GONE);
                        break;
                    case BY_EXPENSE:
                        view.ivFirstArrow.setVisibility(View.GONE);
                        view.ivSecondArrow.setVisibility(View.GONE);
                        view.ivThirdArrow.setVisibility(View.VISIBLE);
                        if (orderAsc)
                            view.ivThirdArrow.setImageResource(R.drawable.sorting_triangle_down);
                        else
                            view.ivThirdArrow.setImageResource(R.drawable.sorting_triangle_up);
                        break;
                }
                view.rlFirstContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) listener.onTableHeadClick(0);
                    }
                });
                view.rlSecondContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) listener.onTableHeadClick(1);
                    }
                });
                view.rlThirdContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) listener.onTableHeadClick(2);
                    }
                });
            } else if (position == result.size() - 1) {
                view.ivFirstArrow.setVisibility(View.GONE);
                view.ivSecondArrow.setVisibility(View.GONE);
                view.ivThirdArrow.setVisibility(View.GONE);

                int color = ColorUtils.setAlphaComponent(GetterAttributColors.fetchHeadColor(getContext()),50);
                view.rlFirstContainer.setBackgroundColor(color);
                view.rlSecondContainer.setBackgroundColor(color);
                view.rlThirdContainer.setBackgroundColor(color);
                view.tvFirstColumn.setText(getResources().getString(R.string.total));
                RelativeLayout.LayoutParams firstLp = (LayoutParams) view.tvFirstColumn.getLayoutParams();
                firstLp.addRule(RelativeLayout.CENTER_VERTICAL);
                firstLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                view.tvFirstColumn.setLayoutParams(firstLp);
                view.tvSecondColumn.setText(result.get(position).getIncome());
                RelativeLayout.LayoutParams secondLp = (LayoutParams) view.tvSecondColumn.getLayoutParams();
                secondLp.addRule(RelativeLayout.CENTER_VERTICAL);
                secondLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                view.tvSecondColumn.setLayoutParams(secondLp);
                view.tvThirdColumn.setText(result.get(position).getExpense());
                RelativeLayout.LayoutParams thirdLp = (LayoutParams) view.tvThirdColumn.getLayoutParams();
                thirdLp.addRule(RelativeLayout.CENTER_VERTICAL);
                thirdLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                view.tvThirdColumn.setLayoutParams(thirdLp);
            }
            else {
                view.ivFirstArrow.setVisibility(View.GONE);
                view.ivSecondArrow.setVisibility(View.GONE);
                view.ivThirdArrow.setVisibility(View.GONE);
                if (position%2 == 0) {
                    view.rlFirstContainer.setBackgroundColor(Color.parseColor("#fcfcfc"));
                    view.rlSecondContainer.setBackgroundColor(Color.parseColor("#fcfcfc"));
                    view.rlThirdContainer.setBackgroundColor(Color.parseColor("#fcfcfc"));
                } else {
                    view.rlFirstContainer.setBackgroundColor(Color.WHITE);
                    view.rlSecondContainer.setBackgroundColor(Color.WHITE);
                    view.rlThirdContainer.setBackgroundColor(Color.WHITE);
                }
                view.tvFirstColumn.setText(result.get(position).getDate());
                RelativeLayout.LayoutParams firstLp = (LayoutParams) view.tvFirstColumn.getLayoutParams();
                firstLp.addRule(RelativeLayout.CENTER_VERTICAL);
                firstLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                view.tvFirstColumn.setLayoutParams(firstLp);
                view.tvSecondColumn.setText(result.get(position).getIncome());
                RelativeLayout.LayoutParams secondLp = (LayoutParams) view.tvSecondColumn.getLayoutParams();
                secondLp.addRule(RelativeLayout.CENTER_VERTICAL);
                secondLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                view.tvSecondColumn.setLayoutParams(secondLp);
                view.tvThirdColumn.setText(result.get(position).getExpense());
                RelativeLayout.LayoutParams thirdLp = (LayoutParams) view.tvThirdColumn.getLayoutParams();
                thirdLp.addRule(RelativeLayout.CENTER_VERTICAL);
                thirdLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                view.tvThirdColumn.setLayoutParams(thirdLp);
                view.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null)
                            listener.onTableRowClick(position-1);
                    }
                });
            }
        }

        public TableView.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_view_item, parent, false);
            return new TableView.ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFirstColumn, tvSecondColumn, tvThirdColumn;
        View view;
        ImageView ivFirstArrow, ivSecondArrow, ivThirdArrow;
        RelativeLayout rlFirstContainer, rlSecondContainer, rlThirdContainer;
        public ViewHolder(View view) {
            super(view);
            tvFirstColumn = (TextView) view.findViewById(R.id.tvFirstColumn);
            tvSecondColumn = (TextView) view.findViewById(R.id.tvSecondColumn);
            tvThirdColumn = (TextView) view.findViewById(R.id.tvThirdColumn);
            ivFirstArrow = (ImageView) view.findViewById(R.id.ivFirstArrow);
            ivSecondArrow = (ImageView) view.findViewById(R.id.ivSecondArrow);
            ivThirdArrow = (ImageView) view.findViewById(R.id.ivThirdArrow);
            rlFirstContainer = (RelativeLayout) view.findViewById(R.id.rlFirstContainer);
            rlSecondContainer = (RelativeLayout) view.findViewById(R.id.rlSecondContainer);
            rlThirdContainer = (RelativeLayout) view.findViewById(R.id.rlThirdContainer);
            this.view = view;
        }
    }

    public void setOrderAsc(boolean orderAsc) {
        this.orderAsc = orderAsc;
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    public static class TableViewData {
        String date, income, expense;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getIncome() { return income; }
        public void setIncome(String income) { this.income = income; }
        public String getExpense() { return expense; }
        public void setExpense(String expense) { this.expense = expense; }
    }

    public interface OnTableRowClickListener {
        public void onTableHeadClick(int column);
        public void onTableRowClick(int position);
    }
}
