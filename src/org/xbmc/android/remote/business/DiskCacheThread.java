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

import java.io.File;

import org.xbmc.android.util.Crc32;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.ThumbSize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * This thread asynchronously delivers sdcard-cached bitmaps.
 * 
 * The sdcard cache keeps thumb bitmaps in three sizes (small, medium, 
 * original). This thread is directly accessed by the original HttpApi thread, 
 * through one of its wrappers.
 * 
 * @author Team XBMC
 */
class DiskCacheThread extends AbstractThread {
	
	/**
	 * Singleton instance of this thread
	 */
	protected static DiskCacheThread sHttpApiThread;

	/**
	 * Constructor is protected, use get().
	 */
	protected DiskCacheThread() {
		super("HTTP API Disk Cache Thread");
	}
	
	/**
	 * Asynchronously returns a thumb from the disk cache, or null if 
	 * not available. Accessed covers get automatically added to the 
	 * memory cache.
	 * 
	 * @param response Response object
	 * @param cover   Which cover to return
	 * @param thumbSize    Which size to return
	 */
	public void getCover(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, final INotifiableController controller) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover != null) {
					final File file = ImportUtilities.getCacheFile(MediaType.getArtFolder(cover.getMediaType()), thumbSize, Crc32.formatAsHexLowerCase(cover.getCrc()));
				    if (file.exists()) {
				    	final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
				    	if (bitmap == null) { // file is available but obviously corruped, so delete it.
				    		file.delete();
				    		response.value = null;
				    	} else {
				    		MemCacheThread.addCoverToCache(cover, bitmap, thumbSize);
				    		response.value = bitmap;
				    	}
				    }
				}
				done(controller, response);
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
		final File file = ImportUtilities.getCacheFile(MediaType.getArtFolder(cover.getMediaType()), thumbSize, Crc32.formatAsHexLowerCase(cover.getCrc()));
	    if (file.exists()) {
	    	final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
	    	MemCacheThread.addCoverToCache(cover, bitmap, thumbSize);
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
		return ImportUtilities.getCacheFile(MediaType.getArtFolder(cover.getMediaType()), ThumbSize.BIG, Crc32.formatAsHexLowerCase(cover.getCrc()).toLowerCase()).exists();
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
	public static DiskCacheThread get() {
		if (sHttpApiThread == null) {
			Log.i("DiskCacheThread", "Spawning new thread...");
 			sHttpApiThread = new DiskCacheThread();
			sHttpApiThread.start();
			// thread must be entirely started
			waitForStartup(sHttpApiThread);
		} else {
			Log.i("DiskCacheThread", "Returning current thread.");
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