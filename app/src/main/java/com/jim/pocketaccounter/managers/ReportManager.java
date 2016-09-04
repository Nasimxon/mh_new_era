package com.jim.pocketaccounter.managers;

import android.content.Context;
import android.util.Log;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.AccountOperations;
import com.jim.pocketaccounter.database.AccountOperationsDao;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.database.Recking;
import com.jim.pocketaccounter.database.ReckingCredit;
import com.jim.pocketaccounter.report.ReportObject;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by DEV on 01.09.2016.
 */

public class ReportManager {
    @Inject
    DaoSession daoSession;
    @Inject
    CommonOperations commonOperations;
    private Context context;
    private AccountOperationsDao accountOperationsDao;
    private FinanceRecordDao financeRecordDao;
    private DebtBorrowDao debtBorrowDao;
    private CreditDetialsDao creditDetialsDao;
    private AccountDao accountDao;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    public ReportManager(Context context) {
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        this.context = context;
        accountOperationsDao = daoSession.getAccountOperationsDao();
        financeRecordDao = daoSession.getFinanceRecordDao();
        debtBorrowDao = daoSession.getDebtBorrowDao();
        creditDetialsDao = daoSession.getCreditDetialsDao();
        accountDao = daoSession.getAccountDao();
    }

    public List<ReportObject> getReportObjects(boolean toMainCurrency, Calendar b, Calendar e, Class ... classes) {
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
                for (Account account : accountDao.loadAll()) {
                    if (account.getAmount() != 0 &&
                            account.getCalendar().compareTo(begin) >= 0 &&
                            account.getCalendar().compareTo(end) <= 0) {
                        ReportObject reportObject = new ReportObject();
                        reportObject.setType(PocketAccounterGeneral.INCOME);
                        reportObject.setAccount(account);
                        reportObject.setDate((Calendar)account.getCalendar().clone());
                        if (toMainCurrency) {
                            reportObject.setCurrency(commonOperations.getMainCurrency());
                            reportObject.setAmount(commonOperations.getCost(account.getCalendar(),
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
            if (cl.getName().matches(AccountOperations.class.getName())) {
                for (AccountOperations accountOperations : accountOperationsDao.loadAll()) {
                    if (accountOperations.getDate().compareTo(begin) >= 0 &&
                            accountOperations.getDate().compareTo(end) <= 0) {
                        ReportObject reportObject = new ReportObject();
                        reportObject.setType(accountOperations.getType());
                        reportObject.setAccount(accountOperations.getAccount());
                        reportObject.setDate((Calendar)accountOperations.getDate().clone());
                        if (toMainCurrency) {
                            reportObject.setCurrency(commonOperations.getMainCurrency());
                            reportObject.setAmount(commonOperations.getCost(accountOperations.getDate(), accountOperations.getCurrency(), accountOperations.getAmount()));
                        }
                        else {
                            reportObject.setCurrency(reportObject.getCurrency());
                            reportObject.setAmount(accountOperations.getAmount());
                        }
                        if (accountOperations.getType() == PocketAccounterGeneral.INCOME)
                            reportObject.setDescription(context.getResources().getString(R.string.income));
                        else if (accountOperations.getType() == PocketAccounterGeneral.EXPENSE)
                            reportObject.setDescription(context.getResources().getString(R.string.expanse));
                        else
                            reportObject.setDescription(context.getString(R.string.transfer));
                        result.add(reportObject);
                    }
                }
            }
            if (cl.getName().matches(FinanceRecord.class.getName())) {
                for (FinanceRecord financeRecord : financeRecordDao.loadAll()) {
                    if (financeRecord.getDate().compareTo(begin) >= 0 &&
                            financeRecord.getDate().compareTo(end) <= 0) {
                        ReportObject reportObject = new ReportObject();
                        reportObject.setType(financeRecord.getCategory().getType());
                        reportObject.setAccount(financeRecord.getAccount());
                        reportObject.setDate((Calendar) financeRecord.getDate().clone());
                        if (toMainCurrency) {
                            reportObject.setCurrency(commonOperations.getMainCurrency());
                            reportObject.setAmount(commonOperations.getCost(financeRecord.getDate(), financeRecord.getCurrency(), financeRecord.getAmount()));
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
                for (DebtBorrow debtBorrow : debtBorrowDao.loadAll()) {
                    if (!debtBorrow.isCalculate()) continue;
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
                            reportObject.setAmount(commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), debtBorrow.getAmount()));
                            reportObject.setCurrency(commonOperations.getMainCurrency());
                        }
                        else {
                            reportObject.setCurrency(debtBorrow.getCurrency());
                            reportObject.setAmount(debtBorrow.getAmount());
                        }
                        result.add(reportObject);
                    }
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(dateFormat.parse(recking.getPayDate()));
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
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
                            for (Account acc : accountDao.loadAll()) {
                                if (acc.getId().matches(recking.getAccountId())) {
                                    account = acc;
                                    break;
                                }
                            }
                            if (account == null) throw new RuntimeException("Account not found in class: " + getClass().getName() + ". Method: getRecordObjects();");
                            reportObject.setAccount(account);
                            if (toMainCurrency) {
                                reportObject.setAmount(commonOperations.getCost(calendar,
                                        debtBorrow.getCurrency(), recking.getAmount()));
                                reportObject.setCurrency(commonOperations.getMainCurrency());
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
                for (CreditDetials creditDetials : creditDetialsDao.loadAll()) {
                    if (!creditDetials.isKey_for_include()) continue;
                    for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(reckingCredit.getPayDate());
                        if (calendar.compareTo(begin) >= 0 && calendar.compareTo(end) <= 0) {
                            ReportObject reportObject = new ReportObject();
                            reportObject.setType(PocketAccounterGeneral.EXPENSE);
                            reportObject.setDate(calendar);
                            reportObject.setDescription(context.getResources().getString(R.string.credit));
                            Account account = null;
                            for (Account acc : accountDao.loadAll()) {
                                if (acc.getId().matches(reckingCredit.getAccountId())) {
                                    account = acc;
                                    break;
                                }
                            }
                            if (account == null) throw new RuntimeException("Account not found in class: " +
                                                                            getClass().getName() +
                                                                            ". Method: getRecordObjects();");
                            reportObject.setAccount(account);
                            if (toMainCurrency) {
                                reportObject.setCurrency(commonOperations.getMainCurrency());
                                reportObject.setAmount(commonOperations.getCost(calendar,
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

    public Map<String, Double> calculateBalance(Calendar begin, Calendar end) {
        Map<String, Double> result = new HashMap<>();
        List<ReportObject> list = getReportObjects(true, begin, end, Account.class, FinanceRecord.class, DebtBorrow.class, CreditDetials.class);
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

    public double calculateLimitAccountsAmount(Account account) {
        List<ReportObject> list = null;
        if (account.getLimitInterval())
            list = getReportObjects(false, account.getLimitBeginTime(), account.getLimitTime(),
                                                    Account.class,
                                                    FinanceRecord.class,
                                                    DebtBorrow.class,
                                                    CreditDetials.class);
        else
            list = getReportObjects(false, getFirstDay(), Calendar.getInstance(),
                    Account.class,
                    FinanceRecord.class,
                    DebtBorrow.class,
                    CreditDetials.class);
        double result = 0.0d;
        for (ReportObject reportObject : list) {
            if (reportObject.getAccount().getId().matches(account.getId()) &&
                    reportObject.getCurrency().getId().matches(account.getStartMoneyCurrency().getId())) {

                if (reportObject.getType() == PocketAccounterGeneral.INCOME)
                    result = result + reportObject.getAmount();
                else
                    result = result - reportObject.getAmount();
            }
        }
        return result;
    }

    public Calendar getFirstDay() {
        Calendar result = Calendar.getInstance();
        for (FinanceRecord financeRecord : financeRecordDao.loadAll()) {
            if (financeRecord.getDate().compareTo(result) <= 0)
                result = (Calendar) financeRecord.getDate().clone();
        }
        for (DebtBorrow debtBorrow : debtBorrowDao.loadAll()) {
            if (debtBorrow.getTakenDate().compareTo(result) <= 0)
                result = (Calendar) debtBorrow.getTakenDate().clone();
        }
        for (CreditDetials creditDetials : creditDetialsDao.loadAll()) {
            if (creditDetials.getTake_time().compareTo(result) <= 0)
                result = (Calendar) creditDetials.getTake_time().clone();
        }
        for (Account account : accountDao.loadAll()) {
            if (account.getCalendar().compareTo(result) <= 0)
                result = (Calendar) account.getCalendar().clone();
        }
        return result;
    }

    public Map<Currency, Double> getRemain(Account account) {
        List<ReportObject> list = getReportObjects(false, getFirstDay(), Calendar.getInstance(),
                                                    Account.class,
                                                    FinanceRecord.class,
                                                    DebtBorrow.class,
                                                    CreditDetials.class);
        Map<Currency, Double> result = new HashMap<>();
        for(ReportObject reportObject : list) {
            if (reportObject.getAccount().getId().matches(account.getId())) {
                Currency temp = null;
                boolean found = false;
                for (Currency currency : result.keySet()) {
                    if (currency.getId().matches(reportObject.getCurrency().getId())) {
                        found = true;
                        temp = currency;
                        break;
                    }
                }
                if (found) {
                    if (reportObject.getType() == PocketAccounterGeneral.INCOME)
                        result.put(temp, result.get(temp).doubleValue() + reportObject.getAmount());
                    else
                        result.put(temp, result.get(temp).doubleValue() - reportObject.getAmount());
                }
                else {
                    temp = reportObject.getCurrency();
                    if (reportObject.getType() == PocketAccounterGeneral.INCOME)
                        result.put(temp, reportObject.getAmount());
                    else
                        result.put(temp, -reportObject.getAmount());
                }
            }
        }
        Log.d("sss", result.size()+" size");
        return  result;
    }

    public List<ReportObject> getAccountOperations(Account account, Calendar begin, Calendar end) {
        List<ReportObject> result = new ArrayList<>();
        List<ReportObject> allObjects = getReportObjects(false, begin, end,
                Account.class,
                AccountOperations.class,
                FinanceRecord.class,
                DebtBorrow.class,
                CreditDetials.class);
        for (ReportObject reportObject : allObjects) {
            if (reportObject.getAccount().getId().matches(account.getId()))
                result.add(reportObject);
        }
        return result;
    }
}
