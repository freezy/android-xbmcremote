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

package org.xbmc.jsonrpc.client;

import org.codehaus.jackson.JsonNode;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Host;
import org.xbmc.api.type.SeekType;
import org.xbmc.jsonrpc.Connection;

/**
 * The ControlClient class takes care of everything related to controlling
 * XBMC. These are essentially play controls, navigation controls other actions
 * the user may wants to execute. It equally reads the information instead of
 * setting it.
 * 
 * @author Team XBMC
 */
public class ControlClient extends Client implements IControlClient {

	/**
	 * Class constructor needs reference to HTTP client connection
	 * @param connection
	 */
	public ControlClient(Connection connection) {
		super(connection);
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
		
		String type = "file";
		if(fileOrFolder.endsWith("/") || fileOrFolder.endsWith("\\"))
			type = "directory";
		
		return mConnection.getString(manager, "Playlist.Add", obj().p("playlistid", playlistId).p("item", obj().p(type, fileOrFolder))).equals("OK");
	}
	
	public boolean play(INotifiableManager manager, int playlistId){
		return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("playlistid", playlistId))).equals("OK");
	}
	/**
	 * Starts playing the media file <code>filename</code> .
	 * @param manager Manager reference
	 * @param filename File to play
	 * @return true on success, false otherwise.
	 */
	public boolean playFile(INotifiableManager manager, String filename, int playlistId) {
		
		return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("file", filename)).p("options", obj().p("resume", true))).equals("OK");
}
	
	/**
	 * Starts playing/showing the next media/image in the current playlist or,
	 * if currently showing a slideshow, the slideshow playlist. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean playNext(INotifiableManager manager) {
		return mConnection.getString(manager, "Player.GoNext", obj().p("playlistid", getPlaylistId(manager))).equals("OK");
	}

	/**
	 * Starts playing/showing the previous media/image in the current playlist
	 * or, if currently showing a slidshow, the slideshow playlist.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean playPrevious(INotifiableManager manager) {
		return mConnection.getString(manager, "Player.GoPrevious", obj().p("playlistid", getPlaylistId(manager))).equals("OK");
	}
	
	/**
	 * Pauses the currently playing media. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean pause(INotifiableManager manager) {
		mConnection.getInt(manager, "Player.PlayPause", obj().p("playerid", getActivePlayerId(manager)), "speed");
		return true;
		
	}
	
	/**
	 * Stops the currently playing media. 
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean stop(INotifiableManager manager) {
		return mConnection.getString(manager, "Player.Stop", obj().p("playerid", getActivePlayerId(manager))).equals("OK");
	}
	
	/**
	 * Start playing the media file at the given URL
	 * @param manager Manager reference
	 * @param url An URL pointing to a supported media file
	 * @return true on success, false otherwise.
	 */
	public boolean playUrl(INotifiableManager manager, String url) {
		return playFile(manager, url, 1);
	}
	
	/**
	 * Show the picture file <code>filename</code> .
	 * @param manager Manager reference
	 * @param filename File to show
	 * @return true on success, false otherwise.
	 */
	public boolean showPicture(INotifiableManager manager, String filename) {
		return playNext(manager);
		
	}
	
	/**
	 * Send the string <code>text</code> via keys on the virtual keyboard.
	 * @param manager Manager reference
	 * @param text The text string to send.
	 * @return true on success, false otherwise.
	 */
	public boolean sendText(INotifiableManager manager, String text) {
		
		boolean done = false;
		if(text.endsWith("\n")){
			text = text.substring(0, text.length()-1);
			done = true;
		}
		return mConnection.getString(manager, "Input.SendText", obj().p("text", text).p("done", done)).equals("OK");
	}
	
	/**
	 * Sets the volume as a percentage of the maximum possible.
	 * @param manager Manager reference
	 * @param volume New volume (0-100)
	 * @return true on success, false otherwise.
	 */
	public boolean setVolume(INotifiableManager manager, int volume) {
		return mConnection.getString(manager, "Application.SetVolume", obj().p("volume", volume)).equals("OK");
	}
	
	/**
	 * Seeks to a position. If type is
	 * <ul>
	 * 	<li><code>absolute</code> - Sets the playing position of the currently 
	 *		playing media as a percentage of the media�s length.</li>
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
			return mConnection.getJson(manager, "Player.Seek", obj().p("playerid", getActivePlayerId(manager)).p("value", progress)).get("percentage")!=null;
		else
			return false;//mConnection.getBoolean(manager, "SeekPercentageRelative", String.valueOf(progress));
	}
	
	/**
	 * Toggles the sound on/off.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean mute(INotifiableManager manager) {
		return mConnection.getString(manager, "Application.SetMute", obj().p("volume", 1)).equals("OK");
	}
	
	
	/**
	 * Retrieves the current playing position of the currently playing media as
	 * a percentage of the media's length. 
	 * @param manager Manager reference
	 * @return Percentage (0-100)
	 */
	public int getPercentage(INotifiableManager manager) {
		return mConnection.getInt(manager, "Player.GetProperties", obj().p("playerid", getActivePlayerId(manager)).p(PARAM_PROPERTIES, arr().add("percentage")), "percentage");
	}
	
	/**
	 * Retrieves the current volume setting as a percentage of the maximum 
	 * possible value.
	 * @param manager Manager reference
	 * @return Volume (0-100)
	 */
	public int getVolume(INotifiableManager manager) {
		return mConnection.getInt(manager, "Application.GetProperties", obj().p("playerid", getActivePlayerId(manager)).p(PARAM_PROPERTIES, arr().add("volume")), "volume");
	}
	
	/**
	 * Navigates... UP!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navUp(INotifiableManager manager) {
		return mConnection.getString(manager, "Input.Up", null).equals("OK");
	}

	/**
	 * Navigates... DOWN!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navDown(INotifiableManager manager) {
		return mConnection.getString(manager, "Input.Down", null).equals("OK");
	}
	
	/**
	 * Navigates... LEFT!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navLeft(INotifiableManager manager) {
		return mConnection.getString(manager, "Input.Left", null).equals("OK");
	}
	
	/**
	 * Navigates... RIGHT!
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navRight(INotifiableManager manager) {
		return mConnection.getString(manager, "Input.Right", null).equals("OK");
	}
	
	/**
	 * Selects current item.
	 * @param manager Manager reference
	 * @return true on success, false otherwise.
	 */
	public boolean navSelect(INotifiableManager manager) {
		return mConnection.getString(manager, "Input.Select", null).equals("OK");
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
		if(mediaType == "video")
			return mConnection.getString(manager, "VideoLibrary.Scan", null).equals("OK");
		else if(mediaType == "music")
			return mConnection.getString(manager, "AudioLibrary.Scan", null).equals("OK");
		else
			return false;
	}
	
	/**
	 * Broadcast a message. Used to test broadcasting feature. 
	 * @param manager Manager reference
	 * @param message
	 * @return True on success, false otherwise.
	 */
	public boolean broadcast(INotifiableManager manager, String message) {
		//TODO
		return false;//mConnection.getBoolean(manager, "Broadcast", message);
	}
	
	/**
	 * Returns the current broadcast port number, or 0 if deactivated.
	 * @param manager Manager reference
	 * @return Current broadcast port number.
	 */
	public int getBroadcast(INotifiableManager manager) {
		
		//TODO
		/*final String ret[] = mConnection.getString(manager, "GetBroadcast").split(";");
		try {
			final int port = Integer.parseInt(ret[1]);
			return port > 1 && !ret[0].equals("0") ? port : 0;
		} catch (NumberFormatException e) {
			return 0;
		}*/
		return 0;
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
		//TODO
		return false;//mConnection.getBoolean(manager, "SetBroadcast", level + ";" + port);
	}

	/**
	 * Returns current play state
	 * @param manager Manager reference
	 * @return
	 */
	public int getPlayState(INotifiableManager manager) {
		return mConnection.getInt(manager, "Application.GetProperties", obj().p("playerid", getActivePlayerId(manager)).p(PARAM_PROPERTIES, arr().add("speed")), "speed");
	}
	
	/**
	 * Returns the current playlist identifier
	 * @param manager Manager reference
	 */
	public int getPlaylistId(INotifiableManager manager) {
		return mConnection.getInt(manager, "Player.GetProperties", obj().p("playerid", getActivePlayerId(manager)).p(PARAM_PROPERTIES, arr().add("playlistid")), "playlistid");
	}
	
	/**
	 * Sets the current playlist identifier
	 * @param manager Manager reference
	 * @param id Playlist identifier
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistId(INotifiableManager manager, int id) {
		return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("playlistid", id))).equals("OK");
	}
	
	/**
	 * Sets the current playlist position
	 * @param manager Manager reference0
	 * @param position New playlist position
	 * @return True on success, false otherwise.
	 */
	public boolean setPlaylistPos(INotifiableManager manager, int playlistId, int position) {
		int playerid = getActivePlayerId(manager);
		int currentplaylistid = getPlaylistId(manager);
		
		
		if(playerid == -1 || currentplaylistid != playlistId)
			return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("playlistid", playlistId).p("position", position))).equals("OK");
		else
			return mConnection.getString(manager, "Player.GoTo", obj().p("playerid", getActivePlayerId(manager)).p("position", position)).equals("OK");
	}
	
	/**
	 * Clears a playlist.
	 * @param manager Manager reference
	 * @param int Playlist to clear (0 = music, 1 = video)
	 * @return True on success, false otherwise.
	 */
	public boolean clearPlaylist(INotifiableManager manager, int playlistId) {
		return mConnection.getString(manager, "Playlist.Clear", obj().p("playlistid", playlistId)).equals("OK");
	}
	
	/**
	 * Sets current playlist
	 * @param manager Manager reference
	 * @param playlistId Playlist ID ("0" = music, "1" = video)
	 * @return True on success, false otherwise.
	 */
	public boolean setCurrentPlaylist(INotifiableManager manager, int playlistId) {
		return mConnection.getString(manager, "Player.Open", obj().p("item", obj().p("playlistid", playlistId))).equals("OK");
	}
	
	/**
	 * Sets the correct response format to default values
	 * @param manager Manager reference	 
	 * @return True on success, false otherwise.
	 */
	
	/**
	 * Sets the gui setting of XBMC to value
	 * @param manager 
	 * @param setting see {@link org.xbmc.api.info.GuiSettings} for the available settings
	 * @param value the value to set
	 * @return {@code true} if the value was set successfully 
	 */
	public boolean setGuiSetting(INotifiableManager manager, final int setting, final String value) {
		return false;//mConnection.getBoolean(manager, "SetGUISetting", GuiSettings.getType(setting) + ";" + GuiSettings.getName(setting) + ";" + value);
	}
	
	/**
	 * Returns state and type of the media currently playing.
	 * @return
	 */
	public ICurrentlyPlaying getCurrentlyPlaying(INotifiableManager manager) {
		
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
		final JsonNode active = mConnection.getJson(manager, "Player.GetActivePlayers", null);
		if(active.size() == 0)
			return nothingPlaying;
		
		int playerid = getActivePlayerId(manager);
		
		final JsonNode player_details = mConnection.getJson(manager, "Player.GetProperties", obj().p("playerid", playerid).p(PARAM_PROPERTIES, arr().add("percentage").add("position").add("speed").add("time").add("totaltime").add("type")));
		
		if(player_details != null){
			
			final JsonNode file_details = mConnection.getJson(manager, "Player.GetItem", obj().p("playerid", playerid).p(PARAM_PROPERTIES, arr().add("artist").add("album").add("duration").add("episode").add("genre").add("file").add("season").add("showtitle").add("tagline").add("title"))).get("item");
		
			if(file_details.get("Filename") != null && file_details.get("Filename").getTextValue().contains("Nothing Playing")) {
				return nothingPlaying;
			}
			
		
			if(getString(file_details, "type").equals("episode")){
				return TvShowClient.getCurrentlyPlaying(player_details, file_details);			
			}
			else if(getString(player_details, "type").equals("video")){
				return VideoClient.getCurrentlyPlaying(player_details, file_details);			
			}
			if(getString(player_details, "type").equals("audio")){
				return MusicClient.getCurrentlyPlaying(player_details, file_details);
			}
			else
				return nothingPlaying;
		}
		else
			return nothingPlaying;
	}
	
	public static int parseTime(JsonNode node) {
		
		int time=0;
		time += node.get("hours").getIntValue() * 3600;
		time += node.get("minutes").getIntValue() * 60;
		time += node.get("seconds").getIntValue();
		
		return time;
		
	}
}

