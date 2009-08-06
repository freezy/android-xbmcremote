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
import java.util.PriorityQueue;

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
	 * Get all albums in database
	 * @return list of album names
	 */
	public ArrayList<DatabaseItem> getAlbums() {
		return getMergedList("idAlbum", "SELECT idAlbum, strAlbum from album ORDER BY strAlbum");
	}
	
	/**
	 * Get songs from given database root
	 * @param artist
	 * @return list of song names
	 */
	public ArrayList<DatabaseItem> getSongs(DatabaseItem root) {
		return getMergedList("idSong", "SELECT idSong, strTitle from song WHERE " + root.formatSQL() + " ORDER BY iTrack");
	}
	
	/**
	 * Get all songs in database
	 * @return list of song names
	 */
	public ArrayList<DatabaseItem> getSongs() {
		return getMergedList("idSong", "SELECT idSong, strTitle from song ORDER BY iTrack");
	}
}
