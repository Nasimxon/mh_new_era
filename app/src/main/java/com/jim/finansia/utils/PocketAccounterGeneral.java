package com.jim.finansia.utils;

import com.jim.finansia.BuildConfig;

public class PocketAccounterGeneral {
	public static final int PAGES_MAX_COUNT = 10;
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

	public static final String INFO_VISIBILITY = "INFO_VISIBILITY", VERTICAL_SELECTED_PAGE = "MAIN_SELECTED_PAGE";
	public static final String HEAD_COLOR = "HEAD_COLOR", HELPER_COLOR = "HELPER_COLOR";
	public static final String BASE64RSA = BuildConfig.base64rsa;
	public static final String CHOOSEN_THEME_NAME_KEY = "CHOOSEN_THEME_NAME_KEY";
	public class MoneyHolderSkus {
		public static final String SMS_PARSING_SKU = BuildConfig.sms_parsing_sku;
		public static final String ZERO_PAGE_SKU = BuildConfig.zero_page_sku;
		public static final String FIRST_PAGE_SKU = BuildConfig.first_page_sku;
		public static final String SECOND_PAGE_SKU = BuildConfig.second_page_sku;
		public static final String THIRD_PAGE_SKU = BuildConfig.third_page_sku;
		public static final String FOURTH_PAGE_SKU = BuildConfig.fourth_page_sku;
		public static final String FIFTH_PAGE_SKU = BuildConfig.fifth_page_sku;
		public static final String SIXTH_PAGE_SKU = BuildConfig.sixth_page_sku;
		public static final String SEVENTH_PAGE_SKU = BuildConfig.seventh_page_sku;
		public static final String EIGHTH_PAGE_SKU = BuildConfig.eighth_page_sku;
		public static final String NINTH_PAGE_SKU = BuildConfig.ninth_page_sku;
		public static final String ADD_REPLACE_CATEGORY_ON_MAIN_BOARD_SKU = BuildConfig.add_replace_category_on_main_board_sku;
		public static final String ADD_REPLACE_DEBT_BORROW_ON_MAIN_BOARD_SKU = BuildConfig.add_replace_debt_borrow_on_main_board_sku;
		public static final String ADD_REPLACE_CREDIT_ON_MAIN_BOARD_SKU = BuildConfig.add_replace_credit_on_main_board_sku;
		public static final String ADD_REPLACE_FUNCTION_ON_MAIN_BOARD_SKU = BuildConfig.add_replace_function_on_main_board_sku;
		public static final String ADD_REPLACE_PAGE_ON_MAIN_BOARD_SKU = BuildConfig.add_replace_page_on_main_board_sku;
		public static final String VOICE_RECOGNITION_SKU = BuildConfig.voice_recognition_sku;
		public class SkuPreferenceKeys {
			public static final String SMS_PARSING_COUNT_KEY = BuildConfig.SMS_PARSING_COUNT_KEY;
			public static final String ZERO_PAGE_COUNT_KEY = BuildConfig.ZERO_PAGE_COUNT_KEY;
			public static final String FIRST_PAGE_COUNT_KEY = BuildConfig.FIRST_PAGE_COUNT_KEY;
			public static final String SECOND_PAGE_COUNT_KEY = BuildConfig.SECOND_PAGE_COUNT_KEY;
			public static final String THIRD_PAGE_COUNT_KEY = BuildConfig.THIRD_PAGE_COUNT_KEY;
			public static final String FOURTH_PAGE_COUNT_KEY = BuildConfig.FOURTH_PAGE_COUNT_KEY;
			public static final String FIFTH_PAGE_COUNT_KEY = BuildConfig.FIFTH_PAGE_COUNT_KEY;
			public static final String SIXTH_PAGE_COUNT_KEY = BuildConfig.SIXTH_PAGE_COUNT_KEY;
			public static final String SEVENTH_PAGE_COUNT_KEY = BuildConfig.SEVENTH_PAGE_COUNT_KEY;
			public static final String EIGHTH_PAGE_COUNT_KEY = BuildConfig.EIGHTH_PAGE_COUNT_KEY;
			public static final String NINTH_PAGE_COUNT_KEY = BuildConfig.NINTH_PAGE_COUNT_KEY;
			public static final String IS_AVAILABLE_CHANGING_OF_CATEGORY_KEY = BuildConfig.IS_AVAILABLE_CHANGING_OF_CATEGORY_KEY;
			public static final String IS_AVAILABLE_CHANGING_OF_DEBT_BORROW_KEY = BuildConfig.IS_AVAILABLE_CHANGING_OF_DEBT_BORROW_KEY;
			public static final String IS_AVAILABLE_CHANGING_OF_CREDIT_KEY = BuildConfig.IS_AVAILABLE_CHANGING_OF_CREDIT_KEY;
			public static final String IS_AVAILABLE_CHANGING_OF_FUNCTION = BuildConfig.IS_AVAILABLE_CHANGING_OF_FUNCTION;
			public static final String IS_AVAILABLE_CHANGING_OF_PAGE = BuildConfig.IS_AVAILABLE_CHANGING_OF_PAGE;
			public static final String VOICE_RECOGNITION_KEY = BuildConfig.VOICE_RECOGNITION_KEY;
			public static final String VOICE_RECOGNITION_COUNT = "VOICE_RECOGNITION_COUNT";
			public static final String BLUE_THEME = BuildConfig.BLUE_THEME;
			public static final String FIOLA_THEME = BuildConfig.FIOLA_THEME;
			public static final String YELLOW_THEME = BuildConfig.YELLOW_THEME;


		}
	}
}
