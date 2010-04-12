package org.xbmc.api.object;

import java.util.List;

import org.xbmc.android.util.Crc32;
import org.xbmc.api.type.MediaType;

public class TvShow implements ICoverArt, INamedResource {

	public final static String TAG = "TvShow";
	/**
	 * Points to where the movie thumbs are stored
	 */
	public final static String THUMB_PREFIX = "special://masterprofile/Thumbnails/Video/";
	
	/**
	 * Save this once it's calculated
	 */
	public long thumbID = 0L;
	
	public List<Season> seasons = null;
	public List<Actor> actors = null;
	
	public TvShow(int id, String title, String summary, double rating, String firstAired, 
			String genre, String contentRating, String network, String path, int numEpisodes, int watchedEpisodes, boolean watched) {
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.rating = rating;
		this.firstAired = firstAired;
		this.contentRating = contentRating;
		this.network = network;
		this.genre = genre;
		this.path = path;
		this.numEpisodes = numEpisodes;
		this.watchedEpisodes = watchedEpisodes;
		this.watched = watched;
	}

	public String getShortName() {
		return title;
	}
	
	/**
	 * Composes the complete path to the album's thumbnail
	 * @return Path to thumbnail
	 */
	public String getThumbUri() {
		return getThumbUri(this);
	}
	
	public static String getThumbUri(ICoverArt cover) {
		return getFallbackThumbUri(cover);
//		return cover.getPath() + "banner.jpg";
	}
	
	public static String getFallbackThumbUri(ICoverArt cover) {
		final String hex = Crc32.formatAsHexLowerCase(cover.getCrc());
		return THUMB_PREFIX + hex.charAt(0) + "/" + hex + ".tbn";
	}

	public long getCrc() {
		if (thumbID == 0L) {
			thumbID = Crc32.computeLowerCase(path);
		}
		return thumbID;
	}

	public int getFallbackCrc() {
		return 0;
	}

	public int getId() {
		return id;
	}

	public int getMediaType() {
		return MediaType.VIDEO_TVSHOW;
	}

	public String getName() {
		return title;
	}
	
	public String getPath() {
		return path;
	}
	
	/**
	 * Something descriptive
	 */
	public String toString() {
		return "[" + id + "] " + title;
	}

	public int id;
	public String title;
	public String summary;
	public double rating = 0.0;
	public String firstAired;
	public String contentRating;
	public String network;
	public String genre;
	public String path;
	public int numEpisodes;
	public int watchedEpisodes;
	public boolean watched;
	
	private static final long serialVersionUID = -902152099894950269L;
	
}
