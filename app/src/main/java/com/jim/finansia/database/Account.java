package com.jim.finansia.database;

import com.jim.finansia.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Calendar;
import java.util.UUID;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "ACCOUNTS", active = true)
public class Account {
	@Property
	private String name;
	@Id
	@Property
	private String id;
	@Property
	private String icon;
	@Property
	private double amount;
	@ToOne(joinProperty = "startMoneyCurrencyId")
	private Currency startMoneyCurrency;
	@Property
	private String startMoneyCurrencyId;
	@Convert(converter = CalendarConvertor.class, columnType = String.class)
	private Calendar calendar;
	@Property
	private boolean noneMinusAccount;
	@Property
	private double limite;
	@Property
	private boolean isLimited;
	@Property
	private String limitCurId;
	@ToOne (joinProperty = "limitCurId")
	private Currency currency;
	@Generated(hash = 1170963677)
	private transient String currency__resolvedKey;
	@Generated(hash = 1719102162)
	private transient String startMoneyCurrency__resolvedKey;
	/** Used for active entity operations. */
	@Generated(hash = 335469827)
	private transient AccountDao myDao;
	/** Used to resolve relations */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	@Keep
	public Account () {
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
	@Generated(hash = 2026330276)
	public void setCurrency(Currency currency) {
		synchronized (this) {
			this.currency = currency;
			limitCurId = currency == null ? null : currency.getId();
			currency__resolvedKey = limitCurId;
		}
	}
	/** To-one relationship, resolved on first access. */
	@Generated(hash = 560884125)
	public Currency getCurrency() {
		String __key = this.limitCurId;
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
	@Generated(hash = 1165649871)
	public void setStartMoneyCurrency(Currency startMoneyCurrency) {
		synchronized (this) {
			this.startMoneyCurrency = startMoneyCurrency;
			startMoneyCurrencyId = startMoneyCurrency == null ? null : startMoneyCurrency
					.getId();
			startMoneyCurrency__resolvedKey = startMoneyCurrencyId;
		}
	}
	/** To-one relationship, resolved on first access. */
	@Generated(hash = 1596678324)
	public Currency getStartMoneyCurrency() {
		String __key = this.startMoneyCurrencyId;
		if (startMoneyCurrency__resolvedKey == null
				|| startMoneyCurrency__resolvedKey != __key) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			CurrencyDao targetDao = daoSession.getCurrencyDao();
			Currency startMoneyCurrencyNew = targetDao.load(__key);
			synchronized (this) {
				startMoneyCurrency = startMoneyCurrencyNew;
				startMoneyCurrency__resolvedKey = __key;
			}
		}
		return startMoneyCurrency;
	}
	public String getLimitCurId() {
		return this.limitCurId;
	}
	public void setLimitCurId(String limitCurId) {
		this.limitCurId = limitCurId;
	}
	public boolean getIsLimited() {
		return this.isLimited;
	}
	public void setIsLimited(boolean isLimited) {
		this.isLimited = isLimited;
	}
	public double getLimite() {
		return this.limite;
	}
	public void setLimite(double limite) {
		this.limite = limite;
	}
	public boolean getNoneMinusAccount() {
		return this.noneMinusAccount;
	}
	public void setNoneMinusAccount(boolean noneMinusAccount) {
		this.noneMinusAccount = noneMinusAccount;
	}
	public Calendar getCalendar() {
		return this.calendar;
	}
	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}
	public String getStartMoneyCurrencyId() {
		return this.startMoneyCurrencyId;
	}
	public void setStartMoneyCurrencyId(String startMoneyCurrencyId) {
		this.startMoneyCurrencyId = startMoneyCurrencyId;
	}
	public double getAmount() {
		return this.amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getIcon() {
		return this.icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 1812283172)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getAccountDao() : null;
	}
	@Generated(hash = 984441795)
	public Account(String name, String id, String icon, double amount,
			String startMoneyCurrencyId, Calendar calendar, boolean noneMinusAccount,
			double limite, boolean isLimited, String limitCurId) {
		this.name = name;
		this.id = id;
		this.icon = icon;
		this.amount = amount;
		this.startMoneyCurrencyId = startMoneyCurrencyId;
		this.calendar = calendar;
		this.noneMinusAccount = noneMinusAccount;
		this.limite = limite;
		this.isLimited = isLimited;
		this.limitCurId = limitCurId;
	}
}