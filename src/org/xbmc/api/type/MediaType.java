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

package org.xbmc.api.type;

public abstract class MediaType {
	
	public static final int UNKNOWN = -1;
	public static final int MUSIC = 1;
	public static final int VIDEO = 2;
	public static final int PICTURES = 3;
	
	public static String getName(int type) {
		switch (type) {
			case MUSIC:
				return "music";
			case VIDEO:
				return "video";
			case PICTURES:
				return "pictures";
			default:
				return "";
		}
	}
	
	/**
	 * Returns all media types.
	 * @return
	 */
	public static int[] getTypes() {
		int[] types = new int[3];
		types[0] = MUSIC;
		types[1] = VIDEO;
		types[2] = PICTURES;
		return types;
	}
	
	public static String getArtFolder(int type) {
		switch (type) {
			case MUSIC:
				return "/Music";
			case VIDEO:
				return "/Video";
			case PICTURES:
				return "/Pictures";
			default:
				return "";
		}
	}
	
}