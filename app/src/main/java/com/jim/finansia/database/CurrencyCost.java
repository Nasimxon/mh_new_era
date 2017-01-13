package com.jim.finansia.database;

import java.util.Calendar;

public class CurrencyCost {
	private double cost;
	private Calendar day;

	public CurrencyCost() {
	}

	public CurrencyCost(double cost, Calendar day) {
		this.cost = cost;
		this.day = day;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public Calendar getDay() {
		return day;
	}

	public void setDay(Calendar day) {
		this.day = day;
	}
}

