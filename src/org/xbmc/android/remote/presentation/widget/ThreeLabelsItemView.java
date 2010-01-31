package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.IManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint.Align;

public class ThreeLabelsItemView extends AbstractItemView {

	public String subtitle;
	public String subsubtitle;
	
	private final static int POSTER_WIDTH = 50;
	private final static int POSTER_HEIGHT = 50;
	private final static int PADDING = 5;
	private final static Rect RECT_DEST = new Rect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);
	

	public ThreeLabelsItemView(Context context, IManager manager, int width) {
		super(context, manager, width);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, POSTER_HEIGHT);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		final int width = canvas.getWidth();
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setColor(Color.WHITE);
		
		// background
		if (isSelected() || isPressed()) {
			canvas.drawBitmap(sSelected, null, new Rect(POSTER_WIDTH, 0, width, POSTER_HEIGHT), PAINT);
		} else {
			canvas.drawRect(POSTER_WIDTH, 0, width, POSTER_HEIGHT, PAINT);
		}
		
		// cover
		if (mCover != null) {
			final int w = mCover.getWidth();
			final int h = mCover.getHeight();
			int dx = 0;
			int dy = 0;
			if (w > POSTER_WIDTH) {
				dx = (w - POSTER_WIDTH) / 2;  
			}
			if (h > POSTER_HEIGHT) {
				dy = (h - POSTER_HEIGHT) / 2;  
			}
			if (dx > 0 || dy > 0) {
				canvas.drawBitmap(mCover, new Rect(dx, dy, dx + POSTER_WIDTH, dy + POSTER_HEIGHT), RECT_DEST, PAINT);
			} else {
				canvas.drawBitmap(mCover, 0.0f, 0.0f, null);
			}
		} else {
			canvas.drawBitmap(sDefaultCover, 0.0f, 0.0f, null);
		}
		
		// text
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setColor(isSelected() || isPressed() ? Color.WHITE : Color.BLACK);
			PAINT.setTextSize(18);
			canvas.drawText(title, POSTER_WIDTH + PADDING, 25, PAINT);
		}
		PAINT.setColor(isSelected() || isPressed() ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(12);
		if (subtitle != null) {
			canvas.drawText(subtitle, POSTER_WIDTH + PADDING, 42, PAINT);
		}
		if (subsubtitle != null) {
			PAINT.setTextAlign(Align.RIGHT);
			canvas.drawText(subsubtitle, width - PADDING, 42, PAINT);
		}
	}
}