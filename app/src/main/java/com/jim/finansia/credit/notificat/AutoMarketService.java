package com.jim.finansia.credit.notificat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jim.finansia.database.AutoMarket;
import com.jim.finansia.database.AutoMarketDao;
import com.jim.finansia.database.DaoMaster;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.utils.PocketAccounterGeneral;

import org.greenrobot.greendao.database.Database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
        List<FinanceRecord> financeRecords = daoSession.queryBuilder(FinanceRecord.class).where(FinanceRecordDao.Properties.RecordId.like("auto%")).list();
        Collections.sort(financeRecords, new Comparator<FinanceRecord>() {
            @Override
            public int compare(FinanceRecord financeRecord, FinanceRecord t1) {
                return t1.getDate().compareTo(financeRecord.getDate());
            }
        });
        Calendar lastRecord = Calendar.getInstance();
        if(!financeRecords.isEmpty()){
            lastRecord = (Calendar) financeRecords.get(0).getDate().clone();
            lastRecord.add(Calendar.DAY_OF_MONTH, 1);
        }
        lastRecord.set(Calendar.HOUR_OF_DAY,0);
        lastRecord.set(Calendar.MINUTE,0);
        lastRecord.set(Calendar.SECOND,0);
        lastRecord.set(Calendar.MILLISECOND,0);
        List<AutoMarket> allAutoOperations  = autoMarketDao.loadAll();
        while (lastRecord.compareTo(currentDay) <= 0) {
            for (AutoMarket autoMarket : allAutoOperations) {
                if (autoMarket.getType()) {
                    String[] days = autoMarket.getDates().split(",");
                    for (String day : days) {
                        int d = Integer.parseInt(day);
                        if (d == lastRecord.get(Calendar.DAY_OF_MONTH)) {
                            FinanceRecord financeRecord = new FinanceRecord();
                            financeRecord.setRecordId("auto" + UUID.randomUUID().toString());
                            financeRecord.setCategory(autoMarket.getRootCategory());
                            financeRecord.setSubCategory(autoMarket.getSubCategory());
                            financeRecord.setCurrency(autoMarket.getCurrency());
                            financeRecord.setAccount(autoMarket.getAccount());
                            financeRecord.setAmount(autoMarket.getAmount());
                            financeRecord.setDate(lastRecord);
                            daoSession.insertOrReplace(financeRecord);
                        }
                    }
                }
                else {
                    String dayString [] = autoMarket.getPosDays().split(",");
                    int positionDays [] = new int[dayString.length];
                    for (int i = 0; i < dayString.length; i++) {
                        positionDays[i] = Integer.parseInt(dayString[i])+2;
                        if(positionDays[i]==8){
                            positionDays[i]=1;
                        }
                    }
                    for (int i : positionDays) {
                        if (lastRecord.get(Calendar.DAY_OF_WEEK) == i) {
                            FinanceRecord financeRecord = new FinanceRecord();
                            financeRecord.setRecordId("auto" + UUID.randomUUID().toString());
                            financeRecord.setCategory(autoMarket.getRootCategory());
                            financeRecord.setSubCategory(autoMarket.getSubCategory());
                            financeRecord.setCurrency(autoMarket.getCurrency());
                            financeRecord.setAccount(autoMarket.getAccount());
                            financeRecord.setAmount(autoMarket.getAmount());
                            financeRecord.setDate(lastRecord);
                            daoSession.insertOrReplace(financeRecord);
                        }
                    }
                }
                lastRecord.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        db.close();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
