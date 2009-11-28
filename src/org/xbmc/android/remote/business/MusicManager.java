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

package org.xbmc.android.remote.business;

import java.util.ArrayList;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IMusicManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.data.IControlClient.PlayStatus;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Song;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.info.GuiSettings;
import org.xbmc.httpapi.type.SortType;

import android.content.SharedPreferences;

/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * @author Team XBMC
 */
public class MusicManager extends AbstractManager implements IMusicManager, ISortableManager {
	
	private SharedPreferences mPref;
	private int mCurrentSortKey;
	
	/**
	 * Gets all albums from database
	 * @param response Response object
	 */
	public void getCompilations(final DataResponse<ArrayList<Album>> response) {
		mResponse.post(new Runnable() {
			public void run() {
				final MusicClient mc = music(response);
				ArrayList<Integer> compilationArtistIDs = mc.getCompilationArtistIDs();
				response.value = mc.getAlbums(compilationArtistIDs);
				done(response);
			}
		});
	}
	
	/**
	 * Gets all albums from database
	 * @param response Response object
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).getAlbums(getSortBy(SortType.ALBUM), getSortOrder());
				done(response);
			}
		});
	}
	
	/**
	 * Gets all albums of an artist from database
	 * @param response Response object
	 * @param artist  Artist of the albums
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response, final Artist artist) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).getAlbums(artist, getSortBy(SortType.ALBUM), getSortOrder());
				done(response);
			}
		});
	}

	/**
	 * Gets all albums of a genre from database
	 * @param response Response object
	 * @param artist  Genre of the albums
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response, final Genre genre) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).getAlbums(genre, getSortBy(SortType.ALBUM), getSortOrder());
				done(response);
			}
		});
	}
	
	/**
	 * Gets all songs of an album from database
	 * @param response Response object
	 * @param album Album
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Album album) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).getSongs(album, getSortBy(SortType.ARTIST), getSortOrder());
				done(response);
			}
		});
	}

	/**
	 * Gets all songs from an artist from database
	 * @param response Response object
	 * @param album Artist
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Artist artist) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).getSongs(artist, getSortBy(SortType.ARTIST), getSortOrder());
				done(response);
			}
		});
	}
	
	/**
	 * Gets all songs of a genre from database
	 * @param response Response object
	 * @param album Genre
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Genre genre) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).getSongs(genre, getSortBy(SortType.ARTIST), getSortOrder());
				done(response);
			}
		});
	}

	/**
	 * Gets all artists from database
	 * @param response Response object
	 */
	public void getArtists(final DataResponse<ArrayList<Artist>> response) {
		mResponse.post(new Runnable() {
			public void run() { 
				final boolean albumArtistsOnly = info(response).getGuiSettingBool(GuiSettings.MusicLibrary.ALBUM_ARTISTS_ONLY);
				response.value = music(response).getArtists(albumArtistsOnly);
				done(response);
			}
		});
	}
	
	/**
	 * Gets all artists with at least one song of a genre.
	 * @param response Response object
	 * @param genre Genre
	 */
	public void getArtists(final DataResponse<ArrayList<Artist>> response, final Genre genre) {
		mResponse.post(new Runnable() {
			public void run() { 
				final boolean albumArtistsOnly = info(response).getGuiSettingBool(GuiSettings.MusicLibrary.ALBUM_ARTISTS_ONLY);
				response.value = music(response).getArtists(genre, albumArtistsOnly);
				done(response);
			}
		});
	}
	
	/**
	 * Gets all artists from database
	 * @param response Response object
	 */
	public void getGenres(final DataResponse<ArrayList<Genre>> response) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).getGenres();
				done(response);
			}
		});
	}
	
	/**
	 * Adds an album to the current playlist. If current playlist is stopped,
	 * the album is added to playlist and the first song is selected to play. 
	 * If something is playing already, the album is only queued.
	 * 
	 * @param response Response object
	 * @param album Album to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Album album) {
		mResponse.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(response);
				final ControlClient cc = control(response);
				final int numAlreadyQueued = mc.getPlaylistSize();
				response.value = mc.addToPlaylist(album);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(response);
			}
		});
	}
	
	/**
	 * Adds all songs of a genre to the current playlist. If current playlist is stopped,
	 * play is executed. Value is the first song of the added album.
	 * @param response Response object
	 * @param genre Genre of songs to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Genre genre) {
		mResponse.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(response);
				final ControlClient cc = control(response);
				final int numAlreadyQueued = mc.getPlaylistSize();
				response.value = mc.addToPlaylist(genre);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(response);
			}
		});
	}
	
	/**
	 * Adds a song to the current playlist. Even if the playlist is empty, only this song will be added.
	 * @param response Response object
	 * @param album Song to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Song song) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).addToPlaylist(song);
				done(response);
			}
		});
	}

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
	public void addToPlaylist(final DataResponse<Boolean> response, final Album album, final Song song) {
		mResponse.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(response);
				final PlayStatus ps = control(response).getPlayState();
				mc.setCurrentPlaylist();
				final int playlistSize = mc.getPlaylistSize(); 
				int playPos = -1;
				if (playlistSize == 0) {  // if playlist is empty, add the whole album
					int n = 0;
					for (Song albumSong : mc.getSongs(album, SortType.DONT_SORT, null)) {
						if (albumSong.id == song.id) {
							playPos = n;
							break;
						}
						n++;
					}
					mc.addToPlaylist(album);
					response.value = true;
				} else {                          // otherwise, only add the song
					mc.addToPlaylist(song);
					response.value = false;
				}
				if (ps == PlayStatus.Stopped) { // if nothing is playing, play the song
					if (playPos == 0) {
						mc.playlistSetSong(playPos + 1);
						mc.playPrev();
					} else if (playPos > 0) {
						mc.playlistSetSong(playPos - 1);
						mc.playNext();
					} else {
						mc.playlistSetSong(playlistSize - 1);
						mc.playNext();
					}
				}
				done(response);
			}
		});
	}

	/**
	 * Adds all songs from an artist to the playlist. If current playlist is
	 * stopped, the all songs of the artist are added to playlist and the first
	 * song is selected to play. If something is playing already, the songs are
	 * only queued.
	 * @param response Response object
	 * @param artist Artist
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Artist artist) {
		mResponse.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(response);
				final ControlClient cc = control(response);
				final int numAlreadyQueued = mc.getPlaylistSize();
				response.value = mc.addToPlaylist(artist);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(response);
			}
		});
	}

	/**
	 * Adds all songs of a genre from an artist to the playlist. If nothing is playing, 
	 * the first song will be played, otherwise songs are just added to the playlist.
	 * @param response Response object
	 * @param artist Artist
	 * @param genre Genre
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Artist artist, final Genre genre) {
		mResponse.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(response);
				final ControlClient cc = control(response);
				final int numAlreadyQueued = mc.getPlaylistSize();
				response.value = mc.addToPlaylist(artist, genre);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(response);
			}
		});
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param response Response object
	 * @param position Position, starting with 0.
	 */
	public void setPlaylistSong(final DataResponse<Boolean> response, final int position) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).setPlaylistPosition(position);
				done(response);
			}
		});
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Position to remove, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> response, final int position) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).removeFromPlaylist(position);
				done(response);
			}
		});
	}

	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> response, final String path) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).removeFromPlaylist(path);
				done(response);
			}
		});
	}
	
	/**
	 * Plays an album
	 * @param response Response object
	 * @param album Album to play
	 */
	public void play(final DataResponse<Boolean> response, final Album album) {
		mResponse.post(new Runnable() {
			public void run() { 
				control(response).stop();
				response.value = music(response).play(album, getSortBy(SortType.TRACK), getSortOrder());
				done(response);
			}
		});
	}
	
	/**
	 * Plays all songs of a genre
	 * @param response Response object
	 * @param genre Genre of songs to play
	 */
	public void play(final DataResponse<Boolean> response, final Genre genre) {
		mResponse.post(new Runnable() {
			public void run() { 
				control(response).stop();
				response.value = music(response).play(genre, getSortBy(SortType.ARTIST), getSortOrder());
				done(response);
			}
		});
	}
	
	/**
	 * Plays a song
	 * @param response Response object
	 * @param song Song to play
	 */
	public void play(final DataResponse<Boolean> response, final Song song) {
		mResponse.post(new Runnable() {
			public void run() { 
				control(response).stop();
				response.value = music(response).play(song);
				done(response);
			}
		});
	}
	
	/**
	 * Plays a song, but the whole album is added to the playlist.
	 * @param response Response object
	 * @param album Album to queue
	 * @param song Song to play
	 */
	public void play(final DataResponse<Boolean> response, final Album album, final Song song) {
		mResponse.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(response);
				final ControlClient cc = control(response);
				int n = 0;
				int playPos = 0;
				mc.clearPlaylist();
				for (Song albumSong : mc.getSongs(album, SortType.DONT_SORT, null)) {
					if (albumSong.id == song.id) {
						playPos = n;
						break;
					}
					n++;
				}
				cc.stop();
				mc.addToPlaylist(album);
				mc.setCurrentPlaylist();
				if (playPos > 0) {
					mc.playlistSetSong(playPos - 1);
				}				
				response.value = mc.playNext();
				done(response);
			}
		});
	}

	/**
	 * Plays all songs from an artist
	 * @param response Response object
	 * @param artist Artist whose songs to play
	 */
	public void play(final DataResponse<Boolean> response, final Artist artist) {
		mResponse.post(new Runnable() {
			public void run() { 
				control(response).stop();
				response.value = music(response).play(artist, getSortBy(SortType.ALBUM), getSortOrder());
				done(response);
			}
		});
	}
	
	/**
	 * Plays songs of a genre from an artist
	 * @param response Response object
	 * @param artist Artist whose songs to play
	 * @param genre  Genre filter
	 */
	public void play(final DataResponse<Boolean> response, final Artist artist, final Genre genre) {
		mResponse.post(new Runnable() {
			public void run() { 
				control(response).stop();
				response.value = music(response).play(artist, genre);
				done(response);
			}
		});
	}
	
	
	/**
	 * Starts playing the next media in the current playlist. 
	 * @param response Response object
	 */
	public void playlistNext(final DataResponse<Boolean> response) {
		mResponse.post(new Runnable() {
			public void run() { 
				response.value = music(response).playNext();
				done(response);
			}
		});
	}
	
	/**
	 * Returns an array of songs on the playlist. Empty array if nothing is playing.
	 * @param response Response object
	 */
	public void getPlaylist(final DataResponse<ArrayList<String>> response) {
		mResponse.post(new Runnable() {
			public void run() {
				response.value = music(response).getPlaylist();
				final String firstEntry = response.value.get(0);
				if (firstEntry != null && firstEntry.equals("[Empty]")) {
					response.value = new ArrayList<String>();
				} 
				done(response);
			}
		});
	}
	
	/**
	 * Returns the position of the currently playing song in the playlist. First position is 0.
	 * @param response Response object
	 */
	public void getPlaylistPosition(final DataResponse<Integer> response) {
		mResponse.post(new Runnable() {
			public void run() {
				response.value = music(response).getPlaylistPosition();
				done(response);
			}
		});
	}
	
	/**
	 * Sets the static reference to the preferences object. Used to obtain
	 * current sort values.
	 * @param pref
	 */
	public void setPreferences(SharedPreferences pref) {
		mPref = pref;
	}
	
	/**
	 * Sets which kind of view is currently active.
	 * @param sortKey
	 */
	public void setSortKey(int sortKey) {
		mCurrentSortKey = sortKey;
	}
	
	/**
	 * Checks if something's playing. If that's not the case, set the 
	 * playlist's play position either to the start if there were no items
	 * before, or to the first position of the newly added files.
	 * @param mc Music client
	 * @param cc Control client
	 * @param numAlreadyQueued Number of previously queued items
	 */
	private void checkForPlayAfterQueue(final MusicClient mc, final ControlClient cc, int numAlreadyQueued) {
		final PlayStatus ps = cc.getPlayState();
		if (ps == PlayStatus.Stopped) { // if nothing is playing, play the song
			mc.setCurrentPlaylist();
			if (numAlreadyQueued == 0) {
				mc.playNext();
			} else {
				mc.playlistSetSong(numAlreadyQueued);
			}
		}
	}
	
	/**
	 * Returns currently saved "sort by" value. If the preference was not set yet, or
	 * if the current sort key is not set, return default value.
	 * @param type Default value
	 * @return Sort by field
	 */
	private int getSortBy(int type) {
		if (mPref != null) {
			return mPref.getInt(AbstractManager.PREF_SORT_BY_PREFIX + mCurrentSortKey, type);
		}
		return type;
	}
	
	/**
	 * Returns currently saved "sort by" value. If the preference was not set yet, or
	 * if the current sort key is not set, return "ASC".
	 * @return Sort order
	 */
	private String getSortOrder() {
		if (mPref != null) {
			return mPref.getString(AbstractManager.PREF_SORT_ORDER_PREFIX + mCurrentSortKey, SortType.ORDER_ASC);
		}
		return SortType.ORDER_ASC;
	}

}