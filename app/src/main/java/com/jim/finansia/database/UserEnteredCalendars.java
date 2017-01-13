package com.jim.finansia.database;

import com.jim.finansia.database.convertors.CalendarConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Calendar;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by root on 10/12/16.
 */
@Entity
public class UserEnteredCalendars {
    @Property
    @Id
    private Long id;
    @Convert(converter = CalendarConvertor.class, columnType = String.class)
    private Calendar calendar;
    @Property
    private String currencyId;
    public String getCurrencyId() {
        return this.currencyId;
    }
    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }
    public Calendar getCalendar() {
        return this.calendar;
    }
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 1845832029)
    public UserEnteredCalendars(Long id, Calendar calendar, String currencyId) {
        this.id = id;
        this.calendar = calendar;
        this.currencyId = currencyId;
    }
    @Generated(hash = 443835265)
    public UserEnteredCalendars() {
    }
    

}
