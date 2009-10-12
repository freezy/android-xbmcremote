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

package org.xbmc.android.backend.httpapi;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.xbmc.android.remote.drawable.FastBitmapDrawable;
import org.xbmc.httpapi.data.ICoverArt;

import android.graphics.Bitmap;

/**
 * This thread asynchronously delivers memory-cached bitmaps.
 * 
 * The memory cache keeps small-size thumb bitmaps in a soft-referenced list.
 * This thread is directly accessed by the original HttpApi thread, through one
 * of its wrappers.
 * 
 * @author Team XBMC
 */
class HttpApiMemCacheThread extends HttpApiAbstractThread {
	
	/**
	 * Singleton instance of this thread
	 */
	protected static HttpApiMemCacheThread sHttpApiThread;
	
	/**
	 * The actual cache variable. Here are the thumbs stored. 
	 */
	private static final HashMap<Integer, SoftReference<Bitmap>> sArtCache = new HashMap<Integer, SoftReference<Bitmap>>();

	/**
	 * Constructor is protected, use get().
	 */
	protected HttpApiMemCacheThread() {
		super("HTTP API Mem Cache Thread");
	}
	
	/**
	 * Asynchronously returns a thumb from the mem cache, or null if 
	 * not available.
	 * 
	 * @param handler Callback
	 * @param cover   Which cover to return
	 */
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover != null) {
					SoftReference<Bitmap> ref = sArtCache.get(cover.getCrc());
			        if (ref != null) {
			            handler.value = ref.get();
			        }
				}
				done(handler);
			}
		});
	}
	
	/**
	 * Synchronously returns a thumb from the mem cache, or null 
	 * if not available.
	 * 
	 * @param cover Which cover to return
	 * @return Bitmap or null if not available.
	 */
	public static Bitmap getCover(ICoverArt cover) {
		return sArtCache.get(cover.getCrc()).get();
	}
	
	/**
	 * Checks if a thumb is in the mem cache.
	 * @param cover
	 * @return True if thumb is in mem cache, false otherwise.
	 */
	public static boolean isInCache(ICoverArt cover) {
		return sArtCache.containsKey(cover.getCrc());
	}
	
	/**
	 * Adds a cover to the mem cache
	 * @param cover  Which cover to add
	 * @param bitmap Bitmap data
	 */
	public static void addCoverToCache(ICoverArt cover, Bitmap bitmap) {
		sArtCache.put(cover.getCrc(), new SoftReference<Bitmap>(bitmap));
	}

	/**
	 * Returns an instance of this thread. Spawns if necessary.
	 * @return
	 */
	public static HttpApiMemCacheThread get() {
		if (sHttpApiThread == null) {
 			sHttpApiThread = new HttpApiMemCacheThread();
			sHttpApiThread.start();
			// thread must be entirely started
			waitForStartup(sHttpApiThread);
		}
		return sHttpApiThread;
	}
}