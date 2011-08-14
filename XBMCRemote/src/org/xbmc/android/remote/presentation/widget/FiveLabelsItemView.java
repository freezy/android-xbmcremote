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

public class FiveLabelsItemView extends AbstractItemView {
	
	private final int posterWidth, posterHeight;
	private final Rect posterRect;
	
	public Bitmap posterOverlay;
	public String subtitle;
	public String subtitleRight;
	public String bottomtitle;
	public String bottomright;
	
	public FiveLabelsItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection, boolean fixedSize) {
		super(context, manager, width, defaultCover, selection, ThumbSize.SMALL, fixedSize);
		
		posterWidth = ThumbSize.getPixel(ThumbSize.SMALL, fixedSize);
		posterHeight = (int)(posterWidth * ThumbSize.POSTER_AR);;
		posterRect = new Rect(0, 0, posterWidth, posterHeight);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, posterHeight);
	}
	
	protected void onDraw(Canvas canvas) {
		drawPoster(canvas, posterWidth, posterHeight, mWidth);
		drawPosterOverlay(canvas, posterWidth, posterHeight);
		drawPortraitText(canvas);
	}
	
	protected void drawPosterOverlay(Canvas canvas, int posterWidth, int posterHeight) {
		if (posterOverlay != null) {
			final int w = posterOverlay.getWidth();
			final int h = posterOverlay.getHeight();
			int dx = posterWidth - w;
			int dy = posterHeight - h;
			canvas.drawBitmap(posterOverlay, dx, dy, null);
		}		
	}

	/**
	 * Draws text labels for portrait text view
	 * <pre>
	 * ,-----. 
	 * |     | title (big)
	 * |     | subtitle           subtitleRight
	 * |     | bottomtitle          bottomRight
	 * `-----'
	 * </pre>
	 * @param canvas Canvas to draw on
	 */
	protected void drawPortraitText(Canvas canvas) {
		final int width = mWidth;
		final boolean isSelected = isSelected() || isPressed();
		
		// init paint
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setColor(Color.WHITE);
		PAINT.setFakeBoldText(false);
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setAntiAlias(true);
		
		// title
		if (title != null) {
			PAINT.setColor(isSelected ? Color.WHITE : Color.BLACK);
			PAINT.setTextSize(size18);
			canvas.drawText(title, posterWidth + padding, size25, PAINT);
		}
		
		// subtitle right
		PAINT.setColor(isSelected ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(size12);
		PAINT.setTextAlign(Align.RIGHT);
		int subtitleRightWidth = 0;
		if (subtitleRight != null) {
			subtitleRightWidth = (int)PAINT.measureText(subtitleRight);
			canvas.drawText(subtitleRight, width - padding, size42, PAINT);
		}
		
		// bottom right
		int bottomrightWidth = 0;
		if (bottomright != null) {
			PAINT.setTextSize(size20);
			PAINT.setFakeBoldText(true);
			PAINT.setColor(isSelected ? Color.WHITE : Color.argb(68, 0, 0, 0));
			bottomrightWidth = (int)PAINT.measureText(subtitleRight);
			canvas.drawText(bottomright, width - padding, size65, PAINT);
		}
		
		// subtitle
		PAINT.setColor(isSelected ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(size12);
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setFakeBoldText(false);
		if (subtitle != null) {
			canvas.drawText(ellipse(subtitle, width - subtitleRightWidth - size50 - (3 * padding)), size55, size42, PAINT);
		}
		if (bottomtitle != null) {
			canvas.drawText(ellipse(bottomtitle, width - bottomrightWidth - size50 - (3 * padding)), size55, size59, PAINT);
		}
	}
	
	@Override
	protected Rect getPosterRect() {
		return posterRect;
	}
}