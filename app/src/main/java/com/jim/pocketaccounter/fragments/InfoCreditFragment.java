package com.jim.pocketaccounter.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.database.Recking;
import com.jim.pocketaccounter.database.ReckingCredit;
import com.jim.pocketaccounter.database.ReckingCreditDao;
import com.jim.pocketaccounter.debt.PocketClassess;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.cache.DataCache;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;


public class InfoCreditFragment extends Fragment {
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    DaoSession daoSession;
    @Inject
    DataCache dataCache;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    CommonOperations commonOperations;
    @Inject
    LogicManager logicManager;
    @Inject
    LogicManager financeManager;

    WarningDialog warningDialog;

    CreditDetialsDao creditDetialsDao;
    ReckingCreditDao reckingCreditDao;
    AccountDao accountDao;
    FinanceRecordDao financeRecordDao;
    DebtBorrowDao debtBorrowDao;

    ImageView expandableBut;
    ImageView cancel_button;
    RelativeLayout expandablePanel;
    RecyclerView tranact_recyc;
    CreditDetials currentCredit;
    boolean toArcive = false;
    TextView myCreditName;
    TextView myLefAmount;
    TextView myProcent;
    TextView myPeriodOfCredit;
    TextView myTakedCredTime;
    TextView myTakedValue;
    TextView myReturnValue;
    TextView myTotalPaid;
    TextView calculeted;
    ImageView icon_credit;
    ConWithFragments A1;
    PaysCreditAdapter adapRecyc;
    List<ReckingCredit> rcList;
    boolean delete_flag = false;
    int currentPOS = 0;
    final static long forDay = 1000L * 60L * 60L * 24L;
    final static long forMoth = 1000L * 60L * 60L * 24L * 30L;
    final static long forWeek = 1000L * 60L * 60L * 24L * 7L;
    final static long forYear = 1000L * 60L * 60L * 24L * 365L;
    boolean isExpandOpen = false;
    private Context context;
    DecimalFormat formater;
    TextView myPay;
//    ImageView myDelete;
    boolean fromMainWindow = false;
    private boolean[] isCheks;
    private int positionOfBourdMain;
    private int modeOfMain;
    boolean fromSearch = false;

    public InfoCreditFragment() {
        // Required empty public constructor
    }

    public void setConteent(CreditDetials temp, int currentPOS, ConWithFragments A1) {
        currentCredit = temp;
        this.A1 = A1;
        this.currentPOS = currentPOS;
    }

    public void setContentFromMainWindow(CreditDetials temp, int positionOfBourd, int modeOfMain) {
        fromMainWindow = true;

        currentCredit = temp;
        this.positionOfBourdMain = positionOfBourd;
        this.modeOfMain = modeOfMain;
    }

    public void setDefaultContent(CreditDetials temp) {
        currentCredit = temp;
        fromSearch = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        creditDetialsDao = daoSession.getCreditDetialsDao();
        reckingCreditDao = daoSession.getReckingCreditDao();
        accountDao = daoSession.getAccountDao();
        formater = new DecimalFormat("0.##");
        context = getActivity();
        warningDialog = new WarningDialog(context);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_info_credit_modern, container, false);
        Date dateForSimpleDate = (new Date());
        if (fromMainWindow)
            paFragmentManager.setMainReturn(true);
        expandableBut = (ImageView) V.findViewById(R.id.wlyuzik_opener);
        expandablePanel = (RelativeLayout) V.findViewById(R.id.shlyuzik);
        myCreditName = (TextView) V.findViewById(R.id.name_of_credit);
        myLefAmount = (TextView) V.findViewById(R.id.value_credit_all);
        myProcent = (TextView) V.findViewById(R.id.procentCredInfo);
//        myLefDate = (TextView) V.findViewById(R.id.leftDateInfo);
        myPeriodOfCredit = (TextView) V.findViewById(R.id.intervalCreditInfo);
        myTakedCredTime = (TextView) V.findViewById(R.id.takedtimeInfo);
        myTakedValue = (TextView) V.findViewById(R.id.takedValueInfo);
        myReturnValue = (TextView) V.findViewById(R.id.totalReturnValueInfo);
        myTotalPaid = (TextView) V.findViewById(R.id.total_transaction);
        calculeted = (TextView) V.findViewById(R.id.it_is_include_balance);
        tranact_recyc = (RecyclerView) V.findViewById(R.id.recycler_for_transactions);
        icon_credit = (ImageView) V.findViewById(R.id.icon_creditt);
        cancel_button = (ImageView) V.findViewById(R.id.cancel_button);

        rcList = currentCredit.getReckings();
        currentCredit.resetReckings();
        adapRecyc = new PaysCreditAdapter(rcList);
        myPay = (TextView) V.findViewById(R.id.paybut);
//        myDelete = (ImageView) V.findViewById(R.id.deleterbut);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        tranact_recyc.setLayoutManager(llm);

        isCheks = new boolean[currentCredit.getReckings().size()];
        toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
        toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO add remove and edit
                final AlertDialog.Builder builderChouse = new AlertDialog.Builder(getActivity());
                builderChouse.setTitle(getString(R.string.choose_type_p)).setItems(R.array.more_option_for_credit_debt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            paFragmentManager.getFragmentManager().popBackStack();
                            AddCreditFragment forEdit = new AddCreditFragment();
                            if (fromMainWindow)
                                forEdit.setDateFormatModes(modeOfMain, positionOfBourdMain);
                            forEdit.shareForEdit(currentCredit);
                            paFragmentManager.displayFragment(forEdit);
                        } else {
                            warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!fromMainWindow) {
                                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                                        for (BoardButton boardButton : boardButtons) {
                                            if (boardButton.getCategoryId() != null)
                                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {
                                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                                    else
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                                }
                                        }

                                        logicManager.deleteCredit(currentCredit);
                                        dataCache.updateAllPercents();
                                        paFragmentManager.updateAllFragmentsOnViewPager();

                                        A1.delete_item(currentPOS);
                                    } else if (fromSearch) {
                                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                                        for (BoardButton boardButton : boardButtons) {
                                            if (boardButton.getCategoryId() != null)
                                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {

                                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                                    else
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);

                                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                                }
                                        }
                                        dataCache.updateAllPercents();
                                        paFragmentManager.updateAllFragmentsOnViewPager();
                                        logicManager.deleteCredit(currentCredit);

                                    } else {
                                        if (modeOfMain == PocketAccounterGeneral.EXPENSE) {
                                            logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, positionOfBourdMain, null);
                                        } else {
                                            logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, positionOfBourdMain, null);
                                        }
                                        commonOperations.changeIconToNull(positionOfBourdMain, dataCache, modeOfMain);

                                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                                        for (BoardButton boardButton : boardButtons) {
                                            if (boardButton.getCategoryId() != null) {
                                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {

                                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE) {
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                                    } else {
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                                    }

                                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                                }
                                            }
                                        }
                                        logicManager.deleteCredit(currentCredit);
                                        dataCache.updateAllPercents();
                                        paFragmentManager.updateAllFragmentsOnViewPager();

                                    }
                                    if (fromMainWindow)
                                        dataCache.updateOneDay(dataCache.getEndDate());
                                    if (fromMainWindow) {
                                        getActivity().getSupportFragmentManager().popBackStack();
                                        paFragmentManager.displayMainWindow();
                                    } else {

                                        getActivity().getSupportFragmentManager().popBackStack();

                                        paFragmentManager.displayFragment(new CreditTabLay());
                                    }
                                    warningDialog.dismiss();
                                }
                            });
                            warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    warningDialog.dismiss();
                                }
                            });
                            warningDialog.setText(getString(R.string.delete_credit));
                            warningDialog.show();
                        }
                    }
                });
                builderChouse.create().show();
            }
        });

        tranact_recyc.setAdapter(adapRecyc);

        double total_paid = 0;
        for (ReckingCredit item : rcList) {
            total_paid += item.getAmount();
        }

        adapRecyc.notifyDataSetChanged();
        V.findViewById(R.id.frameLayout2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toArcive && !delete_flag) {
                    currentCredit.setKey_for_archive(true);
                    logicManager.insertCredit(currentCredit);
                    if (!fromMainWindow) {
                        A1.to_Archive(currentPOS);
                        paFragmentManager.getFragmentManager().popBackStack();
                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                        for (BoardButton boardButton : boardButtons) {
                            if (boardButton.getCategoryId() != null)
                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {
                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                    else
                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                }
                        }
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();

                    } else if (fromSearch) {

                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                        for (BoardButton boardButton : boardButtons) {
                            if (boardButton.getCategoryId() != null)
                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {
                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                    else
                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                }
                        }
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();

                        paFragmentManager.getFragmentManager().popBackStack();
                    } else {
                        if (modeOfMain == PocketAccounterGeneral.EXPENSE)
                            logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, positionOfBourdMain, null);
                        else
                            logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, positionOfBourdMain, null);

                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                        for (BoardButton boardButton : boardButtons) {
                            if (boardButton.getCategoryId() != null)
                                if (boardButton.getCategoryId().equals(Long.toString(currentCredit.getMyCredit_id()))) {
                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                    else
                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());

                                }
                        }
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        commonOperations.changeIconToNull(positionOfBourdMain, dataCache, modeOfMain);
                        paFragmentManager.displayMainWindow();
                    }
                } else if (!delete_flag) {
                    openDialog();
                } else if (toArcive && delete_flag) {
                    delete_flag = false;
                    myPay.setText(R.string.archive);
                    adapRecyc.notifyDataSetChanged();
                }


            }
        });

        myTakedValue.setText(parseToWithoutNull(currentCredit.getValue_of_credit()) + currentCredit.getValyute_currency().getAbbr());
        myReturnValue.setText(parseToWithoutNull(currentCredit.getValue_of_credit_with_procent()) + currentCredit.getValyute_currency().getAbbr());
        int resId = context.getResources().getIdentifier(currentCredit.getIcon_ID(), "drawable", context.getPackageName());
        icon_credit.setImageResource(resId);
        dateForSimpleDate.setTime(currentCredit.getTake_time().getTimeInMillis());
        myTakedCredTime.setText(dateFormat.format(dateForSimpleDate));
        myCreditName.setText(currentCredit.getCredit_name());
        calculeted.setText((currentCredit.getKey_for_include()) ? getString(R.string.calculaed) : getString(R.string.not_calc));
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_flag = false;
                cancel_button.setVisibility(View.GONE);
//                    myPay.setText(R.string.pay);
                for (int i = 0; i < isCheks.length; i++) {
                    isCheks[i] = false;
                }
                adapRecyc.notifyDataSetChanged();
            }
        });
        myTotalPaid.setText(parseToWithoutNull(total_paid) + currentCredit.getValyute_currency().getAbbr());
        if (currentCredit.getValue_of_credit_with_procent() - total_paid <= 0) {
            myLefAmount.setText(getString(R.string.repaid));
            toArcive = true;
            myPay.setText(getString(R.string.archive));
        } else
            myLefAmount.setText(parseToWithoutNull(currentCredit.getValue_of_credit_with_procent() - total_paid) + currentCredit.getValyute_currency().getAbbr());

        String suffix = "";
        if (currentCredit.getProcent_interval() == forDay) {
            suffix = getString(R.string.per_day);
        } else if (currentCredit.getProcent_interval() == forWeek) {
            suffix = getString(R.string.per_week);
        } else if (currentCredit.getProcent_interval() == forMoth) {
            suffix = getString(R.string.per_month);
        } else {
            suffix = getString(R.string.per_year);
        }

        myProcent.setText(parseToWithoutNull(currentCredit.getProcent()) + "%" + " " + suffix);
        V.findViewById(R.id.frameLayout3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delete_flag) {
                    delete_checked_items();
                } else {
                    delete_flag = true;
                    adapRecyc.notifyDataSetChanged();
                    cancel_button.setVisibility(View.VISIBLE);
//                    myPay.setText(getString(R.string.cancel));
                }
            }
        });

        Calendar to = (Calendar) currentCredit.getTake_time().clone();
        long period_tip = currentCredit.getPeriod_time_tip();
        long period_voqt = currentCredit.getPeriod_time();

        int voqt_soni = (int) (period_voqt / period_tip);

        if (period_tip == forDay) {
            suffix = getString(R.string.dayy);
            to.add(Calendar.DAY_OF_YEAR, (int) voqt_soni);
        } else if (period_tip == forWeek) {
            suffix = getString(R.string.weekk);
            to.add(Calendar.WEEK_OF_YEAR, (int) voqt_soni);
        } else if (period_tip == forMoth) {
            suffix = getString(R.string.mont);
            to.add(Calendar.MONTH, (int) voqt_soni);
        } else {
            suffix = getString(R.string.yearr);
            to.add(Calendar.YEAR, (int) voqt_soni);
        }

        myPeriodOfCredit.setText(Integer.toString(voqt_soni) + " " + suffix);

        V.findViewById(R.id.infoooc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpandOpen) {
                    expandablePanel.setVisibility(View.GONE);
                    expandableBut.setImageResource(R.drawable.info_open);
                    isExpandOpen = false;
                } else {
                    expandablePanel.setVisibility(View.VISIBLE);
                    expandableBut.setImageResource(R.drawable.info_pastga);
                    isExpandOpen = true;
                }
            }
        });

        long for_compute_interval = currentCredit.getTake_time().getTimeInMillis() + currentCredit.getPeriod_time() - System.currentTimeMillis();

        Date from = new Date();
        int t[] = getDateDifferenceInDDMMYYYY(from, to.getTime());
        if (t[0] * t[1] * t[2] < 0 && (t[0] + t[1] + t[2]) != 0) {
//            myLefDate.setText(R.string.ends);
//            myLefDate.setTextColor(Color.parseColor("#832e1c"));
        } else {
            String left_date_string = "";
            if (t[0] != 0) {
                if (t[0] > 1) {
                    left_date_string += Integer.toString(t[0]) + " " + getString(R.string.years);
                } else {
                    left_date_string += Integer.toString(t[0]) + " " + getString(R.string.year);
                }

            }
            if (t[1] != 0) {
                if (!left_date_string.matches("")) {
                    left_date_string += " ";
                }
                if (t[1] > 1) {
                    left_date_string += Integer.toString(t[1]) + " " + getString(R.string.moths);
                } else {
                    left_date_string += Integer.toString(t[1]) + " " + getString(R.string.moth);
                }
            }
            if (t[2] != 0) {
                if (!left_date_string.matches("")) {
                    left_date_string += " ";
                }
                if (t[2] > 1) {
                    left_date_string += Integer.toString(t[2]) + " " + getString(R.string.days);
                } else {
                    left_date_string += Integer.toString(t[2]) + " " + getString(R.string.day);
                }
            }
//            myLefDate.setText(left_date_string);
        }



        return V;
    }

    public String parseToWithoutNull(double A) {
        if (A == (int) A)
            return Integer.toString((int) A);
        else
            return formater.format(A);
    }

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

    public interface ConWithFragments {
        void change_item(CreditDetials creditDetials, int position);

        void to_Archive(int position);

        void delete_item(int position);
    }

    ArrayList<Account> accaunt_AC;

    private void openDialog() {
        final Dialog dialog = new Dialog(context);
        View dialogView = ((PocketAccounter) context).getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        final EditText enterDate = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowDate);
        final EditText enterPay = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPaySumm);
        final EditText comment = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPayComment);
        final Spinner accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);
        accaunt_AC = null;
        if (currentCredit.getKey_for_include()) {
            accaunt_AC = (ArrayList<Account>) accountDao.queryBuilder().list();
            String[] accaounts = new String[accaunt_AC.size()];
            for (int i = 0; i < accaounts.length; i++) {
                accaounts[i] = accaunt_AC.get(i).getName();
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    context, R.layout.spiner_gravity_right, accaounts);

            accountSp.setAdapter(arrayAdapter);

        } else {
            dialogView.findViewById(R.id.is_calc).setVisibility(View.GONE);
        }
        final Calendar date;
        if (fromMainWindow)
            date = dataCache.getEndDate();
        else date = Calendar.getInstance();

        enterDate.setText(dateFormat.format(date.getTime()));
        ImageView cancel = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
        ImageView save = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        final DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                enterDate.setText(dateFormat.format((new GregorianCalendar(arg1, arg2, arg3)).getTime()));
                date.set(arg1, arg2, arg3);
            }
        };
        enterDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                Dialog mDialog = new DatePickerDialog(context,
                        getDatesetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH));
                mDialog.show();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String amount = enterPay.getText().toString();
                double total_paid = 0;
                for (ReckingCredit item : rcList)
                    total_paid += item.getAmount();

                if (!amount.matches("")) {
                    if (currentCredit.getKey_for_include()) {
                        Account account = accaunt_AC.get(accountSp.getSelectedItemPosition());
                        if (account.getIsLimited()) {
                            //TODO editda tekwir ozini hisoblamaslini
                            double limit = account.getLimite();
                            double accounted = logicManager.isLimitAccess(account, date);

                            accounted = accounted - commonOperations.getCost(date, currentCredit.getValyute_currency(), account.getCurrency(), Double.parseDouble(amount));
                            if (-limit > accounted) {
                                Toast.makeText(context, R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }


                    if (Double.parseDouble(amount) > currentCredit.getValue_of_credit_with_procent() - total_paid) {
                        warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ReckingCredit rec = null;
                                if (!amount.matches("") && currentCredit.getKey_for_include())
                                    rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(),
                                            currentCredit.getMyCredit_id(), comment.getText().toString());
                                else
                                    rec = new ReckingCredit(date, Double.parseDouble(amount), "",
                                            currentCredit.getMyCredit_id(), comment.getText().toString());
//                                        rcList.add(rec);
//                                        currentCredit.getReckings().addAll(rcList);
                                logicManager.insertReckingCredit(rec);
                                dataCache.updateAllPercents();
                                paFragmentManager.updateAllFragmentsOnViewPager();
//                                        paFragmentManager.getCurrentFragment().update();
                                currentCredit.resetReckings();
                                rcList = currentCredit.getReckings();
                                updateDate();
                                adapRecyc.setMyList(rcList);
                                if (!fromMainWindow && A1 != null)
                                    A1.change_item(currentCredit, currentPOS);
                                isCheks = new boolean[rcList.size()];
                                for (int i = 0; i < isCheks.length; i++) {
                                    isCheks[i] = false;
                                }
                                dialog.dismiss();
                                adapRecyc.notifyDataSetChanged();
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setText(context.getString(R.string.payment_balans) + parseToWithoutNull(currentCredit.getValue_of_credit_with_procent() - total_paid) +
                                currentCredit.getValyute_currency().getAbbr() + "." + context.getString(R.string.payment_balance2) +
                                parseToWithoutNull(Double.parseDouble(amount) - (currentCredit.getValue_of_credit_with_procent() - total_paid)) +
                                currentCredit.getValyute_currency().getAbbr());
                        warningDialog.show();
                    } else {
                        ReckingCredit rec = null;
                        if (!amount.matches("") && currentCredit.getKey_for_include())
                            rec = new ReckingCredit(date, Double.parseDouble(amount), accaunt_AC.get(accountSp.getSelectedItemPosition()).getId(), currentCredit.getMyCredit_id(), comment.getText().toString());
                        else
                            rec = new ReckingCredit(date, Double.parseDouble(amount), "", currentCredit.getMyCredit_id(), comment.getText().toString());
//                        currentCredit.getReckings().add(rec);
//                        rcList.add(rec);
                        logicManager.insertReckingCredit(rec);
                        currentCredit.resetReckings();
                        rcList = currentCredit.getReckings();
                        adapRecyc.setMyList(rcList);
                        updateDate();
                        dataCache.updateAllPercents();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        isCheks = new boolean[rcList.size()];
                        for (int i = 0; i < isCheks.length; i++) {
                            isCheks[i] = false;
                        }
                        if (!fromMainWindow)
                            A1.change_item(currentCredit, currentPOS);
                        else if (fromSearch) {

                        }
                        adapRecyc.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }

            }
        });
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        dialog.getWindow().setLayout(7 * width / 8, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    public void openFragment(Fragment fragment) {
        if (fragment != null) {
            final android.support.v4.app.FragmentTransaction ft = ((PocketAccounter) context)
                    .getSupportFragmentManager().beginTransaction()
                    .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            ft.add(R.id.flMain);
            ft.commit();
        }
    }

    public void updateDate() {
        double total_paid = 0;
        for (ReckingCredit item : rcList)
            total_paid += item.getAmount();
        if (currentCredit.getValue_of_credit_with_procent() - total_paid <= 0) {
            myLefAmount.setText(getString(R.string.repaid));
            myPay.setText(getString(R.string.archive));
            toArcive = true;
        } else {
            toArcive = false;
            myLefAmount.setText(parseToWithoutNull(currentCredit.getValue_of_credit_with_procent() - total_paid) + currentCredit.getValyute_currency().getAbbr());
        }

        myTotalPaid.setText(parseToWithoutNull(total_paid) + currentCredit.getValyute_currency().getAbbr());
        //TODO update recycler
    }

    public void delete_checked_items() {
        boolean keyfor = false;
        final int lenght = rcList.size() - 1;
        for (boolean isChek : isCheks) {
            if (isChek) {
                keyfor = true;
                break;
            }
        }
        delete_flag = false;
        if (keyfor) {
            warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int t = isCheks.length - 1; t >= 0; t--) {
                        if (isCheks[t]) {
                            logicManager.deleteReckingCredit(rcList.get(t));
                            currentCredit.resetReckings();
                            rcList = currentCredit.getReckings();
                            adapRecyc.setMyList(rcList);
                            adapRecyc.notifyItemRemoved(t);
                            if (!fromMainWindow)
                                A1.change_item(currentCredit, currentPOS);
                        } else adapRecyc.notifyItemChanged(t);
                    }
                    isCheks = new boolean[rcList.size()];
                    for (int i = 0; i < isCheks.length; i++) {
                        isCheks[i] = false;
                    }
                    updateDate();
                    dataCache.updateAllPercents();
                    paFragmentManager.updateAllFragmentsOnViewPager();
                    warningDialog.dismiss();
                }
            });
            warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warningDialog.dismiss();
                }
            });
            warningDialog.setText(getString(R.string.accept_delete_reck));
            warningDialog.show();
        } else {
            adapRecyc.notifyDataSetChanged();
        }
        cancel_button.setVisibility(View.GONE);
//        myPay.setText(getString(R.string.pay));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private class PaysCreditAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<ReckingCredit> list;

        public PaysCreditAdapter(List<ReckingCredit> list) {
            this.list = list;
        }

        public void setMyList(List<ReckingCredit> list) {
            this.list = list;
        }

        public int getItemCount() {
            return list.size();
        }

        public void onBindViewHolder(final ViewHolder view, final int position) {
            ReckingCredit item = list.get(position);
            view.infoDate.setText(dateFormat.format(item.getPayDate().getTime()));
            view.infoSumm.setText(parseToWithoutNull(item.getAmount()) + currentCredit.getValyute_currency().getAbbr());
            if (currentCredit.getKey_for_include()) {
                ArrayList<Account> accounts = (ArrayList<Account>) accountDao.queryBuilder().list();
                String accs = accounts.get(0).getName();
                for (int i = 0; i < accounts.size(); i++) {
                    if (item.getAccountId().equals(accounts.get(i).getId())) {
                        accs = accounts.get(i).getName();
                    }
                }
                view.infoAccount.setText(getString(R.string.via) + ": " + accs);
            } else {
                view.infoAccount.setVisibility(View.GONE);
            }
            if (!item.getComment().matches(""))
                view.comment.setText(getString(R.string.comment) + ": " + item.getComment());
            else
                view.comment.setVisibility(View.GONE);
            if (delete_flag) {
                view.forDelete.setVisibility(View.VISIBLE);
                view.forDelete.setChecked(isCheks[position]);
                view.glav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (view.forDelete.isChecked())
                            view.forDelete.setChecked(false);
                        else view.forDelete.setChecked(true);
                        isCheks[position] = !isCheks[position];
                    }
                });
            } else {
                view.forDelete.setChecked(false);
                view.forDelete.setVisibility(View.GONE);
            }
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payed_item, parent, false);
            return new ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView infoDate;
        public TextView infoSumm;
        public TextView infoAccount;
        public TextView comment;
        public CheckBox forDelete;
        public View glav;

        public ViewHolder(View view) {
            super(view);
            infoDate = (TextView) view.findViewById(R.id.date_of_trans);
            infoAccount = (TextView) view.findViewById(R.id.via_acount);
            comment = (TextView) view.findViewById(R.id.comment_trans);
            infoSumm = (TextView) view.findViewById(R.id.paid_value);
            forDelete = (CheckBox) view.findViewById(R.id.for_delete_check_box);
            glav = view;
        }
    }
}
