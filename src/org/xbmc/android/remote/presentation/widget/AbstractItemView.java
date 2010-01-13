package org.xbmc.android.remote.presentation.widget;

import org.xbmc.android.remote.R;
import org.xbmc.api.business.CoverResponse;
import org.xbmc.api.business.IManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.view.View;

public abstract class AbstractItemView extends View {

	protected final static Paint PAINT = new Paint();
	
	protected static Bitmap sDefaultCover;
	
	private final CoverResponse mResponse;
	protected Bitmap mCover;
	
	public int position;
	public String title;
	
	public AbstractItemView(Context context, IManager manager) {
		super(context);
		mResponse = new CoverResponse(this, manager);
		sDefaultCover = BitmapFactory.decodeResource(getResources(), R.drawable.icon_album_dark);
	}
	public AbstractItemView(Context context, int iconResourceId) {
		super(context);
		mResponse = null;
		sDefaultCover = BitmapFactory.decodeResource(getResources(), iconResourceId);
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