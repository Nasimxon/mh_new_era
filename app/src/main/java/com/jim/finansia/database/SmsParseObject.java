package com.jim.finansia.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.List;
import java.util.UUID;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "SMS_PARSE_OBJECTS", active = true)
public class SmsParseObject {
    @Property
    @Id
    private String id;
    @Property
    private String number = "";
    @Property
    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "parseObjectId")
    })
    private List<TemplateSms> templates;
    @Property
    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "smsParseObjectId")
    })
    private List<SmsParseSuccess> successList;
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
    @Generated(hash = 1170963677)
    private transient String currency__resolvedKey;
    @Generated(hash = 1221310859)
    private transient String account__resolvedKey;
    /** Used for active entity operations. */
    @Generated(hash = 645439137)
    private transient SmsParseObjectDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    @Keep
    public SmsParseObject() {
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
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 678356865)
    public synchronized void resetSuccessList() {
        successList = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 315779555)
    public List<SmsParseSuccess> getSuccessList() {
        if (successList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SmsParseSuccessDao targetDao = daoSession.getSmsParseSuccessDao();
            List<SmsParseSuccess> successListNew = targetDao._querySmsParseObject_SuccessList(id);
            synchronized (this) {
                if(successList == null) {
                    successList = successListNew;
                }
            }
        }
        return successList;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1036578811)
    public synchronized void resetTemplates() {
        templates = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 922169954)
    public List<TemplateSms> getTemplates() {
        if (templates == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TemplateSmsDao targetDao = daoSession.getTemplateSmsDao();
            List<TemplateSms> templatesNew = targetDao._querySmsParseObject_Templates(id);
            synchronized (this) {
                if(templates == null) {
                    templates = templatesNew;
                }
            }
        }
        return templates;
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
    @Generated(hash = 1939383941)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getSmsParseObjectDao() : null;
    }
    @Generated(hash = 634174835)
    public SmsParseObject(String id, String number, String accountId,
            String currencyId) {
        this.id = id;
        this.number = number;
        this.accountId = accountId;
        this.currencyId = currencyId;
    }
}
