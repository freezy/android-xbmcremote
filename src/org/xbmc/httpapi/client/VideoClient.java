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

import java.util.ArrayList;
import java.util.HashMap;

import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.client.ControlClient.PlayStatus;
import org.xbmc.httpapi.data.Movie;
import org.xbmc.httpapi.type.MediaType;
import org.xbmc.httpapi.type.SortType;

/**
 * Takes care of everything related to the video database.
 * 
 * @author Team XBMC
 */
public class VideoClient {
	
	private final Connection mConnection;

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public VideoClient(Connection connection) {
		mConnection = connection;
	}
	
	/**
	 * Gets all albums from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All albums
	 */
	public ArrayList<Movie> getMovies(int sortBy, String sortOrder) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT idMovie, c00, c07, strPath, c06");
		sb.append(" FROM movieview WHERE movieview.idmovie NOT IN (SELECT idmovie FROM setlinkmovie)");
		sb.append(moviesOrderBy(sortBy, sortOrder));
		return parseMovies(mConnection.query("QueryVideoDatabase", sb.toString()));
	}

	/**
	 * Converts query response from HTTP API to a list of Album objects. Each
	 * row must return the following attributes in the following order:
	 * <ol>
	 * 	<li><code>idMovie</code></li>
	 * 	<li><code>c00</code></li>
	 * 	<li><code>c07</code></li>
	 * 	<li><code>strPath</code></li>
	 * 	<li><code>c06</code></li>
	 * </ol> 
	 * @param response
	 * @return List of movies
	 */
	private ArrayList<Movie> parseMovies(String response) {
		ArrayList<Movie> movies = new ArrayList<Movie>();
		String[] fields = response.split("<field>");
		try {
			for (int row = 1; row < fields.length; row += 5) {
				movies.add(new Movie( // int id, String title, int year, String path, String director
						Connection.trimInt(fields[row]), 
						Connection.trim(fields[row + 1]), 
						Connection.trimInt(fields[row + 2]),
						Connection.trim(fields[row + 3]),
						Connection.trim(fields[row + 4])
				));
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println("response = " + response);
			e.printStackTrace();
		}
		return movies;
	}
	
	
	/**
	 * Returns an SQL String of given sort options of movies query
	 * @param sortBy    Sort field
	 * @param sortOrder Sort order
	 * @return SQL "ORDER BY" string
	 */
	private String moviesOrderBy(int sortBy, String sortOrder) {
		switch (sortBy) {
			default:
			case SortType.TITLE:
				return " ORDER BY lower(c00) " + sortOrder;
			case SortType.YEAR:
				return " ORDER BY c07 " + sortOrder + ", lower(c00) " + sortOrder;
			case SortType.RATING:
				return " ORDER BY c05" + sortOrder;
		}
	}
	
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
			public int getPlaylistPosition() {
				return Integer.parseInt(map.get("VideoNo"));
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