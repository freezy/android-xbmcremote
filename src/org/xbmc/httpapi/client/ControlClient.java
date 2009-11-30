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

package org.xbmc.httpapi.client;

import java.util.HashMap;

import org.xbmc.api.data.IControlClient;
import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.info.GuiActions;
import org.xbmc.httpapi.type.MediaType;
import org.xbmc.httpapi.type.SeekType;

/**
 * The ControlClient class takes care of everything related to controlling
 * XBMC. These are essentially play controls, navigation controls other actions
 * the user may wants to execute. It equally reads the information instead of
 * setting it.
 * 
 * @author Team XBMC
 */
public class ControlClient implements IControlClient {

	private final Connection mConnection;

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public ControlClient(Connection connection) {
		mConnection = connection;
	}
	
	/**
	 * Adds a file or folder (<code>fileOrFolder</code> is either a file or a folder) to the current playlist.
	 * @param fileOrFolder
	 * @return true on success, false otherwise.
	 */
	public boolean addToPlaylist(String fileOrFolder) {
		return mConnection.getBoolean("AddToPlayList", fileOrFolder);
	}
	
	/**
	 * Starts playing the media file <code>filename</code> .
	 * @param filename File to play
	 * @return true on success, false otherwise.
	 */
	public boolean playFile(String filename) {
		return mConnection.getBoolean("PlayFile", filename);
	}
	
	/**
	 * Starts playing/showing the next media/image in the current playlist or,
	 * if currently showing a slideshow, the slideshow playlist. 
	 * @return true on success, false otherwise.
	 */
	public boolean playNext() {
		return mConnection.getBoolean("PlayNext");
	}

	/**
	 * Starts playing/showing the previous media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @return true on success, false otherwise.
	 */
	public boolean playPrevious() {
		return mConnection.getBoolean("PlayPrev");
	}
	
	/**
	 * Pauses the currently playing media. 
	 * @return true on success, false otherwise.
	 */
	public boolean pause() {
		return mConnection.getBoolean("Pause");
	}
	
	/**
	 * Stops the currently playing media. 
	 * @return true on success, false otherwise.
	 */
	public boolean stop() {
		return mConnection.getBoolean("Stop");
	}
	
	/**
	 * Start playing the media file at the given URL
	 * @param url An URL pointing to a supported media file
	 * @return true on success, false otherwise.
	 */
	public boolean playUrl(String url) {
		return mConnection.getBoolean("ExecBuiltin", "PlayMedia(" + url + ")");
	}
	
	/**
	 * Sets the volume as a percentage of the maximum possible.
	 * @param volume New volume (0-100)
	 * @return true on success, false otherwise.
	 */
	public boolean setVolume(int volume) {
		return mConnection.getBoolean("SetVolume", String.valueOf(volume));
	}
	
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
	public boolean seek(SeekType type, int progress) {
		if (type.compareTo(SeekType.absolute) == 0)
			return mConnection.getBoolean("SeekPercentage", String.valueOf(progress));
		else
			return mConnection.getBoolean("SeekPercentageRelative", String.valueOf(progress));
	}
	
	/**
	 * Toggles the sound on/off.
	 * @return true on success, false otherwise.
	 */
	public boolean mute() {
		return mConnection.getBoolean("Mute");
	}
	
	/**
	 * Retrieves the current playing position of the currently playing media as
	 * a percentage of the media's length. 
	 * @return Percentage (0-100)
	 */
	public int getPercentage() {
		return mConnection.getInt("GetPercentage");
	}
	
	/**
	 * Retrieves the current volume setting as a percentage of the maximum 
	 * possible value.
	 * @return Volume (0-100)
	 */
	public int getVolume() {
		return mConnection.getInt("GetVolume");
	}
	
	/**
	 * Navigates... UP!
	 * @return true on success, false otherwise.
	 */
	public boolean navUp() {
		return mConnection.getBoolean("Action", String.valueOf(GuiActions.ACTION_MOVE_UP));
	}

	/**
	 * Navigates... DOWN!
	 * @return true on success, false otherwise.
	 */
	public boolean navDown() {
		return mConnection.getBoolean("Action", String.valueOf(GuiActions.ACTION_MOVE_DOWN));
	}
	
	/**
	 * Navigates... LEFT!
	 * @return true on success, false otherwise.
	 */
	public boolean navLeft() {
		return mConnection.getBoolean("Action", String.valueOf(GuiActions.ACTION_MOVE_LEFT));
	}
	
	/**
	 * Navigates... RIGHT!
	 * @return true on success, false otherwise.
	 */
	public boolean navRight() {
		return mConnection.getBoolean("Action", String.valueOf(GuiActions.ACTION_MOVE_RIGHT));
	}
	
	/**
	 * Selects current item.
	 * @return true on success, false otherwise.
	 */
	public boolean navSelect() {
		return mConnection.getBoolean("Action", String.valueOf(GuiActions.ACTION_SELECT_ITEM));
	}
	
	/**
	 * Takes either "video" or "music" as a parameter to begin updating the 
	 * corresponding database. 
	 * 
	 * TODO For "video" you can additionally specify a specific path to be scanned.
	 * 
	 * @param mediaType Either <code>video</code> or <code>music</code>.
	 * @return True on success, false otherwise.
	 */
	public boolean updateLibrary(String mediaType) {
		return mConnection.getBoolean("ExecBuiltin", "UpdateLibrary(" + mediaType + ")");
	}
	
	/**
	 * Broadcast a message. Used to test broadcasting feature. 
	 * @param message
	 * @return True on success, false otherwise.
	 */
	public boolean broadcast(String message) {
		return mConnection.getBoolean("Broadcast", message);
	}
	
	/**
	 * Returns the current broadcast port number, or 0 if deactivated.
	 * @return Current broadcast port number.
	 */
	public int getBroadcast() {
		final String ret[] = mConnection.getString("GetBroadcast").split(";");
		try {
			final int port = Integer.parseInt(ret[1]);
			return port > 1 && !ret[0].equals("0") ? port : 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
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
	public boolean setBroadcast(int port, int level) {
		return mConnection.getBoolean("SetBroadcast", level + ";" + port);
	}

	/**
	 * Returns current play state
	 * @return
	 */
	public PlayStatus getPlayState() {
		return PlayStatus.parse(mConnection.getString("GetCurrentlyPlaying"));
	}
	
	/**
	 * Returns state and type of the media currently playing.
	 * @return
	 */
	public ICurrentlyPlaying getCurrentlyPlaying() {
		final HashMap<String, String> map = mConnection.getPairs("GetCurrentlyPlaying");
		final IControlClient.ICurrentlyPlaying nothingPlaying = new IControlClient.ICurrentlyPlaying() {
			private static final long serialVersionUID = -1554068775915058884L;
			public boolean isPlaying() { return false; }
			public int getMediaType() { return 0; }
			public int getPlaylistPosition() { return 0; }
			public String getTitle() { return ""; }
			public int getTime() { return 0; }
			public PlayStatus getPlayStatus() { return PlayStatus.Stopped; }
			public float getPercentage() { return 0; }
			public String getFilename() { return ""; }
			public int getDuration() { return 0; }
			public String getArtist() { return ""; }
			public String getAlbum() { return ""; }
			public int getHeight() { return 0; }
			public int getWidth() { return 0; }
		};
		if (map == null)
			return nothingPlaying;
		if (map.get("Filename") != null && map.get("Filename").contains("Nothing Playing")) {
			return nothingPlaying;
		} else {
			//final int type = map.get("Type").contains("Audio") ? MediaType.MUSIC : (map.get("Type").contains("Video") ? MediaType.VIDEO : MediaType.PICTURES );
			final int type;
			if(map.get("Type").contains("Audio"))
				type = MediaType.MUSIC;
			else if(map.get("Type").contains("Video"))
				type = MediaType.VIDEO;
			else
				type = MediaType.PICTURES;
			
			switch (type) {
				case MediaType.MUSIC:
					return MusicClient.getCurrentlyPlaying(map);
				case MediaType.VIDEO:
					return VideoClient.getCurrentlyPlaying(map);
				case MediaType.PICTURES:
					return PictureClient.getCurrentlyPlaying(map);
				default:
					return nothingPlaying;
			}
		}
	}
	
	
	public boolean isConnected() {
		return mConnection.isConnected();
	}
}
