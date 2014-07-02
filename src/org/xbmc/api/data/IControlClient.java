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

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.type.SeekType;


/**
 * This is the interface between the business layer and the presentation layer.
 * All the business layer gets to see is this interface.
 *  
 * @author Team XBMC
 */
public interface IControlClient extends IClient {
	
	/**
	 * Adds a file or folder (<code>fileOrFolder</code> is either a file or a folder) to the current playlist.
	 * @param manager Manager reference
	 * @param fileOrFolder
	 * @return true on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, String fileOrFolder, int playlistType);
	
	/**
	 * Starts playing the media file <code>filename</code> .
	 * @param manager Manager reference
	 * @param filename File to play
	 * @return true on success, false otherwise.
	 */
	public boolean playFile(INotifiableManager manager, String filename, int playlistType);
	
	/**
	 * Starts playing/showing the next media/image in the current playlist or,
	 * if currently showing a slideshow, the slideshow playlist. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean playNext(INotifiableManager manager);

	/**
	 * Starts playing/showing the previous media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean playPrevious(INotifiableManager manager);
	
	/**
	 * Pauses the currently playing media. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean pause(INotifiableManager manager);
	
	/**
	 * Powers off the system.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean powerOff(INotifiableManager manager);

	/**
	 * Stops the currently playing media. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean stop(INotifiableManager manager);
	
	/**
	 * Start playing the media file at the given URL
	 * @param manager Manager reference
	 * @param url An URL pointing to a supported media file
	 * @return true on success, false otherwise.
	 */
	public boolean playUrl(INotifiableManager manager, String url);
	
	/**
	 * Sets the volume as a percentage of the maximum possible.
	 * @param manager Manager reference
	 * @param volume New volume (0-100)
	 * @return true on success, false otherwise.
	 */
	public boolean setVolume(INotifiableManager manager, int volume);
	
	/**
	 * Seeks to a position. If type is
	 * <ul>
	 * 	<li><code>absolute</code> - Sets the playing position of the currently 
	 *		playing media as a percentage of the media's length.</li>
	 *  <li><code>relative</code> - Adds/Subtracts the current percentage on to
	 *		the current position in the song</li>
	 * </ul> 
	 * @param manager Manager reference
	 * @param type     Seek type, relative or absolute
	 * @param progress Progress
	 * @return true on success, false otherwise.
	 */
	public boolean seek(INotifiableManager manager, SeekType type, int progress);
	
	/**
	 * Send the string <code>text</code> via keys on the virtual keyboard.
	 * @param manager Manager reference
	 * @param text The text string to send.
	 * @return true on success, false otherwise.
	 */
	public boolean sendText(INotifiableManager manager, String text);
	
	/**
	 * Toggles the sound on/off.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean mute(INotifiableManager manager);
	
	/**
	 * Retrieves the current playing position of the currently playing media as
	 * a percentage of the media's length. 
	 * @param manager Manager reference
	 * @return Percentage (0-100)
	 */
	public int getPercentage(INotifiableManager manager);
	
	/**
	 * Retrieves the current volume setting as a percentage of the maximum 
	 * possible value.
	 * @param manager Manager reference
	 * @return Volume (0-100)
	 */
	public int getVolume(INotifiableManager manager);
	
	/**
	 * Navigates... UP!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navUp(INotifiableManager manager);

	/**
	 * Navigates... DOWN!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navDown(INotifiableManager manager);
	
	/**
	 * Navigates... LEFT!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navLeft(INotifiableManager manager);
	
	/**
	 * Navigates... RIGHT!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navRight(INotifiableManager manager);
	
	/**
	 * Selects current item.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navSelect(INotifiableManager manager);
	
	/**
	 * Takes either "video" or "music" as a parameter to begin updating the 
	 * corresponding database. 
	 * 
	 * TODO For "video" you can additionally specify a specific path to be scanned.
	 * 
	 * @param manager Manager reference
	 * @param mediaType Either <code>video</code> or <code>music</code>.
	 * @return True on success, false otherwise.
	 */
	public boolean updateLibrary(INotifiableManager manager, String mediaType);
	
	/**
	 * Show the picture file <code>filename</code>.
	 * @param manager Manager reference
	 * @param filename File to show
	 * @return true on success, false otherwise.
	 */
	public boolean showPicture(INotifiableManager manager, String filename);
	
	/**
	 * Broadcast a message. Used to test broadcasting feature. 
	 * @param manager Manager reference
	 * @param message
	 * @return True on success, false otherwise.
	 */
	public boolean broadcast(INotifiableManager manager, String message);
	
	/**
	 * Returns the current broadcast port number, or 0 if deactivated.
	 * @param manager Manager reference
	 * @return Current broadcast port number.
	 */
	public int getBroadcast(INotifiableManager manager);
	
	/**
	 * Sets the brodcast level and port. Level currently only takes three values:
	 * <ul> 
	 *  	<li><code>0</code> - No broadcasts</li>
	 *  	<li><code>1</code> - Media playback and startup & shutdown events
	 *  	<li><code>2</code> - "OnAction" events (e.g. buttons) as well as level 1 events. 
	 *  </ul>
	 *
	 * @param manager Manager reference
	 * @param port  Broadcast port
	 * @param level Broadcast level
	 * @return True on success, false otherwise.
	 */
	public boolean setBroadcast(INotifiableManager manager, int port, int level);

	/**
	 * Returns current play state
	 * @param manager Manager reference
	 * @return
	 */
	public int getPlayState(INotifiableManager manager);
	
	/**
	 * Returns the current playlist identifier
	 * @param manager Manager reference
	 */
	public int getPlaylistId(INotifiableManager manager);
	
	/**
	 * Sets current playlist
	 * @param manager Manager reference
	 * @param playlistId Playlist ID ("0" = music, "1" = video)
	 * @return True on success, false otherwise.
	 */
	public boolean setCurrentPlaylist(INotifiableManager manager, int playlistId);
	
	/**
	 * Sets the current playlist identifier
	 * @param manager Manager reference
	 * @param id Playlist identifier
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistId(INotifiableManager manager, int id);
	
	/**
	 * Sets the current playlist position
	 * @param manager Manager reference
	 * @param position New playlist position
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistPos(INotifiableManager manager, int playlistId, int position);
	
	/**
	 * Clears a playlist.
	 * @param manager Manager reference
	 * @param playlistId Playlist ID to clear (0 = music, 1 = video)
	 * @return True on success, false otherwise.
	 */
	public boolean clearPlaylist(INotifiableManager manager, int playlistId);
	
	/**
	 * Returns state and type of the media currently playing.
	 * @return
	 */
	public ICurrentlyPlaying getCurrentlyPlaying(INotifiableManager manager);
	
	/**
	 * Sets the gui setting of XBMC to value
	 * @param manager 
	 * @param setting see {@link org.xbmc.api.info.GuiSettings} for the available settings
	 * @param value the value to set
	 * @return {@code true} if the value was set successfully 
	 */
	public boolean setGuiSetting(INotifiableManager manager, final int setting, final String value);
	
	/**
	 * Data object for "Currently playing" info.
	 * @TODO rename fields so it doesn't feel wierd using for videos..
	 * @TODO move class to objects, this is public, not data..
	 * @author Team XBMC
	 */
	public interface ICurrentlyPlaying extends Serializable {
		public int getPlayStatus();
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
}