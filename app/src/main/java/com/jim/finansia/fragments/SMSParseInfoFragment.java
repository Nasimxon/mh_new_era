package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.SmsParseObject;
import com.jim.finansia.database.SmsParseSuccess;
import com.jim.finansia.database.SmsParseSuccessDao;
import com.jim.finansia.managers.CommonOperations;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.database.TemplateSms;
import com.jim.finansia.utils.WarningDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
@SuppressLint({"InflateParams", "ValidFragment"})
public class SMSParseInfoFragment extends Fragment {
    @Inject
    DaoSession daoSession;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    LogicManager logicManager;
    @Inject
    CommonOperations commonOperations;
    WarningDialog warningDialog;

    private SmsParseObject object;
    private RecyclerView recyclerView;
    private TextView ifListEmpty;

    public SMSParseInfoFragment(SmsParseObject object) {
        this.object = object;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        View rootView = inflater.inflate(R.layout.sms_parse_info, container, false);
        warningDialog = new WarningDialog(getContext());

        ifListEmpty = (TextView) rootView.findViewById(R.id.ifListEmpty);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvSmsParseInfo);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        return rootView;
    }
    public void refresh() {
        if (recyclerView != null) {
            MyAdapter myAdapter = new MyAdapter();
            recyclerView.setAdapter(myAdapter);
        }
    }
    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            toolbarManager.setImageToSecondImage(R.drawable.ic_delete_proz);
            toolbarManager.setSubtitle("");
            toolbarManager.setOnTitleClickListener(null);
            toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            logicManager.deleteSmsParseObject(object);
                            paFragmentManager.getFragmentManager().popBackStack();
                            paFragmentManager.displayFragment(new SmsParseMainFragment());
                            warningDialog.dismiss();
                        }
                    });
                    warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            warningDialog.dismiss();
                        }
                    });
                    warningDialog.setText(getString(R.string.delete_all_records));
                    int width = getResources().getDisplayMetrics().widthPixels;
                    warningDialog.getWindow().setLayout(8*width/10, ViewGroup.LayoutParams.WRAP_CONTENT);
                    warningDialog.show();
                }
            });
        }
    }
    private class MyAdapter extends RecyclerView.Adapter<SMSParseInfoFragment.ViewHolder> implements View.OnClickListener {
        private List<SmsParseSuccess> successList;
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MM:yyyy");
        private List<String> strings;
        private List<TextView> tvList;
        private List<String> incomeKeys;
        private List<String> expanceKeys;
        private List<String> amountKeys;
        private List<String> amountKeyOld;
        private List<TemplateSms> templateSmsList;
        private int posIncExp = -1;
        private int posAmount = -1;

        int txSize = (int) ((int) getResources().getDimension(R.dimen.fourteen_dp)/getResources().getDisplayMetrics().density);

        public MyAdapter() {
            successList = daoSession.getSmsParseSuccessDao().queryBuilder().
                    where(SmsParseSuccessDao.Properties.SmsParseObjectId.eq(object.getId())).list();

            if (successList.isEmpty()) {
                ifListEmpty.setVisibility(View.VISIBLE);
            }
            else {
                ifListEmpty.setVisibility(View.GONE);
            }
            incomeKeys = new ArrayList<>();
            expanceKeys = new ArrayList<>();
            amountKeys = new ArrayList<>();
            amountKeyOld = new ArrayList<>();
        }

        public int getItemCount() {
            return successList.size();
        }

        public void onBindViewHolder(final SMSParseInfoFragment.ViewHolder view, final int position) {
            view.tvDate.setText(simpleDateFormat.format(successList.get(position).getDate().getTime()));
            view.smsBody.setText(successList.get(position).getBody());
            view.deleteImage.setOnClickListener(new View.OnClickListener() {
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
                            logicManager.deleteSmsParseSuccess(successList.get(position));
                            notifyDataSetChanged();
                            paFragmentManager.getFragmentManager().popBackStack();
                            paFragmentManager.displayFragment(new SmsParseMainFragment());
                            warningDialog.dismiss();
                        }
                    });
                    warningDialog.setText(getResources().getString(R.string.delete));
                    warningDialog.show();
                }
            });
            if (successList.get(position).getIsSuccess()) {
                view.linearLayout.setVisibility(View.GONE);
                view.tvAmount.setText("" + successList.get(position).getAmount()
                        + successList.get(position).getCurrency().getAbbr());
                if (successList.get(position).getType() == PocketAccounterGeneral.EXPENSE)
                    view.tvAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                else
                    view.tvAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
                view.tvType.setText(successList.get(position).getType() ==
                        PocketAccounterGeneral.INCOME ? getResources().getString(R.string.income) : getResources().getString(R.string.expanse));
            } else {
                view.linearLayout.setVisibility(View.VISIBLE);
                view.tvAmount.setText(R.string.no_success);
                view.tvAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                view.tvType.setText(R.string.no_parsing);

                view.thisExpance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogSms(false, position);
                    }
                });
                view.thisIncome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogSms(true, position);
                    }
                });
            }
        }

        private List<String> smsBodyParse(String body) {
            String anyWordWithoutNumber = "([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`])";
            String anyNumber = "([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`]*)([0-9]+[.,]?[0-9]*)([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`]*)";
            String numberWordNumberWord = "([0-9]+[.,]?[0-9]*)([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`]*)([0-9]+[.,]?[0-9]*)([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`]*)";
            String wordNumberWordNumber = "([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`]*)([0-9]+[.,]?[0-9]*)([a-zA-Z/^*~&%@!+()$#-\\/'\"\\{`]*)([0-9]+[.,]?[0-9]*)";
            String[] strings = body.split(" ");
            List<String> temp = Arrays.asList(strings);
            for (String s : temp) s.replace("\n", "");
            List<String> words = new ArrayList<>();
            for (int i = temp.size() - 1; i >= 0; i--) {
                Pattern pattern = Pattern.compile(anyWordWithoutNumber);
                Matcher matcher = pattern.matcher(temp.get(i));
                if (matcher.matches()) {
                    words.add(matcher.group(1));
                    continue;
                }

                pattern = Pattern.compile(anyNumber);
                matcher = pattern.matcher(temp.get(i));
                if (matcher.matches()) {
                    words.add(matcher.group(3));
                    words.add(matcher.group(2));
                    words.add(matcher.group(1));
                    continue;
                }
                pattern = Pattern.compile(numberWordNumberWord);
                matcher = pattern.matcher(temp.get(i));
                if (matcher.matches()) {
                    words.add(matcher.group(4));
                    words.add(matcher.group(3));
                    words.add(matcher.group(2));
                    words.add(matcher.group(1));
                    continue;
                }
                pattern = Pattern.compile(wordNumberWordNumber);
                matcher = pattern.matcher(temp.get(i));
                if (matcher.matches()) {
                    words.add(matcher.group(4));
                    words.add(matcher.group(3));
                    words.add(matcher.group(2));
                    words.add(matcher.group(1));
                    continue;
                }
                words.add(temp.get(i));
            }
            Collections.reverse(words);
            return words;
        }

        TextView amountkey;
        TextView parsingkey;
        private void dialogSms(final boolean type, final int position) {
            posIncExp = -1;
            posAmount = -1;
            final Dialog dialog = new Dialog(getActivity());
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_parsin_sms_select_word, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView);
            final ImageView close = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
            final ImageView save = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);
            final LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.llDialogSmsParseAdd);
            final TextView tvSmsDialogTypeTitle = (TextView) dialogView.findViewById(R.id.tvSmsDialogTypeTitle);
            if (type) {
                tvSmsDialogTypeTitle.setText(getResources().getString(R.string.income_decide_with_static_word));
            }
            else {
                tvSmsDialogTypeTitle.setText(getResources().getString(R.string.expense_decide_with_static_word));
            }
            amountkey = (TextView) dialogView.findViewById(R.id.amountKey);
            parsingkey = (TextView) dialogView.findViewById(R.id.parsingKey);
            int eni = (int) ((8 * getResources().getDisplayMetrics().widthPixels / 10
                    - 2 * commonOperations.convertDpToPixel(24)) / getResources().getDisplayMetrics().density);

            strings = smsBodyParse(successList.get(position).getBody());
            tvList = new ArrayList<>();

            Map<Integer, List<String>> map = new TreeMap<>();

            for (int i = strings.size() - 1; i >= 0; i--) {
                if (strings.get(i).isEmpty()) {
                    strings.remove(i);
                } else
                    strings.set(i, strings.get(i) + " ");
            }

            List<String> tempList = new ArrayList<>();
            int length;
            int row = 1;

            for (int i = 0; i < strings.size(); i++) {
                List<String> temp = new ArrayList<>();
                temp.addAll(tempList);
                temp.add(strings.get(i));
                length = measureListText(temp);
                if (eni > length) {
                    tempList.add(strings.get(i));
                } else {
                    map.put(row++, tempList);
                    tempList = new ArrayList<>();
                    tempList.add(strings.get(i));
                }
                if (i == strings.size() - 1 && !tempList.isEmpty()) {
                    map.put(row++, tempList);
                }
            }
            row = 0;
            for (Integer integer : map.keySet()) {
                List<String> lt = map.get(integer);
                LinearLayout linearLayout1 = new LinearLayout(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout1.setLayoutParams(layoutParams);
                linearLayout.addView(linearLayout1);
                for (int i = 0; i < lt.size(); i++) {
                    TextView textView = new TextView(getContext());
                    textView.setTag(row++);
                    textView.setTextSize(txSize);
                    textView.setText(lt.get(i));
                    tvList.add(textView);
                    textView.setBackgroundResource(R.drawable.select_grey);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                            (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins((int) getResources().getDimension(R.dimen.five_dp), 0, 0, 0);
                    textView.setLayoutParams(lp);
                    textView.setOnClickListener(MyAdapter.this);
                    linearLayout1.addView(textView);
                }
            }
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (posAmount == -1) {
                        Toast.makeText(getContext(), R.string.choose_amount, Toast.LENGTH_SHORT).show();
                        return;
                    } else if (posIncExp == -1) {
                        Toast.makeText(getContext(),  (type ? "Choose income key" : "Choose expance key"), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        incomeKeys = incomeKeys == null ? new ArrayList<String>() : incomeKeys;
                        expanceKeys = expanceKeys == null ? new ArrayList<String>() : expanceKeys;
                        amountKeys = amountKeys == null ? new ArrayList<String>() : amountKeys;
                        templateSmsList = templateSmsList == null ? new ArrayList<TemplateSms>() : templateSmsList;
                        for (int i = 0; i < strings.size(); i++) {
                            strings.set(i, strings.get(i).trim());
                        }
                        if (type)
                            incomeKeys.add(strings.get(posIncExp));
                        else
                            expanceKeys.add(strings.get(posIncExp));
                        amountKeys.add(strings.get(posAmount));
                        boolean amountKeyDefined = false;
                        int amountKeyPos;
                        String dateRegex = "[0-9]+[.,|/^*~&%@!+()$#-\\/'\"\\{`\\];\\[:][0-9]+[.,|/^*~&%@!+()$#-\\/'\"\\{`\\];\\[:]?[0-9]*";
                        if (posAmount == 0) {
                            amountKeyPos = posAmount+1;
                            while (!amountKeyDefined) {
                                if (amountKeyPos >= strings.size()) break;
                                else {
                                    if (!strings.get(amountKeyPos).matches(dateRegex))
                                        amountKeyDefined = true;
                                    else {
                                        amountKeyPos++;
                                    }
                                }
                            }
                            if (!amountKeyDefined) {
                                amountKeyPos = posAmount + 1;
                            }
                        }
                        else if (posAmount > 0 && posAmount < strings.size()-1) {
                            amountKeyPos = posAmount-1;
                            boolean forward = false;
                            while (!amountKeyDefined) {
                                if (amountKeyPos < 0) {
                                    forward = true;
                                    amountKeyPos = posAmount+1;
                                }
                                else if (amountKeyPos >= strings.size()) break;
                                else if (!forward) {
                                    if (!strings.get(amountKeyPos).matches(dateRegex))
                                        amountKeyDefined = true;
                                    else
                                        amountKeyPos--;
                                } else if (forward) {
                                    if (!strings.get(amountKeyPos).matches(dateRegex))
                                        amountKeyDefined = true;
                                    else
                                        amountKeyPos++;
                                }
                            }
                            if (!amountKeyDefined) {
                                amountKeyPos = posAmount - 1;
                            }
                        }
                        else {
                            amountKeyPos = posAmount - 1;
                            while (!amountKeyDefined) {
                                if (amountKeyPos >= strings.size()) break;
                                else {
                                    if (!strings.get(amountKeyPos).matches(dateRegex))
                                        amountKeyDefined = true;
                                    else
                                        amountKeyPos++;
                                }
                            }
                            if (!amountKeyDefined) {
                                amountKeyPos = posAmount-1;
                            }
                        }
                        amountKeys.add(strings.get(amountKeyPos));
                        for (int i = 0; i < incomeKeys.size(); i++) {
                            if (incomeKeys.get(i) == null || incomeKeys.get(i).isEmpty()) {
                                incomeKeys.remove(i);
                                i--;
                            }
                        }
                        for (int i = 0; i < expanceKeys.size(); i++) {
                            if (expanceKeys.get(i) == null || expanceKeys.get(i).isEmpty()) {
                                expanceKeys.remove(i);
                                i--;
                            }
                        }
                        templateSmsList = commonOperations.generateSmsTemplateList(strings, posIncExp, posAmount, incomeKeys, expanceKeys, amountKeys);
                        if (templateSmsList != null) {
                            for (TemplateSms templateSms : templateSmsList)
                                templateSms.setParseObjectId(object.getId());
                        }
                        daoSession.getTemplateSmsDao().insertInTx(templateSmsList);
                        for (int i = 0; i < incomeKeys.size(); i++) {
                            if (incomeKeys.get(i) == null || incomeKeys.get(i).isEmpty()) {
                                incomeKeys.remove(i);
                                i--;
                            }
                        }
                        for (int i = successList.size() - 1; i >= 0; i--) {
                            for (TemplateSms templateSms : templateSmsList) {
                                if (!successList.get(i).getIsSuccess() && successList.get(i).getBody().matches(templateSms.getRegex())) {
                                    Pattern pattern = Pattern.compile(templateSms.getRegex());
                                    Matcher matcher = pattern.matcher(successList.get(i).getBody());
                                    matcher.matches();
                                    if (matcher.group(templateSms.getPosAmountGroup()) != null
                                            && !matcher.group(templateSms.getPosAmountGroup()).isEmpty()) {
                                        try {
                                            double summ = Double.parseDouble(matcher.group(templateSms.getPosAmountGroup()));
                                            successList.get(i).setAmount(summ);
                                            successList.get(i).setIsSuccess(true);
                                            successList.get(i).setType(templateSms.getType());
                                        } catch (Exception e) {
                                            try {
                                                if (matcher.group(templateSms.getPosAmountGroupSecond()) != null
                                                        && !matcher.group(templateSms.getPosAmountGroupSecond()).isEmpty()) {
                                                    double summ = Double.parseDouble(matcher.group(templateSms.getPosAmountGroupSecond()));
                                                    successList.get(i).setAmount(summ);
                                                    successList.get(i).setIsSuccess(true);
                                                    successList.get(i).setType(templateSms.getType());
                                                }
                                            } catch (Exception e1) {
                                                successList.get(i).setIsSuccess(false);
                                            }
                                        }
                                    } else if (matcher.group(templateSms.getPosAmountGroupSecond()) != null
                                            && !matcher.group(templateSms.getPosAmountGroupSecond()).isEmpty()) {
                                        try {
                                            double summ = Double.parseDouble(matcher.group(templateSms.getPosAmountGroupSecond()));
                                            successList.get(i).setAmount(summ);
                                            successList.get(i).setIsSuccess(true);
                                            successList.get(i).setType(templateSms.getType());
                                        } catch (Exception e1) {
                                            successList.get(i).setIsSuccess(false);
                                        }
                                    } else {
                                        successList.get(i).setIsSuccess(false);
                                    }
                                    daoSession.getSmsParseSuccessDao().insertOrReplace(successList.get(i));
                                    break;
                                }
                            }
                        }
                        try {
                            successList.get(position).setAmount(Double.parseDouble(strings.get(posAmount)));
                            successList.get(position).setIsSuccess(true);
                            daoSession.getSmsParseSuccessDao().insertOrReplace(successList.get(position));
                        } catch (Exception e) {}
                        notifyDataSetChanged();
                    }
                    dialog.dismiss();
                }
            });
            int width = getResources().getDisplayMetrics().widthPixels;
            dialog.getWindow().setLayout(8 * width / 10, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            dialog.show();
        }

        private int measureListText(List<String> list) {
            Paint paint = new Paint();
            Rect rect = new Rect();
            paint.setTextSize(txSize);
            int length = 0;
            for (String s : list) {
                paint.getTextBounds(s, 0, s.length(), rect);
                length += rect.width();
            }
            return length;
        }

        public SMSParseInfoFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_recived_item, parent, false);
            return new SMSParseInfoFragment.ViewHolder(view);
        }

        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                String regex = "([0-9]+[.,]?[0-9]*\\s*)+";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(strings.get((Integer) v.getTag()));
                if (!matcher.matches() && !strings.get((int) v.getTag()).matches("\\s?[0-9]+\\s?")) {
                    if (posIncExp != -1) {
                        parsingkey.setText(getResources().getString(R.string.select_word));
                        tvList.get(posIncExp).setBackgroundResource(R.drawable.select_grey);
                    }
                    posIncExp = (int) v.getTag();
                    v.setBackgroundResource(R.drawable.select_green);
                    parsingkey.setText(((TextView)v).getText().toString());
                } else {
                    if (posAmount != -1) {
                        amountkey.setText(getResources().getString(R.string.select_word));
                        tvList.get(posAmount).setBackgroundResource(R.drawable.select_grey);
                    }
                    posAmount = (int) v.getTag();
                    v.setBackgroundResource(R.drawable.select_yellow);
                    amountkey.setText(((TextView)v).getText().toString());
                }
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView smsBody;
        public TextView tvType;
        public TextView tvAmount;
        public TextView tvDate;
        public ImageView deleteImage;
        public LinearLayout linearLayout;
        public TextView thisIncome;
        public TextView thisExpance;

        public ViewHolder(View view) {
            super(view);
            smsBody = (TextView) view.findViewById(R.id.tvSmsParseRecieveBody);
            tvType = (TextView) view.findViewById(R.id.tvIncomeOrExpenseType);
            tvAmount = (TextView) view.findViewById(R.id.tvSmsParseRecieveAmount);
            tvDate = (TextView) view.findViewById(R.id.tvSmsParseSuccessDate);
            deleteImage = (ImageView) view.findViewById(R.id.imageView8);
            linearLayout = (LinearLayout) view.findViewById(R.id.llSmsRecive);
            thisIncome = (TextView) view.findViewById(R.id.tvAddSmsParseItemIncome);
            thisExpance = (TextView) view.findViewById(R.id.tvAddSmsParseItemExpance);
        }
    }
}
