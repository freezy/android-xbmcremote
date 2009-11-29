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

package org.xbmc.api.data;

import java.util.ArrayList;

import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.object.Song;

/**
 * This is the interface between the business layer and the presentation layer.
 * All the business layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IMusicClient {

	/**
	 * Adds an album to the current playlist.
	 * @param album Album
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(Album album);

	/**
	 * Adds all songs from an artist to the current playlist.
	 * @param artist Artist
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(Artist artist);

	/**
	 * Adds all songs from a genre to the current playlist.
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(Genre genre);

	/**
	 * Adds songs of a genre from an artist to the current playlist.
	 * @param artist Artist
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(Artist artist, Genre genre);
	
	/**
	 * Adds a song to the current playlist.
	 * @param song Song to add
	 * @return True on success, false otherwise.
	 */
	public boolean addToPlaylist(Song song);
	
	/**
	 * Returns how many items are in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistSize();
	
	/**
	 * Retrieves the currently playing song number in the playlist.
	 * @return Number of items in the playlist
	 */
	public int getPlaylistPosition();
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param position New position, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistPosition(int position);
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Position to remove, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public boolean removeFromPlaylist(int position);
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public boolean removeFromPlaylist(String path);
	
	/**
	 * Returns the first {@link PLAYLIST_LIMIT} songs of the playlist. 
	 * @return Songs in the playlist.
	 */
	public ArrayList<String> getPlaylist();
	
	/**
	 * Clears current playlist
	 * @return True on success, false otherwise.
	 */
	public boolean clearPlaylist();
	
	/**
	 * Adds a song to the current playlist and plays it.
	 * @param song Song
	 * @return True on success, false otherwise.
	 */
	public boolean play(Song song);
	
	/**
	 * Plays an album. Playlist is previously cleared.
	 * @param album Album to play
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(Album album, int sortBy, String sortOrder);
	
	/**
	 * Plays all songs of a genre. Playlist is previously cleared.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(Genre genre, int sortBy, String sortOrder);
	
	/**
	 * Plays all songs from an artist. Playlist is previously cleared.
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return True on success, false otherwise.
	 */
	public boolean play(Artist artist, int sortBy, String sortOrder);
	
	/**
	 * Plays songs of a genre from an artist. Playlist is previously cleared.
	 * @param artist Artist
	 * @param genre Genre
	 * @return True on success, false otherwise.
	 */
	public boolean play(Artist artist, Genre genre);

	/**
	 * Starts playing/showing the next media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return True on success, false otherwise.
	 */
	public boolean playNext();

	/**
	 * Starts playing/showing the previous media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return True on success, false otherwise.
	 */
	public boolean playPrev();
	
	/**
	 * Sets the media at playlist position position to be the next item to be 
	 * played. Position starts at 0, so SetPlaylistSong(5) sets the position
	 * to the 6th song in the playlist.
	 * @param pos Position
	 * @return true on success, false otherwise.
	 */
	public boolean playlistSetSong(int pos);
	
	/**
	 * Sets current playlist to "0"
	 * @return True on success, false otherwise.
	 */
	public boolean setCurrentPlaylist();
	
	/**
	 * Gets all albums with given artist IDs
	 * @param artistIDs Array of artist IDs
	 * @return All compilation albums
	 */
	public ArrayList<Album> getAlbums(ArrayList<Integer> artistIDs);
	
	/**
	 * Gets all albums from database
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All albums
	 */
	public ArrayList<Album> getAlbums(int sortBy, String sortOrder);

	/**
	 * Gets all albums of an artist from database
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Albums with an artist
	 */
	public ArrayList<Album> getAlbums(Artist artist, int sortBy, String sortOrder);

	/**
	 * Gets all albums of with at least one song in a genre
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return Albums of a genre
	 */
	public ArrayList<Album> getAlbums(Genre genre, int sortBy, String sortOrder);
	
	/**
	 * Gets all albums from database
	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
	 * @return All albums
	 */
	public ArrayList<Artist> getArtists(boolean albumArtistsOnly);

	/**
	 * Gets all artists with at least one song of a genre.
	 * @param genre Genre
	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
	 * @return Albums with a genre
	 */
	public ArrayList<Artist> getArtists(Genre genre, boolean albumArtistsOnly);
	
	/**
	 * Gets all genres from database
	 * @return All genres
	 */
	public ArrayList<Genre> getGenres();
	
	/**
	 * Updates the album object with additional data from the albuminfo table
	 * @param album
	 * @return Updated album
	 */
	public Album updateAlbumInfo(Album album);
	
	/**
	 * Returns a list containing all tracks of an album. The list is sorted by filename.
	 * @param album Album
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.	 
	 * @return All tracks of an album
	 */
	public ArrayList<Song> getSongs(Album album, int sortBy, String sortOrder);

	/**
	 * Returns a list containing all tracks of an artist. The list is sorted by album name, filename.
	 * @param artist Artist
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the artist
	 */
	public ArrayList<Song> getSongs(Artist artist, int sortBy, String sortOrder);
	
	/**
	 * Returns a list containing all tracks of a genre. The list is sorted by artist, album name, filename.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the genre
	 */
	public ArrayList<Song> getSongs(Genre genre, int sortBy, String sortOrder);
	
	/**
	 * Returns a list containing all tracks of a genre AND and artist. The list is sorted by 
	 * artist, album name, filename.
	 * @param genre Genre
	 * @param sortBy Sort field, see SortType.* 
	 * @param sortOrder Sort order, must be either SortType.ASC or SortType.DESC.
	 * @return All tracks of the genre
	 */
	public ArrayList<Song> getSongs(Artist artist, Genre genre, int sortBy, String sortOrder);
	
	/**
	 * Returns a list containing all artist IDs that stand for "compilation".
	 * Best case scenario would be only one ID for "Various Artists", though
	 * there are also just "V.A." or "VA" naming conventions.
	 * @return List of compilation artist IDs
	 */
	public ArrayList<Integer> getCompilationArtistIDs();

	/**
	 * Returns album thumbnail as base64-encoded string
	 * @param album
	 * @return Base64-encoded content of thumb
	 */
	public String getCover(ICoverArt art);
	
}