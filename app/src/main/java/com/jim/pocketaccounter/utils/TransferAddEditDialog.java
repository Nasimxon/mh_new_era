package com.jim.pocketaccounter.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.AccountOperation;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.PurposeDao;
import com.jim.pocketaccounter.fragments.AccountEditFragment;
import com.jim.pocketaccounter.fragments.AccountFragment;
import com.jim.pocketaccounter.managers.LogicManagerConstants;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by root on 10/7/16.
 */

public class TransferAddEditDialog extends Dialog {
    private View dialogView;
    private RecyclerView recyclerView;
    private ImageView ivClose;
    @Inject
    DaoSession daoSession;

    public TransferAddEditDialog(Context context) {
        super(context);
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        dialogView = getLayoutInflater().inflate(R.layout.transfer_add_edit_dialog, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogView);
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        ivClose = (ImageView) dialogView.findViewById(R.id.ivClose);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        recyclerView = (RecyclerView) dialogView.findViewById(R.id.rvTransferAddEdit);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshList();
    }

    private void refreshList() {
        TransferAddEditDialogAdapter transferAddEditDialogAdapter = new TransferAddEditDialogAdapter(daoSession.getAccountOperationDao().loadAll());
        recyclerView.setAdapter(transferAddEditDialogAdapter);
    }

    private String getAccountOrPurposeNameById(String id) {
        if (!daoSession.getAccountDao().queryBuilder().where(AccountDao.Properties.Id.eq(id)).list().isEmpty()) {
            return daoSession.getAccountDao().queryBuilder().where(AccountDao.Properties.Id.eq(id)).list().get(0).getName();
        } else if (!daoSession.getPurposeDao().queryBuilder().where(PurposeDao.Properties.Id.eq(id)).list().isEmpty()) {
            return daoSession.getPurposeDao().queryBuilder().where(PurposeDao.Properties.Id.eq(id)).list().get(0).getDescription();
        } else return null;
    }

    private class TransferAddEditDialogAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<AccountOperation> result;
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

        public TransferAddEditDialogAdapter(List<AccountOperation> result) {
            this.result = result;
        }

        public int getItemCount() {
            return result.size();
        }

        private void showOperationsList(View v, final int position) {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.inflate(R.menu.toolbar_popup);
            MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popupMenu.getMenu(), v);
            menuHelper.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.delete: {
                            final WarningDialog warningDialog = new WarningDialog(getContext());
                            warningDialog.setText(getContext().getResources().getString(R.string.do_you_want_to_delete));
                            warningDialog.setOnYesButtonListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    daoSession.getAccountOperationDao().delete(result.get(0));
                                    refreshList();
                                    warningDialog.dismiss();
                                }
                            });
                            warningDialog.setOnNoButtonClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    warningDialog.dismiss();
                                }
                            });
                            warningDialog.show();
                            break;
                        }
                        case R.id.edit: {
                            final TransferDialog transferDialog = new TransferDialog(getContext());
                            transferDialog.setEditAccountPurpose(result.get(position));
                            transferDialog.setOnTransferDialogSaveListener(new TransferDialog.OnTransferDialogSaveListener() {
                                @Override
                                public void OnTransferDialogSave() {
                                    refreshList();
                                }
                            });
                            transferDialog.show();
                            break;
                        }
                    }
                    return false;
                }
            });
            popupMenu.show();
        }

        public void onBindViewHolder(final ViewHolder view, final int position) {
            view.ivItemTransferAccountEditDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOperationsList(v, position);
                }
            });
            String fromName = getAccountOrPurposeNameById(result.get(position).getSourceId());
            if (fromName != null)
                view.tvItemAccountFromName.setText(fromName);
            DecimalFormat dateFormat = new DecimalFormat("0.00");
            String toName = getAccountOrPurposeNameById(result.get(position).getTargetId());
            if (toName != null)
                view.tvItemAccountToName.setText(toName);
            view.tvItemAccountAmount.setText(dateFormat.format(result.get(position).getAmount()) + result.get(position).getCurrency().getAbbr());
            view.tvItemAccountDate.setText(simpleDateFormat.format(result.get(position).getDate().getTime()));
            if (daoSession.getAccountDao().load(result.get(position).getSourceId()) != null) {
                view.ivItemTransferAccountFrom.setImageResource(getContext().getResources().getIdentifier(
                        daoSession.getAccountDao().load(result.get(position).getSourceId()).getIcon(),
                        "drawable", getContext().getPackageName()));
            } else {
                view.ivItemTransferAccountFrom.setImageResource(getContext().getResources().getIdentifier(
                        daoSession.getPurposeDao().load(result.get(position).getSourceId()).getIcon(),
                        "drawable", getContext().getPackageName()));
            }
            if (daoSession.getAccountDao().load(result.get(position).getTargetId()) != null) {
                view.ivItemTransferAccountTo.setImageResource(getContext().getResources().getIdentifier(
                        daoSession.getAccountDao().load(result.get(position).getTargetId()).getIcon(),
                        "drawable", getContext().getPackageName()));
            } else {
                view.ivItemTransferAccountTo.setImageResource(getContext().getResources().getIdentifier(
                        daoSession.getPurposeDao().load(result.get(position).getTargetId()).getIcon(),
                        "drawable", getContext().getPackageName()));
            }
            if (position == result.size())
                view.ivItemTransferAccountEditBottomLine.setVisibility(View.GONE);
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer_account_modern, parent, false);
            return new ViewHolder(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemTransferAccountEditDelete;
        ImageView ivItemTransferAccountFrom;
        ImageView ivItemTransferAccountTo;
        ImageView ivItemTransferAccountEditBottomLine;
        TextView tvItemAccountFromName;
        TextView tvItemAccountToName;
        TextView tvItemAccountAmount;
        TextView tvItemAccountDate;

        public ViewHolder(View view) {
            super(view);
            ivItemTransferAccountFrom = (ImageView) view.findViewById(R.id.ivItemTransferAccountFrom);
            ivItemTransferAccountTo = (ImageView) view.findViewById(R.id.ivItemAccountTo);
            tvItemAccountFromName = (TextView) view.findViewById(R.id.tvItemAccountFromName);
            tvItemAccountToName = (TextView) view.findViewById(R.id.tvItemAccountToName);
            tvItemAccountAmount = (TextView) view.findViewById(R.id.tvItemAccountAmount);
            tvItemAccountDate = (TextView) view.findViewById(R.id.tvItemAccountDate);
            ivItemTransferAccountEditDelete = (ImageView) view.findViewById(R.id.ivItemTransferAccountEditDelete);
            ivItemTransferAccountEditBottomLine = (ImageView) view.findViewById(R.id.ivItemTransferAccountEditBottomLine);
        }
    }

}
