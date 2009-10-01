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

package org.xbmc.android.backend.httpapi;

import java.util.ArrayList;

import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.type.ThumbSize;

import android.graphics.Bitmap;


/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * @author Team XBMC
 */
public class MusicWrapper extends Wrapper {
	
	/**
	 * Gets all albums from database
	 * @param handler Callback handler
	 */
	public void getAlbums(final HttpApiHandler<ArrayList<Album>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getAlbums();
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all songs of an album from database
	 * @param handler Callback handler
	 * @param album The album
	 */
	public void getSongs(final HttpApiHandler<ArrayList<Song>> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getSongs(album);
				done(handler);
			}
		});
	}
	
	/**
	 * Adds an album to the current playlist. If current playlist is stopped,
	 * play is executed. Value is the first song of the added album.
	 * @param handler Callback
	 * @param album Album to add
	 */
	public void addToPlaylist(final HttpApiHandler<Song> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient.PlayState ps = control(handler).getPlayState();
				handler.value = mc.addToPlaylist(album);
				if (ps == ControlClient.PlayState.Stopped) { // if nothing is playing, play the song
					mc.play(handler.value);
				}
				done(handler);
			}
		});
	}
	
	/**
	 * Adds a song to the current playlist. Even if the playlist is empty, only this song will be added.
	 * @param handler Callback
	 * @param album Song to add
	 */
	public void addToPlaylist(final HttpApiHandler<Boolean> handler, final Song song) {
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
	 * @param handler Callback
	 * @param album Song to add
	 */
	public void addToPlaylist(final HttpApiHandler<Boolean> handler, final Album album, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient.PlayState ps = control(handler).getPlayState();
				if (mc.getPlaylistSize() == 0) {  // if playlist is empty, add the whole album
					mc.addToPlaylist(album);
				} else {                          // otherwise, only add the song
					handler.value = mc.addToPlaylist(song);
				}
				if (ps == ControlClient.PlayState.Stopped) { // if nothing is playing, play the song
					handler.value = mc.play(song);
				}
				done(handler);
			}
		});
	}
	
	/**
	 * Plays an album
	 * @param handler Callback
	 * @param album Album to play
	 */
	public void play(final HttpApiHandler<Boolean> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).play(album);
				done(handler);
			}
		});
	}
	
	/**
	 * Plays a song
	 * @param handler Callback
	 * @param song Song to play
	 */
	public void play(final HttpApiHandler<Boolean> handler, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).play(song);
				done(handler);
			}
		});
	}
	
	/**
	 * Returns bitmap of an album cover
	 * @param handler Callback handler
	 */
	public void getAlbumCover(final HttpApiHandler<Bitmap> handler, final Album album, final ThumbSize size) {
		final boolean debug = false;
		mHandler.post(new Runnable() {
			public void run() {
				if (debug) System.out.println("[" + album.getId() + "] Checking in mem cache..");
				// first, try mem cache
				HttpApiMemCacheThread.get().getCover(new HttpApiHandler<Bitmap>(handler.getActivity()) {
					public void run() {
						if (value == null) {
							if (debug) System.out.println("[" + album.getId() + " empty]");
							if (debug) System.out.println("[" + album.getId() + "] Checking in disk cache..");
							// then, try sdcard cache
							HttpApiDiskCacheThread.get().getCover(new HttpApiHandler<Bitmap>(handler.getActivity()) {
								public void run() {
									if (value == null) {
										if (debug) System.out.println("[" + album.getId() + " empty]");
										if (debug) System.out.println("[" + album.getId() + "] Downloading..");
										// well, let's download
										HttpApiDownloadThread.get().getCover(new HttpApiHandler<Bitmap>(handler.getActivity()) {
											public void run() {
												if (value == null) {
													if (debug) System.out.println("[" + album.getId() + " empty]");
												} else {
													if (debug) System.out.println("[" + album.getId() + " DOWNLOADED!]");
													handler.value = value;
													done(handler);
												}
											}
										}, album, size);
										done(handler);
									} else {
										if (debug) System.out.println("[" + album.getId() + " FOUND on disk!]");
										handler.value = value;
										done(handler);
									}
								}
							}, album, size);
							
						} else {
							if (debug) System.out.println("[" + album.getId() + " FOUND in memory!]");
							handler.value = value;
							done(handler);
						}
					}
				}, album);
			}
		});
	}
}