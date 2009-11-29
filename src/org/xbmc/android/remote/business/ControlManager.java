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

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;

/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * @author Team XBMC
 */
public class ControlManager extends AbstractManager implements IControlManager {
	
	/**
	 * Starts playing the media file <code>filename</code> .
	 * @param response Response object
	 * @param filename File to play
	 */
	public void playFile(final DataResponse<Boolean> response, final String filename) {
		mHandler.post(new Runnable() {
			public void run() { 
				response.value = control(response).playFile(filename);
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
				response.value = control(response).addToPlaylist(fileOrFolder);
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
				response.value = control(response).updateLibrary(mediaType);
				done(response);
			}
		});
	}
}
