package org.xbmc.android.remote.presentation.widget;

import org.xbmc.android.remote.R;
import org.xbmc.api.business.CoverResponse;
import org.xbmc.api.business.IManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

public class FiveLabelsItemView extends View implements IItemView {
	
	private static Bitmap sDefaultCover;
	
	private final static Paint PAINT = new Paint();
	
	private final CoverResponse mResponse;
	private Bitmap mCover;
	
	public String title;
	public String subtitle;
	public String subtitleRight;
	public String bottomtitle;
	public String bottomright;
	public int position;

	public FiveLabelsItemView(Context context, IManager manager) {
		super(context);
		mResponse = new CoverResponse(this, manager);
		if (sDefaultCover == null) {
			sDefaultCover = BitmapFactory.decodeResource(getResources(), R.drawable.poster);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(320, 73);
//		super.onMeasure(150, 50);
	}
	
	public void reset() {
		mCover = null;
	}
	
	public void setCover(Bitmap cover) {
		mCover = cover;
		invalidate();
	}
	
	protected void onDraw(Canvas canvas) {
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setColor(Color.WHITE);
		PAINT.setFakeBoldText(false);
		if (mCover != null) {
			canvas.drawBitmap(mCover, 0.0f, 0.0f, null);
		} else {
			canvas.drawBitmap(sDefaultCover, 0.0f, 0.0f, null);
		}
		canvas.drawRect(50, 0, getWidth(), 73, PAINT);
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setColor(Color.BLACK);
			PAINT.setTextSize(18);
			canvas.drawText(title, 55, 25, PAINT);
		}
		PAINT.setColor(Color.rgb(80, 80, 80));
		PAINT.setTextSize(12);
		
		PAINT.setTextAlign(Align.RIGHT);
		float subtitleRightWidth = 0;
		if (subtitleRight != null) {
			subtitleRightWidth = PAINT.measureText(subtitleRight);
			canvas.drawText(subtitleRight, 315, 42, PAINT);
		}
		float bottomrightWidth = 0;
		if (bottomright != null) {
			PAINT.setTextSize(20);
			PAINT.setFakeBoldText(true);
			PAINT.setColor(Color.argb(68, 0, 0, 0));
			bottomrightWidth = PAINT.measureText(subtitleRight);
			canvas.drawText(bottomright, 315, 65, PAINT);
		}
		
		PAINT.setColor(Color.rgb(80, 80, 80));
		PAINT.setTextSize(12);
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setFakeBoldText(false);
		if (subtitle != null) {
			canvas.drawText(ellipse(subtitle, 320 - (int)subtitleRightWidth - 50), 55, 42, PAINT);
		}
		if (bottomtitle != null) {
			canvas.drawText(ellipse(bottomtitle, 320 - (int)bottomrightWidth - 50), 55, 59, PAINT);
		}

	}
	
	public String ellipse(String text, int width) {
		if (PAINT.measureText(text) <= width) {
			return text;
		}
		for (int i = text.length(); i >= 0; i--) {
			if (PAINT.measureText(text.substring(0, i).concat("...")) <= width) {
				return text.substring(0, text.charAt(i - 1) == ' ' ? i - 1 : i).concat("...");
			}
		}
		return "";
	}

	public int getPosition() {
		return position;
	}

	public boolean hasBitmap() {
		return mCover != null;
	}

	public CoverResponse getResponse() {
		return mResponse;
	}
	
}