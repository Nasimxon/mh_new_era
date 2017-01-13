package com.jim.finansia.database;

import com.jim.finansia.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Calendar;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by root on 10/12/16.
 */
@Entity(nameInDb = "CURRENCY_COST_STATE", active = true)
public class CurrencyCostState {
    @Id
    @Property
    private Long id;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar day;
    @Property
    private String mainCurId;
    @Property
    @ToOne(joinProperty = "mainCurId")
    private Currency mainCurrency;
    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "parentId")
    })
    private List<CurrencyWithAmount> currencyWithAmountList;
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
    @Generated(hash = 356737629)
    public synchronized void resetCurrencyWithAmountList() {
        currencyWithAmountList = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 878111117)
    public List<CurrencyWithAmount> getCurrencyWithAmountList() {
        if (currencyWithAmountList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CurrencyWithAmountDao targetDao = daoSession.getCurrencyWithAmountDao();
            List<CurrencyWithAmount> currencyWithAmountListNew = targetDao._queryCurrencyCostState_CurrencyWithAmountList(id);
            synchronized (this) {
                if(currencyWithAmountList == null) {
                    currencyWithAmountList = currencyWithAmountListNew;
                }
            }
        }
        return currencyWithAmountList;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 887622709)
    public void setMainCurrency(Currency mainCurrency) {
        synchronized (this) {
            this.mainCurrency = mainCurrency;
            mainCurId = mainCurrency == null ? null : mainCurrency.getId();
            mainCurrency__resolvedKey = mainCurId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1454685319)
    public Currency getMainCurrency() {
        String __key = this.mainCurId;
        if (mainCurrency__resolvedKey == null || mainCurrency__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CurrencyDao targetDao = daoSession.getCurrencyDao();
            Currency mainCurrencyNew = targetDao.load(__key);
            synchronized (this) {
                mainCurrency = mainCurrencyNew;
                mainCurrency__resolvedKey = __key;
            }
        }
        return mainCurrency;
    }
    @Generated(hash = 737556193)
    private transient String mainCurrency__resolvedKey;
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 213189674)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCurrencyCostStateDao() : null;
    }
    /** Used for active entity operations. */
    @Generated(hash = 1837569904)
    private transient CurrencyCostStateDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    public String getMainCurId() {
        return this.mainCurId;
    }
    public void setMainCurId(String mainCurId) {
        this.mainCurId = mainCurId;
    }
    public Calendar getDay() {
        return this.day;
    }
    public void setDay(Calendar day) {
        this.day = day;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 216855175)
    public CurrencyCostState(Long id, Calendar day, String mainCurId) {
        this.id = id;
        this.day = day;
        this.mainCurId = mainCurId;
    }
    @Generated(hash = 1365894125)
    public CurrencyCostState() {
    }

}
