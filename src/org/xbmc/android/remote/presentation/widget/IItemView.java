package org.xbmc.android.remote.presentation.widget;

import org.xbmc.api.business.CoverResponse;

import android.graphics.Bitmap;

public interface IItemView {
	
	public void setCover(Bitmap bitmap);
	public CoverResponse getResponse();
	
	public int getPosition();
	public boolean hasBitmap();
	public Object getTag();
}