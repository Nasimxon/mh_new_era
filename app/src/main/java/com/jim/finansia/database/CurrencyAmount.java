package com.jim.finansia.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "CURRENCIES_AND_AMOUNTS", active = true)
public class CurrencyAmount {
    @Id(autoincrement = true)
    private Long id;
    @ToOne
    private Currency currency;
    @Property
    private double amount;
    @Generated(hash = 131953365)
    private transient boolean currency__refreshed;
    /** Used for active entity operations. */
    @Generated(hash = 1828674899)
    private transient CurrencyAmountDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    @Generated(hash = 906193559)
    public CurrencyAmount(Long id, double amount) {
        this.id = id;
        this.amount = amount;
    }
    @Generated(hash = 1218078350)
    public CurrencyAmount() {
    }
    public Currency getOwnCurrency() {
        return currency;
    }
    public void setOwnCurrency(Currency currency) {
        this.currency = currency;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
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
    @Generated(hash = 509189523)
    public void setCurrency(Currency currency) {
        synchronized (this) {
            this.currency = currency;
            currency__refreshed = true;
        }
    }
    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 257181933)
    public Currency peakCurrency() {
        return currency;
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 878915352)
    public Currency getCurrency() {
        if (currency != null || !currency__refreshed) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CurrencyDao targetDao = daoSession.getCurrencyDao();
            targetDao.refresh(currency);
            currency__refreshed = true;
        }
        return currency;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 124921682)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCurrencyAmountDao() : null;
    }
}
