package org.xbmc.api.object;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.jsonrpc.api.model.VideoModel.TVShowDetail;
import org.xbmc.android.util.Crc32;
import org.xbmc.api.type.MediaType;

public class TvShow implements ICoverArt, INamedResource {

	public final static String TAG = "TvShow";
	/**
	 * Points to where the movie thumbs are stored
	 */
	public final static String THUMB_PREFIX = "special://profile/Thumbnails/Video/";
	
	/**
	 * Save this once it's calculated
	 */
	public long thumbID = 0L;
	
	protected String thumbnail; 
	
	public List<Season> seasons = new ArrayList<Season>();
	public List<Actor> actors = new ArrayList<Actor>();
	
	public TvShow(int id, String title, String summary, double rating, String firstAired, 
			String genre, String contentRating, String network, String path, int numEpisodes, int watchedEpisodes, boolean watched) {
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.rating = rating;
		this.firstAired = firstAired;
		this.contentRating = contentRating;
		this.network.add(network);
		this.genre.add(genre);
		this.path = path;
		this.numEpisodes = numEpisodes;
		this.watchedEpisodes = watchedEpisodes;
		this.watched = watched;
	}
	
	public TvShow(int id) {
		this.id = id;
	}
	
	public TvShow(TVShowDetail detail) {
		this.id = detail.tvshowid;
		this.title = detail.title;
		this.summary = detail.plot;
		this.rating = detail.rating;
		this.firstAired = detail.premiered;
		this.contentRating = Double.toString(detail.rating);
		this.network = detail.studio;
		this.genre = detail.genre;
		this.path = detail.file;
		this.numEpisodes = detail.episode;
		this.watchedEpisodes = detail.watchedepisodes;
		this.thumbnail = detail.thumbnail;
	}

	public String getShortName() {
		return title;
	}
	
	public static String getThumbUri(ICoverArt cover) {
		// use the banner if possible
		if (cover.getMediaType() == MediaType.VIDEO_TVSHOW) {
			return cover.getPath() != null ? cover.getPath().replace("\\", "/") + "banner.jpg" : getFallbackThumbUri(cover);
		}
		
		// then use the thumbnail
		if(cover.getThumbnail() != null) {
			return cover.getThumbnail();
		}

		// then fallback
		if (cover.getMediaType() == MediaType.VIDEO_TVEPISODE) {
			final String hex = Crc32.formatAsHexLowerCase(cover.getCrc());
			return THUMB_PREFIX + hex.charAt(0) + "/" + hex + ".tbn";
		} 
		return getFallbackThumbUri(cover);
	}
	
	public static String getFallbackThumbUri(ICoverArt cover) {
		final String hex;
		if (cover.getMediaType() == MediaType.VIDEO_TVEPISODE) {
			hex = Crc32.formatAsHexLowerCase(cover.getFallbackCrc());
		} else {
			hex = Crc32.formatAsHexLowerCase(cover.getCrc());
		}
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
	
	public String getThumbnail() {
		return thumbnail;
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
	public List<String> network = new ArrayList<String>();
	public List<String> genre = new ArrayList<String>();
	public String path;
	public int numEpisodes;
	public int watchedEpisodes;
	public boolean watched;
	
	private static final long serialVersionUID = -902152099894950269L;
	
}
