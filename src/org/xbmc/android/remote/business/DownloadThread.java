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

import java.io.IOException;

import org.xbmc.android.util.Base64;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.MediaType;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	public void getCover(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, final INotifiableController controller, final INotifiableManager manager) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover != null) {
					if (DEBUG) Log.i(TAG, "Downloading cover " + cover);
					/* it can happen that the same cover is queued consecutively several
					 * times. that's why we check both the disk cache and memory cache if
					 * the cover is not already available from a previously queued download. 
					 */
					if (MemCacheThread.isInCache(cover, thumbSize)) { // we're optimistic, let's check the memory first.
						if (DEBUG) Log.i(TAG, "Cover is now already in mem cache, directly returning...");
						response.value = MemCacheThread.getCover(cover, thumbSize);
						done(controller, response);
					} else if (DiskCacheThread.isInCache(cover)) {
						if (DEBUG) Log.i(TAG, "Cover is not in mem cache anymore but still on disk, directly returning...");
						response.value = DiskCacheThread.getCover(cover, thumbSize);
						done(controller, response);
					} else {
						if (DEBUG) Log.i(TAG, "Download START..");
						String b64enc = null;
						switch (cover.getMediaType()) {
							case MediaType.MUSIC:
								b64enc = music(manager).getCover(manager, cover);
								break;
							case MediaType.VIDEO:
								b64enc = video(manager).getCover(manager, cover);
								break;
							case MediaType.PICTURES:
								done(controller, response);
								break;
							default:
								done(controller, response);
						}
						if (DEBUG) Log.i(TAG, "Download END.");
						byte[] bytes;
						try {
							bytes = Base64.decode(b64enc);
							if (bytes.length > 0) {
								if (DEBUG) Log.i(TAG, "Decoding, resizing and adding to cache");
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								if (bitmap != null) {
									// add to disk cache
									response.value = DiskCacheThread.addCoverToCache(cover, bitmap, thumbSize);
									// add to mem cache
									MemCacheThread.addCoverToCache(cover, bitmap, thumbSize);
									if (DEBUG) Log.i(TAG, "Done");
								}
							} else {
								// still add null value to mem cache so we don't try to fetch it again
								if (DEBUG) Log.i(TAG, "Adding null-value (" + cover.getCrc() + ") to mem cache in order to block future downloads");
								MemCacheThread.addCoverToCache(cover, null, 0);
							}
						} catch (IOException e) {
							System.out.println("IOException: " + e.getMessage());
							System.out.println(e.getStackTrace());
						} finally {
							done(controller, response);
						}
					}
				} else {
					done(controller, response);
				}
			}
		});
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