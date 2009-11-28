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

package org.xbmc.android.remote.presentation.controller.holder;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.drawable.CrossFadeDrawable;
import org.xbmc.android.widget.IdleListDetector;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.httpapi.type.CacheType;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

public abstract class AbstractHolder {
	
	/**
	 * Typically the CRC of the thumb. We need this in order to know if an image view
	 * should be updated after a download.
	 */
	public long id = 0;
	
	/**
	 * Resource ID of the fallback cover in case nothing is found.
	 */
	public int defaultCover = R.drawable.icon_album;
	
	/**
	 * Reference to the image view of the element
	 */
	public ImageView iconView;
	
	/**
	 * Reference to the transition object.
	 */
	public CrossFadeDrawable transition;
	
	/**
	 * The object containing the cover
	 */
	public ICoverArt coverItem;
	
	/**
	 * If true, a download has been started by the view, but not by the idle listener.
	 */
	public boolean tempBind;
	
	/**
	 * Cache the download handler object.
	 */
	private CoverDownloadHandler mCoverDownloadHandler;
	
	/**
	 * Construct with reference to activity and list idler.
	 * @param activity
	 * @param idler
	 * @return
	 */
	public CoverDownloadHandler getCoverDownloadHandler(Activity activity, IdleListDetector idler) {
		CoverDownloadHandler cdh = mCoverDownloadHandler;
		if (cdh == null) {
			cdh = new CoverDownloadHandler(activity, idler); 
		}
		return cdh;
	}
	
	public ICoverArt getCoverItem() {
		return coverItem;
	}
	
	public class CoverDownloadHandler extends DataResponse<Bitmap> {
		private final IdleListDetector mIdler;
		public CoverDownloadHandler(Activity activity, IdleListDetector idler) {
			super(activity, id, AbstractHolder.this.defaultCover);
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
					iconView.setImageResource(AbstractHolder.this.defaultCover);
				} else {
					// only "fade" if cover was downloaded.
					if (cacheType == CacheType.NETWORK) {
						CrossFadeDrawable t = transition;
						t.setEnd(value);
						iconView.setImageDrawable(t);
						t.startTransition(500);
					} else {
						iconView.setImageBitmap(value);
					}
					AbstractHolder.this.tempBind = false;
				}
			}
		}

		/**
		 * This is executed after every cache lookup. It avoids downloading 
		 * covers while the list is scrolling.
		 */
		public boolean postCache() {
			if (mIdler == null || mIdler.isListIdle()) {
				// download, list is idleing
				AbstractHolder.this.tempBind = false;
				return true;
			} else {
				// don't download, list is scrolling.
				AbstractHolder.this.tempBind = true;
				return false;
			}
		}
	}
}