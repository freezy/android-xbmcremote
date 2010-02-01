package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.IManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint.Align;

public class FiveLabelsItemView extends AbstractItemView {
	
	private final static int POSTER_WIDTH = 50;
	private final static int POSTER_HEIGHT = 73;
	private final static int PADDING = 5;
	private final static Rect POSTER_RECT = new Rect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);
	
	public String subtitle;
	public String subtitleRight;
	public String bottomtitle;
	public String bottomright;
	
	public FiveLabelsItemView(Context context, IManager manager, int width, Bitmap defaultCover) {
		super(context, manager, width, defaultCover);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, POSTER_HEIGHT);
	}
	
	protected void onDraw(Canvas canvas) {
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setColor(Color.WHITE);
		PAINT.setFakeBoldText(false);
		final int width = mWidth;
		PAINT.setTextAlign(Align.LEFT);
		
		drawPoster(canvas, POSTER_WIDTH, POSTER_HEIGHT, width);
		
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setColor(Color.BLACK);
			PAINT.setTextSize(18);
			canvas.drawText(title, POSTER_WIDTH + PADDING, 25, PAINT);
		}
		PAINT.setColor(Color.rgb(80, 80, 80));
		PAINT.setTextSize(12);
		
		PAINT.setTextAlign(Align.RIGHT);
		float subtitleRightWidth = 0;
		if (subtitleRight != null) {
			subtitleRightWidth = PAINT.measureText(subtitleRight);
			canvas.drawText(subtitleRight, width - PADDING, 42, PAINT);
		}
		float bottomrightWidth = 0;
		if (bottomright != null) {
			PAINT.setTextSize(20);
			PAINT.setFakeBoldText(true);
			PAINT.setColor(Color.argb(68, 0, 0, 0));
			bottomrightWidth = PAINT.measureText(subtitleRight);
			canvas.drawText(bottomright, width - PADDING, 65, PAINT);
		}
		
		PAINT.setColor(Color.rgb(80, 80, 80));
		PAINT.setTextSize(12);
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setFakeBoldText(false);
		if (subtitle != null) {
			canvas.drawText(ellipse(subtitle, width - (int)subtitleRightWidth - 50), 55, 42, PAINT);
		}
		if (bottomtitle != null) {
			canvas.drawText(ellipse(bottomtitle, width - (int)bottomrightWidth - 50), 55, 59, PAINT);
		}

	}

	@Override
	protected Rect getPosterRect() {
		return POSTER_RECT;
	}
}