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
	
	private final int posterWidth, posterHeight;
	private final Rect posterRect;
	
	public OneLabelItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection, boolean fixedSize) {
		super(context, manager, width, defaultCover, selection, ThumbSize.SMALL, fixedSize);
		posterWidth = ThumbSize.getPixel(ThumbSize.SMALL, fixedSize);
		posterHeight = posterWidth;
		posterRect = new Rect(0, 0, posterWidth, posterHeight);
	}
	
	public OneLabelItemView(Context context, int width, Bitmap defaultCover, Drawable selection, boolean fixedSize) {
		this(context, null, width, defaultCover, selection, fixedSize);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, posterHeight);
	}
	
	protected void onDraw(Canvas canvas) {
		final int width = mWidth;
		PAINT.setTextAlign(Align.LEFT);
		
		drawPoster(canvas, posterWidth, posterHeight, width);

		// label
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setColor(isSelected() || isPressed() ? Color.WHITE : Color.BLACK);
			PAINT.setTextSize(size18);
			canvas.drawText(title, posterWidth + padding, size35, PAINT);
		}
	}

	@Override
	protected Rect getPosterRect() {
		return posterRect;
	}
}