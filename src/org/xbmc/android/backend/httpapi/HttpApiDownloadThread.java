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
import org.xbmc.httpapi.type.ThumbSize;

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
	
	private static final String LOG_TAG = "HttpApi-Network";

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
	 * @param size    Which size to return
	 */
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final ThumbSize size) {
		mHandler.post(new Runnable() {
			public void run() {
				Log.i(LOG_TAG, "Downloading cover " + cover);
				/* it can happen that the same cover is queued consecutively several
				 * times. that's why we check both the disk cache and memory cache if
				 * the cover is not already available from a previously queued download. 
				 */
				if (size == ThumbSize.small && HttpApiMemCacheThread.isInCache(cover)) { // we're optimistic, let's check the memory first.
					Log.i(LOG_TAG, "Cover is now already in mem cache, directly returning...");
					handler.value = HttpApiMemCacheThread.getCover(cover);
					done(handler);
				} else if (HttpApiDiskCacheThread.isInCache(cover)) {
					Log.i(LOG_TAG, "Cover is not in mem cache anymore but still on disk, directly returning...");
					handler.value = HttpApiDiskCacheThread.getCover(cover, size);
				} else {
					Log.i(LOG_TAG, "Download START..");
					String b64enc = music(handler).getAlbumThumb(cover);
					Log.i(LOG_TAG, "Download END.");
					byte[] bytes;
					try {
						bytes = Base64.decode(b64enc);
						if (bytes.length > 0) {
							Log.i(LOG_TAG, "Decoding, resizing and adding to cache");
							Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
							if (bitmap != null) {
								HttpApiDiskCacheThread.addCoverToCache(cover, bitmap);
								HttpApiMemCacheThread.addCoverToCache(cover, bitmap);
								handler.value = bitmap;
								Log.i(LOG_TAG, "Done");
							} else {
								Log.w(LOG_TAG, "Bitmap was null, couldn't decode XBMC's response (" + bytes.length + " bytes).");
							}
						}
					} catch (IOException e) {
						System.out.println("IOException " + e.getMessage());
						System.out.println(e.getStackTrace());
					} finally {
						done(handler);
					}
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