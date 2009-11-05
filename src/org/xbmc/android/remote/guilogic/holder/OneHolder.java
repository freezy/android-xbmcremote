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

	public final ImageView iconView;
	public final TextView titleView;
	
	public T holderItem;
	public ICoverArt coverItem;

	boolean tempBind;
	public CrossFadeDrawable transition;

	public OneHolder(ImageView icon, TextView title) {
		iconView = icon;
		titleView = title;
	}
	
	public CoverDownloadHandler getCoverDownloadHandler(Activity activity, IdleListDetector idler) {
		return new CoverDownloadHandler(activity, idler);
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
					iconView.setImageResource(R.drawable.icon_album);
				} else {
					// only "fade" if cover was downloaded.
					if (mCacheType != null && mCacheType.equals(CacheType.network)) {
						CrossFadeDrawable t = transition;
						t.setEnd(value);
						iconView.setImageDrawable(t);
						t.startTransition(500);
					} else {
						iconView.setImageBitmap(value);
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

	public ICoverArt getCoverItem() {
		return coverItem;
	}

	public boolean isTemporaryBind() {
		return tempBind;
	}

	public void setTemporaryBind(boolean temp) {
		tempBind = temp;
	}
}
