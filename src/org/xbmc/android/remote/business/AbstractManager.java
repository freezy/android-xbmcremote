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

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.util.ClientFactory;
import org.xbmc.android.util.Crc32;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.data.IMusicClient;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.CacheType;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

/**
 * Super class of the wrappers, keeps common code.
 * 
 * @author Team XBMC
 */
public abstract class AbstractManager implements INotifiableManager {
	
	public static final Boolean DEBUG = false;
	
	protected static final String TAG = "AbstractManager";
	
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
	
	protected INotifiableController mController = null;
	
	protected Handler mHandler;
	
	protected List<Runnable> failedRequests = new ArrayList<Runnable>();
	/**
	 * Sets the handler used in the looping thread
	 * @param handler
	 */
	void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	public void setController(INotifiableController controller) {
		mController = controller;
	}
	
	public void postActivity() {
		AbstractThread.quitThreads();
	}
	
	/**
	 * Returns the InfoClient class
	 * @param response Response object
	 * @return
	 * @throws WifiStateException 
	 */
	protected IInfoClient info(Context context) throws WifiStateException {
		return ClientFactory.getInfoClient(this, context);
	}
	
	/**
	 * Returns the ControlClient class
	 * @param response Response object
	 * @return
	 * @throws WifiStateException 
	 */
	protected IControlClient control(Context context) throws WifiStateException {
		return ClientFactory.getControlClient(this, context);
	}
	
	/**
	 * Returns the VideoClient class
	 * @param response Response object
	 * @return
	 * @throws WifiStateException 
	 */
	protected IVideoClient video(Context context) throws WifiStateException {
		return ClientFactory.getVideoClient(this, context);
	}
	
	/**
	 * Returns the MusicClient class
	 * @param response Response object
	 * @return
	 * @throws WifiStateException 
	 */
	protected IMusicClient music(Context context) throws WifiStateException {
		return ClientFactory.getMusicClient(this, context);
	}
	
	protected ITvShowClient shows(Context context) throws WifiStateException {
		return ClientFactory.getTvShowClient(this, context);
	}
	
	/**
	 * Calls the UI thread's callback code.
	 * @param response Response object
	 */
	public void onFinish(DataResponse<?> response) {
		if (mController != null) {
			mController.runOnUI(response);
		}else{
			mHandler.post(response);
		}
	}
	
	/**
	 * Returns bitmap of any cover. Note that the callback is done by the
	 * helper methods below.
	 * @param response Response object
	 */
	public void getCover(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, final Bitmap defaultCover, final Context context, final boolean getFromCacheOnly) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover.getCrc() != 0L) {
					// first, try mem cache (only if size = small, other sizes aren't mem-cached.
					if (thumbSize == ThumbSize.SMALL || thumbSize == ThumbSize.MEDIUM) {
						if (DEBUG) Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] Trying memory (" + Crc32.formatAsHexLowerCase(cover.getCrc()) + ")");
						getCoverFromMem(response, cover, thumbSize, defaultCover, context, getFromCacheOnly);
					} else {
						if (getFromCacheOnly) {
							Log.e(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] ERROR: NOT downloading big covers is a bad idea because they are not cached!");
							response.value = null;
							onFinish(response);
						} else {
							if (DEBUG) Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] Downloading directly");
							getCoverFromNetwork(response, cover, thumbSize, context);
						}
					}
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] no crc, skipping.");
					response.value = null;
					onFinish(response);
				}
			}
		});
	}
	
	/**
	 * Synchronously downloads a cover and stores on on disk cache.
	 * @param cover Cover to cache
	 * @param manager Reference to manager
	 * @param context Reference to context
	 * @return True if cover has been downloaded, false otherwise.
	 */
	public static boolean cacheCover(final ICoverArt cover, final INotifiableManager manager, final Context context) {
		if (!DiskCacheThread.isInCache(cover)) {
			return DownloadThread.download(null, cover, ThumbSize.MEDIUM, null, manager, context, false);
		}
		return false;
	}
	
	/**
	 * Tries to get small cover from memory, then from disk, then download it from XBMC.
	 * @param response Response object
	 * @param cover    Get cover for this object
	 */
	protected void getCoverFromMem(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, Bitmap defaultCover, final Context context, final boolean getFromCacheOnly) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Checking in mem cache..");
		MemCacheThread.get().getCover(new DataResponse<Bitmap>() {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] empty");
					// then, try sdcard cache
					getCoverFromDisk(response, cover, thumbSize, context, getFromCacheOnly);
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] FOUND in memory!");
					response.value = value;
					response.cacheType = CacheType.MEMORY;
					onFinish(response);
				}
			}
		}, cover, thumbSize, mController, defaultCover);
	}
	
	/**
	 * Tries to get cover from disk, then download it from XBMC.
	 * @param response  Response object
	 * @param cover     Get cover for this object
	 * @param thumbSize Cover size
	 */
	protected void getCoverFromDisk(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, final Context context, final boolean getFromCacheOnly) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Checking in disk cache..");
		DiskCacheThread.get().getCover(new DataResponse<Bitmap>() {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] Disk cache empty.");
					if (response.postCache()) {
						// well, let's download
						if (getFromCacheOnly) {
							if (DEBUG) Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] Skipping download.");
							response.value = null;
							onFinish(response);
						} else {
							getCoverFromNetwork(response, cover, thumbSize, context);
						}
					}
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize) + "] FOUND on disk!");
					response.value = value;
					response.cacheType = CacheType.SDCARD;
					onFinish(response);
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
	protected void getCoverFromNetwork(final DataResponse<Bitmap> response, final ICoverArt cover, final int thumbSize, final Context context) {
		if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Downloading..");
		DownloadThread.get().getCover(new DataResponse<Bitmap>() {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] Download empty");
				} else {
					if (DEBUG) Log.i(TAG, "[" + cover.getId() + "] DOWNLOADED (" + value.getWidth() + "x" + value.getHeight() + ")!");
					response.cacheType = CacheType.NETWORK;
					response.value = value;
				}
				onFinish(response); // callback in any case, since we don't go further than that.
			}
		}, cover, thumbSize, mController, this, context);
	}
	
	/**
	 * Commands failed because of wrong connection state are special. After the connection has the right state we
	 * could retry the command
	 */
	public void onWrongConnectionState(int state, Command<?> cmd) {
		failedRequests.add(cmd);
		if (mController != null)
			mController.onWrongConnectionState(state, this, cmd);
	}

	public void onError(Exception e) {
		if (mController != null) {
			mController.onError(e);
		}
	}

	public void onMessage(String message) {
		if (mController != null) {
			mController.onMessage(message);
		}
	}

	public void onMessage(int code, String message) {
		onMessage(message);
	}
	
	public void retryAll() {
		Log.d(TAG, "Posting retries to the queue");
		mHandler.post(new Runnable() {
			public void run() {
				Log.d(TAG, "runnable started, posting retries");
				while(failedRequests.size() > 0) {
					if(mHandler.post(failedRequests.get(0)))
						Log.d(TAG, "Runnable posted");
					else
						Log.d(TAG, "Runnable coudln't be posted");
					failedRequests.remove(0);
				}
			}
		});
	}
}
