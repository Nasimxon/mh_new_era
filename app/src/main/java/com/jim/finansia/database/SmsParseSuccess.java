package com.jim.finansia.database;

import com.jim.finansia.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by root on 10/6/16.
 */
@Entity(nameInDb = "SMS_PARSE_SUCCESS", active = true)
public class SmsParseSuccess {
    @Property
    @Id
    private String id;
    @Property
    private String number = "";
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar date;
    @Property
    private String body;
    @Property
    private int type;
    @Property
    private String accountId;
    @Property
    @ToOne(joinProperty = "accountId")
    private Account account;
    @Property
    private String currencyId;
    @Property
    @ToOne(joinProperty = "currencyId")
    private Currency currency;
    @Property
    private double amount;
    @Property
    private String smsParseObjectId;
    @Property
    private boolean isSuccess;
    @Generated(hash = 1170963677)
    private transient String currency__resolvedKey;
    @Generated(hash = 1221310859)
    private transient String account__resolvedKey;
    /** Used for active entity operations. */
    @Generated(hash = 1806441768)
    private transient SmsParseSuccessDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    @Keep
    public SmsParseSuccess() {
        id = UUID.randomUUID().toString();
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
    public boolean getIsSuccess() {
        return this.isSuccess;
    }
    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
    public String getSmsParseObjectId() {
        return this.smsParseObjectId;
    }
    public void setSmsParseObjectId(String smsParseObjectId) {
        this.smsParseObjectId = smsParseObjectId;
    }
    public double getAmount() {
        return this.amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
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
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getBody() {
        return this.body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public Calendar getDate() {
        return this.date;
    }
    public void setDate(Calendar date) {
        this.date = date;
    }
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 561365418)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getSmsParseSuccessDao() : null;
    }
    @Generated(hash = 802090454)
    public SmsParseSuccess(String id, String number, Calendar date, String body,
            int type, String accountId, String currencyId, double amount,
            String smsParseObjectId, boolean isSuccess) {
        this.id = id;
        this.number = number;
        this.date = date;
        this.body = body;
        this.type = type;
        this.accountId = accountId;
        this.currencyId = currencyId;
        this.amount = amount;
        this.smsParseObjectId = smsParseObjectId;
        this.isSuccess = isSuccess;
    }
}
