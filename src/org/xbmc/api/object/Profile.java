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
 * Stores what we can get from the profileview table.
 * 
 * @author Team XBMC
 */
public class Profile implements ICoverArt, Serializable, INamedResource {
	
	/**
	 * Constructor
	 * @param id		Database ID
	 * @param title		Profile name
	 * @param lockmode	0 is unlocked, anything else is locked 
	 * @param artUrl	Path for the profile thumbnail 
	 */
	public Profile(int id, String title, int lockmode, String artUrl) {
		this.id = id;
		this.title = title;
		this.lockmode = lockmode;
		this.artUrl = artUrl;
	}
	
	public int getMediaType() {
		return MediaType.PROFILE;
	}
	
	public String getPath() {
		return artUrl;
	}
	
	public String getShortName() {
		return this.title;
	}
	
	public static String getThumbUri(ICoverArt cover) {
		return cover.getThumbUrl();
	}
	
	/**
	 * Returns the CRC of the profile on which the thumb name is based upon.
	 * @return CRC32
	 */
	public long getCrc() {
		thumbID = Crc32.computeLowerCase(artUrl);
		return thumbID;
	}
	
	public int getFallbackCrc() {
		return Crc32.computeLowerCase(artUrl);
	}
	
	public String getThumbUrl() {
		return artUrl;
	}
	
	/**
	 * Returns database ID.
	 * @return
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Returns profile name.
	 * @return
	 */
	public String getName() {
		return title;
	}
	
	/**
	 * Something descriptive
	 */
	public String toString() {
		return "[" + id + "] " + title;
	}
	
	/**
	 * Database ID
	 */
	private final int id;
	/**
	 * Profile title
	 */

	public final String title;
	/**
	 * LockMode
	 */
	public final int lockmode;
	
	public String artUrl;
	/**
	 * Save this once it's calculated
	 */
	public long thumbID = 0L;
	
	private static final long serialVersionUID = 1208620993981377535L;

}
