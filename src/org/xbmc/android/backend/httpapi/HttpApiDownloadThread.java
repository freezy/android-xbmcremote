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

import java.io.IOException;

import org.xbmc.android.util.Base64;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.type.MediaType;

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
class HttpApiDownloadThread extends HttpApiAbstractThread {
	
	/**
	 * Singleton instance of this thread
	 */
	protected static HttpApiDownloadThread sHttpApiThread;
	
	private static final String TAG = "HttpApi-Network";

	/**
	 * Constructor is protected, use get().
	 */
	protected HttpApiDownloadThread() {
		super("HTTP API Network Thread");
	}
	
	/**
	 * Asynchronously downloads a thumb from XBMC and stores it locally.
	 * 
	 * @param handler Callback
	 * @param cover   Which cover to download
	 * @param thumbSize    Which size to return
	 */
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final int thumbSize) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover != null) {
					Log.i(TAG, "Downloading cover " + cover);
					/* it can happen that the same cover is queued consecutively several
					 * times. that's why we check both the disk cache and memory cache if
					 * the cover is not already available from a previously queued download. 
					 */
					if (HttpApiMemCacheThread.isInCache(cover, thumbSize)) { // we're optimistic, let's check the memory first.
						Log.i(TAG, "Cover is now already in mem cache, directly returning...");
						handler.value = HttpApiMemCacheThread.getCover(cover, thumbSize);
						done(handler);
					} else if (HttpApiDiskCacheThread.isInCache(cover)) {
						Log.i(TAG, "Cover is not in mem cache anymore but still on disk, directly returning...");
						handler.value = HttpApiDiskCacheThread.getCover(cover, thumbSize);
					} else {
						Log.i(TAG, "Download START..");
						String b64enc = null;
						switch (cover.getMediaType()) {
							case MediaType.MUSIC:
								b64enc = music(handler).getCover(cover);
								break;
							case MediaType.VIDEO:
								b64enc = video(handler).getCover(cover);
								break;
							case MediaType.PICTURES:
								done(handler);
								break;
							default:
								done(handler);
						}
						Log.i(TAG, "Download END.");
						byte[] bytes;
						try {
							bytes = Base64.decode(b64enc);
							if (bytes.length > 0) {
								Log.i(TAG, "Decoding, resizing and adding to cache");
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								if (bitmap != null) {
									// add to disk cache
									handler.value = HttpApiDiskCacheThread.addCoverToCache(cover, bitmap, thumbSize);
									// add to mem cache
									HttpApiMemCacheThread.addCoverToCache(cover, bitmap, thumbSize);
									Log.i(TAG, "Done");
								}
							} else {
								// still add null value to mem cache so we don't try to fetch it again
								Log.i(TAG, "Adding null-value (" + cover.getCrc() + ") to mem cache in order to block future downloads");
								HttpApiMemCacheThread.addCoverToCache(cover, null, 0);
							}
						} catch (IOException e) {
							System.out.println("IOException: " + e.getMessage());
							System.out.println(e.getStackTrace());
						} finally {
							done(handler);
						}
					}
				} else {
					done(handler);
				}
			}
		});
	}

	/**
	 * Returns an instance of this thread. Spawns if necessary.
	 * @return
	 */
	public static HttpApiDownloadThread get() {
		if (sHttpApiThread == null) {
 			sHttpApiThread = new HttpApiDownloadThread();
			sHttpApiThread.start();
			// thread must be entirely started
			waitForStartup(sHttpApiThread);
		}
		return sHttpApiThread;
	}
}