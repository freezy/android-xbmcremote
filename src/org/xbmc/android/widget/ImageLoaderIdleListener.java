/*
 * Code taken from jasta's five project:
 *   http://code.google.com/p/five/
 *
 * Much of this logic was taken from Romain Guy's Shelves project:
 *   http://code.google.com/p/shelves/
 */

package org.xbmc.android.widget;

import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.guilogic.holder.ThreeHolder;
import org.xbmc.android.widget.IdleListDetector.OnListIdleListener;
import org.xbmc.httpapi.data.Album;
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
public class ImageLoaderIdleListener implements OnListIdleListener {
	private final Activity mActivity;

	private final AbsListView mList;
//	private final ArrayAdapter<Album> mAdapter;

//	private static final int TRANSITION_DURATION = 175;

	public ImageLoaderIdleListener(Activity activity, AbsListView list) {
		mActivity = activity;
		mList = list;
//		mAdapter = (ArrayAdapter<Album>) list.getAdapter();

	}

	@SuppressWarnings("unchecked")
	public void onListIdle() {
		int n = mList.getChildCount();
		Log.i("ImageLoaderIdleListener", "IDLEING, downloading covers");
		for (int i = 0; i < n; i++) {
			View row = mList.getChildAt(i);
			final ThreeHolder<Album> holder = (ThreeHolder<Album>)row.getTag();
			if (holder.isTemporaryBind()) {
				Log.i("ImageLoaderIdleListener", "Album: " + holder.getItem());
				HttpApiThread.music().getAlbumCover(holder.getCoverDownloadHandler(mActivity, null), holder.getItem(), ThumbSize.small);
				holder.setTemporaryBind(false);
			}
		}
//		mList.invalidate();
	}
}
