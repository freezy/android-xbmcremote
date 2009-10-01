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
 * Spawned on first access, then looping. Takes all HTTP API commands and
 * synchronously returns the result.
 * 
 * @author Team XBMC
 */
class HttpApiDiskCacheThread extends HttpApiAbstractThread {
	
	protected static HttpApiDiskCacheThread sHttpApiThread;

	protected HttpApiDiskCacheThread() {
		super("HTTP API Disk Cache Thread");
	}
	
	
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final ThumbSize size) {
		mHandler.post(new Runnable() {
			public void run() {
				final File file = new File(ImportUtilities.getCacheDirectory(cover.getArtFolder(), size), cover.getCrc());
			    if (file.exists()) {
			    	handler.value = BitmapFactory.decodeFile(file.getAbsolutePath());
			    	HttpApiMemCacheThread.addCoverToCache(cover, handler.value);
			    }
				done(handler);
			}
		});
	}
	
	public static void addCoverToCache(ICoverArt cover, Bitmap bitmap) {
		ImportUtilities.addCoverToCache(cover, bitmap);
	}

    
    

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