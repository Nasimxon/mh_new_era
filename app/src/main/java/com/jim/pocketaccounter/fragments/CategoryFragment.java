package com.jim.pocketaccounter.fragments;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.database.RootCategoryDao;
import com.jim.pocketaccounter.database.SubCategory;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.utils.FABIcon;
import com.jim.pocketaccounter.utils.OnSubcategorySavingListener;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.StyleSetter;
import com.jim.pocketaccounter.utils.Styleable;
import com.jim.pocketaccounter.utils.SubCatAddEditDialog;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends PABaseListFragment implements OnClickListener, OnCheckedChangeListener {
	private RecyclerView rvCategories;
	private CheckBox chbCatIncomes, chbCatExpanses;
	@Styleable(colorLayer = PocketAccounterGeneral.HEAD_COLOR)
	private FABIcon fabCategoryAdd;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.category_layout, container, false);
		rootView.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(PocketAccounter.keyboardVisible){
					InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);}
			}
		},100);
		toolbarManager.setTitle(getResources().getString(R.string.category));
		toolbarManager.setSubtitle("");
		toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.GONE);
		fabCategoryAdd = (FABIcon) rootView.findViewById(R.id.fabAccountAdd);
		fabCategoryAdd.setOnClickListener(this);
		rvCategories = (RecyclerView) rootView.findViewById(R.id.rvCategories);
		rvCategories.addItemDecoration(new MarginDecoration((int) getResources().getDimension(R.dimen.ten_dp)));
		rvCategories.setHasFixedSize(true);
		rvCategories.setLayoutManager(new GridLayoutManager(getContext(), 2));
		rvCategories.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				try {
					onScrolledList(dy > 0);
				} catch (NullPointerException e) {
				}
			}
		});
		chbCatIncomes = (CheckBox) rootView.findViewById(R.id.chbCatIncomes);
		chbCatIncomes.setOnCheckedChangeListener(this);
		chbCatExpanses = (CheckBox) rootView.findViewById(R.id.chbCatExpanses);
		chbCatExpanses.setOnCheckedChangeListener(this);
		refreshList();
		new StyleSetter(this, preferences).set();
		return rootView;
	}

	private boolean show = false;
	public void onScrolledList(boolean k) {
		if (k) {
			if (!show)
				fabCategoryAdd.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_down));
			show = true;
		} else {
			if (show)
				fabCategoryAdd.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_up));
			show = false;
		}
	}
	@Override
	void refreshList() {
		ArrayList<RootCategory> categories = new ArrayList<RootCategory>();
		List<RootCategory> rootCategories = daoSession.getRootCategoryDao().queryBuilder().orderAsc(RootCategoryDao.Properties.Name).list();
		for (RootCategory rootCategory : rootCategories) {
			if (chbCatIncomes.isChecked()) {
				if (rootCategory.getType() == PocketAccounterGeneral.INCOME)
					categories.add(rootCategory);
			}
			if(chbCatExpanses.isChecked()) {
				if (rootCategory.getType() == PocketAccounterGeneral.EXPENSE)
					categories.add(rootCategory);
			}
		}
		CategoryAdapter adapter = new CategoryAdapter(categories);
		rvCategories.setAdapter(adapter);
		getClass().getDeclaredMethods();
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.fabAccountAdd:
				paFragmentManager.displayFragment(new RootCategoryEditFragment(null, PocketAccounterGeneral.NO_MODE, 0, null));
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		refreshList();
	}

	private class CategoryAdapter extends RecyclerView.Adapter<CategoryFragment.ViewHolder> {
		private List<RootCategory> result;
		public CategoryAdapter(List<RootCategory> result) {
			this.result = result;
		}
		public int getItemCount() {
			return result.size();
		}
		public void onBindViewHolder(final CategoryFragment.ViewHolder view, final int position) {
			view.tvCategoryListItemName.setText(result.get(position).getName());
			final int resId = getResources().getIdentifier(result.get(position).getIcon(),"drawable", getContext().getPackageName());
			view.ivCategoryItem.setImageResource(resId);
			view.llCategoryListItemAdd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final SubCatAddEditDialog subCatAddEditDialog = new SubCatAddEditDialog(getContext());
					subCatAddEditDialog.setRootCategory(result.get(position).getId());
					subCatAddEditDialog.setSubCat(null, new OnSubcategorySavingListener() {
						@Override
						public void onSubcategorySaving(SubCategory subCategory) {
							if (result.get(position).getSubCategories() != null)
								result.get(position).getSubCategories().add(subCategory);
							else {
								List<SubCategory> subCategoryList = new ArrayList<>();
								subCategoryList.add(subCategory);
								result.get(position).setSubCategories(subCategoryList);
							}
							List<SubCategory> subCategories = new ArrayList<>();
							subCategories.add(subCategory);

							if (logicManager.insertSubCategory(subCategories) == LogicManagerConstants.SUCH_NAME_ALREADY_EXISTS)
								Toast.makeText(getContext(), R.string.such_subcat_exist,
										Toast.LENGTH_SHORT).show();
							subCatAddEditDialog.dismiss();
						}
					});
					subCatAddEditDialog.show();
				}
			});
			view.llCategoryListItemEdit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dataCache.getCategoryEditFragmentDatas().setMode(PocketAccounterGeneral.NO_MODE);
					paFragmentManager.displayFragment(new RootCategoryEditFragment(result.get(position), PocketAccounterGeneral.NO_MODE, 0, null));
				}
			});
			view.ivCategoryInfo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					paFragmentManager.displayFragment(new CategoryInfoFragment(result.get(position)));
				}
			});
		}
		public CategoryFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item, parent, false);
			return new CategoryFragment.ViewHolder(view);
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		ImageView ivCategoryInfo;
		ImageView ivCategoryItem;
		TextView tvCategoryListItemName;
		LinearLayout llCategoryListItemAdd, llCategoryListItemEdit;
		public ViewHolder(View view) {
			super(view);
			ivCategoryInfo = (ImageView) view.findViewById(R.id.ivCategoryInfo);
			ivCategoryItem = (ImageView) view.findViewById(R.id.ivCategoryItem);
			tvCategoryListItemName = (TextView) view.findViewById(R.id.tvCategoryListItemName);
			llCategoryListItemAdd = (LinearLayout) view.findViewById(R.id.llCategoryListItemAdd);
			llCategoryListItemEdit = (LinearLayout) view.findViewById(R.id.llCategoryListItemEdit);
		}
	}

	class MarginDecoration extends RecyclerView.ItemDecoration {
		private int space;
		public MarginDecoration(int space) {
			this.space = space;
		}
		@Override
		public void getItemOffsets(Rect outRect, View view,
								   RecyclerView parent, RecyclerView.State state) {
			int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
			if (itemPosition % 2 != 0) {
				outRect.left = space/2;
				outRect.right = space;
			} else {
				outRect.left = space;
				outRect.right = space/2;

			}
			outRect.bottom = space/2;
			outRect.top = space/2;
		}

	}
}