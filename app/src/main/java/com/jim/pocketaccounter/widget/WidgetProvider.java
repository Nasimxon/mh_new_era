package com.jim.pocketaccounter.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountOperation;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.DaoMaster;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DatabaseMigration;
import com.jim.pocketaccounter.database.ReckingCredit;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.Recking;
//import com.jim.pocketaccounter.finance.FinanceManager;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.report.ReportObject;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import org.greenrobot.greendao.database.Database;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by DEV on 09.07.2016.
 */

public class WidgetProvider extends AppWidgetProvider {


    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        SharedPreferences sPref;
        sPref = context.getSharedPreferences("infoFirst", MODE_PRIVATE);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences sPref;
        sPref = context.getSharedPreferences("infoFirst", MODE_PRIVATE);
        sPref.edit().putInt(WidgetKeys.SPREF_WIDGET_ID,-1).apply();
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }

    }

    static public void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             int widgetID) {
        SharedPreferences sPref;
        ArrayList<RootCategory> listCategory;
        String butID_1, butID_2, butID_3, butID_4;
        sPref = context.getSharedPreferences("infoFirst", MODE_PRIVATE);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, PocketAccounterGeneral.CURRENT_DB_NAME);
        Database db = helper.getReadableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();


        sPref.edit().putInt(WidgetKeys.SPREF_WIDGET_ID,widgetID).apply();

        butID_1 = sPref.getString(WidgetKeys.BUTTON_1_ID, WidgetKeys.BUTTON_DISABLED);
        butID_2 = sPref.getString(WidgetKeys.BUTTON_2_ID, WidgetKeys.BUTTON_DISABLED);
        butID_3 = sPref.getString(WidgetKeys.BUTTON_3_ID, WidgetKeys.BUTTON_DISABLED);
        butID_4 = sPref.getString(WidgetKeys.BUTTON_4_ID, WidgetKeys.BUTTON_DISABLED);


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        Log.d(WidgetKeys.TAG, "UPDATE");


        // knopka instalizatsiya bloki
        if (!butID_1.matches(WidgetKeys.BUTTON_DISABLED) || !butID_2.matches(WidgetKeys.BUTTON_DISABLED)
                || !butID_3.matches(WidgetKeys.BUTTON_DISABLED) || !butID_4.matches(WidgetKeys.BUTTON_DISABLED))
            for (RootCategory temp : daoSession.getRootCategoryDao().loadAll()) {

                if (!butID_1.matches(WidgetKeys.BUTTON_DISABLED) && temp.getId().matches(butID_1)) {
                    //ustanovka ikonki
                    views.setImageViewResource(R.id.button_1_ramka, R.drawable.shape_for_widget_black);
                    int resId = context.getResources().getIdentifier(temp.getIcon(), "drawable", context.getPackageName());
                    views.setImageViewResource(R.id.button_1_icon, resId);
                    //ustanovka onclika
                    Intent button1 = new Intent(context, CalcActivity.class);
                    button1.putExtra(WidgetKeys.KEY_FOR_INTENT_ID, temp.getId());
                    button1.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
                    PendingIntent.getActivity(context, 1, button1, 0).cancel();
                    PendingIntent pendingIntent_button1 = PendingIntent.getActivity(context, 1, button1, 0);
                    views.setOnClickPendingIntent(R.id.button_1, pendingIntent_button1);

                }

                if (!butID_2.matches(WidgetKeys.BUTTON_DISABLED) && temp.getId().matches(butID_2)) {
                    //ustanovka ikonki
                    views.setImageViewResource(R.id.button_2_ramka, R.drawable.shape_for_widget_black);
                    int resId = context.getResources().getIdentifier(temp.getIcon(), "drawable", context.getPackageName());
                    views.setImageViewResource(R.id.button_2_icon, resId);
                    //ustanovka onclika
                    Intent button2 = new Intent(context, CalcActivity.class);
                    button2.putExtra(WidgetKeys.KEY_FOR_INTENT_ID, temp.getId());
                    button2.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
                    PendingIntent.getActivity(context, 2, button2, 0).cancel();
                    PendingIntent pendingIntent_button2 = PendingIntent.getActivity(context, 2, button2, 0);
                    views.setOnClickPendingIntent(R.id.button_2, pendingIntent_button2);


                }

                if (!butID_3.matches(WidgetKeys.BUTTON_DISABLED) && temp.getId().matches(butID_3)) {
                    //ustanovka ikonki
                    views.setImageViewResource(R.id.button_3_ramka, R.drawable.shape_for_widget_black);
                    int resId = context.getResources().getIdentifier(temp.getIcon(), "drawable", context.getPackageName());
                    views.setImageViewResource(R.id.button_3_icon, resId);
                    //ustanovka onclika
                    Intent button3 = new Intent(context, CalcActivity.class);
                    button3.putExtra(WidgetKeys.KEY_FOR_INTENT_ID, temp.getId());
                    button3.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
                    PendingIntent.getActivity(context, 3, button3, 0).cancel();
                    PendingIntent pendingIntent_button3 = PendingIntent.getActivity(context, 3, button3, 0);
                    views.setOnClickPendingIntent(R.id.button_3, pendingIntent_button3);

                }

                if (!butID_4.matches(WidgetKeys.BUTTON_DISABLED) && temp.getId().matches(butID_4)) {
                    //ustanovka ikonki
                    views.setImageViewResource(R.id.button_4_ramka, R.drawable.shape_for_widget_black);
                    int resId = context.getResources().getIdentifier(temp.getIcon(), "drawable", context.getPackageName());
                    views.setImageViewResource(R.id.button_4_icon, resId);
                    //ustanovka onclika
                    Intent button4 = new Intent(context, CalcActivity.class);
                    button4.putExtra(WidgetKeys.KEY_FOR_INTENT_ID, temp.getId());
                    button4.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
                    PendingIntent.getActivity(context, 4, button4, 0).cancel();
                    PendingIntent pendingIntent_button4 = PendingIntent.getActivity(context, 4, button4, 0);
                    views.setOnClickPendingIntent(R.id.button_4, pendingIntent_button4);

                }

                for (SubCategory subTemp : temp.getSubCategories()) {

                    if (!butID_1.matches(WidgetKeys.BUTTON_DISABLED) && subTemp.getId().matches(butID_1)) {
                        //ustanovka ikonki
                        views.setImageViewResource(R.id.button_1_ramka, R.drawable.shape_for_widget_black);
                        int resId = context.getResources().getIdentifier(subTemp.getIcon(), "drawable", context.getPackageName());
                        views.setImageViewResource(R.id.button_1_icon, resId);
                        //ustanovka onclika
                        Intent button1 = new Intent(context, CalcActivity.class);
                        button1.putExtra(WidgetKeys.KEY_FOR_INTENT_ID, subTemp.getId());
                        button1.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
                        PendingIntent.getActivity(context, 1, button1, 0).cancel();
                        PendingIntent pendingIntent_button1 = PendingIntent.getActivity(context, 1, button1, 0);
                        views.setOnClickPendingIntent(R.id.button_1, pendingIntent_button1);

                    }

                    if (!butID_2.matches(WidgetKeys.BUTTON_DISABLED) && subTemp.getId().matches(butID_2)) {
                        //ustanovka ikonki
                        views.setImageViewResource(R.id.button_2_ramka, R.drawable.shape_for_widget_black);
                        int resId = context.getResources().getIdentifier(subTemp.getIcon(), "drawable", context.getPackageName());
                        views.setImageViewResource(R.id.button_2_icon, resId);
                        //ustanovka onclika
                        Intent button2 = new Intent(context, CalcActivity.class);
                        button2.putExtra(WidgetKeys.KEY_FOR_INTENT_ID, subTemp.getId());
                        button2.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
                        PendingIntent.getActivity(context, 2, button2, 0).cancel();
                        PendingIntent pendingIntent_button2 = PendingIntent.getActivity(context, 2, button2, 0);
                        views.setOnClickPendingIntent(R.id.button_2, pendingIntent_button2);


                    }

                    if (!butID_3.matches(WidgetKeys.BUTTON_DISABLED) && subTemp.getId().matches(butID_3)) {
                        //ustanovka ikonki
                        views.setImageViewResource(R.id.button_3_ramka, R.drawable.shape_for_widget_black);
                        int resId = context.getResources().getIdentifier(subTemp.getIcon(), "drawable", context.getPackageName());
                        views.setImageViewResource(R.id.button_3_icon, resId);
                        //ustanovka onclika
                        Intent button3 = new Intent(context, CalcActivity.class);
                        button3.putExtra(WidgetKeys.KEY_FOR_INTENT_ID, subTemp.getId());
                        button3.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
                        PendingIntent.getActivity(context, 3, button3, 0).cancel();
                        PendingIntent pendingIntent_button3 = PendingIntent.getActivity(context, 3, button3, 0);
                        views.setOnClickPendingIntent(R.id.button_3, pendingIntent_button3);

                    }

                    if (!butID_4.matches(WidgetKeys.BUTTON_DISABLED) && subTemp.getId().matches(butID_4)) {
                        //ustanovka ikonki
                        views.setImageViewResource(R.id.button_4_ramka, R.drawable.shape_for_widget_black);
                        int resId = context.getResources().getIdentifier(subTemp.getIcon(), "drawable", context.getPackageName());
                        views.setImageViewResource(R.id.button_4_icon, resId);
                        //ustanovka onclika
                        Intent button4 = new Intent(context, CalcActivity.class);
                        button4.putExtra(WidgetKeys.KEY_FOR_INTENT_ID, subTemp.getId());
                        button4.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
                        PendingIntent.getActivity(context, 4, button4, 0).cancel();
                        PendingIntent pendingIntent_button4 = PendingIntent.getActivity(context, 4, button4, 0);
                        views.setOnClickPendingIntent(R.id.button_4, pendingIntent_button4);

                    }
                }

            }


        if (butID_1.matches(WidgetKeys.BUTTON_DISABLED)) {
            views.setImageViewResource(R.id.button_1_ramka, R.drawable.shape_for_widget);
            views.setImageViewResource(R.id.button_1_icon, R.drawable.ic_add_widget);

            Intent button4 = new Intent(context, ChooseWidget.class);
            button4.putExtra(WidgetKeys.KEY_FOR_INTENT, WidgetKeys.BUTTON_1_ID);
            button4.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
            PendingIntent.getActivity(context, 1, button4, 0).cancel();
            PendingIntent pendingIntent_button4 = PendingIntent.getActivity(context, 1, button4, 0);
            views.setOnClickPendingIntent(R.id.button_1, pendingIntent_button4);

        }
        if (butID_2.matches(WidgetKeys.BUTTON_DISABLED)) {
            views.setImageViewResource(R.id.button_2_ramka, R.drawable.shape_for_widget);
            views.setImageViewResource(R.id.button_2_icon, R.drawable.ic_add_widget);
            Intent button4 = new Intent(context, ChooseWidget.class);
            button4.putExtra(WidgetKeys.KEY_FOR_INTENT, WidgetKeys.BUTTON_2_ID);
            button4.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
            PendingIntent.getActivity(context, 2, button4, 0).cancel();
            PendingIntent pendingIntent_button4 = PendingIntent.getActivity(context, 2, button4, 0);
            views.setOnClickPendingIntent(R.id.button_2, pendingIntent_button4);
        }
        if (butID_3.matches(WidgetKeys.BUTTON_DISABLED)) {
            views.setImageViewResource(R.id.button_3_ramka, R.drawable.shape_for_widget);
            views.setImageViewResource(R.id.button_3_icon, R.drawable.ic_add_widget);
            Intent button4 = new Intent(context, ChooseWidget.class);
            button4.putExtra(WidgetKeys.KEY_FOR_INTENT, WidgetKeys.BUTTON_3_ID);
            button4.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
            PendingIntent.getActivity(context, 3, button4, 0).cancel();
            PendingIntent pendingIntent_button4 = PendingIntent.getActivity(context, 3, button4, 0);
            views.setOnClickPendingIntent(R.id.button_3, pendingIntent_button4);
        }
        if (butID_4.matches(WidgetKeys.BUTTON_DISABLED)) {
            views.setImageViewResource(R.id.button_4_ramka, R.drawable.shape_for_widget);
            views.setImageViewResource(R.id.button_4_icon, R.drawable.ic_add_widget);
            Intent button4 = new Intent(context, ChooseWidget.class);
            button4.putExtra(WidgetKeys.KEY_FOR_INTENT, WidgetKeys.BUTTON_4_ID);
            button4.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID, widgetID);
            PendingIntent.getActivity(context, 4, button4, 0).cancel();
            PendingIntent pendingIntent_button4 = PendingIntent.getActivity(context, 4, button4, 0);
            views.setOnClickPendingIntent(R.id.button_4, pendingIntent_button4);
        }

        Intent active = new Intent(context, WidgetProvider.class);
        active.setAction(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM);
        active.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID,
                widgetID);
        Log.d(WidgetKeys.TAG, widgetID + "");
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, widgetID
                , active, 0);
        views.setOnClickPendingIntent(R.id.diagramma_widget, actionPendingIntent);


        Intent active_s = new Intent(context, SettingsWidget.class);
        active_s.setAction(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_set);
        active_s.putExtra(WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID,
                widgetID);
        PendingIntent actionPendingIntent_s = PendingIntent.getActivity(context, widgetID
                , active_s, 0);
        views.setOnClickPendingIntent(R.id.settings_widget, actionPendingIntent_s);

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("securewidget",false)){
            views.setViewVisibility(R.id.forgone, View.GONE);
            views.setViewVisibility(R.id.forgone1, View.GONE);
            views.setViewVisibility(R.id.forgone2, View.GONE);
            views.setViewVisibility(R.id.todayis, View.VISIBLE);
            SimpleDateFormat foramat=new SimpleDateFormat("dd.MM.yyyy");
            views.setTextViewText(R.id.todayis,foramat.format(new Date()
            ));

            Bitmap bitmap = makeDiagram(context,daoSession);
            views.setImageViewBitmap(R.id.diagramma_widget, bitmap);
             appWidgetManager.updateAppWidget(widgetID, views);
            db.close();
            return;
        }

        else {
            views.setViewVisibility(R.id.forgone, View.VISIBLE);
            views.setViewVisibility(R.id.forgone1, View.VISIBLE);
            views.setViewVisibility(R.id.forgone2, View.VISIBLE);
            views.setTextViewText(R.id.todayis,"");
            views.setViewVisibility(R.id.todayis, View.GONE);


        }

        Map<String, Double> resultMap = calculateBalance(new GregorianCalendar(2016,0,0),Calendar.getInstance(),context,daoSession);
        if(resultMap!=null){
        //balanceni berisiz
            if(getMainCurrency(daoSession)!=null) {
                views.setTextViewText(R.id.balance_widget, parseToWithoutNull(resultMap.get(PocketAccounterGeneral.BALANCE)) + getMainCurrency(daoSession).getAbbr());
                //rasxodni berisiz
                views.setTextViewText(R.id.expence_widget, parseToWithoutNull(resultMap.get(PocketAccounterGeneral.EXPENSES)) + getMainCurrency(daoSession).getAbbr());
                //doxoddi berisiz
                views.setTextViewText(R.id.income_widget, parseToWithoutNull(resultMap.get(PocketAccounterGeneral.INCOMES)) + getMainCurrency(daoSession).getAbbr());
            }
            else {
                views.setTextViewText(R.id.balance_widget, parseToWithoutNull(resultMap.get(PocketAccounterGeneral.BALANCE)) + "$");
                //rasxodni berisiz
                views.setTextViewText(R.id.expence_widget, parseToWithoutNull(resultMap.get(PocketAccounterGeneral.EXPENSES)) + "$");
                //doxoddi berisiz
                views.setTextViewText(R.id.income_widget, parseToWithoutNull(resultMap.get(PocketAccounterGeneral.INCOMES)) + "$");
            }
        }


        //income diagramma
        Bitmap bitmap = makeDiagram(context,daoSession);
        views.setImageViewBitmap(R.id.diagramma_widget, bitmap);
        appWidgetManager.updateAppWidget(widgetID, views);
        db.close();
    }

    static Bitmap makeDiagram(Context context,DaoSession daoSession) {
        List<FinanceRecord> listFn=daoSession.getFinanceRecordDao().loadAll();
        Calendar begin = Calendar.getInstance();
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        SharedPreferences prefs = context.getSharedPreferences("infoFirst", MODE_PRIVATE);
        int type = prefs.getInt(WidgetKeys.SETTINGS_WIDGET_PERIOD_TYPE, WidgetKeys.SETTINGS_WIDGET_PERIOD_TYPE_MONTH);
        int h = (int) context.getResources().getDimension(R.dimen.seventy_dp), w = (int)(4.5 * h);
        int distance = 0;
        int countOfDays = 0;
        if (type == WidgetKeys.SETTINGS_WIDGET_PERIOD_TYPE_MONTH)
            begin.add(Calendar.MONTH, -1);
        else
            begin.add(Calendar.DAY_OF_MONTH, -6);
        countOfDays = countOfDays(begin, end);
        distance = (int)(w/(countOfDays-0.8));
        ArrayList<FinanceRecord> tempInc = new ArrayList<>();
        ArrayList<FinanceRecord> tempExp = new ArrayList<>();

        for (int i=0; i<daoSession.getFinanceRecordDao().loadAll().size(); i++) {
            if (begin.compareTo(listFn.get(i).getDate()) <= 0 &&
                    end.compareTo(listFn.get(i).getDate()) >= 0) {
                if (listFn.get(i).getCategory().getType() == PocketAccounterGeneral.INCOME)
                    tempInc.add(listFn.get(i));
                else
                    tempExp.add(listFn.get(i));
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        ArrayList<Double> incomes = new ArrayList<>();
        ArrayList<Double> expenses = new ArrayList<>();
        while(begin.compareTo(end) <= 0) {
            Calendar first = (Calendar)begin.clone();
            Calendar second = (Calendar)begin.clone();
            second.set(Calendar.HOUR_OF_DAY, 23);
            second.set(Calendar.MINUTE, 59);
            second.set(Calendar.SECOND, 59);
            second.set(Calendar.MILLISECOND, 59);
            double incAmount = 0.0, expAmount = 0.0;
            for (int i=0; i<tempInc.size(); i++) {
                if (first.compareTo(tempInc.get(i).getDate()) <= 0 &&
                        second.compareTo(tempInc.get(i).getDate()) >= 0) {
                    incAmount = incAmount + getCost(tempInc.get(i));
                }
            }
            for (int i=0; i<tempExp.size(); i++) {
                if (first.compareTo(tempExp.get(i).getDate()) <= 0 &&
                        second.compareTo(tempExp.get(i).getDate()) >= 0) {
                    expAmount = expAmount + getCost(tempExp.get(i));
                }
            }
            for (Account account:daoSession.getAccountDao().loadAll()) {
                if (daoSession.getCurrencyDao().load(account.getLimitCurId()) != null)
                    incAmount = incAmount + getCost(Calendar.getInstance(), daoSession.getCurrencyDao().load(account.getLimitCurId()) , account.getAmount());
            }
            for (DebtBorrow debtBorrow : daoSession.getDebtBorrowDao().loadAll()) {
                if (debtBorrow.getCalculate()) {
                    if (debtBorrow.getTakenDate().compareTo(first) >= 0 && debtBorrow.getTakenDate().compareTo(second) <= 0) {
                        if (debtBorrow.getType() == DebtBorrow.BORROW) {
                            expAmount = expAmount + getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), debtBorrow.getAmount());
                        }
                        else {
                            incAmount = incAmount + getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), debtBorrow.getAmount());
                        }
                    }
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar cal = recking.getPayDate();
                        if (cal.compareTo(first) >= 0 && cal.compareTo(second) <= 0) {
                            if (debtBorrow.getType() == DebtBorrow.BORROW) {
                                incAmount = incAmount + getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), debtBorrow.getAmount());
                            }
                            else {
                                expAmount = expAmount + getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), debtBorrow.getAmount());
                            }
                        }
                    }
                }
            }
            for (CreditDetials creditDetials : daoSession.getCreditDetialsDao().loadAll()) {
                if (creditDetials.getKey_for_include()) {
                    for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                        Calendar cal = reckingCredit.getPayDate();
                        if (cal.compareTo(first) >= 0 && cal.compareTo(second) <= 0) {
                            expAmount = expAmount + getCost(cal, creditDetials.getValyute_currency(), reckingCredit.getAmount());
                        }
                    }
                }
            }
            incomes.add(incAmount);
            expenses.add(expAmount);
            begin.add(Calendar.DAY_OF_YEAR, 1);
        }

        //calculating debtborrows

        double max = 0.0;
        for (int i=0; i<incomes.size(); i++) {
            if (incomes.get(i) >= max)
                max = incomes.get(i);
        }
        for (int i=0; i<expenses.size(); i++) {
            if (expenses.get(i) >= max)
                max = expenses.get(i);
        }
        PointF[] incPoints = new PointF[countOfDays];
        PointF[] expPoints = new PointF[countOfDays];
        for (int i=0; i<countOfDays; i++) {
            incPoints[i] = new PointF();
            expPoints[i] = new PointF();
        }
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawRect(new RectF(0, 0, w, h), paint);
        //TODO
        paint.setColor(ContextCompat.getColor(context, R.color.info_header_lines));
        float one_dp = context.getResources().getDimension(R.dimen.one_dp);
        RectF container = new RectF(3*one_dp, 3*one_dp, w-3*one_dp, h-3*one_dp);
        float margin = container.height()/5;
        for (int i=0; i<6; i++)
            canvas.drawLine(container.left, i*margin+container.top, container.right, i*margin + container.top, paint);
        double amount = 0.0;
        for (int i=0; i<incPoints.length; i++) {
            if (max == 0.0) amount = 0.0;
            else amount = container.height()*incomes.get(i)/max;
            incPoints[i].set(i*distance+container.left, container.bottom - (float) amount);
        }
        for (int i=0; i<expPoints.length; i++) {
            if (max == 0.0) amount = 0.0;
            else amount = container.height()*expenses.get(i)/max;
            expPoints[i].set(i*distance+container.left, container.bottom - (float) amount);
        }
        paint.setAlpha(0xAA);
        paint.setStrokeWidth(1.2f*one_dp);
        paint.setColor(ContextCompat.getColor(context,R.color.green_light_darker_transparent));
        for (int i=0; i<incPoints.length; i++) {
            if (i == 0) continue;
            canvas.drawCircle(incPoints[i-1].x, incPoints[i-1].y, one_dp*1.6f, paint);
            canvas.drawLine(incPoints[i-1].x, incPoints[i-1].y, incPoints[i].x, incPoints[i].y, paint);
            canvas.drawCircle(incPoints[i].x, incPoints[i].y, one_dp*1.6f, paint);
        }
        paint.setColor(ContextCompat.getColor(context, R.color.red_green_darker_monoxrom_transparent ));
        for (int i=0; i<expPoints.length; i++) {
            if (i == 0) continue;
            canvas.drawCircle(expPoints[i-1].x, expPoints[i-1].y, one_dp*1.6f, paint);
            canvas.drawLine(expPoints[i-1].x, expPoints[i-1].y, expPoints[i].x, expPoints[i].y, paint);
            canvas.drawCircle(expPoints[i].x, expPoints[i].y, one_dp*1.6f, paint);
        }
        return bmp;
    }


   public static List<ReportObject> getReportObjects(Context context, DaoSession daoSession, boolean toMainCurrency, Calendar b, Calendar e, Class... classes) {
        Calendar begin = (Calendar) b.clone();
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) e.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        List<ReportObject> result = new ArrayList<>();
        for (Class cl : classes) {
            if (cl.getName().matches(Account.class.getName())) {
                for (Account account : daoSession.getAccountDao().loadAll()) {
                    if (account.getAmount() != 0 &&
                            account.getCalendar().compareTo(begin) >= 0 &&
                            account.getCalendar().compareTo(end) <= 0) {
                        ReportObject reportObject = new ReportObject();
                        reportObject.setType(PocketAccounterGeneral.INCOME);
                        reportObject.setAccount(account);
                        reportObject.setDate((Calendar)account.getCalendar().clone());
                        if (toMainCurrency) {
                            reportObject.setCurrency(getMainCurrency(daoSession));
                            reportObject.setAmount(getCost(account.getCalendar(),
                                    account.getStartMoneyCurrency(), account.getAmount()));
                        } else {
                            reportObject.setCurrency(account.getStartMoneyCurrency());
                            reportObject.setAmount(account.getAmount());
                        }
                        reportObject.setDescription(context.getResources().getString(R.string.start_amount));
                        result.add(reportObject);
                    }
                }
            }
            if (cl.getName().matches(AccountOperation.class.getName())) {
                for (AccountOperation accountOperations : daoSession.getAccountOperationDao().loadAll()) {
                    if (accountOperations.getDate().compareTo(begin) >= 0 &&
                            accountOperations.getDate().compareTo(end) <= 0) {
                        ReportObject reportObject = new ReportObject();
                        reportObject.setType(PocketAccounterGeneral.TRANSFER);
//                        reportObject.setAccount(accountOperations.getAccount());
//                        reportObject.setDate((Calendar)accountOperations.getDate().clone());
//                        if (toMainCurrency) {
//                            reportObject.setCurrency(commonOperations.getMainCurrency());
//                            reportObject.setAmount(commonOperations.getCost(accountOperations.getDate(), accountOperations.getCurrency(), accountOperations.getAmount()));
//                        }
//                        else {
//                            reportObject.setCurrency(reportObject.getCurrency());
//                            reportObject.setAmount(accountOperations.getAmount());
//                        }
//                        if (accountOperations.getType() == PocketAccounterGeneral.INCOME)
//                            reportObject.setDescription(context.getResources().getString(R.string.income));
//                        else if (accountOperations.getType() == PocketAccounterGeneral.EXPENSE)
//                            reportObject.setDescription(context.getResources().getString(R.string.expanse));
//                        else
//                            reportObject.setDescription(context.getString(R.string.transfer));
//                        result.add(reportObject);
                    }
                }
            }
            if (cl.getName().matches(FinanceRecord.class.getName())) {
                for (FinanceRecord financeRecord : daoSession.getFinanceRecordDao().loadAll()) {
                    if (financeRecord.getDate().compareTo(begin) >= 0 &&
                            financeRecord.getDate().compareTo(end) <= 0) {
                        ReportObject reportObject = new ReportObject();
                        reportObject.setType(financeRecord.getCategory().getType());
                        reportObject.setAccount(financeRecord.getAccount());
                        reportObject.setDate((Calendar) financeRecord.getDate().clone());
                        if (toMainCurrency) {
                            reportObject.setCurrency(getMainCurrency(daoSession));
                            reportObject.setAmount(getCost(financeRecord.getDate(), financeRecord.getCurrency(), financeRecord.getAmount()));
                        }
                        else {
                            reportObject.setCurrency(financeRecord.getCurrency());
                            reportObject.setAmount(financeRecord.getAmount());
                        }
                        reportObject.setDescription(financeRecord.getCategory().getName());
                        result.add(reportObject);
                    }
                }
            }
            if (cl.getName().matches(DebtBorrow.class.getName())) {
                for (DebtBorrow debtBorrow : daoSession.getDebtBorrowDao().loadAll()) {
                    if (!debtBorrow.getCalculate()) continue;
                    if (debtBorrow.getTakenDate().compareTo(begin) >= 0 &&
                            debtBorrow.getTakenDate().compareTo(end) <= 0) {
                        ReportObject reportObject = new ReportObject();
                        if (debtBorrow.getType() == DebtBorrow.BORROW) {
                            reportObject.setDescription(context.getResources().getString(R.string.borrow_statistics));
                            reportObject.setType(PocketAccounterGeneral.EXPENSE);
                        }
                        else {
                            reportObject.setDescription(context.getResources().getString(R.string.debt_statistics));
                            reportObject.setType(PocketAccounterGeneral.INCOME);
                        }
                        reportObject.setDate((Calendar) debtBorrow.getTakenDate().clone());
                        reportObject.setAccount(debtBorrow.getAccount());
                        if (toMainCurrency) {
                            reportObject.setAmount(getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), debtBorrow.getAmount()));
                            reportObject.setCurrency(getMainCurrency(daoSession));
                        }
                        else {
                            reportObject.setCurrency(debtBorrow.getCurrency());
                            reportObject.setAmount(debtBorrow.getAmount());
                        }
                        result.add(reportObject);
                    }
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar calendar = recking.getPayDate();

                        if (calendar.compareTo(begin) >= 0 && calendar.compareTo(end) <= 0) {
                            ReportObject reportObject = new ReportObject();
                            reportObject.setDate(calendar);
                            if (debtBorrow.getType() == DebtBorrow.BORROW) {
                                reportObject.setDescription(context.getResources().getString(R.string.borrow_recking_statistics));
                                reportObject.setType(PocketAccounterGeneral.INCOME);
                            }
                            else {
                                reportObject.setDescription(context.getResources().getString(R.string.debt_recking_statistics));
                                reportObject.setType(PocketAccounterGeneral.EXPENSE);
                            }
                            Account account = null;
                            for (Account acc : daoSession.getAccountDao().loadAll()) {
                                if (acc.getId().matches(recking.getAccountId())) {
                                    account = acc;
                                    break;
                                }
                            }
                            reportObject.setAccount(account);
                            if (toMainCurrency) {
                                reportObject.setAmount(getCost(calendar,
                                        debtBorrow.getCurrency(), recking.getAmount()));
                                reportObject.setCurrency(getMainCurrency(daoSession));
                            }
                            else {
                                reportObject.setAmount(recking.getAmount());
                                reportObject.setCurrency(debtBorrow.getCurrency());
                            }
                            result.add(reportObject);
                        }
                    }
                }
            }
            if (cl.getName().matches(CreditDetials.class.getName())) {
                for (CreditDetials creditDetials : daoSession.getCreditDetialsDao().loadAll()) {
                    if (!creditDetials.getKey_for_include()) continue;
                    for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                        Calendar calendar = reckingCredit.getPayDate();
                        if (calendar.compareTo(begin) >= 0 && calendar.compareTo(end) <= 0) {
                            ReportObject reportObject = new ReportObject();
                            reportObject.setType(PocketAccounterGeneral.EXPENSE);
                            reportObject.setDate(calendar);
                            reportObject.setDescription(context.getResources().getString(R.string.credit));
                            Account account = null;
                            for (Account acc : daoSession.getAccountDao().loadAll()) {
                                if (acc.getId().matches(reckingCredit.getAccountId())) {
                                    account = acc;
                                    break;
                                }
                            }

                            reportObject.setAccount(account);
                            if (toMainCurrency) {
                                reportObject.setCurrency(getMainCurrency(daoSession));
                                reportObject.setAmount(getCost(calendar,
                                        creditDetials.getValyute_currency(), reckingCredit.getAmount()));
                            }
                            else {
                                reportObject.setCurrency(creditDetials.getValyute_currency());
                                reportObject.setAmount(reckingCredit.getAmount());
                            }
                            result.add(reportObject);
                        }
                    }
                }
            }
        }
        return result;
    }
    public static  String parseToWithoutNull(double A) {
        if (A == (int) A) {
            return Integer.toString((int) A);
        } else{
            DecimalFormat formater;
            formater = new DecimalFormat("0.##");
            return formater.format(A);
        }
    }
    static public Currency getMainCurrency(DaoSession daoSession) {
        List<Currency> currencies = daoSession.getCurrencyDao().loadAll();
        for (Currency currency : currencies) {
            if (currency.getMain()) return currency;
        }
        return null;
    }

    static public double getCost(FinanceRecord record) {
        double amount = 0.0;
        if (record.getCurrency().getMain())
            return record.getAmount();
        double koeff = 1.0;
        long diff = record.getDate().getTimeInMillis() - record.getCurrency().getCosts().get(0).getDay().getTimeInMillis();
        if (diff < 0) {
            koeff = record.getCurrency().getCosts().get(0).getCost();
            return record.getAmount()/koeff;
        }
        int pos = 0;
        while (diff >= 0 && pos < record.getCurrency().getCosts().size()) {
            diff = record.getDate().getTimeInMillis() - record.getCurrency().getCosts().get(pos).getDay().getTimeInMillis();
            if(diff>=0)
                koeff = record.getCurrency().getCosts().get(pos).getCost();
            pos++;
        }
        amount = record.getAmount()/koeff;
        return amount;
    }

    public static double getCost(Calendar date, Currency currency, double amount) {
        if (currency.getMain()) return amount;
        double koeff = 1.0;
        long diff = date.getTimeInMillis() - currency.getCosts().get(0).getDay().getTimeInMillis();
        if (diff < 0) {
            koeff = currency.getCosts().get(0).getCost();
            return amount/koeff;
        }
        int pos = 0;
        while (diff >= 0 && pos < currency.getCosts().size()) {
            diff = date.getTimeInMillis() - currency.getCosts().get(pos).getDay().getTimeInMillis();
            if(diff>=0)
                koeff = currency.getCosts().get(pos).getCost();
            pos++;
        }
        amount = amount/koeff;
        Log.d("sss", "getCost: "+amount);
        return amount;
    }

    static  public Map<String, Double> calculateBalance(Calendar begin, Calendar end,Context context,DaoSession daoSession) {
        Map<String, Double> result = new HashMap<>();
        List<ReportObject> list = getReportObjects(context,daoSession,true, begin, end, Account.class, FinanceRecord.class, DebtBorrow.class, CreditDetials.class);
        Double incomes = 0.0d, expenses = 0.0d, balance = 0.0d;
        for (ReportObject reportObject : list) {
            if (reportObject.getType() == PocketAccounterGeneral.INCOME)
                incomes += reportObject.getAmount();
            else
                expenses += reportObject.getAmount();
        }
        balance = incomes - expenses;
        result.put(PocketAccounterGeneral.INCOMES, incomes);
        result.put(PocketAccounterGeneral.EXPENSES, expenses);
        result.put(PocketAccounterGeneral.BALANCE, balance);
        return result;
    }

    static int countOfDays(Calendar beg, Calendar e) {
        Calendar begin = (Calendar) beg.clone();
        Calendar end = (Calendar) e.clone();
        int countOfDays = 0;
        while(begin.compareTo(end) <= 0) {
            countOfDays++;
            begin.add(Calendar.DAY_OF_YEAR, 1);
        }
        return countOfDays;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM.equals(action)) {
            SharedPreferences sPref;
            Log.d(WidgetKeys.TAG, "coming");

            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Log.d(WidgetKeys.TAG, "bundle_not_null");
                mAppWidgetId = intent.getIntExtra(
                        WidgetKeys.ACTION_WIDGET_RECEIVER_CHANGE_DIAGRAM_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

            }
            Log.d(WidgetKeys.TAG, mAppWidgetId + "");

           Intent Intik=new Intent(context, PocketAccounter.class);
            Intik.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intik);
            if(AppWidgetManager.INVALID_APPWIDGET_ID!=mAppWidgetId)
                WidgetProvider.updateWidget(context, AppWidgetManager.getInstance(context),
                        mAppWidgetId);
        }
        super.onReceive(context, intent);
    }
}
