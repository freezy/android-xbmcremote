package org.xbmc.android.remote.business.cm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.Player;
import org.xbmc.android.jsonrpc.api.call.Playlist;
import org.xbmc.android.jsonrpc.api.model.ListModel;
import org.xbmc.android.jsonrpc.api.model.ListModel.Sort;
import org.xbmc.android.jsonrpc.api.model.PlayerModel;
import org.xbmc.android.jsonrpc.io.ApiCallback;
import org.xbmc.android.jsonrpc.io.ConnectionManager;
import org.xbmc.android.remote.business.AbstractThread;
import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.business.DiskCacheThread;
import org.xbmc.android.remote.business.DownloadThread;
import org.xbmc.android.remote.business.MemCacheThread;
import org.xbmc.android.util.Crc32;
import org.xbmc.android.util.HostFactory;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.api.type.CacheType;
import org.xbmc.api.type.SortType;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.api.type.ThumbSize.Dimension;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class AbstractManager implements INotifiableManager {

	public static final String EMPTY_PLAYLIST_ITEM = "[Empty]";

	public static final Integer PLAYLIST_MUSIC = 0;
	public static final Integer PLAYLIST_VIDEO = 1;
	public static final Integer PLAYLIST_PICTURE = 2;

	public static final Boolean DEBUG = false;
	protected static final String TAG = "cm.AbstractManager";

	public static final String PREF_SORT_BY_PREFIX = "sort_by_";
	public static final String PREF_SORT_ORDER_PREFIX = "sort_order_";

	private static ConnectionManager connectionManager;
	protected INotifiableController mController;
	protected SharedPreferences mPref;
	protected int mCurrentSortKey;
	protected boolean mCurrentIgnoreArticle;
	
	protected List<Runnable> failedRequests = new ArrayList<Runnable>();

	public void setController(INotifiableController controller) {
		this.mController = controller;
	}

	protected ConnectionManager getConnectionManager(Context context) {
		if (connectionManager == null) {
			connectionManager = new ConnectionManager(context,
					HostFactory.host.toHostConfig());
			// Why did we have this?
			//connectionManager.setPreferHTTP();
		}
		return connectionManager;
	}

	public static void resetClient() {
		if(connectionManager == null) {
			return;
		}
		connectionManager.disconnect();
		connectionManager = null;
	}

	protected abstract static class ApiHandler<T, S> {
		public abstract T handleResponse(AbstractCall<S> apiCall);
	}

	protected <T, S> void call(AbstractCall<S> call,
			final ApiHandler<T, S> handler, final DataResponse<T> response,
			Context context) {
		getConnectionManager(context).call(call, new ApiCallback<S>() {

			public void onResponse(AbstractCall<S> apiCall) {
				response.value = handler.handleResponse(apiCall);
				AbstractManager.this.onFinish(response);
			}

			public void onError(int code, String message, String hint) {
				AbstractManager.this.onError(new Exception(message));
			}
		});

	}

	protected <T, S> void callRaw(AbstractCall<S> call,
			final ApiHandler<T, S> handler, Context context) {
		getConnectionManager(context).call(call, new ApiCallback<S>() {

			public void onResponse(AbstractCall<S> apiCall) {
				handler.handleResponse(apiCall);
			}

			public void onError(int code, String message, String hint) {
				AbstractManager.this.onError(new Exception(message));
			}
		});
	}

	public void postActivity() {
		AbstractThread.quitThreads();

	}

	public void getCover(DataResponse<Bitmap> response, ICoverArt cover,
			int thumbSize, Bitmap defaultCover, Context context,
			boolean getFromCacheOnly) {
		if (cover.getCrc() != 0L) {
			// first, try mem cache (only if size = small, other sizes aren't
			// mem-cached.
			if (thumbSize == ThumbSize.SMALL || thumbSize == ThumbSize.MEDIUM) {
				if (DEBUG)
					Log.i(TAG,
							"["
									+ cover.getId()
									+ ThumbSize.getDir(thumbSize)
									+ "] Trying memory ("
									+ Crc32.formatAsHexLowerCase(cover.getCrc())
									+ ")");
				getCoverFromMem(response, cover, thumbSize, defaultCover,
						context, getFromCacheOnly);
			} else {
				if (getFromCacheOnly) {
					Log.e(TAG,
							"["
									+ cover.getId()
									+ ThumbSize.getDir(thumbSize)
									+ "] ERROR: NOT downloading big covers is a bad idea because they are not cached!");
					response.value = null;
					onFinish(response);
				} else {
					if (DEBUG)
						Log.i(TAG,
								"[" + cover.getId()
										+ ThumbSize.getDir(thumbSize)
										+ "] Downloading directly");
					getCoverFromNetwork(response, cover, thumbSize, context);
				}
			}
		} else {
			if (DEBUG)
				Log.i(TAG, "[" + cover.getId() + ThumbSize.getDir(thumbSize)
						+ "] no crc, skipping.");
			response.value = null;
			onFinish(response);
		}

	}

	/**
	 * Tries to get small cover from memory, then from disk, then download it
	 * from XBMC.
	 * 
	 * @param response
	 *            Response object
	 * @param cover
	 *            Get cover for this object
	 */
	protected void getCoverFromMem(final DataResponse<Bitmap> response,
			final ICoverArt cover, final int thumbSize, Bitmap defaultCover,
			final Context context, final boolean getFromCacheOnly) {
		if (DEBUG)
			Log.i(TAG, "[" + cover.getId() + "] Checking in mem cache..");
		MemCacheThread.get().getCover(new DataResponse<Bitmap>() {
			public void run() {
				if (value == null) {
					if (DEBUG)
						Log.i(TAG,
								"[" + cover.getId()
										+ ThumbSize.getDir(thumbSize)
										+ "] empty");
					// then, try sdcard cache
					getCoverFromDisk(response, cover, thumbSize, context,
							getFromCacheOnly);
				} else {
					if (DEBUG)
						Log.i(TAG,
								"[" + cover.getId()
										+ ThumbSize.getDir(thumbSize)
										+ "] FOUND in memory!");
					response.value = value;
					response.cacheType = CacheType.MEMORY;
					onFinish(response);
				}
			}
		}, cover, thumbSize, mController, defaultCover);
	}

	/**
	 * Tries to get cover from disk, then download it from XBMC.
	 * 
	 * @param response
	 *            Response object
	 * @param cover
	 *            Get cover for this object
	 * @param thumbSize
	 *            Cover size
	 */
	protected void getCoverFromDisk(final DataResponse<Bitmap> response,
			final ICoverArt cover, final int thumbSize, final Context context,
			final boolean getFromCacheOnly) {
		if (DEBUG)
			Log.i(TAG, "[" + cover.getId() + "] Checking in disk cache..");
		DiskCacheThread.get().getCover(new DataResponse<Bitmap>() {
			public void run() {
				if (value == null) {
					if (DEBUG)
						Log.i(TAG,
								"[" + cover.getId()
										+ ThumbSize.getDir(thumbSize)
										+ "] Disk cache empty.");
					if (response.postCache()) {
						// well, let's download
						if (getFromCacheOnly) {
							if (DEBUG)
								Log.i(TAG,
										"[" + cover.getId()
												+ ThumbSize.getDir(thumbSize)
												+ "] Skipping download.");
							response.value = null;
							onFinish(response);
						} else {
							getCoverFromNetwork(response, cover, thumbSize,
									context);
						}
					}
				} else {
					if (DEBUG)
						Log.i(TAG,
								"[" + cover.getId()
										+ ThumbSize.getDir(thumbSize)
										+ "] FOUND on disk!");
					response.value = value;
					response.cacheType = CacheType.SDCARD;
					onFinish(response);
				}
			}
		}, cover, thumbSize, mController);
	}

	/**
	 * Last stop: try to download from XBMC.
	 * 
	 * @param response
	 *            Response object
	 * @param cover
	 *            Get cover for this object
	 * @param thumbSize
	 *            Cover size
	 */
	protected void getCoverFromNetwork(final DataResponse<Bitmap> response,
			final ICoverArt cover, final int thumbSize, final Context context) {
		if (DEBUG)
			Log.i(TAG, "[" + cover.getId() + "] Downloading..");
		DownloadThread.get().getCover(new DataResponse<Bitmap>() {
			public void run() {
				if (value == null) {
					if (DEBUG)
						Log.i(TAG, "[" + cover.getId() + "] Download empty");
				} else {
					if (DEBUG)
						Log.i(TAG, "[" + cover.getId() + "] DOWNLOADED ("
								+ value.getWidth() + "x" + value.getHeight()
								+ ")!");
					response.cacheType = CacheType.NETWORK;
					response.value = value;
				}
				onFinish(response); // callback in any case, since we don't go
									// further than that.
			}
		}, cover, thumbSize, mController, this, context);
	}

	public Bitmap getCoverSync(ICoverArt cover, int thumbSize) {
		if(MemCacheThread.isInCache(cover, thumbSize))
			return MemCacheThread.getCover(cover, thumbSize);
		else if(DiskCacheThread.isInCache(cover, thumbSize))
			return DiskCacheThread.getCover(cover, thumbSize);
		else
			return null;			
	}

	public boolean coverLoaded(ICoverArt cover, int thumbSize) {
		return (MemCacheThread.isInCache(cover, thumbSize) || DiskCacheThread.isInCache(cover, thumbSize));
	}

	public void onFinish(DataResponse<?> response) {
		if (mController != null) {
			mController.runOnUI(response);
		}
	}

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
		// TODO make this queue work

	}

	/**
	 * Sets the static reference to the preferences object. Used to obtain
	 * current sort values.
	 * 
	 * @param pref
	 */
	public void setPreferences(SharedPreferences pref) {
		mPref = pref;
	}

	/**
	 * Sets which kind of view is currently active.
	 * 
	 * @param sortKey
	 */
	public void setSortKey(int sortKey) {
		mCurrentSortKey = sortKey;
	}

	public void setIgnoreArticle(boolean ignoreArticle) {
		// FIXME: make this configurable and respected
		mCurrentIgnoreArticle = ignoreArticle;
	}

	/**
	 * Returns currently saved "sort by" value. If the preference was not set
	 * yet, or if the current sort key is not set, return default value.
	 * 
	 * @param type
	 *            Default value
	 * @return Sort by field
	 */
	protected String getSortBy(String type) {
		if (mPref != null) {
			int sort = mPref.getInt(PREF_SORT_BY_PREFIX + mCurrentSortKey, -1);
			switch (sort) {
			case SortType.ALBUM:
				return "album";
			case SortType.ARTIST:
				return "artist";
			case SortType.GENRE:
				return "genre";
			case SortType.FILENAME:
				return "file";
			case SortType.TITLE:
				return "title";
			case SortType.YEAR:
				return "year";
			case SortType.EPISODE_TITLE:
				return "episode";
			case SortType.PLAYCOUNT:
				return "playcount";
			case SortType.DATE_ADDED:
				return "dateadded";
			case SortType.LASTPLAYED:
				return "lastplayed";
			default:
				return type;
			}
		}
		return type;
	}

	/**
	 * Returns an object representing the current sort.
	 * 
	 * @param type
	 *            sorting field
	 * @return Sort
	 */
	public Sort getSort(String method) {
		return new Sort(mCurrentIgnoreArticle, getSortBy(method),
				getSortOrder());
	}

	/**
	 * Returns currently saved "sort by" value. If the preference was not set
	 * yet, or if the current sort key is not set, return "ASC".
	 * 
	 * @return Sort order
	 */
	protected String getSortOrder() {
		String order = SortType.ORDER_ASC;
		if (mPref != null) {
			order = mPref.getString(PREF_SORT_ORDER_PREFIX + mCurrentSortKey,
					SortType.ORDER_ASC);
		}
		if (order.equals(SortType.ORDER_ASC)) {
			return "ascending";
		}
		return "descending";
	}

	/**
	 * Downloads a cover.
	 * 
	 * First, only boundaries are downloaded in order to determine the sample
	 * size. Setting sample size > 1 will do two things:
	 * <ol>
	 * <li>Only a fragment of the total size will be downloaded</li>
	 * <li>Resizing will be smooth and not pixelated as before</li>
	 * </ol>
	 * The returned size is the next bigger (but smaller than the double) size
	 * of the original image.
	 * 
	 * @param manager
	 *            Postback manager
	 * @param cover
	 *            Cover object
	 * @param size
	 *            Minmal size to pre-resize to.
	 * @param url
	 *            URL to primary cover
	 * @param fallbackUrl
	 *            URL to fallback cover
	 * @return Bitmap
	 */
	protected Bitmap getCover(ICoverArt cover, int size, String url,
			String fallbackUrl) {
		final int mediaType = cover.getMediaType();
		// don't fetch small sizes
		size = size < ThumbSize.BIG ? ThumbSize.MEDIUM : ThumbSize.BIG;
		InputStream is = null;
		try {
			Log.i(TAG, "Starting download (" + HostFactory.host.getVfsUrl(url) + ")");

			BitmapFactory.Options opts = prefetch(HostFactory.host.getVfsUrl(url), size, mediaType);
			Dimension dim = ThumbSize.getTargetDimension(size, mediaType,
					opts.outWidth, opts.outHeight);

			Log.i(TAG, "Pre-fetch: " + opts.outWidth + "x" + opts.outHeight
					+ " => " + dim);
			if (opts.outWidth < 0) {
				if (fallbackUrl != null) {
					Log.i(TAG, "Starting fallback download (" + fallbackUrl
							+ ")");
					opts = prefetch(fallbackUrl, size, mediaType);
					dim = ThumbSize.getTargetDimension(size, mediaType,
							opts.outWidth, opts.outHeight);
					Log.i(TAG, "FALLBACK-Pre-fetch: " + opts.outWidth + "x"
							+ opts.outHeight + " => " + dim);
					if (opts.outWidth < 0) {
						return null;
					} else {
						url = fallbackUrl;
					}
				} else {
					Log.i(TAG, "Fallback url is null, returning null-bitmap");
					return null;
				}
			}
			final int ss = ImportUtilities.calculateSampleSize(opts, dim);
			Log.i(TAG, "Sample size: " + ss);

			is = new BufferedInputStream(getInputStream(HostFactory.host.getVfsUrl(url)), 8192);
			opts.inDither = true;
			opts.inSampleSize = ss;
			opts.inJustDecodeBounds = false;

			Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
			if (ss == 1) {
				bitmap = blowup(bitmap);
			}
			is.close();
			if (bitmap == null) {
				Log.i(TAG, "Fetch: Bitmap is null!!");
				return null;
			} else {
				Log.i(TAG,
						"Fetch: Bitmap: " + bitmap.getWidth() + "x"
								+ bitmap.getHeight());
				return bitmap;
			}
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}
		return null;
	}
	
	private InputStream getInputStream(String downloadURI) throws IOException {
		final URL u = new URL(downloadURI);
		Log.i(TAG, "Returning input stream for " + u.toString());
		URLConnection uc;
		uc = u.openConnection();
		uc.setConnectTimeout(5000);
		uc.setReadTimeout(HostFactory.host.getTimeout());
		return uc.getInputStream();
	}
	
	private BitmapFactory.Options prefetch(String url, int size, int mediaType) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		try {
			InputStream is = new BufferedInputStream(getInputStream(url), 8192);
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, opts);
		} catch (IOException e) {
			Log.e("Client", e.getMessage(), e);
			return opts;
		}
		return opts;
	}

	/**
	 * Doubles the size of a bitmap and re-reads it with samplesize 2. I've
	 * found no other way to smoothely resize images with samplesize = 1.
	 * 
	 * @param source
	 * @return
	 */
	private Bitmap blowup(Bitmap source) {
		if (source != null) {
			Bitmap big = Bitmap.createScaledBitmap(source,
					source.getWidth() * 2, source.getHeight() * 2, true);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 2;

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			big.compress(CompressFormat.PNG, 100, os);

			byte[] array = os.toByteArray();
			return BitmapFactory.decodeByteArray(array, 0, array.length, opts);
		}
		return null;
	}

	protected void setPlaylist(int playlistid, DataResponse<Boolean> response,
			int position, Context context) {
		call(new Player.GoTo(playlistid, position),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	protected void removeFromPlaylist(int playlistid,
			DataResponse<Boolean> response, int position, Context context) {
		call(new Playlist.Remove(playlistid, position),
				new ApiHandler<Boolean, String>() {
					@Override
					public Boolean handleResponse(AbstractCall<String> apiCall) {
						return "OK".equals(apiCall.getResult());
					}
				}, response, context);
	}

	protected void getPlaylist(int playlistid,
			DataResponse<ArrayList<String>> response, Context context) {
		call(new Playlist.GetItems(playlistid),
				new ApiHandler<ArrayList<String>, ListModel.AllItems>() {

					@Override
					public ArrayList<String> handleResponse(
							AbstractCall<ListModel.AllItems> apiCall) {
						ArrayList<String> playlistItems = new ArrayList<String>();

						ArrayList<ListModel.AllItems> items = apiCall
								.getResults();
						if (items == null || items.size() == 0) {

							playlistItems.add(EMPTY_PLAYLIST_ITEM);
						}
						for (ListModel.AllItems item : items) {
							playlistItems.add(item.label);
						}
						return playlistItems;
					}
				}, response, context);

	}

	protected void getPlaylistPosition(int playlistid,
			DataResponse<Integer> response, Context context) {
		call(new Player.GetProperties(playlistid, "position"),
				new ApiHandler<Integer, PlayerModel.PropertyValue>() {

					@Override
					public Integer handleResponse(
							AbstractCall<PlayerModel.PropertyValue> apiCall) {
						PlayerModel.PropertyValue properties = apiCall
								.getResult();
						return properties.position;
					}
				}, response, context);

	}

}
