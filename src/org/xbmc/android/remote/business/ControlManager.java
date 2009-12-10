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

import org.xbmc.android.util.ClientFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.type.SeekType;

/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * @author Team XBMC
 */
public class ControlManager extends AbstractManager implements IControlManager, INotifiableManager {
	
	/**
	 * Starts playing the media file <code>filename</code> .
	 * @param response Response object
	 * @param filename File to play
	 */
	public void playFile(final DataResponse<Boolean> response, final String filename) {
		mHandler.post(new Runnable() {
			public void run() { 
				response.value = control(response).playFile(ControlManager.this, filename);
				done(response);
			}
		});
	}
	
	/**
	 * Start playing the media file at the given URL
	 * @param response Response object
	 * @param url An URL pointing to a supported media file
	 * @return true on success, false otherwise.
	 */
	public void playUrl(final DataResponse<Boolean> response, final String url) {
		mHandler.post(new Runnable() {
			public void run() {
				response.value = control(response).playUrl(ControlManager.this, url);
				done(response);
			}
		});
	}
	
	/**
	 * Plays the next item in the playlist.
	 * @param response Response object
	 * @return true on success, false otherwise.
	 */
	public void playNext(final DataResponse<Boolean> response) {
		mHandler.post(new Runnable() {
			public void run() { 
				response.value = control(response).playNext(ControlManager.this);
				done(response);
			}
		});
	}
	
	/**
	 * Adds a file or folder (<code>fileOrFolder</code> is either a file or a folder) to the current playlist.
	 * @param response Response object
	 * @param fileOrFolder File to play
	 */
	public void addToPlaylist(final DataResponse<Boolean> response, final String fileOrFolder) {
		mHandler.post(new Runnable() {
			public void run() { 
				response.value = control(response).addToPlaylist(ControlManager.this, fileOrFolder);
				done(response);
			}
		});
	}
	
	/**
	 * Seeks to a position. If type is
	 * <ul>
	 * 	<li><code>absolute</code> - Sets the playing position of the currently 
	 *		playing media as a percentage of the media's length.</li>
	 *  <li><code>relative</code> - Adds/Subtracts the current percentage on to
	 *		the current position in the song</li>
	 * </ul> 
	 * 
	 * @param response Response object	 
	 * @param type     Seek type, relative or absolute
	 * @param progress Progress
	 * @return true on success, false otherwise.
	 */
	public void seek(final DataResponse<Boolean> response, final SeekType type, final int progress) {
		mHandler.post(new Runnable() {
			public void run() { 
				response.value = control(response).seek(ControlManager.this, type, progress);
				done(response);
			}
		});
	}

	/**
	 * Takes either "video" or "music" as a parameter to begin updating the 
	 * corresponding database.
	 * 
	 * @param response Response object
	 * @param mediaType
	 */
	public void updateLibrary(final DataResponse<Boolean> response, final String mediaType) {
		mHandler.post(new Runnable() {
			public void run() {
				response.value = control(response).updateLibrary(ControlManager.this, mediaType);
				done(response);
			}
		});
	}
	
	/**
	 * Resets the client so it has to re-read the settings and recreate the instance.
	 */
	public void resetClient() {
		ClientFactory.resetClient();
	}

	/**
	 * Returns what's currently playing.
	 * @param response
	 */
	public void getCurrentlyPlaying(final DataResponse<ICurrentlyPlaying> response) {
		mHandler.post(new Runnable() {
			public void run() {
				response.value = control(response).getCurrentlyPlaying(ControlManager.this);
				done(response);
			}
		});
	}
	
	
	/**
	 * Returns the current playlist identifier
	 * @param response Response object
	 */
	public void getPlaylistId(final DataResponse<Integer> response) {
		mHandler.post(new Runnable() {
			public void run() { 
				response.value = control(response).getPlaylistId(ControlManager.this);
				done(response);
			}
		});
	}
	
	/**
	 * Sets the current playlist identifier
	 * @param response Response object
	 * @param id Playlist identifier
	 */
	public void setPlaylistId(final DataResponse<Boolean> response, final int id) {
		mHandler.post(new Runnable() {
			public void run() { 
				response.value = control(response).setPlaylistId(ControlManager.this, id);
				done(response);
			}
		});
	}
	
	/**
	 * Sets the current playlist position
	 * @param response Response object
	 * @param position New playlist position
	 */
	public void setPlaylistPos(final DataResponse<Boolean> response, final int position) {
		mHandler.post(new Runnable() {
			public void run() { 
				response.value = control(response).setPlaylistPos(ControlManager.this, position);
				done(response);
			}
		});
	}
}