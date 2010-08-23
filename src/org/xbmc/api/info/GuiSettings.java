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
	
	public static class Services {
		public static final int EVENTSERVER_ENABLED = 1;
		public static final int EVENTSERVER_ENABLED_ALL = 2;
		public static final int EVENTSERVER_PORT = 3;
		
		private static final String NAME_PREFIX = "services.";
		
		
	}
	
	public static class MusicLibrary {
		
		public static final int LIBARY_ENABLED = 418;
		public static final int SHOW_COMPLATION_ARTISTS = 13414;
		
		private static final String NAME_PREFIX = "musiclibrary.";
		
	}

	public static String getName(int name) {
		switch (name) {
			case MusicLibrary.LIBARY_ENABLED: 
				return MusicLibrary.NAME_PREFIX + "enabled";
			case MusicLibrary.SHOW_COMPLATION_ARTISTS: 
				return MusicLibrary.NAME_PREFIX + "showcompilationartists";
			case Services.EVENTSERVER_ENABLED:
				return Services.NAME_PREFIX + "esenabled";
			case Services.EVENTSERVER_ENABLED_ALL:
				return Services.NAME_PREFIX + "esallinterfaces";
			case Services.EVENTSERVER_PORT:
				return Services.NAME_PREFIX + "esport";
		}
		return null;
	}
	public static String getType(int name) {
		switch (name) {
			// boolean
			case MusicLibrary.LIBARY_ENABLED: 
			case MusicLibrary.SHOW_COMPLATION_ARTISTS:
			case Services.EVENTSERVER_ENABLED:
			case Services.EVENTSERVER_ENABLED_ALL:
				return "1";
				
			// String
			case Services.EVENTSERVER_PORT:
				return "3";
		}
		return null;
	}
	public static int getTypeInt(int name) {
		switch (name) {
		// boolean
		case MusicLibrary.LIBARY_ENABLED: 
		case MusicLibrary.SHOW_COMPLATION_ARTISTS:
		case Services.EVENTSERVER_ENABLED:
		case Services.EVENTSERVER_ENABLED_ALL:
			return 1;
			
		// String
		case Services.EVENTSERVER_PORT:
			return 3;
	}
	return -1;
	}
}