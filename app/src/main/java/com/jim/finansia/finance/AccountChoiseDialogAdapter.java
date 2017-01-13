package com.jim.finansia.finance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.R;
import com.jim.finansia.database.Account;

import java.util.List;

@SuppressLint("ViewHolder")
public class AccountChoiseDialogAdapter extends RecyclerView.Adapter<AccountChoiseDialogAdapter.myViewHolder> {
	private List<Account> result;
	private Context context;
	private OnItemSelectListner onItemSelectListner;
	public interface OnItemSelectListner {
		 void onItemSelect(Account account);
	}
	public AccountChoiseDialogAdapter(List<Account> account, Context context, OnItemSelectListner onItemSelectListner) {
		result = account ;
		this.context = context;
		this.onItemSelectListner = onItemSelectListner;
			  }


	@Override
	public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.simple_recycler_item_photo_nam, parent, false);
		AccountChoiseDialogAdapter.myViewHolder vh = new AccountChoiseDialogAdapter.myViewHolder(v);
		return vh;
	}

	@Override
	public void onBindViewHolder(final myViewHolder holder, final int position) {
		final Account account = result.get(position);
		int resId = context.getResources().getIdentifier(account.getIcon(), "drawable", context.getPackageName());
		holder.imageResurs.setImageResource(resId);
		holder.textResurs.setText(account.getName());
		holder.mainView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onItemSelectListner.onItemSelect(account);
			}
		});
	}

		@Override
	public int getItemCount() {
		return result.size();
	}


	public static class myViewHolder extends RecyclerView.ViewHolder {
		TextView textResurs;
		ImageView imageResurs;
		LinearLayout mainView;
		public myViewHolder(View view) {
			super(view);
			imageResurs = (ImageView) view.findViewById(R.id.imageResurs);
			textResurs = (TextView) view.findViewById(R.id.textResurs);
			mainView = (LinearLayout) view.findViewById(R.id.mainView);

		}
	}
}
