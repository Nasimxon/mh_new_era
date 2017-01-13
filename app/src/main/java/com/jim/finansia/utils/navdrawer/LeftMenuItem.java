package com.jim.finansia.utils.navdrawer;

public class LeftMenuItem {
	private String titleName="";
	private int iconId=0;
	public LeftMenuItem(String titleName, int icon) {
		this.titleName = titleName;
		this.setIconId(icon);
	}
	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}
	public String getTitleName() {
		return titleName;
	}
	public int getIconId() {
		return iconId;
	}
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
}
