package com.jim.finansia.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.credit.LinearManagerWithOutEx;
import com.jim.finansia.database.Account;
import com.jim.finansia.database.AccountDao;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.CreditDetialsDao;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.FinanceRecordDao;
import com.jim.finansia.database.Person;
import com.jim.finansia.database.PersonDao;
import com.jim.finansia.database.Recking;
import com.jim.finansia.database.ReckingCredit;
import com.jim.finansia.database.ReckingCreditDao;
import com.jim.finansia.database.ReckingDao;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.database.RootCategoryDao;
import com.jim.finansia.database.SubCategory;
import com.jim.finansia.database.SubCategoryDao;
import com.jim.finansia.debt.DebtBorrowFragment;
import com.jim.finansia.debt.InfoDebtBorrowFragment;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.SearchResultConten;

import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.QueryBuilder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import static com.jim.finansia.utils.ContenerStaticSearchVariables.*;

public class SearchFragment extends Fragment {
    private RecyclerView rvSearchItems;
    private RVAdapterSearch rvAdapterSearch = new RVAdapterSearch();
    private String prevSearchString = "";
    private SimpleDateFormat dateformarter= new SimpleDateFormat("dd.MM.yyyy");
    private DecimalFormat formater= new DecimalFormat("0.00##");
    private TextView textViewSearch;
    private List<SearchResultConten> searchItemsToSend;
    private List<SearchResultConten> searchItemsToSendForUse;
    private String[] tempIcons;

    @Inject DaoSession daoSession;
    @Inject CommonOperations comonOperation;
    @Inject PAFragmentManager paFragmentManager;
    @Inject ToolbarManager toolbarManager;
    @Inject DecimalFormat formatter;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        rvSearchItems = (RecyclerView) view.findViewById(R.id.recyc_item_search);
        textViewSearch = (TextView) view.findViewById(R.id.textViewSearch);
        textViewSearch.setVisibility(View.VISIBLE);
        textViewSearch.setText(getResources().getString(R.string.please_type));
        LinearManagerWithOutEx llm = new LinearManagerWithOutEx(getContext());
        rvSearchItems.setLayoutManager(llm);
        rvSearchItems.setAdapter(rvAdapterSearch);
        tempIcons = getResources().getStringArray(R.array.icons);
        rvSearchItems.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(container.getWindowToken(), 0);
                }
                return false;
            }
        });
        toolbarManager.enableSearchMode();
        toolbarManager.getToolbarSearch().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if (getContext() != null) {
                    String searchString = toolbarManager.getToolbarSearch().getText().toString();
                    changeListForResult(searchString);
                    prevSearchString = searchString;

                }
            }
        });
        toolbarManager.getToolbarSearch().requestFocus();
        toolbarManager.setOnHomeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paFragmentManager.getFragmentManager().popBackStack();
                paFragmentManager.displayMainWindow();
            }
        });
        prevSearchString = toolbarManager.getToolbarSearch().getText().toString();
        changeListForResult(prevSearchString);
        return view;
    }


    public void changeListForResult(String searchSt) {
        if (searchItemsToSend == null) {
            searchItemsToSend = new ArrayList<>();
        }
        if (searchItemsToSendForUse == null) {
            searchItemsToSendForUse = new ArrayList<>();
        }
        if (searchSt.length() == 1) {
            searchItemsToSend.clear();
            /*Credit Search*/
            QueryBuilder<CreditDetials> queryBuilderCred = daoSession.getCreditDetialsDao().queryBuilder();
            queryBuilderCred.whereOr(
                    CreditDetialsDao.Properties.Take_time.like("%" + searchSt + "%"),
                    CreditDetialsDao.Properties.Credit_name.like("%" + searchSt.toLowerCase() + "%"),
                    CreditDetialsDao.Properties.Credit_name.like("%" + searchSt.toUpperCase() + "%"),
                    CreditDetialsDao.Properties.Value_of_credit_with_procent.like("%" + searchSt.toLowerCase() + "%"),
                    CreditDetialsDao.Properties.Value_of_credit_with_procent.like("%" + searchSt.toUpperCase() + "%"));
            List<CreditDetials> cd = queryBuilderCred.build().list();
            for (CreditDetials temp : cd) {
                if (temp.getKey_for_archive())
                    searchItemsToSend.add(new SearchResultConten(temp.getCredit_name(), temp.getValue_of_credit_with_procent(), CREDIT_ARCHIVE, temp.getTake_time(), temp, temp.getIcon_ID(), (temp.getKey_for_include()) ? getString(R.string.include_balance) : getString(R.string.some_credit), temp.getValyute_currency(), null));
                else
                    searchItemsToSend.add(new SearchResultConten(temp.getCredit_name(), temp.getValue_of_credit_with_procent(), CREDIT_VAR, temp.getTake_time(), temp, temp.getIcon_ID(), (temp.getKey_for_include()) ? getString(R.string.cred_include_bal) : getString(R.string.cred_not_include_bal), temp.getValyute_currency(), null));
            }

            /*Credit Recking Search*/
            QueryBuilder<ReckingCredit> queryBuilder = daoSession.getReckingCreditDao().queryBuilder();
            queryBuilder.whereOr(
                    ReckingCreditDao.Properties.PayDate.like("%" + searchSt + "%"),
                    ReckingCreditDao.Properties.Comment.like("%" + searchSt.toLowerCase() + "%"),
                    ReckingCreditDao.Properties.Comment.like("%" + searchSt.toUpperCase() + "%"),
                    ReckingCreditDao.Properties.Amount.like("%" + searchSt + "%"),
                    queryBuilder.join(ReckingCreditDao.Properties.MyCredit_id, CreditDetials.class, CreditDetialsDao.Properties.MyCredit_id)
                            .or(CreditDetialsDao.Properties.Credit_name.like("%" + searchSt.toLowerCase() + "%"), CreditDetialsDao.Properties.Credit_name.like("%" + searchSt.toUpperCase() + "%")),
                    queryBuilder.join(ReckingCreditDao.Properties.AccountId, Account.class, AccountDao.Properties.Id)
                            .or(AccountDao.Properties.Name.like("%" + searchSt.toLowerCase() + "%"), AccountDao.Properties.Name.like("%" + searchSt.toUpperCase() + "%"))
            );
            List<ReckingCredit> rk = queryBuilder.build().list();
            for (ReckingCredit temp : rk) {
                CreditDetials creditDetialse = daoSession.getCreditDetialsDao().load(temp.getMyCredit_id());
                Account acc = daoSession.getAccountDao().load(temp.getAccountId());
                searchItemsToSend.add(new SearchResultConten(creditDetialse.getCredit_name(), temp.getAmount() * -1d, CREDIT_RECKING, temp.getPayDate(), temp, creditDetialse.getIcon_ID(), temp.getComment(), creditDetialse.getValyute_currency(), acc));
            }

            /*Finance Record Search*/
            QueryBuilder<FinanceRecord> queryBuilderFinance1 = daoSession.getFinanceRecordDao().queryBuilder();
            queryBuilderFinance1.whereOr(
                    FinanceRecordDao.Properties.Date.like("%" + searchSt + "%"),
                    FinanceRecordDao.Properties.Amount.like("%" + searchSt + "%"),
                    FinanceRecordDao.Properties.Comment.like("%" + searchSt.toLowerCase() + "%"),
                    FinanceRecordDao.Properties.Comment.like("%" + searchSt.toUpperCase() + "%"),
                    queryBuilderFinance1.join(FinanceRecordDao.Properties.CategoryId, RootCategory.class, RootCategoryDao.Properties.Id)
                            .or(RootCategoryDao.Properties.Name.like("%" + searchSt.toLowerCase() + "%"), RootCategoryDao.Properties.Name.like("%" + searchSt.toUpperCase() + "%")),
                    queryBuilderFinance1.join(FinanceRecordDao.Properties.AccountId, Account.class, AccountDao.Properties.Id)
                            .or(AccountDao.Properties.Name.like("%" + searchSt.toLowerCase() + "%"), AccountDao.Properties.Name.like("%" + searchSt.toUpperCase() + "%"))

            );

            QueryBuilder<FinanceRecord> queryBuilderFinance2 = daoSession.getFinanceRecordDao().queryBuilder();
            queryBuilderFinance2.where(FinanceRecordDao.Properties.SubCategoryId.isNotNull()).where(
                    queryBuilderFinance2.join(FinanceRecordDao.Properties.SubCategoryId, SubCategory.class, SubCategoryDao.Properties.Id)
                            .or(SubCategoryDao.Properties.Name.like("%" + searchSt.toLowerCase() + "%"), SubCategoryDao.Properties.Name.like("%" + searchSt.toUpperCase() + "%"))

            );

            List<FinanceRecord> fcats1 = queryBuilderFinance1.build().list();
            List<FinanceRecord> fcats2 = queryBuilderFinance2.build().list();
            if (fcats2.size() != 0)
                for (int t = fcats2.size() - 1; t >= 0; t--) {
                    for (FinanceRecord item : fcats1) {
                         if (!item.equals(fcats2.get(t))) {
                            fcats2.add(item);
                        }
                    }
                }

            for (FinanceRecord temp : fcats1) {
                if (temp.getCategory().getType() == PocketAccounterGeneral.INCOME) {

                    if (temp.getSubCategory() == null) {
                        searchItemsToSend.add(new SearchResultConten(temp.getCategory().getName(), temp.getAmount(), SIMPLE_RECKING, temp.getDate(), temp, temp.getCategory().getIcon(), temp.getComment(), temp.getCurrency(), temp.getAccount()));

                    } else {
                        searchItemsToSend.add(new SearchResultConten(temp.getCategory().getName() + ",\n" + temp.getSubCategory().getName(), temp.getAmount(), SIMPLE_RECKING, temp.getDate(), temp, temp.getSubCategory().getIcon(), temp.getComment(), temp.getCurrency(), temp.getAccount()));

                    }

                }
                if (temp.getCategory().getType() == PocketAccounterGeneral.EXPENSE) {
                    if (temp.getSubCategory() == null)
                        searchItemsToSend.add(new SearchResultConten(temp.getCategory().getName(), temp.getAmount() * -1d, SIMPLE_RECKING, temp.getDate(), temp, temp.getCategory().getIcon(), temp.getComment(), temp.getCurrency(), temp.getAccount()));
                    else
                        searchItemsToSend.add(new SearchResultConten(temp.getCategory().getName() + ",\n" + temp.getSubCategory().getName(), temp.getAmount() * -1d, SIMPLE_RECKING, temp.getDate(), temp, temp.getSubCategory().getIcon(), temp.getComment(), temp.getCurrency(), temp.getAccount()));
                }

            }

            /*Debt Borrow Record Search*/
            QueryBuilder<DebtBorrow> queryBuilderDebtBorrow = daoSession.getDebtBorrowDao().queryBuilder();
            queryBuilderDebtBorrow.whereOr(
                    DebtBorrowDao.Properties.TakenDate.like("%" + searchSt + "%"),
                    DebtBorrowDao.Properties.Amount.like("%" + searchSt + "%"),
                    queryBuilderDebtBorrow.join(DebtBorrowDao.Properties.PerId, Person.class, PersonDao.Properties.Id)
                            .or(PersonDao.Properties.Name.like("%" + searchSt.toLowerCase() + "%"), PersonDao.Properties.Name.like("%" + searchSt.toUpperCase() + "%")),
                    queryBuilderDebtBorrow.join(DebtBorrowDao.Properties.AccountId, Account.class, AccountDao.Properties.Id)
                            .or(AccountDao.Properties.Name.like("%" + searchSt.toLowerCase() + "%"), AccountDao.Properties.Name.like("%" + searchSt.toUpperCase() + "%"))

            );
            List<DebtBorrow> dbors = queryBuilderDebtBorrow.build().list();
            for (DebtBorrow temp : dbors) {
                if (temp.getType() == DebtBorrow.DEBT)
                    if (temp.getTo_archive()) {
                        searchItemsToSend.add(new SearchResultConten(temp.getPerson().getName(), temp.getAmount() * -1d, DEBT_ARCHIVE, temp.getTakenDate(), temp, temp.getPerson().getPhoto(), (temp.getCalculate()) ? getString(R.string.debt_com) : getString(R.string.debt_comment_not_icluded), temp.getCurrency(), temp.getAccount()));
                    } else
                        searchItemsToSend.add(new SearchResultConten(temp.getPerson().getName(), temp.getAmount() * -1d, DEBT_VAR, temp.getTakenDate(), temp, temp.getPerson().getPhoto(), (temp.getCalculate()) ? getString(R.string.debt_coment) : getString(R.string.debt_comment_to), temp.getCurrency(), temp.getAccount()));

                if (temp.getType() == DebtBorrow.BORROW)
                    if (temp.getTo_archive()) {
                        searchItemsToSend.add(new SearchResultConten(temp.getPerson().getName(), temp.getAmount(), BORROW_ARCHIVE, temp.getTakenDate(), temp, temp.getPerson().getPhoto(), (temp.getCalculate()) ? getString(R.string.borrow_com) : getString(R.string.borrow_comment_not_icl_arch), temp.getCurrency(), temp.getAccount()));
                    } else
                        searchItemsToSend.add(new SearchResultConten(temp.getPerson().getName(), temp.getAmount(), BORROW_VAR, temp.getTakenDate(), temp, temp.getPerson().getPhoto(), (temp.getCalculate()) ? getString(R.string.borrow_comment) : getString(R.string.borrow_coment_incl), temp.getCurrency(), temp.getAccount()));
            }
            QueryBuilder<Recking> queryBuilderDebtBorrowRecking = daoSession.getReckingDao().queryBuilder();
            Join anmeJoin = queryBuilderDebtBorrowRecking.join(ReckingDao.Properties.DebtBorrowsId, DebtBorrow.class, DebtBorrowDao.Properties.Id);
            queryBuilderDebtBorrowRecking.whereOr(
                    ReckingDao.Properties.Amount.like("%" + searchSt + "%"),
                    ReckingDao.Properties.PayDate.like("%" + searchSt + "%"),
                    queryBuilderDebtBorrowRecking.join(ReckingDao.Properties.AccountId, Account.class, AccountDao.Properties.Id)
                            .or(AccountDao.Properties.Name.like("%" + searchSt.toLowerCase() + "%"), AccountDao.Properties.Name.like("%" + searchSt.toUpperCase() + "%")),
                    queryBuilderDebtBorrowRecking.join(anmeJoin, DebtBorrowDao.Properties.PerId, Person.class, PersonDao.Properties.Id)
                            .or(PersonDao.Properties.Name.like("%" + searchSt.toLowerCase() + "%"), PersonDao.Properties.Name.like("%" + searchSt.toUpperCase() + "%"))
            );
            List<Recking> rcDebtBo = queryBuilderDebtBorrowRecking.build().list();
            for (Recking temp : rcDebtBo) {
                DebtBorrow debtBorrowTemp = daoSession.getDebtBorrowDao().load(temp.getDebtBorrowsId());
                Account acc = daoSession.getAccountDao().load(temp.getAccountId());
                if (debtBorrowTemp.getType() == DebtBorrow.DEBT)
                    searchItemsToSend.add(new SearchResultConten(debtBorrowTemp.getPerson().getName(), temp.getAmount(), DEBT_RECKING, temp.getPayDate(), temp, debtBorrowTemp.getPerson().getPhoto(), temp.getComment(), debtBorrowTemp.getCurrency(), acc));
                else if (debtBorrowTemp.getType() == DebtBorrow.BORROW)
                    searchItemsToSend.add(new SearchResultConten(debtBorrowTemp.getPerson().getName(), temp.getAmount(), BORROW_RECKING, temp.getPayDate(), temp, debtBorrowTemp.getPerson().getPhoto(), temp.getComment(), debtBorrowTemp.getCurrency(), acc));

            }

            if (searchItemsToSend != null)
                Collections.sort(searchItemsToSend, new Comparator<SearchResultConten>() {
                    @Override
                    public int compare(SearchResultConten con1, SearchResultConten con2) {
                        return con2.getMyDate().compareTo(con1.getMyDate());
                    }
                });
            if(searchItemsToSend.size()==0){
                textViewSearch.setVisibility(View.VISIBLE);
                textViewSearch.setText(getString(R.string.cant_find)+" \""+searchSt+"\"");
            }
            else
            {
                textViewSearch.setVisibility(View.GONE);
            }
            rvAdapterSearch.setDataList(searchItemsToSend, getActivity(), searchSt);
        } else {
            searchItemsToSendForUse.clear();
            for (SearchResultConten temp : searchItemsToSend) {
                if (temp.getStNameOfItem().toLowerCase().contains(searchSt.toLowerCase())) {
                    searchItemsToSendForUse.add(temp);
                    continue;
                }
                if (temp.getAccount() != null)
                    if (temp.getAccount().getName().toLowerCase().contains(searchSt.toLowerCase())) {
                        searchItemsToSendForUse.add(temp);
                        continue;
                    }

                if (temp.getComment() != null)
                    if (!temp.getComment().equals(""))
                        if (temp.getComment().toLowerCase().contains(searchSt.toLowerCase())) {
                            searchItemsToSendForUse.add(temp);
                            continue;
                        }

                if (formatter.format(temp.getdAmount()).toLowerCase().contains(searchSt.toLowerCase())) {
                    searchItemsToSendForUse.add(temp);
                    continue;
                }
                if (dateformarter.format(temp.getMyDate().getTime()).toLowerCase().contains(searchSt.toLowerCase())) {
                    searchItemsToSendForUse.add(temp);
                    continue;
                }
            }

            if(searchItemsToSendForUse.size()==0){
                textViewSearch.setVisibility(View.VISIBLE);
                textViewSearch.setText(getResources().getString(R.string.cant_find)+" \""+searchSt+"\"");
            }
            else
            {
                textViewSearch.setVisibility(View.GONE);
            }
            rvAdapterSearch.setDataList(searchItemsToSendForUse, getActivity(), searchSt);
        }
        rvAdapterSearch.notifyDataSetChanged();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        toolbarManager.disableSearchMode();
    }

    private class RVAdapterSearch extends RecyclerView.Adapter<RVAdapterSearch.SearchItemViewHolder> {
        private List<SearchResultConten> searchItems;
        private Context context;
        private String seq;
        private final String COLOR_SEQ = "#D8EAC6";

        public void setDataList(List<SearchResultConten> searchItems, Context context, String seq) {
            this.searchItems = searchItems;
            this.context = context;
            this.seq = seq;
        }

        @Override
        public SearchItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_layout, parent, false);
            SearchItemViewHolder vh = new SearchItemViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(SearchItemViewHolder holder, final int position) {
            final SearchResultConten item = searchItems.get(position);

            /*Set icon*/
            if (item.getStTypeSearch() == DEBT_ARCHIVE || item.getStTypeSearch() == DEBT_RECKING
                    || item.getStTypeSearch() == DEBT_VAR || item.getStTypeSearch() == BORROW_ARCHIVE
                    || item.getStTypeSearch() == BORROW_RECKING || item.getStTypeSearch() == BORROW_VAR) {

                if (!item.getIcon().equals("") && !item.getIcon().matches("0")) {
                    try {
                        holder.iconik.setImageBitmap(queryContactImage(Integer.parseInt(item.getIcon())));
                    } catch (NumberFormatException e) {
                        holder.iconik.setImageDrawable(Drawable.createFromPath(item.getIcon()));
                    }
                } else {
                    holder.iconik.setImageResource(R.drawable.no_photo);
                }


            } else {
                if (item.isItIconWithId()) {
                    holder.iconik.setImageResource(item.getIcon_Id());
                } else if (item.getIcon() != null)
                    holder.iconik.setImageResource(getResources().getIdentifier(item.getIcon(), "drawable", context.getPackageName()));
                else
                    holder.iconik.setImageResource(getResources().getIdentifier("icons_2", "drawable", context.getPackageName()));
            }

            /*Set date*/
            if (item.getMyDate() != null)
                comonOperation.ColorSubSeq(dateformarter.format(item.getMyDate().getTime()), seq, COLOR_SEQ, holder.date);

            /*Set name of item*/
            comonOperation.ColorSubSeq(item.getStNameOfItem(), seq, "#D8EAC6", holder.name);

            /*Set item amount*/
            if (item.getdAmount() == 0) {
                comonOperation.ColorSubSeq(formatter.format(item.getdAmount()) + item.getCurrency().getAbbr(), seq, COLOR_SEQ, holder.ammount);

            }
            if (item.getdAmount() > 0) {
                holder.ammount.setTextColor(Color.parseColor("#028929"));
                comonOperation.ColorSubSeq("+" + formatter.format(item.getdAmount()) + item.getCurrency().getAbbr(), seq, COLOR_SEQ, holder.ammount);
            } else {
                holder.ammount.setTextColor(Color.parseColor("#b82101"));
                comonOperation.ColorSubSeq(formatter.format(item.getdAmount()) + item.getCurrency().getAbbr(), seq, COLOR_SEQ, holder.ammount);

            }
            /*Set type*/
            comonOperation.ColorSubSeq(item.getTypeInString(context), seq, COLOR_SEQ, holder.type);

            /*Set Comment*/
            if (item.getComment() != null) {
                if (item.getComment().length() != 0) {
                    comonOperation.ColorSubSeq(item.getComment(), seq, COLOR_SEQ, holder.comment);
                    holder.relativeLayout.setVisibility(View.VISIBLE);
//                    holder.forgoneLine.setVisibility(View.VISIBLE);
                } else {
                    holder.relativeLayout.setVisibility(View.GONE);
//                    holder.forgoneLine.setVisibility(View.GONE);
                }
            } else {
                holder.relativeLayout.setVisibility(View.GONE);
//                holder.forgoneLine.setVisibility(View.GONE);
            }

            /*Set Onclick Listner*/
            switch (item.getStTypeSearch()) {
                case SIMPLE_RECKING:
                    holder.mainItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            paFragmentManager.displayFragment(n);
                            Handler hand = new Handler();
                            hand.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
                                    toolbarManager.setOnHomeButtonClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            paFragmentManager.displayMainWindow();
                                        }
                                    });
                                    toolbarManager.setVisiblityEditSearch();
                                    Bundle bundle = new Bundle();
                                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                                    bundle.putString(RecordDetailFragment.DATE, format.format(((FinanceRecord) item.getParrentObject()).getDate().getTime()));
                                    bundle.putString(RecordDetailFragment.RECORD_ID, ((FinanceRecord) item.getParrentObject()).getRecordId());
                                    bundle.putInt(RecordDetailFragment.PARENT, PocketAccounterGeneral.SEARCH_MODE);
                                    RecordEditFragment fragment = new RecordEditFragment();
                                    fragment.setArguments(bundle);
                                    paFragmentManager.displayFragment(fragment);

                                }
                            }, 500);

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    });
                    break;
                case CREDIT_VAR:
                    holder.mainItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Handler hand = new Handler();
                            hand.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    toolbarManager.disableSearchMode();
                                    if(((CreditDetials) item.getParrentObject()).getKey_for_archive()){
                                        InfoCreditFragmentForArchive temp = new InfoCreditFragmentForArchive();
                                        Bundle bundle = new Bundle();
                                        bundle.putLong(CreditTabLay.CREDIT_ID,((CreditDetials) item.getParrentObject()).getMyCredit_id());
                                        bundle.putInt(CreditTabLay.MODE,PocketAccounterGeneral.SEARCH_MODE);
                                        temp.setArguments(bundle);
                                        toolbarManager.disableSearchMode();
                                        paFragmentManager.displayFragment(temp);
                                    }
                                    else {
                                        InfoCreditFragment temp = new InfoCreditFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putLong(CreditTabLay.CREDIT_ID,((CreditDetials) item.getParrentObject()).getMyCredit_id());
                                        bundle.putInt(CreditTabLay.MODE,PocketAccounterGeneral.SEARCH_MODE);
                                        temp.setArguments(bundle);
                                        toolbarManager.disableSearchMode();
                                        paFragmentManager.displayFragment(temp);
                                    }
                                }
                            }, 500);

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    });

                    break;
                case CREDIT_ARCHIVE:
                case CREDIT_RECKING:
                    holder.mainItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Handler hand = new Handler();
                            hand.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ReckingCredit reckingCredit=(ReckingCredit) item.getParrentObject();
                                    toolbarManager.disableSearchMode();
                                    CreditDetials parentCreditDetials = daoSession.getCreditDetialsDao().load(reckingCredit.getMyCredit_id());
                                    if(parentCreditDetials.getKey_for_archive()){
                                        InfoCreditFragmentForArchive temp = new InfoCreditFragmentForArchive();
                                        Bundle bundle = new Bundle();
                                        bundle.putLong(CreditTabLay.CREDIT_ID,((CreditDetials) item.getParrentObject()).getMyCredit_id());
                                        bundle.putInt(CreditTabLay.MODE,PocketAccounterGeneral.SEARCH_MODE);
                                        temp.setArguments(bundle);
                                        paFragmentManager.displayFragment(temp);
                                        toolbarManager.disableSearchMode();
                                    }
                                    else {
                                        InfoCreditFragment temp = new InfoCreditFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putLong(CreditTabLay.CREDIT_ID,((CreditDetials) item.getParrentObject()).getMyCredit_id());
                                        bundle.putInt(CreditTabLay.MODE,PocketAccounterGeneral.SEARCH_MODE);
                                        temp.setArguments(bundle);
                                        paFragmentManager.displayFragment(temp);
                                        toolbarManager.disableSearchMode();
                                    }

                                }
                            }, 500);

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    });

                    break;

                case DEBT_VAR:
                case BORROW_VAR:
                    holder.mainItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Handler hand = new Handler();
                            hand.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Bundle bundle = new Bundle();
                                    bundle.putString(DebtBorrowFragment.DEBT_BORROW_ID, ((DebtBorrow) item.getParrentObject()).getId());
                                    bundle.putInt(DebtBorrowFragment.MODE, PocketAccounterGeneral.SEARCH_MODE);
                                    InfoDebtBorrowFragment fragment = new InfoDebtBorrowFragment();
                                    fragment.setArguments(bundle);
                                    toolbarManager.disableSearchMode();
                                    paFragmentManager.displayFragment(fragment);
                                }
                            }, 500);

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        }
                    });

                    break;

                case DEBT_RECKING:
                case BORROW_RECKING:
                    holder.mainItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Handler hand = new Handler();
                            hand.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Recking recking = (Recking) item.getParrentObject();
                                    toolbarManager.disableSearchMode();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(DebtBorrowFragment.DEBT_BORROW_ID, recking.getDebtBorrowsId());
                                    bundle.putInt(DebtBorrowFragment.MODE, PocketAccounterGeneral.SEARCH_MODE);
                                    InfoDebtBorrowFragment fragment = new InfoDebtBorrowFragment();
                                    fragment.setArguments(bundle);
                                    paFragmentManager.displayFragment(fragment);

                                }
                            }, 500);

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        }
                    });
                    break;

            }

            /*Account seting*/
            if (item.getAccount() == null) {
                holder.ifNotHaveAccount.setVisibility(View.GONE);

            } else {
                holder.ifNotHaveAccount.setVisibility(View.VISIBLE);
                comonOperation.ColorSubSeq(item.getAccount().getName(), seq, COLOR_SEQ, holder.accountName);
            }

        }

        @Override
        public int getItemCount() {
            return (searchItems == null) ? 0 : searchItems.size();
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

        public class SearchItemViewHolder extends RecyclerView.ViewHolder {
            ImageView iconik;
            TextView name;
            TextView ammount;
            TextView type;
            TextView date;
            TextView comment;
            LinearLayout relativeLayout;
            LinearLayout mainItemView;
            TextView accountName;
            LinearLayout ifNotHaveAccount;

            SearchItemViewHolder(View itemView) {
                super(itemView);
                iconik = (ImageView) itemView.findViewById(R.id.ivRecordDetail);
                name = (TextView) itemView.findViewById(R.id.tvRecordDetailCategoryName);
                ammount = (TextView) itemView.findViewById(R.id.tvRecordDetailCategoryAmount);
                type = (TextView) itemView.findViewById(R.id.type_search_item);
                date = (TextView) itemView.findViewById(R.id.dateToSearch);
                comment = (TextView) itemView.findViewById(R.id.tvComment);
                accountName = (TextView) itemView.findViewById(R.id.accountName);
                mainItemView = (LinearLayout) itemView.findViewById(R.id.mainview);
                relativeLayout = (LinearLayout) itemView.findViewById(R.id.visibleIfCommentHave);
                ifNotHaveAccount = (LinearLayout) itemView.findViewById(R.id.relativeLayout6);
            }
        }
    }
}
