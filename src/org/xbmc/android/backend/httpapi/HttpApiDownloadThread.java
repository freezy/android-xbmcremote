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
import java.lang.ref.SoftReference;

import org.xbmc.android.util.Base64;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.type.ThumbSize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * Spawned on first access, then looping. Takes all HTTP API commands and
 * synchronously returns the result.
 * 
 * @author Team XBMC
 */
class HttpApiDownloadThread extends HttpApiAbstractThread {
	
	protected static HttpApiDownloadThread sHttpApiThread;

	protected HttpApiDownloadThread() {
		super("HTTP API Network Thread");
	}
	
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final ThumbSize size) {
		mHandler.post(new Runnable() {
			public void run() {
				if (HttpApiMemCacheThread.isInCache(cover)) {
					System.out.println("Cover is now in mem cache, directly returning...");
					handler.value = HttpApiMemCacheThread.getCover(cover);
					done(handler);
				} else {
					System.out.println("Download START..");
					String b64enc = music(handler).getAlbumThumb(cover);
					byte[] bytes;
					try {
						bytes = Base64.decode(b64enc);
						if (bytes.length > 0) {
							System.out.println("Decoding, resizing and adding to cache");
							Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
							if (bitmap != null) {
								HttpApiDiskCacheThread.addCoverToCache(cover, bitmap);
								HttpApiMemCacheThread.addCoverToCache(cover, bitmap);
								handler.value = bitmap;
								System.out.println("Done");
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