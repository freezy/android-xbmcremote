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

/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * @author Team XBMC
 */
public class ControlWrapper extends Wrapper {
	
	/**
	 * Starts playing the media file <code>filename</code> .
	 * @param handler Wrapped boolean return value
	 * @param type File to play
	 */
	public void playFile(final HttpApiHandler<Boolean> handler, final String filename) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = control(handler).playFile(filename);
				done(handler);
			}
		});
	}

	/**
	 * Takes either "video" or "music" as a parameter to begin updating the 
	 * corresponding database.
	 * 
	 * @param handler Callback handler
	 * @param mediaType
	 */
	public void updateLibrary(final HttpApiHandler<Void> handler, final String mediaType) {
		mHandler.post(new Runnable() {
			public void run() {
				control(handler).updateLibrary(mediaType);
				done(handler);
			}
		});
	}
}
