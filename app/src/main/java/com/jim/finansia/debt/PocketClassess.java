package com.jim.finansia.debt;

import com.jim.finansia.fragments.AccountEditFragment;
import com.jim.finansia.fragments.AccountFragment;
import com.jim.finansia.fragments.AccountInfoFragment;
import com.jim.finansia.fragments.AddAutoMarketFragment;
import com.jim.finansia.fragments.AddCreditFragment;
import com.jim.finansia.fragments.AddSmsParseFragment;
import com.jim.finansia.fragments.AutoMarketFragment;
import com.jim.finansia.fragments.CategoryFragment;
import com.jim.finansia.fragments.CategoryInfoFragment;
import com.jim.finansia.fragments.ChangeColorOfStyleFragment;
import com.jim.finansia.fragments.CreditTabLay;
import com.jim.finansia.fragments.CurrencyChooseFragment;
import com.jim.finansia.fragments.CurrencyEditFragment;
import com.jim.finansia.fragments.CurrencyFragment;
import com.jim.finansia.fragments.InfoCreditFragment;
import com.jim.finansia.fragments.InfoCreditFragmentForArchive;
import com.jim.finansia.fragments.PurposeEditFragment;
import com.jim.finansia.fragments.PurposeFragment;
import com.jim.finansia.fragments.PurposeInfoFragment;
import com.jim.finansia.fragments.RecordDetailFragment;
import com.jim.finansia.fragments.RecordEditFragment;
import com.jim.finansia.fragments.ReportByAccountFragment;
import com.jim.finansia.fragments.ReportByCategoryFragment;
import com.jim.finansia.fragments.ReportByIncomeExpenseDaily;
import com.jim.finansia.fragments.ReportByIncomeExpenseDailyTableFragment;
import com.jim.finansia.fragments.ReportByIncomeExpenseMonthlyFragment;
import com.jim.finansia.fragments.ReportFragment;
import com.jim.finansia.fragments.RootCategoryEditFragment;
import com.jim.finansia.fragments.SMSParseInfoFragment;
import com.jim.finansia.fragments.ScheduleCreditFragment;
import com.jim.finansia.fragments.SearchFragment;
import com.jim.finansia.fragments.SmsParseMainFragment;
import com.jim.finansia.fragments.TableBarFragment;

/**
 * Created by user on 6/16/2016.
 */

public interface PocketClassess {
    // Records
    String RECORD_EDIT_FRAGMENT = RecordEditFragment.class.getName();
    String RECORD_DETEIL_FRAGMENT = RecordDetailFragment.class.getName();

    // Currency
    String CURRENCY_FRAG = CurrencyFragment.class.getName();
    String CURRENCY_CHOOSE = CurrencyChooseFragment.class.getName();
    String CURRENCY_EDIT = CurrencyEditFragment.class.getName();

    // Category
    String CATEGORY_FRAG = CategoryFragment.class.getName();
    String CATEGORY_INFO = CategoryInfoFragment.class.getName();
    String ADD_CATEGORY = RootCategoryEditFragment.class.getName();

    // Account
    String ACCOUNT_FRAG = AccountFragment.class.getName();
    String ACCOUNT_EDIT = AccountEditFragment.class.getName();
    String ACCOUNT_INFO = AccountInfoFragment.class.getName();

    // Auto Market
    String AUTOMARKET_FRAG = AutoMarketFragment.class.getName();
    String ADD_AUTOMARKET = AddAutoMarketFragment.class.getName();

    // Credit
    String CREDIT_FRAG = CreditTabLay.class.getName();
    String INFO_CREDIT= InfoCreditFragment.class.getName();
    String INFO_CREDIT_ARCHIVE= InfoCreditFragmentForArchive.class.getName();
    String ADD_CREDIT = AddCreditFragment.class.getName();
    String CREDIT_SCHEDULE = ScheduleCreditFragment.class.getName();


    // Debt - Borrow
    String DEBTBORROW_FRAG = DebtBorrowFragment.class.getName();
    String ADD_DEBTBORROW = AddBorrowFragment.class.getName();
    String INFO_DEBTBORROW = InfoDebtBorrowFragment.class.getName();

    // Purpose
    String PURPOSE_FRAG = PurposeFragment.class.getName();
    String INFO_PURPOSE = PurposeInfoFragment.class.getName();
    String ADD_PURPOSE = PurposeEditFragment.class.getName();

    // Report by Account
    String REPORT_ACCOUNT = ReportByAccountFragment.class.getName();
    String REPORT_CATEGORY = ReportByCategoryFragment.class.getName();
    String REPORT_BY_INCOME_EXPANCE = TableBarFragment.class.getName();
    String REPORT = ReportFragment.class.getName();
    String REPORT_DAILY_TABLE = ReportByIncomeExpenseDailyTableFragment.class.getName();
    String REPORT_DAILY = ReportByIncomeExpenseDaily.class.getName();
    String REPORT_MONTHLY = ReportByIncomeExpenseMonthlyFragment.class.getName();

    // Sms Parsing
    String SMS_PARSE_FRAGMENT = SmsParseMainFragment.class.getName();
    String ADD_SMS_PARSE_FRAGMENT = AddSmsParseFragment.class.getName();
    String INFO_SMS_PARSE_FRAGMENT = SMSParseInfoFragment.class.getName();

    // Searching
    String SEARCH_FRAGMENT = SearchFragment.class.getName();
    // Themes
    String THEMES = ChangeColorOfStyleFragment.class.getName();
}
