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
	
	protected final int padding;
	protected final int size12, size18, size20, size25, size35, size42, size50, size55, size59, size65, size103;
	
	public final static int MSG_UPDATE_COVER = 1;
	
	protected static Bitmap sSelected;
	
	private final CoverResponse mResponse;
	private final Bitmap mDefaultCover;
	protected Bitmap mCover;
	protected final int mWidth;
	protected final Drawable mSelection;
	protected int mDefaultColor = Color.WHITE;
	
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
	
	public AbstractItemView(Context context, IManager manager, int width, Bitmap defaultCover, Drawable selection, int thumbSize, boolean fixedSize) {
		super(context);

		if (manager != null) {
			mResponse = new CoverResponse(context, manager, defaultCover, thumbSize, mHandler);
		} else {
			mResponse = null;
		}
		
		mWidth = width;
		mDefaultCover = defaultCover;
		mSelection = selection;
		
		final float screenScale = fixedSize ? 1 : ThumbSize.SCREEN_SCALE;
		padding = (int)(5 * ThumbSize.PIXEL_SCALE * screenScale);
		size12 = (int)(12 * ThumbSize.PIXEL_SCALE * screenScale);
		size18 = (int)(18 * ThumbSize.PIXEL_SCALE * screenScale);
		size20 = (int)(20 * ThumbSize.PIXEL_SCALE * screenScale);
		size25 = (int)(25 * ThumbSize.PIXEL_SCALE * screenScale);	
		size35 = (int)(35 * ThumbSize.PIXEL_SCALE * screenScale);	
		size42 = (int)(42 * ThumbSize.PIXEL_SCALE * screenScale);
		size50 = (int)(50 * ThumbSize.PIXEL_SCALE * screenScale);
		size55 = (int)(55 * ThumbSize.PIXEL_SCALE * screenScale);
		size59 = (int)(59 * ThumbSize.PIXEL_SCALE * screenScale);
		size65 = (int)(65 * ThumbSize.PIXEL_SCALE * screenScale);
		size103 = (int)(103 * ThumbSize.PIXEL_SCALE * screenScale);		
	}
	
	public AbstractItemView(Context context, int width, Bitmap defaultCover, Drawable selection, boolean fixedSize) {
		this(context, null, width, defaultCover, selection, 0, fixedSize);
	}
	
	protected void drawPoster(Canvas canvas, int posterWidth, int posterHeight, int canvasWidth) {
		// background
		if ((isSelected() || isPressed()) && mSelection != null) {
			mSelection.setBounds(posterWidth, 0, canvasWidth, posterHeight);
			mSelection.draw(canvas);
		} else {
			PAINT.setColor(mDefaultColor);
			canvas.drawRect(posterWidth, 0, canvasWidth, posterHeight, PAINT);
		}
		
		// poster
		Bitmap cover = mCover;
		if(mCover == null || mCover.isRecycled())
		{
			cover = mDefaultCover;
		}
		
		if (cover != null && !cover.isRecycled()) {
			int dx = (cover.getWidth() - posterWidth) / 2;  
			int dy = (cover.getHeight() - posterHeight) / 2;
			Rect src = null;
			if(dx > 0 || dy > 0) {
					src = new Rect(dx, dy, dx + posterWidth, dy + posterHeight);
				}
			Rect dst = getPosterRect();
			PAINT.setColor(mDefaultColor);
			canvas.drawRect(0, 0, posterWidth, posterHeight, PAINT);
			canvas.drawBitmap(cover, src, dst, PAINT);
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