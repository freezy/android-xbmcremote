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

public class ThreeLabelsItemView extends AbstractItemView {

	public String subtitle;
	public String subsubtitle;
	
	private final int posterWidth, posterHeight;
	private final Rect posterRect;

	public ThreeLabelsItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection, boolean fixedSize) {
		super(context, manager, width, defaultCover, selection, ThumbSize.SMALL, fixedSize);

		posterWidth = ThumbSize.getPixel(ThumbSize.SMALL, fixedSize);
		posterHeight = posterWidth;
		posterRect = new Rect(0, 0, posterWidth, posterHeight);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, posterHeight);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		final int width = mWidth;
		PAINT.setTextAlign(Align.LEFT);
		
		drawPoster(canvas, posterWidth, posterHeight, width);
		
		// text
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setColor(isSelected() || isPressed() ? Color.WHITE : Color.BLACK);
			PAINT.setTextSize(size18);
			canvas.drawText(ellipse(title, width - size50 - padding), posterWidth + padding, size25, PAINT);
		}
		PAINT.setColor(isSelected() || isPressed() ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(size12);
		if (subtitle != null) {
			canvas.drawText(subtitle, posterWidth + padding, size42, PAINT);
		}
		if (subsubtitle != null) {
			PAINT.setTextAlign(Align.RIGHT);
			canvas.drawText(subsubtitle, width - padding, size42, PAINT);
		}
	}

	@Override
	protected Rect getPosterRect() {
		return posterRect;
	}
}