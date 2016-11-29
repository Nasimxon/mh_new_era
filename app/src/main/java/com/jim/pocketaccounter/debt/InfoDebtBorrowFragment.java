package com.jim.pocketaccounter.debt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
import com.jim.pocketaccounter.database.BoardButtonDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.Recking;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.DatePicker;
import com.jim.pocketaccounter.utils.OperationsListDialog;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.cache.DataCache;

import org.greenrobot.greendao.query.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    WarningDialog warningDialog;
    DebtBorrowDao debtBorrowDao;
    AccountDao accountDao;

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
    private Spinner accountSp;
    private DebtBorrow debtBorrow;
    private ImageView deleteFrame;
    private ImageView pastgaOcil;
    private ImageView cancel_button;
    private ImageView info;
    private RelativeLayout infoFrame;
    //    private FrameLayout isHaveReking;
    private TextView tvInfoDebtBorrowTakeDate;
    private TextView payText;
    private boolean isCheks[];
    int mode = 1;
    private TextView phoneNumber;
    static int TYPE = 0;
    private int posMain = 0;

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

    public void setMainItems(int pos) {
        posMain = pos;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.modern_debt_borrow_info, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        warningDialog = new WarningDialog(getContext());
        debtBorrowDao = daoSession.getDebtBorrowDao();
        accountDao = daoSession.getAccountDao();
        borrowName = (TextView) view.findViewById(R.id.name_of_borrow);
        llDebtBOrrowItemEdit = (LinearLayout) view.findViewById(R.id.llDebtBOrrowItemEdit);
        leftAmount = (TextView) view.findViewById(R.id.tvAmountDebtBorrowInfo);
        borrowLeftDate = (TextView) view.findViewById(R.id.tvLeftDayDebtBorrowInfo);
        borrowPay = (LinearLayout) view.findViewById(R.id.btPayDebtBorrowInfo);
        deleteFrame = (ImageView) view.findViewById(R.id.flInfoDebtBorrowDeleted);
        pastgaOcil = (ImageView) view.findViewById(R.id.pastgaOcil);
        cancel_button = (ImageView) view.findViewById(R.id.cancel_button);
        totalPayAmount = (TextView) view.findViewById(R.id.total_summ_debt_borrow);
        tvTotalsummInfo = (TextView) view.findViewById(R.id.tvInfoDebtBorrowTotalSumm);
        payText = (TextView) view.findViewById(R.id.paybut);
        phoneNumber = (TextView) view.findViewById(R.id.tvInfoDebtBorrowPhoneNumber);
        circleImageView = (CircleImageView) view.findViewById(R.id.imBorrowPerson);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvDebtBorrowInfo);
        info = (ImageView) view.findViewById(R.id.ivInfoDebtBorrowInfo);
        infoFrame = (RelativeLayout) view.findViewById(R.id.flInfoDebtBorrowVisibl);
        tvInfoDebtBorrowTakeDate = (TextView) view.findViewById(R.id.tvInfoDebtBorrowTakeDate);
        infoFrame.setVisibility(View.GONE);
        calculate = (TextView) view.findViewById(R.id.tvInfoDebtBorrowIsCalculate);
        id = getArguments().getString("id");
        rlInfo = (RelativeLayout) view.findViewById(R.id.rlInfo);
//        isHaveReking = (FrameLayout) view.findViewById(R.id.ifListHave);

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
        toolbarManager.setImageToSecondImage(R.drawable.ic_delete_black);
        toolbarManager.setSpinnerVisibility(View.GONE);
        toolbarManager.setImageToHomeButton(R.drawable.ic_drawer);
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
            toolbarManager.setImageToSecondImage(R.drawable.trash);
            toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });
//        }
//        phoneNumber.setText(debtBorrow.getPerson().getPhoneNumber());
//        tvInfoDebtBorrowTakeDate.setText(dateFormat.format(debtBorrow.getTakenDate().getTime()));
//        calculate.setText(debtBorrow.getCalculate() ?
//                getString(R.string.calc_in_balance) :
//                getString(R.string.no_calc_in_balance));
        infoFrame.setVisibility(View.GONE);
//        view.findViewById(R.id.with_wlyuzik).setVisibility(view.GONE);

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
//                                                                       info.setImageResource(R.drawable.pasga_ochil);
//                                                                       view.findViewById(R.id.with_wlyuzik).setVisibility(View.VISIBLE);
                } else {
                    infoFrame.setVisibility(View.GONE);
                    rlInfo.setVisibility(View.VISIBLE);
//                                                                       info.setImageResource(R.drawable.infoo);
//                                                                       view.findViewById(R.id.with_wlyuzik).setVisibility(view.GONE);
                }
            }
        });
        if (debtBorrow.getReckings().isEmpty()) {
//            isHaveReking.setVisibility(View.GONE);
        }

        peysAdapter = new PeysAdapter((ArrayList<Recking>) debtBorrow.getReckings());
        Calendar currentDate = Calendar.getInstance();
        int day = 0;
        int mounth = 0;
        int year = 0;

        if (debtBorrow.getReturnDate() != null && currentDate.compareTo(debtBorrow.getReturnDate()) <= 0) {
            if (currentDate.get(Calendar.DAY_OF_MONTH) <= debtBorrow.getReturnDate().get(Calendar.DAY_OF_MONTH)) {
                day = debtBorrow.getReturnDate().get(Calendar.DAY_OF_MONTH) - currentDate.get(Calendar.DAY_OF_MONTH);
                if (currentDate.get(Calendar.MONTH) <= debtBorrow.getReturnDate().get(Calendar.MONTH)) {
                    mounth = debtBorrow.getReturnDate().get(Calendar.MONTH) - currentDate.get(Calendar.MONTH);
                    if (currentDate.get(Calendar.YEAR) <= debtBorrow.getReturnDate().get(Calendar.YEAR)) {
                        year = debtBorrow.getReturnDate().get(Calendar.YEAR) - currentDate.get(Calendar.YEAR);
                    }
                } else {
                    mounth = debtBorrow.getReturnDate().get(Calendar.MONTH) + 12 - currentDate.get(Calendar.MONTH);
                    year = debtBorrow.getReturnDate().get(Calendar.YEAR) - 1 - currentDate.get(Calendar.YEAR);
                }
            } else {
                debtBorrow.getReturnDate().add(Calendar.MONTH, -1);
                day = debtBorrow.getReturnDate().get(Calendar.DAY_OF_MONTH) + debtBorrow.getReturnDate().getActualMaximum(Calendar.MONTH) - currentDate.get(Calendar.DAY_OF_MONTH);
                if (debtBorrow.getReturnDate().get(Calendar.MONTH) >= currentDate.get(Calendar.MONTH)) {
                    mounth = debtBorrow.getReturnDate().get(Calendar.MONTH) - currentDate.get(Calendar.MONTH);
                    if (debtBorrow.getReturnDate().get(Calendar.YEAR) >= currentDate.get(Calendar.YEAR)) {
                        year = debtBorrow.getReturnDate().get(Calendar.YEAR) - currentDate.get(Calendar.YEAR);
                    }
                } else {
                    mounth = debtBorrow.getReturnDate().get(Calendar.MONTH) + 12 - currentDate.get(Calendar.MONTH);
                    if (debtBorrow.getReturnDate().get(Calendar.YEAR) > currentDate.get(Calendar.YEAR)) {
                        year = debtBorrow.getReturnDate().get(Calendar.YEAR) - 1 - currentDate.get(Calendar.YEAR);
                    }
                }
            }
        }

        final List<Recking> list = debtBorrow.getReckings();
        double total = 0;
        for (Recking rc : list) {
            total += rc.getAmount();
        }
        if (debtBorrow.getTo_archive()) {
//            deleteFrame.setVisibility(View.GONE);
            borrowPay.setVisibility(View.GONE);
        }
        borrowName.setText(debtBorrow.getPerson().getName());
        String qq = ((int) (debtBorrow.getAmount() - total)) == (debtBorrow.getAmount() - total)
                ? "" + ((int) (debtBorrow.getAmount() - total)) : "" + (debtBorrow.getAmount() - total);
        if (total >= debtBorrow.getAmount())
            leftAmount.setText(getResources().getString(R.string.repaid));
        else
            leftAmount.setText(qq + debtBorrow.getCurrency().getAbbr());
        String sana = (year != 0 ? year + " " + getString(R.string.year) : "")
                + (mounth != 0 ? mounth + " " + getString(R.string.moth) : "")
                + (day != 0 ? day + " " + getString(R.string.day) : "");
        if (debtBorrow.getReturnDate() == null) {
//            borrowLeftDate.setText(getResources().getString(R.string.no_date_selected));
        } else {
//            borrowLeftDate.setText(sana);
        }
        totalPayAmount.setText("" + (total == ((int) total) ? ("" + ((int) total)) : ("" + total)) + debtBorrow.getCurrency().getAbbr());
        if (total >= debtBorrow.getAmount()) {
            payText.setText(getResources().getString(R.string.archive));
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
        tvTotalsummInfo.setText("" + (debtBorrow.getAmount() == ((int) debtBorrow.getAmount())
                ? ("" + (int) debtBorrow.getAmount()) : ("" + debtBorrow.getAmount())) + debtBorrow.getCurrency().
                getAbbr());
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

    private void openDialog() {
        if (!payText.getText().toString().matches(getResources().getString(R.string.archive))) {
            final Dialog dialog = new Dialog(getActivity());
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView);
            final EditText enterDate = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowDate);
            final EditText enterPay = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPaySumm);
            final EditText comment = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPayComment);
            accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);

            final String[] accaounts = new String[accountDao.queryBuilder().list().size()];
            for (int i = 0; i < accaounts.length; i++) {
                accaounts[i] = accountDao.queryBuilder().list().get(i).getName();
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getContext(), R.layout.spiner_gravity_right, accaounts);

            accountSp.setAdapter(arrayAdapter);

            if (!debtBorrow.getCalculate()) {
                dialogView.findViewById(R.id.is_calc).setVisibility(View.GONE);
            }

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
                        if (debtBorrow.getCalculate() && isMumkin(debtBorrow, accountDao.queryBuilder().list().
                                get(accountSp.getSelectedItemPosition()).getId(), Double.parseDouble(enterPay.getText().toString())))
                            tek = true;
                        if (!debtBorrow.getCalculate()) tek = true;

                        if (Double.parseDouble(leftAmount.getText().toString().substring(0, leftAmount.getText().toString().length() - len))
                                - Double.parseDouble(enterPay.getText().toString()) < 0) {
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
                                        peysAdapter.setDataChanged(date, Double.parseDouble(enterPay.getText().toString()),
                                                "" + accountSp.getSelectedItem(), comment.getText().toString());
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
                                peysAdapter.setDataChanged(date, Double.parseDouble(enterPay.getText().toString()),
                                        "" + accountSp.getSelectedItem(), comment.getText().toString());
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
            view.infoSumm.setText((list.get(position).getAmount() == ((int) list.get(position).getAmount())
                    ? ("" + ((int) list.get(position).getAmount())) : ("" + list.get(position).getAmount()))
                    + "" + debtBorrow.getCurrency().getAbbr());
            if (!debtBorrow.getCalculate()) {
                view.infoAccount.setVisibility(View.GONE);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.modern_debt_borrow_info_item, parent, false);
            return new ViewHolder(view);
        }

        public void itemDeleted(int position) {
            list.remove(position);
            notifyItemRemoved(position);
            double total = 0;
            for (Recking rc : list) {
                total += rc.getAmount();
            }
            totalPayAmount.setText((total == ((int) total) ? ("" + ((int) total)) :
                    ("" + total)) + debtBorrow.getCurrency().getAbbr());
            String qq = ((int) (debtBorrow.getAmount() - total)) == (debtBorrow.getAmount() - total)
                    ? "" + ((int) (debtBorrow.getAmount() - total)) : "" + (debtBorrow.getAmount() - total);
            leftAmount.setText(qq + "" + debtBorrow.getCurrency().getAbbr());
            if (debtBorrow.getReckings().isEmpty()) {
//                isHaveReking.setVisibility(View.GONE);
            }
            dataCache.updateAllPercents();
            paFragmentManager.updateAllFragmentsOnViewPager();
        }

        public void setDataChanged(Calendar clDate, double value, String accountId, String comment) {
            for (Account account : accountDao.queryBuilder().list()) {
                if (account.getName().matches(accountId)) {
                    accountId = account.getId();
                    break;
                }
            }
            Recking recking = new Recking(clDate, value, debtBorrow.getId(), accountId, comment);
            debtBorrow.getReckings().add(0, recking);
            logicManager.insertReckingDebt(recking);
            double qoldiq = 0;
            for (int i = 0; i < list.size(); i++) {
                qoldiq += list.get(i).getAmount();
            }
            String qq = ((int) (debtBorrow.getAmount() - qoldiq)) == (debtBorrow.getAmount() - qoldiq)
                    ? ("" + ((int) (debtBorrow.getAmount() - qoldiq))) : ("" + (debtBorrow.getAmount() - qoldiq));

            leftAmount.setText(qq + "" + debtBorrow.getCurrency().getAbbr());
            totalPayAmount.setText("" + (qoldiq == ((int) qoldiq) ? ("" + ((int) qoldiq)) : ("" + qoldiq)) + "" + debtBorrow.getCurrency().getAbbr());

            if (qoldiq >= debtBorrow.getAmount()) {
                payText.setText(getResources().getString(R.string.archive));
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