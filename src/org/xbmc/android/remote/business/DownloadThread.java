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

package org.xbmc.android.remote.business;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * This thread asynchronously downloads thumbs from XBMC and returns them as
 * Bitmap.
 * 
 * When downloaded, the thumb is automatically saved to the memory- and disk-
 * cache for further usage.
 * 
 * @author Team XBMC
 */
class DownloadThread extends AbstractThread {
	
	/**
	 * Singleton instance of this thread
	 */
	protected static DownloadThread sHttpApiThread;
	
	private static final String TAG = "DownloadThread";
	private static final boolean DEBUG = AbstractManager.DEBUG;

	/**
	 * Constructor is protected, use get().
	 */
	protected DownloadThread() {
		super("HTTP API Network Thread");
	}
	
	/**
	 * Asynchronously downloads a thumb from XBMC and stores it locally.
	 * 
	 * @param response  Response object
	 * @param cover     Which cover to download
	 * @param thumbSize Which size to return
	 */
	public void getCover(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, final INotifiableController controller, final INotifiableManager manager, final Context context) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover != null) {
					if (DEBUG) Log.i(TAG, "Downloading cover " + cover);
					/* it can happen that the same cover is queued consecutively several
					 * times. that's why we check both the disk cache and memory cache if
					 * the cover is not already available from a previously queued download. 
					 */
					if (thumbSize < ThumbSize.BIG && MemCacheThread.isInCache(cover, thumbSize)) { // we're optimistic, let's check the memory first.
						if (DEBUG) Log.i(TAG, "Cover is now already in mem cache, directly returning...");
						response.value = MemCacheThread.getCover(cover, thumbSize);
						done(controller, response);
					} else if (thumbSize < ThumbSize.BIG && DiskCacheThread.isInCache(cover, thumbSize)) {
						if (DEBUG) Log.i(TAG, "Cover is not in mem cache anymore but still on disk, directly returning...");
						response.value = DiskCacheThread.getCover(cover, thumbSize);
						done(controller, response);
					} else {
						download(response, cover, thumbSize, controller, manager, context, true);
					}
				} else {
					done(controller, response);
				}	
			}
		});
	}
	
	/**
	 * Synchonously downloads a thumb from XBMC and stores it locally.
	 * 
	 * @param response Response object, can be null.
	 * @param cover Cover to download
	 * @param thumbSize Size to return to response object
	 * @param controller Controller to be announced, can be null.
	 * @param manager Manager is needed to obtain different managers for cache access
	 * @param context Context is needed for obtaining other manager instances
	 * @return True if cover was downloaded successfully, false otherwise.
	 */
	public static boolean download(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, final INotifiableController controller, final INotifiableManager manager, final Context context, final boolean addToMemCache) {
		if (DEBUG) Log.i(TAG, "Download START..");
		Bitmap bitmap = null;
		final boolean success;
		switch (cover.getMediaType()) {
			case MediaType.MUSIC:
				try {
					bitmap = music(manager, context).getCover(manager, cover, thumbSize);
				} catch (WifiStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case MediaType.VIDEO_MOVIE:
			case MediaType.VIDEO:
				try {
					bitmap = video(manager, context).getCover(manager, cover, thumbSize);
				} catch (WifiStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case MediaType.VIDEO_TVEPISODE:
			case MediaType.VIDEO_TVSEASON:
			case MediaType.VIDEO_TVSHOW:
				try {
					bitmap = tvshow(manager, context).getCover(manager, cover, thumbSize);
				} catch (WifiStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case MediaType.PICTURES:
				done(controller, response);
				break;
			case MediaType.PROFILE:
				try {
					bitmap = profile(manager, context).getCover(manager, cover, thumbSize);
				} catch (WifiStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			default:
				done(controller, response);
				break;
		}
		if (DEBUG) Log.i(TAG, "Download END.");
		if (bitmap != null) {
			// add to disk cache
			final Bitmap v = DiskCacheThread.addCoverToCache(cover, bitmap, thumbSize);
			// add to mem cache
			if (addToMemCache) {
				MemCacheThread.addCoverToCache(cover, v, thumbSize);
			}
			if (response != null) {
				response.value = v;
			}
			if (DEBUG) Log.i(TAG, "Done");
			success = true;
		} else {
			if (addToMemCache) {
				// still add null value to mem cache so we don't try to fetch it again
				if (DEBUG) Log.i(TAG, "Adding null-value (" + cover.getCrc() + ") to mem cache in order to block future downloads");
				MemCacheThread.addCoverToCache(cover, null, 0);
			}
			success = false;
		}
		done(controller, response);		
		return success;
	}
	

	/**
	 * Returns an instance of this thread. Spawns if necessary.
	 * @return
	 */
	public static DownloadThread get() {
		if (sHttpApiThread == null) {
 			sHttpApiThread = new DownloadThread();
			sHttpApiThread.start();
			// thread must be entirely started
			waitForStartup(sHttpApiThread);
		}
		return sHttpApiThread;
	}
	
	public static synchronized void quit() {
		if (sHttpApiThread != null) {
			sHttpApiThread.mHandler.getLooper().quit();
			sHttpApiThread = null;
		}
	}
}