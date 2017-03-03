package com.jim.finansia.managers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.BoardButtonDao;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.Currency;
import com.jim.finansia.database.CurrencyCost;
import com.jim.finansia.database.CurrencyCostState;
import com.jim.finansia.database.CurrencyDao;
import com.jim.finansia.database.CurrencyWithAmount;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.Person;
import com.jim.finansia.database.PhotoDetails;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.RootCategoryDao;
import com.jim.finansia.database.SmsParseObject;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.database.TemplateAccount;
import com.jim.finansia.database.TemplateCurrencyVoice;
import com.jim.finansia.database.TemplateSms;
import com.jim.finansia.database.TemplateVoice;
import com.jim.finansia.database.UserEnteredCalendars;
import com.jim.finansia.utils.CostMigrateObject;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;
import com.jim.finansia.utils.regex.RegexBuilder;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.StandardDatabase;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by DEV on 01.09.2016.
 */

public class CommonOperations {
    @Inject DaoSession daoSession;
    private CurrencyDao currencyDao;
    private Context context;
    private Currency mainCurrency;
    @Inject @Named(value = "begin") Calendar begin;
    @Inject @Named(value = "end") Calendar end;
    @Inject SharedPreferences sharedPreferences;


    public CommonOperations(Context context) {
        ((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
        this.currencyDao = daoSession.getCurrencyDao();
        this.context = context;
    }

    public Currency getMainCurrency() {
        if (mainCurrency == null) {
            List<Currency> currencies = currencyDao.loadAll();
            for (Currency currency : currencies) {
                if (currency.getIsMain()) {
                    mainCurrency = currency;
                    break;
                }
            }
        }
        return mainCurrency;
    }

    public void refreshCurrency() {
        mainCurrency = null;
        currencyDao.detachAll();
        List<Currency> mainCurrencyList = currencyDao.queryBuilder().where(CurrencyDao.Properties.IsMain.eq(true)).list();
        if (mainCurrencyList != null && !mainCurrencyList.isEmpty()) {
            mainCurrency = mainCurrencyList.get(0);
        }
    }

    public double getCost(FinanceRecord record) {
        double amount = 0.0;
        if (record.getCurrency().getMain())
            return record.getAmount();
        double koeff = 1.0;
        long diff = record.getDate().getTimeInMillis() - record.getCurrency().getCosts().get(0).getDay().getTimeInMillis();
        if (diff < 0) {
            koeff = record.getCurrency().getCosts().get(0).getCost();
            return record.getAmount() * koeff;
        }
        int pos = 0;
        while (diff >= 0 && pos < record.getCurrency().getCosts().size()) {
            diff = record.getDate().getTimeInMillis() - record.getCurrency().getCosts().get(pos).getDay().getTimeInMillis();
            if (diff >= 0)
                koeff = record.getCurrency().getCosts().get(pos).getCost();
            pos++;
        }
        amount = record.getAmount() * koeff;
        return amount;
    }

    public double getCost(Calendar date, Currency currency, double amount) {
        if (currency.getMain()) return amount;
        double koeff = 1.0;
        long diff = date.getTimeInMillis() - currency.getCosts().get(0).getDay().getTimeInMillis();
        if (diff < 0) {
            koeff = currency.getCosts().get(0).getCost();
            return amount * koeff;
        }
        int pos = 0;
        while (diff >= 0 && pos < currency.getCosts().size()) {
            diff = date.getTimeInMillis() - currency.getCosts().get(pos).getDay().getTimeInMillis();
            if (diff >= 0)
                koeff = currency.getCosts().get(pos).getCost();
            pos++;
        }
        amount = amount * koeff;
        return amount;
    }

    public double getCost(Calendar date, Currency fromCurrency, Currency toCurrency, double amount) {
        if (fromCurrency.getId().equals(toCurrency.getId())) return amount;
        double fromKoeff = 1.0;
        double toKoeff = 1.0;
        if (!fromCurrency.getMain()) {
            long fromDiff = date.getTimeInMillis() - fromCurrency.getCosts().get(0).getDay().getTimeInMillis();
            if (fromDiff < 0) {
                fromKoeff = fromCurrency.getCosts().get(0).getCost();
            }
            int pos = 0;
            while (fromDiff >= 0 && pos < fromCurrency.getCosts().size()) {
                fromDiff = date.getTimeInMillis() - fromCurrency.getCosts().get(pos).getDay().getTimeInMillis();
                if (fromDiff >= 0)
                    fromKoeff = fromCurrency.getCosts().get(pos).getCost();
                pos++;
            }
        }
        if (!toCurrency.getMain()) {
            long toDiff = date.getTimeInMillis() - toCurrency.getCosts().get(0).getDay().getTimeInMillis();
            if (toDiff < 0) {
                toKoeff = toCurrency.getCosts().get(0).getCost();
            }
            int pos = 0;
            while (toDiff >= 0 && pos < toCurrency.getCosts().size()) {
                toDiff = date.getTimeInMillis() - toCurrency.getCosts().get(pos).getDay().getTimeInMillis();
                if (toDiff >= 0)
                    toKoeff = toCurrency.getCosts().get(pos).getCost();
                pos++;
            }
        }
        amount = fromKoeff * amount / toKoeff;
        return amount;
    }

    public float convertDpToPixel(float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public void ColorSubSeq(String text, String whichWordColor, String colorCode, TextView textView) {
        String textUpper = text.toUpperCase();
        String whichWordColorUpper = whichWordColor.toUpperCase();
        SpannableString ss = new SpannableString(text);
        int strar = 0;

        while (textUpper.indexOf(whichWordColorUpper, strar) >= 0 && whichWordColor.length() != 0) {
            ss.setSpan(new BackgroundColorSpan(Color.parseColor(colorCode)), textUpper.indexOf(whichWordColorUpper, strar), textUpper.indexOf(whichWordColorUpper, strar) + whichWordColorUpper.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            strar = textUpper.indexOf(whichWordColorUpper, strar) + whichWordColorUpper.length();
        }
        textView.setText(ss);
    }

    public long betweenDays(Calendar begin, Calendar end) {
        Calendar b = (Calendar) begin.clone();
        b.set(Calendar.HOUR_OF_DAY, 0);
        b.set(Calendar.MINUTE, 0);
        b.set(Calendar.SECOND, 0);
        b.set(Calendar.MILLISECOND, 0);
        Calendar e = (Calendar) end.clone();
        e.set(Calendar.HOUR_OF_DAY, 0);
        e.set(Calendar.MINUTE, 0);
        e.set(Calendar.SECOND, 0);
        e.set(Calendar.MILLISECOND, 0);
        long day = 24L * 60L * 60L * 1000L;
        return 1 + (e.getTimeInMillis() - b.getTimeInMillis()) / day;
    }


    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public List<TemplateSms> generateSmsTemplateList(List<String> splittedText, int incExpPos, int amountPos, List<String> incomeKeywords, List<String> expenseKeywords, List<String> amountKeywords) {
        List<TemplateSms> templates = new ArrayList<>();
        int amountBlockPos = 0;
        String regex;
        if (splittedText != null && !splittedText.isEmpty()) {
            //amountPos > incExpPos
            if (incExpPos == 0 && amountPos == 1 && splittedText.size() == 2) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .beginsWithWord(keyWord)
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .build();
                    amountBlockPos = 2;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .beginsWithWord(keyWord)
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .build();
                    amountBlockPos = 2;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (incExpPos == 1 && amountPos == 0 && splittedText.size() == 2) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .defineNumber()
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .build();
                    amountBlockPos = 1;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .defineNumber()
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .build();
                    amountBlockPos = 1;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            }
            else if (incExpPos == 0 && incExpPos + 1 == amountPos && amountPos != splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .beginsWithWord(keyWord)
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyWhitespaceSeq().anyVisibleCharSeq().closeGroup()
                            .build();
                    amountBlockPos = 2;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .beginsWithWord(keyWord)
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyWhitespaceSeq().anyVisibleCharSeq().closeGroup()
                            .build();
                    amountBlockPos = 2;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (incExpPos == 0 && incExpPos + 1 != amountPos && incExpPos < amountPos && amountPos != splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .beginsWithWord(keyWord)
                            .openGroup().anyWhitespaceSeq().anyVisibleCharSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyWhitespaceSeq().anyVisibleCharSeq().closeGroup()
                            .build();
                    amountBlockPos = 4;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .beginsWithWord(keyWord)
                            .openGroup().anyWhitespaceSeq().anyVisibleCharSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyWhitespaceSeq().anyVisibleCharSeq().closeGroup()
                            .build();
                    amountBlockPos = 4;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (incExpPos != 0 && incExpPos + 1 == amountPos && amountPos != splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 4;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 4;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (incExpPos != 0 && amountPos + 1 != incExpPos && incExpPos < amountPos && amountPos != splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 6;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 6;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (incExpPos != 0 && amountPos + 1 != incExpPos && incExpPos < amountPos && amountPos == splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .build();
                    amountBlockPos = 6;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .build();
                    amountBlockPos = 6;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (incExpPos != 0 && amountPos + 1 == incExpPos && amountPos == splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .build();
                    amountBlockPos = 4;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .build();
                    amountBlockPos = 4;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            }
            //amountPos < incExpPos
            else if (amountPos == 0 && amountPos + 1 == incExpPos && incExpPos != splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .defineNumber()
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 1;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .defineNumber()
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 1;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (amountPos == 0 && amountPos + 1 != incExpPos && incExpPos != splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 1;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 1;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (amountPos != 0 && amountPos + 1 == incExpPos && incExpPos != splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 3;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 3;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (amountPos != 0 && amountPos + 1 != incExpPos && amountPos < incExpPos && incExpPos != splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 3;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .build();
                    amountBlockPos = 3;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (amountPos != 0 && amountPos + 1 != incExpPos && incExpPos == splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .build();
                    amountBlockPos = 3;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(incExpPos - 1))
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .build();
                    amountBlockPos = 3;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            } else if (amountPos != 0 && amountPos + 1 == incExpPos && incExpPos == splittedText.size() - 1) {
                for (String keyWord : incomeKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .build();
                    amountBlockPos = 3;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                    templates.add(template);
                }
                for (String keyWord : expenseKeywords) {
                    regex = new RegexBuilder().builder()
                            .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                            .defineWord(splittedText.get(amountPos - 1))
                            .anyWhitespaceSeq()
                            .defineNumber()
                            .anyWhitespaceSeq()
                            .defineWord(keyWord)
                            .build();
                    amountBlockPos = 3;
                    TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                    templates.add(template);
                }
            }
        } else {
            if (!amountKeywords.isEmpty() && (!incomeKeywords.isEmpty() || !expenseKeywords.isEmpty())) {
                for (String amountKeyWord : amountKeywords) {
                    for (String keyWord : incomeKeywords) {
                        regex = new RegexBuilder().builder()
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .defineWord(keyWord)
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .defineWord(amountKeyWord)
                                .anyWhitespaceSeq()
                                .defineNumber()
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .or()
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .defineWord(amountKeyWord)
                                .anyWhitespaceSeq()
                                .defineNumber()
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .defineWord(keyWord)
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .build();
                        amountBlockPos = 5;
                        int secondBlockPos = 9;
                        TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.INCOME, amountBlockPos);
                        template.setPosAmountGroupSecond(secondBlockPos);
                        templates.add(template);
                    }
                    for (String keyWord : expenseKeywords) {
                        regex = new RegexBuilder().builder()
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .defineWord(keyWord)
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .defineWord(amountKeyWord)
                                .anyWhitespaceSeq()
                                .defineNumber()
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .or()
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .defineWord(amountKeyWord)
                                .anyWhitespaceSeq()
                                .defineNumber()
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .defineWord(keyWord)
                                .openGroup().anyVisibleCharSeq().anyWhitespaceSeq().closeGroup()
                                .build();
                        amountBlockPos = 5;
                        int secondBlockPos = 9;
                        TemplateSms template = new TemplateSms(regex, PocketAccounterGeneral.EXPENSE, amountBlockPos);
                        template.setPosAmountGroupSecond(secondBlockPos);
                        templates.add(template);
                    }
                }
            }
        }
        return templates;
    }


    public Calendar getFirstDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 0, 1);
        List<Account> accounts = daoSession.getAccountDao().loadAll();
        for (Account account : accounts) {
            if (calendar == null)
                calendar = (Calendar) account.getCalendar().clone();
            else {
                if (calendar.compareTo(account.getCalendar()) >= 0)
                    calendar = (Calendar) account.getCalendar().clone();
            }
        }
        List<FinanceRecord> records = daoSession.getFinanceRecordDao().loadAll();
        for (FinanceRecord financeRecord : records) {
            if (calendar == null)
                calendar = (Calendar) financeRecord.getDate().clone();
            else {
                if (calendar.compareTo(financeRecord.getDate()) >= 0)
                    calendar = (Calendar) financeRecord.getDate().clone();
            }
        }
        List<CreditDetials> creditDetialses = daoSession.getCreditDetialsDao().loadAll();
        for (CreditDetials creditDetials : creditDetialses) {
            for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
                if (calendar == null)
                    calendar = (Calendar) reckingCredit.getPayDate().clone();
                else {
                    if (calendar.compareTo(reckingCredit.getPayDate()) >= 0)
                        calendar = (Calendar) reckingCredit.getPayDate().clone();
                }
            }
        }
        List<DebtBorrow> debtBorrows = daoSession.getDebtBorrowDao().loadAll();
        for (DebtBorrow debtBorrow : debtBorrows) {
            if (calendar == null)
                calendar = (Calendar) debtBorrow.getTakenDate().clone();
            else {
                if (calendar.compareTo(debtBorrow.getTakenDate()) >= 0)
                    calendar = (Calendar) debtBorrow.getTakenDate().clone();
            }
            for (Recking recking : debtBorrow.getReckings()) {
                if (calendar == null)
                    calendar = (Calendar) recking.getPayDate().clone();
                else {
                    if (calendar.compareTo(recking.getPayDate()) >= 0)
                        calendar = (Calendar) recking.getPayDate().clone();
                }
            }
        }
        List<SmsParseSuccess> smsParseSuccesses = daoSession.getSmsParseSuccessDao().loadAll();
        for (SmsParseSuccess smsParseSuccess : smsParseSuccesses) {
            if (calendar == null)
                calendar = (Calendar) smsParseSuccess.getDate().clone();
            else {
                if (calendar.compareTo(smsParseSuccess.getDate()) >= 0)
                    calendar = (Calendar) smsParseSuccess.getDate().clone();
            }
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }



    public static void createDefaultDatas(SharedPreferences preferences, Context context, DaoSession daoSession) {
        preferences
                .edit()
                .putBoolean(PocketAccounterGeneral.DB_ONCREATE_ENTER, true)
                .commit();
        //inserting currencies
        String[] currencyNames = context.getResources().getStringArray(R.array.base_currencies);
        String[] currencyIds = context.getResources().getStringArray(R.array.currency_ids);
        String[] currencyCostAmounts = context.getResources().getStringArray(R.array.currency_costs);
        String[] currencySigns = context.getResources().getStringArray(R.array.base_abbrs);
        int defaultMainCurrencyPosition = 0;
        String language = Locale.getDefault().getLanguage();
        if (language.equals("ru"))
            defaultMainCurrencyPosition = 1;
        Calendar momentDay = Calendar.getInstance();
        for (int i = 0; i < 3; i++) {
            Currency currency = new Currency();
            currency.setName(currencyNames[i]);
            currency.setId(currencyIds[i]);
            currency.setMain(i == defaultMainCurrencyPosition);
            currency.setAbbr(currencySigns[i]);
            daoSession.getCurrencyDao().insertOrReplace(currency);
            UserEnteredCalendars userEnteredCalendars = new UserEnteredCalendars();
            userEnteredCalendars.setCalendar(momentDay);
            userEnteredCalendars.setCurrencyId(currency.getId());
            daoSession.getUserEnteredCalendarsDao().insertOrReplace(userEnteredCalendars);
        }

        CurrencyCostState currencyCostState = new CurrencyCostState();
        currencyCostState.setDay(momentDay);
        Currency mainCur = daoSession.getCurrencyDao()
                .queryBuilder()
                .where(CurrencyDao.Properties.IsMain.eq(true)).list().get(0);
        currencyCostState.setMainCurrency(mainCur);
        daoSession.getCurrencyCostStateDao().insertOrReplace(currencyCostState);

        List<Currency> notMainCurs = daoSession.getCurrencyDao().queryBuilder().where(
                CurrencyDao.Properties.IsMain.eq(false)).list();

        for (Currency notMainCur : notMainCurs) {
            CurrencyWithAmount withAmount = new CurrencyWithAmount();
            withAmount.setCurrency(notMainCur);
            withAmount.setParentId(currencyCostState.getId());
            for (int i = 0; i < currencyIds.length; i++) {
                if (currencyIds[i].equals(notMainCur.getId())) {
                    withAmount.setAmount(Double.parseDouble(currencyCostAmounts[i]));
                    break;
                }
            }

            daoSession.getCurrencyWithAmountDao().insertOrReplace(withAmount);
        }

        currencyCostState.resetCurrencyWithAmountList();

        for (CurrencyWithAmount currencyWithAmount : currencyCostState.getCurrencyWithAmountList()) {
            CurrencyCostState costState = new CurrencyCostState();
            costState.setDay(momentDay);
            costState.setMainCurrency(currencyWithAmount.getCurrency());
            daoSession.getCurrencyCostStateDao().insertOrReplace(costState);

            CurrencyWithAmount tempWithAmount = new CurrencyWithAmount();
            tempWithAmount.setCurrency(currencyCostState.getMainCurrency());
            tempWithAmount.setAmount(1 / currencyWithAmount.getAmount());
            tempWithAmount.setParentId(costState.getId());
            daoSession.getCurrencyWithAmountDao().insertOrReplace(tempWithAmount);
            for (CurrencyWithAmount withAmount : currencyCostState.getCurrencyWithAmountList()) {
                if (!withAmount.getCurrencyId().equals(currencyWithAmount.getCurrencyId())) {
                    CurrencyWithAmount newWithAmount = new CurrencyWithAmount();
                    newWithAmount.setCurrency(withAmount.getCurrency());
                    newWithAmount.setAmount(withAmount.getAmount() / currencyWithAmount.getAmount());
                    newWithAmount.setParentId(costState.getId());
                    daoSession.getCurrencyWithAmountDao().insertOrReplace(newWithAmount);
                }
            }
        }

        //inserting accounts
        String[] accountNames = context.getResources().getStringArray(R.array.account_names);
        String[] accountIds = context.getResources().getStringArray(R.array.account_ids);
        String[] accountIcons = context.getResources().getStringArray(R.array.account_icons);
        int[] icons = new int[accountIcons.length];
        for (int i = 0; i < accountIcons.length; i++) {
            int resId = context.getResources().getIdentifier(accountIcons[i], "drawable", context.getPackageName());
            icons[i] = resId;
        }
        for (int i = 0; i < accountNames.length; i++) {
            Account account = new Account();
            account.setName(accountNames[i]);
            account.setIcon(accountIcons[i]);
            account.setId(accountIds[i]);
            account.setStartMoneyCurrency(daoSession.getCurrencyDao().loadAll().get(0));
            account.setAmount(0.0d);
            account.setNoneMinusAccount(false);
            account.setCalendar(Calendar.getInstance());
            account.__setDaoSession(daoSession);
            daoSession.getAccountDao().insertOrReplace(account);
//            generateRegexVoice(daoSession, account.getName(), account.getId());
        }

        //inserting categories
        String[] catValues = context.getResources().getStringArray(R.array.cat_values);
        String[] catTypes = context.getResources().getStringArray(R.array.cat_types);
        String[] catIcons = context.getResources().getStringArray(R.array.cat_icons);
        for (int i = 0; i < catValues.length; i++) {
            RootCategory rootCategory = new RootCategory();
            int resId = context.getResources().getIdentifier(catValues[i], "string", context.getPackageName());
            rootCategory.setName(context.getResources().getString(resId));
            rootCategory.setId(catValues[i]);
            rootCategory.setType(Integer.parseInt(catTypes[i]));
            rootCategory.setIcon(catIcons[i]);
            int arrayId = context.getResources().getIdentifier(catValues[i], "array", context.getPackageName());
            if (arrayId != 0) {
                int subcatIconArrayId = context.getResources().getIdentifier(catValues[i] + "_icons", "array", context.getPackageName());
                String[] subCats = context.getResources().getStringArray(arrayId);
                String[] tempIcons = context.getResources().getStringArray(subcatIconArrayId);
                List<SubCategory> subCategories = new ArrayList<>();
                for (int j = 0; j < subCats.length; j++) {
                    SubCategory subCategory = new SubCategory();
                    subCategory.setName(subCats[j]);
                    subCategory.setId(UUID.randomUUID().toString());
                    subCategory.setParentId(catValues[i]);
                    subCategory.setIcon(tempIcons[j]);
                    subCategories.add(subCategory);
                    subCategory.__setDaoSession(daoSession);
                    daoSession.getSubCategoryDao().insertOrReplace(subCategory);
//                    generateRegexVoice(daoSession, subCategory.getName(), subCategory.getId());
                }
                rootCategory.setSubCategories(subCategories);
                rootCategory.__setDaoSession(daoSession);
            }
            daoSession.getRootCategoryDao().insertOrReplace(rootCategory);
//            generateRegexVoice(daoSession, rootCategory.getName(), rootCategory.getId());
        }

        List<RootCategory> incomes = daoSession.getRootCategoryDao()
                .queryBuilder().where(RootCategoryDao.Properties.Type.eq(PocketAccounterGeneral.INCOME))
                .list();
        String[] operationIds = context.getResources().getStringArray(R.array.operation_ids);
        String backIds = operationIds[5];
        String forwardId = operationIds[4];
        BoardButton boardButton;
        for (int i = 0; i < 4; i++) {
            if (incomes.size() - 1 == i) {
                boardButton = new BoardButton();
                boardButton.setCategoryId(forwardId);
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.INCOME);
                boardButton.setType(PocketAccounterGeneral.FUNCTION);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
            } else {
                boardButton = new BoardButton();
                if (incomes.size() <= i || incomes.get(i) == null)
                    boardButton.setCategoryId(null);
                else
                    boardButton.setCategoryId(incomes.get(i).getId());
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.INCOME);
                boardButton.setType(PocketAccounterGeneral.CATEGORY);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
            }
        }
        int page = 2;
        for (int i = 4; i < 40; i++) {
            if ((i + 1) % (page * 4) == 0) {
                boardButton = new BoardButton();
                boardButton.setCategoryId(forwardId);
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.INCOME);
                boardButton.setType(PocketAccounterGeneral.FUNCTION);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
                page++;
            } else {
                boardButton = new BoardButton();
                boardButton.setCategoryId(null);
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.INCOME);
                boardButton.setType(PocketAccounterGeneral.CATEGORY);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
            }
        }
        List<RootCategory> expenses = daoSession.getRootCategoryDao()
                .queryBuilder().where(RootCategoryDao.Properties.Type.eq(PocketAccounterGeneral.EXPENSE))
                .list();
        for (int i = 0; i < 16; i++) {
            if (i == expenses.size() - 2) {
                boardButton = new BoardButton();
                boardButton.setCategoryId(backIds);
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.EXPENSE);
                boardButton.setType(PocketAccounterGeneral.FUNCTION);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
            } else if (i == expenses.size() - 1) {
                boardButton = new BoardButton();
                boardButton.setCategoryId(forwardId);
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.EXPENSE);
                boardButton.setType(PocketAccounterGeneral.FUNCTION);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
            } else {
                boardButton = new BoardButton();
                if (expenses.size() <= i || expenses.get(i) == null)
                    boardButton.setCategoryId(null);
                else
                    boardButton.setCategoryId(expenses.get(i).getId());
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.EXPENSE);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
            }
        }
        page = 2;
        for (int i = 16; i < 160; i++) {
            if ((i + 2) % (page * 16) == 0) {
                boardButton = new BoardButton();
                boardButton.setCategoryId(backIds);
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.EXPENSE);
                boardButton.setType(PocketAccounterGeneral.FUNCTION);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
            } else if ((i + 1) % (page * 16) == 0) {
                boardButton = new BoardButton();
                boardButton.setCategoryId(forwardId);
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.EXPENSE);
                boardButton.setType(PocketAccounterGeneral.FUNCTION);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
                page++;
            } else {
                boardButton = new BoardButton();
                boardButton.setCategoryId(null);
                boardButton.setPos(i);
                boardButton.setTable(PocketAccounterGeneral.EXPENSE);
                boardButton.setType(PocketAccounterGeneral.CATEGORY);
                daoSession.getBoardButtonDao().insertOrReplace(boardButton);
            }
        }
    }



    public void changeIconToNull(int pos, DataCache dataCache, int table) {
        Bitmap scaled = null;
        int resId = context.getResources().getIdentifier("no_category", "drawable", context.getPackageName());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        scaled = BitmapFactory.decodeResource(context.getResources(), resId, options);

        scaled = Bitmap.createScaledBitmap(scaled, (int) context.getResources().getDimension(R.dimen.thirty_dp), (int) context.getResources().getDimension(R.dimen.thirty_dp), true);

        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().queryBuilder()
                .where(BoardButtonDao.Properties.Table.eq(table), BoardButtonDao.Properties.Pos.eq(pos)).build().list();
        if (!boardButtons.isEmpty()) {
            dataCache.getBoardBitmapsCache().put(boardButtons.get(0).getId(),
                    scaled);
        }
    }

    public int defineType(String categoryId) {
        RootCategory categorie = daoSession.getRootCategoryDao().load(categoryId);
        if (categorie != null) return PocketAccounterGeneral.CATEGORY;
        try {
            CreditDetials credit = daoSession.getCreditDetialsDao().load(Long.parseLong(categoryId));
            if (credit != null) return PocketAccounterGeneral.CREDIT;
        }
        catch (Exception e) { e.printStackTrace(); }
        DebtBorrow debtBorrow = daoSession.getDebtBorrowDao().load(categoryId);
        if (debtBorrow != null) return PocketAccounterGeneral.DEBT_BORROW;
        String[] operationIds = context.getResources().getStringArray(R.array.operation_ids);
        for (String operationId : operationIds) {
            if (operationId.equals(categoryId)) return PocketAccounterGeneral.FUNCTION;
        }
        String[] pageIds = context.getResources().getStringArray(R.array.page_ids);
        for (String pageId : pageIds) {
            if (pageId.equals(categoryId)) return PocketAccounterGeneral.PAGE;
        }
        return PocketAccounterGeneral.NULL;
    }

    public static void generateRegexVoice(List<TemplateVoice> voices, RootCategory object) {
        List<SubCategory> subCategories = object.getSubCategories();
        if (subCategories != null && !subCategories.isEmpty()) {
            for (SubCategory subCategory : subCategories) {
                TemplateVoice templateVoice = new TemplateVoice();
                templateVoice.setRegex(new RegexBuilder()
                        .openGroup()
                        .anyVisibleCharSeq()
                        .defineWord(object.getName().toLowerCase())
                        .anyWhitespaceSeq()
                        .anyVisibleCharSeq()
                        .defineWord(subCategory.getName().toLowerCase())
                        .anyVisibleCharSeq()
                        .closeGroup()
                        .or()
                        .openGroup()
                        .anyVisibleCharSeq()
                        .defineWord(subCategory.getName().toLowerCase())
                        .anyWhitespaceSeq()
                        .anyVisibleCharSeq()
                        .defineWord(object.getName().toLowerCase())
                        .anyVisibleCharSeq()
                        .closeGroup()
                        .or()
                        .openGroup()
                        .anyVisibleCharSeq()
                        .defineWord(subCategory.getName().toLowerCase())
                        .anyVisibleCharSeq()
                        .closeGroup()
                        .build());
                templateVoice.getPairs().put(1, Arrays.asList(2, 3));
                templateVoice.getPairs().put(2, Arrays.asList(5, 6));
                templateVoice.getPairs().put(3, Arrays.asList(8, 8));
                templateVoice.setCatName(object.getName().toLowerCase());
                templateVoice.setCategoryId(object.getId());
                templateVoice.setSubCatId(subCategory.getId());
                templateVoice.setSubCatName(subCategory.getName().toLowerCase());
                voices.add(templateVoice);
            }
        }
        TemplateVoice templateVoice = new TemplateVoice();
        templateVoice.setCatName(object.getName().toLowerCase());
        templateVoice.setCategoryId(object.getId());
        templateVoice.setRegex(new RegexBuilder()
                .anyVisibleCharSeq()
                .defineWord(object.getName().toLowerCase())
                .anyVisibleCharSeq()
                .build());
        voices.add(templateVoice);
    }

    public interface AfterAnimationEnd {
        void onAnimoationEnd();
    }

    public static void buttonClickCustomAnimation(final View view, final AfterAnimationEnd afterAnimationEnd) {
        final Runnable onClickAction = new Runnable() {
            @Override
            public void run() {
                afterAnimationEnd.onAnimoationEnd();
            }
        };
        Runnable endAction = new Runnable() {
            public void run() {
                view.animate().setDuration(50).scaleXBy(0.5f).scaleYBy(0.5f).scaleX(1f).scaleY(1f).setInterpolator(new AccelerateInterpolator()).withEndAction(onClickAction);
            }
        };
        view.animate().setDuration(50).scaleXBy(0.5f).scaleYBy(0.5f).scaleX(0.90f).scaleY(0.90f).setInterpolator(new DecelerateInterpolator()).withEndAction(endAction);
    }

    public static void buttonClickCustomAnimation(float scaleSize, final View view, final AfterAnimationEnd afterAnimationEnd) {
        final Runnable onClickAction = new Runnable() {
            @Override
            public void run() {
                afterAnimationEnd.onAnimoationEnd();
            }
        };
        Runnable endAction = new Runnable() {
            public void run() {
                view.animate().setDuration(50).scaleXBy(0.5f).scaleYBy(0.5f).scaleX(1f).scaleY(1f).setInterpolator(new AccelerateInterpolator()).withEndAction(onClickAction);
            }
        };
        view.animate().setDuration(50).scaleXBy(0.5f).scaleYBy(0.5f).scaleX(scaleSize).scaleY(scaleSize).setInterpolator(new DecelerateInterpolator()).withEndAction(endAction);

    }

    public static void generateRegexAcocuntVoice(List<TemplateAccount> templateAccounts, Account account) {
        TemplateAccount templateAccount = new TemplateAccount();
        templateAccount.setRegex(
                new RegexBuilder()
                        .anyVisibleCharSeq()
                        .defineWord(account.getName().toLowerCase())
                        .anyVisibleCharSeq()
                        .build()
        );
        templateAccount.setAccountName(account.getName().toLowerCase());
        templateAccount.setAccountId(account.getId());
        templateAccounts.add(templateAccount);
    }

    public static void generateRegexCurrencyVoice(List<TemplateCurrencyVoice> currencyVoices, Currency currency, Context context) {
        int resId = context.getResources().getIdentifier(currency.getId(), "array", context.getPackageName());
        if (resId != 0) {
            String curRes[] = context.getResources().getStringArray(resId);
            TemplateCurrencyVoice templateCurrencyVoice = new TemplateCurrencyVoice();
            templateCurrencyVoice.setCurName(currency.getName());
            templateCurrencyVoice.setCurId(currency.getId());
            RegexBuilder regexBuilder = new RegexBuilder();
            regexBuilder
                    .anyVisibleCharSeq()
                    .defineWord(currency.getName())
                    .anyVisibleCharSeq();
            for (String curString : curRes) {
                regexBuilder
                        .or()
                        .anyVisibleCharSeq()
                        .defineWord(curString)
                        .anyVisibleCharSeq();
            }
            templateCurrencyVoice.setRegex(regexBuilder.build());
            currencyVoices.add(templateCurrencyVoice);
        }
    }
    public static final int ANUTETNIY = 1 ;
    public static final int DEFERINSIAL = 0;

    public static int[] getDateDifferenceInDDMMYYYY(Date from, Date to) {
        Calendar fromDate = Calendar.getInstance();
        Calendar toDate = Calendar.getInstance();
        fromDate.setTime(from);
        toDate.setTime(to);
        int increment = 0;
        int year, month, day;
        if (fromDate.get(Calendar.DAY_OF_MONTH) > toDate.get(Calendar.DAY_OF_MONTH)) {
            increment = fromDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        if (increment != 0) {
            day = (toDate.get(Calendar.DAY_OF_MONTH) + increment) - fromDate.get(Calendar.DAY_OF_MONTH);
            increment = 1;
        } else {
            day = toDate.get(Calendar.DAY_OF_MONTH) - fromDate.get(Calendar.DAY_OF_MONTH);
        }
        if ((fromDate.get(Calendar.MONTH) + increment) > toDate.get(Calendar.MONTH)) {
            month = (toDate.get(Calendar.MONTH) + 12) - (fromDate.get(Calendar.MONTH) + increment);
            increment = 1;
        } else {
            month = (toDate.get(Calendar.MONTH)) - (fromDate.get(Calendar.MONTH) + increment);
            increment = 0;
        }

        year = toDate.get(Calendar.YEAR) - (fromDate.get(Calendar.YEAR) + increment);
        return new int[]{year, month, day};
    }
}