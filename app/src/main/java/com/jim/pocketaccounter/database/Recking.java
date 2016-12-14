package com.jim.pocketaccounter.database;

import android.support.annotation.Keep;

import com.jim.pocketaccounter.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import java.util.Calendar;
import java.util.UUID;
import com.jim.pocketaccounter.database.DaoSession;

/**
 * Created by user on 6/8/2016.
 */
@Entity(nameInDb = "RECKINGS", active = true)
public class Recking {
    @Id
    @Property
    private String id;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar payDate;
    @Property
    private double amount;
    @Property
    private String accountId;
    @Property
    private String debtBorrowsId;
    @Property
    private String comment;
    /** Used for active entity operations. */
    @Generated(hash = 55176781)
    private transient ReckingDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    @Keep
    public Recking(Calendar payDate, double amount, String id, String accountId, String comment) {
        this.payDate = payDate;
        this.amount = amount;
        this.debtBorrowsId = id;
        this.accountId = accountId;
        this.comment = comment;
        this.id = UUID.randomUUID().toString();
    }
    @Keep
    public Recking(Calendar payDate, double amount, String id, String comment) {
        this.payDate = payDate;
        this.amount = amount;
        this.debtBorrowsId = id;
        this.accountId = "";
        this.comment = comment;
        this.id = UUID.randomUUID().toString();
    }
    @Keep
    public Recking() {
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
    public String getComment() {
        return this.comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getDebtBorrowsId() {
        return this.debtBorrowsId;
    }
    public void setDebtBorrowsId(String debtBorrowsId) {
        this.debtBorrowsId = debtBorrowsId;
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
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1014423750)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getReckingDao() : null;
    }
    @Generated(hash = 1526389974)
    public Recking(String id, Calendar payDate, double amount, String accountId, String debtBorrowsId,
            String comment) {
        this.id = id;
        this.payDate = payDate;
        this.amount = amount;
        this.accountId = accountId;
        this.debtBorrowsId = debtBorrowsId;
        this.comment = comment;
    }

}
