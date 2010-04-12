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

package org.xbmc.httpapi.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xbmc.android.util.Base64;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.api.type.ThumbSize.Dimension;
import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.WrongDataFormatException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

/**
 * Abstract super class of all (media) clients.
 * 
 * @author Team XBMC
 */
public abstract class Client {
	
	public static final String TAG = "Client";
	
	protected final Connection mConnection;

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	Client(Connection connection) {
		mConnection = connection;
	}
	
	/**
	 * Downloads a cover. 
	 * 
	 * First, only boundaries are downloaded in order to determine the sample
	 * size. Setting sample size > 1 will do two things:
	 * <ol><li>Only a fragment of the total size will be downloaded</li>
	 *     <li>Resizing will be smooth and not pixelated as before</li></ol>
	 * The base64-decoding is done using an inputstream directly, without 
	 * having to save the reponse to a String first.
	 * The returned size is the next bigger (but smaller than the double) size
	 * of the original image.
	 * @param manager Postback manager
	 * @param cover Cover object
	 * @param size Minmal size to pre-resize to.
	 * @param url URL to primary cover
	 * @param fallbackUrl URL to fallback cover
	 * @return Bitmap
	 */
	protected Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size, String url, String fallbackUrl) {
		final int mediaType = cover.getMediaType();
		// don't fetch small sizes
		size = size < ThumbSize.BIG ? ThumbSize.MEDIUM : ThumbSize.BIG;
		InputStream is = null;
		try {
			
			// DO THIS EVERY FUCKING TIME!!
			Log.i(TAG, "Setting response format");
			mConnection.assertBoolean(manager, "SetResponseFormat", "WebHeader;false;WebFooter;false");
			Log.i(TAG, "Starting download (" + url + ")");
			
			BitmapFactory.Options opts = prefetch(manager, url, size, mediaType);
			Dimension dim = ThumbSize.getDimension(size, mediaType, opts.outWidth, opts.outHeight);
			
			Log.i(TAG, "Pre-fetch: " + opts.outWidth + "x" + opts.outHeight + " => " + dim);
			if (opts.outWidth < 0) {
				if (fallbackUrl != null) {
					Log.i(TAG, "Starting fallback download (" + fallbackUrl + ")");
					opts = prefetch(manager, fallbackUrl, size, mediaType);
					dim = ThumbSize.getDimension(size, mediaType, opts.outWidth, opts.outHeight);
					Log.i(TAG, "FALLBACK-Pre-fetch: " + opts.outWidth + "x" + opts.outHeight + " => " + dim);
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
			
			is = new Base64.InputStream(new BufferedInputStream(mConnection.getInputStream("FileDownload", url, manager), 8192));
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
				Log.i(TAG, "Fetch: Bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
				return bitmap;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WrongDataFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) { }
		}
		return null;
	}
	
	private BitmapFactory.Options prefetch(INotifiableManager manager, String url, int size, int mediaType) {
		InputStream is = new Base64.InputStream(new BufferedInputStream(mConnection.getInputStream("FileDownload", url, manager), 8192));
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, opts);
		return opts;
	}
	
	/**
	 * Doubles the size of a bitmap and re-reads it with samplesize 2. I've 
	 * found no other way to smoothely resize images with samplesize = 1.
	 * @param source
	 * @return
	 */
	private Bitmap blowup(Bitmap source) {
		if (source != null) {
			Bitmap big = Bitmap.createScaledBitmap(source, source.getWidth() * 2,  source.getHeight() * 2, true);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 2;
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			big.compress(CompressFormat.PNG, 100, os);            
			
			byte[] array = os.toByteArray();
			return BitmapFactory.decodeByteArray(array, 0, array.length, opts);
		}
		return null;
	}	
}
