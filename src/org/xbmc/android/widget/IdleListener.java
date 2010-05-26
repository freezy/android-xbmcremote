/*
 * Code taken from jasta's five project:
 *   http://code.google.com/p/five/
 *
 * Much of this logic was taken from Romain Guy's Shelves project:
 *   http://code.google.com/p/shelves/
 */

package org.xbmc.android.widget;

import org.xbmc.android.remote.presentation.widget.AbstractItemView;
import org.xbmc.android.widget.IdleListDetector.OnListIdleListener;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.type.ThumbSize;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Useful common implementation of OnListIdleListener which handles loading
 * images that temporarily defaulted during a fling. Utilizes a mem cache to
 * further enhance performance.
 */
public class IdleListener implements OnListIdleListener, OnScrollListener{

	private final static String TAG = "IdleListener";
	private final AbsListView mList;
	private final int mThumbSize;

	public IdleListener(AbsListView list, int thumbSize) {
		mList = list;
		mThumbSize = thumbSize;
	}
	
	public void onListIdle() {
		final AbsListView list = mList;
		int n = list.getChildCount();
		Log.i(TAG, "IDLEING, downloading covers");
		loadCovers(0, n);
	}
	
	private void loadCovers(final int start, final int visibleCount){
		final AbsListView list = mList;
		// try to garbage collect before and after idling.
		System.gc();
		for (int i = start; i < start+visibleCount; i++) {
			final AbstractItemView itemView = (AbstractItemView)list.getChildAt(i);
			if (!itemView.hasBitmap()) {
				ICoverArt cover = (ICoverArt)mList.getAdapter().getItem(itemView.getPosition());
				Log.i(TAG, "Cover: " + cover + " (" + ThumbSize.getDir(mThumbSize) + ")");
				itemView.getResponse().load(cover, mThumbSize, false);
			}
		}
		System.gc();
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState != OnScrollListener.SCROLL_STATE_FLING) {
			onListIdle();
		}
		
	}
}