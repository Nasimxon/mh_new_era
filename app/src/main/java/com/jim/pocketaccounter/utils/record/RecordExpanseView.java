package com.jim.pocketaccounter.utils.record;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Handler;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.SettingsActivity;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.BoardButton;
import com.jim.pocketaccounter.database.BoardButtonDao;
import com.jim.pocketaccounter.database.CreditDetials;
import com.jim.pocketaccounter.database.CreditDetialsDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.FinanceRecordDao;
import com.jim.pocketaccounter.database.RootCategoryDao;
import com.jim.pocketaccounter.database.FinanceRecord;
import com.jim.pocketaccounter.database.RootCategory;
import com.jim.pocketaccounter.debt.DebtBorrowFragment;
import com.jim.pocketaccounter.finance.CategoryAdapterForDialog;
import com.jim.pocketaccounter.fragments.AddCreditFragment;
import com.jim.pocketaccounter.fragments.InfoCreditFragment;
import com.jim.pocketaccounter.fragments.AccountFragment;
import com.jim.pocketaccounter.fragments.AutoMarketFragment;
import com.jim.pocketaccounter.fragments.CategoryFragment;
import com.jim.pocketaccounter.fragments.CreditTabLay;
import com.jim.pocketaccounter.fragments.CurrencyFragment;
import com.jim.pocketaccounter.fragments.PurposeFragment;
import com.jim.pocketaccounter.fragments.RecordEditFragment;
import com.jim.pocketaccounter.fragments.RootCategoryEditFragment;
import com.jim.pocketaccounter.managers.CommonOperations;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.report.ReportByAccount;
import com.jim.pocketaccounter.utils.OperationsListDialog;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;
import com.jim.pocketaccounter.utils.TransferDialog;
import com.jim.pocketaccounter.utils.WarningDialog;
import com.jim.pocketaccounter.utils.cache.BoardBitmap;
import com.jim.pocketaccounter.utils.cache.DataCache;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Vibrator;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressLint("DrawAllocation")
public class RecordExpanseView extends View implements 	GestureDetector.OnGestureListener {
	private final float workspaceCornerRadius, workspaceMargin;
	private Bitmap workspaceShader;
	private RectF workspace;
	private ArrayList<RecordButtonExpanse> buttons;
	private GestureDetectorCompat gestureDetector;
	private Calendar date;
	private Canvas canvas;
	private float twoDp;
	private int tableCount;
	private int currentPage;
	private int buttonsSize;
	private int beltBalance;
	private float oneDp;
	@Inject	DaoSession daoSession;
	@Inject	PAFragmentManager paFragmentManager;
	@Inject	LogicManager logicManager;
	@Inject	DataCache dataCache;
	@Inject	CommonOperations commonOperations;
	@Inject SharedPreferences sharedPreferences;
	@Inject @Named(value = "begin") Calendar begin;
	@Inject @Named(value = "end") Calendar end;
	@Inject @Named(value = "common_formatter") SimpleDateFormat simpleDateFormat;
	public RecordExpanseView(Context context, Calendar date) {
		super(context);
		((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
		this.date = date;
		gestureDetector = new GestureDetectorCompat(getContext(),this);
		workspaceCornerRadius = getResources().getDimension(R.dimen.five_dp);
		workspaceMargin = getResources().getDimension(R.dimen.twenty_dp);
		oneDp = getResources().getDimension(R.dimen.one_dp);
		updatePageCountAndPosition();
		setClickable(true);
		buttonsSize =  PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT;
		beltBalance = ContextCompat.getColor(getContext(), R.color.belt_balanse);
		twoDp = getResources().getDimension(R.dimen.four_dp)/getResources().getDisplayMetrics().density;
	}
	public void updatePageCountAndPosition() {
		this.tableCount = sharedPreferences.getInt("key_for_window_top", 4);
		this.currentPage = sharedPreferences.getInt("expense_current_page", 0);
		initButtons();
	}
	private void initButtons() {
		buttons = new ArrayList<>();
		RecordButtonExpanse button;
		int type = 0;
		BoardButtonDao boardButtonDao = daoSession.getBoardButtonDao();
		List<BoardButton> boardButtonList = boardButtonDao
				.queryBuilder()
				.where(BoardButtonDao.Properties.Table.eq(PocketAccounterGeneral.EXPENSE))
				.build()
				.list();
		for (int i = 0; i < PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT; i++) {
			switch(i) {
				case 0:
					type = PocketAccounterGeneral.UP_TOP_LEFT;
					break;
				case 1:
				case 2:
					type = PocketAccounterGeneral.UP_TOP_SIMPLE;
					break;
				case 3:
					type = PocketAccounterGeneral.UP_TOP_RIGHT;
					break;
				case 4:
				case 8:
					type = PocketAccounterGeneral.UP_LEFT_SIMPLE;
					break;
				case 5:
				case 6:
				case 9:
				case 10:
					type = PocketAccounterGeneral.UP_SIMPLE;
					break;
				case 7:
				case 11:
					type = PocketAccounterGeneral.UP_RIGHT_SIMPLE;
					break;
				case 12:
					type = PocketAccounterGeneral.UP_LEFT_BOTTOM;
					break;
				case 13:
				case 14:
					type = PocketAccounterGeneral.UP_BOTTOM_SIMPLE;
					break;
				case 15:
					type = PocketAccounterGeneral.UP_BOTTOM_RIGHT;
					break;
			}
			button = new RecordButtonExpanse(getContext(), type, date);
			for (int j=0; j<boardButtonList.size(); j++) {
				if (boardButtonList.get(j).getTable() == PocketAccounterGeneral.EXPENSE &&
					(boardButtonList.get(j).getPos()-currentPage*PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT) == i) {
					button.setCategory(boardButtonList.get(j));
				}
			}
			buttons.add(button);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		this.canvas = canvas;
		workspace = new RectF(workspaceMargin,
							  workspaceMargin,
							  getWidth()-workspaceMargin,
							  getHeight()-workspaceMargin);
		drawButtons();
		drawPercents();
		drawWorkspaceShader();
		drawIndicator();
	}

	private void drawIndicator() {
		if (tableCount == 1) return;
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		float y = 7.25f*twoDp, x;
		if (tableCount % 2 == 0) {
			x = workspace.centerX()-4*twoDp-tableCount*twoDp-(tableCount/2-1)*twoDp;
			for (int i=0; i<tableCount; i++) {
				if (i == currentPage) {
					paint.setColor(Color.BLACK);
				}
				else {
					paint.setColor(Color.GRAY);
				}
				canvas.drawCircle(i*6*twoDp + x, y, 1.5f*twoDp, paint);
			}
		}
		else {
			x = workspace.centerX()-1.5f*twoDp-4*twoDp*(tableCount-1)/2f-twoDp*(tableCount-1)/2;
			for (int i=0; i<tableCount; i++) {
				if (i == currentPage) {
					paint.setColor(Color.BLACK);
				}
				else {
					paint.setColor(Color.GRAY);
				}
				canvas.drawCircle(i*6*twoDp + x, y, 1.5f*twoDp, paint);
			}
		}
	}

	private void drawButtons() {
		float width, height;
		width = workspace.width()/4;
		height = workspace.height()/4;
		float left, top, right, bottom;
		for (int i=0; i<buttonsSize; i++) {
			left = workspace.left+(i%4)*width;
			top = workspace.top+((int)Math.floor(i/4)*height);
			right = workspace.left+(i%4+1)*width;
			bottom = workspace.top+((int)(Math.floor(i/4)+1)*height);
			buttons.get(i).setBounds(left, top, right, bottom, workspaceCornerRadius);
			buttons.get(i).drawButton(canvas);
		}
		Paint borderPaint = new Paint();
		borderPaint.setColor(beltBalance);
		borderPaint.setStrokeWidth(oneDp);
		for (int i=0; i<3; i++) {
			canvas.drawLine(workspace.left,
					workspace.top+(i+1)*width,
					workspace.right,
					workspace.top+(i+1)*width, borderPaint);
			canvas.drawLine(workspace.left+(i+1)*width,
					workspace.top,
					workspace.left+(i+1)*width,
					workspace.bottom, borderPaint);
		}
	}
	public void drawPercents() {
		Rect bounds = new Rect();
		Paint textPaint = new Paint();
		textPaint.setColor(ContextCompat.getColor(getContext(), R.color.red));
		textPaint.setTextSize(getResources().getDimension(R.dimen.ten_sp));
		textPaint.setAntiAlias(true);
		Rect letBound = new Rect();
		textPaint.getTextBounds("A", 0, "A".length(), letBound);
		DecimalFormat format = new DecimalFormat("0.00");
		float aLetterHeight = letBound.height();
		for (final RecordButtonExpanse button : buttons) {
			Double percent = dataCache.getPercent(PocketAccounterGeneral.EXPENSE,
					date, button.getCategory().getPos());
			if (percent == 0) continue;
			String text = format.format(percent)+"%";
			textPaint.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, button.getContainer().centerX()-bounds.width()/2,
					button.getContainer().centerY()+4*aLetterHeight, textPaint);
		}
	}
	private void drawWorkspaceShader() {
		if (dataCache.getElements().get(PocketAccounterGeneral.UP_WORKSPACE_SHADER) == null) {
			workspaceShader = commonOperations.getRoundedCornerBitmap(
					commonOperations.decodeSampledBitmapFromResource(
																	getResources(), R.drawable.workspace_shader,
																	(int) workspace.width(),
																	(int) workspace.height()),
																	(int) workspaceCornerRadius);
			workspaceShader = Bitmap.createScaledBitmap(workspaceShader, (int)workspace.width(), (int)workspace.height(), false);
			dataCache.getElements().put(PocketAccounterGeneral.UP_WORKSPACE_SHADER, workspaceShader);
		}
		else
			workspaceShader = dataCache.getElements().get(PocketAccounterGeneral.UP_WORKSPACE_SHADER);
		Paint paint = new Paint();
		paint.setAlpha(0x55);
		paint.setAntiAlias(true);
		canvas.drawBitmap(workspaceShader, workspace.left, workspace.top, paint);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(ContextCompat.getColor(getContext(), R.color.record_outline));
		paint.setStrokeWidth(getResources().getDimension(R.dimen.one_dp));
		canvas.drawRoundRect(workspace, workspaceCornerRadius, workspaceCornerRadius, paint);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (PocketAccounter.PRESSED) return true;
		int size = buttons.size();
		float x = e.getX();
		float y = e.getY();
		for (int i=0; i<size; i++) {
			if (buttons.get(i).getContainer() != null && buttons.get(i).getContainer().contains(x, y)) {
				buttons.get(i).setPressed(true);
				final int position = i;
				postDelayed(new Runnable() {
					@Override
					public void run() {
						if (buttons.get(position).getCategory().getCategoryId() == null)
							openTypeChooseDialog(position);
						else if (buttons.get(position).getCategory().getType() == PocketAccounterGeneral.CATEGORY) {
							RootCategory category = null;
							if(buttons.get(position).getCategory().getCategoryId() == null)
								category = null;
							else {
								List<RootCategory> categoryList = daoSession.getRootCategoryDao().queryBuilder()
										.where(RootCategoryDao.Properties.Id.eq(buttons.get(position).getCategory().getCategoryId()))
										.list();
								if (!categoryList.isEmpty())
									category = categoryList.get(0);
							}
							paFragmentManager.displayFragment(new RecordEditFragment(category, date, null, PocketAccounterGeneral.MAIN));
						}
						else if (buttons.get(position).getCategory().getType() == PocketAccounterGeneral.CREDIT) {
							CreditDetials item=daoSession.getCreditDetialsDao().load(Long.parseLong(buttons.get(position).getCategory().getCategoryId()));
							InfoCreditFragment temp = new InfoCreditFragment();
							temp.setContentFromMainWindow(item,position,PocketAccounterGeneral.EXPANSE_MODE);
							paFragmentManager.displayFragment(temp);

						}
						else if (buttons.get(position).getCategory().getType() == PocketAccounterGeneral.DEBT_BORROW) {

						}
						else if (buttons.get(position).getCategory().getType() == PocketAccounterGeneral.PAGE) {
							String[] pageIds = getResources().getStringArray(R.array.page_ids);
							int pos = 0;
							for (int i=0; i<pageIds.length; i++) {
								if (pageIds[i].matches(buttons.get(position).getCategory().getCategoryId())) {
									pos = i;
									break;
								}
							}
							switch (pos) {
								case 0:
									paFragmentManager.displayFragment(new CurrencyFragment());
									break;
								case 1:
									paFragmentManager.displayFragment(new CategoryFragment());

									break;
								case 2:
									paFragmentManager.displayFragment(new AccountFragment());

									break;
								case 3:
									paFragmentManager.displayFragment(new PurposeFragment());

									break;
								case 4:
									paFragmentManager.displayFragment(new AutoMarketFragment());

									break;
								case 5:
									paFragmentManager.displayFragment(new CreditTabLay());

									break;
								case 6:
									paFragmentManager.displayFragment(new DebtBorrowFragment());

									break;
								case 7:
									//report by account
									break;
								case 8:
									//report by incomes and expenses
									break;
								case 9:
									//report by category
									break;
								case 10:
									//SMS parsing
									break;
								case 11:
									Intent intent = new Intent(getContext(), SettingsActivity.class);
									getContext().startActivity(intent);
									break;
							}
						}
						else if (buttons.get(position).getCategory().getType() == PocketAccounterGeneral.FUNCTION) {
							String[] functionIds = getResources().getStringArray(R.array.operation_ids);
							int pos = 0;
							for (int i = 0; i<functionIds.length; i++) {
								if (functionIds[i].matches(buttons.get(position).getCategory().getCategoryId())) {
									pos = i;
									break;
								}
							}
							switch(pos) {
								case 0:
									//google synchronization
									break;
								case 1:
									//google download
									break;
								case 2:
									Account account = daoSession.getAccountDao().loadAll().isEmpty() ?
											null : daoSession.getAccountDao().loadAll().get(0);
									final TransferDialog transferDialog = new TransferDialog(getContext());
									transferDialog.setAccountOrPurpose(account.getId(), true);
									transferDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
										@Override
										public void onCancel(DialogInterface dialog) {
											for (int i=0; i < buttons.size(); i++)
												buttons.get(i).setPressed(false);
											invalidate();
										}
									});
									transferDialog.show();
									break;
								case 3:
									final WarningDialog warningDialog = new WarningDialog(getContext());
									warningDialog.setText(getContext().getString(R.string.whole_day_datas_deleting));
									warningDialog.setOnYesButtonListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											Calendar begin = Calendar.getInstance();
											begin.setTimeInMillis(date.getTimeInMillis());
											begin.set(Calendar.HOUR_OF_DAY, 0);
											begin.set(Calendar.MINUTE, 0);
											begin.set(Calendar.SECOND, 0);
											begin.set(Calendar.MILLISECOND, 0);
											Calendar end = Calendar.getInstance();
											begin.setTimeInMillis(date.getTimeInMillis());
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
											dataCache.updateOneDay(date);
											paFragmentManager.updateAllFragmentsOnViewPager();
										}
									});
									warningDialog.setOnNoButtonClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											warningDialog.dismiss();
										}
									});
									warningDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											for (int i=0; i < buttons.size(); i++) {

															buttons.get(i).setPressed(false);
											}
											PocketAccounter.PRESSED = false;
											invalidate();
										}
									});
									warningDialog.show();
									break;
								case 4:
									if (currentPage == tableCount-1)
										currentPage = 0;
									else
										currentPage++;
									sharedPreferences
											.edit()
											.putInt("expense_current_page", currentPage)
											.commit();
									initButtons();
									invalidate();
									paFragmentManager.updateAllFragmentsPageChanges();
									break;
								case 5:
									if (currentPage == 0)
										currentPage = tableCount-1;
									else
										currentPage--;
									sharedPreferences
											.edit()
											.putInt("expense_current_page", currentPage)
											.commit();
									initButtons();
									invalidate();
									paFragmentManager.updateAllFragmentsPageChanges();
									break;
							}
							PocketAccounter.PRESSED = false;
						}
					}
				}, 150);
				invalidate();
				PocketAccounter.PRESSED = true;
				break;
			}
		}
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		Vibrator vibr = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		vibr.vibrate(60);
		float x = e.getX(), y = e.getY();
		int size = buttons.size();
		for (int i=0; i<size; i++) {
			if (buttons.get(i).getContainer().contains(x, y)) {
				buttons.get(i).setPressed(true);
				final int position = i;
				List<BoardButton> boardButtonList = daoSession.getBoardButtonDao()
						.queryBuilder().where(BoardButtonDao.Properties.Table.eq(PocketAccounterGeneral.EXPENSE))
						.list();
				if (boardButtonList.get(position).getCategoryId() == null) {
					for (int j=0; j<buttons.size(); j++)
						buttons.get(j).setPressed(false);
					PocketAccounter.PRESSED = false;
					invalidate();
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
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}
	private void openChooseDialogLongPress(final int pos) {
		begin.setTimeInMillis(date.getTimeInMillis());
		begin.set(Calendar.HOUR_OF_DAY, 0);
		begin.set(Calendar.MINUTE, 0);
		begin.set(Calendar.SECOND, 0);
		begin.set(Calendar.MILLISECOND, 0);
		end.setTimeInMillis(date.getTimeInMillis());
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 59);
		String edit = getContext().getString(R.string.to_edit);
		String change = getResources().getString(R.string.change);
		String clear = getResources().getString(R.string.clear);
		String clearRecords = getContext().getString(R.string.clear_records);
		BoardButton cur = daoSession.getBoardButtonDao().queryBuilder()
				.where(BoardButtonDao.Properties.Pos.eq(pos), BoardButtonDao.Properties.Table.eq(PocketAccounterGeneral.EXPENSE))
				.list().isEmpty() ?
				null:daoSession.getBoardButtonDao().queryBuilder()
				.where(BoardButtonDao.Properties.Pos.eq(pos), BoardButtonDao.Properties.Table.eq(PocketAccounterGeneral.EXPENSE))
				.list().get(0);
		String[] items = null;
		String format = simpleDateFormat.format(begin.getTime());
		List<FinanceRecord> temp = daoSession.getFinanceRecordDao().queryBuilder()
				.where(FinanceRecordDao.Properties.CategoryId.eq(cur.getCategoryId()),
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
						logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, currentPage*PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT+pos, null);
						changeIconInCache(pos, "no_category");
						initButtons();
						for (int i=0; i<buttons.size(); i++)
							buttons.get(i).setPressed(false);
						invalidate();
						paFragmentManager.updateAllFragmentsOnViewPager();
						dataCache.updateOneDay(date);
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
			}
		});
		operationsListDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				PocketAccounter.PRESSED = false;
				invalidate();
			}
		});
		operationsListDialog.show();
	}

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
											paFragmentManager.displayFragment(new RootCategoryEditFragment(null, PocketAccounterGeneral.EXPANSE_MODE, pos, date));
											break;
										case 1:
											paFragmentManager.displayFragment((new AddCreditFragment()).setDateFormatModes(PocketAccounterGeneral.EXPANSE_MODE,pos));
											break;
										case 2:

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
		operationsListDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				PocketAccounter.PRESSED = false;
				invalidate();
			}
		});
		operationsListDialog.show();
	}

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
				logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, currentPage*PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT+pos, categories.get(position).getId());
				changeIconInCache(pos, categories.get(position).getIcon());
				initButtons();
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				invalidate();
				paFragmentManager.updateAllFragmentsOnViewPager();
				dataCache.updateOneDay(date);
				PocketAccounter.PRESSED = false;
				dialog.dismiss();
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				invalidate();
				PocketAccounter.PRESSED = false;
			}
		});
		dialog.show();
	}

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
				logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, currentPage*PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT+pos, categories.get(position).getId());
				changeIconInCache(pos, categories.get(position).getIcon());
				initButtons();
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				invalidate();
				paFragmentManager.updateAllFragmentsOnViewPager();
				dataCache.updateOneDay(date);
				PocketAccounter.PRESSED = false;
				dialog.dismiss();
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				invalidate();
				PocketAccounter.PRESSED = false;
			}
		});
		dialog.show();
	}

	private void clear(final int pos) {
		begin.setTimeInMillis(date.getTimeInMillis());
		begin.set(Calendar.HOUR_OF_DAY, 0);
		begin.set(Calendar.MINUTE, 0);
		begin.set(Calendar.SECOND, 0);
		begin.set(Calendar.MILLISECOND, 0);
		end.setTimeInMillis(date.getTimeInMillis());
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 59);
		final String id = daoSession.getBoardButtonDao().queryBuilder()
							.where(BoardButtonDao.Properties.Pos.eq(pos))
							.list().get(0).getCategoryId();
		final WarningDialog warningDialog = new WarningDialog(getContext());
		warningDialog.setText(getContext().getString(R.string.clear_warning));
		warningDialog.setOnYesButtonListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				QueryBuilder<FinanceRecord> financeRecordQueryBuilder = daoSession.getFinanceRecordDao().queryBuilder();
				List<FinanceRecord> deletingRecords = financeRecordQueryBuilder.where(FinanceRecordDao.Properties.Date.eq(simpleDateFormat.format(begin.getTime())),
						FinanceRecordDao.Properties.CategoryId.eq(id))
						.list();
				daoSession.getFinanceRecordDao().deleteInTx(deletingRecords);
				PocketAccounter.PRESSED = false;
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				PocketAccounter.PRESSED = false;
				invalidate();
				warningDialog.dismiss();
			}
		});
		warningDialog.setOnNoButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				PocketAccounter.PRESSED = false;
				invalidate();
				warningDialog.dismiss();
			}
		});
		warningDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				PocketAccounter.PRESSED = false;
				invalidate();
			}
		});
		warningDialog.show();
	}

	private void openEditDialog(int position) {
		final Dialog dialog=new Dialog(getContext());
		View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(dialogView);
		ListView lvDialog = (ListView) dialogView.findViewById(R.id.lvDialog);
		String id = buttons.get(position).getCategory().getCategoryId();
		final List<FinanceRecord> records = daoSession.getFinanceRecordDao().queryBuilder()
				.where(FinanceRecordDao.Properties.CategoryId.eq(id)).list();
		LongPressAdapter adapter = new LongPressAdapter(getContext(), records);
		lvDialog.setAdapter(adapter);
		lvDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				paFragmentManager.displayFragment(new RecordEditFragment(records.get(position).getCategory(), date, records.get(position), PocketAccounterGeneral.MAIN));
				PocketAccounter.PRESSED = false;
				dialog.dismiss();
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				invalidate();
				PocketAccounter.PRESSED = false;
			}
		});
		dialog.show();
	}

	private void openCategoryChooseDialog(final int pos) {
		final Dialog dialog=new Dialog(getContext());
		View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(dialogView);
		final ArrayList<IconWithName> categories = new ArrayList<>();
		List<RootCategory> categoryList = daoSession.getRootCategoryDao().queryBuilder()
					.where(RootCategoryDao.Properties.Type.eq(PocketAccounterGeneral.EXPENSE))
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
				logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, currentPage*PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT+pos, categories.get(position).getId());
				changeIconInCache(pos, categories.get(position).getIcon());
				initButtons();
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				invalidate();
				paFragmentManager.updateAllFragmentsOnViewPager();
				dataCache.updateOneDay(date);
				PocketAccounter.PRESSED = false;
				dialog.dismiss();
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				for (int i=0; i<buttons.size(); i++)
					buttons.get(i).setPressed(false);
				invalidate();
				PocketAccounter.PRESSED = false;
			}
		});
		dialog.show();
	}

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
					logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, currentPage*PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT+pos, categories.get(position).getId());
					changeIconInCache(pos, categories.get(position).getIcon());
					initButtons();
					for (int i=0; i<buttons.size(); i++)
						buttons.get(i).setPressed(false);
					invalidate();
					paFragmentManager.updateAllFragmentsOnViewPager();
					dataCache.updateOneDay(date);
					PocketAccounter.PRESSED = false;
					dialog.dismiss();
				}
			});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					for (int i=0; i<buttons.size(); i++)
						buttons.get(i).setPressed(false);
					invalidate();
					PocketAccounter.PRESSED = false;
				}
			});
			dialog.show();
		}
		else {
			Toast.makeText(getContext(), R.string.debt_borrow_list_is_empty, Toast.LENGTH_SHORT).show();
		}

	}

	private void openCreditsChooseDialog(final int pos) {
		List<CreditDetials> debtBorrowList = daoSession.getCreditDetialsDao()
				.queryBuilder().where(CreditDetialsDao.Properties.Key_for_include.eq(true),
						CreditDetialsDao.Properties.Key_for_archive.eq(false)).list();
		if (!debtBorrowList.isEmpty()) {
			final Dialog dialog = new Dialog(getContext());
			View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_listview, null);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(dialogView);
			final ArrayList<IconWithName> categories = new ArrayList<>();
			List<CreditDetials> creditDetialsList = daoSession.getCreditDetialsDao().loadAll();
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
					logicManager.changeBoardButton(PocketAccounterGeneral.EXPENSE, currentPage*PocketAccounterGeneral.EXPENSE_BUTTONS_COUNT+pos, categories.get(position).getId());
					changeIconInCache(pos, categories.get(position).getIcon());
					initButtons();
					for (int i = 0; i < buttons.size(); i++)
						buttons.get(i).setPressed(false);
					invalidate();
					paFragmentManager.updateAllFragmentsOnViewPager();
					dataCache.updateOneDay(date);
					PocketAccounter.PRESSED = false;
					dialog.dismiss();
				}
			});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					for (int i = 0; i < buttons.size(); i++)
						buttons.get(i).setPressed(false);
					invalidate();
					PocketAccounter.PRESSED = false;
				}
			});
			dialog.show();
		}
		else
			Toast.makeText(getContext(), R.string.credit_list_is_empty, Toast.LENGTH_SHORT).show();
	}
	private void changeIconInCache(int pos, String icon) {
		int resId = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap scaled = BitmapFactory.decodeResource(getResources(), resId, options);
		scaled = Bitmap.createScaledBitmap(scaled, (int)getResources().getDimension(R.dimen.thirty_dp), (int) getResources().getDimension(R.dimen.thirty_dp), true);
		dataCache.getBoardBitmapsCache().put(buttons.get(pos).getCategory().getId(),
				scaled);
	}
}