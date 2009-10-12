/*
 * Code taken from jasta's five project:
 *   http://code.google.com/p/five/
 *
 * Much of this logic was taken from Romain Guy's Shelves project:
 *   http://code.google.com/p/shelves/
 */

package org.xbmc.android.widget;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.backend.httpapi.HttpApiThread;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.drawable.CrossFadeDrawable;
import org.xbmc.android.widget.IdleListDetector.OnListIdleListener;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.type.ThumbSize;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
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
	private final Activity mActivity;

	private final AbsListView mList;
	private final ArrayAdapter<Album> mAdapter;


	private static final int TRANSITION_DURATION = 175;

	public ImageLoaderIdleListener(Activity activity, AbsListView list) {
		mActivity = activity;
		mList = list;
		mAdapter = (ArrayAdapter<Album>) list.getAdapter();

	}

	public void onListIdle() {
		int first = mList.getFirstVisiblePosition();
		int n = mList.getChildCount();
		Log.i("ImageLoaderIdleListener", "IDLEING, downloading covers");
		for (int i = 0; i < n; i++) {
			View row = mList.getChildAt(i);
			final ImageLoaderHolder holder = (ImageLoaderHolder) row.getTag();
			if (holder.isTemporaryBind()) {
				Log.i("ImageLoaderIdleListener", "Album: " + holder.getCover());
				HttpApiThread.music().getAlbumCover(new HttpApiHandler<Bitmap>(mActivity) {
					public void run() {
						if (value != null) {
							CrossFadeDrawable transition = holder.getTransitionDrawable();
							transition.setEnd(value);
							holder.getImageLoaderView().setImageDrawable(transition);
							transition.startTransition(500);
						} else {
							holder.getImageLoaderView().setImageResource(R.drawable.icon_album);
						}
					}
				}, holder.getCover(), ThumbSize.small);
				holder.setTemporaryBind(false);
			}
		}
//		mList.invalidate();
	}

	public interface ImageLoaderHolder {
		public String getItemId();
		
		public Album getCover();

		public boolean isTemporaryBind();

		public void setTemporaryBind(boolean temp);

		public ImageView getImageLoaderView();

		public CrossFadeDrawable getTransitionDrawable();
	}
}
