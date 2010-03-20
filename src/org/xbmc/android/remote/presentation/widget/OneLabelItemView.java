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

public class OneLabelItemView extends AbstractItemView {
	
	private final static int POSTER_WIDTH = ThumbSize.getPixel(ThumbSize.SMALL);
	private final static int POSTER_HEIGHT = POSTER_WIDTH;
	private final static Rect POSTER_RECT = new Rect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);
	
	public OneLabelItemView(Context context, int width, Bitmap defaultCover, Drawable selection) {
		super(context, width, defaultCover, selection);
	}
	
	public OneLabelItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection) {
		super(context, manager, width, defaultCover, selection);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, POSTER_HEIGHT);
	}
	
	protected void onDraw(Canvas canvas) {
		final int width = mWidth;
		PAINT.setTextAlign(Align.LEFT);
		
		drawPoster(canvas, POSTER_WIDTH, POSTER_HEIGHT, width);

		// label
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setColor(isSelected() || isPressed() ? Color.WHITE : Color.BLACK);
			PAINT.setTextSize(SIZE18);
			canvas.drawText(title, POSTER_WIDTH + PADDING, SIZE35, PAINT);
		}
	}

	@Override
	protected Rect getPosterRect() {
		return POSTER_RECT;
	}
}