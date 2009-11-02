/*
 *      Copyright (C) 2005-2009 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

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

public class OneHolder<T> implements IHolder {

	public long id = 0;

	private final ImageView mIconView;
	private final TextView mTitleView;
	
	private T mHolderItem;
	private ICoverArt mCoverItem;

	boolean tempBind;
	public CrossFadeDrawable transition;

	public OneHolder(ImageView icon, TextView title) {
		mIconView = icon;
		mTitleView = title;
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

	public void setText(String title) {
		mTitleView.setText(title);
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
