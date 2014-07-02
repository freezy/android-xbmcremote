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

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.info.GuiActions;
import org.xbmc.api.info.GuiSettings;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Host;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.SeekType;
import org.xbmc.httpapi.Connection;
import org.xbmc.httpapi.WrongDataFormatException;

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
	 * Updates host info on the connection.
	 * @param host
	 */
	public void setHost(Host host) {
		mConnection.setHost(host);
	}
	
	/**
	 * Adds a file or folder (<code>fileOrFolder</code> is either a file or a folder) to the current playlist.
	 * @param manager Manager reference
	 * @param fileOrFolder
	 * @return true on success, false otherwise.
	 */
	public boolean addToPlaylist(INotifiableManager manager, String fileOrFolder, int playlistId) {
		return mConnection.getBoolean(manager, "AddToPlayList", fileOrFolder);
	}
	
	/**
	 * Starts playing the media file <code>filename</code> .
	 * @param manager Manager reference
	 * @param filename File to play
	 * @return true on success, false otherwise.
	 */
	public boolean playFile(INotifiableManager manager, String filename, int playlistId) {
		return mConnection.getBoolean(manager, "PlayFile", filename);
	}
	
	/**
	 * Starts playing/showing the next media/image in the current playlist or,
	 * if currently showing a slideshow, the slideshow playlist. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean playNext(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "PlayNext");
	}

	/**
	 * Starts playing/showing the previous media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean playPrevious(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "PlayPrev");
	}
	
	/**
	 * Pauses the currently playing media. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean pause(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Pause");
	}
	
	/**
	 * Stops the currently playing media. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean stop(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Stop");
	}
	
	/**
	 * Start playing the media file at the given URL
	 * @param manager Manager reference
	 * @param url An URL pointing to a supported media file
	 * @return true on success, false otherwise.
	 */
	public boolean playUrl(INotifiableManager manager, String url) {
		return mConnection.getBoolean(manager, "ExecBuiltin", "PlayMedia(" + url + ")");
	}
	
	/**
	 * Show the picture file <code>filename</code> .
	 * @param manager Manager reference
	 * @param filename File to show
	 * @return true on success, false otherwise.
	 */
	public boolean showPicture(INotifiableManager manager, String filename) {
		mConnection.getBoolean(manager, "ClearSlideshow");
		mConnection.getBoolean(manager, "PlaySlideshow", filename.substring(0, filename.replaceAll("\\\\", "/").lastIndexOf("/") ) + ";false");
		mConnection.getBoolean(manager, "SlideshowSelect", filename );
		return playNext(manager);
		
	}
	
	/**
	 * Send the string <code>text</code> via keys on the virtual keyboard.
	 * @param manager Manager reference
	 * @param text The text string to send.
	 * @return true on success, false otherwise.
	 */
	public boolean sendText(INotifiableManager manager, String text) {
		final int codeOffset = 0xf100;
		for (char c : text.toCharArray()) {
			int code = (int)c+codeOffset;
			if (! mConnection.getBoolean(manager, "SendKey", Integer.toString(code))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Sets the volume as a percentage of the maximum possible.
	 * @param manager Manager reference
	 * @param volume New volume (0-100)
	 * @return true on success, false otherwise.
	 */
	public boolean setVolume(INotifiableManager manager, int volume) {
		return mConnection.getBoolean(manager, "SetVolume", String.valueOf(volume));
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
	 * @param manager Manager reference
	 * @param type     Seek type, relative or absolute
	 * @param progress Progress
	 * @return true on success, false otherwise.
	 */
	public boolean seek(INotifiableManager manager, SeekType type, int progress) {
		if (type.compareTo(SeekType.absolute) == 0)
			return mConnection.getBoolean(manager, "SeekPercentage", String.valueOf(progress));
		else
			return mConnection.getBoolean(manager, "SeekPercentageRelative", String.valueOf(progress));
	}
	
	/**
	 * Toggles the sound on/off.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean mute(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Mute");
	}
	
	/**
	 * Retrieves the current playing position of the currently playing media as
	 * a percentage of the media's length. 
	 * @param manager Manager reference
	 * @return Percentage (0-100)
	 */
	public int getPercentage(INotifiableManager manager) {
		return mConnection.getInt(manager, "GetPercentage");
	}
	
	/**
	 * Retrieves the current volume setting as a percentage of the maximum 
	 * possible value.
	 * @param manager Manager reference
	 * @return Volume (0-100)
	 */
	public int getVolume(INotifiableManager manager) {
		return mConnection.getInt(manager, "GetVolume");
	}
	
	/**
	 * Navigates... UP!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navUp(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Action", String.valueOf(GuiActions.ACTION_MOVE_UP));
	}

	/**
	 * Navigates... DOWN!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navDown(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Action", String.valueOf(GuiActions.ACTION_MOVE_DOWN));
	}
	
	/**
	 * Navigates... LEFT!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navLeft(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Action", String.valueOf(GuiActions.ACTION_MOVE_LEFT));
	}
	
	/**
	 * Navigates... RIGHT!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navRight(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Action", String.valueOf(GuiActions.ACTION_MOVE_RIGHT));
	}
	
	/**
	 * Selects current item.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navSelect(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Action", String.valueOf(GuiActions.ACTION_SELECT_ITEM));
	}
	
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
	public boolean updateLibrary(INotifiableManager manager, String mediaType) {
		return mConnection.getBoolean(manager, "ExecBuiltin", "UpdateLibrary(" + mediaType + ")");
	}
	
	/**
	 * Broadcast a message. Used to test broadcasting feature. 
	 * @param manager Manager reference
	 * @param message
	 * @return True on success, false otherwise.
	 */
	public boolean broadcast(INotifiableManager manager, String message) {
		return mConnection.getBoolean(manager, "Broadcast", message);
	}
	
	/**
	 * Returns the current broadcast port number, or 0 if deactivated.
	 * @param manager Manager reference
	 * @return Current broadcast port number.
	 */
	public int getBroadcast(INotifiableManager manager) {
		final String ret[] = mConnection.getString(manager, "GetBroadcast").split(";");
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
	 * @param manager Manager reference 
	 * @param port  Broadcast port
	 * @param level Broadcast level
	 * @return True on success, false otherwise.
	 */
	public boolean setBroadcast(INotifiableManager manager, int port, int level) {
		return mConnection.getBoolean(manager, "SetBroadcast", level + ";" + port);
	}

	/**
	 * Returns current play state
	 * @param manager Manager reference
	 * @return
	 */
	public int getPlayState(INotifiableManager manager) {
		return PlayStatus.parse(mConnection.getString(manager, "GetCurrentlyPlaying"));
	}
	
	/**
	 * Returns the current playlist identifier
	 * @param manager Manager reference
	 */
	public int getPlaylistId(INotifiableManager manager) {
		return mConnection.getInt(manager, "GetCurrentPlaylist");
	}
	
	/**
	 * Sets the current playlist identifier
	 * @param manager Manager reference
	 * @param id Playlist identifier
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistId(INotifiableManager manager, int id) {
		return mConnection.getBoolean(manager, "SetCurrentPlaylist", String.valueOf(id));
	}
	
	/**
	 * Sets the current playlist position
	 * @param manager Manager reference
	 * @param position New playlist position
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistPos(INotifiableManager manager, int playlistId, int position) {
		return mConnection.getBoolean(manager, "SetPlaylistSong", String.valueOf(position));
	}
	
	/**
	 * Clears a playlist.
	 * @param manager Manager reference
	 * @param int Playlist to clear (0 = music, 1 = video)
	 * @return True on success, false otherwise.
	 */
	public boolean clearPlaylist(INotifiableManager manager, int playlistId) {
		return mConnection.getBoolean(manager, "ClearPlayList", String.valueOf(playlistId));
	}
	
	/**
	 * Sets current playlist
	 * @param manager Manager reference
	 * @param playlistId Playlist ID ("0" = music, "1" = video)
	 * @return True on success, false otherwise.
	 */
	public boolean setCurrentPlaylist(INotifiableManager manager, int playlistId) {
		return mConnection.getBoolean(manager, "SetCurrentPlaylist", String.valueOf(playlistId));
	}	
	/**
	 * Sets the correct response format to default values
	 * @param manager Manager reference	 
	 * @return True on success, false otherwise.
	 */
	public boolean setResponseFormat(INotifiableManager manager) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("WebHeader;true;");
			sb.append("WebFooter;true;");
			sb.append("Header; ;");
			sb.append("Footer; ;");
			sb.append("OpenTag;");sb.append(Connection.LINE_SEP);sb.append(";");
			sb.append("CloseTag;\n;");
			sb.append("CloseFinalTag;false");
			mConnection.assertBoolean(manager, "SetResponseFormat", sb.toString());
			
			sb = new StringBuilder();
			sb.append("OpenRecordSet; ;");
			sb.append("CloseRecordSet; ;");
			sb.append("OpenRecord; ;");
			sb.append("CloseRecord; ;");
			sb.append("OpenField;<field>;");
			sb.append("CloseField;</field>");
			mConnection.assertBoolean(manager, "SetResponseFormat", sb.toString());
			
			return true;
		} catch (WrongDataFormatException e) {
			return false;
		}
	}
	
	/**
	 * Sets the gui setting of XBMC to value
	 * @param manager 
	 * @param setting see {@link org.xbmc.api.info.GuiSettings} for the available settings
	 * @param value the value to set
	 * @return {@code true} if the value was set successfully 
	 */
	public boolean setGuiSetting(INotifiableManager manager, final int setting, final String value) {
		return mConnection.getBoolean(manager, "SetGUISetting", GuiSettings.getType(setting) + 
				";" + GuiSettings.getName(setting) + ";" + value);
	}
	
	/**
	 * Returns state and type of the media currently playing.
	 * @return
	 */
	public ICurrentlyPlaying getCurrentlyPlaying(INotifiableManager manager) {
		final HashMap<String, String> map = mConnection.getPairs(manager, "GetCurrentlyPlaying", " ; ; ;true");
		final IControlClient.ICurrentlyPlaying nothingPlaying = new IControlClient.ICurrentlyPlaying() {
			private static final long serialVersionUID = -1554068775915058884L;
			public boolean isPlaying() { return false; }
			public int getMediaType() { return 0; }
			public int getPlaylistPosition() { return -1; }
			public String getTitle() { return ""; }
			public int getTime() { return 0; }
			public int getPlayStatus() { return PlayStatus.STOPPED; }
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
			if (map.containsKey("Type")) {
				if (map.get("Type").contains("Audio"))
					type = MediaType.MUSIC;
				else if (map.get("Type").contains("Video")) {
					if (map.get("Show Title")!= null && map.get("Episode") != null)
						type = MediaType.VIDEO_TVEPISODE;
					else	
						type = MediaType.VIDEO;
				}
				else
					type = MediaType.PICTURES;
			} else {
				return nothingPlaying;
			}
			switch (type) {
				case MediaType.MUSIC:
					return MusicClient.getCurrentlyPlaying(map);
				case MediaType.VIDEO:
					return VideoClient.getCurrentlyPlaying(map);
				case MediaType.VIDEO_TVEPISODE:
					return TvShowClient.getCurrentlyPlaying(map);
				case MediaType.PICTURES:
					return PictureClient.getCurrentlyPlaying(map);
				default:
					return nothingPlaying;
			}
		}
	}

	/**
	 * Powers off the system.
	 * @param response Response object
	 * @return true on success, false otherwise.
	 */
	public boolean powerOff(INotifiableManager manager) {
		return mConnection.getBoolean(manager, "Shutdown()");
	}
}
