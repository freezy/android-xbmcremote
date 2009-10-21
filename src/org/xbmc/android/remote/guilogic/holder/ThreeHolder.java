package org.xbmc.android.remote.guilogic.holder;

import org.xbmc.android.backend.httpapi.HttpApiHandler;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.drawable.CrossFadeDrawable;
import org.xbmc.android.widget.IdleListDetector;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.type.CacheType;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

public class ThreeHolder<T> {

	public long id = 0;

	private final ImageView mIconView;
	private final TextView mTitleView;
	private final TextView mSubtitleView;
	private final TextView mSubsubtitleView;
	
	private T mHolderItem;
	private ICoverArt mCoverItem;

	boolean tempBind;
	public CrossFadeDrawable transition;

	public ThreeHolder(ImageView icon, TextView title, TextView subtitle, TextView subsubtitle) {
		mIconView = icon;
		mTitleView = title;
		mSubtitleView = subtitle;
		mSubsubtitleView = subsubtitle;
	}
	
	public CoverDownloadHandler getCoverDownloadHandler(Activity activity, IdleListDetector idler) {
		return new CoverDownloadHandler(activity, idler);
	}
	
	public void setHolderItem(T item) {
		mHolderItem = item;
	}
	public T getHolderItem() {
		return mHolderItem;
	}
	
	public void setCoverItem(ICoverArt cover) {
		mCoverItem = cover;
	}
	
	public ICoverArt getCoverItem() {
		return mCoverItem;
	}

	public class CoverDownloadHandler extends HttpApiHandler<Bitmap> {
		private final IdleListDetector mIdler;
		public CoverDownloadHandler(Activity activity, IdleListDetector idler) {
			super(activity, id);
			mIdler = idler;
		}
		public void run() {
			/* mTag is the id of the album that finished downloading. holder.id
			 * is the id of the current view. must be equal,
			 * otherwise that means that we already scrolled further and the
			 * downloaded view isn't visible anymore.
			 */
			if (mTag == id) {
				if (value == null) {
					setImageResource(R.drawable.icon_album);
				} else {
					// only "fade" if cover was downloaded.
					if (mCacheType != null && mCacheType.equals(CacheType.network)) {
						CrossFadeDrawable transition = getTransitionDrawable();
						transition.setEnd(value);
						getImageLoaderView().setImageDrawable(transition);
						transition.startTransition(500);
					} else {
						mIconView.setImageBitmap(value);
					}
					setTemporaryBind(false);
				}
			}
		}

		public boolean postCache() {
			if (mIdler == null || mIdler.isListIdle()) {
				// download, list is idleing
				setTemporaryBind(false);
				return true;
			} else {
				// don't download, list is scrolling.
				setTemporaryBind(true);
				return false;
			}
		}
	}

	public void setText(String title, String subtitle, String subsubtitle) {
		mTitleView.setText(title);
		mSubtitleView.setText(subtitle);
		mSubsubtitleView.setText(subsubtitle);
	}

	public void setImageResource(int res) {
		mIconView.setImageResource(res);
	}

	public long getId() {
		return id;
	}

	public boolean isTemporaryBind() {
		return tempBind;
	}

	public void setTemporaryBind(boolean temp) {
		tempBind = temp;
	}

	public ImageView getImageLoaderView() {
		return mIconView;
	}

	public CrossFadeDrawable getTransitionDrawable() {
		return transition;
	}
}
