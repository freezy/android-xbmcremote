package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.IManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;

public class ThreeLabelsItemView extends AbstractItemView {

	public String subtitle;
	public String subsubtitle;

	public ThreeLabelsItemView(Context context, IManager manager) {
		super(context, manager);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(320, 50);
	}
	
	@Override
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
}