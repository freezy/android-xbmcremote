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

import org.xbmc.httpapi.data.ICoverArt;

import android.graphics.Bitmap;


/**
 * Spawned on first access, then looping. Takes all HTTP API commands and
 * synchronously returns the result.
 * 
 * @author Team XBMC
 */
class HttpApiMemCacheThread extends HttpApiAbstractThread {
	
	protected static HttpApiMemCacheThread sHttpApiThread;
	private static final HashMap<String, SoftReference<Bitmap>> sArtCache = new HashMap<String, SoftReference<Bitmap>>();

	protected HttpApiMemCacheThread() {
		super("HTTP API Mem Cache Thread");
	}
	
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover) {
		mHandler.post(new Runnable() {
			public void run() {
				SoftReference<Bitmap> ref = sArtCache.get(cover.getCrc());
		        if (ref != null) {
		            handler.value = ref.get();
		        }
				done(handler);
			}
		});
	}
	
	public static boolean isInCache(ICoverArt cover) {
		return sArtCache.containsKey(cover.getCrc());
	}
	
	public static Bitmap getCover(ICoverArt cover) {
		return sArtCache.get(cover.getCrc()).get();
	}
	
	public static void addCoverToCache(ICoverArt cover, Bitmap bitmap) {
		sArtCache.put(cover.getCrc(), new SoftReference<Bitmap>(bitmap));
	}

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