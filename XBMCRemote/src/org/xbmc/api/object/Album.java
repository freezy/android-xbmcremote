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

package org.xbmc.api.object;

import java.io.Serializable;

import org.xbmc.android.util.Crc32;
import org.xbmc.api.type.MediaType;

/**
 * The album class keeps the basic album information from the album table
 * as well some of the extended info from the albuminfo table.
 * 
 * @author freezy <phreezie@gmail.com>
 */
public class Album implements ICoverArt, Serializable, INamedResource {

	/**
	 * Points to where the album thumbs are stored
	 */
	public final static String THUMB_PREFIX = "special://profile/Thumbnails/Music/";

	/**
	 * Constructor
	 * @param id      Database ID
	 * @param name    Album name
	 * @param artist  Artist
	 * @param year    Year
	 */
	public Album(int id, String name, String artist, int year) {
		this.id = id;
		this.name = name;
		this.artist = artist;
		this.year = year;
	}

	public Album(int id, String name, String artist, int year, String thumbPath) {
		this.id = id;
		this.name = name;
		this.artist = artist;
		this.year = year;
		thumbPath = thumbPath.replace("\\", "/");
		if (!thumbPath.equals("NONE")) {
			try {
				this.thumbID = Long.parseLong(thumbPath.substring(thumbPath.lastIndexOf("/") + 1, thumbPath.length() - 4), 16);
			} catch (NumberFormatException e) {
				this.thumbID = 0L;
			}
		}
	}
	
	public int getMediaType() {
		return MediaType.MUSIC;
	}

	public String getShortName() {
		return this.name;
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
			final String hex = Crc32.formatAsHexLowerCase(crc);
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
		if (thumbID == 0) {
			thumbID = Crc32.computeLowerCase((name + artist));
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
		return toString();
	}
	
	/**
	 * Returns local path name
	 * @return
	 */
	public String getPath() {
		return localPath;
	}
	
	/**
	 * Returns true if the album is a compilation, false otherwise. 
	 * @return True if compilation ("Various Artists"), false otherwise.
	 */
	public boolean isVA() {
		return artist.equalsIgnoreCase("Various Artists") 
			|| artist.equalsIgnoreCase("VariousArtists")
			|| artist.equalsIgnoreCase("VA")
			|| artist.equalsIgnoreCase("V A")
			|| artist.equalsIgnoreCase("V.A.") 
			|| artist.equalsIgnoreCase("V. A.");
	}

	/**
	 * Something descriptive
	 */
	public String toString() {
		return "[" + this.id + "] " + this.name + " (" + this.artist + ")";
	}
	
	/**
	 * Database ID
	 */
	public int id;
	/**
	 * Album name
	 */
	public String name;
	/**
	 * Artist name
	 */
	public String artist;
	/**
	 * Year published
	 */
	public int year = -1;
	
	/**
	 * Local path of the album
	 */
	public String localPath;
	
	/**
	 * Rating
	 */
	public int rating = -1;
	/**
	 * Genres, separated by " / "
	 */
	public String genres = null;
	/**
	 * Music label
	 */
	public String label = null;	
	/**
	 * Save this once it's calculated
	 */
	public long thumbID = 0;
	
	private static final long serialVersionUID = 4779827915067184250L;

}
