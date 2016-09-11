package com.jim.pocketaccounter.managers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.BoardButtonDao;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.CurrencyCost;
import com.jim.pocketaccounter.database.CurrencyCostDao;
import com.jim.pocketaccounter.database.CurrencyDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.database.Person;
import com.jim.pocketaccounter.database.PersonDao;
import com.jim.pocketaccounter.database.Purpose;
import com.jim.pocketaccounter.database.PurposeDao;
import com.jim.pocketaccounter.database.Recking;
import com.jim.pocketaccounter.database.ReckingCredit;
import com.jim.pocketaccounter.database.ReckingCreditDao;
import com.jim.pocketaccounter.database.ReckingDao;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.RootCategoryDao;
import com.jim.pocketaccounter.database.SmsParseObject;
import com.jim.pocketaccounter.database.SmsParseObjectDao;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.database.SubCategoryDao;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by DEV on 28.08.2016.
 */

public class LogicManager {
    @Inject
    DaoSession daoSession;
    @Inject
    CommonOperations commonOperations;
    private CurrencyDao currencyDao;
    private CurrencyCostDao currencyCostDao;
    private FinanceRecordDao recordDao;
    private DebtBorrowDao debtBorrowDao;
    private CreditDetialsDao creditDetialsDao;
    private SmsParseObjectDao smsParseObjectDao;
    private AccountDao accountDao;
    private ReckingCreditDao reckingCreditDao;
    private SubCategoryDao subCategoryDao;
    private BoardButtonDao boardButtonDao;
    private RootCategoryDao rootCategoryDao;
    private PurposeDao purposeDao;
    private PersonDao personDao;
    private ReckingDao reckingDao;

    public LogicManager(Context context) {
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        currencyDao = daoSession.getCurrencyDao();
        currencyCostDao = daoSession.getCurrencyCostDao();
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
    }

    public int deleteCurrency(List<Currency> currencies) {
        List<Currency> allCureencies = currencyDao.loadAll();
        if (allCureencies.size() < 2 || currencies.size() == allCureencies.size())
            return LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT;
        for (Currency currency : currencies) {
            for (FinanceRecord record : recordDao.loadAll()) {
                if (record.getCurrency().getId().matches(currency.getId())) {
                    recordDao.delete(record);
                }
            }
            for (DebtBorrow debtBorrow : debtBorrowDao.loadAll()) {
                if (debtBorrow.getCurrency().getId().matches(currency.getId()))
                    debtBorrowDao.delete(debtBorrow);
            }
            for (CreditDetials creditDetials : creditDetialsDao.loadAll()) {
                if (creditDetials.getValyute_currency().getId().matches(currency.getId()))
                    creditDetialsDao.delete(creditDetials);
            }
            for (SmsParseObject smsParseObject : smsParseObjectDao.loadAll()) {
                if (smsParseObject.getCurrency().getId().matches(currency.getId()))
                    smsParseObjectDao.delete(smsParseObject);
            }
            if (currency.getMain())
                setMainCurrency(null);
            currencyDao.delete(currency);
        }
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
        if (allAccounts.size() < 2 || accounts.size() == accounts.size())
            return LogicManagerConstants.MUST_BE_AT_LEAST_ONE_OBJECT;
        for (Account account : accounts) {
            for (FinanceRecord record : recordDao.loadAll()) {
                if (record.getAccount().getId().matches(account.getId())) {
                    recordDao.delete(record);
                }
            }
            for (DebtBorrow debtBorrow : debtBorrowDao.loadAll()) {
                if (debtBorrow.getAccount().getId().matches(account.getId()))
                    debtBorrowDao.delete(debtBorrow);
            }
            for (CreditDetials creditDetials : creditDetialsDao.loadAll()) {
                for (ReckingCredit reckingCredit : creditDetials.getReckings())
                    if (reckingCredit.getAccountId().matches(account.getId()))
                        reckingCreditDao.delete(reckingCredit);
            }
            for (SmsParseObject smsParseObject : smsParseObjectDao.loadAll()) {
                if (smsParseObject.getAccount().getId().matches(account.getId()))
                    smsParseObjectDao.delete(smsParseObject);
            }
            accountDao.delete(account);
        }
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public void setMainCurrency(Currency currency) {
        List<Currency> currencies = currencyDao.loadAll();
        Currency mainCurrency = null;
        if (currency == null) {
            int pos = 0;
            for (int i = 0; i < currencies.size(); i++) {
                if (currencies.get(i).getMain()) {
                    pos = i;
                    break;
                }
            }
            currencies.get(pos).setMain(false);
            if (pos == currencies.size() - 1) {
                currencies.get(0).setMain(true);
                mainCurrency = currencies.get(0);
            } else {
                currencies.get(pos + 1).setMain(true);
                mainCurrency = currencies.get(pos + 1);
            }
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
            mainCurrency = currencies.get(currMainPos);
        }
        double koeff = mainCurrency.getCosts().get(mainCurrency.getCosts().size() - 1).getCost();
        for (int i = 0; i < mainCurrency.getCosts().size(); i++) {
            CurrencyCost current = mainCurrency.getCosts().get(i);
            Calendar currDay = (Calendar) current.getDay().clone();
            currDay.set(Calendar.HOUR_OF_DAY, 0);
            currDay.set(Calendar.MINUTE, 0);
            currDay.set(Calendar.SECOND, 0);
            currDay.set(Calendar.MILLISECOND, 0);
            for (int j = 0; j < currencies.size(); j++) {
                if (currencies.get(j).getMain()) continue;
                for (int k = 0; k < currencies.get(j).getCosts().size(); k++) {
                    CurrencyCost currencyCost = currencies.get(j).getCosts().get(k);
                    if (currencyCost.getDay().compareTo(currDay) >= 0)
                        currencyCost.setCost(currencyCost.getCost() / current.getCost());
                    currencyCostDao.insertOrReplace(currencyCost);
                }
            }
            mainCurrency.getCosts().get(i).setCost(mainCurrency.getCosts().get(i).getCost() / koeff);
            currencyCostDao.insertOrReplaceInTx(mainCurrency.getCosts().get(i));
        }
        currencyDao.insertOrReplaceInTx(currencies);
    }

    //currency costs
    public int deleteCurrencyCosts(List<CurrencyCost> currencyCost) {
        if (currencyCost.isEmpty() || currencyCost == null)
            return LogicManagerConstants.LIST_IS_EMPTY;
        String currencyId = currencyCost.get(0).getCurrencyId();
        Currency costsCurrency = null;
        for (Currency currency : currencyDao.loadAll()) {
            if (currency.getId().matches(currencyId)) {
                costsCurrency = currency;
                break;
            }
        }
        if (currencyCost.size() == costsCurrency.getCosts().size()) {
            for (int i = 0; i < costsCurrency.getCosts().size(); i++) {
                if (i == 0) continue;
                currencyCostDao.delete(costsCurrency.getCosts().get(i));
                costsCurrency.getCosts().remove(i);
                i--;
            }
        } else {
            for (CurrencyCost cc : currencyCost) {
                for (CurrencyCost currcc : costsCurrency.getCosts()) {
                    if (cc.getCurrencyId().matches(costsCurrency.getId()) &&
                            cc.getId() == currcc.getId()) {
                        costsCurrency.getCosts().remove(currcc);
                    }
                }
            }
            currencyCostDao.deleteInTx(currencyCost);
        }
        return LogicManagerConstants.DELETED_SUCCESSFUL;
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
        Query<BoardButton> query = boardButtonDao
                .queryBuilder()
                .where(BoardButtonDao.Properties.Type.eq(type), BoardButtonDao.Properties.Pos.eq(pos))
                .build();
        List<BoardButton> list = query.list();
        BoardButton boardButton = null;
        if (!list.isEmpty())
            boardButton = list.get(0);
        boardButton.setCategoryId(categoryId);
        Query<BoardButton> boardButtonQuery = boardButtonDao
                .queryBuilder()
                .where(BoardButtonDao.Properties.Id.eq(boardButton.getId()))
                .build();
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
        Query<DebtBorrow> query = debtBorrowDao
                .queryBuilder()
                .where(DebtBorrowDao.Properties.Id.eq(debtBorrow.getId()))
                .build();
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
        debtBorrowDao.delete(debtBorrow);
        return LogicManagerConstants.DELETED_SUCCESSFUL;
    }

    public int insertPerson(Person person) {
        Query<Person> query = personDao
                .queryBuilder()
                .where(PersonDao.Properties.Id.eq(person.getId()))
                .build();
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

    public int insertReckingCredit(ReckingCredit reckingCredit) {
        reckingCreditDao.insertOrReplace(reckingCredit);
        return LogicManagerConstants.SAVED_SUCCESSFULL;
    }

    public double isLimitAccess(Account account, Calendar date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        double accounted = commonOperations.getCost(date, account.getStartMoneyCurrency(), account.getCurrency(), account.getAmount());
        for (int i = 0; i < recordDao.queryBuilder().list().size(); i++) {
            FinanceRecord tempac = recordDao.queryBuilder().list().get(i);
            if (tempac.getAccount().getId().matches(account.getId())) {
                if (tempac.getCategory().getType() == PocketAccounterGeneral.INCOME)
                    accounted = accounted + commonOperations.getCost(tempac.getDate(), tempac.getCurrency(), account.getCurrency(), tempac.getAmount());
                else
                    accounted = accounted - commonOperations.getCost(tempac.getDate(), tempac.getCurrency(), account.getCurrency(), tempac.getAmount());
            }
        }
        for (DebtBorrow debtBorrow : debtBorrowDao.queryBuilder().list()) {
            if (debtBorrow.getCalculate()) {
                if (debtBorrow.getAccount().getId().matches(account.getId())) {
                    if (debtBorrow.getType() == DebtBorrow.BORROW) {
                        accounted = accounted - commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), account.getCurrency(), debtBorrow.getAmount());
                    } else {
                        accounted = accounted + commonOperations.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(), account.getCurrency(), debtBorrow.getAmount());
                    }
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar cal = Calendar.getInstance();
                        try {
                            cal.setTime(simpleDateFormat.parse(recking.getPayDate()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (debtBorrow.getType() == DebtBorrow.DEBT) {
                            accounted = accounted - commonOperations.getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                        } else {
                            accounted = accounted + commonOperations.getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                        }
                    }
                } else {
                    for (Recking recking : debtBorrow.getReckings()) {
                        Calendar cal = Calendar.getInstance();
                        if (recking.getAccountId().matches(account.getId())) {
                            try {
                                cal.setTime(simpleDateFormat.parse(recking.getPayDate()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (debtBorrow.getType() == DebtBorrow.BORROW) {
                                accounted = accounted + commonOperations.getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                            } else {
                                accounted = accounted - commonOperations.getCost(cal, debtBorrow.getCurrency(), account.getCurrency(), recking.getAmount());
                            }
                        }
                    }
                }
            }
        }
        for (CreditDetials creditDetials : creditDetialsDao.queryBuilder().list()) {
            if (creditDetials.isKey_for_include()) {
                for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                    if (reckingCredit.getAccountId().matches(account.getId())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(reckingCredit.getPayDate());
                        accounted = accounted - commonOperations.getCost(cal, creditDetials.getValyute_currency(), account.getCurrency(), reckingCredit.getAmount());
                    }
                }
            }
        }
        return accounted;
    }
}