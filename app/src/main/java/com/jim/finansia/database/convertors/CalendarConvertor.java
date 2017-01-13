package com.jim.finansia.database.convertors;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by DEV on 27.08.2016.
 */

public class CalendarConvertor implements PropertyConverter<Calendar, String> {
    SimpleDateFormat dateformarter=new  SimpleDateFormat("dd.MM.yyyy");
    @Override
    public Calendar convertToEntityProperty(String databaseValue) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateformarter.parse(databaseValue));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    @Override
    public String convertToDatabaseValue(Calendar entityProperty) {
        return dateformarter.format(entityProperty.getTime());
    }
}
