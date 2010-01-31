package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.IManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint.Align;

public class OneLabelItemView extends AbstractItemView {
	
	private final static int POSTER_WIDTH = 50;
	private final static int POSTER_HEIGHT = 50;
	private final static int PADDING = 5;
	private final static Rect POSTER_RECT = new Rect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);
	
	public OneLabelItemView(Context context, int iconResourceId) {
		super(context, iconResourceId);
	}
	
	public OneLabelItemView(Context context, IManager manager, int width) {
		super(context, manager, width);
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
			PAINT.setColor(Color.BLACK);
			PAINT.setTextSize(18);
			canvas.drawText(title, POSTER_WIDTH + PADDING, 35, PAINT);
		}
	}

	@Override
	protected Rect getPosterRect() {
		return POSTER_RECT;
	}
}