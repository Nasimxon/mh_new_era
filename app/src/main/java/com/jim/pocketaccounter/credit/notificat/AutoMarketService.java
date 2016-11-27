package com.jim.pocketaccounter.credit.notificat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.AutoMarket;
import com.jim.pocketaccounter.database.AutoMarketDao;
import com.jim.pocketaccounter.database.DaoMaster;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DatabaseMigration;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import org.greenrobot.greendao.database.Database;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by root on 9/20/16.
 */
public class AutoMarketService extends Service {
    private DaoSession daoSession;
    private FinanceRecordDao financeRecordDao;
    private AutoMarketDao autoMarketDao;

    private final int HAVE_SUCH_CATEGORY_RECORD = 0;
    private final int SAVE_CATEGORY_RECORD = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, PocketAccounterGeneral.CURRENT_DB_NAME);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        autoMarketDao = daoSession.getAutoMarketDao();
        financeRecordDao = daoSession.getFinanceRecordDao();
        Calendar currentDay = Calendar.getInstance();

        for (AutoMarket au : autoMarketDao.loadAll()) {
            String[] days = au.getDates().split(",");
            for (String day : days) {
//                if (day.equals("" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) ||
//                        day.equals("" + getResources().getStringArray(R.array.week_day_auto)[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)])) {
//                    chekAutoMarket(au);
//                }
                if (au.getType()) {
                    // sanalar uchun
                    int dayInt = Integer.parseInt(day);
                    currentDay.set(Calendar.YEAR, currentDay.get(Calendar.YEAR));
                    currentDay.set(Calendar.MONTH, currentDay.get(Calendar.MONTH));
                    currentDay.set(Calendar.DAY_OF_MONTH, dayInt);
                    if (dayInt > currentDay.get(Calendar.DAY_OF_MONTH)) {
                        currentDay.add(Calendar.MONTH, -1);
                    }
                    currentDay.set(Calendar.HOUR_OF_DAY, 0);
                    currentDay.set(Calendar.MINUTE, 0);
                    currentDay.set(Calendar.SECOND, 0);
                    currentDay.set(Calendar.MILLISECOND, 0);

                    while (currentDay.compareTo(au.getCreateDay()) >= 0) {
                        FinanceRecord financeRecord = new FinanceRecord();
                        financeRecord.setRecordId("auto" + UUID.randomUUID().toString());
                        financeRecord.setCategory(au.getRootCategory());
                        financeRecord.setSubCategory(au.getSubCategory());
                        financeRecord.setCurrency(au.getCurrency());
                        financeRecord.setAccount(au.getAccount());
                        financeRecord.setAmount(au.getAmount());
                        financeRecord.setDate(currentDay);
                        boolean tek = false;
                        for (FinanceRecord fn : daoSession.getFinanceRecordDao().loadAll()) {
                            if (fn.getDate().compareTo(financeRecord.getDate()) == 0 && fn.getRecordId().startsWith("auto")
                                    && fn.getCategory().getId().matches(financeRecord.getCategory().getId())
                                    && fn.getSubCategory().getId().matches(financeRecord.getSubCategory().getId())) {
                                tek = true;
                                break;
                            } else if (au.getType() && day.matches("" + getResources().getStringArray(R.array.week_day_auto)[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)])) {
                                tek = true;
                            }
                        }
                        if (!tek)
                            daoSession.getFinanceRecordDao().insertOrReplace(financeRecord);
                        currentDay.add(Calendar.MONTH, -1);
                    }
                } else {
                    // hafta kunlari uchun
                    String[] weekDays = getResources().getStringArray(R.array.week_day_auto);
                    if (!day.equals(weekDays[currentDay.get(Calendar.DAY_OF_WEEK)])) {
                        int pos = 1;
                        for (int i = 0; i < weekDays.length; i++) {
                            if (weekDays[i].equals(day)) {
                                pos = i + 1;
                                break;
                            }
                        }
                        currentDay.set(Calendar.YEAR, currentDay.get(Calendar.YEAR));
                        currentDay.set(Calendar.WEEK_OF_YEAR, currentDay.get(Calendar.WEEK_OF_YEAR));
                        currentDay.set(Calendar.DAY_OF_WEEK, pos);
                    }
                    while (currentDay.compareTo(au.getCreateDay()) >= 0) {
                        FinanceRecord financeRecord = new FinanceRecord();
                        financeRecord.setRecordId("auto" + UUID.randomUUID().toString());
                        financeRecord.setCategory(au.getRootCategory());
                        financeRecord.setSubCategory(au.getSubCategory());
                        financeRecord.setCurrency(au.getCurrency());
                        financeRecord.setAccount(au.getAccount());
                        financeRecord.setAmount(au.getAmount());
                        financeRecord.setDate(currentDay);
                        boolean tek = false;
                        for (FinanceRecord fn : daoSession.getFinanceRecordDao().loadAll()) {
                            if (fn.getDate().compareTo(financeRecord.getDate()) == 0 && fn.getRecordId().startsWith("auto")
                                    && fn.getCategory().getId().matches(financeRecord.getCategory().getId())
                                    && fn.getSubCategory().getId().matches(financeRecord.getSubCategory().getId())) {
                                tek = true;
                                break;
                            } else if (au.getType() && day.matches("" + getResources().getStringArray(R.array.week_day_auto)[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)])) {
                                tek = true;
                            }
                        }
                        if (!tek)
                            daoSession.getFinanceRecordDao().insertOrReplace(financeRecord);
                        currentDay.add(Calendar.WEEK_OF_MONTH, -1);
                    }
                }
                currentDay = Calendar.getInstance();
            }
        }
        db.close();
        return super.onStartCommand(intent, flags, startId);
    }

    private void chekAutoMarket(AutoMarket au) {
        FinanceRecord financeRecord = new FinanceRecord();
        financeRecord.setRecordId("auto" + UUID.randomUUID().toString());
        financeRecord.setCategory(au.getRootCategory());
        financeRecord.setSubCategory(au.getSubCategory());
        financeRecord.setCurrency(au.getCurrency());
        financeRecord.setAccount(au.getAccount());
        financeRecord.setAmount(au.getAmount());
        financeRecord.setDate(Calendar.getInstance());
        switch (insertFinanceRecord(financeRecord)) {
            case HAVE_SUCH_CATEGORY_RECORD: {
                Log.d("sss", "have to Category");
                break;
            }
            case SAVE_CATEGORY_RECORD: {
                Log.d("sss", "saved finance record");
                break;
            }
        }
    }

    private int insertFinanceRecord(FinanceRecord financeRecord) {
        for (FinanceRecord fn : financeRecordDao.loadAll()) {
            if (fn.getDate().compareTo(financeRecord.getDate()) == 0 && fn.getRecordId().startsWith("auto")
                    && fn.getCategory().getId().matches(financeRecord.getCategory().getId()) &&
                    fn.getSubCategory().getId().matches(financeRecord.getSubCategory().getId())) {
                return HAVE_SUCH_CATEGORY_RECORD;
            }
        }
        financeRecordDao.insertOrReplace(financeRecord);
        return SAVE_CATEGORY_RECORD;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
