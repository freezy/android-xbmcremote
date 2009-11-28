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

import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.info.GuiSettings;
import org.xbmc.httpapi.type.SortType;

import android.content.SharedPreferences;


/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * @author Team XBMC
 */
public class MusicManager extends AbstractManager {
	
	private static SharedPreferences sPref;
	private static int sCurrentSortKey;
	
	/**
	 * Gets all albums from database
	 * @param handler Callback handler
	 */
	public void getCompilations(final DataResponse<ArrayList<Album>> handler) {
		mHandler.post(new Runnable() {
			public void run() {
				final MusicClient mc = music(handler);
				ArrayList<Integer> compilationArtistIDs = mc.getCompilationArtistIDs();
				handler.value = mc.getAlbums(compilationArtistIDs);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all albums from database
	 * @param handler Callback handler
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getAlbums(getSortBy(SortType.ALBUM), getSortOrder());
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all albums of an artist from database
	 * @param handler Callback handler
	 * @param artist  Artist of the albums
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> handler, final Artist artist) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getAlbums(artist, getSortBy(SortType.ALBUM), getSortOrder());
				done(handler);
			}
		});
	}

	/**
	 * Gets all albums of a genre from database
	 * @param handler Callback handler
	 * @param artist  Genre of the albums
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getAlbums(genre, getSortBy(SortType.ALBUM), getSortOrder());
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all songs of an album from database
	 * @param handler Callback handler
	 * @param album The album
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getSongs(album, getSortBy(SortType.ARTIST), getSortOrder());
				done(handler);
			}
		});
	}

	/**
	 * Gets all songs from an artist from database
	 * @param handler Callback handler
	 * @param album The artist
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> handler, final Artist artist) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getSongs(artist, getSortBy(SortType.ARTIST), getSortOrder());
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all songs of a genre from database
	 * @param handler Callback handler
	 * @param album The genre
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getSongs(genre, getSortBy(SortType.ARTIST), getSortOrder());
				done(handler);
			}
		});
	}

	/**
	 * Gets all artists from database
	 * @param handler Callback handler
	 */
	public void getArtists(final DataResponse<ArrayList<Artist>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				final boolean albumArtistsOnly = info(handler).getGuiSettingBool(GuiSettings.MusicLibrary.ALBUM_ARTISTS_ONLY);
				handler.value = music(handler).getArtists(albumArtistsOnly);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all artists with at least one song of a genre.
	 * @param handler Callback handler
	 */
	public void getArtists(final DataResponse<ArrayList<Artist>> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				final boolean albumArtistsOnly = info(handler).getGuiSettingBool(GuiSettings.MusicLibrary.ALBUM_ARTISTS_ONLY);
				handler.value = music(handler).getArtists(genre, albumArtistsOnly);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all artists from database
	 * @param handler Callback handler
	 */
	public void getGenres(final DataResponse<ArrayList<Genre>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getGenres();
				done(handler);
			}
		});
	}
	
	/**
	 * Adds an album to the current playlist. If current playlist is stopped,
	 * the album is added to playlist and the first song is selected to play. 
	 * If something is playing already, the album is only queued.
	 * 
	 * @param handler Callback
	 * @param album Album to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				final int numAlreadyQueued = mc.getPlaylistSize();
				handler.value = mc.addToPlaylist(album);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(handler);
			}
		});
	}
	
	/**
	 * Adds all songs of a genre to the current playlist. If current playlist is stopped,
	 * play is executed. Value is the first song of the added album.
	 * @param handler Callback
	 * @param genre Genre of songs to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				final int numAlreadyQueued = mc.getPlaylistSize();
				handler.value = mc.addToPlaylist(genre);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(handler);
			}
		});
	}
	
	/**
	 * Adds a song to the current playlist. Even if the playlist is empty, only this song will be added.
	 * @param handler Callback
	 * @param album Song to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> handler, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).addToPlaylist(song);
				done(handler);
			}
		});
	}

	/**
	 * Adds a song to the current playlist. If the playlist is empty, the whole
	 * album will be added with this song playing, otherwise only this song is
	 * added.
	 * 
	 * <b>Attention</b>, the handler.value result is different as usual: True 
	 * means the whole album was added, false means ony the song.
	 *  
	 * @param handler Callback
	 * @param album Album to add
	 * @param song Song to play
	 */
	public void addToPlaylist(final DataResponse<Boolean> handler, final Album album, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient.PlayStatus ps = control(handler).getPlayState();
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
					handler.value = true;
				} else {                          // otherwise, only add the song
					mc.addToPlaylist(song);
					handler.value = false;
				}
				if (ps == ControlClient.PlayStatus.Stopped) { // if nothing is playing, play the song
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
				done(handler);
			}
		});
	}

	/**
	 * Adds all songs from an artist to the playlist. If current playlist is
	 * stopped, the all songs of the artist are added to playlist and the first
	 * song is selected to play. If something is playing already, the songs are
	 * only queued.
	 * @param handler Callback
	 * @param artist 
	 */
	public void addToPlaylist(final DataResponse<Boolean> handler, final Artist artist) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				final int numAlreadyQueued = mc.getPlaylistSize();
				handler.value = mc.addToPlaylist(artist);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(handler);
			}
		});
	}

	/**
	 * Adds all songs of a genre from an artist to the playlist. If nothing is playing, 
	 * the first song will be played, otherwise songs are just added to the playlist.
	 * @param handler Callback
	 * @param artist 
	 * @param genre 
	 */
	public void addToPlaylist(final DataResponse<Boolean> handler, final Artist artist, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				final int numAlreadyQueued = mc.getPlaylistSize();
				handler.value = mc.addToPlaylist(artist, genre);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(handler);
			}
		});
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param handler Callback
	 * @param position Position, starting with 0.
	 */
	public void setPlaylistSong(final DataResponse<Boolean> handler, final int position) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).setPlaylistPosition(position);
				done(handler);
			}
		});
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Position to remove, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> handler, final int position) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).removeFromPlaylist(position);
				done(handler);
			}
		});
	}

	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> handler, final String path) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).removeFromPlaylist(path);
				done(handler);
			}
		});
	}
	
	/**
	 * Plays an album
	 * @param handler Callback
	 * @param album Album to play
	 */
	public void play(final DataResponse<Boolean> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				control(handler).stop();
				handler.value = music(handler).play(album, getSortBy(SortType.TRACK), getSortOrder());
				done(handler);
			}
		});
	}
	
	/**
	 * Plays all songs of a genre
	 * @param handler Callback
	 * @param genre Genre of songs to play
	 */
	public void play(final DataResponse<Boolean> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				control(handler).stop();
				handler.value = music(handler).play(genre, getSortBy(SortType.ARTIST), getSortOrder());
				done(handler);
			}
		});
	}
	
	/**
	 * Plays a song
	 * @param handler Callback
	 * @param song Song to play
	 */
	public void play(final DataResponse<Boolean> handler, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				control(handler).stop();
				handler.value = music(handler).play(song);
				done(handler);
			}
		});
	}
	
	/**
	 * Plays a song, but the whole album is added to the playlist.
	 * @param handler Callback
	 * @param album Album to queue
	 * @param song Song to play
	 */
	public void play(final DataResponse<Boolean> handler, final Album album, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
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
				handler.value = mc.playNext();
				done(handler);
			}
		});
	}

	/**
	 * Plays all songs from an artist
	 * @param handler Callback
	 * @param artist Artist whose songs to play
	 */
	public void play(final DataResponse<Boolean> handler, final Artist artist) {
		mHandler.post(new Runnable() {
			public void run() { 
				control(handler).stop();
				handler.value = music(handler).play(artist, getSortBy(SortType.ALBUM), getSortOrder());
				done(handler);
			}
		});
	}
	
	/**
	 * Plays songs of a genre from an artist
	 * @param handler Callback
	 * @param artist Artist whose songs to play
	 * @param genre  Genre filter
	 */
	public void play(final DataResponse<Boolean> handler, final Artist artist, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				control(handler).stop();
				handler.value = music(handler).play(artist, genre);
				done(handler);
			}
		});
	}
	
	
	/**
	 * Starts playing the next media in the current playlist. 
	 * @param handler Callback
	 */
	public void playlistNext(final DataResponse<Boolean> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).playNext();
				done(handler);
			}
		});
	}
	
	/**
	 * Returns an array of songs on the playlist. Empty array if nothing is playing.
	 * @param handler Callback
	 */
	public void getPlaylist(final DataResponse<ArrayList<String>> handler) {
		mHandler.post(new Runnable() {
			public void run() {
				handler.value = music(handler).getPlaylist();
				final String firstEntry = handler.value.get(0);
				if (firstEntry != null && firstEntry.equals("[Empty]")) {
					handler.value = new ArrayList<String>();
				} 
				done(handler);
			}
		});
	}
	
	/**
	 * Returns the position of the currently playing song in the playlist. First position is 0.
	 * @param handler Callback
	 */
	public void getPlaylistPosition(final DataResponse<Integer> handler) {
		mHandler.post(new Runnable() {
			public void run() {
				handler.value = music(handler).getPlaylistPosition();
				done(handler);
			}
		});
	}
	
	/**
	 * Sets the static reference to the preferences object. Used to obtain
	 * current sort values.
	 * @param pref
	 */
	public static void setPreferences(SharedPreferences pref) {
		sPref = pref;
	}
	
	/**
	 * Sets which kind of view is currently active.
	 * @param sortKey
	 */
	public static void setSortKey(int sortKey) {
		sCurrentSortKey = sortKey;
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
		final ControlClient.PlayStatus ps = cc.getPlayState();
		if (ps == ControlClient.PlayStatus.Stopped) { // if nothing is playing, play the song
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
	private static int getSortBy(int type) {
		if (sPref != null) {
			return sPref.getInt(AbstractManager.PREF_SORT_BY_PREFIX + sCurrentSortKey, type);
		}
		return type;
	}
	
	/**
	 * Returns currently saved "sort by" value. If the preference was not set yet, or
	 * if the current sort key is not set, return "ASC".
	 * @return Sort order
	 */
	private static String getSortOrder() {
		if (sPref != null) {
			return sPref.getString(AbstractManager.PREF_SORT_ORDER_PREFIX + sCurrentSortKey, SortType.ORDER_ASC);
		}
		return SortType.ORDER_ASC;
	}

}