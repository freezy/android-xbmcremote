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

import java.io.Serializable;

import org.xbmc.api.type.SeekType;


/**
 * This is the interface between the business layer and the presentation layer.
 * All the business layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IControlClient {
	
	/**
	 * Adds a file or folder (<code>fileOrFolder</code> is either a file or a folder) to the current playlist.
	 * @param fileOrFolder
	 * @return true on success, false otherwise.
	 */
	public boolean addToPlaylist(String fileOrFolder);
	
	/**
	 * Starts playing the media file <code>filename</code> .
	 * @param filename File to play
	 * @return true on success, false otherwise.
	 */
	public boolean playFile(String filename);
	
	/**
	 * Starts playing/showing the next media/image in the current playlist or,
	 * if currently showing a slideshow, the slideshow playlist. 
	 * @return true on success, false otherwise.
	 */
	public boolean playNext();

	/**
	 * Starts playing/showing the previous media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return true on success, false otherwise.
	 */
	public boolean playPrevious();
	
	/**
	 * Pauses the currently playing media. 
	 * @return true on success, false otherwise.
	 */
	public boolean pause();
	
	/**
	 * Stops the currently playing media. 
	 * @return true on success, false otherwise.
	 */
	public boolean stop();
	
	/**
	 * Start playing the media file at the given URL
	 * @param url An URL pointing to a supported media file
	 * @return true on success, false otherwise.
	 */
	public boolean playUrl(String url);
	
	/**
	 * Sets the volume as a percentage of the maximum possible.
	 * @param volume New volume (0-100)
	 * @return true on success, false otherwise.
	 */
	public boolean setVolume(int volume);
	
	/**
	 * Seeks to a position. If type is
	 * <ul>
	 * 	<li><code>absolute</code> - Sets the playing position of the currently 
	 *		playing media as a percentage of the media�s length.</li>
	 *  <li><code>relative</code> - Adds/Subtracts the current percentage on to
	 *		the current position in the song</li>
	 * </ul> 
	 * @param type     Seek type, relative or absolute
	 * @param progress Progress
	 * @return true on success, false otherwise.
	 */
	public boolean seek(SeekType type, int progress);
	
	/**
	 * Toggles the sound on/off.
	 * @return true on success, false otherwise.
	 */
	public boolean mute();
	
	/**
	 * Retrieves the current playing position of the currently playing media as
	 * a percentage of the media's length. 
	 * @return Percentage (0-100)
	 */
	public int getPercentage();
	
	/**
	 * Retrieves the current volume setting as a percentage of the maximum 
	 * possible value.
	 * @return Volume (0-100)
	 */
	public int getVolume();
	
	/**
	 * Navigates... UP!
	 * @return true on success, false otherwise.
	 */
	public boolean navUp();

	/**
	 * Navigates... DOWN!
	 * @return true on success, false otherwise.
	 */
	public boolean navDown();
	
	/**
	 * Navigates... LEFT!
	 * @return true on success, false otherwise.
	 */
	public boolean navLeft();
	
	/**
	 * Navigates... RIGHT!
	 * @return true on success, false otherwise.
	 */
	public boolean navRight();
	
	/**
	 * Selects current item.
	 * @return true on success, false otherwise.
	 */
	public boolean navSelect();
	
	/**
	 * Takes either "video" or "music" as a parameter to begin updating the 
	 * corresponding database. 
	 * 
	 * TODO For "video" you can additionally specify a specific path to be scanned.
	 * 
	 * @param mediaType Either <code>video</code> or <code>music</code>.
	 * @return True on success, false otherwise.
	 */
	public boolean updateLibrary(String mediaType);
	
	/**
	 * Broadcast a message. Used to test broadcasting feature. 
	 * @param message
	 * @return True on success, false otherwise.
	 */
	public boolean broadcast(String message);
	
	/**
	 * Returns the current broadcast port number, or 0 if deactivated.
	 * @return Current broadcast port number.
	 */
	public int getBroadcast();
	
	/**
	 * Sets the brodcast level and port. Level currently only takes three values:
	 * <ul> 
	 *  	<li><code>0</code> - No broadcasts</li>
	 *  	<li><code>1</code> - Media playback and startup & shutdown events
	 *  	<li><code>2</code> - "OnAction" events (e.g. buttons) as well as level 1 events. 
	 *  </ul>
	 *  
	 * @param port  Broadcast port
	 * @param level Broadcast level
	 * @return True on success, false otherwise.
	 */
	public boolean setBroadcast(int port, int level);

	/**
	 * Returns current play state
	 * @return
	 */
	public PlayStatus getPlayState();
	
	/**
	 * Returns state and type of the media currently playing.
	 * @return
	 */
	public ICurrentlyPlaying getCurrentlyPlaying();
	
	/**
	 * Data object for "Currently playing" info.
	 * @TODO rename fields so it doesn't feel wierd using for videos..
	 * @TODO move class to objects, this is public, not data..
	 * @author Team XBMC
	 */
	public interface ICurrentlyPlaying extends Serializable {
		public PlayStatus getPlayStatus();
		public int getMediaType();
		public boolean isPlaying();
		public int getPlaylistPosition();
		
		public String getFilename();
		public String getTitle();
		
		public int getTime();
		public int getDuration();
		public float getPercentage();
		
		public String getArtist();
		public String getAlbum();
		
		public int getWidth();
		public int getHeight();
	}

	/**
	 * Describes the current play status
	 */
	public enum PlayStatus {
		Stopped,
		Paused,
		Playing;
		public static PlayStatus parse(String response) {
			if (response.contains("PlayStatus:Paused") || response.equals("Paused")) {
				return PlayStatus.Paused;
			} else if (response.contains("PlayStatus:Playing") || response.equals("Playing")) {
				return PlayStatus.Playing;
			} else {
				return PlayStatus.Stopped;
			}
		}
	}
	
	public boolean isConnected();

}