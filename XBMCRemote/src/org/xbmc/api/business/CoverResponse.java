package org.xbmc.api.business;

import org.xbmc.android.remote.presentation.widget.AbstractItemView;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.type.ThumbSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

public class CoverResponse extends DataResponse<Bitmap> {
	
	private final Context mContext;
	private final IManager mManager;
	private final Bitmap mDefaultCover;
	private final int mThumbSize;
	private final Handler mHandler;
	
	private boolean mIsLoading = false;
	private ICoverArt mMostRecentCover = null;
	
	public CoverResponse(Context context, IManager manager, Bitmap defaultCover, int thumbSize, Handler handler) {
		mContext = context;
		mManager = manager;
		mDefaultCover = defaultCover;
		mThumbSize = thumbSize;
		mHandler = handler;
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
			mManager.getCover(this, cover, size, mDefaultCover, mContext, getFromCacheOnly);
		}
	}
	
	public synchronized void run() {
		if (mMostRecentCover == null) {
			mHandler.sendMessage(mHandler.obtainMessage(AbstractItemView.MSG_UPDATE_COVER, value));
			mIsLoading = false;
		} else {
			mManager.getCover(this, mMostRecentCover, mThumbSize, mDefaultCover, mContext, false);
			mMostRecentCover = null;
		}
	}
}