/*
 * Code taken from jasta's five project:
 *   http://code.google.com/p/five/
 *
 * Much of this logic was taken from Romain Guy's Shelves project:
 *   http://code.google.com/p/shelves/
 */

package org.xbmc.android.widget;

import org.xbmc.android.remote.presentation.controller.holder.AbstractHolder;
import org.xbmc.android.widget.IdleListDetector.OnListIdleListener;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

/**
 * Useful common implementation of OnListIdleListener which handles loading
 * images that temporarily defaulted during a fling. Utilizes a mem cache to
 * further enhance performance.
 */
public class IdleListener implements OnListIdleListener {

	private final static String TAG = "OnListIdleListener";
	private final AbsListView mList;

	public IdleListener(AbsListView list) {
		mList = list;
	}

	public void onListIdle() {
		int n = mList.getChildCount();
		Log.i(TAG, "IDLEING, downloading covers");
		// try to garbage collect before and after idling.
		System.gc();
		for (int i = 0; i < n; i++) {
			View row = mList.getChildAt(i);
			final AbstractHolder holder = (AbstractHolder)row.getTag();
			if (holder.tempBind) {
				Log.i(TAG, "Cover: " + holder.getCoverItem());
				// TODO FIX!!
//				mManager.getCover(holder.getCoverDownloadHandler(mPostScrollLoader), holder.getCoverItem(), ThumbSize.SMALL);
				holder.tempBind = false;
			}
		}
		System.gc();
//		mList.invalidate();
	}
}
