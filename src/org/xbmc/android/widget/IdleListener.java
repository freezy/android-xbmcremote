/*
 * Code taken from jasta's five project:
 *   http://code.google.com/p/five/
 *
 * Much of this logic was taken from Romain Guy's Shelves project:
 *   http://code.google.com/p/shelves/
 */

package org.xbmc.android.widget;

import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.controller.holder.AbstractHolder;
import org.xbmc.android.widget.IdleListDetector.OnListIdleListener;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

/**
 * Useful common implementation of OnListIdleListener which handles loading
 * images that temporarily defaulted during a fling. Utilizes a mem cache to
 * further enhance performance.
 */
public class IdleListener implements OnListIdleListener {
	private final Activity mActivity;

	private final AbsListView mList;
//	private final ArrayAdapter<Album> mAdapter;

//	private static final int TRANSITION_DURATION = 175;

	public IdleListener(Activity activity, AbsListView list) {
		mActivity = activity;
		mList = list;
//		mAdapter = (ArrayAdapter<Album>) list.getAdapter();

	}

	public void onListIdle() {
		int n = mList.getChildCount();
		Log.i("ImageLoaderIdleListener", "IDLEING, downloading covers");
		// try to garbage collect before and after idling.
		System.gc();
		for (int i = 0; i < n; i++) {
			View row = mList.getChildAt(i);
			final AbstractHolder holder = (AbstractHolder)row.getTag();
			if (holder.tempBind) {
				Log.i("ImageLoaderIdleListener", "Album: " + holder.getCoverItem());
				HttpApiThread.music().getCover(holder.getCoverDownloadHandler(mActivity, null), holder.getCoverItem(), ThumbSize.SMALL);
				holder.tempBind = false;
			}
		}
		System.gc();
//		mList.invalidate();
	}
}
