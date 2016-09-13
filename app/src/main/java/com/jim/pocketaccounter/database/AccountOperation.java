package com.jim.pocketaccounter.database;

import android.support.annotation.Keep;

import com.jim.pocketaccounter.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Calendar;
import java.util.UUID;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by DEV on 31.08.2016.
 */
@Entity(nameInDb = "ACCOUNT_OPERATIONS", active = true)
public class AccountOperation {
    @Id
    @Property
    private String id;
    @Convert(converter = CalendarConvertor.class, columnType = Long.class)
    private Calendar date;
    @Property
    private String sourceId;
    @Property
    private String targetId;
    @Property
    private String currencyId;
    @ToOne(joinProperty = "currencyId")
    private Currency currency;
    @Property
    private double amount;
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
    @Generated(hash = 1170963677)
    private transient String currency__resolvedKey;
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 673519319)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAccountOperationDao() : null;
    }
    /** Used for active entity operations. */
    @Generated(hash = 1904212381)
    private transient AccountOperationDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
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
    public String getTargetId() {
        return this.targetId;
    }
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    public String getSourceId() {
        return this.sourceId;
    }
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    public Calendar getDate() {
        return this.date;
    }
    public void setDate(Calendar date) {
        this.date = date;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @Generated(hash = 682025703)
    public AccountOperation(String id, Calendar date, String sourceId,
            String targetId, String currencyId, double amount) {
        this.id = id;
        this.date = date;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.currencyId = currencyId;
        this.amount = amount;
    }
    @Keep
    public AccountOperation() {
        id = UUID.randomUUID().toString();
    }
}
