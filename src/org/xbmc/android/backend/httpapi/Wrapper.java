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

import org.xbmc.android.util.ConnectionManager;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.InfoClient;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.client.VideoClient;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.type.CacheType;
import org.xbmc.httpapi.type.ThumbSize;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

/**
 * Super class of the wrappers, keeps common code.
 * 
 * @author Team XBMC
 */
public abstract class Wrapper {
	
	protected static final String TAG = "Wrapper";
	protected static final Boolean DEBUG = false;
	
	public static final String PREF_SORT_BY_PREFIX = "sort_by_";
	public static final String PREF_SORT_ORDER_PREFIX = "sort_order_";
	
	/* The idea of the sort keys is to remember different sort settings for
	 * each type. In your controller, make sure you run setSortKey() in the
	 * onCreate() method.
	 */
	public static final int PREF_SORT_KEY_ALBUM = 1;
	public static final int PREF_SORT_KEY_ARTIST = 2;
	public static final int PREF_SORT_KEY_SONG = 3;
	public static final int PREF_SORT_KEY_GENRE = 4;
	public static final int PREF_SORT_KEY_FILEMODE = 5;
	
	protected Handler mHandler;
	
	/**
	 * Sets the handler used in the looping thread
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	/**
	 * Returns the InfoClient class
	 * @param handler
	 * @return
	 */
	protected InfoClient info(HttpApiHandler<?> handler) {
		return ConnectionManager.getHttpClient(handler.getActivity()).info;
	}
	
	/**
	 * Returns the ControlClient class
	 * @param handler
	 * @return
	 */
	protected ControlClient control(HttpApiHandler<?> handler) {
		return ConnectionManager.getHttpClient(handler.getActivity()).control;
	}
	
	/**
	 * Returns the VideoClient class
	 * @param handler
	 * @return
	 */
	protected VideoClient video(HttpApiHandler<?> handler) {
		return ConnectionManager.getHttpClient(handler.getActivity()).video;
	}
	
	/**
	 * Returns the MusicClient class
	 * @param handler
	 * @return
	 */
	protected MusicClient music(HttpApiHandler<?> handler) {
		return ConnectionManager.getHttpClient(handler.getActivity()).music;
	}
	
	/**
	 * Calls the UI thread's callback code.
	 * @param handler
	 */
	protected void done(HttpApiHandler<?> handler) {
		handler.getActivity().runOnUiThread(handler);
	}
	
	/**
	 * Returns bitmap of any cover. Note that the callback is done by the
	 * helper methods below.
	 * @param handler Callback handler
	 */
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final int thumbSize) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover.getCrc() != 0L) {
					// first, try mem cache (only if size = small, other sizes aren't mem-cached.
					if (thumbSize == ThumbSize.SMALL || thumbSize == ThumbSize.MEDIUM) {
						if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Trying memory");
						getCoverFromMem(handler, cover, thumbSize);
					} else {
						if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Trying disk directly (size not mem-cached)");
						getCoverFromDisk(handler, cover, thumbSize);
					}
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] no crc, skipping.");
					handler.value = null;
					done(handler);
				}
			}
		});
	}
	
	/**
	 * Tries to get small cover from memory, then from disk, then download it from XBMC.
	 * @param handler Callback handler
	 * @param cover   Get cover for this object
	 */
	protected void getCoverFromMem(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final int thumbSize) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Checking in mem cache..");
		HttpApiMemCacheThread.get().getCover(new HttpApiHandler<Bitmap>(handler) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] empty");
					// then, try sdcard cache
					getCoverFromDisk(handler, cover, thumbSize);
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] FOUND in memory!");
					handler.value = value;
					handler.cacheType = CacheType.MEMORY;
					done(handler);
				}
			}
		}, cover, thumbSize);
	}
	
	/**
	 * Tries to get cover from disk, then download it from XBMC.
	 * @param handler Callback handler
	 * @param cover     Get cover for this object
	 * @param thumbSize    Cover size
	 */
	protected void getCoverFromDisk(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final int thumbSize) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Checking in disk cache..");
		HttpApiDiskCacheThread.get().getCover(new HttpApiHandler<Bitmap>(handler) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Disk cache empty.");
					if (handler.postCache()) {
						// well, let's download
						getCoverFromNetwork(handler, cover, thumbSize);
					}
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] FOUND on disk!");
					handler.value = value;
					handler.cacheType = CacheType.SDCARD;
					done(handler);
				}
			}
		}, cover, thumbSize);
	}
	
	/**
	 * Last stop: try to download from XBMC.
	 * @param handler Callback handler
	 * @param cover     Get cover for this object
	 * @param thumbSize Cover size
	 */
	protected void getCoverFromNetwork(final HttpApiHandler<Bitmap> handler, final ICoverArt cover, final int thumbSize) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Downloading..");
		HttpApiDownloadThread.get().getCover(new HttpApiHandler<Bitmap>(handler) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Download empty");
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] DOWNLOADED!");
					handler.cacheType = CacheType.NETWORK;
					handler.value = value;
				}
				done(handler); // callback in any case, since we don't go further than that.
			}
		}, cover, thumbSize);
	}
}
