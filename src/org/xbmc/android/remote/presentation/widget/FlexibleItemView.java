package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.IManager;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.api.type.ThumbSize.Dimension;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;

public class FlexibleItemView extends FiveLabelsItemView {
		private int POSTER_FORMAT = Dimension.PORTRAIT;
	
	public FlexibleItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection, boolean fixedSize) {
		super(context, manager, width, defaultCover, selection, fixedSize);
	}
	
	protected void onDraw(Canvas canvas) {
		if (mCover != null && !mCover.isRecycled()) {
			drawPoster(canvas, posterWidth, posterHeight, mWidth);
			drawPosterOverlay(canvas, posterWidth, posterHeight);
			switch (POSTER_FORMAT) {
				case Dimension.SQUARE:
				case Dimension.PORTRAIT:
					drawPortrait(canvas);
					break;
				case Dimension.LANDSCAPE:
					drawLandscape(canvas, posterWidth);
					break;
				case Dimension.BANNER:
					drawBanner(canvas);
					break;
				default:
			}
		} else {
			super.onDraw(canvas);
		}
	}
	
	private void drawPortrait(Canvas canvas) {
		drawPortraitText(canvas);
	}
	
	/**
	 * Draws text labels for landscape text view
	 * <pre>
	 * ,----------. 
	 * |          | title (big)
	 * |          | subtitle           subtitleRight
	 * `----------'
	 * </pre>
	 * @param canvas Canvas to draw on
	 */
	private void drawLandscape(Canvas canvas, int drawWidth) {
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
			canvas.drawText(ellipse(title, width - drawWidth + padding), drawWidth + padding, size25, PAINT);
		}
		
		// subtitle right
		PAINT.setColor(isSelected ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(size12);
		PAINT.setTextAlign(Align.RIGHT);
		float subtitleRightWidth = 0;
		if (subtitleRight != null) {
			subtitleRightWidth = PAINT.measureText(subtitleRight);
			canvas.drawText(subtitleRight, width - padding, size42, PAINT);
		}

		// subtitle
		PAINT.setColor(isSelected ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(size12);
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setFakeBoldText(false);
		if (subtitle != null) {
			canvas.drawText(ellipse(subtitle, width - (int)subtitleRightWidth - size50), drawWidth + padding, size42, PAINT);
		}
		
	}
	
	private void drawBanner(Canvas canvas) {
		
	}
	
	
	public void setCover(Bitmap cover) {
		mCover = cover;
		if(mCover != null)
		{
			Dimension renderDim = ThumbSize.getTargetDimension(ThumbSize.SMALL, MediaType.VIDEO, mCover.getWidth(), mCover.getHeight());
			posterWidth = renderDim.x;
			posterHeight = renderDim.y;
			posterRect = new Rect(0, 0, posterWidth, posterHeight);
			POSTER_FORMAT = renderDim.format;
		}
		else
		{
			posterWidth = ThumbSize.getPixel(ThumbSize.SMALL);
			posterHeight = (int)(posterWidth * ThumbSize.POSTER_AR);
			posterRect = new Rect(0, 0, posterWidth, posterHeight);
			POSTER_FORMAT = Dimension.PORTRAIT;
		}
		requestLayout();
		invalidate();
	}
}