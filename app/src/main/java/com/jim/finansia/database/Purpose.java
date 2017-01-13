package com.jim.finansia.database;

import android.support.annotation.Keep;

import com.jim.finansia.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Calendar;
import java.util.UUID;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * Created by DEV on 06.09.2016.
 */
@Entity(nameInDb = "PURPOSES", active = true)
public class Purpose {
    @Property
    private String icon;
    @Property
    private String description;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar begin;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar end;
    @Property
    private double purpose;
    @Property
    private double accumulated;
    @Property
    private String currencyId;
    @ToOne(joinProperty = "currencyId")
    private Currency currency;
    @Id
    @Property
    private String id;
    @Property
    private int periodPos;
    @Property
    private int periodSize;
    @Generated(hash = 1170963677)
    private transient String currency__resolvedKey;
    /** Used for active entity operations. */
    @Generated(hash = 1375959903)
    private transient PurposeDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    @Keep
    public Purpose() {
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
    @Generated(hash = 1602104432)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPurposeDao() : null;
    }

    public int getPeriodSize() {
        return this.periodSize;
    }

    public void setPeriodSize(int periodSize) {
        this.periodSize = periodSize;
    }

    public int getPeriodPos() {
        return this.periodPos;
    }

    public void setPeriodPos(int periodPos) {
        this.periodPos = periodPos;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrencyId() {
        return this.currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public double getAccumulated() {
        return this.accumulated;
    }

    public void setAccumulated(double accumulated) {
        this.accumulated = accumulated;
    }

    public double getPurpose() {
        return this.purpose;
    }

    public void setPurpose(double purpose) {
        this.purpose = purpose;
    }

    public Calendar getEnd() {
        return this.end;
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }

    public Calendar getBegin() {
        return this.begin;
    }

    public void setBegin(Calendar begin) {
        this.begin = begin;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Generated(hash = 1918704743)
    public Purpose(String icon, String description, Calendar begin, Calendar end,
            double purpose, double accumulated, String currencyId, String id,
            int periodPos, int periodSize) {
        this.icon = icon;
        this.description = description;
        this.begin = begin;
        this.end = end;
        this.purpose = purpose;
        this.accumulated = accumulated;
        this.currencyId = currencyId;
        this.id = id;
        this.periodPos = periodPos;
        this.periodSize = periodSize;
    }
}
