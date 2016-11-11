package com.jim.pocketaccounter.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.utils.speech.ListeningOfSpeechListener;
import com.jim.pocketaccounter.utils.speech.PASpeechRecognizer;
import com.jim.pocketaccounter.utils.speech.SpeechListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class VoiceRecognizerFragment extends Fragment {
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
    //booleans are looking after filling all need fields of collecting record
    private boolean isDefinitionSet = false,
            isCategoryChoosen = false,
            isAccountChoosen = false,
            isAmountChoosen = false,
            isCurrencyChoosen = false;
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
    @Inject DaoSession daoSession;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.voice_recognizer, container, false);
        ((PocketAccounterApplication) getContext().getApplicationContext()).component().inject(this);
        rlCenterButton = (RelativeLayout) rootView.findViewById(R.id.rlCenterButton);
        ivCenterButton = (ImageView) rootView.findViewById(R.id.ivCenterButton);
        ivMicrophoneIcon = (ImageView) rootView.findViewById(R.id.ivMicrophoneIcon);
        rlCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!started) {
                    startRecognition();
                }
                else {
                    stopRecognition();

                }
                started = !started;
            }
        });
        tvSpeechModeEnteredText = (TextView) rootView.findViewById(R.id.tvSpeechModeEnteredText);
        tvListeningIndicator = (TextView) rootView.findViewById(R.id.tvListeningIndicator);
//        definitionArrays = getResources().getStringArray(R.array.speechAdjectives);
//        btnVoiceRecognize = (Button) rootView.findViewById(R.id.btnVoiceRecognize);
//        rvSpeechRecognize = (RecyclerView) rootView.findViewById(R.id.rvSpeechRecognize);
//        rvSpeechRecognize.setLayoutManager(new LinearLayoutManager(getContext()));
//        tvFirst = (TextView) rootView.findViewById(R.id.tvFirst);
//        tvSecond = (TextView) rootView.findViewById(R.id.tvSecond);
//        tvThird = (TextView) rootView.findViewById(R.id.tvThird);
//        tvFourth = (TextView) rootView.findViewById(R.id.tvFourth);
//        tvFifth = (TextView) rootView.findViewById(R.id.tvFifth);
        recognizer = new PASpeechRecognizer(getContext());
        recognizer.setSpeechListener(new SpeechListener() {
            @Override
            public void onSpeechEnd(List<String> speechResult) {
//                String speech = speechResult.get(0);
//                String[] splitted = speech.split("\\s");
//                List<String> result = Arrays.asList(splitted);
                processSpeechResults(speechResult);
            }
        });
        recognizer.setListeningOfSpeechListener(new ListeningOfSpeechListener() {
            @Override
            public void onListening(boolean listen) {
                if (listen)
                    tvListeningIndicator.setText("I\'m listening to you...");
                else
                    tvListeningIndicator.setText("I\'m not listening to you...");
            }
        });
//        btnVoiceRecognize.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
//                    if (!started) {
//                        started = true;
//                        recognizer.startVoiceRecognitionCycle();
//                    }
//                }
//                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    recognizer.stopVoiceRecognition();
//                    started = false;
//                }
//                return false;
//            }
//        });
//        refreshList();
        return rootView;
    }

    private void processSpeechResults(List<String> speechResult) {
//        if (record == null)
//            record = new FinanceRecord();
//        List<RootCategory> categories = daoSession.getRootCategoryDao().loadAll();
//        for (String word : speechResult) {
//            for (String def : definitionArrays) {
//                if (word.toLowerCase().equals(def)) {
//                    isDefinitionSet = true;
//
//                    break;
//                }
//            }
//            for (RootCategory category : categories) {
//                if (category.getName().toLowerCase().equals(word.toLowerCase())) {
//                    record.setCategory(category);
//                    break;
//                }
//                for (SubCategory subCategory : category.getSubCategories()) {
//                    if (subCategory.getName().toLowerCase().equals(word.toLowerCase())) {
//                        record.setCategory(category);
//                        record.setCategory(category);
//                        break;
//                    }
//                }
//            }
//        }
        if (speechResult != null && !speechResult.isEmpty())
            tvSpeechModeEnteredText.setText(speechResult.get(0));
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
            view.tvSpeechRecognizeFinanceRecordAmount.setText(""+result.get(position).getAmount());
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



}
