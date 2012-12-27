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

package org.xbmc.jsonrpc.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.type.SortType;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.api.type.ThumbSize.Dimension;
import org.xbmc.jsonrpc.Connection;

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
	
	public static final String TAG = "Client-JSON-RPC";
	public static final String PARAM_FIELDS = "fields";
	public static final String PARAM_PROPERTIES = "properties";
	public static final String PARAM_SORT = "sort";

	public final static ObjectMapper MAPPER = new ObjectMapper();
	public final static JsonNodeFactory FACTORY = JsonNodeFactory.instance;

	protected final Connection mConnection;

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	Client(Connection connection) {
		mConnection = connection;
	}
	
	public int getActivePlayerId(INotifiableManager manager){
		final JsonNode active = mConnection.getJson(manager, "Player.GetActivePlayers", null).get(0);
		if(active == null)
			return -1;
		else
			return getInt(active, "playerid");
	}
	
	/**
	 * Downloads a cover. 
	 * 
	 * First, only boundaries are downloaded in order to determine the sample
	 * size. Setting sample size > 1 will do two things:
	 * <ol><li>Only a fragment of the total size will be downloaded</li>
	 *     <li>Resizing will be smooth and not pixelated as before</li></ol>
	 * The returned size is the next bigger (but smaller than the double) size
	 * of the original image.
	 * @param manager Postback manager
	 * @param cover Cover object
	 * @param size Minmal size to pre-resize to.
	 * @param url URL to primary cover
	 * @param fallbackUrl URL to fallback cover
	 * @return Bitmap
	 */
	protected Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size, String url) {
		
		if(url == null)
			return null;
		
		final int mediaType = cover.getMediaType();
		// don't fetch small sizes
		size = size < ThumbSize.BIG ? ThumbSize.MEDIUM : ThumbSize.BIG;
		InputStream is = null;
		try {
			Log.i(TAG, "Starting download (" + url + ")");
			
			BitmapFactory.Options opts = prefetch(manager, url, size, mediaType);
			Dimension dim = ThumbSize.getTargetDimension(size, mediaType, opts.outWidth, opts.outHeight);
			
			Log.i(TAG, "Pre-fetch: " + opts.outWidth + "x" + opts.outHeight + " => " + dim);
			if (opts.outWidth < 0) {
				return null;
			}
			final int ss = ImportUtilities.calculateSampleSize(opts, dim);
			Log.i(TAG, "Sample size: " + ss);
			
			is = new BufferedInputStream(mConnection.getThumbInputStream(url, manager), 8192);
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
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
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
		BitmapFactory.Options opts = new BitmapFactory.Options();
		try {
			InputStream is = new BufferedInputStream(mConnection.getThumbInputStream(url, manager), 8192);
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, opts);
		} catch (FileNotFoundException e) {
			return opts;
		}
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
	
	/**
	 * Returns an SQL String of given sort options of albums query
	 * @param sortBy    Sort field
	 * @param sortOrder Sort order
	 * @return SQL "ORDER BY" string
	 */
	protected static ObjNode sort(ObjNode params, int sortBy, String sortOrder) {
		
			
		
		final String order = sortOrder.equals(SortType.ORDER_DESC) ? "descending" : "ascending";
		final String sortby;
		switch (sortBy) {
			default:
			case SortType.ALBUM:
				sortby = "label";
				break;
			case SortType.ARTIST:
				sortby = "artist";
				break;
			case SortType.TRACK:
				sortby = "track";
				break;
			case SortType.TITLE:
				sortby = "sorttitle";
				break;
			case SortType.YEAR:
				sortby = "year";
				break;
			case SortType.RATING:
				sortby = "rating";
				break;
			case SortType.DATE_ADDED:
				sortby = "dateadded";
				break;
		}
		
		params.p(PARAM_SORT, obj().p("ignorearticle", true).p("method", sortby).p("order", order));
		return params;
	}
	
	public final static ObjNode obj() {
		return new ObjNode(FACTORY);
	}

	public static class ObjNode extends ObjectNode {
		public ObjNode(JsonNodeFactory nc) {
			super(nc);
		}
		public ObjNode p(String fieldName, Object object) {
			super.put(fieldName, (JsonNode) object);
			return this;
		}
		public ObjNode p(String fieldName, String v) {
			super.put(fieldName, v);
			return this;
		}
		public ObjNode p(String fieldName, int v) {
			super.put(fieldName, v);
			return this;
		}
		public ObjNode p(String fieldName, boolean v) {
			super.put(fieldName, v);
			return this;
		}
	};

	public final static ArrayNode arr() {
		return MAPPER.createArrayNode();
	}
	
	public final static String getString(JsonNode obj, String key) {
		
		if(obj.get(key) == null)
			return "";
		else if(obj.get(key).isArray()){
			String retval = "";
			for (Iterator<JsonNode> i = obj.get(key).getElements(); i.hasNext();) {
				retval += i.next().getTextValue();
				if(i.hasNext())
					retval += ", ";
			}
			return retval;
		}
		else
			return getString(obj, key, "");
	}
	public final static String getString(JsonNode obj, String key, String ifNullResult) {
		return obj.get(key) == null ? ifNullResult : obj.get(key).getTextValue();
	}

	public final static int getInt(JsonNode obj, String key) {
		return obj.get(key) == null ? -1 : obj.get(key).getIntValue();
	}

	public final static double getDouble(JsonNode obj, String key) {
		
		if(obj.get(key) == null)
			return -1;
		
		DecimalFormat twoDForm = new DecimalFormat("#.0");
		
		double val = -1;
		try{
			val = Double.valueOf(twoDForm.format(obj.get(key).getDoubleValue()).replace(',', '.'));
		}
		catch(NumberFormatException e){
			val = -1;
		}
		
		return val;
	}
}
