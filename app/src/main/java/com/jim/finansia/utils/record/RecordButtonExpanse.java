package com.jim.finansia.utils.record;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;

import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.DebtBorrow;
import com.jim.finansia.database.DebtBorrowDao;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.cache.DataCache;

import org.greenrobot.greendao.query.Query;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class RecordButtonExpanse {
	private boolean pressed = false;
	private RectF container;
	private int type;
	private Path shape;
	private Bitmap shadow;
	private float radius, clearance;
	private Context context;
	private BoardButton boardButton;
	private float aLetterHeight;
	private BitmapFactory.Options options;
	private int thirtyDp, toolbarTextColor, tenSp;
	private String[] operationIds, operationIcons, operationNames, pageIcons, pageIds, pageNames;
	@Inject DaoSession daoSession;
	@Inject	DataCache dataCache;
	public RecordButtonExpanse(Context context, int type, Calendar date) {
		this.context = context;
		((PocketAccounterApplication) context.getApplicationContext()).component().inject(this);
		options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		thirtyDp = (int) context.getResources().getDimension(R.dimen.thirty_dp);
		date.setTimeInMillis(date.getTimeInMillis());
		clearance = context.getResources().getDimension(R.dimen.one_dp);
		tenSp = (int) context.getResources().getDimension(R.dimen.ten_sp);
		shape = new Path();
		this.type = type;
		Paint paint = new Paint();
		paint.setTextSize(context.getResources().getDimension(R.dimen.ten_sp));
		Rect bounds = new Rect();
		paint.getTextBounds("A", 0, "A".length(), bounds);
		aLetterHeight = bounds.height();
		container = new RectF();
		operationIds = context.getResources().getStringArray(R.array.operation_ids);
		operationIcons = context.getResources().getStringArray(R.array.operation_icons);
		operationNames = context.getResources().getStringArray(R.array.operation_names);
		pageIds = context.getResources().getStringArray(R.array.page_ids);
		pageIcons = context.getResources().getStringArray(R.array.page_icons);
		pageNames = context.getResources().getStringArray(R.array.page_names);
		toolbarTextColor = ContextCompat.getColor(context, R.color.toolbar_text_color);
	}
	public void setBounds(float left, float top, float right, float bottom, float radius) {
		container.set(left, top, right, bottom);
		this.radius = radius;
		switch(type) {
		case PocketAccounterGeneral.UP_TOP_LEFT:
			initTopLeft();
			break;
		case PocketAccounterGeneral.UP_TOP_SIMPLE:
			initSimple();
			break;
		case PocketAccounterGeneral.UP_TOP_RIGHT:
			initTopRight();
			break;
		case PocketAccounterGeneral.UP_LEFT_SIMPLE:
			initLeftSimple();
			break;
		case PocketAccounterGeneral.UP_SIMPLE:
			initSimple();
			break;
		case PocketAccounterGeneral.UP_RIGHT_SIMPLE:
			initSimple();
			break;
		case PocketAccounterGeneral.UP_LEFT_BOTTOM:
			initLeftBottom();
			break;
		case PocketAccounterGeneral.UP_BOTTOM_SIMPLE:
			initBottomSimple();
			break;
		case PocketAccounterGeneral.UP_BOTTOM_RIGHT:
			initBottomRight();
			break;
		}
	}
	private void initTopRight() {
		shape.moveTo(container.left, container.top);
		shape.lineTo(container.right-2*radius, container.top);
		shape.arcTo(new RectF(container.right-2*radius, container.top, container.right, container.top+2*radius), 270.0f, 90.0f);
		shape.lineTo(container.right, container.bottom);
		shape.lineTo(container.left, container.bottom);
		shape.lineTo(container.left, container.top);
		shape.close();
	}
	private void initBottomRight() {
		shape.moveTo(container.left, container.top);
		shape.lineTo(container.right, container.top);
		shape.lineTo(container.right, container.bottom-2*radius);
		shape.arcTo(new RectF(container.right-2*radius, container.bottom-2*radius, container.right, container.bottom), 0.0f, 90.0f);
		shape.lineTo(container.left, container.bottom);
		shape.lineTo(container.left, container.top);
		shape.close();
		float bitmapWidth, bitmapHeight;
		bitmapHeight = container.height()/5;
		bitmapWidth = bitmapHeight*6-radius/2;
		if (dataCache.getElements().get(PocketAccounterGeneral.UP_BOTTOM_RIGHT) == null) {
			shadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.bottom_shadow, options);
			shadow = Bitmap.createScaledBitmap(shadow, (int)(bitmapWidth-2*clearance), (int)bitmapHeight, false);
			dataCache.getElements().put(PocketAccounterGeneral.UP_BOTTOM_RIGHT, shadow);
		}
		else
			shadow = dataCache.getElements().get(PocketAccounterGeneral.UP_BOTTOM_RIGHT);
	}
	private void initBottomSimple() {
		shape.moveTo(container.left, container.top);
		shape.lineTo(container.right, container.top);
		shape.lineTo(container.right, container.bottom);
		shape.lineTo(container.left, container.bottom);
		shape.lineTo(container.left, container.top);
		shape.close();
		float bitmapWidth, bitmapHeight;
		bitmapHeight = container.height()/5;
		bitmapWidth = bitmapHeight*6;
		if (dataCache.getElements().get(PocketAccounterGeneral.UP_BOTTOM_SIMPLE) == null) {
			shadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.bottom_shadow, options);
			shadow = Bitmap.createScaledBitmap(shadow, (int)(bitmapWidth-2*clearance), (int)bitmapHeight, false);
			dataCache.getElements().put(PocketAccounterGeneral.UP_BOTTOM_SIMPLE, shadow);
		}
		else
			shadow = dataCache.getElements().get(PocketAccounterGeneral.UP_BOTTOM_SIMPLE);
	}
	private void initSimple() {
		shape.moveTo(container.left, container.top);
		shape.lineTo(container.right, container.top);
		shape.lineTo(container.right, container.bottom);
		shape.lineTo(container.left, container.bottom);
		shape.lineTo(container.left, container.top);
		shape.close();
		shadow = null;
	}
	private void initTopLeft() {
		shape.moveTo(container.left+2*radius, container.top);
		shape.lineTo(container.right, container.top);
		shape.lineTo(container.right, container.bottom);
		shape.lineTo(container.left, container.bottom);
		shape.lineTo(container.left, container.top+2*radius);
		RectF rect = new RectF(container.left, container.top, container.left+2*radius, container.top+2*radius);
		shape.arcTo(rect, 180.0f, 90.0f);
		shape.close();
		float y = container.top+radius;
		float delta_y = (float)(y - radius*Math.sin(Math.toRadians(45))-container.top); 
		float bitmapWidth, bitmapHeight;
		bitmapWidth = container.width()/5;
		bitmapHeight = bitmapWidth*6-delta_y;
		if (dataCache.getElements().get(PocketAccounterGeneral.UP_TOP_LEFT) == null) {
			shadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.left_shadow, options);
			shadow = Bitmap.createScaledBitmap(shadow, (int)bitmapWidth, (int)(bitmapHeight-clearance*2), false);
			dataCache.getElements().put(PocketAccounterGeneral.UP_TOP_LEFT, shadow);
		}
		else
			shadow = dataCache.getElements().get(PocketAccounterGeneral.UP_TOP_LEFT);
	}

	private void initLeftSimple() {
		shape.moveTo(container.left, container.top);
		shape.lineTo(container.right, container.top);
		shape.lineTo(container.right, container.bottom);
		shape.lineTo(container.left, container.bottom);
		shape.lineTo(container.left, container.top);
		shape.close();
		float bitmapWidth, bitmapHeight;
		bitmapWidth = container.width()/5;
		bitmapHeight = bitmapWidth*6;
		if (dataCache.getElements().get(PocketAccounterGeneral.UP_LEFT_SIMPLE) == null) {
			shadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.left_shadow, options);
			shadow = Bitmap.createScaledBitmap(shadow, (int)bitmapWidth, (int)(bitmapHeight-clearance*2), false);
			dataCache.getElements().put(PocketAccounterGeneral.UP_LEFT_SIMPLE, shadow);
		}
		else
			shadow = dataCache.getElements().get(PocketAccounterGeneral.UP_LEFT_SIMPLE);
	}
	private void initLeftBottom() {
		shape.moveTo(container.left, container.top);
		shape.lineTo(container.right, container.top);
		shape.lineTo(container.right, container.bottom);
		shape.lineTo(container.left+radius, container.bottom);
		shape.arcTo(new RectF(container.left, container.bottom-2*radius, container.left+2*radius, container.bottom), 90.0f, 90.0f);
		shape.lineTo(container.left, container.top);
		shape.close();
		float bitmapWidth, bitmapHeight;
		bitmapWidth = 6*container.width()/5;
		bitmapHeight = bitmapWidth;
		if (dataCache.getElements().get(PocketAccounterGeneral.UP_LEFT_BOTTOM) == null) {
			shadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.left_bottom, options);
			shadow = Bitmap.createScaledBitmap(shadow, (int)(bitmapWidth-clearance*2), (int)(bitmapHeight+clearance*2), false);
			dataCache.getElements().put(PocketAccounterGeneral.UP_LEFT_BOTTOM, shadow);
		}
		else
			shadow = dataCache.getElements().get(PocketAccounterGeneral.UP_LEFT_BOTTOM);
	}
	public void drawButton(Canvas canvas) {
		Bitmap scaled = null;
		Paint bitmapPaint = new Paint();
		bitmapPaint.setAntiAlias(true);
		bitmapPaint.setAlpha(0x77);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		PointF center = new PointF();
		switch(type) {
		case PocketAccounterGeneral.UP_TOP_LEFT:
			if (!pressed) {
				float shadow_x, shadow_y;
				center.set(container.left+radius, container.top+radius);
				shadow_x = (float)(center.x - radius*Math.cos(Math.toRadians(45)));
				shadow_y = (float)(center.y - radius*Math.sin(Math.toRadians(45))); 
				canvas.drawBitmap(shadow, shadow_x-shadow.getWidth(), shadow_y, bitmapPaint);
			}
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				innerShadowPaint.setAntiAlias(true);
				canvas.drawPath(shape, innerShadowPaint);
				Bitmap innerShadow = null;
				if (dataCache.getElements().get(PocketAccounterGeneral.UP_TOP_LEFT_PRESSED) == null) {
					innerShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_pressed_first, options);
					innerShadow = Bitmap.createScaledBitmap(innerShadow, (int)(container.width()/5), (int)container.height(), false);
					dataCache.getElements().put(PocketAccounterGeneral.UP_TOP_LEFT_PRESSED, innerShadow);
				}
				else
					innerShadow = dataCache.getElements().get(PocketAccounterGeneral.UP_TOP_LEFT_PRESSED);
				innerShadowPaint.setAlpha(0x66);
				canvas.drawBitmap(innerShadow, container.right-innerShadow.getWidth(), container.top, innerShadowPaint);
			}
			break;
		case PocketAccounterGeneral.UP_TOP_SIMPLE:
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				canvas.drawPath(shape, innerShadowPaint);
				Bitmap innerShadow = null;
				if (dataCache.getElements().get(PocketAccounterGeneral.UP_TOP_SIMPLE_PRESSED) == null) {
					innerShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_pressed_first, options);
					innerShadow = Bitmap.createScaledBitmap(innerShadow, (int)(container.width()/5), (int)container.height(), false);
					dataCache.getElements().put(PocketAccounterGeneral.UP_TOP_SIMPLE_PRESSED, innerShadow);
				}
				else
					innerShadow = dataCache.getElements().get(PocketAccounterGeneral.UP_TOP_SIMPLE_PRESSED);
				innerShadowPaint.setAlpha(0x66);
				canvas.drawBitmap(innerShadow, container.right-innerShadow.getWidth(), container.top, innerShadowPaint);
			}
			break;
		case PocketAccounterGeneral.UP_TOP_RIGHT:
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				canvas.drawPath(shape, innerShadowPaint);
			}
			break;
		case PocketAccounterGeneral.UP_LEFT_SIMPLE:
			if (!pressed)
				canvas.drawBitmap(shadow, container.left-shadow.getWidth(), container.top+clearance, bitmapPaint);
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				canvas.drawPath(shape, innerShadowPaint);
				Bitmap innerShadow;
				if (dataCache.getElements().get(PocketAccounterGeneral.UP_LEFT_SIMPLE_PRESSED) == null) {
					innerShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_pressed_second, options);
					innerShadow = Bitmap.createScaledBitmap(innerShadow, (int)(container.width()), (int)container.height(), false);
					dataCache.getElements().put(PocketAccounterGeneral.UP_LEFT_SIMPLE_PRESSED, innerShadow);
				}
				else
					innerShadow = dataCache.getElements().get(PocketAccounterGeneral.UP_LEFT_SIMPLE_PRESSED);
				innerShadowPaint.setAlpha(0x66);
				canvas.drawBitmap(innerShadow, container.left, container.top, innerShadowPaint);
			}
			break;
		case PocketAccounterGeneral.UP_SIMPLE:
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				canvas.drawPath(shape, innerShadowPaint);
				Bitmap innerShadow;
				if (dataCache.getElements().get(PocketAccounterGeneral.UP_SIMPLE_PRESSED) == null) {
					innerShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_pressed_second, options);
					innerShadow = Bitmap.createScaledBitmap(innerShadow, (int)(container.width()), (int)container.height(), false);
					dataCache.getElements().put(PocketAccounterGeneral.UP_SIMPLE_PRESSED, innerShadow);
				}
				else
					innerShadow = dataCache.getElements().get(PocketAccounterGeneral.UP_SIMPLE_PRESSED);
				innerShadowPaint.setAlpha(0x66);
				canvas.drawBitmap(innerShadow, container.left, container.top, innerShadowPaint);
			}
			break;
		case PocketAccounterGeneral.UP_RIGHT_SIMPLE:
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				canvas.drawPath(shape, innerShadowPaint);
				Bitmap innerShadow;
				if (dataCache.getElements().get(PocketAccounterGeneral.UP_RIGHT_SIMPLE_PRESSED) == null) {
					innerShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_pressed_third, options);
					innerShadow = Bitmap.createScaledBitmap(innerShadow, (int)(container.width()), (int)(container.height()/5), false);
					dataCache.getElements().put(PocketAccounterGeneral.UP_RIGHT_SIMPLE_PRESSED, innerShadow);
				}
				else
					innerShadow = dataCache.getElements().get(PocketAccounterGeneral.UP_RIGHT_SIMPLE_PRESSED);
				innerShadowPaint.setAlpha(0x66);
				canvas.drawBitmap(innerShadow, container.left, container.top, innerShadowPaint);
			}
			break;
		case PocketAccounterGeneral.UP_LEFT_BOTTOM:
			if (!pressed)
				canvas.drawBitmap(shadow, container.right-shadow.getWidth()-clearance, container.top-clearance, bitmapPaint);
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				canvas.drawPath(shape, innerShadowPaint);
				Bitmap innerShadow = null;
				if (dataCache.getElements().get(PocketAccounterGeneral.UP_LEFT_BOTTOM_PRESSED) == null) {
					innerShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_pressed_second, options);
					innerShadow = Bitmap.createScaledBitmap(innerShadow, (int)(container.width()), (int)container.height(), false);
					dataCache.getElements().put(PocketAccounterGeneral.UP_LEFT_BOTTOM_PRESSED, innerShadow);
				}
				else
					innerShadow = dataCache.getElements().get(PocketAccounterGeneral.UP_LEFT_BOTTOM_PRESSED);
				innerShadowPaint.setAlpha(0x66);
				canvas.drawBitmap(innerShadow, container.left, container.top, innerShadowPaint);
			}
			break;
		case PocketAccounterGeneral.UP_BOTTOM_SIMPLE:
			if (!pressed)
				canvas.drawBitmap(shadow, container.left-shadow.getHeight()+clearance, container.bottom, bitmapPaint);
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				canvas.drawPath(shape, innerShadowPaint);
				Bitmap innerShadow = null;
				if (dataCache.getElements().get(PocketAccounterGeneral.UP_BOTTOM_SIMPLE_PRESSED) == null) {
					innerShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_pressed_second, options);
					innerShadow = Bitmap.createScaledBitmap(innerShadow, (int)(container.width()), (int)container.height(), false);
					dataCache.getElements().put(PocketAccounterGeneral.UP_BOTTOM_SIMPLE_PRESSED, innerShadow);
				}
				else
					innerShadow = dataCache.getElements().get(PocketAccounterGeneral.UP_BOTTOM_SIMPLE_PRESSED);
				innerShadowPaint.setAlpha(0x66);
				canvas.drawBitmap(innerShadow, container.left, container.top, innerShadowPaint);
			}
			break;
		case PocketAccounterGeneral.UP_BOTTOM_RIGHT:
			if (!pressed) {
				center.set(container.right-radius, container.bottom-radius);
				float delta_x, delta_y;
				delta_x = (float) (radius*Math.sin(Math.toRadians(45)));
				delta_y = (float) (radius*Math.cos(Math.toRadians(45)));
				canvas.drawBitmap(shadow, center.x+delta_x-shadow.getWidth(), center.y+delta_y, bitmapPaint);
			}
			canvas.drawPath(shape, paint);
			if (pressed) {
				Paint innerShadowPaint = new Paint();
				innerShadowPaint.setColor(Color.BLACK);
				innerShadowPaint.setAlpha(0x22);
				canvas.drawPath(shape, innerShadowPaint);
				Bitmap innerShadow;
				if (dataCache.getElements().get(PocketAccounterGeneral.UP_BOTTOM_RIGHT_PRESSED) == null) {
					innerShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.record_pressed_third, options);
					innerShadow = Bitmap.createScaledBitmap(innerShadow, (int)container.width(), (int)(container.height()/5), false);
					dataCache.getElements().put(PocketAccounterGeneral.UP_BOTTOM_RIGHT_PRESSED, innerShadow);
				}
				else
					innerShadow = dataCache.getElements().get(PocketAccounterGeneral.UP_BOTTOM_RIGHT_PRESSED);
				innerShadowPaint.setAlpha(0x66);
				canvas.drawBitmap(innerShadow, container.left, container.top, innerShadowPaint);
			}
			break;
		}
		bitmapPaint.setAlpha(0xFF);
		if (boardButton.getCategoryId() != null) {
			String name = "";
			switch (boardButton.getType()) {
				case PocketAccounterGeneral.CATEGORY:
					RootCategory category = null;
					List<RootCategory> categoryList = daoSession.getRootCategoryDao().loadAll();
					for (RootCategory cat : categoryList) {
						if (cat.getId().equals(boardButton.getCategoryId())) {
							category = cat;
							break;
						}
					}
					name = category.getName();
					if (dataCache.getBoardBitmapsCache().get(boardButton.getId()) == null) {
						int resId = context.getResources().getIdentifier(category.getIcon(), "drawable", context.getPackageName());
						scaled = BitmapFactory.decodeResource(context.getResources(), resId, options);
						scaled = Bitmap.createScaledBitmap(scaled, thirtyDp, thirtyDp, true);
						dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
					}
					else
						scaled = dataCache.getBoardBitmapsCache().get(boardButton.getId());
					break;
				case PocketAccounterGeneral.CREDIT:
					CreditDetials creditDetials = daoSession.getCreditDetialsDao().load(Long.parseLong(boardButton.getCategoryId()));
					if(creditDetials!=null){
						name=creditDetials.getCredit_name();
					}
					if (dataCache.getBoardBitmapsCache().get(boardButton.getId()) == null) {
						int resId = context.getResources().getIdentifier(creditDetials.getIcon_ID(), "drawable", context.getPackageName());
						scaled = BitmapFactory.decodeResource(context.getResources(), resId, options);
						scaled = Bitmap.createScaledBitmap(scaled, thirtyDp, thirtyDp, true);
						dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
					}
					else
						scaled = dataCache.getBoardBitmapsCache().get(boardButton.getId());
					break;
				case PocketAccounterGeneral.DEBT_BORROW:
					Query<DebtBorrow> query = daoSession.getDebtBorrowDao()
							.queryBuilder()
							.where(DebtBorrowDao.Properties.Id.eq(boardButton.getCategoryId()))
							.build();
					DebtBorrow debtBorrow = null;
					if (!query.list().isEmpty()) {
						debtBorrow = query.list().get(0);
						name = debtBorrow.getPerson().getName();
					}
					if (dataCache.getBoardBitmapsCache().get(boardButton.getId()) == null) {
						if (!debtBorrow.getPerson().getPhoto().equals("")) {
							try {
								scaled = queryContactImage(Integer.parseInt(debtBorrow.getPerson().getPhoto()));
							}
							catch (NumberFormatException e) {
								scaled = BitmapFactory.decodeFile(debtBorrow.getPerson().getPhoto());
							}
						}
						if (scaled == null)
							scaled = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_photo, options);
						scaled = Bitmap.createScaledBitmap(scaled, thirtyDp, thirtyDp, true);
						dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
					}
					else
						scaled = dataCache.getBoardBitmapsCache().get(boardButton.getId());
					break;
				case PocketAccounterGeneral.FUNCTION:
					String icon = null;
					for (int i = 0; i < operationIds.length; i++) {
						if (operationIds[i].equals(boardButton.getCategoryId())) {
							icon = operationIcons[i];
							name = operationNames[i];
							break;
						}
					}
					if (dataCache.getBoardBitmapsCache().get(boardButton.getId()) == null) {
						int id = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
						scaled = BitmapFactory.decodeResource(context.getResources(), id, options);
						scaled = Bitmap.createScaledBitmap(scaled, thirtyDp, thirtyDp, true);
						dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
					}
					else
						scaled = dataCache.getBoardBitmapsCache().get(boardButton.getId());
					break;
				case PocketAccounterGeneral.PAGE:
					icon = null;
					for (int i = 0; i < pageIds.length; i++) {
						if (pageIds[i].equals(boardButton.getCategoryId())) {
							icon = pageIcons[i];
							name = pageNames[i];
							break;
						}
					}
					if (dataCache.getBoardBitmapsCache().get(boardButton.getId()) == null) {
						int id = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
						scaled = BitmapFactory.decodeResource(context.getResources(), id, options);
						scaled = Bitmap.createScaledBitmap(scaled, thirtyDp, thirtyDp, true);
						dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
					}
					else
						scaled = dataCache.getBoardBitmapsCache().get(boardButton.getId());
					break;
			}
			canvas.drawBitmap(scaled, container.centerX()-scaled.getWidth()/2, container.centerY()-scaled.getHeight(), bitmapPaint);
			Paint textPaint = new Paint();
			textPaint.setColor(toolbarTextColor);
			textPaint.setTextSize(context.getResources().getDimension(R.dimen.eleven_dp));
			textPaint.setAntiAlias(true);
			Rect bounds = new Rect();
			if (name.length()>=11) {
				for (int i=10; i < name.length(); i++) {
					textPaint.getTextBounds(name, 0, i, bounds);
					if (bounds.width() >= container.width()-aLetterHeight) {
						name = name.substring(0, i-3);
						name += "...";
						break;
					}
				}
			}
			textPaint.getTextBounds(name, 0, name.length(), bounds);
			canvas.drawText(name, container.centerX()-bounds.width()/2, container.centerY()+2*aLetterHeight, textPaint);

		} else {
			if (dataCache.getBoardBitmapsCache().get(boardButton.getId()) == null) {
				scaled = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_category, options);
				scaled = Bitmap.createScaledBitmap(scaled, thirtyDp, thirtyDp, true);
				dataCache.getBoardBitmapsCache().put(boardButton.getId(), scaled);
			}
			else
				scaled = dataCache.getBoardBitmapsCache().get(boardButton.getId());
			canvas.drawBitmap(scaled, container.centerX()-scaled.getWidth()/2, container.centerY()-scaled.getHeight(), bitmapPaint);
			Paint textPaint = new Paint();
			textPaint.setColor(toolbarTextColor);
			textPaint.setTextSize(tenSp);
			String text = context.getResources().getString(R.string.add);
			textPaint.setAntiAlias(true);
			Rect bounds = new Rect();
			textPaint.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, container.centerX()-bounds.width()/2, container.centerY()+2*aLetterHeight, textPaint);
		}
	}
	private Bitmap queryContactImage(int imageDataRow) {
		Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{
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
	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}
	public RectF getContainer() {
		return container;
	}
	public void setCategory(BoardButton boardButton) {this.boardButton = boardButton;}
	public BoardButton getCategory() {return boardButton; }
}
