package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.IManager;
import org.xbmc.api.type.ThumbSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;

public class GridPosterItemView extends AbstractItemView {
	
	private final static int POSTER_WIDTH = ThumbSize.getPixel(ThumbSize.MEDIUM);
	private final static int POSTER_HEIGHT = (int)((float)POSTER_WIDTH * ThumbSize.POSTER_AR);
	private final static Rect POSTER_RECT = new Rect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);
	
	public GridPosterItemView(Context context, int width, Bitmap defaultCover, Drawable selection) {
		super(context, width, defaultCover, selection);
	}
	
	public GridPosterItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection) {
		super(context, manager, width, defaultCover, selection, ThumbSize.MEDIUM);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(POSTER_WIDTH, POSTER_HEIGHT + SIZE25);
	}
	
	protected void onDraw(Canvas canvas) {
		final int width = mWidth;
		PAINT.setTextAlign(Align.LEFT);
		drawPoster(canvas, POSTER_WIDTH, POSTER_HEIGHT, width);
		
		// background
		PAINT.setColor(Color.BLACK);
		canvas.drawRect(0, POSTER_HEIGHT, POSTER_WIDTH, POSTER_HEIGHT + SIZE25, PAINT);
		
		// label
		PAINT.setColor(Color.WHITE);
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setTextAlign(Align.CENTER);
//			PAINT.setColor(isSelected() || isPressed() ? Color.BLACK : Color.WHITE);
			PAINT.setTextSize(SIZE18);
			canvas.drawText(title, POSTER_WIDTH / 2, POSTER_HEIGHT + SIZE18, PAINT);
		}
	}

	@Override
	protected Rect getPosterRect() {
		return POSTER_RECT;
	}
}