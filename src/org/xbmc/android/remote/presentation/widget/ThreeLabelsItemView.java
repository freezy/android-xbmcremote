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
	
	private final static int POSTER_WIDTH = ThumbSize.getPixel(ThumbSize.SMALL);;
	private final static int POSTER_HEIGHT = POSTER_WIDTH;
	private final static Rect POSTER_RECT = new Rect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);

	public ThreeLabelsItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection) {
		super(context, manager, width, defaultCover, selection, ThumbSize.SMALL);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, POSTER_HEIGHT);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		final int width = mWidth;
		PAINT.setTextAlign(Align.LEFT);
		
		drawPoster(canvas, POSTER_WIDTH, POSTER_HEIGHT, width);
		
		// text
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setColor(isSelected() || isPressed() ? Color.WHITE : Color.BLACK);
			PAINT.setTextSize(SIZE18);
			canvas.drawText(ellipse(title, width - SIZE50 - PADDING), POSTER_WIDTH + PADDING, SIZE25, PAINT);
		}
		PAINT.setColor(isSelected() || isPressed() ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(SIZE12);
		if (subtitle != null) {
			canvas.drawText(subtitle, POSTER_WIDTH + PADDING, SIZE42, PAINT);
		}
		if (subsubtitle != null) {
			PAINT.setTextAlign(Align.RIGHT);
			canvas.drawText(subsubtitle, width - PADDING, SIZE42, PAINT);
		}
	}

	@Override
	protected Rect getPosterRect() {
		return POSTER_RECT;
	}
}