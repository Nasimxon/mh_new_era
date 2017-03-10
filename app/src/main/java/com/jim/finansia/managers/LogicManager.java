package com.jim.finansia.managers;

import android.content.Context;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.AccountOperation;
import com.jim.finansia.database.AccountOperationDao;
import com.jim.finansia.database.AutoMarket;
import com.jim.finansia.database.AutoMarketDao;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.BoardButtonDao;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyCostState;
import com.jim.finansia.database.CurrencyCostStateDao;
import com.jim.finansia.database.CurrencyDao;
import com.jim.finansia.database.CurrencyWithAmount;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.database.Person;
import com.jim.finansia.database.PersonDao;
import com.jim.finansia.database.Purpose;
import com.jim.finansia.database.PurposeDao;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.database.ReckingCreditDao;
import com.jim.finansia.database.ReckingDao;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.RootCategoryDao;
import com.jim.finansia.database.SmsParseObject;
import com.jim.finansia.database.SmsParseObjectDao;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.database.SmsParseSuccessDao;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.database.SubCategoryDao;
import com.jim.finansia.database.TemplateSmsDao;
import com.jim.finansia.database.TemplateVoice;
import com.jim.finansia.database.UserEnteredCalendars;
import com.jim.finansia.database.UserEnteredCalendarsDao;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
/**
 * Created by DEV on 28.08.2016.
 */

public class LogicManager {
    @Inject DaoSession daoSession;
    @Inject CommonOperations commonOperations;
    @Inject DataCache dataCache;
    private Context context;
    private CurrencyDao currencyDao;
    private FinanceRecordDao recordDao;
    private DebtBorrowDao debtBorrowDao;
    private CreditDetialsDao creditDetialsDao;
    private AccountDao accountDao;
    private ReckingCreditDao reckingCreditDao;
    private SubCategoryDao subCategoryDao;
    private BoardButtonDao boardButtonDao;
    private RootCategoryDao rootCategoryDao;
    private PurposeDao purposeDao;
    private PersonDao personDao;
    private ReckingDao reckingDao;
    private AccountOperationDao accountOperationDao;
    private AutoMarketDao autoMarketDao;
    private SmsParseObjectDao smsParseObjectDao;
    private SmsParseSuccessDao smsParseSuccessDao;
    private CurrencyCostStateDao currencyCostStateDao;
    private UserEnteredCalendarsDao userEnteredCalendarsDao;
    private TemplateSmsDao templateSmsDao;
    public LogicManager(Context context) {
        this.context = context;
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        currencyDao = daoSession.getCurrencyDao();
        recordDao = daoSession.getFinanceRecordDao();
        debtBorrowDao = daoSession.getDebtBorrowDao();
        creditDetialsDao = daoSession.getCreditDetialsDao();
        smsParseObjectDao = daoSession.getSmsParseObjectDao();
        accountDao = daoSession.getAccountDao();
        reckingCreditDao = daoSession.getReckingCreditDao();
        subCategoryDao = daoSession.getSubCategoryDao();
        boardButtonDao = daoSession.getBoardButtonDao();
        rootCategoryDao = daoSession.getRootCategoryDao();
        purposeDao = daoSession.getPurposeDao();
        personDao = daoSession.getPersonDao();
        reckingDao = daoSession.getReckingDao();
        accountOperationDao = daoSession.getAccountOperationDao();
        autoMarketDao = daoSession.getAutoMarketDao();
        smsParseObjectDao = daoSession.getSmsParseObjectDao();
        smsParseSuccessDao = daoSession.getSmsParseSuccessDao();
        currencyCostStateDao = daoSession.getCurrencyCostStateDao();
        userEnteredCalendarsDao = daoSession.getUserEnteredCalendarsDao();
        templateSmsDao = daoSession.getTemplateSmsDao();
    }

    public int deleteCurrency(List<Currency> currencies) {
        List<Currency> allCureencies = currencyDao.loadAll();
        if (allCureencies.size() < 2 || currencies.size() == allCureencies.size())
            return LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT;
        for (Currency currency : currencies) {
            List<FinanceRecord> financeRecords = recordDao.loadAll();
            for (FinanceRecord record : financeRecords) {
                if (record.getCurrency().getId().equals(currency.getId())) {
                    recordDao.delete(record);
                }
            }
            List<DebtBorrow> debtBorrows = debtBorrowDao.loadAll();
            for (DebtBorrow debtBorrow : debtBorrows) {
                if (debtBorrow.getCurrency().getId().equals(currency.getId()))
                    debtBorrowDao.delete(debtBorrow);
            }

            List<CreditDetials> creditDetialses = creditDetialsDao.loadAll();
            for (CreditDetials creditDetials : creditDetialses) {
                if (creditDetials.getValyute_currency().getId().equals(currency.getId()))
                    creditDetialsDao.delete(creditDetials);
            }
            List<SmsParseObject> smsParseObjects = smsParseObjectDao.loadAll();
            for (SmsParseObject smsParseObject : smsParseObjects) {
                if (smsParseObject.getCurrency().getId().equals(currency.getId()))
                    smsParseObjectDao.delete(smsParseObject);
            }
            List<SmsParseSuccess> smses = daoSession.getSmsParseSuccessDao().loadAll();
            for (SmsParseSuccess sms : smses) {
                if (sms.getCurrencyId().equals(currency.getId())) {
                    daoSession.getSmsParseSuccessDao().delete(sms);
                }
            }
            List<CurrencyCostState> states = currencyCostStateDao.loadAll();
            for (CurrencyCostState currencyCostState : states) {
                boolean found = currencyCostState.getMainCurrency().getId().equals(currency.getId());
                if (found) {
                    for (CurrencyWithAmount withAmount : currencyCostState.getCurrencyWithAmountList())
                        daoSession.getCurrencyWithAmountDao().delete(withAmount);
                    currencyCostStateDao.delete(currencyCostState);
                }
                else {
                    for (CurrencyWithAmount withAmount : currencyCostState.getCurrencyWithAmountList()) {
                        if (withAmount.getCurrencyId().equals(currency.getId())) {
                            daoSession.getCurrencyWithAmountDao().delete(withAmount);
                        }
                    }
                    currencyCostState.resetCurrencyWithAmountList();
                }
            }
            for (UserEnteredCalendars userEnteredCalendars : currency.getUserEnteredCalendarses())
                daoSession.getUserEnteredCalendarsDao().delete(userEnteredCalendars);
            List<Purpose> purposes = daoSession.getPurposeDao().loadAll();
            for (Purpose purpose : purposes) {
                if (purpose.getCurrencyId().equals(currency.getId())) {
                    daoSession.getPurposeDao().delete(purpose);
                }
            }
            currencyDao.delete(currency);
        }
        defineMainCurrency();
        commonOperations.refreshCurrency();
        daoSession.getCurrencyDao().detachAll();
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertAccount(Account account) {
        Query<Account> accountQuery = accountDao.queryBuilder()
                .where(AccountDao.Properties.Name.eq(account.getName())).build();
        if (!accountQuery.list().isEmpty())
            return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
        accountDao.insertOrReplace(account);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteAccount(List<Account> accounts) {
        List<Account> allAccounts = accountDao.loadAll();
        if (allAccounts.size() < 2 || allAccounts.size() == accounts.size())
            return LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT;
        for (Account account : accounts) {
            List<FinanceRecord> records = recordDao.loadAll();
            for (FinanceRecord record : records) {
                if (record.getAccount().getId().equals(account.getId())) {
                    recordDao.delete(record);
                }
            }
            List<DebtBorrow> debtBorrows = debtBorrowDao.loadAll();
            for (DebtBorrow debtBorrow : debtBorrows) {
                if (debtBorrow.getAccount().getId().equals(account.getId()))
                    debtBorrowDao.delete(debtBorrow);
            }
            debtBorrowDao.detachAll();
            debtBorrows = debtBorrowDao.loadAll();
            for (DebtBorrow debtBorrow : debtBorrows) {
                for (Recking recking : debtBorrow.getReckings()) {
                    if (recking.getAccountId().equals(account.getId())) {
                        debtBorrowDao.delete(debtBorrow);
                    }
                }
            }
            List<CreditDetials> creditDetialses = creditDetialsDao.loadAll();
            for (CreditDetials creditDetials : creditDetialses) {
                if (creditDetials.getAccountID().equals(account.getId()))
                    creditDetialsDao.delete(creditDetials);
            }
            creditDetialsDao.detachAll();
            creditDetialses = creditDetialsDao.loadAll();
            for (CreditDetials creditDetials : creditDetialses) {
                for (ReckingCredit reckingCredit : creditDetials.getReckings())
                    if (reckingCredit.getAccountId().matches(account.getId()))
                        reckingCreditDao.delete(reckingCredit);
            }
            List<SmsParseObject> smsParseObjects = smsParseObjectDao.loadAll();
            for (SmsParseObject smsParseObject : smsParseObjects) {
                if (smsParseObject.getAccount().getId().matches(account.getId()))
                    smsParseObjectDao.delete(smsParseObject);
            }
            List<AccountOperation> accountOperations = accountOperationDao.loadAll();
            for (AccountOperation accountOperation : accountOperations) {
                if (accountOperation.getSourceId().equals(account.getId()) ||
                        accountOperation.getTargetId().equals(account.getId())) {
                    daoSession.delete(accountOperation);
                }
            }

            List<SmsParseSuccess> smses = smsParseSuccessDao.loadAll();
            for (SmsParseSuccess sms : smses) {
                if (sms.getAccountId().equals(account.getId())) {
                    smsParseSuccessDao.delete(sms);
                }
            }
            List<AutoMarket> autoMarkets = daoSession.getAutoMarketDao().loadAll();
            for (AutoMarket autoMarket : autoMarkets) {
                if (autoMarket.getAccountId().equals(account.getId())) {
                    daoSession.getAutoMarketDao().delete(autoMarket);
                }
            }
            accountDao.delete(account);
            daoSession.getFinanceRecordDao().detachAll();
            daoSession.getDebtBorrowDao().detachAll();
            daoSession.getCreditDetialsDao().detachAll();
            daoSession.getSmsParseSuccessDao().detachAll();
            daoSession.getAutoMarketDao().detachAll();
            daoSession.getAccountDao().detachAll();
        }
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }



    public void generateCurrencyCosts(Calendar day, double amount, Currency adding) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        String addingDay = format.format(day.getTime());
        Currency mainCurrency = commonOperations.getMainCurrency();
        List<Currency> notMainCurrencies = daoSession.queryBuilder(Currency.class).where(CurrencyDao.Properties.IsMain.eq(false)).list();
        boolean isNew = daoSession.queryBuilder(CurrencyCostState.class).where(CurrencyCostStateDao.Properties.MainCurId.eq(adding.getId())).list().isEmpty();
        if (isNew) {
            List<CurrencyCostState> list = daoSession
                    .queryBuilder(CurrencyCostState.class)
                    .where(CurrencyCostStateDao.Properties.Day.eq(addingDay))
                    .list();
            if (list.isEmpty()) {
                List<CurrencyCostState> allStates = daoSession.loadAll(CurrencyCostState.class);
                Collections.sort(allStates, new Comparator<CurrencyCostState>() {
                    @Override
                    public int compare(CurrencyCostState currencyCostState, CurrencyCostState t1) {
                        return currencyCostState.getDay().compareTo(t1.getDay());
                    }
                });
                String last = "";
                if (day.compareTo(allStates.get(allStates.size()-1).getDay()) >= 0) {
                    last = format.format(allStates.get(allStates.size()-1).getDay().getTime());
                } else if (day.compareTo(allStates.get(0).getDay()) <= 0) {
                    last = format.format(allStates.get(0).getDay().getTime());
                } else {
                    int position = 0;
                    while (position < allStates.size() && day.compareTo(allStates.get(position).getDay()) > 0) {
                        last = format.format(allStates.get(position).getDay().getTime());
                        position++;
                    }
                }
                List<CurrencyCostState> lastStates = daoSession
                        .queryBuilder(CurrencyCostState.class)
                        .where(CurrencyCostStateDao.Properties.Day.eq(last))
                        .list();
                for (CurrencyCostState currencyCostState : lastStates) {
                    CurrencyCostState state = new CurrencyCostState();
                    state.setDay(day);
                    state.setMainCurrency(currencyCostState.getMainCurrency());
                    daoSession.insertOrReplace(state);
                    if (currencyCostState.getMainCurId().equals(mainCurrency.getId())) {
                        CurrencyWithAmount withAmount = new CurrencyWithAmount();
                        withAmount.setParentId(state.getId());
                        withAmount.setCurrency(adding);
                        withAmount.setAmount(amount);
                        daoSession.insertOrReplace(withAmount);
                    }
                    else {
                        double tempAmount = 1.0d;
                        CurrencyCostState main = null;
                        for (CurrencyCostState st : lastStates) {
                            if (st.getMainCurId().equals(mainCurrency.getId())) {
                                main = st;
                                break;
                            }
                        }
                        for (CurrencyWithAmount withAmount : main.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrencyId().equals(currencyCostState.getMainCurId())) {
                                tempAmount = withAmount.getAmount();
                                break;
                            }
                        }
                        CurrencyWithAmount withAmount = new CurrencyWithAmount();
                        withAmount.setCurrency(adding);
                        withAmount.setParentId(state.getId());
                        withAmount.setAmount(amount/tempAmount);
                        daoSession.insertOrReplace(withAmount);

                    }
                    for (CurrencyWithAmount cwa : currencyCostState.getCurrencyWithAmountList()) {
                        CurrencyWithAmount withAmount = new CurrencyWithAmount();
                        withAmount.setParentId(state.getId());
                        withAmount.setCurrency(cwa.getCurrency());
                        withAmount.setAmount(cwa.getAmount());
                        daoSession.insertOrReplace(withAmount);
                    }
                    state.resetCurrencyWithAmountList();
                }
                CurrencyCostState addingState = new CurrencyCostState();
                addingState.setMainCurrency(adding);
                addingState.setDay(day);
                daoSession.insertOrReplace(addingState);
                CurrencyWithAmount mainWithAmount = new CurrencyWithAmount();
                mainWithAmount.setParentId(addingState.getId());
                mainWithAmount.setCurrency(mainCurrency);
                mainWithAmount.setAmount(1/amount);
                daoSession.insertOrReplace(mainWithAmount);
                CurrencyCostState main = null;
                for (CurrencyCostState st : lastStates) {
                    if (st.getMainCurId().equals(mainCurrency.getId())) {
                        main = st;
                        break;
                    }
                }
                for (Currency currency : notMainCurrencies) {
                    double tempAmount = 1.0d;
                    for (CurrencyWithAmount withAmount : main.getCurrencyWithAmountList()) {
                        if (withAmount.getCurrencyId().equals(currency.getId())) {
                            tempAmount = withAmount.getAmount();
                            break;
                        }
                    }
                    CurrencyWithAmount withAmount = new CurrencyWithAmount();
                    withAmount.setCurrency(adding);
                    withAmount.setParentId(addingState.getId());
                    withAmount.setAmount(amount/tempAmount);
                    daoSession.insertOrReplace(withAmount);
                }
                addingState.resetCurrencyWithAmountList();
            } else {
                CurrencyCostState mainState = null;
                for (CurrencyCostState state : list) {
                    if (state.getMainCurId().equals(mainCurrency.getId())) {
                        mainState = state;
                        break;
                    }
                }
                for (CurrencyCostState state : list) {
                    CurrencyWithAmount withAmount = new CurrencyWithAmount();
                    withAmount.setCurrency(adding);
                    withAmount.setParentId(state.getId());
                    if (state.getMainCurId().equals(mainCurrency.getId())) {
                        withAmount.setAmount(amount);
                    } else {
                        double tempAmount = 1.0d;
                        for (CurrencyWithAmount wa : mainState.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrencyId().equals(state.getMainCurId())) {
                                tempAmount = wa.getAmount();
                                break;
                            }
                        }
                        withAmount.setAmount(amount/tempAmount);
                    }
                    daoSession.insertOrReplace(withAmount);
                    state.resetCurrencyWithAmountList();
                }
                CurrencyCostState addingState = new CurrencyCostState();
                addingState.setMainCurrency(adding);
                addingState.setDay(day);
                daoSession.insertOrReplace(addingState);
                CurrencyWithAmount mainWithAmount = new CurrencyWithAmount();
                mainWithAmount.setParentId(addingState.getId());
                mainWithAmount.setCurrency(mainCurrency);
                mainWithAmount.setAmount(1/amount);
                daoSession.insertOrReplace(mainWithAmount);
                addingState.resetCurrencyWithAmountList();
                CurrencyCostState main = null;
                for (CurrencyCostState st : list) {
                    if (st.getMainCurId().equals(mainCurrency.getId())) {
                        main = st;
                        break;
                    }
                }
                for (Currency currency : notMainCurrencies) {
                    double tempAmount = 1.0d;
                    for (CurrencyWithAmount withAmount : main.getCurrencyWithAmountList()) {
                        if (withAmount.getCurrencyId().equals(currency.getId())) {
                            tempAmount = withAmount.getAmount();
                            break;
                        }
                    }
                    CurrencyWithAmount withAmount = new CurrencyWithAmount();
                    withAmount.setCurrency(currency);
                    withAmount.setParentId(addingState.getId());
                    withAmount.setAmount(amount/tempAmount);
                    daoSession.insertOrReplace(withAmount);
                }
                addingState.resetCurrencyWithAmountList();
            }
            //generate for other days
            List<CurrencyCostState> allStatesWithoutToday = daoSession
                    .queryBuilder(CurrencyCostState.class)
                    .where(CurrencyCostStateDao.Properties.Day.notEq(addingDay))
                    .list();
            List<String> days = new ArrayList<>();
            for (CurrencyCostState state : allStatesWithoutToday) {
                boolean found = false;
                for (String temp : days) {
                    if (format.format(state.getDay().getTime()).equals(temp)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    days.add(format.format(state.getDay().getTime()));
            }
            for (String temp : days) {
                List<CurrencyCostState> statesForTheDay = daoSession
                        .queryBuilder(CurrencyCostState.class)
                        .where(CurrencyCostStateDao.Properties.Day.eq(temp))
                        .list();
                CurrencyCostState mainState = null;
                for (CurrencyCostState state : statesForTheDay) {
                    if (state.getMainCurId().equals(mainCurrency.getId())) {
                        mainState = state;
                        break;
                    }
                }
                CurrencyCostState addingState = new CurrencyCostState();
                addingState.setDay(day);
                addingState.setMainCurrency(adding);
                daoSession.insertOrReplace(addingState);
                CurrencyWithAmount mainWithAmount = new CurrencyWithAmount();
                mainWithAmount.setCurrency(mainCurrency);
                mainWithAmount.setAmount(1/amount);
                mainWithAmount.setParentId(addingState.getId());
                daoSession.insertOrReplace(mainWithAmount);
                for (Currency currency : notMainCurrencies) {
                    CurrencyWithAmount currencyWithAmount = new CurrencyWithAmount();
                    currencyWithAmount.setParentId(addingState.getId());
                    currencyWithAmount.setCurrency(currency);
                    double tempAmount = 1.0d;
                    for (CurrencyWithAmount withAmount : mainState.getCurrencyWithAmountList()) {
                        if (withAmount.getCurrencyId().equals(currency.getId())) {
                            tempAmount = withAmount.getAmount();
                            break;
                        }
                    }
                    currencyWithAmount.setAmount(amount/tempAmount);
                    daoSession.insertOrReplace(currencyWithAmount);
                }
                addingState.resetCurrencyWithAmountList();
                for (CurrencyCostState state : statesForTheDay) {
                    CurrencyWithAmount currencyWithAmount = new CurrencyWithAmount();
                    currencyWithAmount.setParentId(state.getId());
                    currencyWithAmount.setCurrency(adding);
                    if (state.getMainCurId().equals(mainCurrency.getId())) {
                        currencyWithAmount.setAmount(amount);
                    }
                    else {
                        double tempAmount = 1.0d;
                        for (CurrencyWithAmount withAmount : mainState.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrencyId().equals(state.getMainCurId())) {
                                tempAmount = withAmount.getAmount();
                                break;
                            }
                        }
                        currencyWithAmount.setAmount(amount/tempAmount);
                    }
                    daoSession.insertOrReplace(currencyWithAmount);
                    state.resetCurrencyWithAmountList();
                }
            }
        }
        else {
            List<CurrencyCostState> list = daoSession
                    .queryBuilder(CurrencyCostState.class)
                    .where(CurrencyCostStateDao.Properties.Day.eq(addingDay))
                    .list();
            if (list.isEmpty()) {
                CurrencyCostState supplimentaryState = null;
                List<CurrencyCostState> allStates = daoSession
                        .queryBuilder(CurrencyCostState.class)
                        .where(CurrencyCostStateDao.Properties.MainCurId.eq(mainCurrency.getId()))
                        .list();
                Collections.sort(allStates, new Comparator<CurrencyCostState>() {
                    @Override
                    public int compare(CurrencyCostState currencyCostState, CurrencyCostState t1) {
                        return currencyCostState.getDay().compareTo(t1.getDay());
                    }
                });
                if (allStates.get(allStates.size() - 1).getDay().compareTo(day) <= 0)
                    supplimentaryState = allStates.get(allStates.size() - 1);
                else if (allStates.get(0).getDay().compareTo(day) >= 0)
                    supplimentaryState = allStates.get(0);
                else {
                    int position = 0;
                    while (allStates.size() > position && allStates.get(position).getDay().compareTo(day) <= 0) {
                        supplimentaryState = allStates.get(position);
                        position++;
                    }
                }
                CurrencyCostState state = new CurrencyCostState();
                state.setDay(day);
                state.setMainCurrency(mainCurrency);
                daoSession.insertOrReplace(state);
                for (CurrencyWithAmount withAmount : supplimentaryState.getCurrencyWithAmountList()) {
                    CurrencyWithAmount currencyWithAmount = new CurrencyWithAmount();
                    currencyWithAmount.setParentId(state.getId());
                    currencyWithAmount.setCurrency(adding);
                    if (withAmount.getCurrencyId().equals(adding.getId())) {
                        currencyWithAmount.setAmount(amount);
                    } else {
                        currencyWithAmount.setAmount(withAmount.getAmount());
                    }
                    daoSession.insertOrReplace(currencyWithAmount);
                }
                state.resetCurrencyWithAmountList();
                List<CurrencyCostState> notMainStates = daoSession
                        .queryBuilder(CurrencyCostState.class)
                        .where(CurrencyCostStateDao.Properties.Day.eq(format.format(supplimentaryState.getDay().getTime())))
                        .list();
                for (Currency currency : notMainCurrencies) {
                    if (currency.getId().equals(adding.getId())) {
                        CurrencyCostState st = new CurrencyCostState();
                        st.setDay(day);
                        st.setMainCurrency(currency);
                        daoSession.insertOrReplace(st);
                        CurrencyCostState supply = null;
                        for (CurrencyCostState costState : notMainStates) {
                            if (costState.getMainCurId().equals(currency.getId())) {
                                supply = costState;
                                break;
                            }
                        }
                        for (CurrencyWithAmount withAmount : supply.getCurrencyWithAmountList()) {
                            CurrencyWithAmount currencyWithAmount = new CurrencyWithAmount();
                            if (withAmount.getCurrencyId().equals(mainCurrency.getId()))
                                currencyWithAmount.setAmount(1/amount);
                            else
                                currencyWithAmount.setAmount(withAmount.getAmount());
                            currencyWithAmount.setCurrency(withAmount.getCurrency());
                            currencyWithAmount.setParentId(st.getId());
                            daoSession.insertOrReplace(currencyWithAmount);
                        }
                        st.resetCurrencyWithAmountList();
                    }
                    else {
                        CurrencyCostState st = new CurrencyCostState();
                        st.setDay(day);
                        st.setMainCurrency(currency);
                        daoSession.insertOrReplace(st);
                        CurrencyCostState supply = null;
                        for (CurrencyCostState costState : notMainStates) {
                            if (costState.getMainCurId().equals(currency.getId())) {
                                supply = costState;
                                break;
                            }
                        }
                        for (CurrencyWithAmount withAmount : supply.getCurrencyWithAmountList()) {
                            CurrencyWithAmount currencyWithAmount = new CurrencyWithAmount();
                            currencyWithAmount.setAmount(withAmount.getAmount());
                            currencyWithAmount.setCurrency(withAmount.getCurrency());
                            currencyWithAmount.setParentId(st.getId());
                            daoSession.insertOrReplace(currencyWithAmount);
                        }
                        st.resetCurrencyWithAmountList();
                    }
                }
            }
            else {
                List<CurrencyCostState> states = daoSession
                        .queryBuilder(CurrencyCostState.class)
                        .where(CurrencyCostStateDao.Properties.Day.eq(format.format(list.get(0).getDay().getTime())))
                        .list();
                for (CurrencyCostState state : states) {
                    if (state.getMainCurId().equals(adding.getId())) {
                        for (CurrencyWithAmount withAmount : state.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrencyId().equals(mainCurrency.getId())) {
                                withAmount.setAmount(1/amount);
                                daoSession.insertOrReplace(withAmount);
                                break;
                            }
                        }
                    }
                    else if (state.getMainCurId().equals(mainCurrency.getId())) {
                        for (CurrencyWithAmount withAmount : state.getCurrencyWithAmountList()) {
                            if (withAmount.getCurrencyId().equals(adding.getId())) {
                                withAmount.setAmount(amount);
                                daoSession.insertOrReplace(withAmount);
                                break;
                            }
                        }
                    }
                    state.resetCurrencyWithAmountList();
                }
            }
            //TODO rest days
            List<CurrencyCostState> states = daoSession.loadAll(CurrencyCostState.class);
            Collections.sort(states, new Comparator<CurrencyCostState>() {
                @Override
                public int compare(CurrencyCostState currencyCostState, CurrencyCostState t1) {
                    return currencyCostState.getDay().compareTo(t1.getDay());
                }
            });
            List<CurrencyCostState> otherDays = new ArrayList<>();
            Calendar addDay = (Calendar) day.clone();
            addDay.set(Calendar.HOUR_OF_DAY, 23);
            addDay.set(Calendar.MINUTE, 59);
            addDay.set(Calendar.SECOND, 59);
            addDay.set(Calendar.MILLISECOND, 59);
            for (CurrencyCostState state : states) {
                if (state.getDay().compareTo(addDay) > 0) {
                    otherDays.add(state);
                }
            }
            for (CurrencyCostState state : otherDays) {
                if (dayExists(adding, state.getDay()))
                    break;
                if (state.equals(mainCurrency.getId())) {
                    for (CurrencyWithAmount withAmount : state.getCurrencyWithAmountList()) {
                        if (withAmount.getCurrencyId().equals(adding.getId())) {
                            withAmount.setAmount(amount);
                            daoSession.insertOrReplace(withAmount);
                            break;
                        }
                    }
                }
                else if (state.equals(adding.getId())) {
                    for (CurrencyWithAmount withAmount : state.getCurrencyWithAmountList()) {
                        if (withAmount.getCurrencyId().equals(mainCurrency.getId())) {
                            withAmount.setAmount(1/amount);
                            daoSession.insertOrReplace(withAmount);
                            break;
                        }
                    }
                }
                state.resetCurrencyWithAmountList();
            }
        }
        List<CurrencyCostState> allStates = daoSession.loadAll(CurrencyCostState.class);
        List<Currency> allCurrencies = daoSession.loadAll(Currency.class);
        for (CurrencyCostState state : allStates)
            state.resetCurrencyWithAmountList();
        for (Currency currency : allCurrencies)
            currency.refreshCosts();
    }

    private boolean dayExists(Currency currency, Calendar day) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        for (UserEnteredCalendars calendar : currency.getUserEnteredCalendarses()) {
            if (format.format(calendar.getCalendar().getTime()).equals(format.format(day.getTime()))) {
                return true;
            }
        }
        return false;
    }



    public int insertUserEnteredCalendars(Currency currency, Calendar day) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        List<UserEnteredCalendars> list = userEnteredCalendarsDao
                .queryBuilder()
                .where(UserEnteredCalendarsDao.Properties.CurrencyId.eq(currency.getId()),
                        UserEnteredCalendarsDao.Properties.Calendar.eq(format.format(day.getTime())))
                .list();
        if (!list.isEmpty()) return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
        UserEnteredCalendars userEnteredCalendars = new UserEnteredCalendars();
        userEnteredCalendars.setCalendar((Calendar)day.clone());
        userEnteredCalendars.setCurrencyId(currency.getId());
        userEnteredCalendarsDao.insertOrReplace(userEnteredCalendars);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    private void defineMainCurrency() {
        List<Currency> mainCurrencyList = daoSession
                .queryBuilder(Currency.class)
                .where(CurrencyDao.Properties.IsMain.eq(true))
                .list();
        if (mainCurrencyList.isEmpty()) {
            Currency currency = daoSession.getCurrencyDao().loadAll().get(0);
            currency.setMain(true);
            daoSession.insertOrReplace(currency);
            daoSession.getCurrencyDao().detachAll();
        }
    }

    public void setMainCurrency(Currency currency) {
        if (currency != null && currency.getMain()) return;
        List<Currency> currencies = daoSession.getCurrencyDao().loadAll();
        if (currency == null) {
            int pos = 0;
            for (int i = 0; i < currencies.size(); i++) {
                if (currencies.get(i).getMain()) {
                    pos = i;
                    break;
                }
            }
            currencies.get(pos).setMain(false);
            if (pos == currencies.size() - 1)
                currencies.get(0).setMain(true);
            else
                currencies.get(pos + 1).setMain(true);
        } else {
            int oldMainPos = 0;
            int currMainPos = 0;
            for (int i = 0; i < currencies.size(); i++) {
                if (currencies.get(i).getMain()) {
                    oldMainPos = i;
                }
                if (currencies.get(i).getId().matches(currency.getId())) {
                    currMainPos = i;
                }
            }
            currencies.get(oldMainPos).setMain(false);
            currencies.get(currMainPos).setMain(true);
        }
        daoSession.getCurrencyDao().insertOrReplaceInTx(currencies);
        List<CurrencyCostState> allStates = daoSession.loadAll(CurrencyCostState.class);
        for (CurrencyCostState state : allStates)
            state.resetCurrencyWithAmountList();
        List<Currency> allCurrencies = daoSession.loadAll(Currency.class);
        for (Currency curr : allCurrencies)
            curr.refreshCosts();
        commonOperations.refreshCurrency();
        dataCache.updateAllPercents();
        commonOperations.refreshCurrency();
    }

    public int insertSubCategory(List<SubCategory> subCategories) {
        subCategoryDao.insertOrReplaceInTx(subCategories);

        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteSubcategories(List<SubCategory> subCategories) {
        for (SubCategory subCategory : subCategories) {
            for (FinanceRecord financeRecord : recordDao.loadAll()) {
                if (financeRecord.getSubCategory() != null && financeRecord.getSubCategory().getId().equals(subCategory.getId()))
                    recordDao.delete(financeRecord);
            }
        }
        subCategoryDao.deleteInTx(subCategories);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public void changeBoardButton(int type, int pos, String categoryId) {
        int t = PocketAccounterGeneral.CATEGORY;
        if (categoryId != null) {
            List<RootCategory> categoryList = daoSession.getRootCategoryDao().loadAll();
            boolean categoryFound = false, operationFound = false, creditFound = false,
                    debtBorrowFound = false;
            for (RootCategory category : categoryList) {
                if (categoryId.matches(category.getId())) {
                    categoryFound = true;
                    t = PocketAccounterGeneral.CATEGORY;
                    break;
                }
            }
            if (!categoryFound) {
                String[] operationIds = context.getResources().getStringArray(R.array.operation_ids);
                for (String operationId : operationIds) {
                    if (operationId.matches(categoryId)) {
                        operationFound = true;
                        t = PocketAccounterGeneral.FUNCTION;
                        break;
                    }
                }
            }
            if (!operationFound) {
                List<CreditDetials> credits = daoSession.getCreditDetialsDao().loadAll();
                for (CreditDetials creditDetials : credits) {
                    if (Long.toString(creditDetials.getMyCredit_id()).matches(categoryId)) {
                        creditFound = true;
                        t = PocketAccounterGeneral.CREDIT;
                        break;
                    }
                }
            }
            if (!creditFound) {
                List<DebtBorrow> debtBorrows = daoSession.getDebtBorrowDao().loadAll();
                for (DebtBorrow debtBorrow : debtBorrows) {
                    if (debtBorrow.getId().matches(categoryId)) {
                        debtBorrowFound = true;
                        t = PocketAccounterGeneral.DEBT_BORROW;
                        break;
                    }
                }
            }
            if (!debtBorrowFound) {
                String[] pageIds = context.getResources().getStringArray(R.array.page_ids);
                for (int i = 0; i < pageIds.length; i++) {
                    if (pageIds[i].matches(categoryId)) {
                        t = PocketAccounterGeneral.PAGE;
                        break;
                    }
                }
            }
        }
        Query<BoardButton> query = boardButtonDao
                .queryBuilder()
                .where(BoardButtonDao.Properties.Table.eq(type),
                        BoardButtonDao.Properties.Pos.eq(pos))
                .build();
        List<BoardButton> list = query.list();
        BoardButton boardButton = null;
        if (!list.isEmpty()) {
            boardButton = list.get(0);
            boardButton.setCategoryId(categoryId);
        }
        if (boardButton != null)
            boardButton.setType(t);
        boardButtonDao.insertOrReplace(boardButton);
    }

    public int insertRootCategory(RootCategory rootCategory) {
        Query<RootCategory> query = rootCategoryDao
                .queryBuilder()
                .where(RootCategoryDao.Properties.Name.eq(rootCategory.getName()))
                .build();
        if (!query.list().isEmpty())
            return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
        rootCategoryDao.insertOrReplace(rootCategory);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteRootCategory(RootCategory category) {
        for (FinanceRecord record : recordDao.loadAll())
            if (record.getCategory().getId().matches(category.getId()))
                recordDao.delete(record);
        for (BoardButton boardButton : boardButtonDao.loadAll())
            if (boardButton.getCategoryId() != null && boardButton.getCategoryId().matches(category.getId())) {
                boardButton.setCategoryId(null);
                boardButtonDao.insertOrReplace(boardButton);
                commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());
            }
        rootCategoryDao.delete(category);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertPurpose(Purpose purpose) {
        Query<Purpose> query = purposeDao
                .queryBuilder()
                .where(PurposeDao.Properties.Id.eq(purpose.getId()))
                .build();
        if (query.list().isEmpty()) {
            query = purposeDao
                    .queryBuilder()
                    .where(PurposeDao.Properties.Description.eq(purpose.getDescription()))
                    .build();
            if (!query.list().isEmpty())
                return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
        }
        purposeDao.insertOrReplace(purpose);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deletePurpose(Purpose purpose) {
        List<AccountOperation> accountOperations = daoSession.loadAll(AccountOperation.class);
        for (AccountOperation accountOperation : accountOperations) {
            if (accountOperation.getSourceId().equals(purpose.getId()) ||
                    accountOperation.getTargetId().equals(purpose.getId())) {
                daoSession.delete(accountOperation);
            }
        }
        Query<Purpose> query = purposeDao
                .queryBuilder()
                .where(PurposeDao.Properties.Id.eq(purpose.getId()))
                .build();
        if (query.list().isEmpty()) {
            return LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND;
        }
        purposeDao.delete(purpose);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertDebtBorrow(DebtBorrow debtBorrow) {
        debtBorrowDao.insertOrReplace(debtBorrow);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteDebtBorrow(DebtBorrow debtBorrow) {
        Query<DebtBorrow> query = debtBorrowDao.queryBuilder()
                .where(DebtBorrowDao.Properties.Id.eq(debtBorrow.getId()))
                .build();
        if (query.list().isEmpty()) {
            return LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND;
        }
        daoSession
                .queryBuilder(Recking.class)
                .where(ReckingDao.Properties.DebtBorrowsId.eq(debtBorrow.getId()))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
        debtBorrowDao.delete(debtBorrow);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertPerson(Person person) {
        personDao.insertOrReplace(person);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int insertCredit(CreditDetials creditDetials) {
        creditDetialsDao.insertOrReplace(creditDetials);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteCredit(CreditDetials creditDetials) {
        Query<CreditDetials> query = creditDetialsDao.queryBuilder()
                .where(CreditDetialsDao.Properties.MyCredit_id.eq(creditDetials.getMyCredit_id()))
                .build();
        if (query.list().isEmpty()) {
            return LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND;
        }
        creditDetialsDao.delete(creditDetials);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertReckingDebt(Recking recking) {
        reckingDao.insertOrReplace(recking);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteRecking(Recking recking) {
        reckingDao.delete(recking);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertReckingCredit(ReckingCredit reckingCredit) {
        reckingCreditDao.insertOrReplace(reckingCredit);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteReckingCredit(ReckingCredit reckingCredit) {
        reckingCreditDao.delete(reckingCredit);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertAccountOperation(AccountOperation accountOperation) {
        accountOperationDao.insertOrReplace(accountOperation);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteAccountOperation(AccountOperation accountOperation) {
        accountOperationDao.delete(accountOperation);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertAutoMarket(AutoMarket autoMarket) {
        autoMarket.__setDaoSession(daoSession);
        Query<AutoMarket> query = autoMarketDao.queryBuilder()
                .where(autoMarketDao.queryBuilder()
                        .and(AutoMarketDao.Properties.CatId.eq(autoMarket.getCatId()),
                                AutoMarketDao.Properties.CatSubId.eq(autoMarket.getSubCategory() == null ? "" : autoMarket.getCatSubId()))).build();

        if (query.list() != null && query.list().isEmpty()) {
            autoMarketDao.insertOrReplace(autoMarket);
            return LogicManagerConstants.SAVED_SUCCESSFULL;
        }
        return LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS;
    }

    public int deleteAutoMarket(AutoMarket autoMarket) {
        autoMarketDao.delete(autoMarket);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int deleteSmsParseObject(SmsParseObject smsParseObject) {
        smsParseSuccessDao.queryBuilder().where(SmsParseSuccessDao.Properties.SmsParseObjectId.eq(smsParseObject.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
        templateSmsDao.queryBuilder().where(TemplateSmsDao.Properties.ParseObjectId.eq(smsParseObject.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
        smsParseObjectDao.delete(smsParseObject);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int deleteSmsParseSuccess(SmsParseSuccess smsParseSuccess) {
        smsParseSuccessDao.delete(smsParseSuccess);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }
    public static int CAN_NOT_NEGATIVE = 121;
    public static int LIMIT = 101;
    public static int YOU_CAN_ADD = 111;

    public int isItPosibleToAdd(Account account,double amount, Currency amountCurrency, Calendar date, double oldEdit,Currency oldAmount,Account oldValueAccount){
        if(account.getIsLimited()||account.getNoneMinusAccount()){
            double limit = account.getLimite();
            double state = isLimitAccess(account,date);
            if(oldEdit!=0){
                if(oldValueAccount.getId().equals(account.getId()))
                state += commonOperations.getCost(date,oldAmount,account.getCurrency(),oldEdit);
            }
            if(account.getNoneMinusAccount()){
                if(state-commonOperations.getCost(date,amountCurrency,account.getCurrency(),amount)<0){
                    return CAN_NOT_NEGATIVE;
                }
            }
            if((-1*limit)<=(state-commonOperations.getCost(date,amountCurrency,account.getCurrency(),amount))){
                return YOU_CAN_ADD;
            }
            else return LIMIT;
        }
        else return YOU_CAN_ADD;
    }
    public int changeAccount(Account account, Calendar date, double newLimit,Currency newLimitCurrency,double startMoney , Currency startmoneyCurrency){
            double state = isLimitSatet(account,date,newLimitCurrency,startMoney,startmoneyCurrency);
            if(newLimit*(-1)<=state){
                return YOU_CAN_ADD;
            }
            else return LIMIT;

    }
    public double isLimitAccess(Account account, Calendar date) {
        double accounted = commonOperations.getCost(date, account.getStartMoneyCurrency(),account.getCurrency(), account.getAmount());
        List<AccountOperation> operations = daoSession.getAccountOperationDao().loadAll();
        for (AccountOperation accountOperation : operations) {
            if (accountOperation.getSourceId().equals(account.getId())) {
                accounted -= commonOperations.getCost(date, accountOperation.getCurrency(), account.getCurrency(), accountOperation.getAmount());
            }
            if (accountOperation.getTargetId().equals(account.getId())) {
                accounted += commonOperations.getCost(date, accountOperation.getCurrency(), account.getCurrency(), accountOperation.getAmount());
            }
        }
        for (int i = 0; i < recordDao.queryBuilder().list().size(); i++) {
            FinanceRecord tempac = recordDao.queryBuilder().list().get(i);
            if (tempac.getAccount().getId().equals(account.getId())) {
                if (tempac.getCategory().getType() == PocketAccounterGeneral.INCOME)
                    accounted = accounted + commonOperations.getCost(tempac.getDate(), tempac.getCurrency(), account.getCurrency(), tempac.getAmount());
                else
                    accounted = accounted - commonOperations.getCost(tempac.getDate(), tempac.getCurrency(), account.getCurrency(), tempac.getAmount());
            }
        }
        for (DebtBorrow debtBorrow : debtBorrowDao.queryBuilder().list()) {
            if (debtBorrow.getCalculate()) {
                if(debtBorrow.getAccount() == null) continue;
                if (debtBorrow.getAccount().getId().equals(account.getId()))
                if (debtBorrow.getType() == DebtBorrow.BORROW) {
                    accounted = accounted - commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), account.getCurrency(), debtBorrow.getAmount());
                } else {
                    accounted = accounted + commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), account.getCurrency(), debtBorrow.getAmount());
                }
            }
             for (Recking recking : debtBorrow.getReckings()) {
                 if(recking.getAccountId() == null) continue;
                 if (recking.getAccountId().equals(account.getId())) {
                     Calendar cal = recking.getPayDate();
                     if (debtBorrow.getType() == DebtBorrow.DEBT) {
                         accounted = accounted - commonOperations.getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                     } else {
                         accounted = accounted + commonOperations.getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                     }
                 }
             }
        }

        for (CreditDetials creditDetials : creditDetialsDao.queryBuilder().list()) {
            if (creditDetials.getKey_for_include()) {
                if(creditDetials.getAccountID().equals(account.getId()))
                accounted = accounted + commonOperations.getCost(creditDetials.getTake_time(), creditDetials.getValyute_currency(), account.getCurrency(), creditDetials.getValue_of_credit());
            }
                for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                    if (reckingCredit.getAccountId().equals(account.getId())) {
                        accounted -= commonOperations.getCost(reckingCredit.getPayDate(), creditDetials.getValyute_currency(), account.getCurrency(), reckingCredit.getAmount());
                    }
                }
            }
        for (SmsParseSuccess success: smsParseSuccessDao.loadAll()) {
            if(success.getAccountId().equals(account.getId()))
            if (success.getType() == PocketAccounterGeneral.INCOME) {
                accounted += commonOperations.getCost(success.getDate(), success.getCurrency(), account.getCurrency(), success.getAmount());
            } else {
                accounted -= commonOperations.getCost(success.getDate(), success.getCurrency(),account.getCurrency(),  success.getAmount());
            }
        }
        return accounted;
    }
    public double isLimitSatet(Account account, Calendar date,Currency currency,double startMoney , Currency startmoneyCurrency){
        double accounted = commonOperations.getCost(date, startmoneyCurrency,currency, startMoney);
        List<AccountOperation> operations = daoSession.getAccountOperationDao().loadAll();
        for (AccountOperation accountOperation : operations) {
            if (accountOperation.getSourceId().equals(account.getId())) {
                accounted -= commonOperations.getCost(date, accountOperation.getCurrency(), currency, accountOperation.getAmount());
            }
            if (accountOperation.getTargetId().equals(account.getId())) {
                accounted += commonOperations.getCost(date, accountOperation.getCurrency(), currency, accountOperation.getAmount());
            }
        }
        for (int i = 0; i < recordDao.queryBuilder().list().size(); i++) {
            FinanceRecord tempac = recordDao.queryBuilder().list().get(i);
            if (tempac.getAccount().getId().equals(account.getId())) {
                if (tempac.getCategory().getType() == PocketAccounterGeneral.INCOME)
                    accounted = accounted + commonOperations.getCost(tempac.getDate(), tempac.getCurrency(), currency, tempac.getAmount());
                else
                    accounted = accounted - commonOperations.getCost(tempac.getDate(), tempac.getCurrency(), currency, tempac.getAmount());
            }
        }
        for (DebtBorrow debtBorrow : debtBorrowDao.queryBuilder().list()) {
            if (debtBorrow.getCalculate()) {
                if(debtBorrow.getAccount() == null) continue;
                if (debtBorrow.getAccount().getId().equals(account.getId()))
                    if (debtBorrow.getType() == DebtBorrow.BORROW) {
                        accounted = accounted - commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), currency, debtBorrow.getAmount());
                    } else {
                        accounted = accounted + commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), currency, debtBorrow.getAmount());
                    }
            }
            for (Recking recking : debtBorrow.getReckings()) {
                if(recking.getAccountId() == null) continue;
                if (recking.getAccountId().equals(account.getId())) {
                    Calendar cal = recking.getPayDate();
                    if (debtBorrow.getType() == DebtBorrow.DEBT) {
                        accounted = accounted - commonOperations.getCost(cal, debtBorrow.getCurrency(), currency, recking.getAmount());
                    } else {
                        accounted = accounted + commonOperations.getCost(cal, debtBorrow.getCurrency(), currency, recking.getAmount());
                    }
                }
            }
        }

        for (CreditDetials creditDetials : creditDetialsDao.queryBuilder().list()) {
            if (creditDetials.getKey_for_include()) {
                if(creditDetials.getAccountID().equals(account.getId()))
                    accounted = accounted + commonOperations.getCost(creditDetials.getTake_time(), creditDetials.getValyute_currency(), currency, creditDetials.getValue_of_credit());
            }
            for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                if (reckingCredit.getAccountId().equals(account.getId())) {
                    accounted -= commonOperations.getCost(reckingCredit.getPayDate(), creditDetials.getValyute_currency(), currency, reckingCredit.getAmount());
                }
            }
        }
        for (SmsParseSuccess success: smsParseSuccessDao.loadAll()) {
            if(success.getAccountId().equals(account.getId()))
                if (success.getType() == PocketAccounterGeneral.INCOME) {
                    accounted += commonOperations.getCost(success.getDate(), success.getCurrency(),currency, success.getAmount());
                } else {
                    accounted -= commonOperations.getCost(success.getDate(), success.getCurrency(),currency, success.getAmount());
                }
        }
        return accounted;
    }

    public int insertRecord(FinanceRecord record) {
        recordDao.insertOrReplace(record);
        daoSession.getFinanceRecordDao().detachAll();
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public int deleteRecord(FinanceRecord record) {
        recordDao.delete(record);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }
}