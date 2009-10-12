/*
 * $Id: ImageLoaderIdleListener.java 1077 2009-01-20 12:00:43Z jasta00 $
 *
 * Much of this logic was taken from Romain Guy's Shelves project:
 * 
 *   http://code.google.com/p/shelves/
 */

package org.xbmc.android.widget;

import org.devtcg.five.music.util.ImageMemCache;
import org.xbmc.android.remote.drawable.CrossFadeDrawable;
import org.xbmc.android.remote.drawable.FastBitmapDrawable;
import org.xbmc.android.widget.IdleListDetector.OnListIdleListener;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.ICoverArt;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Useful common implementation of OnListIdleListener which handles loading
 * images that temporarily defaulted during a fling. Utilizes a mem cache to
 * further enhance performance.
 */
public class ImageLoaderIdleListener implements OnListIdleListener {
	private final Context mContext;

	private final AbsListView mList;
	private final ArrayAdapter<Album> mAdapter;

	private final ImageMemCache mCache;

	private static final int TRANSITION_DURATION = 175;

	public ImageLoaderIdleListener(Context ctx, AbsListView list, ImageMemCache cache) {
		mContext = ctx;
		mList = list;
		mCache = cache;

		mAdapter = (ArrayAdapter<Album>) list.getAdapter();

	}

	public void onListIdle() {
		int first = mList.getFirstVisiblePosition();
		int n = mList.getChildCount();

		for (int i = 0; i < n; i++) {
			View row = mList.getChildAt(i);
			ImageLoaderHolder holder = (ImageLoaderHolder) row.getTag();

			if (holder.isTemporaryBind() == true) {
				FastBitmapDrawable d = mCache.fetchFromXbmc2(mContext, holder.getCover());

				if (d != mCache.getFallback()) {
					CrossFadeDrawable transition = holder.getTransitionDrawable();
					transition.setEnd(d.getBitmap());
					holder.getImageLoaderView().setImageDrawable(transition);
					transition.startTransition(TRANSITION_DURATION);
				}

				holder.setTemporaryBind(false);
			}
		}

		mList.invalidate();
	}

	public interface ImageLoaderHolder {
		public String getItemId();
		
		public ICoverArt getCover();

		public boolean isTemporaryBind();

		public void setTemporaryBind(boolean temp);

		public ImageView getImageLoaderView();

		public CrossFadeDrawable getTransitionDrawable();
	}
}
