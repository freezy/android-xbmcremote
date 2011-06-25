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
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IMusicClient;
import org.xbmc.api.info.GuiSettings;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Album;
import org.xbmc.api.object.Artist;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Song;
import org.xbmc.api.type.SortType;
import org.xbmc.httpapi.WifiStateException;
import org.xbmc.jsonrpc.client.MusicClient;

import android.content.Context;

/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * @author Team XBMC
 */
public class MusicManager extends AbstractManager implements IMusicManager, ISortableManager, INotifiableManager {
	
	/**
	 * Gets all albums from database
	 * @param response Response object
	 */
	public void getCompilations(final DataResponse<ArrayList<Album>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Album>>(response, this){
			@Override
			public void doRun() throws Exception {
				final IMusicClient mc = music(context);
				ArrayList<Integer> compilationArtistIDs = mc.getCompilationArtistIDs(MusicManager.this);
				response.value = mc.getAlbums(MusicManager.this, compilationArtistIDs);
			}
		});
	}
	
	/**
	 * Gets all albums from database
	 * @param response Response object
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response, final Context context) {
		mHandler.post(new Command<ArrayList<Album>>(response, this) {
			@Override
			public void doRun() throws Exception {
				response.value = music(context).getAlbums(MusicManager.this, getSortBy(SortType.ALBUM), getSortOrder());
			}
		});
	}
	
	/**
	 * SYNCHRONOUSLY gets all albums from database
	 * @return All albums in database
	 */
	public ArrayList<Album> getAlbums(final Context context) {
		try { //TODO fix this to throw 
			return music(context).getAlbums(MusicManager.this, getSortBy(SortType.ALBUM), getSortOrder());
		} catch (WifiStateException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets all albums of an artist from database
	 * @param response Response object
	 * @param artist  Artist of the albums
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response, final Artist artist, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				response.value = music(context).getAlbums(MusicManager.this, artist, getSortBy(SortType.ALBUM), getSortOrder());
//				onFinish(response);
//			}
//		});
		mHandler.post(new Command<ArrayList<Album>>(response, this){
			@Override
			public void doRun() throws Exception {
				response.value = music(context).getAlbums(MusicManager.this, artist, getSortBy(SortType.ALBUM), getSortOrder());
			}
		});
	}

	/**
	 * Gets all albums of a genre from database
	 * @param response Response object
	 * @param artist  Genre of the albums
	 */
	public void getAlbums(final DataResponse<ArrayList<Album>> response, final Genre genre, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				response.value = music(context).getAlbums(MusicManager.this, genre, getSortBy(SortType.ALBUM), getSortOrder());
//				onFinish(response);
//			}
//		});
		mHandler.post(new Command<ArrayList<Album>>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).getAlbums(MusicManager.this, genre, getSortBy(SortType.ALBUM), getSortOrder());
			}
		});
	}
	
	/**
	 * Gets all songs of an album from database
	 * @param response Response object
	 * @param album Album
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Album album, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				response.value = music(context).getSongs(MusicManager.this, album, getSortBy(SortType.ARTIST), getSortOrder());
//				onFinish(response);
//			}
//		});
		mHandler.post(new Command<ArrayList<Song>>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).getSongs(MusicManager.this, album, getSortBy(SortType.TRACK), getSortOrder());
			}
		});
	}

	/**
	 * Gets all songs from an artist from database
	 * @param response Response object
	 * @param album Artist
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Artist artist, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				response.value = music(context).getSongs(MusicManager.this, artist, getSortBy(SortType.ARTIST), getSortOrder());
//				onFinish(response);
//			}
//		});
		mHandler.post(new Command<ArrayList<Song>>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).getSongs(MusicManager.this, artist, getSortBy(SortType.ARTIST), getSortOrder());
			}
		});
	}
	
	/**
	 * Gets all songs of a genre from database
	 * @param response Response object
	 * @param album Genre
	 */
	public void getSongs(final DataResponse<ArrayList<Song>> response, final Genre genre, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				response.value = music(context).getSongs(MusicManager.this, genre, getSortBy(SortType.ARTIST), getSortOrder());
//				onFinish(response);
//			}
//		});
		mHandler.post(new Command<ArrayList<Song>>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).getSongs(MusicManager.this, genre, getSortBy(SortType.ARTIST), getSortOrder());
			}
		});
	}

	/**
	 * Gets all artists from database
	 * @param response Response object
	 */
	public void getArtists(final DataResponse<ArrayList<Artist>> response, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				boolean albumArtistsOnly;
//				try {
//					albumArtistsOnly = info(context).getGuiSettingBool(MusicManager.this, GuiSettings.MusicLibrary.ALBUM_ARTISTS_ONLY);
//					response.value = music(context).getArtists(MusicManager.this, albumArtistsOnly);
//					onFinish(response);
//				} catch (WifiStateException e) {
//					onWrongConnectionState(e.getState());
//				}
//			}
//		});
		mHandler.post(new Command<ArrayList<Artist>>(response, this) {
			public void doRun() throws Exception{ 
				final boolean albumArtistsOnly = !info(context).getGuiSettingBool(MusicManager.this, GuiSettings.MusicLibrary.SHOW_COMPLATION_ARTISTS);
				response.value = music(context).getArtists(MusicManager.this, albumArtistsOnly);
			}
		});
	}
	
	/**
	 * Gets all artists with at least one song of a genre.
	 * @param response Response object
	 * @param genre Genre
	 */
	public void getArtists(final DataResponse<ArrayList<Artist>> response, final Genre genre, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				boolean albumArtistsOnly;
//				try {
//					albumArtistsOnly = info(context).getGuiSettingBool(MusicManager.this, GuiSettings.MusicLibrary.ALBUM_ARTISTS_ONLY);
//					response.value = music(context).getArtists(MusicManager.this, genre, albumArtistsOnly);
//					onFinish(response);
//				} catch (WifiStateException e) {
//					onWrongConnectionState(e.getState());
//				}
//			}
//		});
		mHandler.post(new Command<ArrayList<Artist>>(response, this) {
			public void doRun() throws Exception{ 
				final boolean albumArtistsOnly = !info(context).getGuiSettingBool(MusicManager.this, GuiSettings.MusicLibrary.SHOW_COMPLATION_ARTISTS);
				response.value = music(context).getArtists(MusicManager.this, genre, albumArtistsOnly);
			}
		});
	}
	
	/**
	 * Gets all artists from database
	 * @param response Response object
	 */
	public void getGenres(final DataResponse<ArrayList<Genre>> response, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				response.value = music(context).getGenres(MusicManager.this);
//				onFinish(response);
//			}
//		});
		mHandler.post(new Command<ArrayList<Genre>>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).getGenres(MusicManager.this);
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
	public void addToPlaylist(final DataResponse<Boolean> response, final Album album, final Context context) {
//		mHandler.post(new Runnable() {
//			public void run() { 
//				final IMusicClient mc = music(context);
//				final IControlClient cc = control(context);
//				final int numAlreadyQueued = mc.getPlaylistSize(MusicManager.this);
//				response.value = mc.addToPlaylist(MusicManager.this, album);
//				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
//				onFinish(response);
//			}
//		});
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				final IMusicClient mc = music(context);
				final IControlClient cc = control(context);
				final int numAlreadyQueued = mc.getPlaylistSize(MusicManager.this);
				response.value = mc.addToPlaylist(MusicManager.this, album, getSortBy(SortType.TRACK), getSortOrder());
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
			}
		});
	}
	
	/**
	 * Adds all songs of a genre to the current playlist. If current playlist is stopped,
	 * play is executed. Value is the first song of the added album.
	 * @param response Response object
	 * @param genre Genre of songs to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Genre genre, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				final IMusicClient mc = music(context);
				final IControlClient cc = control(context);
				final int numAlreadyQueued = mc.getPlaylistSize(MusicManager.this);
				response.value = mc.addToPlaylist(MusicManager.this, genre, getSortBy(SortType.ARTIST), getSortOrder());
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
			}
		});
	}
	
	/**
	 * Adds a song to the current playlist. Even if the playlist is empty, only this song will be added.
	 * @param response Response object
	 * @param album Song to add
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final Song song, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).addToPlaylist(MusicManager.this, song);
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
	public void addToPlaylist(final DataResponse<Boolean> response, final Album album, final Song song, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				final IMusicClient mc = music(context);
				final IControlClient cc = control(context);
				final int playStatus = cc.getPlayState(MusicManager.this);
				cc.setCurrentPlaylist(MusicManager.this, MusicClient.PLAYLIST_ID);
				final int playlistSize = mc.getPlaylistSize(MusicManager.this); 
				int playPos = -1;
				if (playlistSize == 0) {  // if playlist is empty, add the whole album
					int n = 0;
					for (Song albumSong : mc.getSongs(MusicManager.this, album, getSortBy(PREF_SORT_KEY_ALBUM), getSortOrder())) {
						if (albumSong.id == song.id) {
							playPos = n;
							break;
						}
						n++;
					}
					mc.addToPlaylist(MusicManager.this, album, getSortBy(PREF_SORT_KEY_ALBUM), getSortOrder());
					response.value = true;
				} else {                          // otherwise, only add the song
					mc.addToPlaylist(MusicManager.this, song);
					response.value = false;
				}
				if (playStatus == PlayStatus.STOPPED) { // if nothing is playing, play the song
					if (playPos == 0) {
						mc.playlistSetSong(MusicManager.this, playPos + 1);
						mc.playPrev(MusicManager.this);
					} else if (playPos > 0) {
						mc.playlistSetSong(MusicManager.this, playPos - 1);
						mc.playNext(MusicManager.this);
					} else {
						mc.playlistSetSong(MusicManager.this, playlistSize - 1);
						mc.playNext(MusicManager.this);
					}
				}
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
	public void addToPlaylist(final DataResponse<Boolean> response, final Artist artist, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{  
				final IMusicClient mc = music(context);
				final IControlClient cc = control(context);
				final int numAlreadyQueued = mc.getPlaylistSize(MusicManager.this);
				response.value = mc.addToPlaylist(MusicManager.this, artist, getSortBy(SortType.ALBUM), getSortOrder());
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
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
	public void addToPlaylist(final DataResponse<Boolean> response, final Artist artist, final Genre genre, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				final IMusicClient mc = music(context);
				final IControlClient cc = control(context);
				final int numAlreadyQueued = mc.getPlaylistSize(MusicManager.this);
				response.value = mc.addToPlaylist(MusicManager.this, artist, genre, getSortBy(SortType.ARTIST), getSortOrder());
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
			}
		});
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param response Response object
	 * @param position Position, starting with 0.
	 */
	public void setPlaylistSong(final DataResponse<Boolean> response, final int position, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).setPlaylistPosition(MusicManager.this, position);
			}
		});
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Position to remove, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> response, final int position, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).removeFromPlaylist(MusicManager.this, position);
			}
		});
	}

	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final DataResponse<Boolean> response, final String path, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).removeFromPlaylist(MusicManager.this, path);
			}
		});
	}
	
	/**
	 * Plays an album
	 * @param response Response object
	 * @param album Album to play
	 */
	public void play(final DataResponse<Boolean> response, final Album album, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				control(context).stop(MusicManager.this);
				response.value = music(context).play(MusicManager.this, album, getSortBy(SortType.TRACK), getSortOrder());
			}
		});
	}
	
	/**
	 * Plays all songs of a genre
	 * @param response Response object
	 * @param genre Genre of songs to play
	 */
	public void play(final DataResponse<Boolean> response, final Genre genre, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				control(context).stop(MusicManager.this);
				response.value = music(context).play(MusicManager.this, genre, getSortBy(SortType.ARTIST), getSortOrder());
			}
		});
	}
	
	/**
	 * Plays a song
	 * @param response Response object
	 * @param song Song to play
	 */
	public void play(final DataResponse<Boolean> response, final Song song, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				control(context).stop(MusicManager.this);
				response.value = music(context).play(MusicManager.this, song);
			}
		});
	}
	
	/**
	 * Plays a song, but the whole album is added to the playlist.
	 * @param response Response object
	 * @param album Album to queue
	 * @param song Song to play
	 */
	public void play(final DataResponse<Boolean> response, final Album album, final Song song, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{  
				final IMusicClient mc = music(context);
				final IControlClient cc = control(context);
				int n = 0;
				int playPos = 0;
				mc.clearPlaylist(MusicManager.this);
				for (Song albumSong : mc.getSongs(MusicManager.this, album, getSortBy(SortType.TRACK), getSortOrder())) {
					if (albumSong.id == song.id) {
						playPos = n;
						break;
					}
					n++;
				}
				cc.stop(MusicManager.this);
				mc.addToPlaylist(MusicManager.this, album, getSortBy(SortType.TRACK), getSortOrder());
				cc.setCurrentPlaylist(MusicManager.this, MusicClient.PLAYLIST_ID);
				if (playPos > 0) {
					mc.playlistSetSong(MusicManager.this, playPos - 1);
				}				
				response.value = mc.playNext(MusicManager.this);
			}
		});
	}

	/**
	 * Plays all songs from an artist
	 * @param response Response object
	 * @param artist Artist whose songs to play
	 */
	public void play(final DataResponse<Boolean> response, final Artist artist, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				control(context).stop(MusicManager.this);
				response.value = music(context).play(MusicManager.this, artist, getSortBy(SortType.ALBUM), getSortOrder());
			}
		});
	}
	
	/**
	 * Plays songs of a genre from an artist
	 * @param response Response object
	 * @param artist Artist whose songs to play
	 * @param genre  Genre filter
	 */
	public void play(final DataResponse<Boolean> response, final Artist artist, final Genre genre, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{ 
				control(context).stop(MusicManager.this);
				response.value = music(context).play(MusicManager.this, artist, genre);
			}
		});
	}
	
	
	/**
	 * Starts playing the next media in the current playlist. 
	 * @param response Response object
	 */
	public void playlistNext(final DataResponse<Boolean> response, final Context context) {
		mHandler.post(new Command<Boolean>(response, this) {
			public void doRun() throws Exception{  
				response.value = music(context).playNext(MusicManager.this);
			}
		});
	}
	
	/**
	 * Returns an array of songs on the playlist. Empty array if nothing is playing.
	 * @param response Response object
	 */
	public void getPlaylist(final DataResponse<ArrayList<String>> response, final Context context) {
		mHandler.post(new Command<ArrayList<String>>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).getPlaylist(MusicManager.this);
				final String firstEntry = response.value.get(0);
				if (firstEntry != null && firstEntry.equals("[Empty]")) {
					response.value = new ArrayList<String>();
				} 
			}
		});
	}
	
	/**
	 * Returns the position of the currently playing song in the playlist. First position is 0.
	 * @param response Response object
	 */
	public void getPlaylistPosition(final DataResponse<Integer> response, final Context context) {
		mHandler.post(new Command<Integer>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).getPlaylistPosition(MusicManager.this);
			}
		});
	}
	
	/**
	 * Updates the album object with additional data from the albuminfo table
	 * @param response Response object
	 * @param album Album to update
	 */
	public void updateAlbumInfo(final DataResponse<Album> response, final Album album, final Context context) {
		mHandler.post(new Command<Album>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).updateAlbumInfo(MusicManager.this, album);
			}
		});
	}
	
	/**
	 * Updates the artist object with additional data from the artistinfo table
	 * @param response Response object
	 * @param artist Artist to update
	 */
	public void updateArtistInfo(final DataResponse<Artist> response, final Artist artist, final Context context) {
		mHandler.post(new Command<Artist>(response, this) {
			public void doRun() throws Exception{ 
				response.value = music(context).updateArtistInfo(MusicManager.this, artist);
			}
		});
	}
	
	/**
	 * Checks if something's playing. If that's not the case, set the 
	 * playlist's play position either to the start if there were no items
	 * before, or to the first position of the newly added files.
	 * @param mc Music client
	 * @param cc Control client
	 * @param numAlreadyQueued Number of previously queued items
	 */
	private void checkForPlayAfterQueue(final IMusicClient mc, final IControlClient cc, int numAlreadyQueued) {
		final int ps = cc.getPlayState(MusicManager.this);
		if (ps == PlayStatus.STOPPED) { // if nothing is playing, play the song
			cc.setCurrentPlaylist(MusicManager.this, MusicClient.PLAYLIST_ID);
			if (numAlreadyQueued == 0) {
				mc.playNext(MusicManager.this);
			} else {
				mc.playlistSetSong(MusicManager.this, numAlreadyQueued);
			}
		}
	}

	public void onWrongConnectionState(int state) {
		// TODO Auto-generated method stub
		
	}
}