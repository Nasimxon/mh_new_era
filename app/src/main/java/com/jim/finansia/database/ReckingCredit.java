package com.jim.finansia.database;

import android.support.annotation.Keep;

import com.jim.finansia.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import java.util.Calendar;

/**
 * Created by DEV on 11.06.2016.
 */
@Entity(nameInDb = "RECKING_CREDITS", active = true)
public class ReckingCredit {
    @Id
    @NotNull
    private long id;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar payDate;
    @Property
    private double amount;
    @Property
    private String accountId;
    @Property
    @NotNull
    private long myCredit_id;
    @Property
    private String comment;
    /** Used for active entity operations. */
    @Generated(hash = 1358241115)
    private transient ReckingCreditDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    @Keep
    public ReckingCredit(Calendar payDate, double amount, String accountId, long myCredit_id, String comment) {
        this.payDate = payDate;
        this.amount = amount;
        this.accountId = accountId;
        this.myCredit_id = myCredit_id;
        this.comment = comment;
        this.id = System.currentTimeMillis();
    }
    @Keep
    public ReckingCredit() {
        this.id = System.currentTimeMillis();
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
    public String getComment() {
        return this.comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public long getMyCredit_id() {
        return this.myCredit_id;
    }
    public void setMyCredit_id(long myCredit_id) {
        this.myCredit_id = myCredit_id;
    }
    public String getAccountId() {
        return this.accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public double getAmount() {
        return this.amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public Calendar getPayDate() {
        return this.payDate;
    }
    public void setPayDate(Calendar payDate) {
        this.payDate = payDate;
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 897927379)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getReckingCreditDao() : null;
    }
    @Generated(hash = 944164788)
    public ReckingCredit(long id, Calendar payDate, double amount, String accountId, long myCredit_id,
            String comment) {
        this.id = id;
        this.payDate = payDate;
        this.amount = amount;
        this.accountId = accountId;
        this.myCredit_id = myCredit_id;
        this.comment = comment;
    }

  

}