package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.CoverResponse;
import org.xbmc.api.business.IManager;
import org.xbmc.api.type.ThumbSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

public abstract class AbstractItemView extends View {

	protected final static Paint PAINT = new Paint();
	
	protected final static int PADDING = (int)(5 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE12 = (int)(12 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE18 = (int)(18 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE20 = (int)(20 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE25 = (int)(25 * ThumbSize.PIXEL_SCALE);	
	protected final static int SIZE35 = (int)(35 * ThumbSize.PIXEL_SCALE);	
	protected final static int SIZE42 = (int)(42 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE50 = (int)(50 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE55 = (int)(55 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE59 = (int)(59 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE65 = (int)(65 * ThumbSize.PIXEL_SCALE);
	protected final static int SIZE103 = (int)(103 * ThumbSize.PIXEL_SCALE);
	
	public final static int MSG_UPDATE_COVER = 1;
	
	protected static Bitmap sSelected;
	
	private final CoverResponse mResponse;
	private final Bitmap mDefaultCover;
	protected Bitmap mCover;
	protected final int mWidth;
	protected final Drawable mSelection;
	
	private final Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case MSG_UPDATE_COVER:
					setCover((Bitmap)msg.obj);
				break;
			}
		};
	};
	
	public int position;
	public String title;
	
	protected abstract Rect getPosterRect();
	
	public AbstractItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection, int thumbSize) {
		super(context);
		mResponse = new CoverResponse(context, manager, defaultCover, thumbSize, mHandler);
		mWidth = width;
		mDefaultCover = defaultCover;
		mSelection = selection;
//		sSelected = BitmapFactory.decodeResource(getResources(), R.drawable.selected);
	}
	
	public AbstractItemView(Context context, int width, Bitmap defaultCover, Drawable selection) {
		super(context);
		mResponse = null;
		mWidth = width;
		mDefaultCover = defaultCover;
		mSelection = selection;
	}
	
	protected void drawPoster(Canvas canvas, int posterWidth, int posterHeight, int canvasWidth) {

		// background
		if ((isSelected() || isPressed()) && mSelection != null) { // && sSelected != null && !sSelected.isRecycled()) {
//			canvas.drawBitmap(sSelected, null, new Rect(posterWidth, 0, canvasWidth, posterHeight), PAINT);
			mSelection.setBounds(posterWidth, 0, canvasWidth, posterHeight);
			mSelection.draw(canvas);
		} else {
			PAINT.setColor(Color.WHITE);
			canvas.drawRect(posterWidth, 0, canvasWidth, posterHeight, PAINT);
		}
		
		// poster
		if (mCover != null && !mCover.isRecycled()) {
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
			if (mDefaultCover != null && !mDefaultCover.isRecycled()) {
				PAINT.setColor(Color.WHITE);
				canvas.drawRect(0, 0, posterWidth, posterHeight, PAINT);
				canvas.drawBitmap(mDefaultCover, 0.0f, 0.0f, null);
			}
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