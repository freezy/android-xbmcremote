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

import java.io.File;

import org.xbmc.android.util.ImportUtilities;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.type.ThumbSize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This thread asynchronously delivers sdcard-cached bitmaps.
 * 
 * The sdcard cache keeps thumb bitmaps in three sizes (small, medium, 
 * original). This thread is directly accessed by the original HttpApi thread, 
 * through one of its wrappers.
 * 
 * @author Team XBMC
 */
class HttpApiDiskCacheThread extends HttpApiAbstractThread {
	
	/**
	 * Singleton instance of this thread
	 */
	protected static HttpApiDiskCacheThread sHttpApiThread;

	/**
	 * Constructor is protected, use get().
	 */
	protected HttpApiDiskCacheThread() {
		super("HTTP API Disk Cache Thread");
	}
	
	/**
	 * Asynchronously returns a thumb from the disk cache, or null if 
	 * not available. Accessed covers get automatically added to the 
	 * memory cache.
	 * 
	 * @param handler Callback
	 * @param cover   Which cover to return
	 * @param thumbSize    Which size to return
	 */
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final int thumbSize) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover != null) {
					final File file = new File(ImportUtilities.getCacheDirectory(cover.getArtFolder(), thumbSize), String.format("%08x", cover.getCrc()).toLowerCase());
				    if (file.exists()) {
				    	final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
				    	HttpApiMemCacheThread.addCoverToCache(cover, bitmap, thumbSize);
				    	handler.value = bitmap;
				    }
				}
				done(handler);
			}
		});
	}
	
	/**
	 * Synchronously returns a thumb from the disk cache, or null if not 
	 * available.
	 * 
	 * @param cover Which cover to return
	 * @return Bitmap or null if not available.
	 */
	public static Bitmap getCover(ICoverArt cover, int thumbSize) {
		final File file = new File(ImportUtilities.getCacheDirectory(cover.getArtFolder(), thumbSize), String.format("%08x", cover.getCrc()).toLowerCase());
	    if (file.exists()) {
	    	final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
	    	HttpApiMemCacheThread.addCoverToCache(cover, bitmap, thumbSize);
	    	return bitmap;
	    } else
	    	return null;
	}
	
	/**
	 * Checks if a thumb is in the disk cache.
	 * @param cover
	 * @return True if thumb is in disk cache, false otherwise.
	 */
	public static boolean isInCache(ICoverArt cover) {
		return (new File(ImportUtilities.getCacheDirectory(cover.getArtFolder(), ThumbSize.BIG), String.format("%08x", cover.getCrc()).toLowerCase())).exists();
	}
	
	/**
	 * Adds a cover to the disk cache
	 * @param cover  Which cover to add
	 * @param bitmap Bitmap data, original size.
	 */
	public static Bitmap addCoverToCache(ICoverArt cover, Bitmap bitmap, int size) {
		return ImportUtilities.addCoverToCache(cover, bitmap, size);
	}

	/**
	 * Returns an instance of this thread. Spawns if necessary.
	 * @return
	 */
	public static HttpApiDiskCacheThread get() {
		if (sHttpApiThread == null) {
 			sHttpApiThread = new HttpApiDiskCacheThread();
			sHttpApiThread.start();
			// thread must be entirely started
			waitForStartup(sHttpApiThread);
		}
		return sHttpApiThread;
	}
}