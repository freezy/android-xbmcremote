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

package org.xbmc.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.xbmc.android.remote.activity.AlbumAdapter;
import org.xbmc.android.remote.activity.AlbumGridActivity;
import org.xbmc.android.remote.activity.AlbumHolder;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.type.ThumbSize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

/**
 * The ImageLoader takes care of asynchronously loading covers from disk cache
 * or from XBMC directly. 
 *  
 * @author freezy <phreezie@gmail.com>
 */
public class ImageLoader extends Thread {
	
	public static ImageLoader imageCacheLoader;
	public static ImageLoader imageNetworkLoader;
	
	private static final HashMap<String, SoftReference<Bitmap>> sArtCache = new HashMap<String, SoftReference<Bitmap>>();
	
	private static AlbumAdapter sAdapter;
	private final BlockingQueue<ImageRequest> mQueue = new LinkedBlockingQueue<ImageRequest>();
	private static final Handler sThreadHandler = new Handler();
	
	private ImageRequest mCurrentRequest;
	
	/**
	 * GUI updates are not thread safe, so we need a reference to the adapter
	 * which takes care of that
	 * @param adapter
	 */
	public static void setAdapter(AlbumAdapter adapter) {
		sAdapter = adapter;
	}
	
	/**
	 * Class constructor of the server side
	 */
	public ImageLoader() {
		super("Image Loader Thread");
	}
	
	/**
	 * Adds a new request to the queue
	 * @param request
	 */
	public void accept(ImageRequest request) {
		try {
			// make sure the request is not already in the queue and update imageView reference if that's the case
			if (mCurrentRequest != null && mCurrentRequest.getCoverArt().getCrc().equals(request.getCoverArt().getCrc())) {
				return;
			}			
			for (ImageRequest ir : mQueue) {
				if (ir.getCoverArt().getCrc().equals(request.getCoverArt().getCrc())) {
					ir.setImage(request.getImage());
					return;
				}
			}
//			System.out.println("Adding to queue (" + request.getClass() + "): " + request.getCoverArt());
			mQueue.put(request);
		} catch (InterruptedException e) {
			throw new RuntimeException("add to queue interrupted");
		}
	}

	/**
	 * Starts the server thread.
	 */
	public void run() {
		while (!isInterrupted()) {
			try {
				execute(mQueue.take());
			} catch (InterruptedException e) {
				System.out.println("### INTERRUPTED!");
			}
		}
	}

	/**
	 * Once a request is scheduled to run, it is executed and the adapter is
	 * notified.
	 * @param request
	 */
	private void execute(final ImageRequest request) {
		if (request != null) {
			mCurrentRequest = request;
			request.execute();
			mCurrentRequest = null;
			if (request.getBitmap() != null) {
				((AlbumHolder)request.getImage().getTag()).type = "from network";
				pushImage(request.getBitmap(), request.getCoverArt(), request.getImage());
			}
		}
	}
	
	public static void pushImage(final Bitmap bitmap, final ICoverArt art, final ImageView image) {
		sThreadHandler.post(new Runnable() {
			public void run() {
				sAdapter.onImageReady(bitmap, art, image);
			}
		});
	}
	
    /**
     * Client side of the loader. If the bitmap is already in (memory) cache,
     * it is directly returned. Otherwise, null is returned and the bitmap
     * is loaded by the thread.
     * 
     * @param image ImageView object that will be updated upon completion
     * @param art   Reference to the cover
     * @param size	Which size should be chosen for the returned bitmap
     * @return 
     */
	public static Bitmap loadCachedCover(ImageView image, ICoverArt art, ThumbSize size) {
		AlbumHolder holder = (AlbumHolder)image.getTag();
		Bitmap drawable = holder.cover;
		
        SoftReference<Bitmap> ref = sArtCache.get(art.getCrc());
        if (ref != null) {
            drawable = ref.get();
        }
        if (drawable == null) {
            drawable = loadCover(image, art, size);
            if (drawable == null) {
            	((AlbumHolder)image.getTag()).type = "init";
            	return null; //AlbumGridActivity.coverInit;
            } else {
            	return drawable;
            }
        } else {
        	((AlbumHolder)image.getTag()).type = "from Cache";
        	return drawable;
        }
    }
    
    /**
     * If loadCachedCover was unable to load the image from cache, this will
     * try to load it from disk using the server thread
     * @param image ImageView object that will be updated upon completion
     * @param art   Reference to the cover
     * @param size	Which size should be chosen for the returned bitmap
     * @return
     */
	private static Bitmap loadCover(final ImageView image, final ICoverArt art, final ThumbSize size) {
        final File file = new File(ImportUtilities.getCacheDirectory(art.getArtFolder(), size), art.getCrc());
        if (file.exists()) {
           	imageCacheLoader.accept(new CacheRequest(file, image, art));
           	return null;
        } else {
        	imageNetworkLoader.accept(new DownloadRequest(image, art));
        	((AlbumHolder)image.getTag()).type = "queued";
        	return AlbumGridActivity.coverQueued;
        }
    }    
	
	/**
	 * Interface for download and cache requests
	 */
	private interface ImageRequest {
		public void execute();
		public ImageView getImage();
		public ICoverArt getCoverArt();
		public Bitmap getBitmap();
		public void setImage(ImageView image);
	}
	
	/**
	 * This class reads the image from disk cache
	 */
	private static class CacheRequest implements ImageRequest {
		private final File mFile;
		private final ICoverArt mArt;
		private ImageView mImage;
		private Bitmap mBitmap;
		public CacheRequest(File file, ImageView image, ICoverArt art) {
			mFile = file;
			mImage = image;
			mArt = art;
		}
		public void execute() {
			InputStream stream = null;
			try {
				stream = new FileInputStream(mFile);
				mBitmap = BitmapFactory.decodeStream(stream, null, null);
				sArtCache.put(mArt.getCrc(), new SoftReference<Bitmap>(mBitmap));
			} catch (FileNotFoundException e) {
				// Ignore
			} finally {
				IOUtilities.closeStream(stream);
			}
		}
		public Bitmap getBitmap() {
			return mBitmap;
		}
		public ICoverArt getCoverArt() {
			return mArt;
		}
		public ImageView getImage() {
			return mImage;
		}
		public void setImage(ImageView image) {
			mImage = image;
		}
	}
	
	/**
	 * This class reads the image from XBMC via network
	 */
	private static class DownloadRequest implements ImageRequest {
		private final static MusicClient sMdb = ConnectionManager.getHttpClient().music;
		private final ICoverArt mArt;
		private ImageView mImage;
		private Bitmap mBitmap;
		public DownloadRequest(ImageView image, ICoverArt art) {
			mImage = image;
			mArt = art;
		}
		public void execute() {
			((AlbumHolder)mImage.getTag()).type = "downloading";
			pushImage(AlbumGridActivity.coverDownloading, mArt, mImage);
			System.out.println("Downloading cover for " + mArt);
			String b64enc = sMdb.getAlbumThumb(mArt);
			System.out.println("Download done.");
			byte[] bytes;
			try {
				bytes = Base64.decode(b64enc);
				if (bytes.length > 0) {
					System.out.println("Decoding, resizing and adding to cache..");
					mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
					if (mBitmap != null) {
						ImportUtilities.addCoverToCache(mArt, mBitmap);
						System.out.println("Done");
						sArtCache.put(mArt.getCrc(), new SoftReference<Bitmap>(mBitmap));
					}
				}
			} catch (IOException e) {
				System.out.println("IOException " + e.getMessage());
				System.out.println(e.getStackTrace());
			}
		}
		public Bitmap getBitmap() {
			return mBitmap;
		}
		public ICoverArt getCoverArt() {
			return mArt;
		}
		public ImageView getImage() {
			return mImage;
		}
		public void setImage(ImageView image) {
			mImage = image;
		}
	}
}