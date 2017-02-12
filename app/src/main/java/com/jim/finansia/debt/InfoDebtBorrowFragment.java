package com.jim.finansia.debt;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.Recking;
import com.jim.finansia.fragments.RecordDetailFragment;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.LogicManagerConstants;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ReportManager;
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

public class InfoDebtBorrowFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    @Inject DatePicker datePicker;
    @Inject OperationsListDialog operationsListDialog;
    @Inject LogicManager logicManager;
    @Inject ToolbarManager toolbarManager;
    @Inject CommonOperations commonOperations;
    @Inject PAFragmentManager paFragmentManager;
    @Inject @Named(value = "display_formatter") SimpleDateFormat dateFormat;
    @Inject DaoSession daoSession;
    @Inject DataCache dataCache;
    @Inject ReportManager reportManager;
    private WarningDialog warningDialog;
    private DecimalFormat decimalFormat = new DecimalFormat("0.##");
    private LinearLayout llDebtBOrrowItemEdit;
    private NumberFormat numberFormat;
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    private String id;
    private int mode = PocketAccounterGeneral.NO_MODE;
    private TextView tvBorrowName, tvLeftAmount, tvBorrowLeftDate, tvTotalPaid, calculate, tvTotalsummInfo, tvPhoneNumber;
    private LinearLayout llDebtBorrowPay;
    private ImageView ivDelete;
    private ImageView pastgaOcil;
    private ImageView cancel_button;
    private int deletingMode = 1;
    private CircleImageView circleImageView;
    private android.support.v7.widget.RecyclerView recyclerView;
    private PeysAdapter peysAdapter;
    private DebtBorrow debtBorrow;
    private ImageView debt_icon;
    private RelativeLayout infoFrame;
    private TextView tvInfoDebtBorrowTakeDate;
    private TextView payText;
    private boolean isCheks[];
    private int posMain = 0;
    private RelativeLayout rlInfo;
    private PopupMenu popupMenu;
    private int localAppereance = DebtBorrowFragment.FROM_MAIN;
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.modern_debt_borrow_info, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        if (getArguments() != null) {
            id = getArguments().getString(DebtBorrowFragment.DEBT_BORROW_ID);
            mode = getArguments().getInt(DebtBorrowFragment.MODE);
            localAppereance = getArguments().getInt(DebtBorrowFragment.LOCAL_APPEREANCE);
        }
        warningDialog = new WarningDialog(getContext());
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setDecimalFormatSymbols(symbols);
        tvBorrowName = (TextView) view.findViewById(R.id.name_of_borrow);
        llDebtBOrrowItemEdit = (LinearLayout) view.findViewById(R.id.llDebtBOrrowItemEdit);
        tvLeftAmount = (TextView) view.findViewById(R.id.tvAmountDebtBorrowInfo);
        tvBorrowLeftDate = (TextView) view.findViewById(R.id.tvLeftDayDebtBorrowInfo);
        llDebtBorrowPay = (LinearLayout) view.findViewById(R.id.btPayDebtBorrowInfo);
        ivDelete = (ImageView) view.findViewById(R.id.flInfoDebtBorrowDeleted);
        debt_icon = (ImageView) view.findViewById(R.id.debt_icon);
        pastgaOcil = (ImageView) view.findViewById(R.id.pastgaOcil);
        cancel_button = (ImageView) view.findViewById(R.id.cancel_button);
        tvTotalPaid = (TextView) view.findViewById(R.id.total_summ_debt_borrow);
        tvTotalsummInfo = (TextView) view.findViewById(R.id.tvInfoDebtBorrowTotalSumm);
        payText = (TextView) view.findViewById(R.id.paybut);

        tvPhoneNumber = (TextView) view.findViewById(R.id.tvInfoDebtBorrowPhoneNumber);
        circleImageView = (CircleImageView) view.findViewById(R.id.imBorrowPerson);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvDebtBorrowInfo);
        infoFrame = (RelativeLayout) view.findViewById(R.id.flInfoDebtBorrowVisibl);
        tvInfoDebtBorrowTakeDate = (TextView) view.findViewById(R.id.tvInfoDebtBorrowTakeDate);
        infoFrame.setVisibility(View.GONE);
        calculate = (TextView) view.findViewById(R.id.tvInfoDebtBorrowIsCalculate);
        rlInfo = (RelativeLayout) view.findViewById(R.id.rlInfo);
        debtBorrow = new DebtBorrow();
        List<DebtBorrow> allDebtBorrows = daoSession.loadAll(DebtBorrow.class);
        for (DebtBorrow db : allDebtBorrows) {
            if (db.getId().matches(id)) {
                debtBorrow = db;
                break;
            }
        }
        isCheks = new boolean[debtBorrow.getReckings().size()];

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletingMode = 1;
                for (int i = 0; i < isCheks.length; i++) {
                    isCheks[i] = false;
                    peysAdapter.notifyItemChanged(i);
                }
                cancel_button.setVisibility(View.GONE);
            }
        });
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
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
        tvPhoneNumber.setText(debtBorrow.getPerson().getPhoneNumber());
        llDebtBOrrowItemEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(DebtBorrowFragment.DEBT_BORROW_ID, debtBorrow.getId());
                bundle.putInt(DebtBorrowFragment.MODE, mode);
                bundle.putInt(DebtBorrowFragment.POSITION, 0);
                bundle.putInt(DebtBorrowFragment.TYPE, debtBorrow.getType());
                bundle.putInt(DebtBorrowFragment.LOCAL_APPEREANCE, DebtBorrowFragment.FROM_INFO);
                AddBorrowFragment fragment = new AddBorrowFragment();
                fragment.setArguments(bundle);
                paFragmentManager.displayFragment(fragment);
                operationsListDialog.dismiss();
            }
        });

        view.findViewById(R.id.infoooc).setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   if (infoFrame.getVisibility() == View.GONE) {
                                                                       rlInfo.setVisibility(View.GONE);
                                                                       infoFrame.setVisibility(View.VISIBLE);
                                                                       pastgaOcil.setImageResource(R.drawable.info_pastga);
                                                                   } else {
                                                                       infoFrame.setVisibility(View.GONE);
                                                                       rlInfo.setVisibility(View.VISIBLE);
                                                                       pastgaOcil.setImageResource(R.drawable.info_open);
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

        peysAdapter = new PeysAdapter((ArrayList<Recking>) debtBorrow.getReckings());

        final List<Recking> list = debtBorrow.getReckings();
        double total = 0;
        for (Recking rc : list) {
            total += rc.getAmount();
        }
        if (debtBorrow.getTo_archive()) {
            llDebtBorrowPay.setVisibility(View.INVISIBLE);
            llDebtBOrrowItemEdit.setVisibility(View.INVISIBLE);
            ivDelete.setVisibility(View.INVISIBLE);
        }
        tvBorrowName.setText(debtBorrow.getPerson().getName());
        double amount = debtBorrow.getAmount() - total;
        if (total >= debtBorrow.getAmount())
            tvLeftAmount.setText(getResources().getString(R.string.repaid));
        else
            tvLeftAmount.setText(decimalFormat.format(amount) + debtBorrow.getCurrency().getAbbr());


        if (debtBorrow.getReturnDate() == null) {
            tvBorrowLeftDate.setText(getResources().getString(R.string.no_date_selected));}
        else {
            int t[] = getDateDifferenceInDDMMYYYY(Calendar.getInstance().getTime(), debtBorrow.getReturnDate().getTime());
            if (t[0] * t[1] * t[2] < 0 && (t[0] + t[1] + t[2]) != 0) {
                tvBorrowLeftDate.setText(R.string.ends);
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
                tvBorrowLeftDate.setText(left_date_string);
            }}


        tvTotalPaid.setText("" + decimalFormat.format(total) + debtBorrow.getCurrency().getAbbr());
        if (total >= debtBorrow.getAmount()) {
            payText.setText(getResources().getString(R.string.to_archive));
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
        tvTotalsummInfo.setText("" + decimalFormat.format(debtBorrow.getAmount()) + debtBorrow.getCurrency().getAbbr());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(peysAdapter);
        llDebtBorrowPay.setOnClickListener(this);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deletingMode == 1) deletingMode = 0;
                else deletingMode = 1;
                if (deletingMode == 0) {
                    for (int i = 0; i < peysAdapter.getItemCount(); i++) {
                        peysAdapter.notifyItemChanged(i);
                    }
                    cancel_button.setVisibility(View.VISIBLE);
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
                                deletingMode = 0;
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
                                cancel_button.setVisibility(View.GONE);
                                deletingMode = 1;
                                peysAdapter.notifyDataSetChanged();
                                reportManager.clearCache();
                                dataCache.updateAllPercents();
                                paFragmentManager.updateAllFragmentsOnViewPager();
                                paFragmentManager.updateAllFragmentsPageChanges();
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.show();
                    } else {
                        deletingMode = 1;
                        for (int i = 0; i < isCheks.length; i++) {
                            isCheks[i] = false;
                            peysAdapter.notifyItemChanged(i);
                        }
                        cancel_button.setVisibility(View.GONE);
                    }

                }
            }
        });
        return view;
    }

    public void updateToolbar() {
        if (toolbarManager != null) {
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setTitle(getResources().getString(R.string.debts_title));
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
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

    private void showOperationsList(View v) {
        popupMenu = new PopupMenu(getContext(), v);
        if (debtBorrow.getTo_archive())
            popupMenu.inflate(R.menu.toolbar_popup_without_delete);
        else
            popupMenu.inflate(R.menu.toolbar_popup_debt);
        MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupMenu.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit:
                        Bundle bundle = new Bundle();
                        bundle.putString(DebtBorrowFragment.DEBT_BORROW_ID, debtBorrow.getId());
                        bundle.putInt(DebtBorrowFragment.MODE, PocketAccounterGeneral.NO_MODE);
                        bundle.putInt(DebtBorrowFragment.POSITION, 0);
                        bundle.putInt(DebtBorrowFragment.TYPE, debtBorrow.getType());
                        bundle.putInt(DebtBorrowFragment.LOCAL_APPEREANCE, DebtBorrowFragment.FROM_INFO);
                        final AddBorrowFragment fragment = new AddBorrowFragment();
                        fragment.setArguments(bundle);
                        operationsListDialog.dismiss();
                        paFragmentManager.displayFragment(fragment);
                        break;
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
                                        if (mode == PocketAccounterGeneral.MAIN) {
                                            paFragmentManager.displayMainWindow();
                                        } else if (mode == PocketAccounterGeneral.DETAIL){
                                            Bundle bundle = new Bundle();
                                            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                                            bundle.putString(RecordDetailFragment.DATE, format.format(dataCache.getEndDate().getTime()));
                                            RecordDetailFragment fragment = new RecordDetailFragment();
                                            fragment.setArguments(bundle);
                                            paFragmentManager.getFragmentManager().popBackStack();
                                        } else if (mode == PocketAccounterGeneral.NO_MODE) {
                                            DebtBorrowFragment fragment = new DebtBorrowFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putInt("pos", debtBorrow.getTo_archive() ? 2 : debtBorrow.getType());
                                            fragment.setArguments(bundle);
                                            boolean found = false;
                                            for (Fragment frag : paFragmentManager.getFragmentManager().getFragments()) {
                                                if (frag instanceof  BorrowFragment) {
                                                    BorrowFragment borrowFragment = (BorrowFragment) frag;
                                                    if (borrowFragment != null) {
                                                        borrowFragment.refreshList();
                                                        found = true;
                                                    }
                                                }
                                                if (frag instanceof  DebtBorrowFragment) {
                                                    DebtBorrowFragment DebtBorrowFragment = (DebtBorrowFragment) frag;
                                                    if (DebtBorrowFragment != null) {
                                                        DebtBorrowFragment.updateToolbar();
                                                        found = true;
                                                    }
                                                }
                                            }
                                            paFragmentManager.getFragmentManager().popBackStack();
                                            if (!found) {
                                                paFragmentManager.getFragmentManager().popBackStack();
                                                paFragmentManager.displayFragment(new DebtBorrowFragment());
                                            }
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
                                        reportManager.clearCache();
                                        dataCache.updateAllPercents();
                                        paFragmentManager.updateAllFragmentsOnViewPager();
                                        paFragmentManager.updateAllFragmentsPageChanges();
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


    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setSubtitleIconVisibility(View.GONE);
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
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
        List<Account> allAccounts = daoSession.loadAll(Account.class);
        for (Account ac : allAccounts) {
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
            keyForInclude.setChecked(debtBorrow.getCalculate());
            final Spinner accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);
            ImageView cancel = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
            final TextView save = (TextView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);
            abbrrAmount.setText(debtBorrow.getCurrency().getAbbr());
            final List<Account> allAccounts = daoSession.loadAll(Account.class);
            final String[] accaounts = new String[allAccounts.size()];
            for (int i = 0; i < accaounts.length; i++) {
                accaounts[i] = allAccounts.get(i).getName();
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
                    if (!enterPay.getText().toString().isEmpty() && Double.parseDouble(enterPay.getText().toString()) != 0) {
                        if (keyForInclude.isChecked() && isMumkin(debtBorrow, allAccounts.
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
                                                allAccounts.get(accountSp.getSelectedItemPosition()).getId(), comment.getText().toString());
                                        else
                                        peysAdapter.setDataChanged(date, Double.parseDouble(enterPay.getText().toString()), "", comment.getText().toString());

                                    }
                                    warningDialog.dismiss();
                                    reportManager.clearCache();
                                    dataCache.updateAllPercents();
                                    paFragmentManager.updateAllFragmentsOnViewPager();
                                    paFragmentManager.updateAllFragmentsPageChanges();
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
                                        allAccounts.get(accountSp.getSelectedItemPosition()).getId(), comment.getText().toString());
                                else
                                    peysAdapter.setDataChanged(date, Double.parseDouble(enterPay.getText().toString()), "", comment.getText().toString());
                                reportManager.clearCache();
                                dataCache.updateAllPercents();
                                paFragmentManager.updateAllFragmentsPageChanges();
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
            boolean found = false;
            for (Fragment frag : paFragmentManager.getFragmentManager().getFragments()) {
                if (frag.getClass().getName().equals(BorrowFragment.class.getName())) {
                    BorrowFragment borrowFragment = (BorrowFragment) frag;
                    if (borrowFragment != null) {
                        borrowFragment.refreshList();
                        found = true;
                    }
                }
            }
            paFragmentManager.getFragmentManager().popBackStack();
            if (!found) {
                paFragmentManager.getFragmentManager().popBackStack();
                paFragmentManager.displayFragment(new DebtBorrowFragment());
            }
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
            paFragmentManager.updateAllFragmentsPageChanges();
            dataCache.updateAllPercents();
        }
    }

    @Override
    public void onClick(View v) {
        openDialog();
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
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
            view.infoSumm.setText("" + decimalFormat.format(list.get(position).getAmount())+ "" + debtBorrow.getCurrency().getAbbr());
            if (list.get(position).getAccountId().equals("")) {
                view.infoAccount.setText(R.string.ne_uchitavaetsya);
            } else {
                List<Account> allAccounts = daoSession.loadAll(Account.class);
                for (Account account : allAccounts) {
                    if (account.getId().matches(list.get(position).getAccountId())) {
                        view.infoAccount.setText(getString(R.string.by) + account.getName());
                        break;
                    }
                }
            }
            if (deletingMode == 0) {
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
            if (deletingMode == 1) {
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
            tvTotalPaid.setText(total + debtBorrow.getCurrency().getAbbr());
            double amount = debtBorrow.getAmount() - total;
            tvLeftAmount.setText(decimalFormat.format(amount) + "" + debtBorrow.getCurrency().getAbbr());
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
            double amount = debtBorrow.getAmount() - qoldiq;
            tvLeftAmount.setText(decimalFormat.format(amount) + "" + debtBorrow.getCurrency().getAbbr());
            tvTotalPaid.setText("" + decimalFormat.format(qoldiq) + "" + debtBorrow.getCurrency().getAbbr());

            if (qoldiq >= debtBorrow.getAmount()) {
                payText.setText(getResources().getString(R.string.to_archive));
                tvLeftAmount.setText(getResources().getString(R.string.repaid));
            }
            logicManager.insertReckingDebt(recking);
            notifyItemInserted(0);
            isCheks = new boolean[list.size()];
            for (int i = 0; i < isCheks.length; i++) {
                isCheks[i] = false;
            }
            reportManager.clearCache();
            dataCache.updateAllPercents();
            paFragmentManager.updateAllFragmentsPageChanges();
            paFragmentManager.updateVoiceRecognizePageCurrencyChanges();
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
    public int getMode() {
        return mode;
    }
    public int getLocalAppereance() {
        return localAppereance;
    }
}