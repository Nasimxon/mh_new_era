package com.jim.finansia.debt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Visibility;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.Recking;
import com.jim.finansia.finance.TransferAccountAdapter;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
import com.jim.finansia.utils.DatePicker;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SpinnerAdapter;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.cache.DataCache;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.hdodenhof.circleimageview.CircleImageView;

public class BorrowFragment extends Fragment {
    @Inject DatePicker datePicker;
    @Inject @Named(value = "display_formatter") SimpleDateFormat dateFormat;
    @Inject PAFragmentManager paFragmentManager;
    @Inject LogicManager logicManager;
    @Inject CommonOperations commonOperations;
    @Inject DaoSession daoSession;
    @Inject DataCache dataCache;
    @Inject DecimalFormat formatter;
    @Inject ReportManager reportManager;
    @Inject SharedPreferences preferences;
    private WarningDialog warningDialog;
    private DebtBorrowDao debtBorrowDao;
    private AccountDao accountDao;
    private TextView ifListEmpty;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter myAdapter;
    private DecimalFormat formater;
    private Context context;
    private DebtBorrowFragment debtBorrowFragment;
    private int TYPE = 0;

    public static BorrowFragment getInstance(int type) {
        BorrowFragment fragment = new BorrowFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        TYPE = getArguments().getInt("type", 0);
        formater = new DecimalFormat("0.00");
        context=getContext();
        warningDialog = new WarningDialog(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.borrow_fragment_layout, container, false);
        debtBorrowDao = daoSession.getDebtBorrowDao();
        accountDao = daoSession.getAccountDao();
        recyclerView = (RecyclerView) view.findViewById(R.id.lvBorrowFragment);
        ifListEmpty = (TextView) view.findViewById(R.id.ifListEmpty);
        myAdapter = new MyAdapter();
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(myAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                try {
                    debtBorrowFragment.onScrolledList(dy > 0);
                } catch (NullPointerException e) {
                }
            }
        });
        return view;
    }


    public void refreshList() {
        if (recyclerView != null) {
            myAdapter = new MyAdapter();
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(myAdapter);
        }
    }

    public void setDebtBorrowFragment(DebtBorrowFragment debtBorrowFragment) {
        this.debtBorrowFragment = debtBorrowFragment;
    }

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> {
        private ArrayList<DebtBorrow> persons;

        public MyAdapter() {
            persons = new ArrayList<>();
            for (DebtBorrow person : debtBorrowDao.loadAll()) {
                if (!person.getTo_archive() && person.getType() == TYPE) {
                    persons.add(person);
                } else {
                    if (person.getTo_archive() && TYPE == 2) {
                        persons.add(person);
                    }
                }
            }
            if (persons.size() != 0) {
                ifListEmpty.setVisibility(View.GONE);
            } else {
                String isEmpty;
                if (TYPE == DebtBorrow.BORROW) isEmpty = getString(R.string.borrow_is_empty);
                else if (TYPE == DebtBorrow.DEBT) isEmpty = getString(R.string.debt_is_empty);
                else isEmpty = getString(R.string.archive_is_empty);
                ifListEmpty.setText(isEmpty);
                ifListEmpty.setVisibility(View.VISIBLE);
            }
        }

        public int getItemCount() {
            return persons.size();
        }
        public  int[] getDateDifferenceInDDMMYYYY(Date from, Date to) {
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

        public void onBindViewHolder(final ViewHolder view, final int position) {
            final DebtBorrow person = persons.get(position);
            if (person.getType() == DebtBorrow.DEBT) {
                view.flItemDebtBorrowPay.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.red_pay_background));
                view.tvItemDebtBorrowLeft.setTextColor(Color.parseColor("#dc4849"));
                view.pay.setTextColor(Color.parseColor("#dc4849"));
            }
            try {
                view.BorrowPersonName.setText(person.getPerson().getName());
                if (person.getReturnDate() == null) {
                    view.tvItemDebtBorrowLeft.setText(R.string.no_date_selected);
                } else {
                    view.tvItemDebtBorrowLeft.setText(dateFormat.format(person.getReturnDate().getTime()));
                }
                if (person.getPerson().getPhoto().matches("") || person.getPerson().getPhoto().matches("0")) {
                    view.BorrowPersonPhotoPath.setImageResource(R.drawable.no_photo);
                } else {
                    try {
                        view.BorrowPersonPhotoPath.setImageBitmap(queryContactImage(Integer.parseInt(person.getPerson().getPhoto())));
                    } catch (Exception e) {
                        Bitmap bit = BitmapFactory.decodeFile(person.getPerson().getPhoto());
                        view.BorrowPersonPhotoPath.setImageBitmap(bit);
                    }
                }
            } catch (NullPointerException e) {
            }
            double qq = 0;
            if (person.getReckings() != null) {
                for (Recking rk : person.getReckings()) {
                    qq += rk.getAmount();
                }
            }


            if (person.getReturnDate() == null) {
                view.tvItemDebtBorrowLeftDate.setText(getResources().getString(R.string.no_date_selected));}
            else {
            int t[] = getDateDifferenceInDDMMYYYY(Calendar.getInstance().getTime(), person.getReturnDate().getTime());
            if (t[0] * t[1] * t[2] < 0 && (t[0] + t[1] + t[2]) != 0) {
                view.tvItemDebtBorrowLeftDate.setText(R.string.ends);
            } else {
                String left_date_string = "";
                if (t[0] != 0) {
                    if (t[0] > 1) {
                        left_date_string += Integer.toString(t[0]) + " " + context.getString(R.string.years);
                    } else {
                        left_date_string += Integer.toString(t[0]) + " " + context.getString(R.string.year);
                    }
                }
                if (t[1] != 0) {
                    if (!left_date_string.matches("")) {
                        left_date_string += " ";
                    }
                    if (t[1] > 1) {
                        left_date_string += Integer.toString(t[1]) + " " + context.getString(R.string.moths);
                    } else {
                        left_date_string += Integer.toString(t[1]) + " " + context.getString(R.string.moth);
                    }
                }
                if (t[2] != 0) {
                    if (!left_date_string.matches("")) {
                        left_date_string += " ";
                    }
                    if (t[2] > 1) {
                        left_date_string += Integer.toString(t[2]) + " " + context.getString(R.string.days);
                    } else {
                        left_date_string += Integer.toString(t[2]) + " " + context.getString(R.string.day);
                    }
                }
                view.tvItemDebtBorrowLeftDate.setText(left_date_string);
            }}

            // left Date





            view.llItemDebtBorrowCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!person.getPerson().getPhoneNumber().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + person.getPerson().getPhoneNumber()));
                        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }
            });
            view.llItemDebtBorrowSms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!person.getPerson().getPhoneNumber().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("smsto:" + person.getPerson().getPhoneNumber()));
                        intent.putExtra("address", person.getPerson().getPhoneNumber());
                        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }
            });
            if (person.getTo_archive() || qq >= person.getAmount()) {
                view.tvItemDebtBorrowLeft.setText(getResources().getString(R.string.repaid));
            } else
                view.tvItemDebtBorrowLeft.setText(formatter.format(person.getAmount() - qq) + person.getCurrency().getAbbr());

            view.itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    addNextFragment(person);
                }
            });
            if (TYPE == 2) {
                view.pay.setVisibility(View.GONE);
                view.flItemDebtBorrowPay.setVisibility(View.GONE);
            } else {
                double total = 0;
                for (Recking rec : person.getReckings()) {
                    total += rec.getAmount();
                }
                if (total >= person.getAmount()) {
                    view.pay.setText(getString(R.string.to_archive));
                } else view.pay.setText(getString(R.string.payy));
            }

            view.flItemDebtBorrowPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!view.pay.getText().toString().matches(getString(R.string.to_archive))) {
                        final Dialog dialog = new Dialog(getActivity());
                        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(dialogView);
                        View vi = dialog.getWindow().getDecorView();
                        vi.setBackgroundResource(android.R.color.transparent);

                        final TextView enterDate = (TextView) dialogView.findViewById(R.id.etInfoDebtBorrowDate);
                        final TextView abbrrAmount = (TextView) dialogView.findViewById(R.id.abbrrAmount);
                        final TextView debetorName = (TextView) dialogView.findViewById(R.id.for_period);
                        final TextView tvResidue = (TextView) dialogView.findViewById(R.id.tvResidue);
                        final TextView periodDate = (TextView) dialogView.findViewById(R.id.periodDate);
                        final TextView shouldPayPeriod = (TextView) dialogView.findViewById(R.id.shouldPayPeriod);
                        final EditText enterPay = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPaySumm);
                        final EditText comment = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPayComment);
                        final RelativeLayout checkInclude = (RelativeLayout) dialogView.findViewById(R.id.checkInclude);
                        final RelativeLayout is_calc = (RelativeLayout) dialogView.findViewById(R.id.is_calc);
                        final SwitchCompat keyForInclude = (SwitchCompat) dialogView.findViewById(R.id.key_for_balance);
                        final Spinner accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);
                        ImageView cancel = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
                        final TextView save = (TextView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);


                        abbrrAmount.setText(person.getCurrency().getAbbr());


                        ArrayList accounts = new ArrayList();
                        for (int i = 0; i < accountDao.queryBuilder().list().size(); i++) {
                            accounts.add(accountDao.queryBuilder().list().get(i).getId());
                        }
                        tvResidue.setText(getString(R.string.left)+":");
                        accountSp.setAdapter(new TransferAccountAdapter(getContext(),accounts));
                        String lastAccountId = preferences.getString("CHOSEN_ACCOUNT_ID",  "");
                        if (lastAccountId != null && !lastAccountId.isEmpty()) {
                            int pos = 0;
                            for (int i = 0; i < accountDao.queryBuilder().list().size(); i++) {
                                if (accountDao.queryBuilder().list().get(i).getId().equals(lastAccountId)) {
                                    pos = i;
                                    break;
                                }
                            }
                            accountSp.setSelection(pos);
                        }
                        accountSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                preferences.edit().putString("CHOSEN_ACCOUNT_ID", accountDao.queryBuilder().list().get(i).getId()).commit();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                        keyForInclude.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(keyForInclude.isChecked()){

                                    is_calc.setVisibility(View.VISIBLE);
                                }
                                else {
                                    is_calc.setVisibility(View.GONE);
                                }
                            }
                        });
                        checkInclude.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                keyForInclude.toggle();
                            }
                        });
                        double v1 = person.getAmount();
                        double totalAm = 0;
                        for (Recking recking:person.getReckings()){
                            totalAm +=recking.getAmount();
                        }
                        final double leftAmount = person.getAmount() - totalAm;
                        shouldPayPeriod.setText(formatter.format(v1-totalAm)+person.getCurrency().getAbbr());
                        if(person.getType() == DebtBorrow.DEBT){
                            debetorName.setText(R.string.debtor_name+":");
                            periodDate.setText(person.getPerson().getName());
                        }
                        else {
                            debetorName.setText(getString(R.string.borrower_name)+":");
                            periodDate.setText(person.getPerson().getName());

                        }



                        final Calendar date = Calendar.getInstance();
                        enterDate.setText(dateFormat.format(date.getTime()));
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        final DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);
                                if (date.compareTo(person.getTakenDate()) < 0) {
                                    date.setTime(person.getTakenDate().getTime());
                                    enterDate.setError(getString(R.string.incorrect_date));
                                } else {
                                    enterDate.setError(null);
                                }
                                enterDate.setText(dateFormat.format(date.getTime()));
                            }
                        };
                        enterDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Calendar calendar = Calendar.getInstance();
                                Dialog mDialog = new DatePickerDialog(getContext(),
                                        getDatesetListener, calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH), calendar
                                        .get(Calendar.DAY_OF_MONTH));
                                mDialog.show();
                            }
                        });
                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String ac = "";
                                if (keyForInclude.isChecked()) {
                                    for (Account account : accountDao.queryBuilder().list()) {
                                        if (account.getName().matches(accountSp.getSelectedItem().toString())) {
                                            ac = account.getId();
                                            break;
                                        }
                                    }
                                }
                                boolean tek = false;
                                if (!enterPay.getText().toString().isEmpty()) {
                                    if (leftAmount - Double.parseDouble(enterPay.getText().toString()) < 0) {
                                        if (keyForInclude.isChecked() && isMumkin(person, ac, Double.parseDouble(enterPay.getText().toString())))
                                            tek = true;
                                        if (!keyForInclude.isChecked()) tek = true;
                                        final String finalAc = ac;
                                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (keyForInclude.isChecked()) {
                                                    Recking recking = new Recking(date,
                                                            Double.parseDouble(enterPay.getText().toString()),
                                                            person.getId(), finalAc,
                                                            comment.getText().toString());

                                                    person.getReckings().add(0, recking);
                                                    logicManager.insertReckingDebt(recking);
                                                    double total = 0;
                                                    for (Recking recking1 : persons.get(position).getReckings()) {
                                                        total += recking1.getAmount();
                                                    }
                                                    if (person.getAmount() <= total) {
                                                        view.pay.setText(getString(R.string.to_archive));
                                                    }
                                                    view.tvItemDebtBorrowLeft.setText(getResources().getString(R.string.repaid));
                                                    dialog.dismiss();
                                                } else {
                                                    Recking recking = new Recking(date,
                                                            Double.parseDouble(enterPay.getText().toString()),
                                                            person.getId(), comment.getText().toString());
                                                    logicManager.insertReckingDebt(recking);
                                                    person.getReckings().add(0, recking);
                                                    double total = 0;
                                                    for (Recking recking1 : person.getReckings()) {
                                                        total += recking1.getAmount();
                                                    }
                                                   if (person.getAmount() <= total) {
                                                        view.pay.setText(getString(R.string.to_archive));
                                                    }
                                                    view.tvItemDebtBorrowLeft.setText(getResources().getString(R.string.repaid));
                                                    dialog.dismiss();
                                                }
                                                reportManager.clearCache();
                                                dataCache.updateAllPercents();
                                                paFragmentManager.updateAllFragmentsPageChanges();
                                                paFragmentManager.updateAllFragmentsPageChanges();
                                                warningDialog.dismiss();
                                            }
                                        });
                                        warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                warningDialog.dismiss();
                                            }
                                        });
                                        double pay = Double.parseDouble(enterPay.getText().toString()) - leftAmount;
                                        warningDialog.setText(getString(R.string.payment_balans) + " " + formatter.format(leftAmount) + getString(R.string.payment_balance2) + " " + formatter.format(pay) + person.getCurrency().getAbbr());
                                        if (tek) {
                                            warningDialog.show();
                                        }
                                    } else {
                                        Recking recking;
                                        if (keyForInclude.isChecked() && isMumkin(person, ac, Double.parseDouble(enterPay.getText().toString()))) {
                                            recking = new Recking(date,
                                                    Double.parseDouble(enterPay.getText().toString()),
                                                    person.getId(), ac,
                                                    comment.getText().toString());
                                            person.getReckings().add(0, recking);
                                            double total = 0;
                                            for (Recking recking1 : person.getReckings()) {
                                                total += recking1.getAmount();
                                            }
                                             if (person.getAmount() <= total) {
                                                view.pay.setText(getString(R.string.to_archive));
                                            }
                                            logicManager.insertReckingDebt(recking);
                                            view.tvItemDebtBorrowLeft.setText("" + formatter.format(person.getAmount() - total)  + person.getCurrency().getAbbr());
                                            reportManager.clearCache();
                                            dataCache.updateAllPercents();
                                            paFragmentManager.updateAllFragmentsPageChanges();
                                            paFragmentManager.updateAllFragmentsPageChanges();
                                            dialog.dismiss();
                                        } else {
                                            if (!keyForInclude.isChecked()) {
                                                recking = new Recking(date,
                                                        Double.parseDouble(enterPay.getText().toString()),
                                                        person.getId(), comment.getText().toString());
                                                person.getReckings().add(0, recking);
                                                double total = 0;
                                                for (Recking recking1 : person.getReckings()) {
                                                    total += recking1.getAmount();
                                                }
                                                if (person.getAmount() <= total) {
                                                    view.pay.setText(getString(R.string.to_archive));
                                                }

                                                logicManager.insertReckingDebt(recking);
                                                view.tvItemDebtBorrowLeft.setText("" + formatter.format(person.getAmount() - total) + person.getCurrency().getAbbr());

                                                reportManager.clearCache();
                                                dataCache.updateAllPercents();
                                                paFragmentManager.updateAllFragmentsPageChanges();
                                                paFragmentManager.updateAllFragmentsPageChanges();
                                                dialog.dismiss();
                                            }
                                        }
                                    }
                                } else {
                                    enterPay.setError(getString(R.string.enter_pay_value));
                                }
                            }
                        });
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        int width = displayMetrics.widthPixels;
                        dialog.getWindow().setLayout(7 * width / 8, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        dialog.show();
                    } else {
                        for (int i = 0; i < debtBorrowDao.loadAll().size(); i++) {
                            if (debtBorrowDao.loadAll().get(i).getId().matches(person.getId())) {
                                debtBorrowDao.queryBuilder().list().get(i).setTo_archive(true);
                                person.setTo_archive(true);

                                List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                                for (BoardButton boardButton : boardButtons) {
                                    if (boardButton.getCategoryId() != null)
                                        if (boardButton.getCategoryId().equals(person.getId())) {
                                            if (boardButton.getTable() == PocketAccounterGeneral.EXPANSE_MODE)
                                                logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                            else
                                                logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                            commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());
                                        }
                                }
                                paFragmentManager.updateAllFragmentsOnViewPager();
                                dataCache.updateAllPercents();

                                logicManager.insertDebtBorrow(person);
                                try {
                                    persons.remove(position);
                                } catch (IndexOutOfBoundsException e) {
                                    return;
                                }
                                break;
                            }
                        }
                        notifyItemRemoved(position);
                    }
                }
            });
        }

        private boolean isMumkin(DebtBorrow debt, String accountId, Double summ) {
            Account account = null;
            for (Account ac : accountDao.queryBuilder().list()) {
                if (ac.getId().matches(accountId)) {
                    account = ac;
                    break;
                }
            }
            if (account != null && (account.getIsLimited() || account.getNoneMinusAccount())) {
                double limit = account.getLimite();
                double accounted = logicManager.isLimitAccess(account, debt.getTakenDate());
                if (debt.getType() == DebtBorrow.DEBT) {
                    accounted = accounted - commonOperations.getCost(Calendar.getInstance(), debt.getCurrency(), account.getCurrency(), summ);
                } else {
                    accounted = accounted + commonOperations.getCost(Calendar.getInstance(), debt.getCurrency(), account.getCurrency(), summ);
                }
                if (account.getNoneMinusAccount()) {
                    if (accounted < 0) {
                        Toast.makeText(getContext(), R.string.none_minus_account_warning, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    if (-limit > accounted) {
                        Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
            return true;
        }

        private Bitmap queryContactImage(int imageDataRow) {
            Cursor c = getContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{
                    ContactsContract.CommonDataKinds.Photo.PHOTO
            }, ContactsContract.Data._ID + "=?", new String[]{
                    Integer.toString(imageDataRow)
            }, null);
            byte[] imageBytes = null;
            if (c != null) {
                if (c.moveToFirst()) {
                    imageBytes = c.getBlob(0);
                }
                c.close();
            }
            if (imageBytes != null) {
                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            } else {
                return null;
            }
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrow_fragment_mod, parent, false);
            return new ViewHolder(view);
        }
    }

    private void addNextFragment(DebtBorrow debtBorrow) {
        Bundle bundle = new Bundle();
        bundle.putString(DebtBorrowFragment.DEBT_BORROW_ID, debtBorrow.getId());
        bundle.putInt(DebtBorrowFragment.MODE, PocketAccounterGeneral.NO_MODE);
        InfoDebtBorrowFragment fragment = new InfoDebtBorrowFragment();
        fragment.setArguments(bundle);
        paFragmentManager.displayFragment(fragment);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout llItemDebtBorrowSms;
        public LinearLayout llItemDebtBorrowCall;
        public FrameLayout flItemDebtBorrowPay;
        public TextView BorrowPersonName;
        public TextView tvItemDebtBorrowLeft;
        public CircleImageView BorrowPersonPhotoPath;
        public TextView tvItemDebtBorrowLeftDate;
        public TextView pay;

        public ViewHolder(View view) {
            super(view);
            llItemDebtBorrowSms = (LinearLayout) view.findViewById(R.id.llItemDebtBorrowSms);
            llItemDebtBorrowCall = (LinearLayout) view.findViewById(R.id.llItemDebtBorrowCall);
            BorrowPersonName = (TextView) view.findViewById(R.id.tvBorrowPersonName);
            flItemDebtBorrowPay = (FrameLayout) view.findViewById(R.id.flItemDebtBorrowPay);
            tvItemDebtBorrowLeft = (TextView) view.findViewById(R.id.tvItemDebtBorrowLeft);
            tvItemDebtBorrowLeftDate = (TextView) view.findViewById(R.id.tvItemDebtBorrowLeftDate);
            BorrowPersonPhotoPath = (CircleImageView) view.findViewById(R.id.imBorrowPerson);
            pay = (TextView) view.findViewById(R.id.btBorrowPersonPay);
        }
    }
}