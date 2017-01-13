package com.jim.finansia.database;
import com.jim.finansia.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Calendar;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "FINANCE_RECORDS", active = true)
public class FinanceRecord {
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar date;
    @Property
    private double amount = 0.0;
    @Property
    private String categoryId;
    @ToOne(joinProperty = "categoryId")
    private RootCategory category = null;
    @Property
    private String subCategoryId;
    @ToOne(joinProperty = "subCategoryId")
    private SubCategory subCategory = null;
    @Property
    private String accountId;
    @ToOne(joinProperty = "accountId")
    private Account account = null;
    @Property
    private String currencyId;
    @ToOne(joinProperty = "currencyId")
    private Currency currency = null;
    @Property
    @Id
    private String recordId;
    @ToMany(joinProperties = {
            @JoinProperty(name = "recordId", referencedName = "recordId")
    })
    private List<PhotoDetails> allTickets=null;
    @Property
    private String comment;
    @Generated(hash = 1170963677)
    private transient String currency__resolvedKey;
    @Generated(hash = 1221310859)
    private transient String account__resolvedKey;
    @Generated(hash = 861346724)
    private transient String subCategory__resolvedKey;
    @Generated(hash = 646829400)
    private transient String category__resolvedKey;
    /** Used for active entity operations. */
    @Generated(hash = 43612006)
    private transient FinanceRecordDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    @Generated(hash = 885026433)
    public FinanceRecord(Calendar date, double amount, String categoryId, String subCategoryId,
            String accountId, String currencyId, String recordId, String comment) {
        this.date = date;
        this.amount = amount;
        this.categoryId = categoryId;
        this.subCategoryId = subCategoryId;
        this.accountId = accountId;
        this.currencyId = currencyId;
        this.recordId = recordId;
        this.comment = comment;
    }
    @Generated(hash = 1746567756)
    public FinanceRecord() {
    }
    
    public void setAllTickets(List<PhotoDetails> allTickets) {
        this.allTickets = allTickets;
    }
    @Keep
    public List<PhotoDetails> getAllTickets() {
        if (allTickets == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PhotoDetailsDao targetDao = daoSession.getPhotoDetailsDao();
            List<PhotoDetails> allTicketsNew = targetDao._queryFinanceRecord_AllTickets(recordId);
            synchronized (this) {
                if(allTickets == null) {
                    allTickets = allTicketsNew;
                }
            }
        }
        return allTickets;
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
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 2058894754)
    public synchronized void resetAllTickets() {
        allTickets = null;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1889019422)
    public void setCurrency(Currency currency) {
        synchronized (this) {
            this.currency = currency;
            currencyId = currency == null ? null : currency.getId();
            currency__resolvedKey = currencyId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 376477166)
    public Currency getCurrency() {
        String __key = this.currencyId;
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
    @Generated(hash = 612508343)
    public void setSubCategory(SubCategory subCategory) {
        synchronized (this) {
            this.subCategory = subCategory;
            subCategoryId = subCategory == null ? null : subCategory.getId();
            subCategory__resolvedKey = subCategoryId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 183596225)
    public SubCategory getSubCategory() {
        String __key = this.subCategoryId;
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
    @Generated(hash = 1178232072)
    public void setCategory(RootCategory category) {
        synchronized (this) {
            this.category = category;
            categoryId = category == null ? null : category.getId();
            category__resolvedKey = categoryId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 41481511)
    public RootCategory getCategory() {
        String __key = this.categoryId;
        if (category__resolvedKey == null || category__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            RootCategoryDao targetDao = daoSession.getRootCategoryDao();
            RootCategory categoryNew = targetDao.load(__key);
            synchronized (this) {
                category = categoryNew;
                category__resolvedKey = __key;
            }
        }
        return category;
    }
    public String getComment() {
        return this.comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getRecordId() {
        return this.recordId;
    }
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
    public String getCurrencyId() {
        return this.currencyId;
    }
    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }
    public String getAccountId() {
        return this.accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public String getSubCategoryId() {
        return this.subCategoryId;
    }
    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }
    public String getCategoryId() {
        return this.categoryId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    public double getAmount() {
        return this.amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public Calendar getDate() {
        return this.date;
    }
    public void setDate(Calendar date) {
        this.date = date;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 453688691)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFinanceRecordDao() : null;
    }


}
