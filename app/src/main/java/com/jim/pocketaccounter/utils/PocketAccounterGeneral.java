package com.jim.pocketaccounter.utils;

import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.RootCategory;

import java.util.Calendar;

public class PocketAccounterGeneral {
	public static final String OLD_DB_NAME = "PocketAccounterDatabase", CURRENT_DB_NAME = "pocketaccounter-db";
	public static final int NORMAL_MODE = 0;
	public static final int EDIT_MODE = 1;
	public static final int INCOME=0, EXPENSE =1, TRANSFER = 2;
	public static final int CATEGORY = 0, CREDIT = 1, DEBT_BORROW = 2, FUNCTION = 3, PAGE = 4, NULL = 100;

	public static final int EXPENSE_BUTTONS_COUNT = 16;
	public static final int INCOME_BUTTONS_COUNT = 4;
	public static final int NO_MODE = 0, EXPANSE_MODE = 1, INCOME_MODE = 2;
	public static final int MAIN = 0, DETAIL = 1;
	public static final int SMS_ONLY_EXPENSE = 0, SMS_ONLY_INCOME = 1, SMS_BOTH = 2;
	public static final String EVERY_DAY="EVERY_DAY", EVERY_WEEK="EVERY_WEEK", EVERY_MONTH="EVERY_MONTH";
	public static final String TAG = "sss";
	public static final String DB_ONCREATE_ENTER = "DB_ONCREATE_ENTER";
	public static final String INCOMES = "INCOME", EXPENSES = "EXPENSE", BALANCE = "BALANCE";
	//top board buttons types
	public static final int UP_TOP_LEFT = 0, UP_SIMPLE = 1, UP_LEFT_SIMPLE = 2, UP_LEFT_BOTTOM = 3, UP_BOTTOM_SIMPLE = 4,
			UP_BOTTOM_RIGHT = 5, UP_TOP_RIGHT = 6, UP_TOP_SIMPLE = 7, UP_RIGHT_SIMPLE = 8,
			UP_TOP_LEFT_PRESSED = 9, UP_SIMPLE_PRESSED = 10, UP_LEFT_SIMPLE_PRESSED = 11, UP_LEFT_BOTTOM_PRESSED = 12,
			UP_BOTTOM_SIMPLE_PRESSED = 13,	UP_BOTTOM_RIGHT_PRESSED = 14, UP_TOP_RIGHT_PRESSED = 15, UP_TOP_SIMPLE_PRESSED = 16,
			UP_RIGHT_SIMPLE_PRESSED = 17, UP_WORKSPACE_SHADER = 18, ICONS_NO_CATEGORY = 19,
			DOWN_WORKSPACE_SHADER = 20, DOWN_MOST_LEFT = 21, DOWN_SIMPLE = 22, DOWN_MOST_RIGHT = 23,
			DOWN_MOST_LEFT_PRESSED = 24, DOWN_SIMPLE_PRESSED = 25, DOWN_MOST_RIGHT_PRESSED = 26;

	public static final String HEAD_COLOR = "HEAD_COLOR", HELPER_COLOR = "HELPER_COLOR";
}
