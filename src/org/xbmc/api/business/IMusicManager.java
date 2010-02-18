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

package org.xbmc.api.business;

import java.util.ArrayList;

import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Song;

import android.content.Context;

/**
 * This is the interface between the presentation layer and the business layer.
 * All the controller of the presentation layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IMusicManager extends IManager {
	
	/**
	 * Gets all albums from database
	 * @param response Response object
	 */
	public void getCompilations(final DataResponse<ArrayList<Album>> response, final Context context);
	
	/**
	 * Gets all albums from database
	 * @param response Response object
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response, final Context context);
	
	/**
	 * SYNCHRONOUSLY gets all albums from database
	 * @return All albums in database
	 */
	public ArrayList<Album> getAlbums(final Context context);
	
	/**
	 * Gets all albums of an artist from database
	 * @param response Response object
	 * @param artist  Artist of the albums
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response, final Artist artist, final Context context);
	
	/**
	 * Gets all albums of a genre from database
	 * @param response Response object
	 * @param artist  Genre of the albums
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response, final Genre genre, final Context context);
	
	/**
	 * Gets all songs of an album from database
	 * @param response Response object
	 * @param album Album
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Album album, final Context context);
	
	/**
	 * Gets all songs from an artist from database
	 * @param response Response object
	 * @param album Artist
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Artist artist, final Context context);

	/**
	 * Gets all songs of a genre from database
	 * @param response Response object
	 * @param album Genre
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Genre genre, final Context context);

	/**
	 * Gets all artists from database
	 * @param response Response object
	 */
	public void getArtists(final DataResponse<ArrayList<Artist>> response, final Context context);
	
	/**
	 * Gets all artists with at least one song of a genre.
	 * @param response Response object
	 * @param genre Genre
	 */
	public void getArtists(final DataResponse<ArrayList<Artist>> response, final Genre genre, final Context context);
	
	/**
	 * Gets all artists from database
	 * @param response Response object
	 */
	public void getGenres(final DataResponse<ArrayList<Genre>> response, final Context context);

	/**
	 * Adds an album to the current playlist. If current playlist is stopped,
	 * the album is added to playlist and the first song is selected to play. 
	 * If something is playing already, the album is only queued.
	 * 
	 * @param response Response object
	 * @param album Album to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Album album, final Context context);
	
	/**
	 * Adds all songs of a genre to the current playlist. If current playlist is stopped,
	 * play is executed. Value is the first song of the added album.
	 * @param response Response object
	 * @param genre Genre of songs to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Genre genre, final Context context);
	
	/**
	 * Adds a song to the current playlist. Even if the playlist is empty, only this song will be added.
	 * @param response Response object
	 * @param album Song to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Song song, final Context context);

	/**
	 * Adds a song to the current playlist. If the playlist is empty, the whole
	 * album will be added with this song playing, otherwise only this song is
	 * added.
	 * 
	 * <b>Attention</b>, the response.value result is different as usual: True 
	 * means the whole album was added, false means ony the song.
	 *  
	 * @param response Response object
	 * @param album Album to add
	 * @param song Song to play
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Album album, final Song song, final Context context);
	
	/**
	 * Adds all songs from an artist to the playlist. If current playlist is
	 * stopped, the all songs of the artist are added to playlist and the first
	 * song is selected to play. If something is playing already, the songs are
	 * only queued.
	 * @param response Response object
	 * @param artist 
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Artist artist, final Context context);

	/**
	 * Adds all songs of a genre from an artist to the playlist. If nothing is playing, 
	 * the first song will be played, otherwise songs are just added to the playlist.
	 * @param response Response object
	 * @param artist 
	 * @param genre 
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Artist artist, final Genre genre, final Context context);
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param response Response object
	 * @param position Position, starting with 0.
	 */
	public void setPlaylistSong(final DataResponse<Boolean> response, final int position, final Context context);

	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param response Response object
	 * @param position Position to remove, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> response, final int position, final Context context);
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> response, final String path, final Context context);
	
	/**
	 * Plays an album
	 * @param response Response object
	 * @param album Album to play
	 */
	public void play(final DataResponse<Boolean> response, final Album album, final Context context);
	
	/**
	 * Plays all songs of a genre
	 * @param response Response object
	 * @param genre Genre of songs to play
	 */
	public void play(final DataResponse<Boolean> response, final Genre genre, final Context context);
	
	/**
	 * Plays a song
	 * @param response Response object
	 * @param song Song to play
	 */
	public void play(final DataResponse<Boolean> response, final Song song, final Context context);
	
	/**
	 * Plays a song, but the whole album is added to the playlist.
	 * @param response Response object
	 * @param album Album to queue
	 * @param song Song to play
	 */
	public void play(final DataResponse<Boolean> response, final Album album, final Song song, final Context context);
	
	/**
	 * Plays all songs from an artist
	 * @param response Response object
	 * @param artist Artist whose songs to play
	 */
	public void play(final DataResponse<Boolean> response, final Artist artist, final Context context);
	
	/**
	 * Plays songs of a genre from an artist
	 * @param response Response object
	 * @param artist Artist whose songs to play
	 * @param genre  Genre filter
	 */
	public void play(final DataResponse<Boolean> response, final Artist artist, final Genre genre, final Context context);
	
	/**
	 * Starts playing the next media in the current playlist. 
	 * @param response Response object
	 */
	public void playlistNext(final DataResponse<Boolean> response, final Context context);
	
	/**
	 * Returns an array of songs on the playlist. Empty array if nothing is playing.
	 * @param response Response object
	 */
	public void getPlaylist(final DataResponse<ArrayList<String>> response, final Context context);
	
	/**
	 * Returns the position of the currently playing song in the playlist. First position is 0.
	 * @param response Response object
	 */
	public void getPlaylistPosition(final DataResponse<Integer> response, final Context context);
	
	/**
	 * Updates the album object with additional data from the albuminfo table
	 * @param response Response object
	 * @param album Album to update
	 */
	public void updateAlbumInfo(final DataResponse<Album> response, final Album album, final Context context);
	
	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
	
}