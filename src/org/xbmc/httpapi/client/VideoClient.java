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

package org.xbmc.httpapi.client;

import java.util.HashMap;

import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.client.ControlClient.PlayStatus;
import org.xbmc.httpapi.type.MediaType;

/**
 * Takes care of everything related to the video database.
 * 
 * @author Team XBMC
 */
public class VideoClient {
	
//	private final Connection mConnection;

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public VideoClient(Connection connection) {
//		mConnection = connection;
	}
	
	/**
	 * Get all TVShows in database
	 * @return list of TV Show names
	 */
//	public ArrayList<DatabaseItem> getTVShows() {
//		return null;// getMergedList("idShow", "SELECT idShow, c00 from tvshow ORDER BY c00");
//	}
	
	/**
	 * Get all Movies in database
	 * @return list of Movie names
	 */
//	public ArrayList<DatabaseItem> getMovies() {
//		return null;//getMergedList("idMovie", "SELECT idMovie, c00 from movie ORDER BY c00");
//	}
	
	public static ControlClient.ICurrentlyPlaying getCurrentlyPlaying(final HashMap<String, String> map) {
		return new ControlClient.ICurrentlyPlaying() {
			private static final long serialVersionUID = 5036994329211476713L;
			public String getTitle() {
				return map.get("Tagline");
			}
			public int getTime() {
				return parseTime(map.get("Time"));
			}
			public PlayStatus getPlayStatus() {
				return PlayStatus.parse(map.get("PlayStatus"));
			}
			public float getPercentage() {
				return Float.valueOf(map.get("Percentage"));
			}
			public String getFilename() {
				return map.get("Filename");
			}
			public int getDuration() {
				return parseTime(map.get("Duration"));
			}
			public String getArtist() {
				return map.get("Genre");
			}
			public String getAlbum() {
				return map.get("Title");
			}
			public MediaType getType() {
				return MediaType.video;
			}
			public boolean isPlaying() {
				return PlayStatus.parse(map.get("PlayStatus")).equals(PlayStatus.Playing);
			}
			private int parseTime(String time) {
				String[] s = time.split(":");
				if (s.length == 2) {
					return Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
				} else if (s.length == 3) {
					return Integer.parseInt(s[0]) * 3600 + Integer.parseInt(s[1]) * 60 + Integer.parseInt(s[2]);
				} else {
					return 0;
				}
			}
		};
	}
}