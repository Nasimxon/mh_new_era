package com.jim.pocketaccounter.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.utils.Utils;
import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.animations.Cieo;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.Currency;
import com.jim.pocketaccounter.database.CurrencyDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.database.TemplateVoice;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.cache.DataCache;
import com.jim.pocketaccounter.utils.speech.PASpeechRecognizer;
import com.jim.pocketaccounter.utils.speech.SpeechListener;

import java.io.File;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

public class VoiceRecognizerFragment extends Fragment {
    public static final int DEBTBORROW = 0;
    public static final int CATEGORY = 1;
    public static final int SUBCATEGORY = 2;
    public static final int ACCOUNT = 3;
    //Not speech mode layout
    private RelativeLayout rlNotSpeechMode;
    //Not speech mode balance stripe textviews
    private TextView tvNotSpeechModeIncome, tvNotSpeechModeBalance, tvNotSpeechModeExpense;
    //Not speech mode records list
    private RecyclerView rvNotSpeechModeRecordsList;
    //Speech mode layout
    private LinearLayout llSpeechMode;
    //Speech mode adjective recognition
    private TextView tvSpeechModeAdjective;
    //Speech mode category recognition and its icon
    private TextView tvSpeechModeCategory;
    private ImageView ivSpeechModeCategoryIcon;
    //Speech mode amount and currency recognition
    private TextView tvSpeechAmount;
    private Spinner spSpeechCurrency;
    //Speech mode account recognition
    private Spinner spSpeechAccount;
    //Speech mode entered text
    private TextView tvSpeechModeEnteredText;
    //listening indicator
    private TextView tvListeningIndicator;
    //is listening started?
    boolean started = false;
    //made finance records;
    private List<FinanceRecord> records = new ArrayList<>();
    //collection finance record;
    private FinanceRecord record = null;
    //adjective definition array
    String[] definitionArrays;
    //Center clickable button
    private RelativeLayout rlCenterButton;
    //bg must be changed
    private ImageView ivCenterButton;
    //icon
    private ImageView ivMicrophoneIcon;
    //Speech recognize manager
    private PASpeechRecognizer recognizer;
    //record start left image
    private FrameLayout recStartLeft;
    //record start right image
    private FrameLayout recStartRight;
    //auto save voice
    private TextView autoSave;
    @Inject DaoSession daoSession;
    @Inject PAFragmentManager paFragmentManager;
    @Inject List<TemplateVoice> voices;
    @Inject
    DataCache dataCache;
    private String [] curString;
    private String [] accString;
    private CountDownTimer timer;
    private int leftSaving;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.voice_recognizer, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        rlCenterButton = (RelativeLayout) rootView.findViewById(R.id.rlCenterButton);
        ivCenterButton = (ImageView) rootView.findViewById(R.id.ivCenterButton);
        ivMicrophoneIcon = (ImageView) rootView.findViewById(R.id.ivMicrophoneIcon);
        recStartLeft = (FrameLayout) rootView.findViewById(R.id.flVoiceRecordStartLeft);
        recStartRight = (FrameLayout) rootView.findViewById(R.id.flVoiceRecordStartRight);
        tvSpeechAmount = (TextView) rootView.findViewById(R.id.tvSpeechAmount);
        tvSpeechModeCategory = (TextView) rootView.findViewById(R.id.tvSpeechModeCategory);
        spSpeechCurrency = (Spinner) rootView.findViewById(R.id.spSpeechCurrency);
        spSpeechAccount = (Spinner) rootView.findViewById(R.id.spSpeechAccount);
        tvSpeechModeAdjective = (TextView) rootView.findViewById(R.id.tvSpeechModeAdjective);
        autoSave = (TextView) rootView.findViewById(R.id.tvAutoSaveVoice);
        curString = new String[daoSession.getCurrencyDao().loadAll().size()];
        for (int i = 0; i < curString.length; i++) {
            curString[i] = daoSession.getCurrencyDao().loadAll().get(i).getAbbr();
        }
        ArrayAdapter<String> curAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, curString);
        spSpeechCurrency.setAdapter(curAdapter);
        accString = new String[daoSession.getAccountDao().loadAll().size()];
        for (int i = 0; i < accString.length; i++) {
            accString[i] = daoSession.getAccountDao().loadAll().get(i).getName();
        }
        final ArrayAdapter<String> accAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, accString);
        spSpeechAccount.setAdapter(accAdapter);

        rlCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!started) {
                    askForContactPermission();
                    startRecognition();
                    recStartRight.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.left_gone_anim));
                    recStartLeft.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.right_anim));
                    recStartRight.setVisibility(View.VISIBLE);
                    recStartLeft.setVisibility(View.VISIBLE);
                    paFragmentManager.setVerticalScrolling(false);
                } else {
                    recStartRight.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.left_anim));
                    recStartLeft.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.right_gone_anim));
                    recStartRight.setVisibility(View.GONE);
                    recStartLeft.setVisibility(View.GONE);
                    paFragmentManager.setVerticalScrolling(true);
                    stopRecognition();
                }
                started = !started;
            }
        });
        tvSpeechModeEnteredText = (TextView) rootView.findViewById(R.id.tvSpeechModeEnteredText);
        tvListeningIndicator = (TextView) rootView.findViewById(R.id.tvListeningIndicator);
        recStartLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null) {
                    // canceled task
                    timer.cancel();
                    timer = null;
                    tvSpeechModeAdjective.setText("");
                    tvSpeechAmount.setText("0.0");
                    categoryId = "";
                    accountId = "";
                    currencyId = "";
                    summ = 0;
                }
            }
        });
        recStartRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savingVoice();
            }
        });
        recognizer = new PASpeechRecognizer(getContext());
        recognizer.setSpeechListener(new SpeechListener() {
            @Override
            public void onSpeechEnd(List<String> speechResult) {
                processSpeechResults(speechResult);
            }

            @Override
            public void onSpeechPartialListening(List<String> speechResult) {
                processSpeechResults(speechResult);
            }

            @Override
            public void onChangeState(final boolean started) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (started) {
                            ivCenterButton.setBackgroundResource(R.drawable.speech_pressed_circle);
                            ivMicrophoneIcon.setColorFilter(Color.WHITE);
                            paFragmentManager.setVerticalScrolling(false);
                        }
                        else {
                            ivCenterButton.setBackgroundResource(R.drawable.white_circle);
                            ivMicrophoneIcon.setColorFilter(Color.parseColor("#414141"));
                            paFragmentManager.setVerticalScrolling(true);
                        }
                        VoiceRecognizerFragment.this.started = started;
                    }
                });
            }
        });
        return rootView;
    }

    private void processSpeechResults(final List<String> speechResult) {
        if (speechResult != null && !speechResult.isEmpty()) {
            tvSpeechModeEnteredText.setText(speechResult.get(0));
            parseVoice(speechResult.get(0));
        }
    }

    private void startRecognition() {
        ivCenterButton.setBackgroundResource(R.drawable.speech_pressed_circle);
        ivMicrophoneIcon.setColorFilter(Color.WHITE);
        recognizer.startVoiceRecognitionCycle();
    }
    private void stopRecognition() {
        ivCenterButton.setBackgroundResource(R.drawable.white_circle);
        ivMicrophoneIcon.setColorFilter(Color.parseColor("#414141"));
        recognizer.stopVoiceRecognition();
    }
    private void refreshList() {
        SpeechRecognizerAdapter adapter = new SpeechRecognizerAdapter(records);
//        rvSpeechRecognize.setAdapter(adapter);
    }

    private class SpeechRecognizerAdapter extends RecyclerView.Adapter<VoiceRecognizerFragment.ViewHolder> {
        private List<FinanceRecord> result;
        public SpeechRecognizerAdapter(List<FinanceRecord> result) {
            this.result = result;
        }
        public int getItemCount() {
            return result.size();
        }
        public void onBindViewHolder(final VoiceRecognizerFragment.ViewHolder view, final int position) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            view.tvSpeechRecognizeFinanceRecordDate.setText(simpleDateFormat.format(result.get(position).getDate().getTime()));
            view.tvSpeechRecognizeFinanceRecordCategoryName.setText(result.get(position).getCategory().getName());
            if (result.get(position).getSubCategory().getName() != null)
                view.tvSpeechRecognizeFinanceRecordSubcategoryName.setText(result.get(position).getSubCategory().getName());
            view.tvSpeechRecognizeFinanceRecordAccountName.setText(result.get(position).getAccount().getName());
            view.tvSpeechRecognizeFinanceRecordAccountName.setText(result.get(position).getAccount().getName());
            view.tvSpeechRecognizeFinanceRecordAmount.setText("" + result.get(position).getAmount());
            view.tvSpeechRecognizeFinanceRecordCurrency.setText(result.get(position).getCurrency().getName());

        }
        public VoiceRecognizerFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.speech_rec_finance_record_list_item, parent, false);
            return new VoiceRecognizerFragment.ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpeechRecognizeFinanceRecordDate,
                tvSpeechRecognizeFinanceRecordCategoryName,
                tvSpeechRecognizeFinanceRecordSubcategoryName,
                tvSpeechRecognizeFinanceRecordAccountName,
                tvSpeechRecognizeFinanceRecordAmount,
                tvSpeechRecognizeFinanceRecordCurrency;
        public ViewHolder(View view) {
            super(view);
            tvSpeechRecognizeFinanceRecordDate = (TextView) view.findViewById(R.id.tvSpeechRecognizeFinanceRecordDate);
            tvSpeechRecognizeFinanceRecordCategoryName = (TextView) view.findViewById(R.id.tvSpeechRecognizeFinanceRecordCategoryName);
            tvSpeechRecognizeFinanceRecordSubcategoryName = (TextView) view.findViewById(R.id.tvSpeechRecognizeFinanceRecordSubcategoryName);
            tvSpeechRecognizeFinanceRecordAccountName = (TextView) view.findViewById(R.id.tvSpeechRecognizeFinanceRecordAccountName);
            tvSpeechRecognizeFinanceRecordAmount = (TextView) view.findViewById(R.id.tvSpeechRecognizeFinanceRecordAmount);
            tvSpeechRecognizeFinanceRecordCurrency = (TextView) view.findViewById(R.id.tvSpeechRecognizeFinanceRecordCurrency);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.stopVoiceRecognition();
    }

    private String categoryId = "";
    private String accountId = "";
    private String currencyId = "";
    private double summ = 0;

    private void parseVoice(String newLetter) {
        for (TemplateVoice temp : voices) {
            if (newLetter.toLowerCase().matches(temp.getRegex())) {
                if (categoryId.isEmpty() && daoSession.getRootCategoryDao().load(temp.getCategoryId()) != null
                        || daoSession.getSubCategoryDao().load(temp.getCategoryId()) != null) {
                    categoryId = temp.getCategoryId();
                }
                if (accountId.isEmpty() && daoSession.getAccountDao().load(temp.getCategoryId()) != null) {
                    accountId = temp.getCategoryId();
                }
                if (currencyId.isEmpty() && daoSession.getCurrencyDao().load(temp.getCategoryId()) != null) {
                    currencyId = temp.getCategoryId();
                }
                if (daoSession.getRootCategoryDao().load(temp.getCategoryId()) != null) {
                    RootCategory comeCategory = daoSession.getRootCategoryDao().load(temp.getCategoryId());
                    if (daoSession.getRootCategoryDao().load(categoryId) != null) {
                        RootCategory oldCategory = daoSession.getRootCategoryDao().load(categoryId);
                        if (comeCategory.getName().contains(oldCategory.getName())
                                || newLetter.indexOf(comeCategory.getName()) > newLetter.indexOf(oldCategory.getName())) {
                            categoryId = temp.getCategoryId();
                        }
                    } else {
                        SubCategory oldSubCategory = daoSession.getSubCategoryDao().load(categoryId);
                        if (comeCategory.getName().contains(oldSubCategory.getName())
                                || newLetter.indexOf(comeCategory.getName()) > newLetter.indexOf(oldSubCategory.getName())) {
                            categoryId = temp.getCategoryId();
                        }
                    }
                } else if (daoSession.getSubCategoryDao().load(temp.getCategoryId()) != null) {
                    SubCategory comeSubCategory = daoSession.getSubCategoryDao().load(temp.getCategoryId());
                    if (daoSession.getSubCategoryDao().load(categoryId) != null) {
                        SubCategory oldSubCategory = daoSession.getSubCategoryDao().load(categoryId);
                        if (comeSubCategory.getName().contains(oldSubCategory.getName())
                                || newLetter.indexOf(comeSubCategory.getName()) > newLetter.indexOf(oldSubCategory.getName())) {
                            categoryId = temp.getCategoryId();
                        }
                    } else {
                        RootCategory oldCategory = daoSession.getRootCategoryDao().load(categoryId);
                        if (comeSubCategory.getName().contains(oldCategory.getName())
                                || newLetter.indexOf(comeSubCategory.getName()) > newLetter.indexOf(oldCategory.getName())) {
                            categoryId = temp.getCategoryId();
                        }
                    }
                } else if (daoSession.getAccountDao().load(temp.getCategoryId()) != null) {
                    Account comeAccount = daoSession.getAccountDao().load(temp.getCategoryId());
                    Account oldAccount = daoSession.getAccountDao().load(accountId);
                    if (comeAccount.getName().contains(oldAccount.getName())
                            || newLetter.indexOf(comeAccount.getName()) > newLetter.indexOf(oldAccount.getName())) {
                        accountId = temp.getCategoryId();
                    }
                } else if (daoSession.getCurrencyDao().load(temp.getCategoryId()) != null) {
                    Currency comeCurrency = daoSession.getCurrencyDao().load(temp.getCategoryId());
                    Currency oldCurrency = daoSession.getCurrencyDao().load(currencyId);
                    if (comeCurrency.getName().contains(oldCurrency.getName()) ||
                            newLetter.indexOf(comeCurrency.getName()) > newLetter.indexOf(oldCurrency.getName())) {
                        currencyId = temp.getCategoryId();
                    }
                }
            }
        }
        String amountRegex = "[([^0-9]*)\\s*([0-9]+[.,]?[0-9]*)]*\\s([$]*)([0-9]+[.,]?[0-9]*).*";
        Pattern pattern = Pattern.compile(amountRegex);
        Matcher matcher = pattern.matcher(newLetter);
        if (matcher.matches()) {
            summ = Double.parseDouble(matcher.group(matcher.groupCount()));
        }
        tvSpeechAmount.setText("" + summ);
        if (!categoryId.isEmpty()) {
            if (daoSession.getRootCategoryDao().load(categoryId) != null) {
                RootCategory rootCategory = daoSession.getRootCategoryDao().load(categoryId);
                if (rootCategory.getType() == PocketAccounterGeneral.INCOME)
                    tvSpeechModeAdjective.setText(getResources().getString(R.string.income));
                else tvSpeechModeAdjective.setText(getResources().getString(R.string.expanse));
                tvSpeechModeCategory.setText(rootCategory.getName());
            } else if (daoSession.getSubCategoryDao().load(categoryId) != null) {
                SubCategory subCategory = daoSession.getSubCategoryDao().load(categoryId);
                if (daoSession.getRootCategoryDao().load(subCategory.getParentId()).getType() == PocketAccounterGeneral.INCOME)
                    tvSpeechModeAdjective.setText(getResources().getString(R.string.income));
                else tvSpeechModeAdjective.setText(getResources().getString(R.string.expanse));
                tvSpeechModeCategory.setText(daoSession.getRootCategoryDao().load
                        (subCategory.getParentId()).getName() + ", " + subCategory.getName());
            }
            if (!accountId.isEmpty()) {
                Account account = daoSession.getAccountDao().load(accountId);
                for (int i = 0; i < accString.length; i++) {
                    if (account.getName().toLowerCase().equals(accString[i])) {
                        spSpeechAccount.setSelection(i);
                        break;
                    }
                }
            }
            if (!currencyId.isEmpty()) {
                Currency currency = daoSession.getCurrencyDao().load(currencyId);
                for (int i = 0; i < curString.length; i++) {
                    if (currency.getAbbr().equals(curString[i])) {
                        spSpeechCurrency.setSelection(i);
                        break;
                    }
                }
            }
            leftSaving = 5;
            if(timer != null) {
                timer.cancel();
                timer = null;
            }
            autoSave.setVisibility(View.VISIBLE);
            timer = new CountDownTimer(6000, 1000) {
                @Override
                public void onTick(long l) {
                    autoSave.setText("" + Math.ceil(l/1000));
                }
                @Override
                public void onFinish() {
                    autoSave.setVisibility(View.GONE);
                    savingVoice();
                }
            }.start();
        }
    }

    private void savingVoice() {
        if (timer != null) {
            // saving operation
            if (!categoryId.isEmpty() && summ != 0) {
                if (daoSession.getRootCategoryDao().load(categoryId) != null ||
                        daoSession.getSubCategoryDao().load(categoryId) != null) {
                    FinanceRecord financeRecord = new FinanceRecord();
                    financeRecord.setDate(dataCache.getEndDate());
                    financeRecord.setAmount(summ);
                    financeRecord.setComment("");
                    financeRecord.setRecordId(UUID.randomUUID().toString());
                    if (accountId.isEmpty()) {
                        financeRecord.setAccount(daoSession.getAccountDao().queryBuilder().
                                where(AccountDao.Properties.Name.eq(spSpeechAccount.getSelectedItem())).unique());
                    } else {
                        financeRecord.setAccount(daoSession.getAccountDao().load(accountId));
                    }
                    if (currencyId.isEmpty()) {
                        financeRecord.setCurrency(daoSession.getCurrencyDao().queryBuilder()
                                .where(CurrencyDao.Properties.Abbr.eq(spSpeechCurrency.getSelectedItem())).unique());
                    } else {
                        financeRecord.setCurrency(daoSession.getCurrencyDao().load(currencyId));
                    }
                    if (daoSession.getRootCategoryDao().load(categoryId) != null) {
                        financeRecord.setCategory(daoSession.getRootCategoryDao().load(categoryId));
                    } else {
                        SubCategory subCategory = daoSession.getSubCategoryDao().load(categoryId);
                        financeRecord.setCategory(daoSession.getRootCategoryDao().load(subCategory.getParentId()));
                        financeRecord.setSubCategory(subCategory);
                    }
                    daoSession.getFinanceRecordDao().insertOrReplace(financeRecord);
                    timer.cancel();
                    timer = null;
                    tvSpeechModeAdjective.setText("");
                    tvSpeechAmount.setText("0.0");
                    categoryId = "";
                    accountId = "";
                    currencyId = "";
                    summ = 0;
                    Log.d("sss", "inserting record");
                }
            }
        }
    }

    private final int PERMISSION_REQUEST_RECORD = 0;

    public void askForContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.RECORD_AUDIO)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.contact_access_needed);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage(R.string.please_confirm_contact_access);//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.RECORD_AUDIO}
                                    , PERMISSION_REQUEST_RECORD);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSION_REQUEST_RECORD);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;
            }
        }
    }
}