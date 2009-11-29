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

import org.xbmc.android.util.ConnectionManager;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.InfoClient;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.client.VideoClient;
import org.xbmc.httpapi.type.CacheType;
import org.xbmc.httpapi.type.ThumbSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

/**
 * Super class of the wrappers, keeps common code.
 * 
 * @author Team XBMC
 */
public abstract class AbstractManager {
	
	protected static final String TAG = "AbstractManager";
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
	
	private INotifiableController mController = null;
	private Context mContext;
	
	protected Handler mHandler;
	
	/**
	 * Sets the handler used in the looping thread
	 * @param handler
	 */
	void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	/**
	 * Sets the context. This should always be the application context, not the activity!
	 * @param context Application context
	 */
	public void setContext(Context context) {
		mContext = context;
	}
	
	public void setController(INotifiableController controller) {
		mController = controller;
	}

	
	/**
	 * Returns the InfoClient class
	 * @param response Response object
	 * @return
	 */
	protected InfoClient info(DataResponse<?> response) {
		return ConnectionManager.getHttpClient(mContext).info;
	}
	
	/**
	 * Returns the ControlClient class
	 * @param response Response object
	 * @return
	 */
	protected ControlClient control(DataResponse<?> response) {
		return ConnectionManager.getHttpClient(mContext).control;
	}
	
	/**
	 * Returns the VideoClient class
	 * @param response Response object
	 * @return
	 */
	protected VideoClient video(DataResponse<?> response) {
		return ConnectionManager.getHttpClient(mContext).video;
	}
	
	/**
	 * Returns the MusicClient class
	 * @param response Response object
	 * @return
	 */
	protected MusicClient music(DataResponse<?> response) {
		return ConnectionManager.getHttpClient(mContext).music;
	}
	
	/**
	 * Calls the UI thread's callback code.
	 * @param response Response object
	 */
	protected void done(DataResponse<?> response) {
		if (mController != null) {
			mController.runOnUI(response);
		}
	}
	
	/**
	 * Returns bitmap of any cover. Note that the callback is done by the
	 * helper methods below.
	 * @param response Response object
	 */
	public void getCover(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover.getCrc() != 0L) {
					// first, try mem cache (only if size = small, other sizes aren't mem-cached.
					if (thumbSize == ThumbSize.SMALL || thumbSize == ThumbSize.MEDIUM) {
						if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Trying memory");
						getCoverFromMem(response, cover, thumbSize);
					} else {
						if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Trying disk directly (size not mem-cached)");
						getCoverFromDisk(response, cover, thumbSize);
					}
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] no crc, skipping.");
					response.value = null;
					done(response);
				}
			}
		});
	}
	
	/**
	 * Tries to get small cover from memory, then from disk, then download it from XBMC.
	 * @param response Response object
	 * @param cover    Get cover for this object
	 */
	protected void getCoverFromMem(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Checking in mem cache..");
		MemCacheThread.get().getCover(new DataResponse<Bitmap>(response) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] empty");
					// then, try sdcard cache
					getCoverFromDisk(response, cover, thumbSize);
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] FOUND in memory!");
					response.value = value;
					response.cacheType = CacheType.MEMORY;
					done(response);
				}
			}
		}, cover, thumbSize, mController);
	}
	
	/**
	 * Tries to get cover from disk, then download it from XBMC.
	 * @param response  Response object
	 * @param cover     Get cover for this object
	 * @param thumbSize Cover size
	 */
	protected void getCoverFromDisk(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Checking in disk cache..");
		DiskCacheThread.get().getCover(new DataResponse<Bitmap>(response) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Disk cache empty.");
					if (response.postCache()) {
						// well, let's download
						getCoverFromNetwork(response, cover, thumbSize);
					}
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] FOUND on disk!");
					response.value = value;
					response.cacheType = CacheType.SDCARD;
					done(response);
				}
			}
		}, cover, thumbSize, mController);
	}
	
	/**
	 * Last stop: try to download from XBMC.
	 * @param response  Response object
	 * @param cover     Get cover for this object
	 * @param thumbSize Cover size
	 */
	protected void getCoverFromNetwork(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Downloading..");
		DownloadThread.get().getCover(new DataResponse<Bitmap>(response) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Download empty");
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] DOWNLOADED!");
					response.cacheType = CacheType.NETWORK;
					response.value = value;
				}
				done(response); // callback in any case, since we don't go further than that.
			}
		}, cover, thumbSize, mController, mContext);
	}
}
