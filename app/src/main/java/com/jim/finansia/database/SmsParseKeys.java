package com.jim.finansia.database;

import com.jim.finansia.utils.ListConvertor;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;

import java.util.UUID;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by root on 9/29/16.
 */
@Entity
public class SmsParseKeys {
    @Property
    private String number;
    @Property
    private int type;
    @Convert(converter = ListConvertor.class, columnType = String.class)
    private String[] templates;
    @Property
    @Id
    private String id;
    @Keep
    public SmsParseKeys () {
        id = UUID.randomUUID().toString();
    }
    @Keep
    public SmsParseKeys(String number) {
        id = UUID.randomUUID().toString();
        this.number = number;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String[] getTemplates() {
        return this.templates;
    }
    public void setTemplates(String[] templates) {
        this.templates = templates;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    @Generated(hash = 1461144964)
    public SmsParseKeys(String number, int type, String[] templates, String id) {
        this.number = number;
        this.type = type;
        this.templates = templates;
        this.id = id;
    }
}
