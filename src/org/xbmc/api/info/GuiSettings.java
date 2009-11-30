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

package org.xbmc.api.info;

public class GuiSettings {
	
	public static class MusicLibrary {
		
		public static final int LIBARY_ENABLED = 418;
		public static final int ALBUM_ARTISTS_ONLY = 13414;
		
		private static final String NAME_PREFIX = "musiclibrary.";
		
		public static String getName(int name) {
			switch (name) {
				case LIBARY_ENABLED: return NAME_PREFIX + "enabled";
				case ALBUM_ARTISTS_ONLY: return NAME_PREFIX + "albumartistsonly";
			}
			return null;
		}
		public static String getType(int name) {
			switch (name) {
				// boolean
				case LIBARY_ENABLED: 
				case ALBUM_ARTISTS_ONLY: 
					return "1";
			}
			return null;
		}
	}

}