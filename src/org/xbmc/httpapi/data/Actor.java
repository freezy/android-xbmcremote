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

import org.xbmc.android.util.Crc32;
import org.xbmc.httpapi.type.MediaType;

/**
 * Actor is (for now) a rip-off of artist. It's the same thing named differently for movies.
 * 
 * @author Team XBMC
 */
public class Actor extends Artist {
	
	public final static String THUMB_PREFIX = "special://masterprofile/Thumbnails/Video/";

	public Actor(int id, String name) {
		super(id, name);
	}
	
	public Actor(int id, String name, String role) {
		super(id, name);
		this.role = role;
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
		return THUMB_PREFIX + hex.charAt(0) + "/" + hex + ".tbn";
	}
	
	public int getMediaType() {
		return MediaType.VIDEO;
	}
	
	/**
	 * Returns the CRC of the artist on which the thumb name is based upon.
	 * @return 8-char CRC32
	 */
	public long getCrc() {
		if (thumbID == 0) {
//			thumbID = Crc32.computeLowerCase("videodb://1/4/" + id);
			thumbID = Crc32.computeLowerCase("actor" + name);
		}
		return thumbID;
	}
	
	public String role = null;

	private static final long serialVersionUID = -7026393902334967838L;
}