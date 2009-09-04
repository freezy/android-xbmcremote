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
import java.util.Collections;

import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.data.Song;

/**
 * Takes care of every music related stuff, notably the music database.
 * 
 * @author Team XBMC
 */
public class MusicClient {
	
	private final Connection mConnection;

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public MusicClient(Connection connection) {
		mConnection = connection;
	}
	
	/**
	 * Get all albums from database
	 * @return All albums
	 */
	public ArrayList<Album> getAlbums() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT a.idAlbum, a.strAlbum, i.strArtist");
		sb.append("  FROM album AS a, artist AS i");
		sb.append("  WHERE a.idArtist = i.idArtist");
		sb.append("  ORDER BY i.strArtist ASC");
		sb.append("  LIMIT 300"); // let's keep it at 300 for now
		return parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Updates the album object with additional data from the albuminfo table
	 * @param album
	 * @return Updated album
	 */
	public Album updateAlbumInfo(Album album) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT g.strGenre, a.strExtraGenres, a.iYear, ai.strLabel, ai.iRating");
		sb.append("  FROM album a, genre g");
		sb.append("  LEFT JOIN albuminfo AS ai ON ai.idAlbumInfo = a.idAlbum");
		sb.append("  WHERE a.idGenre = g.idGenre");
		sb.append("  AND a.idAlbum = " + album.id);
		return parseAlbumInfo(album, mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Returns a list containing all tracks of an album. The list is sorted by filename.
	 * @param album
	 * @return Tracks of an album
	 */
	public ArrayList<Song> getSongs(Album album) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.strTitle, a.StrArtist, s.iTrack, s.iDuration, p.strPath, s.strFileName");
		sb.append("  FROM song AS s, path AS p, artist a");
		sb.append("  WHERE s.idPath = p.idPath");
		sb.append("  AND s.idArtist = a.idArtist");
		sb.append("  AND s.idAlbum = " + album.id);
		sb.append("  ORDER BY s.iTrack");
		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Returns album thumbnail as base64-encoded string
	 * @param album
	 * @return Base64-encoded content of thumb
	 */
	public String getAlbumThumb(ICoverArt art) {
		return mConnection.query("FileDownload", Album.getThumbUri(art));
	}

	/**
	 * Converts query response from HTTP API to a list of Album objects. Each
	 * row must return the following attributes in the following order:
	 * <ol>
	 * 	<li><code>idAlbum</code></li>
	 * 	<li><code>strAlbum</code></li>
	 * 	<li><code>strArtist</code></li>
	 * </ol>
	 * @param response
	 * @return List of albums
	 */
	private ArrayList<Album> parseAlbums(String response) {
		ArrayList<Album> albums = new ArrayList<Album>();
		String[] fields = response.split("<field>");
		try {
			for (int row = 1; row < fields.length; row += 3) {
				albums.add(new Album(
						Connection.trimInt(fields[row]), 
						Connection.trim(fields[row + 1]), 
						Connection.trim(fields[row + 2])
				));
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		return albums;
	}
	
	/**
	 * Updates an album with info from HTTP API query response. One row is 
	 * expected, with the following columns:
	 * <ol>
	 * 	<li><code>strGenre</code></li>
	 * 	<li><code>strExtraGenres</code></li>
	 * 	<li><code>iYear</code></li>
	 * 	<li><code>strLabel</code></li>
	 * 	<li><code>iRating</code></li>
	 * </ol>  
	 * @param album
	 * @param response
	 * @return Updated album
	 */
	private Album parseAlbumInfo(Album album, String response) {
		String[] fields = response.split("<field>");
		try {
			if (Connection.trim(fields[2]).length() > 0) {
				album.genres = Connection.trim(fields[1]) + Connection.trim(fields[2]);
			}	
			if (Connection.trim(fields[3]).length() > 0) {
				album.year = Connection.trimInt(fields[3]);
			}
			if (Connection.trim(fields[4]).length() > 0) {
				album.label = Connection.trim(fields[4]);
			}
			if (Connection.trim(fields[5]).length() > 0) {
				album.rating = Connection.trimInt(fields[5]);
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		return album;
	}
	
	/**
	 * Converts query response from HTTP API to a list of Song objects. Each
	 * row must return the following columns in the following order:
	 * <ol>
	 * 	<li><code>strTitle</code></li>
	 * 	<li><code>strArtist</code></li>
	 * 	<li><code>iTrack</code></li>
	 * 	<li><code>iDuration</code></li>
	 * 	<li><code>strPath</code></li>
	 * 	<li><code>strFileName</code></li>
	 * </ol>
	 * @param response
	 * @return List of Songs
	 */
	private ArrayList<Song> parseSongs(String response) {
		ArrayList<Song> songs = new ArrayList<Song>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 6) { 
				songs.add(new Song( // String title, String artist, int track, int duration, String path
						Connection.trim(fields[row]), 
						Connection.trim(fields[row + 1]), 
						Connection.trimInt(fields[row + 2]), 
						Connection.trimInt(fields[row + 3]), 
						Connection.trim(fields[row + 4]),
						Connection.trim(fields[row + 5]) 
				));
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		Collections.sort(songs);
		return songs;		
	}
}
