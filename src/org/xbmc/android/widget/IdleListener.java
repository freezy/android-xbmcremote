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

import android.util.Log;
import android.widget.AbsListView;

/**
 * Useful common implementation of OnListIdleListener which handles loading
 * images that temporarily defaulted during a fling. Utilizes a mem cache to
 * further enhance performance.
 */
public class IdleListener implements OnListIdleListener {

	private final static String TAG = "IdleListener";
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
			final AbstractItemView itemView = (AbstractItemView)mList.getChildAt(i);
			if (!itemView.hasBitmap()) {
				final Object obj = mList.getAdapter().getItem(itemView.getPosition());
				if(obj instanceof ICoverArt) {
					ICoverArt cover = (ICoverArt)obj;
					Log.i(TAG, "Cover: " + cover);
					itemView.getResponse().load(cover);
				}
			}
		}
		System.gc();
	}
}