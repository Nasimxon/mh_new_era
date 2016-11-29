package com.jim.pocketaccounter.debt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Visibility;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
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
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.Recking;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.utils.DatePicker;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.cache.DataCache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 6/4/2016.
 */

public class BorrowFragment extends Fragment {
    @Inject
    DatePicker datePicker;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    LogicManager logicManager;
    @Inject
    CommonOperations commonOperations;
    @Inject
    DaoSession daoSession;
    @Inject
    DataCache dataCache;
    WarningDialog warningDialog;
    DebtBorrowDao debtBorrowDao;
    AccountDao accountDao;
    TextView ifListEmpty;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter myAdapter;

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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TYPE = getArguments().getInt("type", 0);
        warningDialog = new WarningDialog(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.borrow_fragment_layout, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
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


    public void setDebtBorrowFragment(DebtBorrowFragment debtBorrowFragment) {
        this.debtBorrowFragment = debtBorrowFragment;
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
                //TODO Har bittasini zapisi bomi yomi tekshirish kere
                String isEmpty = "";
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

        public void onBindViewHolder(final ViewHolder view, final int position) {
            final int t = 0;
            final DebtBorrow person = persons.get(Math.abs(t - position));
            if (person.getType() == DebtBorrow.DEBT) {
                view.flItemDebtBorrowPay.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.red_pay_background));
                view.pay.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
//                view.rl.setBackgroundResource(R.color.grey_light_red);
//                view.fl.setBackgroundResource(R.color.grey_light_red);
            }
            try {
                view.BorrowPersonName.setText(person.getPerson().getName());
//                view.BorrowPersonNumber.setText(person.getPerson().getPhoneNumber());
//                view.BorrowPersonDateGet.setText(dateFormat.format(person.getTakenDate().getTime()));
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
                if (person.getPerson().getPhoneNumber().matches("")) {
//                    view.call.setVisibility(View.INVISIBLE);
                }
            } catch (NullPointerException e) {
            }
            double qq = 0;
            if (person.getReckings() != null) {
                for (Recking rk : person.getReckings()) {
                    qq += rk.getAmount();
                }
            }

            // left Date
            Calendar currentDate = Calendar.getInstance();
            int day = 0;
            int mounth = 0;
            int year = 0;

            if (person.getReturnDate() != null && currentDate.compareTo(person.getReturnDate()) <= 0) {
                if (currentDate.get(Calendar.DAY_OF_MONTH) <= person.getReturnDate().get(Calendar.DAY_OF_MONTH)) {
                    day = person.getReturnDate().get(Calendar.DAY_OF_MONTH) - currentDate.get(Calendar.DAY_OF_MONTH);
                    if (currentDate.get(Calendar.MONTH) <= person.getReturnDate().get(Calendar.MONTH)) {
                        mounth = person.getReturnDate().get(Calendar.MONTH) - currentDate.get(Calendar.MONTH);
                        if (currentDate.get(Calendar.YEAR) <= person.getReturnDate().get(Calendar.YEAR)) {
                            year = person.getReturnDate().get(Calendar.YEAR) - currentDate.get(Calendar.YEAR);
                        }
                    } else {
                        mounth = person.getReturnDate().get(Calendar.MONTH) + 12 - currentDate.get(Calendar.MONTH);
                        year = person.getReturnDate().get(Calendar.YEAR) - 1 - currentDate.get(Calendar.YEAR);
                    }
                } else {
                    person.getReturnDate().add(Calendar.MONTH, -1);
                    day = person.getReturnDate().get(Calendar.DAY_OF_MONTH) + person.getReturnDate()
                            .getActualMaximum(Calendar.MONTH) - currentDate.get(Calendar.DAY_OF_MONTH);
                    if (person.getReturnDate().get(Calendar.MONTH) >= currentDate.get(Calendar.MONTH)) {
                        mounth = person.getReturnDate().get(Calendar.MONTH) - currentDate.get(Calendar.MONTH);
                        if (person.getReturnDate().get(Calendar.YEAR) >= currentDate.get(Calendar.YEAR)) {
                            year = person.getReturnDate().get(Calendar.YEAR) - currentDate.get(Calendar.YEAR);
                        }
                    } else {
                        mounth = person.getReturnDate().get(Calendar.MONTH) + 12 - currentDate.get(Calendar.MONTH);
                        if (person.getReturnDate().get(Calendar.YEAR) > currentDate.get(Calendar.YEAR)) {
                            year = person.getReturnDate().get(Calendar.YEAR) - 1 - currentDate.get(Calendar.YEAR);
                        }
                    }
                }
            }
            String sana = (year != 0 ? year + " " + getString(R.string.year) : "")
                    + (mounth != 0 ? mounth + " " + getString(R.string.moth) : "")
                    + (day != 0 ? day + " " + getString(R.string.day) : "");
            if (person.getReturnDate() == null) {
                view.tvItemDebtBorrowLeftDate.setText(getResources().getString(R.string.no_date_selected));
            } else
                view.tvItemDebtBorrowLeftDate.setText(sana);

//            if(persons.get(position).getTo_archive())
//                view.forCango.setVisibility(View.GONE);
//            else view.forCango.setVisibility(View.VISIBLE);
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

            double template = persons.get(position).getAmount() / 100;
            int procet = (int) (qq / template);
//            view.frameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, procet));

            String ss = (person.getAmount() - qq) == (int) (person.getAmount() - qq) ? "" + (int) (person.getAmount() - qq) : "" + (person.getAmount() - qq);
            if (person.getTo_archive() || qq >= person.getAmount()) {
                view.tvItemDebtBorrowLeft.setText(getResources().getString(R.string.repaid));
            } else
                view.tvItemDebtBorrowLeft.setText(ss + person.getCurrency().getAbbr());

            view.itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
//                    Fragment fragment = InfoDebtBorrowFragment.getInstance(persons.get(Math.abs(t - position)).getId(), TYPE);
//                    paFragmentManager.getFragmentManager().popBackStack();
//                    paFragmentManager.displayFragment(fragment);
                    addNextFragment(person, view.BorrowPersonPhotoPath, false);
                }
            });
            if (TYPE == 2) {
                view.pay.setVisibility(View.GONE);
            } else {
                double total = 0;
                for (Recking rec : person.getReckings()) {
                    total += rec.getAmount();
                }
                if (total >= person.getAmount()) {
                    view.pay.setText(getString(R.string.archive));
                } else view.pay.setText(getString(R.string.payy));
            }

            view.flItemDebtBorrowPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!view.pay.getText().toString().matches(getString(R.string.archive))) {
                        final Dialog dialog = new Dialog(getActivity());
                        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(dialogView);
                        final EditText enterDate = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowDate);
                        final EditText enterPay = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPaySumm);
                        final EditText comment = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPayComment);
                        final Spinner accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);
                        RelativeLayout relativeLayout = (RelativeLayout) dialogView.findViewById(R.id.is_calc);
                        if (!person.getCalculate()) {
                            relativeLayout.setVisibility(View.GONE);
                        }
                        String[] accaounts = new String[accountDao.queryBuilder().list().size()];
                        for (int i = 0; i < accaounts.length; i++) {
                            accaounts[i] = accountDao.queryBuilder().list().get(i).getName();
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                getContext(), R.layout.spiner_gravity_right, accaounts);

                        accountSp.setAdapter(arrayAdapter);

                        ImageView cancel = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
                        ImageView save = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);
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
                                if (person.getCalculate()) {
                                    for (Account account : accountDao.queryBuilder().list()) {
                                        if (account.getName().matches(accountSp.getSelectedItem().toString())) {
                                            ac = account.getId();
                                            break;
                                        }
                                    }
                                }
                                boolean tek = false;
                                if (!enterPay.getText().toString().isEmpty()) {
                                    int len = person.getCurrency().getAbbr().length();
                                    if (Double.parseDouble(view.tvItemDebtBorrowLeft.getText().toString().substring(0, view.tvItemDebtBorrowLeft.getText().toString().length() - len))
                                            - Double.parseDouble(enterPay.getText().toString()) < 0) {
                                        if (person.getCalculate() && isMumkin(person, ac, Double.parseDouble(enterPay.getText().toString())))
                                            tek = true;
                                        if (!person.getCalculate()) tek = true;

                                        final String finalAc = ac;
                                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (person.getCalculate()) {
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
                                                    double template = person.getAmount() / 100;
                                                    int procet = (int) (total / template);
//                                                    view.frameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, procet));
                                                    if (person.getAmount() <= total) {
                                                        view.pay.setText(getString(R.string.archive));
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
                                                    double template = person.getAmount() / 100;
                                                    int procet = (int) (total / template);
//                                                    view.frameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, procet));
                                                    if (person.getAmount() <= total) {
                                                        view.pay.setText(getString(R.string.archive));
                                                    }
                                                    view.tvItemDebtBorrowLeft.setText(getResources().getString(R.string.repaid));
                                                    dialog.dismiss();
                                                }
                                                paFragmentManager.updateAllFragmentsOnViewPager();
                                                dataCache.updateAllPercents();
                                                warningDialog.dismiss();
                                            }
                                        });
                                        warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                warningDialog.dismiss();
                                            }
                                        });
                                        if (tek) {
                                            warningDialog.show();
                                        }
                                    } else {
                                        Recking recking;
                                        if (person.getCalculate() && isMumkin(person, ac, Double.parseDouble(enterPay.getText().toString()))) {
                                            recking = new Recking(date,
                                                    Double.parseDouble(enterPay.getText().toString()),
                                                    person.getId(), ac,
                                                    comment.getText().toString());
                                            person.getReckings().add(0, recking);
                                            double total = 0;
                                            for (Recking recking1 : person.getReckings()) {
                                                total += recking1.getAmount();
                                            }
                                            double template = person.getAmount() / 100;
                                            int procet = (int) (total / template);
//                                            view.frameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, procet));
                                            if (person.getAmount() <= total) {
                                                view.pay.setText(getString(R.string.archive));
                                            }
                                            logicManager.insertReckingDebt(recking);
                                            view.tvItemDebtBorrowLeft.setText("" + ((person.getAmount() - total) ==
                                                    ((int) (person.getAmount() - total)) ?
                                                    ("" + (int) (person.getAmount() - total)) :
                                                    ("" + (person.getAmount() - total))) + person.getCurrency().getAbbr());
                                            paFragmentManager.updateAllFragmentsOnViewPager();
                                            dataCache.updateAllPercents();
                                            dialog.dismiss();
                                        } else {
                                            if (!person.getCalculate()) {
                                                recking = new Recking(date,
                                                        Double.parseDouble(enterPay.getText().toString()),
                                                        person.getId(), comment.getText().toString());
                                                person.getReckings().add(0, recking);
                                                double total = 0;
                                                for (Recking recking1 : person.getReckings()) {
                                                    total += recking1.getAmount();
                                                }
                                                if (person.getAmount() <= total) {
                                                    view.pay.setText(getString(R.string.archive));
                                                }
                                                double template = person.getAmount() / 100;
                                                int procet = (int) (total / template);
//                                                view.frameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, procet));

                                                logicManager.insertReckingDebt(recking);
                                                view.tvItemDebtBorrowLeft.setText("" + ((person.getAmount() - total) ==
                                                        ((int) (person.getAmount() - total)) ?
                                                        ("" + (int) (person.getAmount() - total)) :
                                                        ("" + (person.getAmount() - total))) + person.getCurrency().getAbbr());
                                                paFragmentManager.updateAllFragmentsOnViewPager();
                                                dataCache.updateAllPercents();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addNextFragment(DebtBorrow debtBorrow, ImageView squareBlue, boolean overlap) {
        Fragment fragment = InfoDebtBorrowFragment.getInstance(debtBorrow.getId(), TYPE);

        Fade slideTransition = new Fade(Gravity.LEFT);
        slideTransition.setMode(Visibility.MODE_IN);
        ChangeBounds changeBoundsTransition = new ChangeBounds();
        slideTransition.setDuration(150);
        changeBoundsTransition.setDuration(150);
        fragment.setEnterTransition(slideTransition);
        fragment.setAllowEnterTransitionOverlap(overlap);
        fragment.setAllowReturnTransitionOverlap(overlap);
        fragment.setSharedElementEnterTransition(changeBoundsTransition);

        int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
        while (count > 0) {
            getActivity().getSupportFragmentManager().popBackStack();
            count--;
        }

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.flMain, fragment)
                .addToBackStack(null)
                .addSharedElement(squareBlue, "imageView")
                .commit();
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

    public void changeList() {
        MyAdapter adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
    }
}