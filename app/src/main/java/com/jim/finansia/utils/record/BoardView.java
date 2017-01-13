package com.jim.finansia.utils.record;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jim.finansia.PocketAccounter;
import com.jim.finansia.R;
import com.jim.finansia.SettingsActivity;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.BoardButtonDao;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.RootCategoryDao;
import com.jim.finansia.debt.AddBorrowFragment;
import com.jim.finansia.debt.DebtBorrowFragment;
import com.jim.finansia.debt.InfoDebtBorrowFragment;
import com.jim.finansia.finance.CategoryAdapterForDialog;
import com.jim.finansia.fragments.AccountFragment;
import com.jim.finansia.fragments.AddCreditFragment;
import com.jim.finansia.fragments.AutoMarketFragment;
import com.jim.finansia.fragments.CategoryFragment;
import com.jim.finansia.fragments.CreditTabLay;
import com.jim.finansia.fragments.CurrencyFragment;
import com.jim.finansia.fragments.InfoCreditFragment;
import com.jim.finansia.fragments.PurposeFragment;
import com.jim.finansia.fragments.RecordEditFragment;
import com.jim.finansia.fragments.ReportByAccountFragment;
import com.jim.finansia.fragments.ReportByCategory;
import com.jim.finansia.fragments.RootCategoryEditFragment;
import com.jim.finansia.fragments.SmsParseMainFragment;
import com.jim.finansia.fragments.TableBarFragment;
import com.jim.finansia.syncbase.SyncBase;
import com.jim.finansia.utils.OperationsListDialog;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.TransferDialog;
import com.jim.finansia.utils.WarningDialog;

import org.greenrobot.greendao.query.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SuppressLint("NewApi")
public class BoardView extends TextDrawingBoardView implements GestureDetector.OnGestureListener {
    private GestureDetectorCompat gestureDetector;
    private PageChangeListener pageChangeListener;
    public BoardView(Context context, int table, Calendar day) {
        super(context, table, day);
        gestureDetector = new GestureDetectorCompat(getContext(), this);
    }
    public BoardView(Context context, AttributeSet attrs, int table, Calendar day) {
        super(context, attrs, table, day);
        init();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr, int table, Calendar day) {
        super(context, attrs, defStyleAttr, table, day);
        init();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int table, Calendar day) {
        super(context, attrs, defStyleAttr, defStyleRes, table, day);
        init();
    }

    @Override
    public void init() {
        super.init();
        setClickable(true);
        gestureDetector = new GestureDetectorCompat(getContext(), this);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (PocketAccounter.PRESSED) return true;
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    @Override
    public boolean onDown(MotionEvent motionEvent) { return false; }
    @Override
    public void onShowPress(MotionEvent motionEvent) { }
    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        if (PocketAccounter.PRESSED) return false;
        int size = buttons.size();
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        for (int i=0; i<size; i++) {
            if (buttons.get(i).getContainer() != null && buttons.get(i).getContainer().contains(x, y)) {
                final int position = i;
                press(position);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BoardButton button = daoSession.getBoardButtonDao().load(buttons.get(position).getButtonId());
                        if (button.getCategoryId() == null)
                            openTypeChooseDialog(position);
                        else if (button.getType() == PocketAccounterGeneral.CATEGORY) {
                            RootCategory category = null;
                            if(button.getCategoryId() == null)
                                category = null;
                            else {
                                List<RootCategory> categoryList = daoSession.getRootCategoryDao().queryBuilder()
                                        .where(RootCategoryDao.Properties.Id.eq(button.getCategoryId()))
                                        .list();
                                if (!categoryList.isEmpty())
                                    category = categoryList.get(0);
                            }
                            paFragmentManager.setMainReturn(true);
                            paFragmentManager.displayFragment(new RecordEditFragment(category, day, null, PocketAccounterGeneral.MAIN));
                        }
                        else if (button.getType() == PocketAccounterGeneral.CREDIT) {
                            CreditDetials item=daoSession.getCreditDetialsDao().load(Long.parseLong(button.getCategoryId()));
                            InfoCreditFragment temp = new InfoCreditFragment();
                            int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                            temp.setContentFromMainWindow(item,currentPage * buttonsCount + position,PocketAccounterGeneral.EXPANSE_MODE);
                            paFragmentManager.setMainReturn(true);
                            paFragmentManager.displayFragment(temp);

                        }
                        else if (button.getType() == PocketAccounterGeneral.DEBT_BORROW) {
                            InfoDebtBorrowFragment fragment = InfoDebtBorrowFragment.getInstance(button.getCategoryId(), DebtBorrow.BORROW);
                            fragment.setMainItems (currentPage*16+position);
                            paFragmentManager.setMainReturn(true);
                            paFragmentManager.displayFragment(fragment);
                        }
                        else if (button.getType() == PocketAccounterGeneral.PAGE) {
                            String[] pageIds = getResources().getStringArray(R.array.page_ids);
                            int pos = 0;
                            for (int i=0; i<pageIds.length; i++) {
                                if (pageIds[i].equals(button.getCategoryId())) {
                                    pos = i;
                                    break;
                                }
                            }
                            switch (pos) {
                                case 0:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new CurrencyFragment());
                                    break;
                                case 1:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new CategoryFragment());
                                    break;
                                case 2:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new AccountFragment());

                                    break;
                                case 3:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new PurposeFragment());

                                    break;
                                case 4:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new AutoMarketFragment());

                                    break;
                                case 5:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new CreditTabLay());

                                    break;
                                case 6:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new DebtBorrowFragment());
                                    break;
                                case 7:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new ReportByAccountFragment());
                                    break;
                                case 8:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new TableBarFragment());
                                    break;
                                case 9:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new ReportByCategory());
                                    break;
                                case 10:
                                    paFragmentManager.setMainReturn(true);
                                    paFragmentManager.displayFragment(new SmsParseMainFragment());
                                    break;
                                case 11:
                                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                                    getContext().startActivity(intent);
                                    break;
                            }
                        }
                        else if (button.getType() == PocketAccounterGeneral.FUNCTION) {
                            String[] functionIds = getResources().getStringArray(R.array.operation_ids);
                            int pos = 0;
                            for (int i = 0; i<functionIds.length; i++) {
                                if (functionIds[i].equals(button.getCategoryId())) {
                                    pos = i;
                                    break;
                                }
                            }
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            switch(pos) {
                                case 0:
                                    if(user!=null){
                                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                                        builder.setMessage(R.string.sync_message)
                                                .setPositiveButton(R.string.sync_short, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                                        StorageReference storageRef = storage.getReferenceFromUrl("gs://pocket-accounter.appspot.com");
                                                        SyncBase mySync;
                                                        mySync = new SyncBase(storageRef, getContext(), PocketAccounterGeneral.CURRENT_DB_NAME);
                                                        mySync.uploadBASE(user.getUid(), new SyncBase.ChangeStateLis() {
                                                            @Override
                                                            public void onSuccses() {
                                                                (new android.os.Handler()).postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {

                                                                        Toast.makeText(getContext(), R.string.sync_suc,Toast.LENGTH_SHORT).show();

                                                                    }
                                                                }, 2000);
                                                            }
                                                            @Override
                                                            public void onFailed(String e) {
                                                                Toast.makeText(getContext(), R.string.sync_failed,Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }).setNegativeButton(((PocketAccounter) getContext()).getString(R.string.cancel1), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                        builder.create().show();
                                    }else {
                                        Toast.makeText(getContext(),R.string.please_sign,Toast.LENGTH_SHORT).show();
                                    }
                                    //google synchronization
                                    break;
                                case 1:
                                    if(user!=null) {
                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        StorageReference storageRef = storage.getReferenceFromUrl("gs://pocket-accounter.appspot.com");
                                        final SyncBase mySync;
                                        mySync = new SyncBase(storageRef, getContext(), PocketAccounterGeneral.CURRENT_DB_NAME);
                                        showProgressDialog(((PocketAccounter)getContext()).getString(R.string.download));
                                        mySync.meta_Message(user.getUid(), new SyncBase.ChangeStateLisMETA() {
                                            @Override
                                            public void onSuccses(final long inFormat) {
                                                Date datee = new Date();
                                                datee.setTime(inFormat);
                                                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(((PocketAccounter) getContext()));
                                                builder.setMessage(((PocketAccounter) getContext()).getString(R.string.sync_last_data_sign_up) + (new SimpleDateFormat("dd.MM.yyyy kk:mm")).format(datee))
                                                        .setPositiveButton(((PocketAccounter) getContext()).getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                showProgressDialog(((PocketAccounter)getContext()).getString(R.string.download));
                                                                mySync.downloadLast(user.getUid(), new SyncBase.ChangeStateLis() {
                                                                    @Override
                                                                    public void onSuccses() {
                                                                        ((PocketAccounter)getContext()).runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                hideProgressDialog();
                                                                            }
                                                                        });
                                                                    }
                                                                    @Override
                                                                    public void onFailed(String e) {
                                                                        hideProgressDialog();
                                                                        Toast.makeText(getContext(),R.string.sync_failed,Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }).setNegativeButton(((PocketAccounter) getContext()).getString(R.string.no), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        hideProgressDialog();
                                                        dialog.cancel();

                                                    }
                                                });
                                                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                    @Override
                                                    public void onCancel(DialogInterface dialog) {
                                                        hideProgressDialog();
                                                    }
                                                });
                                                builder.create().show();
                                            }
                                            @Override
                                            public void onFailed(Exception e) {
                                                hideProgressDialog();

                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(getContext(),R.string.please_sign,Toast.LENGTH_SHORT).show();
                                    }
                                    //google download
                                    break;
                                case 2:
                                    Account account = daoSession.getAccountDao().loadAll().isEmpty() ?
                                            null : daoSession.getAccountDao().loadAll().get(0);
                                    final TransferDialog transferDialog = new TransferDialog(getContext());
                                    transferDialog.setAccountOrPurpose(account.getId(), true);
                                    transferDialog.show();
                                    break;
                                case 3:
                                    final WarningDialog warningDialog = new WarningDialog(getContext());
                                    warningDialog.setText(getContext().getString(R.string.whole_day_datas_deleting));
                                    warningDialog.setOnYesButtonListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Calendar begin = Calendar.getInstance();
                                            begin.setTimeInMillis(day.getTimeInMillis());
                                            begin.set(Calendar.HOUR_OF_DAY, 0);
                                            begin.set(Calendar.MINUTE, 0);
                                            begin.set(Calendar.SECOND, 0);
                                            begin.set(Calendar.MILLISECOND, 0);
                                            Calendar end = Calendar.getInstance();
                                            begin.setTimeInMillis(day.getTimeInMillis());
                                            end.set(Calendar.HOUR_OF_DAY, 23);
                                            end.set(Calendar.MINUTE, 59);
                                            end.set(Calendar.SECOND, 59);
                                            end.set(Calendar.MILLISECOND, 59);
                                            String format = simpleDateFormat.format(begin.getTime());
                                            daoSession.getFinanceRecordDao().queryBuilder()
                                                    .where(FinanceRecordDao.Properties.Date.eq(format))
                                                    .buildDelete()
                                                    .executeDeleteWithoutDetachingEntities();
                                            warningDialog.dismiss();
                                            dataCache.updateOneDay(day);
                                            paFragmentManager.updateAllFragmentsOnViewPager();
                                        }
                                    });
                                    warningDialog.setOnNoButtonClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            warningDialog.dismiss();
                                        }
                                    });
                                    warningDialog.show();
                                    break;
                                case 4:
                                    if (currentPage == setCount-1)
                                        currentPage = 0;
                                    else
                                        currentPage++;
                                    if (table == PocketAccounterGeneral.EXPENSE)
                                        sharedPreferences
                                                .edit()
                                                .putInt("expense_current_page", currentPage)
                                                .commit();
                                    else
                                        sharedPreferences
                                                .edit()
                                                .putInt("income_current_page", currentPage)
                                                .commit();
                                    init();
                                    invalidate();
                                    paFragmentManager.updateAllFragmentsPageChanges();
                                    if (pageChangeListener != null)
                                        pageChangeListener.onPageChange(currentPage);
                                    break;
                                case 5:
                                    if (currentPage == 0)
                                        currentPage = setCount-1;
                                    else
                                        currentPage--;
                                    if (table == PocketAccounterGeneral.EXPENSE)
                                        sharedPreferences
                                            .edit()
                                            .putInt("expense_current_page", currentPage)
                                            .commit();
                                    else
                                        sharedPreferences
                                                .edit()
                                                .putInt("income_current_page", currentPage)
                                                .commit();
                                    init();
                                    invalidate();
                                    paFragmentManager.updateAllFragmentsPageChanges();
                                    if (pageChangeListener != null)
                                        pageChangeListener.onPageChange(currentPage);
                                    break;
                            }
                            PocketAccounter.PRESSED = false;
                        }
                    }
                }, 120);
                PocketAccounter.PRESSED = true;
                break;
            }
        }
        return false;
    }

    public void setOnPageChangeListener(PageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Vibrator vibr = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibr.vibrate(60);
        float x = e.getX(), y = e.getY();
        int size = buttons.size();
        for (int i=0; i<size; i++) {
            if (buttons.get(i).getContainer().contains(x, y)) {
                longPress(i);
                final int position = i;
                List<BoardButton> boardButtonList = daoSession.getBoardButtonDao()
                        .queryBuilder().where(BoardButtonDao.Properties.Table.eq(table))
                        .list();
                if (boardButtonList.get(position).getCategoryId() == null) {
                    releasePress();
                    return;
                }
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openChooseDialogLongPress(position);
                    }
                }, 250);
                invalidate();
                break;
            }
        }
    }

    private void openChooseDialogLongPress(final int pos) {
        begin.setTimeInMillis(day.getTimeInMillis());
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end.setTimeInMillis(day.getTimeInMillis());
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        String edit = getContext().getString(R.string.to_edit);
        String change = getResources().getString(R.string.change);
        String clear = getResources().getString(R.string.clear);
        String clearRecords = getContext().getString(R.string.clear_records);
        int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
        BoardButton cur = daoSession.getBoardButtonDao().queryBuilder()
                .where(BoardButtonDao.Properties.Pos.eq(pos+currentPage*buttonsCount), BoardButtonDao.Properties.Table.eq(table))
                .list().isEmpty() ?
                null:daoSession.getBoardButtonDao().queryBuilder()
                .where(BoardButtonDao.Properties.Pos.eq(pos+currentPage*buttonsCount), BoardButtonDao.Properties.Table.eq(table))
                .list().get(0);
        String[] items = null;
        String format = simpleDateFormat.format(begin.getTime());
        List<FinanceRecord> temp = new ArrayList<>();
        if (cur.getCategoryId() != null)
            temp = daoSession.getFinanceRecordDao().queryBuilder()
                    .where(FinanceRecordDao.Properties.CategoryId.isNotNull(),
                            FinanceRecordDao.Properties.CategoryId.eq(cur.getCategoryId()),
                            FinanceRecordDao.Properties.Date.eq(format)).list();
        if (!temp.isEmpty()) {
            items = new String[4];
            items[0] = change;
            items[1] = clear;
            items[2] = edit;
            items[3] = clearRecords;
        } else {
            items = new String[2];
            items[0] = change;
            items[1] = clear;
        }
        final OperationsListDialog operationsListDialog = new OperationsListDialog(getContext());
        operationsListDialog.setAdapter(items);
        operationsListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        openTypeChooseDialog(pos);
                        break;
                    case 1:
                        int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                        logicManager.changeBoardButton(table, currentPage*buttonsCount+pos, null);
                        changeIconInCache(pos, "no_category");
                        initButtons();
                        releasePress();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        dataCache.updateOneDay(day);
                        operationsListDialog.dismiss();
                        break;
                    case 2:
                        openEditDialog(pos);
                        break;
                    case 3:
                        clear(pos);
                        break;
                }
                PocketAccounter.PRESSED = false;
                operationsListDialog.dismiss();
            }
        });
        operationsListDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                releasePress();
                PocketAccounter.PRESSED = false;
            }
        });
        operationsListDialog.show();
    }

    //clear allday
    private void clear(final int pos) {
        begin.setTimeInMillis(day.getTimeInMillis());
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        end.setTimeInMillis(day.getTimeInMillis());
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
        final String id = daoSession.getBoardButtonDao().queryBuilder()
                .where(BoardButtonDao.Properties.Pos.eq(pos+currentPage*buttonsCount),
                        BoardButtonDao.Properties.Table.eq(table))
                .list().get(0).getCategoryId();
        final WarningDialog warningDialog = new WarningDialog(getContext());
        warningDialog.setText(getContext().getString(R.string.clear_warning));
        warningDialog.setOnYesButtonListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                QueryBuilder<FinanceRecord> financeRecordQueryBuilder = daoSession.getFinanceRecordDao().queryBuilder();
                financeRecordQueryBuilder
                        .where(FinanceRecordDao.Properties.Date.eq(simpleDateFormat.format(dataCache.getEndDate().getTime())),
                                FinanceRecordDao.Properties.CategoryId.eq(id));
                List<FinanceRecord> deletingRecords = financeRecordQueryBuilder.list();
                daoSession.getFinanceRecordDao().deleteInTx(deletingRecords);
                paFragmentManager.getCurrentFragment().update();
                dataCache.updateOneDay(dataCache.getEndDate());
                releasePress();
                PocketAccounter.PRESSED = false;
                invalidate();
                warningDialog.dismiss();
            }
        });
        warningDialog.setOnNoButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePress();
                PocketAccounter.PRESSED = false;
                warningDialog.dismiss();
            }
        });
        warningDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                releasePress();
                PocketAccounter.PRESSED = false;
            }
        });
        warningDialog.show();
    }

    //opens edit dialog list
    private void openEditDialog(int position) {
        final Dialog dialog=new Dialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        ListView lvDialog = (ListView) dialogView.findViewById(R.id.lvDialog);
        BoardButton boardButton = daoSession.getBoardButtonDao().load(buttons.get(position).getButtonId());
        String id = boardButton.getCategoryId();
        final List<FinanceRecord> records = daoSession.getFinanceRecordDao().queryBuilder()
                .where(FinanceRecordDao.Properties.CategoryId.eq(id)).list();
        LongPressAdapter adapter = new LongPressAdapter(getContext(), records);
        lvDialog.setAdapter(adapter);
        lvDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                paFragmentManager.displayFragment(new RecordEditFragment(records.get(position).getCategory(), day, records.get(position), PocketAccounterGeneral.MAIN));
                releasePress();
                PocketAccounter.PRESSED = false;
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                releasePress();
                PocketAccounter.PRESSED = false;
            }
        });
        dialog.show();
    }

    // category, credit, debtborrow, function, page
    private void openTypeChooseDialog(final int pos) {
        String[] items = getResources().getStringArray(R.array.board_operation_names_long_press);
        final OperationsListDialog operationsListDialog = new OperationsListDialog(getContext());
        operationsListDialog.setAdapter(items);
        operationsListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position <= 2) {
                    String[] operationCategory = getResources().getStringArray(R.array.operation_category);
                    operationsListDialog.setAdapter(operationCategory);
                    operationsListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                            switch (p) {
                                case 0:
                                    switch (position) {
                                        case 0:
                                            openCategoryChooseDialog(pos);
                                            break;
                                        case 1:
                                            openCreditsChooseDialog(pos);
                                            break;
                                        case 2:
                                            openDebtBorrowChooseDialog(pos);
                                            break;
                                    }
                                    break;
                                case 1:
                                    switch (position) {
                                        case 0:
                                            paFragmentManager.setMainReturn(true);
                                            int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                                            paFragmentManager.displayFragment(new RootCategoryEditFragment(null, PocketAccounterGeneral.EXPANSE_MODE, currentPage*buttonsCount+pos, day));
                                            break;
                                        case 1:
                                            paFragmentManager.setMainReturn(true);
                                            buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                                            paFragmentManager.displayFragment((new AddCreditFragment()).setDateFormatModes(PocketAccounterGeneral.EXPANSE_MODE,currentPage*buttonsCount+pos));
                                            break;
                                        case 2:
                                            paFragmentManager.setMainReturn(true);
                                            AddBorrowFragment fragment = AddBorrowFragment.getInstance(DebtBorrow.DEBT, null);
                                            fragment.setMainView(daoSession.getBoardButtonDao().load(buttons.get(pos).getButtonId()));
                                            paFragmentManager.displayFragment(fragment);
                                            break;
                                    }
                                    break;
                            }
                            operationsListDialog.dismiss();
                        }
                    });
                    operationsListDialog.show();
                }
                else {
                    switch (position) {
                        case 3:
                            openOperationsList(pos);
                            break;
                        case 4:
                            openPageChooseDialog(pos);
                            break;
                    }
                    operationsListDialog.dismiss();
                }
            }
        });
        operationsListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                releasePress();
                PocketAccounter.PRESSED = false;
            }
        });
        operationsListDialog.show();
    }

    //page choose dialog
    private void openPageChooseDialog(final int pos) {
        final Dialog dialog=new Dialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        final ArrayList<IconWithName> categories = new ArrayList<>();
        String[] pageNames = getContext().getResources().getStringArray(R.array.page_names);
        String[] pageIds = getContext().getResources().getStringArray(R.array.page_ids);
        String[] pageIcons = getContext().getResources().getStringArray(R.array.page_icons);
        for (int i=0; i<pageNames.length; i++) {
            IconWithName iconWithName = new IconWithName(pageIcons[i], pageNames[i], pageIds[i]);
            categories.add(iconWithName);
        }
        CategoryAdapterForDialog adapter = new CategoryAdapterForDialog(getContext(), categories);
        ListView lvDialog = (ListView) dialogView.findViewById(R.id.lvDialog);
        lvDialog.setAdapter(adapter);
        lvDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isAvailable = sharedPreferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_PAGE, false);
                if (isAvailable) {
                    int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                    logicManager.changeBoardButton(table, currentPage * buttonsCount + pos, categories.get(position).getId());
                    changeIconInCache(pos, categories.get(position).getIcon());
                    init();
                    paFragmentManager.updateAllFragmentsOnViewPager();
                    dataCache.updateOneDay(day);
                } else
                    purchaseImplementation.buyChangingPage();
                PocketAccounter.PRESSED = false;
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                releasePress();
                PocketAccounter.PRESSED = false;
            }
        });
        dialog.show();
    }
    //functions choose dialog
    private void openOperationsList(final int pos) {
        final Dialog dialog=new Dialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        final ArrayList<IconWithName> categories = new ArrayList<>();
        String[] operationNames = getResources().getStringArray(R.array.operation_names);
        String[] operationIds = getResources().getStringArray(R.array.operation_ids);
        String[] operationIcons = getResources().getStringArray(R.array.operation_icons);
        for (int i=0; i<operationNames.length; i++) {
            IconWithName iconWithName = new IconWithName(operationIcons[i], operationNames[i], operationIds[i]);
            categories.add(iconWithName);
        }
        CategoryAdapterForDialog adapter = new CategoryAdapterForDialog(getContext(), categories);
        ListView lvDialog = (ListView) dialogView.findViewById(R.id.lvDialog);
        lvDialog.setAdapter(adapter);
        lvDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isAvailable = sharedPreferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_FUNCTION, false);
                if (isAvailable) {
                    int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                    logicManager.changeBoardButton(table, currentPage * buttonsCount + pos, categories.get(position).getId());
                    changeIconInCache(pos, categories.get(position).getIcon());
                    init();
                    paFragmentManager.updateAllFragmentsOnViewPager();
                    dataCache.updateOneDay(day);
                }
                else
                    purchaseImplementation.buyChangingFunction();
                PocketAccounter.PRESSED = false;
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                releasePress();
                PocketAccounter.PRESSED = false;
            }
        });
        dialog.show();
    }


    //categories choose dialog
    private void openCategoryChooseDialog(final int pos) {
        final Dialog dialog=new Dialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        final ArrayList<IconWithName> categories = new ArrayList<>();
        List<RootCategory> categoryList = daoSession.getRootCategoryDao().queryBuilder()
                .where(RootCategoryDao.Properties.Type.eq(table))
                .build()
                .list();
        for (RootCategory category : categoryList) {
            IconWithName iconWithName = new IconWithName(category.getIcon(), category.getName(), category.getId());
            categories.add(iconWithName);
        }
        CategoryAdapterForDialog adapter = new CategoryAdapterForDialog(getContext(), categories);
        ListView lvDialog = (ListView) dialogView.findViewById(R.id.lvDialog);
        lvDialog.setAdapter(adapter);
        lvDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (table == PocketAccounterGeneral.EXPENSE) {
                    boolean isAvailable = sharedPreferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_CATEGORY_KEY, false);
                    if (isAvailable) {
                        int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                        logicManager.changeBoardButton(table, currentPage*buttonsCount+pos, categories.get(position).getId());
                        changeIconInCache(pos, categories.get(position).getIcon());
                        init();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        dataCache.updateOneDay(day);
                    }
                    else
                        purchaseImplementation.buyChangingCategory();
                } else {
                    int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                    logicManager.changeBoardButton(table, currentPage*buttonsCount+pos, categories.get(position).getId());
                    changeIconInCache(pos, categories.get(position).getIcon());
                    init();
                    paFragmentManager.updateAllFragmentsOnViewPager();
                    dataCache.updateOneDay(day);
                }
                PocketAccounter.PRESSED = false;
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                releasePress();
                PocketAccounter.PRESSED = false;
            }
        });
        dialog.show();
    }

    // debt borrow choose dialog
    private void openDebtBorrowChooseDialog(final int pos) {
        final ArrayList<IconWithName> categories = new ArrayList<>();
        List<DebtBorrow> debtBorrowList = daoSession.getDebtBorrowDao()
                .queryBuilder().where(DebtBorrowDao.Properties.To_archive.eq(false)).list();
        if (!debtBorrowList.isEmpty()) {
            final Dialog dialog=new Dialog(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView);
            for (DebtBorrow debtBorrow : debtBorrowList) {
                IconWithName iconWithName = new IconWithName(debtBorrow.getPerson().getPhoto(),
                        debtBorrow.getPerson().getName(), debtBorrow.getId());
                categories.add(iconWithName);
            }
            CategoryAdapterForDialog adapter = new CategoryAdapterForDialog(getContext(), categories);
            ListView lvDialog = (ListView) dialogView.findViewById(R.id.lvDialog);
            lvDialog.setAdapter(adapter);
            lvDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    boolean isAvailable = sharedPreferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_DEBT_BORROW_KEY, false);
                    if (isAvailable) {
                        int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                        logicManager.changeBoardButton(table, currentPage * buttonsCount + pos, categories.get(position).getId());
                        changeIconInCache(pos, categories.get(position).getIcon());
                        init();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        dataCache.updateOneDay(day);
                    }
                    else
                        purchaseImplementation.buyChangingDebtBorrow();
                    PocketAccounter.PRESSED = false;
                    dialog.dismiss();
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    releasePress();
                    PocketAccounter.PRESSED = false;
                }
            });
            dialog.show();
        }
        else {
            releasePress();
            PocketAccounter.PRESSED = false;
            Toast.makeText(getContext(), R.string.debt_borrow_list_is_empty, Toast.LENGTH_SHORT).show();
        }

    }

    //credits choose dialog
    private void openCreditsChooseDialog(final int pos) {
        List<CreditDetials> creditDetialsList = daoSession.getCreditDetialsDao()
                .queryBuilder().where(CreditDetialsDao.Properties.Key_for_include.eq(true),
                        CreditDetialsDao.Properties.Key_for_archive.eq(false)).list();
        if (!creditDetialsList.isEmpty()) {
            final Dialog dialog = new Dialog(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView);
            final ArrayList<IconWithName> categories = new ArrayList<>();
            for (CreditDetials creditDetials : creditDetialsList) {
                IconWithName iconWithName = new IconWithName(creditDetials.getIcon_ID(),
                        creditDetials.getCredit_name(), Long.toString(creditDetials.getMyCredit_id()));
                categories.add(iconWithName);
            }
            CategoryAdapterForDialog adapter = new CategoryAdapterForDialog(getContext(), categories);
            ListView lvDialog = (ListView) dialogView.findViewById(R.id.lvDialog);
            lvDialog.setAdapter(adapter);
            lvDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    boolean isAvailble = sharedPreferences.getBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_CREDIT_KEY, false);
                    if (isAvailble) {
                        int buttonsCount = table == PocketAccounterGeneral.INCOME ? INCOME_BUTTONS_COUNT_PER_PAGE : EXPENSE_BUTTONS_COUNT_PER_PAGE;
                        logicManager.changeBoardButton(table, currentPage * buttonsCount + pos, categories.get(position).getId());
                        changeIconInCache(pos, categories.get(position).getIcon());
                        init();
                        invalidate();
                        paFragmentManager.updateAllFragmentsOnViewPager();
                        dataCache.updateOneDay(day);
                    }
                    else
                        purchaseImplementation.buyChanchingCredit();
                    PocketAccounter.PRESSED = false;
                    dialog.dismiss();
                }
            });
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    releasePress();
                    PocketAccounter.PRESSED = false;
                }
            });
        }
        else {
            releasePress();
            PocketAccounter.PRESSED = false;
            Toast.makeText(getContext(), R.string.credit_list_is_empty, Toast.LENGTH_SHORT).show();
        }
    }

    //changes icon in datacache class
    private void changeIconInCache(int pos, String icon) {
        Bitmap scaled;
        BoardButton button = daoSession.getBoardButtonDao().load(buttons.get(pos).getButtonId());
        if (icon.equals("no_category") || button.getType() != PocketAccounterGeneral.DEBT_BORROW) {
            int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            scaled = BitmapFactory.decodeResource(getResources(), resId, options);
        }
        else {
            if (!icon.equals("") && !icon.equals("0")) {
                try {
                    scaled = queryContactImage(Integer.parseInt(icon));
                }
                catch (NumberFormatException e) {
                    scaled = BitmapFactory.decodeFile(icon);
                }
            }
            else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                scaled = BitmapFactory.decodeResource(getResources(), R.drawable.no_photo, options);
            }
        }
        scaled = Bitmap.createScaledBitmap(scaled, (int)getResources().getDimension(R.dimen.thirty_dp), (int) getResources().getDimension(R.dimen.thirty_dp), true);
        dataCache.getBoardBitmapsCache().put(buttons.get(pos).getButtonId(),
                scaled);
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
    private ProgressDialog mProgressDialog;
    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public void lockPage() {
        manualAlphaSetted = true;
        alpha = (int) (fullAlpha * 0.1);
        invalidate();
    }

    public void unlockPage() {
        alpha = fullAlpha;
        invalidate();
        manualAlphaSetted = false;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }


    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    public void incCurrentPage() {
        if (currentPage == setCount-1)
            currentPage = 0;
        else
            currentPage++;
        if (table == PocketAccounterGeneral.EXPENSE)
            sharedPreferences
                    .edit()
                    .putInt("expense_current_page", currentPage)
                    .commit();
        else
            sharedPreferences
                    .edit()
                    .putInt("income_current_page", currentPage)
                    .commit();
        if (pageChangeListener != null)
            pageChangeListener.onPageChange(currentPage);
        init();
        invalidate();
        paFragmentManager.updateAllFragmentsPageChanges();

    }

    public void decCurrentPage() {
        if (currentPage == 0)
            currentPage = setCount-1;
        else
            currentPage--;
        if (table == PocketAccounterGeneral.EXPENSE)
            sharedPreferences
                    .edit()
                    .putInt("expense_current_page", currentPage)
                    .commit();
        else
            sharedPreferences
                    .edit()
                    .putInt("income_current_page", currentPage)
                    .commit();
        if (pageChangeListener != null)
            pageChangeListener.onPageChange(currentPage);
        init();
        invalidate();
        paFragmentManager.updateAllFragmentsPageChanges();
    }

}
