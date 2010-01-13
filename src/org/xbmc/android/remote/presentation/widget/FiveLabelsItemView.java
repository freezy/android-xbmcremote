package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.IManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;

public class FiveLabelsItemView extends AbstractItemView {
	
	public String subtitle;
	public String subtitleRight;
	public String bottomtitle;
	public String bottomright;
	
	public FiveLabelsItemView(Context context, IManager manager) {
		super(context, manager);
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(320, 73);
//		super.onMeasure(150, 50);
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
}