package com.jim.finansia.database;

import android.support.annotation.Keep;

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
import java.util.UUID;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "DEBT_BORROWS", active = true)
public class DebtBorrow {
    @Property
    private String perId;
    @ToOne(joinProperty = "perId")
    private Person person;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar takenDate;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar returnDate;
    @Property
    private int type;
    @Property
    private String accountId;
    @ToOne(joinProperty = "accountId")
    private Account account;
    @Property
    private String currencyId;
    @ToOne(joinProperty = "currencyId")
    private Currency currency;
    @Property
    private boolean calculate;
    @Property
    private boolean to_archive = false;
    @Property
    private double amount;
    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "debtBorrowsId")
    })
    private List<Recking> reckings;
    public static final int DEBT = 1, BORROW = 0;
    private String info = "";
    @Property
    @Id
    private String id; //"debt_"+UUID.randowUUID().toString();
    @Generated(hash = 1170963677)
    private transient String currency__resolvedKey;
    @Generated(hash = 1221310859)
    private transient String account__resolvedKey;
    @Generated(hash = 1979224670)
    private transient String person__resolvedKey;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1007809152)
    private transient DebtBorrowDao myDao;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    @Keep
    public DebtBorrow() {
        id = UUID.randomUUID().toString();
    }

    @Keep
    public DebtBorrow(Person person, Calendar takenDate,
                      Calendar returnDate, String id,
                      Account account, Currency currency,
                      double amount, int type, boolean calculate) {
        setPerson(person);
        this.takenDate = takenDate;
        this.returnDate = returnDate;
        this.id = id;
        if(account!=null)
        this.accountId = account.getId();
        else  this.accountId = "";

        this.currencyId = currency.getId();
        this.amount = amount;
        this.type = type;
        this.calculate = calculate;
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

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1034520580)
    public synchronized void resetReckings() {
        reckings = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1906484870)
    public List<Recking> getReckings() {
        if (reckings == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ReckingDao targetDao = daoSession.getReckingDao();
            List<Recking> reckingsNew = targetDao._queryDebtBorrow_Reckings(id);
            synchronized (this) {
                if (reckings == null) {
                    reckings = reckingsNew;
                }
            }
        }
        return reckings;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1889019422)
    public void setCurrency(Currency currency) {
        synchronized (this) {
            this.currency = currency;
            currencyId = currency == null ? null : currency.getId();
            currency__resolvedKey = currencyId;
        }
    }

    /**
     * To-one relationship, resolved on first access.
     */
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1910176546)
    public void setAccount(Account account) {
        synchronized (this) {
            this.account = account;
            accountId = account == null ? null : account.getId();
            account__resolvedKey = accountId;
        }
    }

    /**
     * To-one relationship, resolved on first access.
     */
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 2049612581)
    public void setPerson(Person person) {
        synchronized (this) {
            this.person = person;
            perId = person == null ? null : person.getId();
            person__resolvedKey = perId;
        }
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 821346317)
    public Person getPerson() {
        String __key = this.perId;
        if (person__resolvedKey == null || person__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PersonDao targetDao = daoSession.getPersonDao();
            Person personNew = targetDao.load(__key);
            synchronized (this) {
                person = personNew;
                person__resolvedKey = __key;
            }
        }
        return person;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean getTo_archive() {
        return this.to_archive;
    }

    public void setTo_archive(boolean to_archive) {
        this.to_archive = to_archive;
    }

    public boolean getCalculate() {
        return this.calculate;
    }

    public void setCalculate(boolean calculate) {
        this.calculate = calculate;
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

    public Calendar getReturnDate() {
        return this.returnDate;
    }

    public void setReturnDate(Calendar returnDate) {
        this.returnDate = returnDate;
    }

    public Calendar getTakenDate() {
        return this.takenDate;
    }

    public void setTakenDate(Calendar takenDate) {
        this.takenDate = takenDate;
    }

    public String getPerId() {
        return this.perId;
    }

    public void setPerId(String perId) {
        this.perId = perId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 861691792)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDebtBorrowDao() : null;
    }

    @Generated(hash = 439704250)
    public DebtBorrow(String perId, Calendar takenDate, Calendar returnDate, int type,
            String accountId, String currencyId, boolean calculate, boolean to_archive,
            double amount, String info, String id) {
        this.perId = perId;
        this.takenDate = takenDate;
        this.returnDate = returnDate;
        this.type = type;
        this.accountId = accountId;
        this.currencyId = currencyId;
        this.calculate = calculate;
        this.to_archive = to_archive;
        this.amount = amount;
        this.info = info;
        this.id = id;
    }
}