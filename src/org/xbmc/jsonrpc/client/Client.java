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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IClient;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Host;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.Sort;
import org.xbmc.api.type.SortType;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.api.type.ThumbSize.Dimension;
import org.xbmc.api.type.Version;
import org.xbmc.jsonrpc.Connection;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Abstract super class of all (media) clients.
 * 
 * @author Team XBMC
 */
public abstract class Client implements IClient {
	
	public static final Integer PLAYLIST_MUSIC = 0;
	public static final Integer PLAYLIST_VIDEO = 1;
	public static final Integer PLAYLIST_PICTURE = 2;

	
	public static final String TAG = "Client-JSON-RPC";
	public static final String PARAM_FIELDS = "fields";

	public final static ObjectMapper MAPPER = new ObjectMapper();
	public final static JsonNodeFactory FACTORY = JsonNodeFactory.instance;

	protected final Connection mConnection;
	private int apiVersion = -1;
	

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	Client(Connection connection) {
		mConnection = connection;
	}
	
	public void setHost(Host host) {
		// reset the API Version
		apiVersion = -1;
		mConnection.setHost(host);
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
	protected Bitmap getCover(INotifiableManager manager, ICoverArt cover, int size, String url, String fallbackUrl) {
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
				if (fallbackUrl != null) {
					Log.i(TAG, "Starting fallback download (" + fallbackUrl + ")");
					opts = prefetch(manager, fallbackUrl, size, mediaType);
					dim = ThumbSize.getTargetDimension(size, mediaType, opts.outWidth, opts.outHeight);
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
			Log.e("Client", e.getMessage(), e);
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
	
	public final static ObjNode obj() {
		return new ObjNode(FACTORY);
	}

	public static class ObjNode extends ObjectNode {
		public ObjNode(JsonNodeFactory nc) {
			super(nc);
		}
		public ObjNode p(String fieldName, JsonNode value) {
			super.put(fieldName, value);
			return this;
		}
		public ObjNode p(String fieldName, String v) {
			super.put(fieldName, v);
			return this;
		}
		public ObjNode p(String fieldName, boolean v) {
			super.put(fieldName, v);
			return this;
		}
		public ObjNode p(String fieldName, Integer v) {
			super.put(fieldName, v);
			return this;
		}
	};

	public final static ArrayNode arr() {
		return MAPPER.createArrayNode();
	}
	
	public final static String getString(JsonNode obj, String key) {
		return getString(obj, key, "");
	}
	public final static String getString(JsonNode obj, String key, String ifNullResult) {
		return obj.get(key) == null ? ifNullResult : obj.get(key).getTextValue();
	}
	
	public final static Double getDouble(JsonNode obj, String key) {
		return getDouble(obj, key, 0d);
	}
	
	public final static Double getDouble(JsonNode obj, String key, Double ifNullResult) {
		return obj.get(key) == null ? ifNullResult : obj.get(key).getDoubleValue();
	}
	
	public final static JsonNode getNode(JsonNode obj, String key) {
		return (JsonNode) obj.get(key);
	}
	public final static int getInt(JsonNode obj, String key) {
		return obj.get(key) == null ? -1 : obj.get(key).getIntValue();
	}
	
	protected ObjNode filter(ObjNode params, Object ... filterStrings) {
		if(getAPIVersion() >= Version.FRODO.ordinal()) {
			
			ObjNode filters = obj();
			for(int i = 0; i < filterStrings.length; i+=2) {
				addObject(filters, filterStrings[i].toString(), filterStrings[i + 1]);
			}
			params.p("filter", filters);
			return params;
		}
		for(int i = 0; i < filterStrings.length; i+=2) {
			addObject(params, filterStrings[i].toString(), filterStrings[i + 1]);
		}
		return params;
	}	
	
	private void addObject(ObjNode parent, String key, Object value) {
		if(value instanceof Integer) {
			parent.p(key, (Integer) value);
		}
		else if(value instanceof JsonNode) {
			parent.p(key, (JsonNode) value);
		}
		else if(value instanceof Boolean) {
			parent.p(key, (Boolean) value);
		}
		else {
			parent.p(key, value.toString());
		}
	}
	
	/**
	 * Returns an object node of given sort options of albums query
	 * @param sortBy    Sort field
	 * @param sortOrder Sort order
	 * @return query ObjNode
	 */
	protected ObjNode sort(ObjNode params, Sort sort) {
		
		final boolean ignoreArticle = sort.ignoreArticle;
		final String order = sort.sortOrder.equals(SortType.ORDER_DESC) ? "descending" : "ascending";
		switch (sort.sortBy) {
			default:

			case SortType.GENRE:
				params.p("sort", MusicClient.obj().p("order", order).p("method", "genre").p("ignorearticle", ignoreArticle));
				break;
			case SortType.ALBUM:
				params.p("sort", MusicClient.obj().p("order", order).p("method", "label").p("ignorearticle", ignoreArticle));
				break;
			case SortType.ARTIST:
				params.p("sort", MusicClient.obj().p("order", order).p("method", "artist").p("ignorearticle", ignoreArticle));
				break;
			case SortType.TRACK:
				params.p("sort", MusicClient.obj().p("order", order).p("method", "track").p("ignorearticle", ignoreArticle));
				break;
			case SortType.DATE_ADDED:
				params.p("sort", MusicClient.obj().p("order", order).p("method", "dateadded").p("ignorearticle", ignoreArticle));
				break;
			case SortType.PLAYCOUNT:
				params.p("sort", MusicClient.obj().p("order", order).p("method", "playcount").p("ignorearticle", ignoreArticle));
				break;
			case SortType.LASTPLAYED:
				params.p("sort", MusicClient.obj().p("order", order).p("method", "lastplayed").p("ignorearticle", ignoreArticle));
				break;
		}
		return params;
	}
	
	public JsonNode getActivePlayer(INotifiableManager manager) {
		ArrayNode result = (ArrayNode) mConnection.getJson(manager, "Player.GetActivePlayers");
		if(result == null || result.size() == 0) {
			// not currently playing
			return null;
		}
		JsonNode player = result.get(0);
		return player;
	}
	
	
	public Integer getActivePlayerId(INotifiableManager manager) {
		ArrayNode result = (ArrayNode) mConnection.getJson(manager, "Player.GetActivePlayers");
		if(result == null || result.size() == 0) {
			// not currently playing
			return null;
		}
		JsonNode player = result.get(0);
		return player.get("playerid").getIntValue();
	}
	
	public Integer getActivePlayerId(INotifiableManager manager, int type) {
		
		ArrayNode result = (ArrayNode) mConnection.getJson(manager, "Player.GetActivePlayers");
		if(result.size() == 0) {
			// not currently playing
			return null;
		}
		for(JsonNode player : result) {
			if(player.get("type").getTextValue().equals(MediaType.getName(type))) {
				return player.get("playerid").getIntValue();
			}
		}
		return null;
	}
	
	protected ArrayList<Genre> parseGenres(JsonNode result) {
		ArrayList<Genre> genres = new ArrayList<Genre>();
		final JsonNode jsonAlbums = result.get("genres");
		if(jsonAlbums == null) {
			return genres;
		}
		
		for (Iterator<JsonNode> i = jsonAlbums.getElements(); i.hasNext();) {
			JsonNode jsonArtist = (JsonNode)i.next();
			genres.add(new Genre(
					getInt(jsonArtist, "genreid"), 
					getString(jsonArtist, "label") 
					));			
		}
		
		return genres;
	}

	public int getAPIVersion() {
		return getAPIVersion(null);
	}
	public int getAPIVersion(INotifiableManager manager) {
		if(apiVersion > 0) {
			return apiVersion;
		}
		String version = mConnection.getString(manager, "JSONRPC.Version", "version");
		apiVersion = Integer.parseInt(version);
		return apiVersion;
	}


	
}
