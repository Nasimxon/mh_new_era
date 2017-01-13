package com.jim.finansia.utils.record;

/**
 * Created by root on 9/22/16.
 */
public class IconWithName {
    private String icon, name, id;
    public IconWithName(String icon, String name, String id) {
        this.icon = icon;
        this.name = name;
        this.id = id;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
