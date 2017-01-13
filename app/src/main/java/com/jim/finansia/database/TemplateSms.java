package com.jim.finansia.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

import java.util.UUID;

/**
 * Created by root on 10/2/16.
 */
@Entity
public class TemplateSms {
    @Property
    private String id;
    @Property
    private String parseObjectId;
    @Property
    private String regex;
    @Property
    private int type;
    @Property
    private int posAmountGroup;
    @Property
    private int posAmountGroupSecond;
    @Keep
    public TemplateSms(String regex, int type, int posAmountGroup) {
        id = UUID.randomUUID().toString();
        this.regex = regex;
        this.type = type;
        this.posAmountGroup = posAmountGroup;
    }
    public int getPosAmountGroupSecond() {
        return this.posAmountGroupSecond;
    }
    public void setPosAmountGroupSecond(int posAmountGroupSecond) {
        this.posAmountGroupSecond = posAmountGroupSecond;
    }
    public int getPosAmountGroup() {
        return this.posAmountGroup;
    }
    public void setPosAmountGroup(int posAmountGroup) {
        this.posAmountGroup = posAmountGroup;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getRegex() {
        return this.regex;
    }
    public void setRegex(String regex) {
        this.regex = regex;
    }
    public String getParseObjectId() {
        return this.parseObjectId;
    }
    public void setParseObjectId(String parseObjectId) {
        this.parseObjectId = parseObjectId;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @Generated(hash = 2087317679)
    public TemplateSms(String id, String parseObjectId, String regex, int type,
            int posAmountGroup, int posAmountGroupSecond) {
        this.id = id;
        this.parseObjectId = parseObjectId;
        this.regex = regex;
        this.type = type;
        this.posAmountGroup = posAmountGroup;
        this.posAmountGroupSecond = posAmountGroupSecond;
    }
    @Keep
    public TemplateSms() {
        id = UUID.randomUUID().toString();
    }
}
