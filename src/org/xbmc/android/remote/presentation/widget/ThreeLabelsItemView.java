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
import android.graphics.Paint.Align;
import android.view.View;

public class ThreeLabelsItemView extends View implements IItemView {
	
	private static Bitmap sDefaultCover;
	
	private final static Paint PAINT = new Paint();
	
	private final CoverResponse mResponse;
	private Bitmap mCover;
	
	public String title;
	public String subtitle;
	public String subsubtitle;
	public int position;

	public ThreeLabelsItemView(Context context, IManager manager) {
		super(context);
		mResponse = new CoverResponse(this, manager);
		if (sDefaultCover == null) {
			sDefaultCover = BitmapFactory.decodeResource(getResources(), R.drawable.icon_album_dark);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(320, 50);
//		super.onMeasure(150, 50);
	}
	
	public void reset() {
		mCover = null;
	}
	
	public void setCover(Bitmap cover) {
		mCover = cover;
		invalidate();
	}
	
	protected void onDraw(Canvas canvas) {
		PAINT.setTextAlign(Align.LEFT);
		PAINT.setColor(Color.WHITE);
		if (mCover != null) {
			canvas.drawBitmap(mCover, 0.0f, 0.0f, null);
		} else {
			canvas.drawBitmap(sDefaultCover, 0.0f, 0.0f, null);
		}
		canvas.drawRect(50, 0, getWidth(), 50, PAINT);
		PAINT.setAntiAlias(true);
		if (title != null) {
			PAINT.setColor(Color.BLACK);
			PAINT.setTextSize(18);
			canvas.drawText(title, 55, 25, PAINT);
		}
		PAINT.setColor(Color.rgb(80, 80, 80));
		PAINT.setTextSize(12);
		if (subtitle != null) {
			canvas.drawText(subtitle, 55, 42, PAINT);
		}
		if (subsubtitle != null) {
			PAINT.setTextAlign(Align.RIGHT);
			canvas.drawText(subsubtitle, 315, 42, PAINT);
		}
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