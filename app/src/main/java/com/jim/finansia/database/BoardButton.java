package com.jim.finansia.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by DEV on 28.08.2016.
 */
@Entity(nameInDb = "BOARD_BUTTONS", active = true)
public class BoardButton {
    @Id
    @Property
    private Long id;
    @Property
    private String categoryId;
    @Property
    private int table;
    @Property
    private int pos;
    @Property
    private int type;
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
    /** Used for active entity operations. */
    @Generated(hash = 363839140)
    private transient BoardButtonDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getPos() {
        return this.pos;
    }
    public void setPos(int pos) {
        this.pos = pos;
    }
    public int getTable() {
        return this.table;
    }
    public void setTable(int table) {
        this.table = table;
    }
    public String getCategoryId() {
        return this.categoryId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1356069910)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBoardButtonDao() : null;
    }
    @Generated(hash = 356784743)
    public BoardButton(Long id, String categoryId, int table, int pos, int type) {
        this.id = id;
        this.categoryId = categoryId;
        this.table = table;
        this.pos = pos;
        this.type = type;
    }
    @Generated(hash = 1734855031)
    public BoardButton() {
    }
}
