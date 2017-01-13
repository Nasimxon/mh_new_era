package com.jim.finansia.database;

import android.support.annotation.Keep;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Calendar;
import java.util.UUID;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import com.jim.finansia.database.convertors.CalendarConvertor;

/**
 * Created by root on 9/15/16.
 */
@Entity
public class AutoMarket {
    @Property
    @Id
    @NotNull
    private String id;
    @Property
    private double amount;
    @Property
    private String curId;
    @Property
    private String accountId;
    @Property
    private String catId;
    @Property
    private String catSubId;
    @Property
    private boolean type;
    @Property
    private String dates;
    @ToOne(joinProperty = "curId")
    private Currency currency;
    @ToOne(joinProperty = "accountId")
    private Account account;
    @ToOne(joinProperty = "catId")
    private RootCategory rootCategory;
    @ToOne(joinProperty = "catSubId")
    private SubCategory subCategory;
    @Convert(columnType = String.class, converter = CalendarConvertor.class)
    private Calendar createDay;
    @Property
    private String posDays;
    @Generated(hash = 861346724)
    private transient String subCategory__resolvedKey;
    @Generated(hash = 1021791985)
    private transient String rootCategory__resolvedKey;
    @Generated(hash = 1221310859)
    private transient String account__resolvedKey;
    @Generated(hash = 1170963677)
    private transient String currency__resolvedKey;
    /** Used for active entity operations. */
    @Generated(hash = 1148468049)
    private transient AutoMarketDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    @Keep
    public AutoMarket() {
        id = "auto:" + UUID.randomUUID().toString();
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1591199015)
    public void setSubCategory(SubCategory subCategory) {
        synchronized (this) {
            this.subCategory = subCategory;
            catSubId = subCategory == null ? null : subCategory.getId();
            subCategory__resolvedKey = catSubId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1527263568)
    public SubCategory getSubCategory() {
        String __key = this.catSubId;
        if (subCategory__resolvedKey == null || subCategory__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SubCategoryDao targetDao = daoSession.getSubCategoryDao();
            SubCategory subCategoryNew = targetDao.load(__key);
            synchronized (this) {
                subCategory = subCategoryNew;
                subCategory__resolvedKey = __key;
            }
        }
        return subCategory;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 392290114)
    public void setRootCategory(RootCategory rootCategory) {
        synchronized (this) {
            this.rootCategory = rootCategory;
            catId = rootCategory == null ? null : rootCategory.getId();
            rootCategory__resolvedKey = catId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 497497640)
    public RootCategory getRootCategory() {
        String __key = this.catId;
        if (rootCategory__resolvedKey == null || rootCategory__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            RootCategoryDao targetDao = daoSession.getRootCategoryDao();
            RootCategory rootCategoryNew = targetDao.load(__key);
            synchronized (this) {
                rootCategory = rootCategoryNew;
                rootCategory__resolvedKey = __key;
            }
        }
        return rootCategory;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1910176546)
    public void setAccount(Account account) {
        synchronized (this) {
            this.account = account;
            accountId = account == null ? null : account.getId();
            account__resolvedKey = accountId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1230349477)
    public Account getAccount() {
        String __key = this.accountId;
        if (account__resolvedKey == null || account__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AccountDao targetDao = daoSession.getAccountDao();
            Account accountNew = targetDao.load(__key);
            synchronized (this) {
                account = accountNew;
                account__resolvedKey = __key;
            }
        }
        return account;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2072894682)
    public void setCurrency(Currency currency) {
        synchronized (this) {
            this.currency = currency;
            curId = currency == null ? null : currency.getId();
            currency__resolvedKey = curId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1555757165)
    public Currency getCurrency() {
        String __key = this.curId;
        if (currency__resolvedKey == null || currency__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CurrencyDao targetDao = daoSession.getCurrencyDao();
            Currency currencyNew = targetDao.load(__key);
            synchronized (this) {
                currency = currencyNew;
                currency__resolvedKey = __key;
            }
        }
        return currency;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 391731539)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAutoMarketDao() : null;
    }
    public String getPosDays() {
        return this.posDays;
    }
    public void setPosDays(String posDays) {
        this.posDays = posDays;
    }
    public Calendar getCreateDay() {
        return this.createDay;
    }
    public void setCreateDay(Calendar createDay) {
        this.createDay = createDay;
    }
    public String getDates() {
        return this.dates;
    }
    public void setDates(String dates) {
        this.dates = dates;
    }
    public boolean getType() {
        return this.type;
    }
    public void setType(boolean type) {
        this.type = type;
    }
    public String getCatSubId() {
        return this.catSubId;
    }
    public void setCatSubId(String catSubId) {
        this.catSubId = catSubId;
    }
    public String getCatId() {
        return this.catId;
    }
    public void setCatId(String catId) {
        this.catId = catId;
    }
    public String getAccountId() {
        return this.accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public String getCurId() {
        return this.curId;
    }
    public void setCurId(String curId) {
        this.curId = curId;
    }
    public double getAmount() {
        return this.amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @Generated(hash = 203716419)
    public AutoMarket(@NotNull String id, double amount, String curId,
            String accountId, String catId, String catSubId, boolean type,
            String dates, Calendar createDay, String posDays) {
        this.id = id;
        this.amount = amount;
        this.curId = curId;
        this.accountId = accountId;
        this.catId = catId;
        this.catSubId = catSubId;
        this.type = type;
        this.dates = dates;
        this.createDay = createDay;
        this.posDays = posDays;
    }
}
