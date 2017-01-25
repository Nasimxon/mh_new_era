package com.jim.finansia.debt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.jim.finansia.database.BoardButtonDao;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.Recking;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.DatePicker;
import com.jim.finansia.utils.OperationsListDialog;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.cache.DataCache;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 6/7/2016.
 */

public class InfoDebtBorrowFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    @Inject
    DatePicker datePicker;
    @Inject
    OperationsListDialog operationsListDialog;
    @Inject
    LogicManager logicManager;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    CommonOperations commonOperations;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    @Named(value = "display_formatter")
    SimpleDateFormat dateFormat;
    @Inject
    DaoSession daoSession;
    @Inject
    DataCache dataCache;
    @Inject
    DecimalFormat formatter;
    WarningDialog warningDialog;
    DebtBorrowDao debtBorrowDao;
    AccountDao accountDao;
    DecimalFormat decimalFormat;
    NumberFormat numberFormat;
    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");

    private Context context;
    private TextView borrowName;
    private TextView leftAmount;
    private TextView borrowLeftDate;
    private TextView totalPayAmount;
    private TextView calculate;
    private TextView tvTotalsummInfo;
    private LinearLayout borrowPay;
    private CircleImageView circleImageView;
    private android.support.v7.widget.RecyclerView recyclerView;
    private String id = "";
    private PeysAdapter peysAdapter;
    private DebtBorrow debtBorrow;
    private ImageView deleteFrame;
    private ImageView pastgaOcil;
    private ImageView cancel_button;
    private ImageView debt_icon;
    private RelativeLayout infoFrame;
    //    private FrameLayout isHaveReking;
    private TextView tvInfoDebtBorrowTakeDate;
    private TextView payText;
    private boolean isCheks[];
    int mode = 1;
    private TextView phoneNumber;
    static int TYPE = 0;
    private int posMain = 0;
    private Spinner spinner;
    private RelativeLayout rlInfo;
    private LinearLayout llDebtBOrrowItemEdit;


    public static InfoDebtBorrowFragment getInstance(String id, int type) {

        InfoDebtBorrowFragment fragment = new InfoDebtBorrowFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("type", type);
        TYPE = type;
        fragment.setArguments(bundle);
        return fragment;
    }
    private PopupMenu popupMenu;
    private void showOperationsList(View v) {
        popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.toolbar_popup_debt);
        MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupMenu.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
//                    case R.id.edit:
//                               AddBorrowFragment addBorrowFragment = AddBorrowFragment.getInstance(TYPE, debtBorrow);
//
//                                if (!daoSession.getBoardButtonDao().queryBuilder()
//                                        .where(BoardButtonDao.Properties.CategoryId.eq(debtBorrow.getId()))
//                                        .list().isEmpty()) {
//                                    BoardButton boardButton = daoSession.getBoardButtonDao().queryBuilder()
//                                            .where(BoardButtonDao.Properties.CategoryId.eq(debtBorrow.getId()))
//                                            .list().get(0);
//                                    addBorrowFragment.setMainView(boardButton);
//                                }
//
//                                int count = paFragmentManager.getFragmentManager().getBackStackEntryCount();
//                                while (count > 0) {
//                                    paFragmentManager.getFragmentManager().popBackStack();
//                                    count--;
//                                }
//                                operationsListDialog.dismiss();
//                                paFragmentManager.displayFragment(addBorrowFragment);
//                        break;
                    case R.id.delete: {
                        warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (logicManager.deleteDebtBorrow(debtBorrow)) {
                                    case LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND: {
                                        Toast.makeText(getContext(), "No has debt", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case LogicManagerConstants.DELETED_SUCCESSFUL: {
                                        if (paFragmentManager.isMainReturn()) {
                                            paFragmentManager.displayMainWindow();
                                        } else {
                                            DebtBorrowFragment fragment = new DebtBorrowFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putInt("pos", debtBorrow.getTo_archive() ? 2 : debtBorrow.getType());
                                            fragment.setArguments(bundle);
                                            paFragmentManager.getFragmentManager().popBackStack();
                                            paFragmentManager.displayFragment(new DebtBorrowFragment());
                                        }
                                        List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
                                        for (BoardButton boardButton : boardButtons) {
                                            if (boardButton.getCategoryId() != null)
                                                if (boardButton.getCategoryId().equals(debtBorrow.getId())) {
                                                    if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                                                    else
                                                        logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                                                    commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());
                                                }
                                        }

                                        paFragmentManager.updateAllFragmentsOnViewPager();
                                        dataCache.updateAllPercents();
                                        break;
                                    }
                                }
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setText(debtBorrow.getCalculate() ?
                                getResources().getString(R.string.delete_credit) : getString(R.string.delete));
                        warningDialog.show();
                        break;
                    }

                }
                return false;
            }
        });
        popupMenu.show();
    }
    public void setMainItems(int pos) {
        posMain = pos;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.modern_debt_borrow_info, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        formater = new DecimalFormat("0.00");
        warningDialog = new WarningDialog(getContext());
        debtBorrowDao = daoSession.getDebtBorrowDao();
        accountDao = daoSession.getAccountDao();
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setDecimalFormatSymbols(symbols);
        borrowName = (TextView) view.findViewById(R.id.name_of_borrow);
        llDebtBOrrowItemEdit = (LinearLayout) view.findViewById(R.id.llDebtBOrrowItemEdit);
        leftAmount = (TextView) view.findViewById(R.id.tvAmountDebtBorrowInfo);
        borrowLeftDate = (TextView) view.findViewById(R.id.tvLeftDayDebtBorrowInfo);
        borrowPay = (LinearLayout) view.findViewById(R.id.btPayDebtBorrowInfo);
        deleteFrame = (ImageView) view.findViewById(R.id.flInfoDebtBorrowDeleted);
        debt_icon = (ImageView) view.findViewById(R.id.debt_icon);
        pastgaOcil = (ImageView) view.findViewById(R.id.pastgaOcil);
        cancel_button = (ImageView) view.findViewById(R.id.cancel_button);
        totalPayAmount = (TextView) view.findViewById(R.id.total_summ_debt_borrow);
        tvTotalsummInfo = (TextView) view.findViewById(R.id.tvInfoDebtBorrowTotalSumm);
        payText = (TextView) view.findViewById(R.id.paybut);
        phoneNumber = (TextView) view.findViewById(R.id.tvInfoDebtBorrowPhoneNumber);
        circleImageView = (CircleImageView) view.findViewById(R.id.imBorrowPerson);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvDebtBorrowInfo);
        infoFrame = (RelativeLayout) view.findViewById(R.id.flInfoDebtBorrowVisibl);
        tvInfoDebtBorrowTakeDate = (TextView) view.findViewById(R.id.tvInfoDebtBorrowTakeDate);
        infoFrame.setVisibility(View.GONE);
        calculate = (TextView) view.findViewById(R.id.tvInfoDebtBorrowIsCalculate);
        id = getArguments().getString("id");
        rlInfo = (RelativeLayout) view.findViewById(R.id.rlInfo);
        context = getContext();
        debtBorrow = new DebtBorrow();
        if (debtBorrowDao.queryBuilder().list() != null) {
            for (DebtBorrow db : debtBorrowDao.queryBuilder().list()) {
                if (db.getId().matches(id)) {
                    debtBorrow = db;
                    break;
                }
            }
        }

        isCheks = new boolean[debtBorrow.getReckings().size()];
        for (int i = 0; i < isCheks.length; i++) {
            isCheks[i] = false;
        }
        toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, new String[]{
                getResources().getString(R.string.delete)});
        spinner.setAdapter(arrayAdapter);

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = 1;
                for (int i = 0; i < isCheks.length; i++) {
                    isCheks[i] = false;
                    peysAdapter.notifyItemChanged(i);
                }
               cancel_button.setVisibility(View.GONE);
            }
        });
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
//        if (!debtBorrow.getTo_archive()) {
//            toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
//            toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String[] st = new String[2];
//                    st[0] = getResources().getString(R.string.edit);
//                    st[1] = getResources().getString(R.string.delete);
//                    operationsListDialog.setAdapter(st);
//                    operationsListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            if (position == 0) {
//                                AddBorrowFragment addBorrowFragment = AddBorrowFragment.getInstance(TYPE, debtBorrow);
//
//                                if (!daoSession.getBoardButtonDao().queryBuilder()
//                                        .where(BoardButtonDao.Properties.CategoryId.eq(debtBorrow.getId()))
//                                        .list().isEmpty()) {
//                                    BoardButton boardButton = daoSession.getBoardButtonDao().queryBuilder()
//                                            .where(BoardButtonDao.Properties.CategoryId.eq(debtBorrow.getId()))
//                                            .list().get(0);
//                                    addBorrowFragment.setMainView(boardButton);
//                                }
//
//                                int count = paFragmentManager.getFragmentManager().getBackStackEntryCount();
//                                while (count > 0) {
//                                    paFragmentManager.getFragmentManager().popBackStack();
//                                    count--;
//                                }
//                                operationsListDialog.dismiss();
//                                paFragmentManager.displayFragment(addBorrowFragment);
//                            } else {
//                                warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        warningDialog.dismiss();
//                                    }
//                                });
//                                warningDialog.setOnYesButtonListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        switch (logicManager.deleteDebtBorrow(debtBorrow)) {
//                                            case LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND: {
//                                                Toast.makeText(getContext(), "No this debt", Toast.LENGTH_SHORT).show();
//                                                operationsListDialog.dismiss();
//                                                break;
//                                            }
//                                            case LogicManagerConstants.DELETED_SUCCESSFUL: {
//                                                if (paFragmentManager.isMainReturn()) {
//                                                    paFragmentManager.displayMainWindow();
//                                                } else {
//                                                    paFragmentManager.getFragmentManager().popBackStack();
//                                                    paFragmentManager.displayFragment(new DebtBorrowFragment());
//                                                }
//                                                List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
//                                                for (BoardButton boardButton : boardButtons) {
//                                                    if (boardButton.getCategoryId() != null)
//                                                        if (boardButton.getCategoryId().equals(debtBorrow.getId())) {
//                                                            if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
//                                                                logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
//                                                            else
//                                                                logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
//                                                            commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());
//                                                        }
//                                                }
//                                                dataCache.updateAllPercents();
//                                                paFragmentManager.updateAllFragmentsOnViewPager();
//                                                operationsListDialog.dismiss();
//                                                break;
//                                            }
//                                        }
//                                        warningDialog.dismiss();
//                                    }
//                                });
//                                warningDialog.setText(debtBorrow.getCalculate() ?
//                                        getResources().getString(R.string.delete_credit) : getString(R.string.delete));
//                                warningDialog.show();
//                            }
//                        }
//                    });
//                    operationsListDialog.show();
//                }
//            });
//        } else {

//        }
//
        if(debtBorrow.getType() == DebtBorrow.BORROW){
            debt_icon.setImageResource(R.drawable.debt_icon);
            payText.setTextColor(Color.parseColor("#8cc156"));

        }else {
            debt_icon.setImageResource(R.drawable.borr);
            payText.setTextColor(Color.parseColor("#dc4849"));

        }
        calculate.setText(debtBorrow.getCalculate() ?
                getString(R.string.calc_in_balance) :
                getString(R.string.no_calc_in_balance));
        infoFrame.setVisibility(View.GONE);

        tvInfoDebtBorrowTakeDate.setText(sDateFormat.format(debtBorrow.getTakenDate().getTime()));
        phoneNumber.setText(debtBorrow.getPerson().getPhoneNumber());
        llDebtBOrrowItemEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddBorrowFragment addBorrowFragment = AddBorrowFragment.getInstance(TYPE, debtBorrow);
                if (!daoSession.getBoardButtonDao().queryBuilder()
                        .where(BoardButtonDao.Properties.CategoryId.eq(debtBorrow.getId()))
                        .list().isEmpty()) {
                    BoardButton boardButton = daoSession.getBoardButtonDao().queryBuilder()
                            .where(BoardButtonDao.Properties.CategoryId.eq(debtBorrow.getId()))
                            .list().get(0);
                    addBorrowFragment.setMainView(boardButton);
                }

                int count = paFragmentManager.getFragmentManager().getBackStackEntryCount();
                while (count > 0) {
                    paFragmentManager.getFragmentManager().popBackStack();
                    count--;
                }
                operationsListDialog.dismiss();
                paFragmentManager.displayFragment(addBorrowFragment);
            }
        });

        view.findViewById(R.id.infoooc).setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   if (infoFrame.getVisibility() == View.GONE) {
                                                                       rlInfo.setVisibility(View.GONE);
                                                                       infoFrame.setVisibility(View.VISIBLE);
                                                                       pastgaOcil.setImageResource(R.drawable.info_pastga);
//                                                                       info.setImageResource(R.drawable.pasga_ochil);
//                                                                       view.findViewById(R.id.with_wlyuzik).setVisibility(View.VISIBLE);
                                                                   } else {
                                                                       infoFrame.setVisibility(View.GONE);
                                                                       rlInfo.setVisibility(View.VISIBLE);
                                                                       pastgaOcil.setImageResource(R.drawable.info_open);
//                                                                       info.setImageResource(R.drawable.infoo);
//                                                                       view.findViewById(R.id.with_wlyuzik).setVisibility(view.GONE);
                                                                   }
                                                               }
                                                           }
        );
        view.findViewById(R.id.rlBottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoFrame.getVisibility() == View.GONE) {
                    infoFrame.setVisibility(View.VISIBLE);
                    rlInfo.setVisibility(View.GONE);
                    pastgaOcil.setImageResource(R.drawable.info_pastga);
                } else {
                    infoFrame.setVisibility(View.GONE);
                    rlInfo.setVisibility(View.VISIBLE);
                    pastgaOcil.setImageResource(R.drawable.info_open);
              }
            }
        });
        if (debtBorrow.getReckings().isEmpty()) {
//            isHaveReking.setVisibility(View.GONE);
        }

        peysAdapter = new PeysAdapter((ArrayList<Recking>) debtBorrow.getReckings());




        final List<Recking> list = debtBorrow.getReckings();
        double total = 0;
        for (Recking rc : list) {
            total += rc.getAmount();
        }
        if (debtBorrow.getTo_archive()) {
//            deleteFrame.setVisibility(View.GONE);
            borrowPay.setVisibility(View.INVISIBLE);
            llDebtBOrrowItemEdit.setVisibility(View.INVISIBLE);

            deleteFrame.setVisibility(View.INVISIBLE);
        }
        borrowName.setText(debtBorrow.getPerson().getName());
//        String qq = ((int) (debtBorrow.getAmount() - total)) == (debtBorrow.getAmount() - total)
//                ? "" + ((int) (debtBorrow.getAmount() - total)) : "" + (debtBorrow.getAmount() - total);
        double amount = debtBorrow.getAmount() - total;
        if (total >= debtBorrow.getAmount())
            leftAmount.setText(getResources().getString(R.string.repaid));
        else
            leftAmount.setText(formatter.format(amount) + debtBorrow.getCurrency().getAbbr());


        if (debtBorrow.getReturnDate() == null) {
            borrowLeftDate.setText(getResources().getString(R.string.no_date_selected));}
        else {
            int t[] = getDateDifferenceInDDMMYYYY(Calendar.getInstance().getTime(), debtBorrow.getReturnDate().getTime());
            if (t[0] * t[1] * t[2] < 0 && (t[0] + t[1] + t[2]) != 0) {
                borrowLeftDate.setText(R.string.ends);
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
                borrowLeftDate.setText(left_date_string);
            }}


        totalPayAmount.setText("" + formatter.format(total) + debtBorrow.getCurrency().getAbbr());
        if (total >= debtBorrow.getAmount()) {
            payText.setText(getResources().getString(R.string.to_archive));
//            deleteFrame.setVisibility(View.GONE);
        }

        if (!debtBorrow.getPerson().getPhoto().equals("") && !debtBorrow.getPerson().getPhoto().matches("0")) {
            try {
                circleImageView.setImageBitmap(queryContactImage(Integer.parseInt(debtBorrow.getPerson().getPhoto())));
            } catch (NumberFormatException e) {
                circleImageView.setImageDrawable(Drawable.createFromPath(debtBorrow.getPerson().getPhoto()));
            }
        } else {
            circleImageView.setImageResource(R.drawable.no_photo);
        }
        tvTotalsummInfo.setText("" + formatter.format(debtBorrow.getAmount()) + debtBorrow.getCurrency().getAbbr());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(peysAdapter);
        borrowPay.setOnClickListener(this);
        deleteFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == 1) mode = 0;
                else mode = 1;
                if (mode == 0) {
                    for (int i = 0; i < peysAdapter.getItemCount(); i++) {
                        peysAdapter.notifyItemChanged(i);
                    }
                    //as
                    cancel_button.setVisibility(View.VISIBLE);
//                    payText.setText(getResources().getString(R.string.cancel));
                } else {
                    boolean tek = false;
                    for (boolean isChek : isCheks) {
                        if (isChek) {
                            tek = true;
                        }
                    }
                    if (tek) {
                        warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mode = 0;
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (int i = isCheks.length - 1; i >= 0; i--) {
                                    if (isCheks[i]) {
                                        logicManager.deleteRecking(list.get(i));
                                        peysAdapter.itemDeleted(i);
                                    } else {
                                        peysAdapter.notifyItemChanged(i);
                                    }
                                }
                                isCheks = new boolean[debtBorrow.getReckings().size()];
                                for (int i = 0; i < isCheks.length; i++) {
                                    isCheks[i] = false;
                                }
//                                payText.setText(getResources().getString(R.string.payy));
                                cancel_button.setVisibility(View.GONE);
                                mode = 1;
                                peysAdapter.notifyDataSetChanged();
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.show();
                    } else {
                        mode = 1;
                        for (int i = 0; i < isCheks.length; i++) {
                            isCheks[i] = false;
                            peysAdapter.notifyItemChanged(i);
                        }
//                        payText.setText(getResources().getString(R.string.payy));
                        cancel_button.setVisibility(View.GONE);
                    }

                }
            }
        });
        return view;
    }
    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOperationsList(v);
                }
            });
            toolbarManager.setImageToHomeButton(R.drawable.ic_drawer);
            toolbarManager.setSubtitle("");
        }
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

    boolean tek = false;
    DecimalFormat formater;
    private void openDialog() {
        if (!payText.getText().toString().matches(getResources().getString(R.string.to_archive))) {
            final Dialog dialog = new Dialog(getActivity());
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView);
            View v = dialog.getWindow().getDecorView();
            v.setBackgroundResource(android.R.color.transparent);

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
            abbrrAmount.setText(debtBorrow.getCurrency().getAbbr());
            final String[] accaounts = new String[accountDao.queryBuilder().list().size()];
            for (int i = 0; i < accaounts.length; i++) {
                accaounts[i] = accountDao.queryBuilder().list().get(i).getName();
            }
            tvResidue.setText(getString(R.string.left)+":");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getContext(), R.layout.spiner_gravity_left, accaounts);

            accountSp.setAdapter(arrayAdapter);
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

            double v1 = debtBorrow.getAmount();
            double totalAm = 0;
            for (Recking recking:debtBorrow.getReckings()){
                totalAm +=recking.getAmount();
            }
            final double lAmount = debtBorrow.getAmount() - totalAm;
            shouldPayPeriod.setText(decimalFormat.format(v1-totalAm)+debtBorrow.getCurrency().getAbbr());
            if(debtBorrow.getType() == DebtBorrow.DEBT){
                debetorName.setText(getString(R.string.debtor_name)+":");
                periodDate.setText(debtBorrow.getPerson().getName());
            }
            else {
                debetorName.setText(getString(R.string.borrower_name)+":");
                periodDate.setText(debtBorrow.getPerson().getName());

            }

            if (!debtBorrow.getCalculate()) {
                dialogView.findViewById(R.id.is_calc).setVisibility(View.GONE);
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
                    if (date.compareTo(debtBorrow.getTakenDate()) < 0) {
                        date.setTime(debtBorrow.getTakenDate().getTime());
                        enterDate.setError(getResources().getString(R.string.incorrect_date));
                    } else enterDate.setError(null);
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
                    tek = true;
                    int len = debtBorrow.getCurrency().getAbbr().length();
                    if (!enterPay.getText().toString().isEmpty() && Double.parseDouble(enterPay.getText().toString()) != 0) {
                        if (keyForInclude.isChecked() && isMumkin(debtBorrow, accountDao.queryBuilder().list().
                                get(accountSp.getSelectedItemPosition()).getId(), Double.parseDouble(enterPay.getText().toString())))
                            tek = true;
                        if (!keyForInclude.isChecked()) tek = true;

                        if (lAmount - Double.parseDouble(enterPay.getText().toString()) < 0) {
                            warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    warningDialog.dismiss();
                                }
                            });
                            warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (tek) {
                                        if(keyForInclude.isChecked())
                                        peysAdapter.setDataChanged(date, Double.parseDouble(enterPay.getText().toString()),
                                                accountDao.queryBuilder().list().
                                                        get(accountSp.getSelectedItemPosition()).getId(), comment.getText().toString());
                                        else
                                        peysAdapter.setDataChanged(date, Double.parseDouble(enterPay.getText().toString()),"", comment.getText().toString());

                                    }
                                    warningDialog.dismiss();
                                    dialog.dismiss();
                                }
                            });
                            warningDialog.setText(getResources().getString(R.string.incorrect_pay));

                            if (tek) {
                                warningDialog.show();
                            }
                        } else {
                            if (tek) {
                                if(keyForInclude.isChecked())
                                peysAdapter.setDataChanged(date, Double.parseDouble(enterPay.getText().toString()),
                                        accountDao.queryBuilder().list().
                                                get(accountSp.getSelectedItemPosition()).getId(), comment.getText().toString());
                                else
                                    peysAdapter.setDataChanged(date, Double.parseDouble(enterPay.getText().toString()),"", comment.getText().toString());

                                warningDialog.dismiss();
                                dialog.dismiss();
                            }
                        }
                    } else {
                        enterPay.setError(getResources().getString(R.string.enter_pay_value));
                    }
                }
            });
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            dialog.getWindow().setLayout(7 * width / 8, RelativeLayout.LayoutParams.WRAP_CONTENT);
            dialog.show();
        } else {
            debtBorrow.setTo_archive(true);
            logicManager.insertDebtBorrow(debtBorrow);
            int count = paFragmentManager.getFragmentManager().getBackStackEntryCount();
            while (count > 0) {
                paFragmentManager.getFragmentManager().popBackStack();
                count--;
            }
            paFragmentManager.displayFragment(new DebtBorrowFragment());
            List<BoardButton> boardButtons = daoSession.getBoardButtonDao().loadAll();
            for (BoardButton boardButton : boardButtons) {
                if (boardButton.getCategoryId() != null)
                    if (boardButton.getCategoryId().equals(debtBorrow.getId())) {
                        if (boardButton.getTable() == PocketAccounterGeneral.EXPENSE)
                            logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, boardButton.getPos(), null);
                        else {
                            logicManager.changeBoardButton(PocketAccounterGeneral.INCOME, boardButton.getPos(), null);
                        }
                        commonOperations.changeIconToNull(boardButton.getPos(), dataCache, boardButton.getTable());
                    }
            }
            paFragmentManager.updateAllFragmentsOnViewPager();
            dataCache.updateAllPercents();
        }
    }

    @Override
    public void onClick(View v) {
//        if (payText.getText().toString().matches(getResources().getString(R.string.cancel))) {
//
//        } else {
            openDialog();
//        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
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

    private class PeysAdapter extends RecyclerView.Adapter<InfoDebtBorrowFragment.ViewHolder> {
        private ArrayList<Recking> list;

        public PeysAdapter(ArrayList<Recking> list) {
            this.list = list;
        }

        public int getItemCount() {
            return list.size();
        }

        public void onBindViewHolder(final InfoDebtBorrowFragment.ViewHolder view, final int position) {
            view.infoDate.setText(dateFormat.format(list.get(position).getPayDate().getTime()));
            view.infoSumm.setText("" + formatter.format(list.get(position).getAmount())+ "" + debtBorrow.getCurrency().getAbbr());
            if (list.get(position).getAccountId().equals("")) {
                view.infoAccount.setText(R.string.ne_uchitavaetsya);
            } else {
                for (Account account : accountDao.queryBuilder().list()) {
                    if (account.getId().matches(list.get(position).getAccountId())) {
                        view.infoAccount.setText(getString(R.string.by) + account.getName());
                        break;
                    }
                }
            }
            if (mode == 0) {
                view.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.checkBox.setChecked(!view.checkBox.isChecked());
                        isCheks[position] = !isCheks[position];
                    }
                });
            } else {
                view.rootView.setOnClickListener(null);
            }
            if (!list.get(position).getComment().matches("")) {
                view.comment.setText(getResources().getString(R.string.comment) + ": " + list.get(position).getComment());
            } else {
                view.comment.setVisibility(View.GONE);
            }
            if (mode == 1) {
                view.checkBox.setVisibility(View.GONE);
            } else {
                view.checkBox.setVisibility(View.VISIBLE);
            }
            view.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.checkBox.setChecked(isCheks[position]);
                    if (view.checkBox.isChecked()) {
                        view.checkBox.setChecked(false);
                    } else {
                        view.checkBox.setChecked(true);
                    }
                    isCheks[position] = view.checkBox.isChecked();
                }
            });
            view.checkBox.setChecked(isCheks[position]);
        }

        public InfoDebtBorrowFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payed_item, parent, false);
            return new ViewHolder(view);
        }

        public void itemDeleted(int position) {
            list.remove(position);
            notifyItemRemoved(position);
            double total = 0;
            for (Recking rc : list) {
                total += rc.getAmount();
            }
            totalPayAmount.setText(total + debtBorrow.getCurrency().getAbbr());
//            String qq = ((int) (debtBorrow.getAmount() - total)) == (debtBorrow.getAmount() - total)
//                    ? "" + ((int) (debtBorrow.getAmount() - total)) : "" + (debtBorrow.getAmount() - total);
            double amount = debtBorrow.getAmount() - total;
            leftAmount.setText(formatter.format(amount) + "" + debtBorrow.getCurrency().getAbbr());
            if (debtBorrow.getReckings().isEmpty()) {
//                isHaveReking.setVisibility(View.GONE);
            }
            dataCache.updateAllPercents();
            paFragmentManager.updateAllFragmentsOnViewPager();
        }

        public void setDataChanged(Calendar clDate, double value, String accountId, String comment) {

            Recking recking = new Recking(clDate, value, debtBorrow.getId(), accountId, comment);
            debtBorrow.getReckings().add(0, recking);
            logicManager.insertReckingDebt(recking);
            double qoldiq = 0;
            for (int i = 0; i < list.size(); i++) {
                qoldiq += list.get(i).getAmount();
            }
//            String qq = ((int) (debtBorrow.getAmount() - qoldiq)) == (debtBorrow.getAmount() - qoldiq)
//                    ? ("" + ((int) (debtBorrow.getAmount() - qoldiq))) : ("" + (debtBorrow.getAmount() - qoldiq));
            double amount = debtBorrow.getAmount() - qoldiq;
            leftAmount.setText(formatter.format(amount) + "" + debtBorrow.getCurrency().getAbbr());
            totalPayAmount.setText("" + formatter.format(qoldiq) + "" + debtBorrow.getCurrency().getAbbr());

            if (qoldiq >= debtBorrow.getAmount()) {
                payText.setText(getResources().getString(R.string.to_archive));
//                deleteFrame.setVisibility(View.GONE);
                leftAmount.setText(getResources().getString(R.string.repaid));
            }
            logicManager.insertReckingDebt(recking);
//            isHaveReking.setVisibility(View.VISIBLE);
            notifyItemInserted(0);
            isCheks = new boolean[list.size()];
            for (int i = 0; i < isCheks.length; i++) {
                isCheks[i] = false;
            }
            dataCache.updateAllPercents();
            paFragmentManager.updateAllFragmentsOnViewPager();
        }
    }

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public TextView infoDate;
        public TextView infoSumm;
        public TextView infoAccount;
        public TextView comment;
        public CheckBox checkBox;
        public View rootView;

        public ViewHolder(View view) {
            super(view);
            infoDate = (TextView) view.findViewById(R.id.date_of_trans);
            infoAccount = (TextView) view.findViewById(R.id.via_acount);
            comment = (TextView) view.findViewById(R.id.comment_trans);
            infoSumm = (TextView) view.findViewById(R.id.paid_value);
            checkBox = (CheckBox) view.findViewById(R.id.for_delete_check_box);
            rootView = view.findViewById(R.id.rlRootView);
        }
    }
}