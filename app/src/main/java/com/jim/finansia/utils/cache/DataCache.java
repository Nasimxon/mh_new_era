package com.jim.finansia.utils.cache;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.util.LruCache;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.utils.PocketAccounterGeneral;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

public class DataCache {
    private CategoryEditFragmentDatas categoryEditFragmentDatas;
    private LruCache<Long, Bitmap> boardBitmaps;
    private LruCache<Integer, Bitmap> elements;
    private LruCache<String, List<BoardButtonPercent>> percents;
    private Calendar beginDate, endDate;
    private SimpleDateFormat simpleDateFormat;
    @Inject SharedPreferences sharedPreferences;
    @Inject DaoSession daoSession;
    @Inject CommonOperations commonOperations;
    @Inject ReportManager reportManager;
    @Named(value = "begin") @Inject Calendar begin;
    @Named(value = "end") @Inject Calendar end;
    public DataCache(PocketAccounterApplication application) {
        application.component().inject(this);
        simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        initBeginAndEndDates();
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 16;
        percents = new LruCache<>(cacheSize);
        updatePercentsWhenSwiping();
        categoryEditFragmentDatas = new CategoryEditFragmentDatas();
        elements = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
                    return bitmap.getByteCount() / 1024;
                else
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
        boardBitmaps = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
                    return bitmap.getByteCount() / 1024;
                else
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }
    public LruCache<Long, Bitmap> getBoardBitmapsCache() {
        return boardBitmaps;
    }
    public LruCache<Integer, Bitmap> getElements() {
        return elements;
    }

    public LruCache<String, List<BoardButtonPercent>> getPercents() {
        return percents;
    }
    public void clearAllCaches(){
        boardBitmaps.evictAll();
        percents.evictAll();
    }

    public CategoryEditFragmentDatas getCategoryEditFragmentDatas() {
        if (categoryEditFragmentDatas == null)
            categoryEditFragmentDatas = new CategoryEditFragmentDatas();
        return categoryEditFragmentDatas;
    }
    public class CategoryEditFragmentDatas {
        private int mode;
        private Calendar date;
        private int pos;
        private int type;
        public void setPos(int pos) {
            this.pos = pos;
        }
        public int getPos() {
            return pos;
        }
        public int getMode() {
            return mode;
        }
        public void setMode(int mode) {
            this.mode = mode;
        }
        public Calendar getDate() {
            return date;
        }
        public void setDate(Calendar date) {
            this.date = date;
        }
        public void setType(int type) {
            this.type = type;
        }
        public int getType() {
            return type;
        }
    }

    private String format(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH)+"."
                +(calendar.get(Calendar.MONTH)+1)+"."
                +calendar.get(Calendar.YEAR);
    }

    public void updateOneDay(final Calendar day) {
        String date = format(day);
        //defining interval for accumulating data (00:00:00 - 23:59:59)
        begin.setTimeInMillis(day.getTimeInMillis());
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end.setTimeInMillis(day.getTimeInMillis());
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        //all finance records
        List<FinanceRecord> financeRecords = daoSession.getFinanceRecordDao()
                                                        .queryBuilder()
                                                        .where(FinanceRecordDao.Properties.Date.eq(simpleDateFormat.format(begin.getTime())))
                                                        .list();
        //all credits
        List<CreditDetials> tempCredits = daoSession.getCreditDetialsDao().loadAll();
        List<CreditDetials> creditDetialList= new ArrayList<>();
        for (CreditDetials creditDetial : tempCredits) {
            if (!creditDetial.getKey_for_include()) continue;
            for (ReckingCredit reckingCredit : creditDetial.getReckings()) {
                if (reckingCredit.getPayDate().compareTo(begin) >= 0 &&
                        reckingCredit.getPayDate().compareTo(end) <= 0) {
                    creditDetialList.add(creditDetial);
                    break;
                }
            }
        }
        //all debt borrows
        List<DebtBorrow> tempDebtBorrows = daoSession.getDebtBorrowDao().loadAll();
        List<DebtBorrow> debtBorrows = new ArrayList<>();
        for (DebtBorrow debtBorrow : tempDebtBorrows) {
            if (!debtBorrow.getCalculate()) continue;
            if (debtBorrow.getTakenDate().compareTo(begin) >= 0 &&
                    debtBorrow.getTakenDate().compareTo(end) <= 0) {
                debtBorrows.add(debtBorrow);
                continue;
            }
            for (Recking recking : debtBorrow.getReckings()) {
                if (recking.getPayDate().compareTo(begin) >= 0 &&
                        recking.getPayDate().compareTo(end) <= 0) {
                    debtBorrows.add(debtBorrow);
                    break;
                }
            }
        }
        //balance data for given interval(day)
        Map<String, Double> balance = reportManager.calculateBalance(begin, end);
        Double allExpenses = balance.get(PocketAccounterGeneral.EXPENSES);
        Double allIncomes = balance.get(PocketAccounterGeneral.INCOMES);
        //all board buttons
        List<BoardButton> boardButtonList = daoSession.getBoardButtonDao().loadAll();
        BoardButtonPercent boardButtonPercent = null;
        for (int i=0; i<boardButtonList.size(); i++) {
            if (boardButtonList.get(i).getCategoryId() == null ||
                    (allExpenses == 0.0d && allIncomes == 0.0d)) {
                boardButtonPercent = new BoardButtonPercent();
                boardButtonPercent.setPosition(boardButtonList.get(i).getPos());
                boardButtonPercent.setTable(boardButtonList.get(i).getTable());
                boardButtonPercent.setAmount(0.0d);
                if (percents.get(date) == null) {
                    List<BoardButtonPercent> list = new ArrayList<>();
                    list.add(boardButtonPercent);
                    percents.put(date, list);
                }
                else {
                    List<BoardButtonPercent> list = percents.get(date);
                    int pos = 0;
                    boolean found = false;
                    for (int j=0; j<list.size(); j++) {
                        if (list.get(j).getTable() == boardButtonList.get(i).getTable()
                                && list.get(j).getPosition() == boardButtonList.get(i).getPos()) {
                            found = true;
                            pos = j;
                            break;
                        }
                    }
                    if (found)
                        percents.get(date).get(pos).setAmount(0.0d);
                    else
                        percents.get(date).add(boardButtonPercent);

                }
                continue;
            }
            int type = boardButtonList.get(i).getType();
            switch (type) {
                case PocketAccounterGeneral.CATEGORY:
                    Double categoryExpenses = 0.0d, categoryIncomes = 0.0d;
                    for (FinanceRecord record : financeRecords) {
                        if (record.getCategory().getId().equals(boardButtonList.get(i).getCategoryId())) {
                            if (boardButtonList.get(i).getTable() == PocketAccounterGeneral.EXPENSE) {
                                categoryExpenses += commonOperations.getCost(record);
                            }
                            else {
                                categoryIncomes += commonOperations.getCost(record);
                            }
                        }
                    }
                    boardButtonPercent = new BoardButtonPercent();
                    boardButtonPercent.setPosition(boardButtonList.get(i).getPos());
                    boardButtonPercent.setTable(boardButtonList.get(i).getTable());
                    if (boardButtonList.get(i).getTable() == PocketAccounterGeneral.EXPENSE) {
                        if (allExpenses == 0)
                            boardButtonPercent.setAmount(0.0d);
                        else
                            boardButtonPercent.setAmount(categoryExpenses);
                    }
                    else {
                        if (allIncomes == 0)
                            boardButtonPercent.setAmount(0.0d);
                        else
                            boardButtonPercent.setAmount(categoryIncomes);
                    }

                    if (percents.get(date) == null) {
                        List<BoardButtonPercent> list = new ArrayList<>();
                        list.add(boardButtonPercent);
                        percents.put(date, list);
                    }
                    else {
                        List<BoardButtonPercent> list = percents.get(date);
                        int pos = 0;
                        boolean found = false;
                        for (int j=0; j<list.size(); j++) {
                            if (list.get(j).getTable() == boardButtonList.get(i).getTable()
                                    && list.get(j).getPosition() == boardButtonList.get(i).getPos()) {
                                found = true;
                                pos = j;
                                break;
                            }
                        }
                        if (found)
                            percents.get(date).get(pos).setAmount(boardButtonPercent.getAmount());
                        else
                            percents.get(date).add(boardButtonPercent);

                    }
                    break;
                case PocketAccounterGeneral.CREDIT:
                    Double credit = 0.0d;
                    for (CreditDetials creditDetials : creditDetialList) {
                        if (!boardButtonList.get(i).getCategoryId().equals(Long.toString(creditDetials.getMyCredit_id()))) continue;
                        for (ReckingCredit recking : creditDetials.getReckings()) {
                            if (recking.getPayDate().compareTo(begin) >= 0 &&
                                    recking.getPayDate().compareTo(end) <= 0) {
                                credit += commonOperations.getCost(day, creditDetials.getValyute_currency(), recking.getAmount());
                            }
                        }
                    }
                    boardButtonPercent = new BoardButtonPercent();
                    boardButtonPercent.setPosition(boardButtonList.get(i).getPos());
                    boardButtonPercent.setTable(boardButtonList.get(i).getTable());
                    boardButtonPercent.setAmount(credit);
                    if (percents.get(date) == null) {
                        List<BoardButtonPercent> list = new ArrayList<>();
                        list.add(boardButtonPercent);
                        percents.put(date, list);
                    }
                    else {
                        List<BoardButtonPercent> list = percents.get(date);
                        int pos = 0;
                        boolean found = false;
                        for (int j=0; j<list.size(); j++) {
                            if (list.get(j).getTable() == boardButtonList.get(i).getTable()
                                    && list.get(j).getPosition() == boardButtonList.get(i).getPos()) {
                                found = true;
                                pos = j;
                                break;
                            }
                        }
                        if (found)
                            percents.get(date).get(pos).setAmount(boardButtonPercent.getAmount());
                        else
                            percents.get(date).add(boardButtonPercent);

                    }
                    break;
                case PocketAccounterGeneral.DEBT_BORROW:
                    Double debtBorrowExpenses = 0.0d, debtBorrowIncomes = 0.0d;
                    for (DebtBorrow db : debtBorrows) {
                        if (db.getId().equals(boardButtonList.get(i).getCategoryId()) &&
                                db.getTakenDate().compareTo(begin) >= 0 &&
                                    db.getTakenDate().compareTo(end) <= 0) {
                            if (db.getType() == DebtBorrow.BORROW )
                                debtBorrowExpenses += commonOperations.getCost(day, db.getCurrency(), db.getAmount());
                            else
                                debtBorrowIncomes += commonOperations.getCost(day, db.getCurrency(), db.getAmount());
                            for (Recking recking : db.getReckings()) {
                                if (recking.getPayDate().compareTo(begin) >= 0 &&
                                        recking.getPayDate().compareTo(end) <= 0) {
                                    if (db.getType() == DebtBorrow.BORROW)
                                        debtBorrowIncomes += commonOperations.getCost(day, db.getCurrency(), recking.getAmount());
                                    else
                                        debtBorrowExpenses += commonOperations.getCost(day, db.getCurrency(), recking.getAmount());
                                }
                            }

                        }
                    }
                    boardButtonPercent = new BoardButtonPercent();
                    boardButtonPercent.setPosition(boardButtonList.get(i).getPos());
                    boardButtonPercent.setTable(boardButtonList.get(i).getTable());
                    if (boardButtonList.get(i).getTable() == PocketAccounterGeneral.EXPENSE) {
                        if (allExpenses == 0.0d)
                            boardButtonPercent.setAmount(0.0d);
                        else
                            boardButtonPercent.setAmount(debtBorrowExpenses);
                    }
                    else {
                        if (allIncomes ==0)
                            boardButtonPercent.setAmount(0.0d);
                        else
                            boardButtonPercent.setAmount(debtBorrowIncomes);
                    }
                    if (percents.get(date) == null) {
                        List<BoardButtonPercent> list = new ArrayList<>();
                        list.add(boardButtonPercent);
                        percents.put(date, list);
                    }
                    else {
                        List<BoardButtonPercent> list = percents.get(date);
                        int pos = 0;
                        boolean found = false;
                        for (int j=0; j<list.size(); j++) {
                            if (list.get(j).getTable() == boardButtonList.get(i).getTable()
                                    && list.get(j).getPosition() == boardButtonList.get(i).getPos()) {
                                found = true;
                                pos = j;
                                break;
                            }
                        }
                        if (found)
                            percents.get(date).get(pos).setAmount(boardButtonPercent.getAmount());
                        else
                            percents.get(date).add(boardButtonPercent);
                    }
                    break;
            }
        }
    }

    public void updatePercentsWhenSwiping() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Map<String, List<BoardButtonPercent>> snapshot = percents.snapshot();
                begin.setTimeInMillis(endDate.getTimeInMillis());
                begin.set(Calendar.HOUR_OF_DAY, 0);
                begin.set(Calendar.MINUTE, 0);
                begin.set(Calendar.SECOND, 0);
                begin.set(Calendar.MILLISECOND, 0);
                end.setTimeInMillis(endDate.getTimeInMillis());
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                end.set(Calendar.MILLISECOND, 59);
                for (int i=0; i<10; i++) {
                    boolean hasSuchRecord = false;
                    Set<String> keys = snapshot.keySet();
                    for (String key : keys) {
                        if (key.equalsIgnoreCase(format(begin))) {
                            hasSuchRecord = true;
                            break;
                        }
                    }
                    if (!hasSuchRecord) {
                        updateOneDay(end);
                    }
                    if (i >= 5) {
                        if (i == 5) {
                            begin.setTimeInMillis(endDate.getTimeInMillis());
                            begin.set(Calendar.HOUR_OF_DAY, 0);
                            begin.set(Calendar.MINUTE, 0);
                            begin.set(Calendar.SECOND, 0);
                            begin.set(Calendar.MILLISECOND, 0);
                            end.setTimeInMillis(endDate.getTimeInMillis());
                            end.set(Calendar.HOUR_OF_DAY, 23);
                            end.set(Calendar.MINUTE, 59);
                            end.set(Calendar.SECOND, 59);
                            end.set(Calendar.MILLISECOND, 59);
                        }
                        begin.add(Calendar.DAY_OF_MONTH, -1);
                        end.add(Calendar.DAY_OF_MONTH, -1);
                    }
                    else {
                        begin.add(Calendar.DAY_OF_MONTH, 1);
                        end.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }
            }
        }, 500);
    }

    public void updateAllPercents() {
        begin.setTimeInMillis(endDate.getTimeInMillis());
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end.setTimeInMillis(endDate.getTimeInMillis());
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        percents.evictAll();
        for (int i=0; i<10; i++) {
            updateOneDay(end);
            if (i >= 5) {
                if (i == 5) {
                    begin.setTimeInMillis(endDate.getTimeInMillis());
                    begin.set(Calendar.HOUR_OF_DAY, 0);
                    begin.set(Calendar.MINUTE, 0);
                    begin.set(Calendar.SECOND, 0);
                    begin.set(Calendar.MILLISECOND, 0);
                    end.setTimeInMillis(endDate.getTimeInMillis());
                    end.set(Calendar.HOUR_OF_DAY, 23);
                    end.set(Calendar.MINUTE, 59);
                    end.set(Calendar.SECOND, 59);
                    end.set(Calendar.MILLISECOND, 59);
                }
                begin.add(Calendar.DAY_OF_MONTH, -1);
                end.add(Calendar.DAY_OF_MONTH, -1);
            }
            else {
                begin.add(Calendar.DAY_OF_MONTH, 1);
                end.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
    }

    public Double getPercent(Integer table, Calendar day, Integer position) {
        Map<String, List<BoardButtonPercent>> snapshot = percents.snapshot();
        if (snapshot.get(format(day)) != null) {
            List<BoardButtonPercent> boardButtonPercents = snapshot.get(format(day));
            for (BoardButtonPercent boardButtonPercent : boardButtonPercents) {
                if (boardButtonPercent.getTable() == table &&
                        boardButtonPercent.getPosition() == position)
                    return boardButtonPercent.getAmount();
            }
        }
        return 0.0;
    }

    private void initBeginAndEndDates() {
        if (endDate == null)
            endDate = Calendar.getInstance();
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
        endDate.set(Calendar.MILLISECOND, 59);
        if (commonOperations.getFirstDay() == null)
            beginDate = Calendar.getInstance();
        else
            beginDate = (Calendar) commonOperations.getFirstDay().clone();
        beginDate.set(Calendar.HOUR_OF_DAY, 0);
        beginDate.set(Calendar.MINUTE, 0);
        beginDate.set(Calendar.SECOND, 0);
        beginDate.set(Calendar.MILLISECOND, 0);
    }

    public Calendar getBeginDate() {
        return beginDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setBeginDate(Calendar beginDate) {
        this.beginDate.setTimeInMillis(beginDate.getTimeInMillis());
    }

    public void setEndDate(Calendar endDate) {
        this.endDate.setTimeInMillis(endDate.getTimeInMillis());
    }
    class UpdatePercentsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }
    }
}
