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
	
//	private static final String TAG = "FlexibleItemView";
	
	private final static int POSTER_WIDTH = ThumbSize.getPixel(ThumbSize.SMALL);;
	private final static int POSTER_HEIGHT = (int)(POSTER_WIDTH * ThumbSize.POSTER_AR);
	private final static Rect POSTER_RECT = new Rect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);
	
	public FlexibleItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection) {
		super(context, manager, width, defaultCover, selection);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mCover == null) {
			setMeasuredDimension(mWidth, POSTER_HEIGHT);
		} else {
			setMeasuredDimension(mWidth, mCover.getHeight());
		}
	}

	protected void onDraw(Canvas canvas) {
		if (mCover != null && !mCover.isRecycled()) {
			Dimension renderDim = ThumbSize.getDimension(ThumbSize.SMALL, MediaType.VIDEO, mCover.getWidth(), mCover.getHeight());
			drawPoster(canvas, renderDim.x, renderDim.y, mWidth);
			switch (renderDim.format) {
				case Dimension.SQUARE:
				case Dimension.PORTRAIT:
					drawPortrait(canvas);
					break;
				case Dimension.LANDSCAPE:
					drawLandscape(canvas, renderDim);
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
	private void drawLandscape(Canvas canvas, Dimension coverDim) {
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
			PAINT.setTextSize(SIZE18);
			canvas.drawText(ellipse(title, width - coverDim.x + PADDING), coverDim.x + PADDING, SIZE25, PAINT);
		}
		
		// subtitle right
		PAINT.setColor(isSelected ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(SIZE12);
		PAINT.setTextAlign(Align.RIGHT);
		float subtitleRightWidth = 0;
		if (subtitleRight != null) {
			subtitleRightWidth = PAINT.measureText(subtitleRight);
			canvas.drawText(subtitleRight, width - PADDING, SIZE42, PAINT);
		}

		// subtitle
		PAINT.setColor(isSelected ? Color.WHITE : Color.rgb(80, 80, 80));
		PAINT.setTextSize(SIZE12);
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setFakeBoldText(false);
		if (subtitle != null) {
			canvas.drawText(ellipse(subtitle, width - (int)subtitleRightWidth - SIZE50), coverDim.x + PADDING, SIZE42, PAINT);
		}
		
	}
	
	private void drawBanner(Canvas canvas) {
		
	}
	
	
	public void setCover(Bitmap cover) {
		mCover = cover;
		requestLayout();
		invalidate();
	}
	
	@Override
	protected Rect getPosterRect() {
		return POSTER_RECT;
	}
}