package org.xbmc.android.remote.presentation.widget;

import org.xbmc.android.remote.R;
import org.xbmc.api.business.CoverResponse;
import org.xbmc.api.business.IManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public abstract class AbstractItemView extends View {

	protected final static Paint PAINT = new Paint();
	
	protected static Bitmap sSelected;
	
	private final CoverResponse mResponse;
	private final Bitmap mDefaultCover;
	protected Bitmap mCover;
	protected final int mWidth;
	
	public int position;
	public String title;
	
	protected abstract Rect getPosterRect();
	
	public AbstractItemView(Context context, IManager manager, int width, Bitmap defaultCover) {
		super(context);
		mResponse = new CoverResponse(this, manager, defaultCover);
		mWidth = width;
		mDefaultCover = defaultCover;
		sSelected = BitmapFactory.decodeResource(getResources(), R.drawable.selected);
	}
	
	public AbstractItemView(Context context, int width, Bitmap defaultCover) {
		super(context);
		mResponse = null;
		mWidth = width;
		mDefaultCover = defaultCover;
	}
	
	protected void drawPoster(Canvas canvas, int posterWidth, int posterHeight, int canvasWidth) {

		// background
		if ((isSelected() || isPressed()) && sSelected != null && !sSelected.isRecycled()) {
			canvas.drawBitmap(sSelected, null, new Rect(posterWidth, 0, canvasWidth, posterHeight), PAINT);
		} else {
			PAINT.setColor(Color.WHITE);
			canvas.drawRect(posterWidth, 0, canvasWidth, posterHeight, PAINT);
		}
		
		// poster
		if (mCover != null) {
			final int w = mCover.getWidth();
			final int h = mCover.getHeight();
			int dx = 0;
			int dy = 0;
			if (w > posterWidth) {
				dx = (w - posterWidth) / 2;  
			}
			if (h > posterHeight) {
				dy = (h - posterHeight) / 2;  
			}
			if (dx > 0 || dy > 0) {
				canvas.drawBitmap(mCover, new Rect(dx, dy, dx + posterWidth, dy + posterHeight), getPosterRect(), PAINT);
			} else {
				canvas.drawBitmap(mCover, 0.0f, 0.0f, null);
			}
		} else {
			PAINT.setColor(Color.WHITE);
			canvas.drawRect(0, 0, posterWidth, posterHeight, PAINT);
			canvas.drawBitmap(mDefaultCover, 0.0f, 0.0f, null);
		}
	}
	
	protected String ellipse(String text, int width) {
		if (PAINT.measureText(text) <= width) {
			return text;
		}
		for (int i = text.length(); i >= 0; i--) {
			if (PAINT.measureText(text.substring(0, i).concat("...")) <= width) {
				return text.substring(0, text.charAt(i - 1) == ' ' ? i - 1 : i).concat("...");
			}
		}
		return "";
	}
	
	public void reset() {
		mCover = null;
	}
	
	public void setCover(Bitmap cover) {
		mCover = cover;
		invalidate();
	}
	
	public int getPosition() {
		return position;
	}

	public boolean hasBitmap() {
		return mCover != null;
	}

	public CoverResponse getResponse() {
		return mResponse;
	}
}