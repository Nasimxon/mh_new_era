package com.jim.finansia.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Transient;


@Entity(nameInDb = "CURRENCIES", active = true)
public class Currency {
	@Property
	private String name;
	@Property
	private String abbr;
	@Id
	@Property
	private String id;
	@Property
	private boolean isMain = false;
	@ToMany(joinProperties = {
			@JoinProperty(name = "id", referencedName = "currencyId")
	})
	private List<UserEnteredCalendars> userEnteredCalendarses;
	@Transient
	private List<CurrencyCost> costs;
	/** Used for active entity operations. */
	@Generated(hash = 1033120508)
	private transient CurrencyDao myDao;
	/** Used to resolve relations */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	@Generated(hash = 2048053614)
	public Currency(String name, String abbr, String id, boolean isMain) {
		this.name = name;
		this.abbr = abbr;
		this.id = id;
		this.isMain = isMain;
	}
	@Generated(hash = 1387171739)
	public Currency() {
	}
	@Keep
	public List<CurrencyCost> getCosts() {
		if (costs == null) {
			costs = new ArrayList<>();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
			for (UserEnteredCalendars userCalendar : getUserEnteredCalendarses()) {
				List<Currency> currencies = daoSession
						.queryBuilder(Currency.class)
						.where(CurrencyDao.Properties.IsMain.eq(true))
						.list();
				Currency mainCurrency = currencies.get(0);
				List<CurrencyCostState> mainState = daoSession
						.queryBuilder(CurrencyCostState.class)
						.where(CurrencyCostStateDao.Properties.Day.eq(simpleDateFormat.format(userCalendar.getCalendar().getTime())),
								CurrencyCostStateDao.Properties.MainCurId.eq(mainCurrency.getId()))
						.list();
				if (mainState.isEmpty()) {
					double cost = 1.0;
					CurrencyCost currencyCost = new CurrencyCost(cost, userCalendar.getCalendar());
					costs.add(currencyCost);
				} else {
					double cost = 1.0d;
					for (CurrencyWithAmount withAmount : mainState.get(0).getCurrencyWithAmountList()) {
						if (withAmount.getCurrencyId().equals(getId())) {
							cost = withAmount.getAmount();
							break;
						}
					}
					CurrencyCost currencyCost = new CurrencyCost(cost, userCalendar.getCalendar());
					costs.add(currencyCost);
				}
			}
			Collections.sort(costs, new Comparator<CurrencyCost>() {
				@Override
				public int compare(CurrencyCost lhs, CurrencyCost rhs) {
					return lhs.getDay().compareTo(rhs.getDay());
				}
			});
		}
		return costs;
	}

	@Keep
	public void refreshCosts() {
		costs = null;
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
	@Generated(hash = 1792366061)
	public synchronized void resetUserEnteredCalendarses() {
		userEnteredCalendarses = null;
	}
	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Keep
	public List<UserEnteredCalendars> getUserEnteredCalendarses() {
	    if (userEnteredCalendarses == null) {
	        final DaoSession daoSession = this.daoSession;
	        if (daoSession == null) {
	            throw new DaoException("Entity is detached from DAO context");
	        }
	        UserEnteredCalendarsDao targetDao = daoSession.getUserEnteredCalendarsDao();
	        List<UserEnteredCalendars> userEnteredCalendarsesNew = targetDao._queryCurrency_UserEnteredCalendarses(id);
	        synchronized (this) {
	            if(userEnteredCalendarses == null) {
	                userEnteredCalendarses = userEnteredCalendarsesNew;
	            }
	        }
	    }
		Collections.sort(userEnteredCalendarses, new Comparator<UserEnteredCalendars>() {
			@Override
			public int compare(UserEnteredCalendars lhs, UserEnteredCalendars rhs) {
				return lhs.getCalendar().compareTo(rhs.getCalendar());
			}
		});
	    return userEnteredCalendarses;
	}
	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 869658167)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getCurrencyDao() : null;
	}
	public boolean getMain() {
		return this.isMain;
	}
	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAbbr() {
		return this.abbr;
	}
	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean getIsMain() {
		return this.isMain;
	}
	public void setIsMain(boolean isMain) {
		this.isMain = isMain;
	}
}
