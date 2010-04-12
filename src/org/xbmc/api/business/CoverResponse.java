package org.xbmc.api.business;

import org.xbmc.android.remote.presentation.widget.AbstractItemView;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.type.ThumbSize;

import android.graphics.Bitmap;

public class CoverResponse extends DataResponse<Bitmap> {
	
	private final AbstractItemView mView;
	private final IManager mManager;
	private final Bitmap mDefaultCover;
	
	private boolean mIsLoading = false;
	private ICoverArt mMostRecentCover = null;
	
	public CoverResponse(AbstractItemView view, IManager manager, Bitmap defaultCover) {
		mView = view;
		mManager = manager;
		mDefaultCover = defaultCover;
	}
	
	public synchronized void load(ICoverArt cover, boolean getFromCacheOnly) {
		load(cover, ThumbSize.SMALL, getFromCacheOnly);
	}
	public synchronized void load(ICoverArt cover, int size, boolean getFromCacheOnly) {
		if (mIsLoading) {
			mMostRecentCover = cover;
		} else {
			mIsLoading = true;
			mMostRecentCover = null;
			mManager.getCover(this, cover, size, mDefaultCover, mView.getContext(), getFromCacheOnly);
		}
	}
	
	public synchronized void run() {
		if (mMostRecentCover == null) {
			mView.setCover(value);
			mIsLoading = false;
		} else {
			mManager.getCover(this, mMostRecentCover, ThumbSize.SMALL, mDefaultCover, mView.getContext(), false);
			mMostRecentCover = null;
		}
	}
}