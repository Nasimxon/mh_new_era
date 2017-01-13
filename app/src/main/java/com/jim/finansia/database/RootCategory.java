package com.jim.finansia.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "ROOT_CATEGORIES", active = true)
public class RootCategory {
	@Property
	private String name;
	@Id
	@Property
	private String id;
	@Property
	private String icon;
	@Property
	private int type;
	@ToMany(joinProperties = {
			@JoinProperty(
					name = "id", referencedName = "parentId"
			)
	})
	@NotNull
	private List<SubCategory> subCategories;
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
	@Generated(hash = 832942771)
	public synchronized void resetSubCategories() {
		subCategories = null;
	}
	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1519955387)
	public List<SubCategory> getSubCategories() {
	    if (subCategories == null) {
	        final DaoSession daoSession = this.daoSession;
	        if (daoSession == null) {
	            throw new DaoException("Entity is detached from DAO context");
	        }
	        SubCategoryDao targetDao = daoSession.getSubCategoryDao();
	        List<SubCategory> subCategoriesNew = targetDao._queryRootCategory_SubCategories(id);
	        synchronized (this) {
	            if(subCategories == null) {
	                subCategories = subCategoriesNew;
	            }
	        }
	    }
	    return subCategories;
	}

	public void setSubCategories(List<SubCategory> subCategories) {
		this.subCategories = subCategories;
	}

	/** Used for active entity operations. */
	@Generated(hash = 1635362545)
	private transient RootCategoryDao myDao;
	/** Used to resolve relations */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	public int getType() {
		return this.type;
	}
	public void setType(int type) {
		this.type = type;
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
	@Generated(hash = 606159597)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getRootCategoryDao() : null;
	}
	@Generated(hash = 1463833150)
	public RootCategory(String name, String id, String icon, int type) {
		this.name = name;
		this.id = id;
		this.icon = icon;
		this.type = type;
	}
	@Generated(hash = 1043935042)
	public RootCategory() {
	}
}