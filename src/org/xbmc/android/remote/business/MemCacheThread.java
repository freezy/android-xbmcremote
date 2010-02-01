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

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.ThumbSize;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * This thread asynchronously delivers memory-cached bitmaps.
 * 
 * The memory cache keeps small-size thumb bitmaps in a soft-referenced list.
 * This thread is directly accessed by the original HttpApi thread, through one
 * of its wrappers.
 * 
 * @author Team XBMC
 */
class MemCacheThread extends AbstractThread {
	
	private final static String TAG = "MemCacheThread";
	private final static boolean DEBUG = AbstractManager.DEBUG;
	
	/**
	 * Singleton instance of this thread
	 */
	protected static MemCacheThread sHttpApiThread;
	
	/**
	 * The actual cache variable. Here are the thumbs stored. 
	 */
	private static final HashMap<Long, SoftReference<Bitmap>> sCacheSmall = new HashMap<Long, SoftReference<Bitmap>>();
	private static final HashMap<Long, SoftReference<Bitmap>> sCacheMedium = new HashMap<Long, SoftReference<Bitmap>>();
	private static final HashMap<Long, Boolean> sNotAvailable = new HashMap<Long, Boolean>();

	/**
	 * Constructor is protected, use get().
	 */
	protected MemCacheThread() {
		super("HTTP API Mem Cache Thread");
	}
	
	/**
	 * Asynchronously returns a thumb from the mem cache, or null if 
	 * not available.
	 * 
	 * @param response Response object
	 * @param cover   Which cover to return
	 */
	public void getCover(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, final INotifiableController controller, final Bitmap defaultCover) {
		if (controller == null) {
			Log.w(TAG, "[" + cover.getId() + "] Controller is null.");
		}
		mHandler.post(new Runnable() {
			public void run() {
				if (DEBUG) Log.i(TAG,  "[" + cover.getId() + "] Checking if cover in cache..");
				if (cover != null) {
					final long crc = cover.getCrc();
					final SoftReference<Bitmap> ref = getCache(thumbSize).get(crc);
			        if (ref != null) {
			        	if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] -> In cache.");
			        	response.value = ref.get();
			        } else if (sNotAvailable.containsKey(crc)) {
			        	if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] -> Marked as not-in-cache (" + crc + ").");
			        	response.value = defaultCover;
			        } else {
			        	if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] -> Not in cache.");
			        }
				}
				done(controller, response);
			}
		});
	}
	
	private static HashMap<Long, SoftReference<Bitmap>> getCache(int thumbSize) {
		return (thumbSize == ThumbSize.MEDIUM) ? sCacheMedium : sCacheSmall;
	}
	
	/**
	 * Synchronously returns a thumb from the mem cache, or null 
	 * if not available.
	 * 
	 * @param cover Which cover to return
	 * @return Bitmap or null if not available.
	 */
	public static Bitmap getCover(ICoverArt cover, int thumbSize) {
		return getCache(thumbSize).get(cover.getCrc()).get();
	}
	
	/**
	 * Checks if a thumb is in the mem cache.
	 * @param cover
	 * @return True if thumb is in mem cache, false otherwise.
	 */
	public static boolean isInCache(ICoverArt cover, int thumbSize) {
		return (thumbSize == ThumbSize.SMALL || thumbSize == ThumbSize.MEDIUM) && getCache(thumbSize).containsKey(cover.getCrc());
	}
	
	/**
	 * Adds a cover to the mem cache
	 * @param cover  Which cover to add
	 * @param bitmap Bitmap data
	 */
	public static void addCoverToCache(ICoverArt cover, Bitmap bitmap, int thumbSize) {
		// if bitmap is null, add an entry to the sNotAvailable table so we can return the default bitmap later directly.
		if (bitmap == null) {
			sNotAvailable.put(cover.getCrc(), true);
		} else if (thumbSize == ThumbSize.SMALL || thumbSize == ThumbSize.MEDIUM) {
			getCache(thumbSize).put(cover.getCrc(), new SoftReference<Bitmap>(bitmap));
		}
	}

	/**
	 * Returns an instance of this thread. Spawns if necessary.
	 * @return
	 */
	public static MemCacheThread get() {
		if (sHttpApiThread == null) {
 			sHttpApiThread = new MemCacheThread();
			sHttpApiThread.start();
			// thread must be entirely started
			waitForStartup(sHttpApiThread);
		}
		return sHttpApiThread;
	}
	
	public synchronized static void quit() {
		if (sHttpApiThread != null) {
			sHttpApiThread.mHandler.getLooper().quit();
			sHttpApiThread = null;
		}
	}
}