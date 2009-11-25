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

package org.xbmc.httpapi.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.xbmc.android.util.Crc32;
import org.xbmc.httpapi.type.MediaType;

/**
 * Stores what we can get from the movieview table.
 * 
 * @author Team XBMC
 */
public class Movie implements ICoverArt, Serializable, NamedResource {
	
	/**
	 * Points to where the movie thumbs are stored
	 */
	public final static String THUMB_PREFIX = "special://masterprofile/Thumbnails/Video/";

	/**
	 * Constructor
	 * @param id		Database ID
	 * @param name		Album name
	 * @param artist	Artist
	 */
	public Movie(int id, String title, int year, String path, String filename, String director, String runtime, String genres, double rating) {
		this.id = id;
		this.title = title;
		this.year = year;
		this.director = director;
		this.runtime = runtime;
		this.genres = genres;
		this.rating = rating;
		this.localPath = path;
		this.filename = filename;
	}
	
	public int getMediaType() {
		return MediaType.VIDEO;
	}
	
	/**
	 * Returns the path XBMC needs to play the movie. This can either
	 * localPath + filename or filename only (in case of stacks) 
	 * @return
	 */
	public String getPath() {
		if (filename.contains("://")) {
			return filename;
		} else {
			return localPath + filename;
		}
	}

	public String getShortName() {
		return this.title;
	}
	
	/**
	 * Composes the complete path to the album's thumbnail
	 * @return Path to thumbnail
	 */
	public String getThumbUri() {
		return getThumbUri(this);
	}
	
	public static String getThumbUri(ICoverArt cover) {
		final String hex = Crc32.formatAsHexLowerCase(cover.getCrc());
		return THUMB_PREFIX + hex.charAt(0) + "/" + hex + ".tbn";
	}
	
	public static String getFallbackThumbUri(ICoverArt cover) {
		final int crc = cover.getFallbackCrc();
		if (crc != 0) {
			final String hex = Crc32.formatAsHexLowerCase(cover.getFallbackCrc());
			return THUMB_PREFIX + hex.charAt(0) + "/" + hex + ".tbn";
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the CRC of the album on which the thumb name is based upon.
	 * @return CRC32
	 */
	public long getCrc() {
		if (thumbID == 0L) {
			thumbID = Crc32.computeLowerCase(localPath);
		}
		return thumbID;
	}
	
	/**
	 * If no album thumb CRC is found, try to get the thumb of the album's
	 * directory.
	 * @return 0-char CRC32
	 */
	public int getFallbackCrc() {
		if (localPath != null) {
			final String lp = localPath;
			return Crc32.computeLowerCase(lp.substring(0, lp.length() - 1));
		} else {
			return 0;
		}
	}
	
	/**
	 * Returns database ID.
	 * @return
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Returns database ID.
	 * @return
	 */
	public String getName() {
		return title + " (" + year + ")";
	}
	
	/**
	 * Something descriptive
	 */
	public String toString() {
		return "[" + id + "] " + title + " (" + year + ")";
	}
	
	/**
	 * Database ID
	 */
	public int id;
	/**
	 * Movie title
	 */
	public String title;
	/**
	 * Director(s), can be several separated by " / "
	 */
	public String director;
	/**
	 * Runtime, can be several also, separated by " | "
	 */
	public String runtime;
	/**
	 * Genre(s), can be several, normally separated by " / "
	 */
	public String genres;
	/**
	 * Year released
	 */
	public int year = -1;
	
	/**
	 * Local path of the movie (without file name)
	 */
	public String localPath;
	
	/**
	 * File name of the movie
	 */
	public String filename;
	
	/**
	 * Rating
	 */
	public double rating = 0.0;
	
	/**
	 * URL to the trailer, if available.
	 */
	public String trailerUrl = null;
	/**
	 * Movie plot
	 */
	public String plot = null;
	/**
	 * Movie's tagline 
	 */
	public String tagline = null;
	/**
	 * Number of votes, -1 if not set.
	 */
	public int numVotes = -1;
	/**
	 * Parental Rating with description (e.g.: "Rated R for strong violence, sexuality, drug use and language.")
	 */
	public String rated = null;
	/**
	 * Studio
	 */
	public String studio = null;
	
	/**
	 * List of actors; 
	 */
	public ArrayList<Actor> actors = null;
	
	
	
	/**
	 * Save this once it's calculated
	 */
	public long thumbID = 0L;
	
	private static final long serialVersionUID = 4779827915067184250L;

}
