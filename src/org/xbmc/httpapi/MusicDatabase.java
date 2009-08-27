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

package org.xbmc.httpapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Song;

public class MusicDatabase extends Database {

	protected MusicDatabase(HttpApiConnection instance, PriorityQueue<Message> messenger) {
		super(instance, "QueryMusicDatabase");
	}
	
	/**
	 * Get artists which contains "like".
	 * Use this method sparsely as it adds more stress on the Database than getting all artists and let the program sort them, so only usefull if client is embedded. 
	 * @param like
	 * @return list of artist names
	 */
	public ArrayList<DatabaseItem> getArtists(String like) {
		 return getMergedList("idArtist", "SELECT idAlbum, strAlbum FROM artist WHERE strArtist LIKE %%" + like + "%%" + " ORDER BY strArtist");
	}

	/**
	 * Get all artists available in the database
	 * @return list of artist names
	 */
	public ArrayList<DatabaseItem> getArtists() {
		return getMergedList("idArtist", "SELECT idArtist, strArtist FROM artist ORDER BY strArtist");
	}
	
	/**
	 * Get all albums from given artist.
	 * @param artist
	 * @return list of album names
	 */
	public ArrayList<DatabaseItem> getAlbums(DatabaseItem root) {
		return getMergedList("idAlbum", "SELECT idAlbum, strAlbum from album WHERE " + root.formatSQL() + " ORDER BY strAlbum");
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
		sb.append("  ORDER BY i.strArtist DESC");
		sb.append("  LIMIT 300");
		return parseAlbums(instance.getString("QueryMusicDatabase", sb.toString()));
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
		return parseAlbumInfo(album, instance.getString("QueryMusicDatabase", sb.toString()));
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
		return parseSongs(instance.getString("QueryMusicDatabase", sb.toString()));
	}
	
	/**
	 * Returns album thumbnail as base64-encoded string
	 * @param album
	 * @return Base64-encoded content of thumb
	 */
	public String getAlbumThumb(Album album) {
		return instance.getString("FileDownload", album.getThumbUri());
	}

	/**
	 * Converts query response from HTTP API to a list of Album objects. Each
	 * row must return the following attributes in the following order:
	 * 	1. idAlbum
	 * 	2. strAlbum
	 * 	3. strArtist
	 * @param response
	 * @return List of albums
	 */
	private ArrayList<Album> parseAlbums(String response) {
		ArrayList<Album> albums = new ArrayList<Album>();
		String[] fields = response.split("<field>");
		try {
			for (int row = 1; row < fields.length; row += 3) {
				albums.add(new Album(
						trimInt(fields[row]), 
						trim(fields[row + 1]), 
						trim(fields[row + 2])
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
	 * 	1. strGenre
	 * 	2. strExtraGenres
	 * 	3. iYear
	 * 	4. strLabel
	 * 	5. iRating  
	 * @param album
	 * @param response
	 * @return Updated album
	 */
	private Album parseAlbumInfo(Album album, String response) {
		String[] fields = response.split("<field>");
		try {
			if (trim(fields[2]).length() > 0) {
				album.genres = trim(fields[1]) + trim(fields[2]);
			}	
			if (trim(fields[3]).length() > 0) {
				album.year = trimInt(fields[3]);
			}
			if (trim(fields[4]).length() > 0) {
				album.label = trim(fields[4]);
			}
			if (trim(fields[5]).length() > 0) {
				album.rating = trimInt(fields[5]);
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		return album;
	}
	
	/**
	 * Converts query response from HTTP API to a list of Song objects. Each
	 * row must return the following columns in the following order:
	 * 	1. strTitle
	 * 	2. StrArtist
	 * 	3. iTrack
	 * 	4. iDuration
	 * 	5. strPath
	 * 	6. strFileName
	 * @param response
	 * @return List of Songs
	 */
	private ArrayList<Song> parseSongs(String response) {
		ArrayList<Song> songs = new ArrayList<Song>();
		String[] fields = response.split("<field>");
		try { 
			for (int row = 1; row < fields.length; row += 6) { 
				songs.add(new Song( // String title, String artist, int track, int duration, String path
						trim(fields[row]), 
						trim(fields[row + 1]), 
						trimInt(fields[row + 2]), 
						trimInt(fields[row + 3]), 
						trim(fields[row + 4]),
						trim(fields[row + 5]) 
				));
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		Collections.sort(songs);
		return songs;		
	}

}
