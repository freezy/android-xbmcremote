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

import org.xbmc.android.util.Crc32;
import org.xbmc.httpapi.type.MediaType;

/**
 * Not very much going on, artist is basically a name.
 * 
 * @author Team XBMC
 */
public class Artist implements ICoverArt, Serializable, NamedResource {

	/**
	 * TODO verify that's correct and test!
	 * Points to where the artist thumbs are stored
	 */
	public final static String THUMB_PREFIX = "special://masterprofile/Thumbnails/Music/Artists/";

	/**
	 * Constructor
	 * @param id		Database ID
	 * @param name		Artist name
	 */
	public Artist(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getMediaType() {
		return MediaType.MUSIC;
	}

	public String getShortName() {
		return this.name;
	}
	
	/**
	 * Composes the complete path to the artist's thumbnail
	 * @return Path to thumbnail
	 */
	public String getThumbUri() {
		return getThumbUri(this);
	}
	
	public static String getThumbUri(ICoverArt cover) {
		final String hex = Crc32.formatAsHexLowerCase(cover.getCrc());
		return THUMB_PREFIX + hex + ".tbn";
	}
	
	/**
	 * Returns the CRC of the artist on which the thumb name is based upon.
	 * @return 8-char CRC32
	 */
	public long getCrc() {
		if (thumbID == 0) {
			thumbID = Crc32.computeLowerCase("artist" + id);
		}
		return thumbID;
	}

	/**
	 * No fallback here
	 */
	public int getFallbackCrc() {
		return 0;
	}
	
	/**
	 * Returns database ID.
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns database ID.
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Something descriptive
	 */
	public String toString() {
		return "[" + this.id + "] " + this.name;
	}
	
	/**
	 * Database ID
	 */
	public int id;
	/**
	 * Artist name
	 */
	public String name;
	
	public long thumbID = 0;
	
	private static final long serialVersionUID = 9073064679039418773L;
}
