package com.jim.pocketaccounter.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.BoardButtonDao;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.RootCategoryDao;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.report.FilterSelectable;
import com.jim.pocketaccounter.utils.FilterDialog;
import com.jim.pocketaccounter.utils.OnCheckedChangeListener;
import com.jim.pocketaccounter.utils.OperationsListDialog;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.SubcatItemChecker;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.catselector.OnItemSelectedListener;
import com.jim.pocketaccounter.utils.catselector.SelectorView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressLint({"InflateParams", "ValidFragment"})
public class CategoryInfoFragment extends PABaseInfoFragment {
	WarningDialog warningDialog;
    private RootCategory rootCategory;
	private RecyclerView rvCategoryInfoOperations, rvCatInfoSubcats;
	private TextView tvCategoryInfoTotal;
	private TextView tvCategoryInfoSubcategories;
	private SelectorView svCategorySelector;
	private boolean[] subcatChecked;
	private List<SubCategory> subCategories;
	@SuppressLint("ValidFragment")
	public CategoryInfoFragment(RootCategory rootCategory) {
		this.rootCategory = rootCategory;
	}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.category_info_layout, container, false);
		((PocketAccounter)getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
		subCategories = new ArrayList<>();
		SubCategory all = new SubCategory();
		all.setName(getString(R.string.all));
		all.setIcon("all");
		subCategories.add(all);
		subCategories.addAll(rootCategory.getSubCategories());
		subcatChecked = new boolean[subCategories.size()];
		for (int i = 0; i < subcatChecked.length; i++)
			subcatChecked[i] = (i == 0);
		warningDialog = new WarningDialog(getContext());
		toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
		toolbarManager.setTitle(getResources().getString(R.string.category));
		toolbarManager.setSubtitle(rootCategory.getName());
		toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				paFragmentManager.getFragmentManager().popBackStack();
				paFragmentManager.displayFragment(new CategoryFragment());
			}
		});
		toolbarManager.setOnSecondImageClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showOperationsList();
			}
		});
		rvCategoryInfoOperations = (RecyclerView) rootView.findViewById(R.id.rvAccountDetailsInfo);
		svCategorySelector = (SelectorView) rootView.findViewById(R.id.svCategorySelector);
		final List<RootCategory> rootCategories = daoSession.getRootCategoryDao().queryBuilder().orderAsc(RootCategoryDao.Properties.Name).list();
		CategorySelectorAdapter adapter = new CategorySelectorAdapter(rootCategories);
		int selectedPos = 0;
		for (int i = 0; i < rootCategories.size(); i++) {
			if (rootCategories.get(i).getId().equals(rootCategory.getId())) {
				selectedPos = i;
				break;
			}
		}
		svCategorySelector.setAdapter(adapter);
		svCategorySelector.setSelection(selectedPos);
		svCategorySelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(int selectedItemPosition) {
				rootCategory = rootCategories.get(selectedItemPosition);
				toolbarManager.setSubtitle(rootCategory.getName());
				subCategories = new ArrayList<>();
				SubCategory all = new SubCategory();
				all.setName(getString(R.string.all));
				all.setIcon("all");
				subCategories.add(all);
				subCategories.addAll(rootCategory.getSubCategories());
				subcatChecked = new boolean[subCategories.size()];
				for (int i = 0; i < subcatChecked.length; i++)
					subcatChecked[i] = (i == 0);
				refreshSubcatItems();
				refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
			}
		});
		rvCatInfoSubcats = (RecyclerView) rootView.findViewById(R.id.rvCatInfoSubcats);
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		rvCatInfoSubcats.setLayoutManager(layoutManager);
		refreshSubcatItems();

		rvCategoryInfoOperations = (RecyclerView) rootView.findViewById(R.id.rvCategoryInfoOperations);
		rvCategoryInfoOperations.setLayoutManager(new LinearLayoutManager(getContext()));
		tvCategoryInfoTotal = (TextView) rootView.findViewById(R.id.tvCategoryInfoTotal);
		refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
		return rootView;
	}
	private void refreshSubcatItems() {
		SubcatAdapter subcatAdapter = new SubcatAdapter(subCategories);
		rvCatInfoSubcats.setAdapter(subcatAdapter);
	}
	private void showOperationsList() {
		String[] ops = new String[2];
		ops[0] = getResources().getString(R.string.to_edit);
		ops[1] = getResources().getString(R.string.delete);
		final OperationsListDialog operationsListDialog = new OperationsListDialog(getContext());
		operationsListDialog.setAdapter(ops);
		operationsListDialog.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch(position) {
					case 0:
						paFragmentManager.getFragmentManager().popBackStack();
						paFragmentManager.displayFragment(new RootCategoryEditFragment(rootCategory, PocketAccounterGeneral.NO_MODE, 0, null));
						break;
					case 1:
						warningDialog.setText(getResources().getString(R.string.category_delete_warning));
						warningDialog.setOnNoButtonClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								warningDialog.dismiss();
							}
						});
						warningDialog.setOnYesButtonListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								List<BoardButton> list = daoSession.getBoardButtonDao().queryBuilder().where(BoardButtonDao.Properties.CategoryId.eq(rootCategory.getId())).list();
								if (!list.isEmpty()) {
									int currentPage = 0, countOfButtons = 0;
									if (rootCategory.getType() == PocketAccounterGeneral.INCOME) {
										currentPage = preferences.getInt("income_current_page", 1);
										countOfButtons = 4;
									}
									else {
										currentPage = preferences.getInt("expense_current_page", 1);
										countOfButtons = 16;
									}
									for (BoardButton boardButton : list) {
										if (currentPage*countOfButtons <= boardButton.getPos()
												&& (currentPage+1)*countOfButtons > currentPage*countOfButtons) {
											BitmapFactory.Options options = new BitmapFactory.Options();
											options.inPreferredConfig = Bitmap.Config.RGB_565;
											Bitmap scaled = BitmapFactory.decodeResource(getResources(), R.drawable.no_category, options);
											scaled = Bitmap.createScaledBitmap(scaled, (int) getResources().getDimension(R.dimen.thirty_dp),
													(int) getResources().getDimension(R.dimen.thirty_dp), false);
											dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
										}
									}
								}
								logicManager.deleteRootCategory(rootCategory);
								dataCache.updateAllPercents();
								paFragmentManager.getFragmentManager().popBackStack();
								paFragmentManager.displayFragment(new CategoryFragment());
								warningDialog.dismiss();
							}
						});
						warningDialog.show();
						break;
				}
				operationsListDialog.dismiss();
			}
		});
		operationsListDialog.show();
	}

	private void refreshOperationsList(Calendar begin, Calendar end) {
		List<FinanceRecord> objects = reportManager.getCategoryOperations(rootCategory, begin, end);
		if (!subcatChecked[0]) {
			List<SubCategory> selected = new ArrayList<>();
			for (int i = 0; i < subcatChecked.length; i++) {
				if (subcatChecked[i]) {
					selected.add(subCategories.get(i));
				}
			}
			for (int i = 0; i < objects.size(); i++) {
				if (objects.get(i).getSubCategory() == null) continue;
				boolean notSelected = true;
				for (SubCategory subCategory : selected) {
					if (subCategory.getId().equals(objects.get(i).getSubCategoryId())) {
						notSelected = false;
						break;
					}
				}
				if (notSelected) {
					objects.remove(i);
					i--;
				}
			}
		}
		CategoryOperationsAdapter accountOperationsAdapter = new CategoryOperationsAdapter(objects);;
		rvCategoryInfoOperations.setAdapter(accountOperationsAdapter);
		DecimalFormat format = new DecimalFormat("0.##");
		double total = 0.0d;
		for (FinanceRecord record : objects) {
			if (record.getCategory().getType() == PocketAccounterGeneral.INCOME)
				total += commonOperations.getCost(record);
			else
				total -= commonOperations.getCost(record);
		}
		tvCategoryInfoTotal.setText(getResources().getString(R.string.total)+" "+format.format(total) + commonOperations.getMainCurrency().getAbbr());
	}

	@Override
	void refreshList() {}

	private class CategoryOperationsAdapter extends RecyclerView.Adapter<CategoryInfoFragment.ViewHolder> {
		private List<FinanceRecord> result;
		public CategoryOperationsAdapter(List<FinanceRecord> result) {
			this.result = result;
		}
		public int getItemCount() {
			return result.size();
		}
		public void onBindViewHolder(final CategoryInfoFragment.ViewHolder view, final int position) {
			view.tvAccountInfoDate.setText(dateFormat.format(result.get(position).getDate().getTime()));
			String text = result.get(position).getCategory().getName();
			if (result.get(position).getSubCategory() != null)
				text += ", " + result.get(position).getSubCategory().getName();
			view.tvAccountInfoName.setText(text);
			String amount = "";
			if (result.get(position).getCategory().getType() == PocketAccounterGeneral.INCOME) {
				amount += "+"+result.get(position).getAmount() + result.get(position).getCurrency().getAbbr();
				view.tvAccountInfoAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.green_just));
			}
			else {
				amount += "-"+result.get(position).getAmount() + result.get(position).getCurrency().getAbbr();
				view.tvAccountInfoAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
			}
			view.tvAccountInfoAmount.setText(amount);
		}

		public CategoryInfoFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_info_operations, parent, false);
			return new CategoryInfoFragment.ViewHolder(view);
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		TextView tvAccountInfoDate;
		TextView tvAccountInfoName;
		TextView tvAccountInfoAmount;
		public ViewHolder(View view) {
			super(view);
			tvAccountInfoDate = (TextView) view.findViewById(R.id.tvAccountInfoDate);
			tvAccountInfoName = (TextView) view.findViewById(R.id.tvAccountInfoName);
			tvAccountInfoAmount = (TextView) view.findViewById(R.id.tvAccountInfoAmount);
		}
	}

	private class SubcatAdapter extends RecyclerView.Adapter<CategoryInfoFragment.SubcatViewHolder> {
		private List<SubCategory> result;
		public SubcatAdapter(List<SubCategory> result) {
			this.result = result;
		}
		public int getItemCount() {
			return result.size();
		}
		public void onBindViewHolder(final CategoryInfoFragment.SubcatViewHolder view, final int position) {
			if (position == 0) {
				view.sichCatInfo.setSubCategory(result.get(position));
				view.sichCatInfo.setChecked(subcatChecked[position]);
				view.sichCatInfo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChange(boolean isChecked) {
						for (int i = 0; i < subcatChecked.length; i++) {
							View view = rvCatInfoSubcats.getChildAt(i);
							if (view != null)
								((SubcatItemChecker) view.findViewById(R.id.sichCatInfo)).setChecked(false);
							subcatChecked[i] = false;
						}
						subcatChecked[position] = true;
						View view = rvCatInfoSubcats.getChildAt(position);
						if (view != null)
							((SubcatItemChecker) view.findViewById(R.id.sichCatInfo)).setChecked(true);
						rvCatInfoSubcats.getAdapter().notifyDataSetChanged();
						refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
					}
				});
			} else {
				view.sichCatInfo.setSubCategory(result.get(position));
				view.sichCatInfo.setChecked(subcatChecked[position]);
				view.sichCatInfo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChange(boolean isChecked) {
						if (isChecked) {
							subcatChecked[0] = false;
						} else {
							int count = 0;
							for (int i = 0; i < subcatChecked.length; i++) {
								if (subcatChecked[i]) count++;
							}
							if (count <= 1)
								subcatChecked[0] = true;
						}
						subcatChecked[position] = isChecked;
						rvCatInfoSubcats.getAdapter().notifyDataSetChanged();
						refreshOperationsList(commonOperations.getFirstDay(), Calendar.getInstance());
					}
				});
			}
		}

		public CategoryInfoFragment.SubcatViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catinfo_subcat_list_item, parent, false);
			return new CategoryInfoFragment.SubcatViewHolder(view);
		}
	}

	public class SubcatViewHolder extends RecyclerView.ViewHolder {
		SubcatItemChecker sichCatInfo;
		public SubcatViewHolder(View view) {
			super(view);
			sichCatInfo = (SubcatItemChecker) view.findViewById(R.id.sichCatInfo);
		}
	}

	class CategorySelectorAdapter extends SelectorView.SelectorAdapter<RootCategory> {
		public CategorySelectorAdapter(List<RootCategory> list) {
			this.list = list;
		}
		@Override
		protected ViewGroup getInstantiatedView(int position, ViewGroup parent) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_selector_item, parent, false);
			ImageView ivCatSelectorItem = (ImageView) view.findViewById(R.id.ivCatSelectorItem);
			int resId = getResources().getIdentifier(list.get(position).getIcon(), "drawable", getContext().getPackageName());
			ivCatSelectorItem.setImageResource(resId);
			TextView tvCatSelectorItem = (TextView) view.findViewById(R.id.tvCatSelectorItem);
			tvCatSelectorItem.setText(list.get(position).getName());
			parent.addView(view);
			return parent;
		}
		@Override
		protected int getCount() {
			return list.size();
		}

	}

}
